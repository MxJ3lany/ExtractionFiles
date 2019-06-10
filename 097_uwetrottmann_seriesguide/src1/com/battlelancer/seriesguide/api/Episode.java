package com.battlelancer.seriesguide.api;

import android.os.Bundle;

public class Episode {
    private static final String KEY_TITLE = "title";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_NUMBER_ABSOLUTE = "numberAbsolute";
    private static final String KEY_SEASON = "season";
    private static final String KEY_TVDBID = "tvdbid";
    private static final String KEY_IMDBID = "imdbid";

    private static final String KEY_SHOW_TITLE = "showTitle";
    private static final String KEY_SHOW_TVDBID = "showTvdbId";
    private static final String KEY_SHOW_IMDBID = "showImdbId";
    private static final String KEY_SHOW_FIRST_RELEASE_DATE = "showFirstReleaseDate";

    private String title;
    private Integer number;
    private Integer numberAbsolute;
    private Integer season;
    private Integer tvdbId;
    private String imdbId;

    private String showTitle;
    private Integer showTvdbId;
    private String showImdbId;
    private String showFirstReleaseDate;

    private Episode() {
    }

    public String getTitle() {
        return title;
    }

    public Integer getNumber() {
        return number;
    }

    public Integer getNumberAbsolute() {
        return numberAbsolute;
    }

    public Integer getSeason() {
        return season;
    }

    public String getImdbId() {
        return imdbId;
    }

    public Integer getTvdbId() {
        return tvdbId;
    }

    public String getShowTitle() {
        return showTitle;
    }

    public Integer getShowTvdbId() {
        return showTvdbId;
    }

    public String getShowImdbId() {
        return showImdbId;
    }

    /**
     * Release date of the first episode. Encoded as ISO 8601 datetime string.
     *
     * <pre>
     * Example: "2008-01-20T02:00:00.000Z"
     * Default: ""
     * </pre>
     */
    public String getShowFirstReleaseDate() {
        return showFirstReleaseDate;
    }

    public static class Builder {
        private final Episode mEpisode;

        public Builder() {
            mEpisode = new Episode();
        }

        public Builder title(String episodeTitle) {
            mEpisode.title = episodeTitle;
            return this;
        }

        public Builder number(Integer episodeNumber) {
            mEpisode.number = episodeNumber;
            return this;
        }

        public Builder numberAbsolute(Integer absoluteNumber) {
            mEpisode.numberAbsolute = absoluteNumber;
            return this;
        }

        public Builder season(Integer seasonNumber) {
            mEpisode.season = seasonNumber;
            return this;
        }

        public Builder tvdbId(Integer episodeTvdbId) {
            mEpisode.tvdbId = episodeTvdbId;
            return this;
        }

        public Builder imdbId(String episodeImdbId) {
            mEpisode.imdbId = episodeImdbId;
            return this;
        }

        public Builder showTitle(String showTitle) {
            mEpisode.showTitle = showTitle;
            return this;
        }

        public Builder showTvdbId(Integer showTvdbId) {
            mEpisode.showTvdbId = showTvdbId;
            return this;
        }

        public Builder showImdbId(String showImdbId) {
            mEpisode.showImdbId = showImdbId;
            return this;
        }

        public Builder showFirstReleaseDate(String showFirstReleaseDate) {
            mEpisode.showFirstReleaseDate = showFirstReleaseDate;
            return this;
        }

        public Episode build() {
            return mEpisode;
        }
    }

    /**
     * Serializes this {@link Episode} object to a {@link android.os.Bundle} representation.
     */
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, title);
        bundle.putInt(KEY_NUMBER, number);
        bundle.putInt(KEY_NUMBER_ABSOLUTE, numberAbsolute);
        bundle.putInt(KEY_SEASON, season);
        bundle.putInt(KEY_TVDBID, tvdbId);
        bundle.putString(KEY_IMDBID, imdbId);
        bundle.putString(KEY_SHOW_TITLE, showTitle);
        bundle.putInt(KEY_SHOW_TVDBID, showTvdbId);
        bundle.putString(KEY_SHOW_IMDBID, showImdbId);
        bundle.putString(KEY_SHOW_FIRST_RELEASE_DATE, showFirstReleaseDate);
        return bundle;
    }

    /**
     * Deserializes an {@link Episode} into a {@link android.os.Bundle} object.
     */
    public static Episode fromBundle(Bundle bundle) {
        Builder builder = new Builder()
                .title(bundle.getString(KEY_TITLE))
                .number(bundle.getInt(KEY_NUMBER))
                .numberAbsolute(bundle.getInt(KEY_NUMBER_ABSOLUTE))
                .season(bundle.getInt(KEY_SEASON))
                .tvdbId(bundle.getInt(KEY_TVDBID))
                .imdbId(bundle.getString(KEY_IMDBID))
                .showTitle(bundle.getString(KEY_SHOW_TITLE))
                .showTvdbId(bundle.getInt(KEY_SHOW_TVDBID))
                .showImdbId(bundle.getString(KEY_SHOW_IMDBID))
                .showFirstReleaseDate(bundle.getString(KEY_SHOW_FIRST_RELEASE_DATE));

        return builder.build();
    }
}
