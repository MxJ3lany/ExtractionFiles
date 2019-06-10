package com.battlelancer.seriesguide.modules;

import com.battlelancer.seriesguide.BuildConfig;
import com.uwetrottmann.seriesguide.tmdbapi.SgTmdb;
import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.services.ConfigurationService;
import com.uwetrottmann.tmdb2.services.FindService;
import com.uwetrottmann.tmdb2.services.MoviesService;
import com.uwetrottmann.tmdb2.services.PeopleService;
import com.uwetrottmann.tmdb2.services.SearchService;
import com.uwetrottmann.tmdb2.services.TvService;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;

@Module
public class TmdbModule {

    @Singleton
    @Provides
    ConfigurationService provideConfigurationService(Tmdb tmdb) {
        return tmdb.configurationService();
    }

    @Singleton
    @Provides
    FindService provideFindService(Tmdb tmdb) {
        return tmdb.findService();
    }

    @Singleton
    @Provides
    MoviesService provideMovieService(Tmdb tmdb) {
        return tmdb.moviesService();
    }

    @Singleton
    @Provides
    PeopleService providePeopleService(Tmdb tmdb) {
        return tmdb.personService();
    }

    @Singleton
    @Provides
    SearchService provideSearchService(Tmdb tmdb) {
        return tmdb.searchService();
    }

    @Singleton
    @Provides
    TvService provideTvService(Tmdb tmdb) {
        return tmdb.tvService();
    }

    @Singleton
    @Provides
    Tmdb provideSgTmdb(OkHttpClient okHttpClient) {
        return new SgTmdb(okHttpClient, BuildConfig.TMDB_API_KEY);
    }
}
