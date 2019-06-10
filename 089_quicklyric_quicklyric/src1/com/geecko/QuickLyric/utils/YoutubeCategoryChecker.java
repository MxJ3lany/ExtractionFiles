/*
 * *
 *  * This file is part of QuickLyric
 *  * Created by geecko
 *  *
 *  * QuickLyric is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * QuickLyric is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  * You should have received a copy of the GNU General Public License
 *  * along with QuickLyric.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.geecko.QuickLyric.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.search.SearchResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeCategoryChecker {

    static {
        NewPipe.init(new Downloader());
    }

    public static boolean isMusicVideo(String channel, String title) {
        try {
            SearchEngine engine = YouTube.getService().getSearchEngine();
            SearchResult result = engine.search(channel + " " + title, 0, "", SearchEngine.Filter.ANY).getSearchResult();
            for (InfoItem infoItem : result.resultList) {
                if (isMusicVideo(new URL(infoItem.url), title))
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isMusicVideo(URL url, String title) throws IOException {
        String page;
        try {
            page = NewPipe.getDownloader().download(url.toString());
        } catch (ReCaptchaException e) {
            e.printStackTrace();
            return false;
        }
        Document doc = Jsoup.parse(page, url.toString());
        return doc.title().startsWith(title.replaceAll("\\s+", " ")) && !doc.select("meta[itemprop=\"genre\"][content=\"Music\"]").isEmpty();
    }

    private static class Downloader implements org.schabi.newpipe.extractor.Downloader {

            private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";
            private static String mCookies = "";

            private static Downloader instance = null;

            private Downloader() {
            }

            public static Downloader getInstance() {
                if (instance == null) {
                    synchronized (Downloader.class) {
                        if (instance == null) {
                            instance = new Downloader();
                        }
                    }
                }
                return instance;
            }

            public static synchronized void setCookies(String cookies) {
                Downloader.mCookies = cookies;
            }

            public static synchronized String getCookies() {
                return Downloader.mCookies;
            }

            /**
             * Download the text file at the supplied URL as in download(String),
             * but set the HTTP header field "Accept-Language" to the supplied string.
             *
             * @param siteUrl  the URL of the text file to return the contents of
             * @param language the language (usually a 2-character code) to set as the preferred language
             * @return the contents of the specified text file
             */
            public String download(String siteUrl, String language) throws IOException, ReCaptchaException {
                Map<String, String> requestProperties = new HashMap<>();
                requestProperties.put("Accept-Language", language);
                return download(siteUrl, requestProperties);
            }


            /**
             * Download the text file at the supplied URL as in download(String),
             * but set the HTTP header field "Accept-Language" to the supplied string.
             *
             * @param siteUrl          the URL of the text file to return the contents of
             * @param customProperties set request header properties
             * @return the contents of the specified text file
             * @throws IOException
             */
            public String download(String siteUrl, Map<String, String> customProperties) throws IOException, ReCaptchaException {
                URL url = new URL(siteUrl);
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                for (Map.Entry<String, String> pair: customProperties.entrySet()) {
                    con.setRequestProperty(pair.getKey(), pair.getValue());
                }
                return dl(con);
            }

            /**
             * Common functionality between download(String url) and download(String url, String language)
             */
            private static String dl(HttpsURLConnection con) throws IOException, ReCaptchaException {
                StringBuilder response = new StringBuilder();
                BufferedReader in = null;

                try {
                    con.setConnectTimeout(30 * 1000);// 30s
                    con.setReadTimeout(30 * 1000);// 30s
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", USER_AGENT);

                    if (getCookies().length() > 0) {
                        con.setRequestProperty("Cookie", getCookies());
                    }

                    in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                } catch (UnknownHostException uhe) {//thrown when there's no internet connection
                    throw new IOException("unknown host or no network", uhe);
                    //Toast.makeText(getActivity(), uhe.getMessage(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
            /*
             * HTTP 429 == Too Many Request
             * Receive from Youtube.com = ReCaptcha challenge request
             * See : https://github.com/rg3/youtube-dl/issues/5138
             */
                    if (con.getResponseCode() == 429) {
                        throw new ReCaptchaException("reCaptcha Challenge requested");
                    }

                    throw new IOException(con.getResponseCode() + " " + con.getResponseMessage(), e);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }

                return response.toString();
            }

            /**
             * Download (via HTTP) the text file located at the supplied URL, and return its contents.
             * Primarily intended for downloading web pages.
             *
             * @param siteUrl the URL of the text file to download
             * @return the contents of the specified text file
             */
            public String download(String siteUrl) throws IOException, ReCaptchaException {
                URL url = new URL(siteUrl);
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                //HttpsURLConnection con = NetCipher.getHttpsURLConnection(url);
                return dl(con);
            }
        }
}
