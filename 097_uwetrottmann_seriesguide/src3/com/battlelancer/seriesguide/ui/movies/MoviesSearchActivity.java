package com.battlelancer.seriesguide.ui.movies;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.settings.SearchSettings;
import com.battlelancer.seriesguide.ui.BaseNavDrawerActivity;
import com.battlelancer.seriesguide.util.SearchHistory;
import com.battlelancer.seriesguide.util.ViewTools;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MoviesSearchActivity extends BaseNavDrawerActivity implements
        MoviesSearchFragment.OnSearchClickListener {

    static final String EXTRA_ID_LINK = "idLink";
    private static final String STATE_SEARCH_VISIBLE = "searchVisible";

    @BindView(R.id.containerSearchBar) View containerSearchBar;
    @BindView(R.id.editTextSearchBar) AutoCompleteTextView searchView;
    @BindView(R.id.imageButtonSearchClear) View clearButton;
    @BindView(R.id.containerMoviesSearchFragment) View containerMoviesSearchFragment;

    private SearchHistory searchHistory;
    private ArrayAdapter<String> searchHistoryAdapter;
    private boolean showSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_search);
        setupNavDrawer();

        int linkId = getIntent().getIntExtra(EXTRA_ID_LINK,
                MoviesDiscoverAdapter.DISCOVER_LINK_DEFAULT.id);
        MoviesDiscoverLink link = MoviesDiscoverLink.fromId(linkId);
        showSearchView = link == MoviesDiscoverAdapter.DISCOVER_LINK_DEFAULT;
        if (savedInstanceState != null) {
            showSearchView = savedInstanceState.getBoolean(STATE_SEARCH_VISIBLE, showSearchView);
        }

        ButterKnife.bind(this);
        setupActionBar(link);

        if (savedInstanceState == null) {
            if (showSearchView) {
                ViewTools.showSoftKeyboardOnSearchView(this, searchView);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.containerMoviesSearchFragment,
                            MoviesSearchFragment.newInstance(link))
                    .commit();
        } else {
            postponeEnterTransition();
            // allow the adapter to repopulate during the next layout pass
            // before starting the transition animation
            containerMoviesSearchFragment.post(this::startPostponedEnterTransition);
        }
    }

    private void setupActionBar(MoviesDiscoverLink link) {
        super.setupActionBar();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // set title for screen readers
        if (showSearchView) {
            setTitle(R.string.search);
        } else {
            setTitle(link.titleRes);
        }

        setSearchViewVisible(showSearchView);

        // setup search box
        searchView.setThreshold(1);
        searchView.setOnClickListener(searchViewClickListener);
        searchView.setOnItemClickListener(searchViewItemClickListener);
        searchView.setOnEditorActionListener(searchViewActionListener);
        searchView.setHint(R.string.movies_search_hint);
        // set in code as XML is overridden
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        // manually retrieve the auto complete view popup background to override the theme
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.autoCompleteTextViewStyle, outValue, true);
        int[] attributes = new int[] { android.R.attr.popupBackground };
        TypedArray a = getTheme().obtainStyledAttributes(outValue.data, attributes);
        if (a.hasValue(0)) {
            searchView.setDropDownBackgroundDrawable(a.getDrawable(0));
        }
        a.recycle();

        // setup search history
        searchHistory = new SearchHistory(this, SearchSettings.KEY_SUFFIX_TMDB);
        searchHistoryAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown,
                searchHistory.getSearchHistory());
        searchView.setAdapter(searchHistoryAdapter);
        // drop-down is auto-shown on config change, ensure it is hidden when recreating views
        searchView.dismissDropDown();

        // setup clear button
        clearButton.setOnClickListener(v -> {
            searchView.setText(null);
            searchView.requestFocus();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.movies_search_menu, menu);

        MenuItem itemSearch = menu.findItem(R.id.menu_action_movies_search_display_search);
        itemSearch.setVisible(!showSearchView);
        itemSearch.setEnabled(!showSearchView);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_action_movies_search_change_language) {
            MovieLocalizationDialogFragment.show(getSupportFragmentManager());
            return true;
        }
        if (itemId == R.id.menu_action_movies_search_display_search) {
            setSearchViewVisible(true);
            ViewTools.showSoftKeyboardOnSearchView(this, searchView);
            showSearchView = true;
            supportInvalidateOptionsMenu();
            return true;
        }
        if (itemId == R.id.menu_action_movies_search_clear_history) {
            searchHistory.clearHistory();
            searchHistoryAdapter.clear();
            // setting text to null seems to fix the dropdown from not clearing
            searchView.setText(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SEARCH_VISIBLE, showSearchView);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventLanguageChanged(
            MovieLocalizationDialogFragment.LocalizationChangedEvent event) {
        // just run the current search again
        search();
    }

    @Override
    public void onSearchClick() {
        search();
    }

    private void search() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(
                R.id.containerMoviesSearchFragment);
        if (fragment == null) {
            return;
        }

        String query = searchView.getText().toString().trim();
        // perform search
        MoviesSearchFragment searchFragment = (MoviesSearchFragment) fragment;
        searchFragment.search(query);
        // update history
        if (searchHistory.saveRecentSearch(query)) {
            searchHistoryAdapter.clear();
            searchHistoryAdapter.addAll(searchHistory.getSearchHistory());
        }
    }

    private void setSearchViewVisible(boolean visible) {
        containerSearchBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(!visible);
        }
    }

    private View.OnClickListener searchViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            searchView.showDropDown();
        }
    };

    private AdapterView.OnItemClickListener searchViewItemClickListener
            = (parent, view, position, id) -> search();

    private TextView.OnEditorActionListener searchViewActionListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_SEARCH
                || (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            search();
            return true;
        }
        return false;
    };
}
