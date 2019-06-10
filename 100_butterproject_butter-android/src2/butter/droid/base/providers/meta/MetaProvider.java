/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.base.providers.meta;

import com.google.gson.Gson;

import org.threeten.bp.LocalDate;
import butter.droid.base.providers.BaseProvider;
import okhttp3.OkHttpClient;

public abstract class MetaProvider extends BaseProvider {

    public MetaProvider(OkHttpClient client, Gson gson) {
        super(client, gson);
    }

    public static class MetaData {

        public String title;
        public Integer year;
        public String imdbId;
        public LocalDate released;
        public String trailer;
        public Integer runtime;
        public Double rating;
        public String tagline;
        public String overview;
        public String certification;
        public Images images;
        public String[] genres;

        public static class Images {

            public String poster;
            public String backdrop;

            public Images(String poster, String backdrop) {
                this.poster = poster;
                this.backdrop = backdrop;
            }

            public Images(String poster) {
                this.poster = this.backdrop = poster;
            }
        }
    }

    public interface Callback {

        public void onResult(MetaData metaData, Exception e);
    }

    public void getMovieMeta(String imdbId, Callback callback) {
    }

    public void getEpisodeMeta(String imdbId, int season, int episode, Callback callback) {
    }

    public void getShowMeta(String imdbId, Callback callback) {
    }

}
