package com.hiroshi.cimoc.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.component.ThemeResponsive;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.MainPresenter;
import com.hiroshi.cimoc.ui.fragment.BaseFragment;
import com.hiroshi.cimoc.ui.fragment.ComicFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.fragment.recyclerview.SourceFragment;
import com.hiroshi.cimoc.ui.fragment.recyclerview.TagFragment;
import com.hiroshi.cimoc.ui.view.MainView;
import com.hiroshi.cimoc.utils.HintUtils;
import com.hiroshi.cimoc.utils.PermissionUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class MainActivity extends BaseActivity implements MainView, NavigationView.OnNavigationItemSelectedListener {

    private static final int DIALOG_REQUEST_NOTICE = 0;
    private static final int DIALOG_REQUEST_PERMISSION = 1;

    private static final int REQUEST_ACTIVITY_SETTINGS = 0;

    private static final int FRAGMENT_NUM = 3;

    @BindView(R.id.main_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.main_navigation_view) NavigationView mNavigationView;
    @BindView(R.id.main_fragment_container) FrameLayout mFrameLayout;
    private TextView mLastText;
    private SimpleDraweeView mDraweeView;
    private ControllerBuilderProvider mControllerBuilderProvider;

    private MainPresenter mPresenter;
    private ActionBarDrawerToggle mDrawerToggle;
    private long mExitTime = 0;
    private long mLastId = -1;
    private int mLastSource = -1;
    private String mLastCid;

    private int mCheckItem;
    private SparseArray<BaseFragment> mFragmentArray;
    private BaseFragment mCurrentFragment;
    private boolean night;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new MainPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        initDrawerToggle();
        initNavigation();
        initFragment();
    }

    @Override
    protected void initData() {
        mPresenter.loadLast();
        checkUpdate();
        if (!showAuthorNotice()) {
            showPermission();
        }
    }

    private void initDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (refreshCurrentFragment()) {
                    getFragmentManager().beginTransaction().show(mCurrentFragment).commit();
                } else {
                    getFragmentManager().beginTransaction().add(R.id.main_fragment_container, mCurrentFragment).commit();
                }
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void initNavigation() {
        night = mPreference.getBoolean(PreferenceManager.PREF_NIGHT, false);
        mNavigationView.getMenu().findItem(R.id.drawer_night).setTitle(night ? R.string.drawer_light : R.string.drawer_night);
        mNavigationView.setNavigationItemSelectedListener(this);
        View header = mNavigationView.getHeaderView(0);
        mLastText = ButterKnife.findById(header, R.id.drawer_last_title);
        mDraweeView = ButterKnife.findById(header, R.id.drawer_last_cover);
        mLastText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPresenter.checkLocal(mLastId)) {
                    Intent intent = TaskActivity.createIntent(MainActivity.this, mLastId);
                    startActivity(intent);
                } else if (mLastSource != -1 && mLastCid != null) {
                    Intent intent = DetailActivity.createIntent(MainActivity.this, null, mLastSource, mLastCid);
                    startActivity(intent);
                } else {
                    HintUtils.showToast(MainActivity.this, R.string.common_execute_fail);
                }
            }
        });
        mControllerBuilderProvider = new ControllerBuilderProvider(this,
                SourceManager.getInstance(this).new HeaderGetter(), false);
    }

    private void initFragment() {
        int home = mPreference.getInt(PreferenceManager.PREF_OTHER_LAUNCH, PreferenceManager.HOME_FAVORITE);
        switch (home) {
            default:
            case PreferenceManager.HOME_FAVORITE:
            case PreferenceManager.HOME_HISTORY:
            case PreferenceManager.HOME_DOWNLOAD:
                mCheckItem = R.id.drawer_comic;
                break;
            case PreferenceManager.HOME_SOURCE:
                mCheckItem = R.id.drawer_source;
                break;
            case PreferenceManager.HOME_TAG:
                mCheckItem = R.id.drawer_tag;
                break;
        }
        mNavigationView.setCheckedItem(mCheckItem);
        mFragmentArray = new SparseArray<>(FRAGMENT_NUM);
        refreshCurrentFragment();
        getFragmentManager().beginTransaction().add(R.id.main_fragment_container, mCurrentFragment).commit();
    }

    private boolean refreshCurrentFragment() {
        mCurrentFragment = mFragmentArray.get(mCheckItem);
        if (mCurrentFragment == null) {
            switch (mCheckItem) {
                case R.id.drawer_comic:
                    mCurrentFragment = new ComicFragment();
                    break;
                case R.id.drawer_source:
                    mCurrentFragment = new SourceFragment();
                    break;
                case R.id.drawer_tag:
                    mCurrentFragment = new TagFragment();
                    break;
            }
            mFragmentArray.put(mCheckItem, mCurrentFragment);
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mControllerBuilderProvider.clear();
        ((App) getApplication()).getBuilderProvider().clear();
        ((App) getApplication()).getGridRecycledPool().clear();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {}

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (System.currentTimeMillis() - mExitTime > 2000) {
            HintUtils.showToast(this, R.string.main_double_click);
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId != mCheckItem) {
            switch (itemId) {
                case R.id.drawer_comic:
                case R.id.drawer_source:
                case R.id.drawer_tag:
                    mCheckItem = itemId;
                    getFragmentManager().beginTransaction().hide(mCurrentFragment).commit();
                    if (mToolbar != null) {
                        mToolbar.setTitle(item.getTitle().toString());
                    }
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    break;
                case R.id.drawer_night:
                    onNightSwitch();
                    mPreference.putBoolean(PreferenceManager.PREF_NIGHT, night);
                    break;
                case R.id.drawer_settings:
                    startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), REQUEST_ACTIVITY_SETTINGS);
                    break;
                case R.id.drawer_about:
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    break;
                case R.id.drawer_backup:
                    startActivity(new Intent(MainActivity.this, BackupActivity.class));
                    break;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ACTIVITY_SETTINGS:
                    int[] result = data.getIntArrayExtra(Extra.EXTRA_RESULT);
                    if (result[0] == 1) {
                        changeTheme(result[1], result[2], result[3]);
                    }
                    if (result[4] == 1 && mNightMask != null) {
                        mNightMask.setBackgroundColor(result[5] << 24);
                    }
                    break;
            }
        }
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_NOTICE:
                mPreference.putBoolean(PreferenceManager.PREF_MAIN_NOTICE, true);
                showPermission();
                break;
            case DIALOG_REQUEST_PERMISSION:
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((App) getApplication()).initRootDocumentFile();
                    HintUtils.showToast(this, R.string.main_permission_success);
                } else {
                    HintUtils.showToast(this, R.string.main_permission_fail);
                }
                break;
        }
    }

    @Override
    public void onNightSwitch() {
        night = !night;
        mNavigationView.getMenu().findItem(R.id.drawer_night).setTitle(night ? R.string.drawer_light : R.string.drawer_night);
        if (mNightMask != null) {
            mNightMask.setVisibility(night ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onUpdateReady() {
        HintUtils.showToast(this, R.string.main_ready_update);
    }

    @Override
    public void onLastLoadSuccess(long id, int source, String cid, String title, String cover) {
        onLastChange(id, source, cid, title,cover);
    }

    @Override
    public void onLastLoadFail() {
        HintUtils.showToast(this, R.string.main_last_read_fail);
    }

    @Override
    public void onLastChange(long id, int source, String cid, String title, String cover) {
        mLastId = id;
        mLastSource = source;
        mLastCid = cid;
        mLastText.setText(title);
        ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(cover))
                .setResizeOptions(new ResizeOptions(App.mWidthPixels, App.mHeightPixels))
                .build();
        DraweeController controller = mControllerBuilderProvider.get(source)
                .setOldController(mDraweeView.getController())
                .setImageRequest(request)
                .build();
        mDraweeView.setController(controller);
    }

    private void changeTheme(@StyleRes int theme, @ColorRes int primary, @ColorRes int accent) {
        setTheme(theme);
        ColorStateList itemList = new ColorStateList(new int[][]{{ -android.R.attr.state_checked },
                { android.R.attr.state_checked }},
                new int[]{Color.BLACK, ContextCompat.getColor(this, accent)});
        mNavigationView.setItemTextColor(itemList);
        ColorStateList iconList = new ColorStateList(new int[][]{{ -android.R.attr.state_checked },
                { android.R.attr.state_checked }},
                new int[]{0x8A000000, ContextCompat.getColor(this, accent)});
        mNavigationView.setItemIconTintList(iconList);
        mNavigationView.getHeaderView(0).setBackgroundColor(ContextCompat.getColor(this, primary));
        if (mToolbar != null) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(this, primary));
        }

        for (int i = 0; i < mFragmentArray.size(); ++i) {
            ((ThemeResponsive) mFragmentArray.valueAt(i)).onThemeChange(primary, accent);
        }
    }

    private boolean showAuthorNotice() {
        if (!mPreference.getBoolean(PreferenceManager.PREF_MAIN_NOTICE, false)) {
            MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.main_notice,
                    R.string.main_notice_content, false, DIALOG_REQUEST_NOTICE);
            fragment.show(getFragmentManager(), null);
            return true;
        }
        return false;
    }

    private void showPermission() {
        if (!PermissionUtils.hasStoragePermission(this)) {
            MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.main_permission,
                    R.string.main_permission_content, false, DIALOG_REQUEST_PERMISSION);
            fragment.show(getFragmentManager(), null);
        }
    }

    private void checkUpdate() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            mPresenter.checkUpdate(info.versionName);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected String getDefaultTitle() {
        int home = mPreference.getInt(PreferenceManager.PREF_OTHER_LAUNCH, PreferenceManager.HOME_FAVORITE);
        switch (home) {
            default:
            case PreferenceManager.HOME_FAVORITE:
            case PreferenceManager.HOME_HISTORY:
            case PreferenceManager.HOME_DOWNLOAD:
            case PreferenceManager.HOME_LOCAL:
                return getString(R.string.drawer_comic);
            case PreferenceManager.HOME_SOURCE:
                return getString(R.string.drawer_source);
            case PreferenceManager.HOME_TAG:
                return getString(R.string.drawer_tag);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected View getLayoutView() {
        return mDrawerLayout;
    }

}
