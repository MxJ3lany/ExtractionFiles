package com.battlelancer.seriesguide.ui.movies

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.battlelancer.seriesguide.R
import com.battlelancer.seriesguide.model.SgMovie
import com.battlelancer.seriesguide.settings.TmdbSettings
import java.text.DateFormat

internal class MoviesWatchedAdapter(
    context: Context,
    val itemClickListener: MoviesAdapter.ItemClickListener
) : PagedListAdapter<SgMovie, MovieViewHolder>(DIFF_CALLBACK) {

    private val dateFormatMovieReleaseDate: DateFormat = MovieTools.getMovieShortDateFormat()
    private val posterBaseUrl = TmdbSettings.getPosterBaseUrl(context)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovieViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discover_movie, parent, false)
        return MovieViewHolder(itemView, itemClickListener)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie: SgMovie? = getItem(position)

        // Note that "movie" is a placeholder if it's null.
        holder.bindTo(movie, dateFormatMovieReleaseDate, posterBaseUrl)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SgMovie>() {

            override fun areItemsTheSame(oldItem: SgMovie, newItem: SgMovie) =
                oldItem.id == newItem.id

            // If you use the "==" operator, make sure that the object implements
            // .equals(). Alternatively, write custom data comparison logic here.
            override fun areContentsTheSame(oldItem: SgMovie, newItem: SgMovie): Boolean {
                return oldItem.title == newItem.title
                        && oldItem.poster == newItem.poster
                        && oldItem.releasedMs == newItem.releasedMs
            }
        }
    }
}