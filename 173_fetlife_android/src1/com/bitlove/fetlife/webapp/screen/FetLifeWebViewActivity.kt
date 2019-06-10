package com.bitlove.fetlife.webapp.screen

import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import com.bitlove.fetlife.FetLifeApplication
import com.bitlove.fetlife.R
import com.bitlove.fetlife.inbound.onesignal.NotificationParser
import com.bitlove.fetlife.view.screen.BaseActivity
import com.bitlove.fetlife.view.screen.component.MenuActivityComponent
import com.bitlove.fetlife.view.screen.resource.ResourceActivity
import com.bitlove.fetlife.view.screen.standalone.LoginActivity
import com.bitlove.fetlife.webapp.kotlin.getBooleanExtra
import com.bitlove.fetlife.webapp.kotlin.getStringExtra
import com.bitlove.fetlife.webapp.navigation.WebAppNavigation
import com.crashlytics.android.Crashlytics

class FetLifeWebViewActivity : ResourceActivity() {

    override fun onCreateActivityComponents() {
        addActivityComponent(MenuActivityComponent())
    }

    override fun onSetContentView() {
        setContentView(R.layout.webapp_activity_webview)
    }

    companion object {
        private const val EXTRA_PAGE_URL = "EXTRA_PAGE_URL"
        private const val EXTRA_HAS_BOTTOM_NAVIGATION = BaseActivity.EXTRA_HAS_BOTTOM_BAR
        private const val EXTRA_SELECTED_BOTTOM_NAV_ITEM = BaseActivity.EXTRA_SELECTED_BOTTOM_NAV_ITEM

        fun startActivity(context: Context, pageUrl: String, hasBottomNavigation: Boolean = false, selectedBottomNavigationItem: Int? = null, newTask: Boolean = false, options: Bundle?) {
            context.startActivity(createIntent(context, pageUrl, hasBottomNavigation, selectedBottomNavigationItem, newTask), options)
        }

        fun createIntent(context: Context, pageUrl: String, hasBottomNavigation: Boolean, selectedBottomNavigationItem: Int?, newTask: Boolean): Intent {
            return Intent(context, FetLifeWebViewActivity::class.java).apply {
                val pageUri = Uri.parse(pageUrl)
                if (pageUri.isAbsolute) {
                    putExtra(EXTRA_PAGE_URL, pageUrl)
                } else {
                    putExtra(EXTRA_PAGE_URL, WebAppNavigation.WEBAPP_BASE_URL + "/" + pageUrl)
                }
                putExtra(EXTRA_HAS_BOTTOM_NAVIGATION, hasBottomNavigation)
                putExtra(EXTRA_SELECTED_BOTTOM_NAV_ITEM, selectedBottomNavigationItem)
                flags = if (newTask) {
                    FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_NO_ANIMATION
                } else {
                    FLAG_ACTIVITY_NO_ANIMATION
                }
            }
        }
    }

    override fun verifyUser(): Boolean {
        return if (FetLifeApplication.getInstance().webAppNavigation.isResourceUrl(getStringExtra(EXTRA_PAGE_URL))) {
            super.verifyUser()
        } else {
            true
        }
    }

    override fun onResourceCreate(savedInstanceState: Bundle?) {
//        setContentView(R.layout.webapp_activity_webview)

        var hasHomeNavigation = getBooleanExtra(EXTRA_HAS_BOTTOM_NAVIGATION) != true
        var pageUrl = getStringExtra(EXTRA_PAGE_URL)

        if (pageUrl == null) {
            pageUrl = intent.data?.toString()
            hasHomeNavigation = true
            // if opened externally, logout user for security reasons
            FetLifeApplication.getInstance().userSessionManager.onUserLogOut()
        }

        if (pageUrl == null) {
            // In theory this should not happen, but based on some stackoverflow threads there are cases when the intent is null, usually when the App is updated and there is a dying App till in the memory
            // If this is the case it is safe to ignore null cases, and just make sure the App does not crash, but just o be sure these cases should be monitored for a while
            Crashlytics.log("$javaClass.simpleName: nullIntent? ${intent == null}; nullExtra? ${intent?.extras == null}; hasUrlExtra? ${intent?.hasExtra(EXTRA_PAGE_URL)}")
            Crashlytics.logException(Exception("Page Url is null"))
            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.content_layout, FetLifeWebViewFragment.newInstance(pageUrl, hasHomeNavigation), "FetLifeWebViewFragment")
                    .commit()
        }
    }

    override fun onResourceStart() {
        var pageUrl = getStringExtra(EXTRA_PAGE_URL)
        NotificationParser.Companion.clearNotificationTypeForUrl(pageUrl)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                var pageUrl = getStringExtra(EXTRA_PAGE_URL)
                if (pageUrl == null) {
                    LoginActivity.startLogin(fetLifeApplication)
                }
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event)
        }

        val wentBack = (supportFragmentManager.fragments.getOrNull(0) as? FetLifeWebViewFragment)?.onKeyBack()
        return if (wentBack == true) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun getFabLink(): String? {
        return (supportFragmentManager.fragments.getOrNull(0) as? FetLifeWebViewFragment)?.getFabLink()
                ?: FetLifeApplication.getInstance().webAppNavigation.getFabLink(getStringExtra(EXTRA_PAGE_URL))
    }

    fun getCurrentUrl(): String? {
        return (supportFragmentManager.fragments.getOrNull(0) as? FetLifeWebViewFragment)?.getCurrentUrl()
    }


}