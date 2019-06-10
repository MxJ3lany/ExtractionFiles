package com.topjohnwu.magisk.ui.base

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import com.skoumal.teanity.viewevents.ViewEvent
import com.topjohnwu.magisk.Config
import com.topjohnwu.magisk.model.events.BackPressEvent
import com.topjohnwu.magisk.model.events.PermissionEvent
import com.topjohnwu.magisk.model.events.ViewActionEvent
import com.topjohnwu.magisk.model.navigation.MagiskAnimBuilder
import com.topjohnwu.magisk.model.navigation.MagiskNavigationEvent
import com.topjohnwu.magisk.model.navigation.Navigator
import com.topjohnwu.magisk.model.permissions.PermissionRequestBuilder
import com.topjohnwu.magisk.utils.LocaleManager
import com.topjohnwu.magisk.utils.Utils
import timber.log.Timber
import kotlin.reflect.KClass


abstract class MagiskActivity<ViewModel : MagiskViewModel, Binding : ViewDataBinding> :
    MagiskLeanbackActivity<ViewModel, Binding>(), FragNavController.RootFragmentListener,
    Navigator, FragNavController.TransactionListener {

    override val numberOfRootFragments: Int get() = baseFragments.size
    override val baseFragments: List<KClass<out Fragment>> = listOf()

    protected open val defaultPosition: Int = 0

    protected val navigationController get() = if (navHostId == 0) null else _navigationController
    private val _navigationController by lazy {
        if (navHostId == 0) throw IllegalStateException("Did you forget to override \"navHostId\"?")
        FragNavController(supportFragmentManager, navHostId)
    }

    private val isRootFragment
        get() = navigationController?.let { it.currentStackIndex != defaultPosition } ?: false

    init {
        val isDarkTheme = Config.get<Boolean>(Config.Key.DARK_THEME)
        val theme = if (isDarkTheme) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(theme)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun applyOverrideConfiguration(config: Configuration?) {
        // Force applying our preferred local
        config?.setLocale(LocaleManager.locale)
        super.applyOverrideConfiguration(config)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.getLocaleContext(base, LocaleManager.locale))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationController?.apply {
            rootFragmentListener = this@MagiskActivity
            transactionListener = this@MagiskActivity
            initialize(defaultPosition, savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        navigationController?.onSaveInstanceState(outState)
    }

    @CallSuper
    override fun onEventDispatched(event: ViewEvent) {
        super.onEventDispatched(event)
        when (event) {
            is BackPressEvent -> onBackPressed()
            is MagiskNavigationEvent -> navigateTo(event)
            is ViewActionEvent -> event.action(this)
            is PermissionEvent -> withPermissions(*event.permissions.toTypedArray()) {
                onSuccess { event.callback.onNext(true) }
                onFailure {
                    event.callback.onNext(false)
                    event.callback.onError(SecurityException("User refused permissions"))
                }
            }
        }
    }

    override fun getRootFragment(index: Int) = baseFragments[index].java.newInstance()

    override fun navigateTo(event: MagiskNavigationEvent) {
        val directions = event.navDirections

        navigationController?.defaultTransactionOptions = FragNavTransactionOptions.newBuilder()
            .customAnimations(event.animOptions)
            .build()

        navigationController?.currentStack
            ?.indexOfFirst { it.javaClass == event.navOptions.popUpTo }
            ?.let { if (it == -1) null else it } // invalidate if class is not found
            ?.let { if (event.navOptions.inclusive) it + 1 else it }
            ?.let { navigationController?.popFragments(it) }

        when (directions.isActivity) {
            true -> navigateToActivity(event)
            else -> navigateToFragment(event)
        }
    }

    private fun navigateToActivity(event: MagiskNavigationEvent) {
        val destination = event.navDirections.destination?.java ?: let {
            Timber.e("Cannot navigate to null destination")
            return
        }
        val options = event.navOptions

        Intent(this, destination)
            .putExtras(event.navDirections.args)
            .apply {
                if (options.singleTop) addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                if (options.clearTask) addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            .let { startActivity(it) }
    }

    private fun navigateToFragment(event: MagiskNavigationEvent) {
        val destination = event.navDirections.destination?.java ?: let {
            Timber.e("Cannot navigate to null destination")
            return
        }

        when (val index = baseFragments.indexOfFirst { it.java.name == destination.name }) {
            -1 -> destination.newInstance()
                .apply { arguments = event.navDirections.args }
                .let { navigationController?.pushFragment(it) }
            // When it's desired that fragments of same class are put on top of one another edit this
            else -> navigationController?.switchTab(index)
        }
    }

    override fun onBackPressed() {
        val fragment = navigationController?.currentFrag as? MagiskFragment<*, *>

        if (fragment?.onBackPressed() == true) {
            return
        }

        try {
            navigationController?.popFragment() ?: throw UnsupportedOperationException()
        } catch (e: UnsupportedOperationException) {
            when {
                isRootFragment -> {
                    val options = FragNavTransactionOptions.newBuilder()
                        .transition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        .build()
                    navigationController?.switchTab(defaultPosition, options)
                }
                else -> super.onBackPressed()
            }
        }
    }

    override fun onFragmentTransaction(
        fragment: Fragment?,
        transactionType: FragNavController.TransactionType
    ) = Unit

    override fun onTabTransaction(fragment: Fragment?, index: Int) = Unit

    fun openUrl(url: String) = Utils.openLink(this, url.toUri())

    fun withPermissions(vararg permissions: String, builder: PermissionRequestBuilder.() -> Unit) {
        val request = PermissionRequestBuilder().apply(builder).build()
        Dexter.withActivity(this)
            .withPermissions(*permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) =
                    if (report?.areAllPermissionsGranted() == true) {
                        request.onSuccess()
                    } else {
                        request.onFailure()
                    }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) = request.onShowRationale(permissions.orEmpty().map { it.name })
            })
            .check()
    }

    private fun FragNavTransactionOptions.Builder.customAnimations(options: MagiskAnimBuilder) =
        customAnimations(options.enter, options.exit, options.popEnter, options.popExit).apply {
            if (!options.anySet) {
                transition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }
        }

}
