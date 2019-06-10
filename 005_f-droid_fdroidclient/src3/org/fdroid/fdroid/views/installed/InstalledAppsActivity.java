/*
 * Copyright (C) 2010-12  Ciaran Gultnieks, ciaran@ciarang.com
 * Copyright (C) 2009  Roberto Jacinto, roberto.jacinto@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.fdroid.fdroid.views.installed;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import org.fdroid.fdroid.FDroidApp;
import org.fdroid.fdroid.R;
import org.fdroid.fdroid.data.App;
import org.fdroid.fdroid.data.AppProvider;
import org.fdroid.fdroid.data.Schema;

public class InstalledAppsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private InstalledAppListAdapter adapter;
    private RecyclerView appList;
    private TextView emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ((FDroidApp) getApplication()).applyTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.installed_apps_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.installed_apps__activity_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new InstalledAppListAdapter(this);

        appList = (RecyclerView) findViewById(R.id.app_list);
        appList.setHasFixedSize(true);
        appList.setLayoutManager(new LinearLayoutManager(this));
        appList.setAdapter(adapter);

        emptyState = (TextView) findViewById(R.id.empty_state);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Starts a new or restarts an existing Loader in this manager
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                AppProvider.getInstalledUri(),
                Schema.AppMetadataTable.Cols.ALL,
                null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        adapter.setApps(cursor);

        if (adapter.getItemCount() == 0) {
            appList.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            appList.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        adapter.setApps(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.installed_apps, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_share:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("packageName,versionCode,versionName\n");
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    App app = adapter.getItem(i);
                    if (app != null) {
                        stringBuilder.append(app.packageName).append(',')
                                .append(app.installedVersionCode).append(',')
                                .append(app.installedVersionName).append('\n');
                    }
                }
                ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this)
                        .setSubject(getString(R.string.send_installed_apps))
                        .setChooserTitle(R.string.send_installed_apps)
                        .setText(stringBuilder.toString())
                        .setType("text/csv");
                startActivity(intentBuilder.getIntent());
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
