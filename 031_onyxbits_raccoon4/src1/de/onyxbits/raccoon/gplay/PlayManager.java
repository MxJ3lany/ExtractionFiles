/*
 * Copyright 2015 Patrick Ahlbrecht
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.onyxbits.raccoon.gplay;

import java.awt.Window;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;

import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;
import com.akdeniz.googleplaycrawler.Utils;

import de.onyxbits.raccoon.db.DatabaseManager;
import de.onyxbits.raccoon.db.Variables;

/**
 * Core glue, tying everything else together
 * 
 * @author patrick
 * 
 */
public class PlayManager implements Variables {

	private static final String ID = PlayManager.class.getSimpleName();

	private ArrayList<PlayListener> listeners;
	private String appQuery;
	private DatabaseManager databaseManager;
	private SearchAppWorker currentAppSearch;
	private SearchEngineResultPage serp;

	public PlayManager(DatabaseManager dbm) {
		this.listeners = new ArrayList<PlayListener>();
		this.databaseManager = dbm;
	}

	/**
	 * Start a new search
	 * 
	 * @param query
	 *          what to search for
	 */
	public void searchApps(String query) {
		this.appQuery = query;
		this.serp = null;
		moreApps();
	}

	/**
	 * Load the next page of search results.
	 */
	public void moreApps() {
		PlayListener[] tmp = listeners.toArray(new PlayListener[listeners.size()]);
		for (PlayListener listener : tmp) {
			listener.onAppSearch();
		}
		if (currentAppSearch != null) {
			currentAppSearch.cancel(false);
		}
		currentAppSearch = new SearchAppWorker(this, createConnection(), appQuery,
				serp);
		currentAppSearch.execute();
	}

	/**
	 * create a proxy client
	 * 
	 * @return either a client or null if none is configured
	 * @throws KeyManagementException
	 * @throws NumberFormatException
	 *           if that port could not be parsed.
	 * @throws NoSuchAlgorithmException
	 */
	private static HttpClient createProxyClient(PlayProfile profile)
			throws KeyManagementException, NoSuchAlgorithmException {
		if (profile.getProxyAddress() == null) {
			return null;
		}

		PoolingClientConnectionManager connManager = new PoolingClientConnectionManager(
				SchemeRegistryFactory.createDefault());
		connManager.setMaxTotal(100);
		connManager.setDefaultMaxPerRoute(30);

		DefaultHttpClient client = new DefaultHttpClient(connManager);
		client.getConnectionManager().getSchemeRegistry()
				.register(Utils.getMockedScheme());
		HttpHost proxy = new HttpHost(profile.getProxyAddress(),
				profile.getProxyPort());
		client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		if (profile.getProxyUser() != null && profile.getProxyPassword() != null) {
			client.getCredentialsProvider().setCredentials(
					new AuthScope(proxy),
					new UsernamePasswordCredentials(profile.getProxyUser(), profile
							.getProxyPassword()));
		}
		return client;
	}

	/**
	 * Create a connection object for accessing Google Play, using the currently
	 * active profile.
	 * 
	 * @return a connection according to the active profile settings.
	 */
	public GooglePlayAPI createConnection() {
		PlayProfile active = databaseManager.get(PlayProfileDao.class).get();
		if (active == null) {
			// Semi error state.
			return new GooglePlayAPI();
		}
		return createConnection(active);
	}

	/**
	 * Create a connection object for accessing Google Play, using an arbitrary
	 * profile
	 * 
	 * @param profile
	 *          the profile to use for connecting
	 * @return a connection according to the submitted profile
	 */
	public static GooglePlayAPI createConnection(PlayProfile profile) {
		GooglePlayAPI ret = new GooglePlayAPI(profile.getUser(),
				profile.getPassword());
		ret.setUseragent(profile.getAgent());
		ret.setAndroidID(profile.getGsfId());
		ret.setToken(profile.getToken());
		Locale l = Locale.getDefault();
		String s = l.getLanguage();
		if (l.getCountry() != null) {
			s = s + "-" + l.getCountry();
		}
		ret.setLocalization(s);
		try {
			HttpClient proxy = createProxyClient(profile);
			if (proxy != null) {
				ret.setClient(proxy);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Called by the worker after finishing.
	 * 
	 * @param apps
	 */
	protected void fireAppSearchResult(SearchEngineResultPage s) {
		currentAppSearch = null;
		serp = s;
		for (PlayListener listener : listeners) {
			listener.onAppSearchResult(s.getContent(), appQuery == null);
		}
		appQuery=null;
	}

	protected void fireUnauthorized() {
		// TODO: This is a quickfix, not the proper way to handle it!
		fireAppSearchResult(new SearchEngineResultPage(SearchEngineResultPage.ALL));
		JOptionPane.showMessageDialog(Window.getWindows()[0],
				Messages.getString(ID + ".session.message"),
				Messages.getString(ID + ".session.title"), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Called to show app details
	 * 
	 * @param app
	 *          the app to show
	 * @param brief
	 *          true if this is a brief doc.
	 */
	protected void fireAppView(DocV2 app, boolean brief) {
		// Lists don't like to be modified while iterated and we got a little
		// speedhack in HostBuilder that does exactly that.
		PlayListener[] tmp = listeners.toArray(new PlayListener[listeners.size()]);
		for (PlayListener listener : tmp) {
			listener.onAppView(app, brief);
		}
	}

	public void addPlayListener(PlayListener listener) {
		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removePlayListener(PlayListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Force (re-)authentication with Play and persist the token.
	 */
	public void login() {
		GooglePlayAPI api = createConnection();
		try {
			api.login();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		PlayProfile active = databaseManager.get(PlayProfileDao.class).get();
		active.setToken(api.getToken());
		try {
			databaseManager.get(PlayProfileDao.class).update(active);
		}
		catch (SQLException e) {
			// We can live with it not being persisted.
		}
	}
}
