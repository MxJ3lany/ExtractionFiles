package com.battlelancer.seriesguide.provider;

import androidx.room.Dao;
import androidx.room.Query;
import com.battlelancer.seriesguide.model.SgSeason;
import com.battlelancer.seriesguide.provider.SeriesGuideDatabase.Tables;

/**
 * Data Access Object for the seasons table.
 */
@Dao
public interface SeasonHelper {

    /**
     * For testing: Get the first season from the table.
     */
    @Query("SELECT * FROM " + Tables.SEASONS + " LIMIT 1")
    SgSeason getSeason();

}
