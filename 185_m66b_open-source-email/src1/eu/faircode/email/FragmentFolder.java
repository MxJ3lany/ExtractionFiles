package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

public class FragmentFolder extends FragmentBase {
    private ViewGroup view;
    private EditText etName;
    private EditText etDisplay;
    private CheckBox cbUnified;
    private CheckBox cbNavigation;
    private CheckBox cbNotify;
    private CheckBox cbSynchronize;
    private CheckBox cbPoll;
    private CheckBox cbDownload;
    private EditText etSyncDays;
    private EditText etKeepDays;
    private CheckBox cbKeepAll;
    private CheckBox cbAutoDelete;
    private Button btnSave;
    private ContentLoadingProgressBar pbSave;
    private ContentLoadingProgressBar pbWait;

    private long id = -1;
    private long account = -1;
    private Boolean subscribed = null;
    private boolean saving = false;
    private boolean deletable = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        id = args.getLong("id", -1);
        account = args.getLong("account", -1);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_edit_folder);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_folder, container, false);

        // Get controls
        etName = view.findViewById(R.id.etName);
        etDisplay = view.findViewById(R.id.etDisplay);
        cbUnified = view.findViewById(R.id.cbUnified);
        cbNavigation = view.findViewById(R.id.cbNavigation);
        cbNotify = view.findViewById(R.id.cbNotify);
        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        cbPoll = view.findViewById(R.id.cbPoll);
        cbDownload = view.findViewById(R.id.cbDownload);
        etSyncDays = view.findViewById(R.id.etSyncDays);
        etKeepDays = view.findViewById(R.id.etKeepDays);
        cbKeepAll = view.findViewById(R.id.cbKeepAll);
        cbAutoDelete = view.findViewById(R.id.cbAutoDelete);
        btnSave = view.findViewById(R.id.btnSave);
        pbSave = view.findViewById(R.id.pbSave);
        pbWait = view.findViewById(R.id.pbWait);

        cbUnified.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    cbNotify.setChecked(true);
            }
        });

        // Navigating to individual messages requires notification grouping
        cbNotify.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? View.VISIBLE : View.GONE);

        cbSynchronize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbPoll.setEnabled(isChecked);
                cbDownload.setEnabled(isChecked);
            }
        });

        cbKeepAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etKeepDays.setEnabled(!isChecked);
                cbAutoDelete.setEnabled(!isChecked);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);
        cbAutoDelete.setVisibility(View.GONE);
        btnSave.setEnabled(false);
        pbSave.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    private void onSave() {
        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putLong("account", account);
        args.putString("name", etName.getText().toString());
        args.putString("display", etDisplay.getText().toString());
        args.putBoolean("unified", cbUnified.isChecked());
        args.putBoolean("navigation", cbNavigation.isChecked());
        args.putBoolean("notify", cbNotify.getVisibility() == View.VISIBLE && cbNotify.isChecked());
        args.putBoolean("synchronize", cbSynchronize.isChecked());
        args.putBoolean("poll", cbPoll.isChecked());
        args.putBoolean("download", cbDownload.isChecked());
        args.putString("sync", etSyncDays.getText().toString());
        args.putString("keep", cbKeepAll.isChecked()
                ? Integer.toString(Integer.MAX_VALUE)
                : etKeepDays.getText().toString());
        args.putBoolean("auto_delete", cbAutoDelete.isChecked());

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                saving = true;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, false);
                pbSave.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                saving = false;
                getActivity().invalidateOptionsMenu();
                Helper.setViewsEnabled(view, true);
                pbSave.setVisibility(View.GONE);
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                long aid = args.getLong("account");
                String name = args.getString("name");
                String display = args.getString("display");
                boolean unified = args.getBoolean("unified");
                boolean navigation = args.getBoolean("navigation");
                boolean notify = args.getBoolean("notify");
                boolean synchronize = args.getBoolean("synchronize");
                boolean poll = args.getBoolean("poll");
                boolean download = args.getBoolean("download");
                String sync = args.getString("sync");
                String keep = args.getString("keep");
                boolean auto_delete = args.getBoolean("auto_delete");

                if (TextUtils.isEmpty(display) || display.equals(name))
                    display = null;
                int sync_days = (TextUtils.isEmpty(sync) ? EntityFolder.DEFAULT_SYNC : Integer.parseInt(sync));
                int keep_days = (TextUtils.isEmpty(keep) ? EntityFolder.DEFAULT_KEEP : Integer.parseInt(keep));
                if (keep_days < sync_days)
                    keep_days = sync_days;

                boolean reload;
                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder folder = db.folder().getFolder(id);

                    if (folder == null) {
                        reload = true;
                        Log.i("Creating folder=" + name);

                        if (TextUtils.isEmpty(name))
                            throw new IllegalArgumentException(context.getString(R.string.title_folder_name_missing));
                        if (db.folder().getFolderByName(aid, name) != null)
                            throw new IllegalArgumentException(context.getString(R.string.title_folder_exists, name));

                        EntityFolder create = new EntityFolder();
                        create.account = aid;
                        create.name = name;
                        create.display = display;
                        create.type = EntityFolder.USER;
                        create.unified = unified;
                        create.navigation = navigation;
                        create.notify = notify;
                        create.synchronize = synchronize;
                        create.poll = poll;
                        create.download = download;
                        create.sync_days = sync_days;
                        create.keep_days = keep_days;
                        create.tbc = true;
                        db.folder().insertFolder(create);
                    } else {
                        reload = (!folder.synchronize.equals(synchronize) ||
                                !folder.poll.equals(poll));

                        Log.i("Updating folder=" + name);
                        db.folder().setFolderProperties(id,
                                display, unified, navigation, notify,
                                synchronize, poll, download,
                                sync_days, keep_days, auto_delete);
                        db.folder().setFolderError(id, null);

                        if (!reload && synchronize)
                            EntityOperation.sync(context, folder.id, true);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (reload)
                    ServiceSynchronize.reload(context, "save folder");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                getFragmentManager().popBackStack();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentFolder.this, args, "folder:save");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_folder, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean subscriptions = prefs.getBoolean("subscriptions", false);

        menu.findItem(R.id.menu_subscribe).setChecked(subscribed != null && subscribed);
        menu.findItem(R.id.menu_subscribe).setVisible(subscriptions && id > 0 && subscribed != null);
        menu.findItem(R.id.menu_delete).setVisible(id > 0 && !saving && deletable);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_subscribe:
                subscribed = !item.isChecked();
                item.setChecked(subscribed);
                onMenuSubscribe();
                return true;
            case R.id.menu_delete:
                onMenuDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuSubscribe() {
        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putBoolean("subscribed", subscribed);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                boolean subscribed = args.getBoolean("subscribed");

                EntityOperation.subscribe(context, id, subscribed);

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, args, "folder:subscribe");
    }

    private void onMenuDelete() {
        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setMessage(R.string.title_folder_delete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Helper.setViewsEnabled(view, false);
                        pbSave.setVisibility(View.VISIBLE);

                        Bundle args = new Bundle();
                        args.putLong("id", id);

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                long id = args.getLong("id");

                                DB db = DB.getInstance(context);
                                int count = db.operation().getOperationCount(id, null);
                                if (count > 0)
                                    throw new IllegalArgumentException(
                                            context.getResources().getQuantityString(
                                                    R.plurals.title_notification_operations, count, count));
                                db.folder().setFolderTbd(id);

                                ServiceSynchronize.reload(context, "delete folder");

                                return null;
                            }

                            @Override
                            protected void onExecuted(Bundle args, Void data) {
                                getFragmentManager().popBackStack();
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.setViewsEnabled(view, true);
                                pbSave.setVisibility(View.GONE);

                                if (ex instanceof IllegalArgumentException)
                                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                                else
                                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(FragmentFolder.this, args, "folder:delete");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<EntityFolder>() {
            @Override
            protected EntityFolder onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                return DB.getInstance(context).folder().getFolder(id);
            }

            @Override
            protected void onExecuted(Bundle args, EntityFolder folder) {
                if (savedInstanceState == null) {
                    etName.setText(folder == null ? null : folder.name);
                    etDisplay.setText(folder == null ? null : folder.display);
                    etDisplay.setHint(folder == null ? null : Helper.localizeFolderName(getContext(), folder.name));
                    cbUnified.setChecked(folder == null ? false : folder.unified);
                    cbNavigation.setChecked(folder == null ? false : folder.navigation);
                    cbNotify.setChecked(folder == null ? false : folder.notify);
                    cbSynchronize.setChecked(folder == null || folder.synchronize);
                    cbPoll.setChecked(folder == null ? false : folder.poll);
                    cbDownload.setChecked(folder == null ? true : folder.download);
                    etSyncDays.setText(Integer.toString(folder == null ? EntityFolder.DEFAULT_SYNC : folder.sync_days));
                    if (folder != null && folder.keep_days == Integer.MAX_VALUE)
                        cbKeepAll.setChecked(true);
                    else
                        etKeepDays.setText(Integer.toString(folder == null ? EntityFolder.DEFAULT_KEEP : folder.keep_days));
                    cbAutoDelete.setChecked(folder == null ? false : folder.auto_delete);
                    cbAutoDelete.setVisibility(folder != null && EntityFolder.TRASH.equals(folder.type) ? View.VISIBLE : View.GONE);
                }

                // Consider previous save as cancelled
                pbWait.setVisibility(View.GONE);

                Helper.setViewsEnabled(view, true);

                etName.setEnabled(folder == null);
                cbPoll.setEnabled(cbSynchronize.isChecked());
                cbDownload.setEnabled(cbSynchronize.isChecked());
                etKeepDays.setEnabled(!cbKeepAll.isChecked());
                cbAutoDelete.setEnabled(!cbKeepAll.isChecked());
                btnSave.setEnabled(true);

                subscribed = (folder == null ? null : folder.subscribed != null && folder.subscribed);
                deletable = (folder != null && EntityFolder.USER.equals(folder.type));
                getActivity().invalidateOptionsMenu();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, args, "folder:get");
    }
}
