package com.battlelancer.seriesguide.ui.search

import android.app.SearchManager
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.loader.app.LoaderManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.battlelancer.seriesguide.R
import com.battlelancer.seriesguide.provider.SeriesGuideContract.EpisodeSearch
import com.battlelancer.seriesguide.provider.SeriesGuideContract.Shows
import com.battlelancer.seriesguide.provider.SeriesGuideDatabase
import com.battlelancer.seriesguide.ui.SearchActivity
import com.battlelancer.seriesguide.ui.episodes.EpisodesActivity
import com.battlelancer.seriesguide.util.TabClickEvent
import com.battlelancer.seriesguide.util.Utils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Displays episode search results.
 */
class EpisodeSearchFragment : BaseSearchFragment() {

    private lateinit var adapter: EpisodeResultsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // list items do not have right hand-side buttons, list may be long: enable fast scrolling
        gridView.apply {
            isFastScrollAlwaysVisible = false
            isFastScrollEnabled = true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = EpisodeResultsAdapter(activity, onItemClickListener).also {
            gridView.adapter = it
        }

        // load for given query or restore last loader (ignoring args)
        LoaderManager.getInstance(this)
            .initLoader(SearchActivity.EPISODES_LOADER_ID, loaderArgs, searchLoaderCallbacks)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: SearchActivity.SearchQueryEvent) {
        LoaderManager.getInstance(this)
            .restartLoader(SearchActivity.EPISODES_LOADER_ID, event.args, searchLoaderCallbacks)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventTabClick(event: TabClickEvent) {
        if (event.position == SearchActivity.TAB_POSITION_EPISODES) {
            gridView.smoothScrollToPosition(0)
        }
    }

    private val searchLoaderCallbacks = object : LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            loaderArgs = args
            var query = args?.getString(SearchManager.QUERY)
            if (query.isNullOrEmpty()) {
                query = ""
            }

            var selection: String? = null
            var selectionArgs = arrayOf(query)

            val appData = args?.getBundle(SearchManager.APP_DATA)
            if (appData != null) {
                val showtitle = appData.getString(ARG_SHOW_TITLE)
                // set show filter instead
                if (showtitle != null) {
                    selection = Shows.TITLE + "=?"
                    selectionArgs = arrayOf(query, showtitle)
                }
            }

            return CursorLoader(requireContext(), EpisodeSearch.CONTENT_URI_SEARCH,
                    SeriesGuideDatabase.EpisodeSearchQuery.PROJECTION, selection, selectionArgs,
                    null)
        }

        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
            adapter.swapCursor(data)
            updateEmptyState(data)
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            adapter.swapCursor(null)
        }
    }

    private val onItemClickListener =
        EpisodeResultsAdapter.OnItemClickListener { anchor, episodeTvdbId ->
            Intent(activity, EpisodesActivity::class.java)
                .putExtra(EpisodesActivity.InitBundle.EPISODE_TVDBID, episodeTvdbId)
                .also { Utils.startActivityWithAnimation(activity, it, anchor) }
        }

    companion object {
        const val ARG_SHOW_TITLE = "title"
    }
}
