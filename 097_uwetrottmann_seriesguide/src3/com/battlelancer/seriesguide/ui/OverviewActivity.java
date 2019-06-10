package com.battlelancer.seriesguide.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.adapters.TabStripAdapter;
import com.battlelancer.seriesguide.dataliberation.model.Show;
import com.battlelancer.seriesguide.ui.overview.OverviewFragment;
import com.battlelancer.seriesguide.ui.overview.SeasonsFragment;
import com.battlelancer.seriesguide.ui.overview.ShowFragment;
import com.battlelancer.seriesguide.ui.search.EpisodeSearchFragment;
import com.battlelancer.seriesguide.ui.shows.RemoveShowDialogFragment;
import com.battlelancer.seriesguide.util.DBUtils;
import com.battlelancer.seriesguide.util.Shadows;
import com.battlelancer.seriesguide.util.tasks.RemoveShowTask;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Hosts an {@link OverviewFragment}.
 */
public class OverviewActivity extends BaseNavDrawerActivity {

    public static final int SHOW_LOADER_ID = 100;
    public static final int SHOW_CREDITS_LOADER_ID = 101;
    public static final int OVERVIEW_EPISODE_LOADER_ID = 102;
    public static final int OVERVIEW_SHOW_LOADER_ID = 103;
    public static final int OVERVIEW_ACTIONS_LOADER_ID = 104;
    public static final int SEASONS_LOADER_ID = 105;
    private static final String EXTRA_INT_SHOW_TVDBID = OverviewFragment.ARG_INT_SHOW_TVDBID;
    private static final String EXTRA_BOOLEAN_DISPLAY_SEASONS = "EXTRA_DISPLAY_SEASONS";

    private int showTvdbId;

    @Nullable @BindView(R.id.viewOverviewShadowStart) View shadowOverviewStart;
    @Nullable @BindView(R.id.viewOverviewShadowEnd) View shadowOverviewEnd;
    @Nullable @BindView(R.id.viewOverviewShadowBottom) View shadowShowBottom;

    /** After opening, switches to overview tab (only if not multi-pane). */
    public static Intent intentShow(Context context, int showTvdbId) {
        return new Intent(context, OverviewActivity.class)
                .putExtra(EXTRA_INT_SHOW_TVDBID, showTvdbId);
    }

    /** After opening, switches to seasons tab (only if not multi-pane). */
    public static Intent intentSeasons(Context context, int showTvdbId) {
        return intentShow(context, showTvdbId).putExtra(EXTRA_BOOLEAN_DISPLAY_SEASONS, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        ButterKnife.bind(this);
        setupActionBar();
        setupNavDrawer();

        showTvdbId = getIntent().getIntExtra(EXTRA_INT_SHOW_TVDBID, -1);
        if (showTvdbId < 0 || !DBUtils.isShowExists(this, showTvdbId)) {
            finish();
            return;
        }

        setupViews(savedInstanceState);

        updateShowDelayed(showTvdbId);
    }

    @Override
    protected void setupActionBar() {
        super.setupActionBar();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViews(Bundle savedInstanceState) {
        // look if we are on a multi-pane or single-pane layout...
        View pagerView = findViewById(R.id.pagerOverview);
        if (pagerView != null && pagerView.getVisibility() == View.VISIBLE) {
            // ...single pane layout with view pager

            // clear up left-over fragments from multi-pane layout
            findAndRemoveFragment(R.id.fragment_overview);
            findAndRemoveFragment(R.id.fragment_seasons);

            setupViewPager(pagerView);
        } else {
            // ...multi-pane overview and seasons fragment

            // clear up left-over fragments from single-pane layout
            boolean isSwitchingLayouts = getActiveFragments().size() != 0;
            for (Fragment fragment : getActiveFragments()) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }

            // attach new fragments if there are none or if we just switched
            // layouts
            if (savedInstanceState == null || isSwitchingLayouts) {
                setupPanes();
            }
        }

        if (shadowOverviewStart != null) {
            Shadows.getInstance().setShadowDrawable(this, shadowOverviewStart,
                    GradientDrawable.Orientation.RIGHT_LEFT);
        }
        if (shadowOverviewEnd != null) {
            Shadows.getInstance().setShadowDrawable(this, shadowOverviewEnd,
                    GradientDrawable.Orientation.LEFT_RIGHT);
        }
        if (shadowShowBottom != null) {
            Shadows.getInstance().setShadowDrawable(this, shadowShowBottom,
                    GradientDrawable.Orientation.TOP_BOTTOM);
        }
    }

    private void setupPanes() {
        Fragment showsFragment = ShowFragment.newInstance(showTvdbId);
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft1.replace(R.id.fragment_show, showsFragment);
        ft1.commit();

        Fragment overviewFragment = OverviewFragment.newInstance(showTvdbId);
        FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
        ft2.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft2.replace(R.id.fragment_overview, overviewFragment);
        ft2.commit();

        Fragment seasonsFragment = SeasonsFragment.newInstance(showTvdbId);
        FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
        ft3.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft3.replace(R.id.fragment_seasons, seasonsFragment);
        ft3.commit();
    }

    private void setupViewPager(View pagerView) {
        ViewPager pager = (ViewPager) pagerView;

        // setup tab strip
        TabStripAdapter tabsAdapter = new TabStripAdapter(getSupportFragmentManager(), this, pager,
                findViewById(R.id.tabsOverview));
        Bundle argsShow = new Bundle();
        argsShow.putInt(ShowFragment.ARG_SHOW_TVDBID, showTvdbId);
        tabsAdapter.addTab(R.string.show, ShowFragment.class, argsShow);

        tabsAdapter.addTab(R.string.description_overview, OverviewFragment.class, getIntent()
                .getExtras());

        Bundle argsSeason = new Bundle();
        argsSeason.putInt(SeasonsFragment.ARG_SHOW_TVDBID, showTvdbId);
        tabsAdapter.addTab(R.string.seasons, SeasonsFragment.class, argsSeason);
        tabsAdapter.notifyTabsChanged();

        // select overview to be shown initially
        boolean displaySeasons = getIntent().getBooleanExtra(EXTRA_BOOLEAN_DISPLAY_SEASONS, false);
        pager.setCurrentItem(displaySeasons ? 2 /* seasons */ : 1 /* overview */);
    }

    private void findAndRemoveFragment(int fragmentId) {
        Fragment overviewFragment = getSupportFragmentManager().findFragmentById(fragmentId);
        if (overviewFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(overviewFragment).commit();
        }
    }

    List<WeakReference<Fragment>> fragments = new ArrayList<>();

    @Override
    public void onAttachFragment(Fragment fragment) {
        /*
         * View pager fragments have tags set by the pager, we can use this to
         * only add refs to those then, making them available to get removed if
         * we switch to a non-pager layout.
         */
        if (fragment.getTag() != null) {
            fragments.add(new WeakReference<>(fragment));
        }
    }

    public ArrayList<Fragment> getActiveFragments() {
        ArrayList<Fragment> ret = new ArrayList<>();
        for (WeakReference<Fragment> ref : fragments) {
            Fragment f = ref.get();
            if (f != null) {
                if (f.isAdded()) {
                    ret.add(f);
                }
            }
        }
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overview_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_overview_search) {
            launchSearch();
            return true;
        }
        if (itemId == R.id.menu_overview_remove_show) {
            RemoveShowDialogFragment.show(this, getSupportFragmentManager(), showTvdbId);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RemoveShowTask.OnRemovingShowEvent event) {
        if (event.showTvdbId == showTvdbId) {
            finish(); // finish this activity if the show it displays is about to get removed
        }
    }

    private void launchSearch() {
        // refine search with the show's title
        Show show = DBUtils.getShow(this, showTvdbId);
        if (show != null) {
            Bundle appSearchData = new Bundle();
            appSearchData.putString(EpisodeSearchFragment.ARG_SHOW_TITLE, show.title);

            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(SearchManager.APP_DATA, appSearchData);
            intent.setAction(Intent.ACTION_SEARCH);
            startActivity(intent);
        }
    }

    @Override
    protected View getSnackbarParentView() {
        if (getResources().getBoolean(R.bool.isSinglePane)) {
            return findViewById(R.id.coordinatorLayoutOverview);
        } else {
            return super.getSnackbarParentView();
        }
    }
}
