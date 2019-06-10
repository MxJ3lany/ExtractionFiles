/**
 * Main application activity.
 * This is the screen displayed when you open the application
 * <p>
 * Copyright (C) 2009-2011  Rodrigo Zechin Rosauro
 * Copyright (C) 2011-2012  Umakanthan Chandran
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Rodrigo Zechin Rosauro, Umakanthan Chandran
 * @version 1.1
 */

package dev.ukanth.ufirewall;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.raizlabs.android.dbflow.config.FlowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import dev.ukanth.ufirewall.Api.PackageInfoData;
import dev.ukanth.ufirewall.activity.CustomScriptActivity;
import dev.ukanth.ufirewall.activity.HelpActivity;
import dev.ukanth.ufirewall.activity.LogActivity;
import dev.ukanth.ufirewall.activity.OldLogActivity;
import dev.ukanth.ufirewall.activity.RulesActivity;
import dev.ukanth.ufirewall.log.Log;
import dev.ukanth.ufirewall.log.LogPreference;
import dev.ukanth.ufirewall.log.LogPreferenceDB;
import dev.ukanth.ufirewall.preferences.PreferencesActivity;
import dev.ukanth.ufirewall.profiles.ProfileData;
import dev.ukanth.ufirewall.profiles.ProfileHelper;
import dev.ukanth.ufirewall.service.FirewallService;
import dev.ukanth.ufirewall.service.RootCommand;
import dev.ukanth.ufirewall.util.AppListArrayAdapter;
import dev.ukanth.ufirewall.util.FileDialog;
import dev.ukanth.ufirewall.util.G;
import dev.ukanth.ufirewall.util.ImportApi;
import dev.ukanth.ufirewall.util.PackageComparator;
import dev.ukanth.ufirewall.util.SecurityUtil;
import eu.chainfire.libsuperuser.Shell;
import haibison.android.lockpattern.utils.AlpSettings;

import static dev.ukanth.ufirewall.util.G.ctx;
import static dev.ukanth.ufirewall.util.G.isDonate;
import static dev.ukanth.ufirewall.util.SecurityUtil.LOCK_VERIFICATION;
import static dev.ukanth.ufirewall.util.SecurityUtil.REQ_ENTER_PATTERN;
import static haibison.android.lockpattern.LockPatternActivity.RESULT_FAILED;
import static haibison.android.lockpattern.LockPatternActivity.RESULT_FORGOT_PATTERN;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnClickListener, SwipeRefreshLayout.OnRefreshListener,
        RadioGroup.OnCheckedChangeListener {


    private static final int SHOW_ABOUT_RESULT = 1200;
    private static final int PREFERENCE_RESULT = 1205;
    private static final int SHOW_CUSTOM_SCRIPT = 1201;
    private static final int SHOW_RULES_ACTIVITY = 1202;
    private static final int SHOW_LOGS_ACTIVITY = 1203;
    private static final int VERIFY_CHECK = 10000;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 2;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE_ASSET = 3;
    public static boolean dirty = false;
    private static Menu mainMenu;
    private ListView listview = null;
    private MaterialDialog plsWait;
    private ArrayAdapter<String> spinnerAdapter = null;
    private SwipeRefreshLayout mSwipeLayout;
    private int index;
    private int top;
    private List<String> mlocalList = new ArrayList<>(new LinkedHashSet<String>());
    private int initDone = 0;
    private Spinner mSpinner;
    private MaterialDialog runProgress;
    private AlertDialog dialogLegend = null;

    private BroadcastReceiver uiProgressReceiver;
    private BroadcastReceiver toastReceiver;

    private Shell.Interactive rootShell = null;
    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
            showApplications(s.toString());
        }


        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            showApplications(s.toString());
        }

    };

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        MainActivity.dirty = dirty;
    }

    /**
     * Called when the activity is first created
     * .
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(G.getInstance());*/

        try {
            final int FLAG_HARDWARE_ACCELERATED = WindowManager.LayoutParams.class
                    .getDeclaredField("FLAG_HARDWARE_ACCELERATED").getInt(null);
            getWindow().setFlags(FLAG_HARDWARE_ACCELERATED,
                    FLAG_HARDWARE_ACCELERATED);
        } catch (Exception e) {
        }

        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setSupportActionBar(toolbar);

        this.findViewById(R.id.img_wifi).setOnClickListener(this);
        this.findViewById(R.id.img_reset).setOnClickListener(this);
        this.findViewById(R.id.img_invert).setOnClickListener(this);

        AlpSettings.Display.setStealthMode(getApplicationContext(), G.enableStealthPattern());
        AlpSettings.Display.setMaxRetries(getApplicationContext(), G.getMaxPatternTry());

        Api.assertBinaries(this, true);

        initDone = 0;
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);

        //queue = new HashSet<>();

        if (!G.hasRoot()) {
            (new RootCheck()).setContext(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            startRootShell(rootShell);
            new SecurityUtil(MainActivity.this).passCheck();
            registerNetworkObserver();
        }
        //registerQuickApply();
        registerUIbroadcast();
        registerToastbroadcast();
        migrateNotification();
        //checkAndAskForBatteryOptimization();
    }


    private void registerNetworkObserver() {
        startService(new Intent(getBaseContext(), FirewallService.class));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (action == null) {
            return;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private void checkAndAskForBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                new MaterialDialog.Builder(MainActivity.this).cancelable(false)
                        .title(R.string.battery_optimization_title)
                        .content(R.string.battery_optimization_desc)
                        .onPositive((dialog, which) -> {
                            try {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getApplicationContext(), "Unable to open battery optimisation screen. Please add it manually", Toast.LENGTH_LONG).show();
                            }
                        })
                        .onNegative((dialog, which) -> {
                            dialog.dismiss();
                        })
                        .positiveText(R.string.Continue)
                        .negativeText(R.string.exit)
                        .show();
            }
        }
    }

    private void migrateNotification() {
        try {
            if (!G.isNotificationMigrated()) {
                List<Integer> idList = G.getBlockedNotifyList();
                for (Integer uid : idList) {
                    LogPreference preference = new LogPreference();
                    preference.setUid(uid);
                    preference.setTimestamp(System.currentTimeMillis());
                    preference.setDisable(true);
                    FlowManager.getDatabase(LogPreferenceDB.class).beginTransactionAsync(databaseWrapper -> preference.save(databaseWrapper)).build().execute();
                }
                G.isNotificationMigrated(true);
            }
        } catch (Exception e) {
            Log.e(G.TAG, "Unable to migrate notification", e);
        }

    }

    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_STORAGE_ASSET);
        }
    }

    private void registerToastbroadcast() {
        IntentFilter filter = new IntentFilter("TOAST");
        toastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Api.toast(getApplicationContext(), intent.getExtras().get("MSG") != null ? intent.getExtras().get("MSG").toString() : "", Toast.LENGTH_SHORT);
            }
        };
        registerReceiver(toastReceiver, filter);
    }

    private void registerUIbroadcast() {
        IntentFilter filter = new IntentFilter("UPDATEUI");

        uiProgressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String rules = G.enableIPv6() ? " (v4 & v6) " : " (v4) ";
                if (runProgress != null) {
                    runProgress.setContent(context.getString(R.string.applying) + rules + intent.getExtras().get("INDEX") + "/" + intent.getExtras().get("SIZE"));
                }
            }
        };
        registerReceiver(uiProgressReceiver, filter);
    }

    /**
     * Register quick apply from main screen
     */
    private void registerQuickApply() {
       /* fab = (FloatingActionButton) findViewById(R.id.fab);
        if (showQuickButton()) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //lets save the rules
                if (queue != null && !queue.isEmpty()) {
                    List<PackageInfoData> apps = new ArrayList<>(queue);
                    for (PackageInfoData data : apps) {
                        Log.i(TAG, data.pkgName + " " + data.uid);
                    }
                    Api.RuleDataSet existingRuleSet  = Api.getExistingRuleSet();
                    Api.RuleDataSet ruleData = Api.generateRules(getApplicationContext(), apps, false);
                    Api.RuleDataSet merged = Api.merge(existingRuleSet, ruleData);
                    Log.i(TAG, "Generated RuleIDs: " + merged.toString());

                    queue.clear();
                    new RunQuickApply().setDataSet(merged).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    //save the rules
                    Api.generateRules(getApplicationContext(),  Api.getApps(getApplicationContext(),null), true);
                }
            }
        });*/
    }

    @Override
    public void onRefresh() {
        index = 0;
        top = 0;
        Api.applications = null;
        showOrLoadApplications();
        mSwipeLayout.setRefreshing(false);
    }

    private void updateRadioFilter() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.appFilterGroup);
        if (G.showFilter()) {
            switch (G.selectedFilter()) {
                case 0:
                    radioGroup.check(R.id.rpkg_core);
                    break;
                case 1:
                    radioGroup.check(R.id.rpkg_sys);
                    break;
                case 2:
                    radioGroup.check(R.id.rpkg_user);
                    break;
                default:
                    radioGroup.check(R.id.rpkg_all);
                    break;
            }
        } else {
            radioGroup.check(R.id.rpkg_all);
        }
        radioGroup.setOnCheckedChangeListener(this);
    }

    private void selectFilterGroup() {
        if (G.showFilter()) {
            RadioGroup radioGroup = (RadioGroup) findViewById(R.id.appFilterGroup);
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.rpkg_core:
                    filterApps(2);
                    break;
                case R.id.rpkg_sys:
                    filterApps(0);
                    break;
                case R.id.rpkg_user:
                    filterApps(1);
                    break;
                default:
                    filterApps(-1);
                    break;
            }
        } else {
            filterApps(-1);
        }

    }

    /**
     * Filter application based on app tpe
     *
     * @param i
     */
    private void filterApps(int i) {
        Set<PackageInfoData> returnList = new HashSet<>();
        List<PackageInfoData> inputList;
        List<PackageInfoData> allApps = Api.getApps(getApplicationContext(), null);
        if (i >= 0) {
            for (PackageInfoData infoData : allApps) {
                if (infoData != null) {
                    if (infoData.appType == i) {
                        returnList.add(infoData);
                    }
                }
            }
            inputList = new ArrayList<>(returnList);
        } else {
            inputList = allApps;
        }

        try {
            Collections.sort(inputList, new PackageComparator());
        } catch (Exception e) {
            Log.d(Api.TAG, "Exception in filter Sorting");
        }

        ArrayAdapter appAdapter = new AppListArrayAdapter(this, getApplicationContext(), inputList);
        this.listview.setAdapter(appAdapter);
        appAdapter.notifyDataSetChanged();
        // restore
        this.listview.setSelectionFromTop(index, top);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private void updateIconStatus() {
        if (Api.isEnabled(getApplicationContext())) {
            getSupportActionBar().setIcon(R.drawable.notification);
        } else {
            getSupportActionBar().setIcon(R.drawable.notification_error);
        }
    }

    private void startRootShell(Shell.Interactive rootShell) {
        if (rootShell == null) {
            List<String> cmds = new ArrayList<String>();
            cmds.add("true");
            new RootCommand().setFailureToast(R.string.error_su)
                    .setReopenShell(true)
                    .setCallback(new RootCommand.Callback() {
                        public void cbFunc(RootCommand state) {
                            //failed to acquire root
                            if (state.exitCode != 0) {
                                runOnUiThread(() -> {
                                    disableFirewall();
                                    showRootNotFoundMessage();
                                });
                            }
                        }
                    }).run(getApplicationContext(), cmds);
        }

       /* if (G.activeNotification()) {
            Api.showNotification(Api.isEnabled(getApplicationContext()), getApplicationContext());
        }*/
    }

    private void showRootNotFoundMessage() {
        if (G.isActivityVisible()) {
            try {
                new MaterialDialog.Builder(this).cancelable(false)
                        .title(R.string.error_common)
                        .content(R.string.error_su)
                        .onPositive((dialog, which) -> dialog.dismiss())
                        .onNegative((dialog, which) -> {
                            MainActivity.this.finish();
                            android.os.Process.killProcess(android.os.Process.myPid());
                            dialog.dismiss();
                        })
                        .positiveText(R.string.Continue)
                        .negativeText(R.string.exit)
                        .show();
            } catch (Exception e) {
                Api.toast(this, getString(R.string.error_su_toast), Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if (showQuickButton()) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }*/
        G.activityResumed();
    }

    private void reloadPreferences() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        G.reloadPrefs();
        checkPreferences();
        //language
        Api.updateLanguage(getApplicationContext(), G.locale());

        if (this.listview == null) {
            this.listview = (ListView) this.findViewById(R.id.listview);
        }

        //verifyMultiProfile();
        refreshHeader();
        updateIconStatus();

        //make sure we cancel notification posted by app notification.
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(2);

        if (G.disableIcons()) {
            this.findViewById(R.id.imageHolder).setVisibility(View.GONE);
        } else {
            this.findViewById(R.id.imageHolder).setVisibility(View.VISIBLE);
        }

        if (G.showFilter()) {
            this.findViewById(R.id.filerOption).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.filerOption).setVisibility(View.GONE);
        }

        if (G.enableMultiProfile()) {
            this.findViewById(R.id.profileOption).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.profileOption).setVisibility(View.GONE);
        }
        if (G.enableRoam()) {
            addColumns(R.id.img_roam);
        } else {
            hideColumns(R.id.img_roam);
        }
        if (G.enableVPN()) {
            addColumns(R.id.img_vpn);
        } else {
            hideColumns(R.id.img_vpn);
        }

        if (!Api.isMobileNetworkSupported(getApplicationContext())) {
            ImageView view = (ImageView) this.findViewById(R.id.img_3g);
            view.setVisibility(View.GONE);

        } else {
            this.findViewById(R.id.img_3g).setOnClickListener(this);
        }

        if (G.enableLAN()) {
            addColumns(R.id.img_lan);
        } else {
            hideColumns(R.id.img_lan);
        }
        if (G.enableTor()) {
            addColumns(R.id.img_tor);
        } else {
            hideColumns(R.id.img_tor);
        }


        updateRadioFilter();

        if (G.enableMultiProfile()) {
            setupMultiProfile();
        }

        selectFilterGroup();
    }

    /**
     * This will be used to migrate the profiles to a better one ( get ridoff of default profile )
     */


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rpkg_all:
                filterApps(-1);
                G.saveSelectedFilter(99);
                break;
            case R.id.rpkg_core:
                filterApps(2);
                G.saveSelectedFilter(0);
                break;
            case R.id.rpkg_sys:
                filterApps(0);
                G.saveSelectedFilter(1);
                break;
            case R.id.rpkg_user:
                filterApps(1);
                G.saveSelectedFilter(2);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initDone = 0;
        //startRootShell();
        reloadPreferences();
        //registerNetwork();
    }

    private void addColumns(int id) {
        ImageView view = (ImageView) this.findViewById(id);
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(this);
    }

    private void hideColumns(int id) {
        ImageView view = (ImageView) this.findViewById(id);
        view.setVisibility(View.GONE);
        view.setOnClickListener(this);
    }

    private void setupMultiProfile() {
        reloadProfileList(true);
        mSpinner = (Spinner) findViewById(R.id.profileGroup);
        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                mlocalList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(this);
        String currentProfile = G.storedProfile();
        if (currentProfile != null) {
            if (!G.isProfileMigrated()) {
                switch (currentProfile) {
                    case Api.DEFAULT_PREFS_NAME:
                        mSpinner.setSelection(0);
                        break;
                    case "AFWallProfile1":
                        mSpinner.setSelection(1);
                        break;
                    case "AFWallProfile2":
                        mSpinner.setSelection(2);
                        break;
                    case "AFWallProfile3":
                        mSpinner.setSelection(3);
                        break;
                    default:
                        mSpinner.setSelection(spinnerAdapter.getPosition(currentProfile), false);
                }
            } else {
                if (!currentProfile.equals(Api.DEFAULT_PREFS_NAME)) {
                    ProfileData data = ProfileHelper.getProfileByIdentifier(currentProfile);
                    if (data != null) {
                        mSpinner.setSelection(spinnerAdapter.getPosition(data.getName()), false);
                    }
                } else {
                    mSpinner.setSelection(spinnerAdapter.getPosition(currentProfile), false);
                }
            }
        }
    }

    private void reloadProfileList(boolean reset) {
        if (reset) {
            mlocalList = new ArrayList<>(new LinkedHashSet<String>());
        }

        mlocalList.add(G.gPrefs.getString("default", getString(R.string.defaultProfile)));

        if (!G.isProfileMigrated()) {
            mlocalList.add(G.gPrefs.getString("profile1", getString(R.string.profile1)));
            mlocalList.add(G.gPrefs.getString("profile2", getString(R.string.profile2)));
            mlocalList.add(G.gPrefs.getString("profile3", getString(R.string.profile3)));
            List<String> profilesList = G.getAdditionalProfiles();
            for (String profiles : profilesList) {
                if (profiles != null && profiles.length() > 0) {
                    mlocalList.add(profiles);
                }
            }
        } else {
            List<ProfileData> profilesList = ProfileHelper.getProfiles();
            for (ProfileData data : profilesList) {
                mlocalList.add(data.getName());
            }
        }
    }

    public void deviceCheck() {
        if (Build.VERSION.SDK_INT >= 21) {
            if ((G.isDoKey(getApplicationContext()) || isDonate())) {
                KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if (keyguardManager.isKeyguardSecure()) {
                    Intent createConfirmDeviceCredentialIntent = keyguardManager.createConfirmDeviceCredentialIntent(null, null);
                    if (createConfirmDeviceCredentialIntent != null) {
                        try {
                            startActivityForResult(createConfirmDeviceCredentialIntent, LOCK_VERIFICATION);
                        } catch (ActivityNotFoundException e) {
                        }
                    }
                } else {
                    Toast.makeText(this, getText(R.string.android_version), Toast.LENGTH_SHORT).show();
                }
            } else {
                Api.donateDialog(MainActivity.this, true);
            }
        }
    }

    /* private boolean passCheck() {
         if (G.enableDeviceCheck()) {
             deviceCheck();
         } else {
             switch (G.protectionLevel()) {
                 case "p0":
                     return true;
                 case "p1":
                     final String oldpwd = G.profile_pwd();
                     if (oldpwd.length() == 0) {
                         return true;
                     } else {
                         // Check the password
                         requestPassword();
                     }
                     break;
                 case "p2":
                     final String pwd = G.sPrefs.getString("LockPassword", "");
                     if (pwd.length() == 0) {
                         return true;
                     } else {
                         requestPassword();
                     }
                     break;
                 case "p3":

                     if (FingerprintUtil.isAndroidSupport() && G.isFingerprintEnabled()) {

                         requestFingerprint();
                     }
             }
         }
         return false;
     }
 */
    @Override
    protected void onPause() {
        super.onPause();
        try {
            if ((plsWait != null) && plsWait.isShowing()) {
                plsWait.dismiss();
            }

        } catch (final IllegalArgumentException e) {
            // Handle or log or ignore
        } catch (final Exception e) {
            // Handle or log or ignore
        } finally {
            plsWait = null;
        }
        //this.listview.setAdapter(null);
        //mLastPause = Syst em.currentTimeMillis();
        //isOnPause = true;
        //checkForProfile = true;
        index = this.listview.getFirstVisiblePosition();
        View v = this.listview.getChildAt(0);
        top = (v == null) ? 0 : v.getTop();
        G.activityPaused();
    }

    /**
     * Check if the stored preferences are OK
     */
    private void checkPreferences() {
        final Editor editor = G.pPrefs.edit();
        boolean changed = false;
        if (G.pPrefs.getString(Api.PREF_MODE, "").length() == 0) {
            editor.putString(Api.PREF_MODE, Api.MODE_WHITELIST);
            changed = true;
        }
        if (changed)
            editor.commit();
    }


    /*private void selectMode() {
        final Resources res = getResources();

        new MaterialDialog.Builder(this)
                .title(R.string.selectMode)
                .cancelable(true)
                .items(new String[]{
                        res.getString(R.string.mode_whitelist),
                        res.getString(R.string.mode_blacklist)})
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        final String mode = (which == 0 ? Api.MODE_WHITELIST : Api.MODE_BLACKLIST);
                        final Editor editor = getSharedPreferences(Api.PREFS_NAME, 0).edit();
                        editor.putString(Api.PREF_MODE, mode);
                        editor.commit();
                        refreshHeader();
                    }
                })
                .show();
    }*/


    /**
     * Request the password lock before displayed the main screen.
     */
    /*private void requestPassword() {
        switch (G.protectionLevel()) {
            case "p1":
                new MaterialDialog.Builder(MainActivity.this).cancelable(false)
                        .title(R.string.pass_titleget).autoDismiss(false)
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        .positiveText(R.string.submit)
                        .negativeText(R.string.Cancel)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                MainActivity.this.finish();
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        })
                        .input(R.string.enterpass, R.string.password_empty, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                String pass = input.toString();
                                boolean isAllowed = false;
                                if (G.isEnc()) {
                                    String decrypt = Api.unhideCrypt("AFW@LL_P@SSWORD_PR0T3CTI0N", G.profile_pwd());
                                    if (decrypt != null) {
                                        if (decrypt.equals(pass)) {
                                            isAllowed = true;
                                        }
                                    }
                                } else {
                                    if (pass.equals(G.profile_pwd())) {
                                        isAllowed = true;
                                    }
                                }
                                if (isAllowed) {
                                    showOrLoadApplications();
                                    dialog.dismiss();
                                } else {
                                    Api.toast(MainActivity.this, getString(R.string.wrong_password));
                                }


                            }
                        }).show();
                break;
            case "p2":
                Intent intent = new Intent(ACTION_COMPARE_PATTERN, null, getApplicationContext(), LockPatternActivity.class);
                String savedPattern = G.sPrefs.getString("LockPassword", "");
                intent.putExtra(EXTRA_PATTERN, savedPattern.toCharArray());
                startActivityForResult(intent, REQ_ENTER_PATTERN);
                break;
        }

    }

    *//**
     * Request the fingerprint lock before displayed the main screen.
     *//*
    private void requestFingerprint() {
        FingerprintUtil.FingerprintDialog dialog = new FingerprintUtil.FingerprintDialog(this);
        dialog.setOnFingerprintFailureListener(new FingerprintUtil.OnFingerprintFailure() {
            @Override
            public void then() {
                MainActivity.this.finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        dialog.show();
    }*/

    /**
     * Refresh informative header
     */
    private void refreshHeader() {
        final String mode = G.pPrefs.getString(Api.PREF_MODE, Api.MODE_WHITELIST);
        //final TextView labelmode = (TextView) this.findViewById(R.id.label_mode);
        final Resources res = getResources();

        if (mode.equals(Api.MODE_WHITELIST)) {
            if (mainMenu != null) {
                mainMenu.findItem(R.id.allowmode).setChecked(true);
                mainMenu.findItem(R.id.menu_mode).setIcon(R.drawable.ic_allow);
            }
        } else {
            if (mainMenu != null) {
                mainMenu.findItem(R.id.blockmode).setChecked(true);
                mainMenu.findItem(R.id.menu_mode).setIcon(R.drawable.ic_deny);
            }
        }
        //int resid = (mode.equals(Api.MODE_WHITELIST) ? R.string.mode_whitelist: R.string.mode_blacklist);
        //labelmode.setText(res.getString(R.string.mode_header, res.getString(resid)));
    }

    /**
     * If the applications are cached, just show them, otherwise load and show
     */
    private void showOrLoadApplications() {
        //nocache!!
        GetAppList getAppList = new GetAppList();
        if (plsWait == null && (getAppList.getStatus() == AsyncTask.Status.PENDING || getAppList.getStatus() == AsyncTask.Status.FINISHED)) {
            getAppList.setContext(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        initDone = initDone + 1;
        if (initDone > 1) {
            Spinner spinner = (Spinner) findViewById(R.id.profileGroup);
            String profileName = spinner.getSelectedItem().toString();
            if (!G.isProfileMigrated()) {
                switch (position) {
                    case 0:
                        G.setProfile(true, "AFWallPrefs");
                        break;
                    case 1:
                        G.setProfile(true, "AFWallProfile1");
                        break;
                    case 2:
                        G.setProfile(true, "AFWallProfile2");
                        break;
                    case 3:
                        G.setProfile(true, "AFWallProfile3");
                        break;
                    default:
                        if (profileName != null) {
                            G.setProfile(true, profileName);
                        }

                }
                setDirty(true);
            } else {
                switch (position) {
                    case 0:
                        G.setProfile(true, "AFWallPrefs");
                        break;
                    default:
                        if (profileName != null) {
                            ProfileData data = ProfileHelper.getProfileByName(profileName);
                            G.setProfile(true, data.getIdentifier());
                        }
                }
                setDirty(true);
            }
            G.reloadProfile();
            refreshHeader();
            showOrLoadApplications();
            if (G.applyOnSwitchProfiles()) {
                applyOrSaveRules();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    ;

    /**
     * Show the list of applications
     */
    private void showApplications(final String searchStr) {

        setDirty(false);

        List<PackageInfoData> searchApp = new ArrayList<>();
        HashSet<Integer> unique = new HashSet<>();
        final List<PackageInfoData> apps = Api.getApps(this, null);
        boolean isResultsFound = false;

        if (searchStr != null && searchStr.length() > 1) {
            for (PackageInfoData app : apps) {
                for (String str : app.names) {
                    if (str != null && searchStr != null) {
                        if (str.contains(searchStr.toLowerCase()) || str.toLowerCase().contains(searchStr.toLowerCase())
                                && !searchApp.contains(app) || (G.showUid() && (str + " " + app.uid).contains(searchStr) && !unique.contains(app.uid))) {
                            searchApp.add(app);
                            unique.add(app.uid);
                            isResultsFound = true;
                        }
                    }
                }
            }
        }

        List<PackageInfoData> apps2 = null;
        if (searchStr != null && searchStr.equals("")) {
            apps2 = apps;
        } else if (isResultsFound || searchApp.size() > 0) {
            apps2 = searchApp;
        }
        // Sort applications - selected first, then alphabetically
        try {
            if (apps2 != null) {
                Collections.sort(apps2, new PackageComparator());
                this.listview.setAdapter(new AppListArrayAdapter(this, getApplicationContext(), apps2));
                // restore
                this.listview.setSelectionFromTop(index, top);
            }
        } catch (Exception e) {
            Log.d(Api.TAG, "Exception on Sorting");
        }
    }

    @Override
    public boolean onSearchRequested() {
        MenuItem menuItem = mainMenu.findItem(R.id.menu_search); // R.string.search is the id of the searchview
        if (menuItem != null) {
            if (menuItem.isActionViewExpanded()) {
                menuItem.collapseActionView();
            } else {
                menuItem.expandActionView();
                search(menuItem);
            }
        }
        return super.onSearchRequested();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //language
        Api.updateLanguage(getApplicationContext(), G.locale());
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_bar, menu);

        // Get widget's instance
        mainMenu = menu;
        //make sure we update sort entry
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (G.sortBy()) {
                    case "s0":
                        mainMenu.findItem(R.id.sort_default).setChecked(true);
                        break;
                    case "s1":
                        mainMenu.findItem(R.id.sort_lastupdate).setChecked(true);
                        break;
                    case "s2":
                        mainMenu.findItem(R.id.sort_uid).setChecked(true);
                        break;
                }

                refreshHeader();
            }
        });
        return true;
    }

    public void menuSetApplyOrSave(final Menu menu, final boolean isEnabled) {
        runOnUiThread(() -> {
            if (menu != null) {
                if (isEnabled) {
                    menu.findItem(R.id.menu_toggle).setTitle(R.string.fw_disabled).setIcon(R.drawable.notification_error);
                    menu.findItem(R.id.menu_apply).setTitle(R.string.applyrules);
                    getSupportActionBar().setIcon(R.drawable.notification);
                } else {
                    menu.findItem(R.id.menu_toggle).setTitle(R.string.fw_enabled).setIcon(R.drawable.notification);
                    menu.findItem(R.id.menu_apply).setTitle(R.string.saverules);
                    getSupportActionBar().setIcon(R.drawable.notification_error);
                }
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        //language
        Api.updateLanguage(getApplicationContext(), G.locale());
        if (menu != null) {
            menuSetApplyOrSave(mainMenu, Api.isEnabled(MainActivity.this));
        }
        return true;
    }

    private void disableFirewall() {
        Api.setEnabled(this, false, true);
        menuSetApplyOrSave(MainActivity.this.mainMenu, false);
    }

    private void disableOrEnable() {
        final boolean enabled = !Api.isEnabled(this);
        Api.setEnabled(this, enabled, true);
        if (enabled) {
            applyOrSaveRules();
        } else {
            if (G.enableConfirm()) {
                confirmDisable();
            } else {
                purgeRules();
            }
        }
        refreshHeader();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);
        MenuItem menuItem;
        switch (item.getItemId()) {

		/*case android.R.id.home:
            disableOrEnable();
	        return true;*/
            case R.id.menu_legend:
                LayoutInflater inflater = LayoutInflater.from(this);
                View view = inflater.inflate(R.layout.legend, null, false);
                dialogLegend = new AlertDialog.Builder(this)
                        .setView(view)
                        .setCancelable(true)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                dialogLegend = null;
                            }
                        })
                        .create();
                dialogLegend.show();
                return true;
            case R.id.menu_toggle:
                disableOrEnable();
                return true;
            case R.id.allowmode:
                item.setChecked(true);
                Editor editor = getSharedPreferences(Api.PREFS_NAME, 0).edit();
                editor.putString(Api.PREF_MODE, Api.MODE_WHITELIST);
                editor.commit();
                refreshHeader();
                return true;
            case R.id.blockmode:
                item.setChecked(true);
                Editor editor2 = getSharedPreferences(Api.PREFS_NAME, 0).edit();
                editor2.putString(Api.PREF_MODE, Api.MODE_BLACKLIST);
                editor2.commit();
                refreshHeader();
                return true;
            case R.id.sort_default:
                G.sortBy("s0");
                item.setChecked(true);
                Api.applications = null;
                showOrLoadApplications();
                return true;
            case R.id.sort_lastupdate:
                G.sortBy("s1");
                item.setChecked(true);
                Api.applications = null;
                showOrLoadApplications();
                return true;
            case R.id.sort_uid:
                G.sortBy("s2");
                item.setChecked(true);
                Api.applications = null;
                showOrLoadApplications();
                return true;
            case R.id.menu_apply:
                applyOrSaveRules();
                return true;
            case R.id.menu_exit:
                finish();
                //System.exit(0);
                return false;
            case R.id.menu_help:
                showAbout();
                return true;
            /*case R.id.menu_customrules:
                G.hidden()
                startCustomRules();
                return true;*/
            case R.id.menu_log:
                showLog();
                return true;
            case R.id.menu_rules:
                showRules();
                return true;
            case R.id.menu_setcustom:
                setCustomScript();
                return true;
            case R.id.menu_preference:
                showPreferences();
                return true;
        /*case R.id.menu_reload:
            Api.applications = null;
			showOrLoadApplications();
			return true;*/
            case R.id.menu_search:
                search(item);
                return true;
            case R.id.menu_export:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // permissions have not been granted.
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                } else {
                    showExportDialog();
                }
                return true;
            case R.id.menu_import:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // permissions have not been granted.
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_STORAGE);

                } else {
                    showImportDialog();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void search(MenuItem item) {
        item.setActionView(R.layout.searchbar);
        final EditText filterText = (EditText) item.getActionView().findViewById(
                R.id.searchApps);
        filterText.addTextChangedListener(filterTextWatcher);
        filterText.setEllipsize(TruncateAt.END);
        filterText.setSingleLine();

        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                selectFilterGroup();
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                filterText.post(() -> {
                    filterText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(filterText, InputMethodManager.SHOW_IMPLICIT);
                });
                return true;  // Return true to expand action view
            }
        });
    }

    private void showImportDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.imports)
                .cancelable(false)
                .items(new String[]{
                        getString(R.string.import_rules),
                        getString(R.string.import_all),
                        getString(R.string.import_rules_droidwall)})
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                //Intent intent = new Intent(MainActivity.this, FileChooserActivity.class);
                                //startActivityForResult(intent, FILE_CHOOSER_LOCAL);
                                File mPath = new File(Environment.getExternalStorageDirectory() + "//afwall//");
                                FileDialog fileDialog = new FileDialog(MainActivity.this, mPath, true);
                                //fileDialog.setFlag(true);
                                //fileDialog.setFileEndsWith(new String[] {"backup", "afwall-backup"}, "all");
                                fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                                    public void fileSelected(File file) {
                                        String fileSelected = file.toString();
                                        StringBuilder builder = new StringBuilder();
                                        if (Api.loadSharedPreferencesFromFile(MainActivity.this, builder, fileSelected, false)) {
                                            Api.applications = null;
                                            showOrLoadApplications();
                                            Api.toast(MainActivity.this, getString(R.string.import_rules_success) + fileSelected);
                                        } else {
                                            if (builder.toString().equals("")) {
                                                Api.toast(MainActivity.this, getString(R.string.import_rules_fail));
                                            } else {
                                                Api.toast(MainActivity.this, builder.toString());
                                            }
                                        }
                                    }
                                });
                                fileDialog.showDialog();
                                break;
                            case 1:

                                if (G.isDoKey(getApplicationContext()) || isDonate()) {

                                    File mPath2 = new File(Environment.getExternalStorageDirectory() + "//afwall//");
                                    FileDialog fileDialog2 = new FileDialog(MainActivity.this, mPath2, false);
                                    //fileDialog2.setFlag(false);
                                    //fileDialog2.setFileEndsWith(new String[] {"backup_all", "afwall-backup-all"}, "" );
                                    fileDialog2.addFileListener(new FileDialog.FileSelectedListener() {
                                        public void fileSelected(File file) {
                                            String fileSelected = file.toString();
                                            StringBuilder builder = new StringBuilder();
                                            if (Api.loadSharedPreferencesFromFile(MainActivity.this, builder, fileSelected, true)) {
                                                Api.applications = null;
                                                showOrLoadApplications();
                                                Api.toast(MainActivity.this, getString(R.string.import_rules_success) + fileSelected);
                                                Intent intent = getIntent();
                                                finish();
                                                startActivity(intent);
                                            } else {
                                                if (builder.toString().equals("")) {
                                                    Api.toast(MainActivity.this, getString(R.string.import_rules_fail));
                                                } else {
                                                    Api.toast(MainActivity.this, builder.toString());
                                                }
                                            }
                                        }
                                    });
                                    fileDialog2.showDialog();
                                } else {
                                    Api.donateDialog(MainActivity.this, false);
                                }
                                break;
                            case 2:

                                new MaterialDialog.Builder(MainActivity.this).cancelable(false)
                                        .title(R.string.import_rules_droidwall)
                                        .content(R.string.overrideRules)
                                        .positiveText(R.string.Yes)
                                        .negativeText(R.string.No)
                                        .icon(getResources().getDrawable(R.drawable.ic_launcher))
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                if (ImportApi.loadSharedPreferencesFromDroidWall(MainActivity.this)) {
                                                    Api.applications = null;
                                                    showOrLoadApplications();
                                                    Api.toast(MainActivity.this, getString(R.string.import_rules_success) + Environment.getExternalStorageDirectory().getAbsolutePath() + "/afwall/");
                                                } else {
                                                    Api.toast(MainActivity.this, getString(R.string.import_rules_fail));
                                                }
                                            }
                                        })

                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                dialog.cancel();
                                            }
                                        })
                                        .show();


                                break;
                        }
                        return true;
                    }
                })
                .positiveText(R.string.imports)
                .negativeText(R.string.Cancel)
                .show();
    }

    private void showExportDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.exports)
                .cancelable(false)
                .items(new String[]{
                        getString(R.string.export_rules),
                        getString(R.string.export_all)})
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                Api.exportRulesToFileConfirm(MainActivity.this);
                                break;
                            case 1:
                                Api.exportAllPreferencesToFileConfirm(MainActivity.this);
                                break;
                        }
                        return true;
                    }
                }).positiveText(R.string.exports)
                .negativeText(R.string.Cancel)
                .show();
    }

    private void showPreferences() {
        Intent i = new Intent(this, PreferencesActivity.class);
        //startActivity(i);
        startActivityForResult(i, PREFERENCE_RESULT);
    }

    private void showAbout() {
        Intent i = new Intent(this, HelpActivity.class);
        startActivityForResult(i, SHOW_ABOUT_RESULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showExportDialog();
                } else {
                    Toast.makeText(this, R.string.permissiondenied_importexport, Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE_ASSET: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Api.assertBinaries(this, true);
                } else {
                    Toast.makeText(this, R.string.permissiondenied_asset, Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showImportDialog();
                } else {
                    Toast.makeText(this, R.string.permissiondenied_importexport, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void confirmDisable() {

        new MaterialDialog.Builder(this)
                .title(R.string.confirmMsg)
                //.content(R.string.confirmMsg)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        purgeRules();
                       /* if (G.activeNotification()) {
                            Api.showNotification(Api.isEnabled(getApplicationContext()), getApplicationContext());
                        }*/
                        Api.updateNotification(Api.isEnabled(getApplicationContext()), getApplicationContext());
                        //ServiceCompat.stopForeground(FirewallService.class,Service.STOP_FOREGROUND_REMOVE);
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Api.setEnabled(getApplicationContext(), true, true);
                        dialog.dismiss();
                    }
                })
                .positiveText(R.string.Yes)
                .negativeText(R.string.No)
                .show();
    }

    /**
     * Set a new init script
     */
    private void setCustomScript() {
        Intent intent = new Intent();
        intent.setClass(this, CustomScriptActivity.class);
        startActivityForResult(intent, SHOW_CUSTOM_SCRIPT);
    }

    private void startCustomRules() {
        if ((G.isDoKey(getApplicationContext()) || isDonate())) {
            Intent intent = new Intent();
            intent.setClass(this, CustomRulesActivity.class);
            startActivity(intent);
        } else {
            Api.donateDialog(MainActivity.this, false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOCK_VERIFICATION: {
                switch (resultCode) {
                    case RESULT_OK:
                        showOrLoadApplications();
                        break;
                    default:
                        MainActivity.this.finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        break;
                }
            }
            break;

            case VERIFY_CHECK: {
                Log.i(Api.TAG, "In VERIFY_CHECK");
                switch (resultCode) {
                    case RESULT_OK:
                        G.isDo(true);
                        break;
                    case RESULT_CANCELED:
                        G.isDo(false);
                }
            }
            break;

            case REQ_ENTER_PATTERN: {
                switch (resultCode) {
                    case RESULT_OK:
                        //isPassVerify = true;
                        showOrLoadApplications();
                        break;
                    case RESULT_CANCELED:
                        MainActivity.this.finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        break;
                    case RESULT_FAILED:
                        MainActivity.this.finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        break;
                    case RESULT_FORGOT_PATTERN:
                        MainActivity.this.finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        break;
                    default:
                        MainActivity.this.finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        break;
                }
            }
            break;
            case PREFERENCE_RESULT: {
                invalidateOptionsMenu();
            }
            break;
        }
        if (resultCode == RESULT_OK
                && data != null && Api.CUSTOM_SCRIPT_MSG.equals(data.getAction())) {
            final String script = data.getStringExtra(Api.SCRIPT_EXTRA);
            final String script2 = data.getStringExtra(Api.SCRIPT2_EXTRA);
            setCustomScript(script, script2);
        }
    }

    /**
     * Set a new init script
     *
     * @param script  new script (empty to remove)
     * @param script2 new "shutdown" script (empty to remove)
     */
    private void setCustomScript(String script, String script2) {
        final Editor editor = getSharedPreferences(Api.PREFS_NAME, 0).edit();
        // Remove unnecessary white-spaces, also replace '\r\n' if necessary
        script = script.trim().replace("\r\n", "\n");
        script2 = script2.trim().replace("\r\n", "\n");
        editor.putString(Api.PREF_CUSTOMSCRIPT, script);
        editor.putString(Api.PREF_CUSTOMSCRIPT2, script2);
        int msgid;
        if (editor.commit()) {
            if (script.length() > 0 || script2.length() > 0) {
                msgid = R.string.custom_script_defined;
            } else {
                msgid = R.string.custom_script_removed;
            }
        } else {
            msgid = R.string.custom_script_error;
        }
        Api.toast(MainActivity.this, MainActivity.this.getString(msgid));
        if (Api.isEnabled(this)) {
            // If the firewall is enabled, re-apply the rules
            applyOrSaveRules();
        }
    }

    /**
     * Show iptables rules on a dialog
     */
    private void showRules() {
        Intent i = new Intent(this, RulesActivity.class);
        startActivityForResult(i, SHOW_RULES_ACTIVITY);
    }

    /**
     * Show logs on a dialog
     */
    private void showLog() {
        if (G.oldLogView()) {
            Intent i = new Intent(this, OldLogActivity.class);
            startActivityForResult(i, SHOW_LOGS_ACTIVITY);
        } else {
            Intent i = new Intent(this, LogActivity.class);
            startActivityForResult(i, SHOW_LOGS_ACTIVITY);
        }
    }

    /**
     * Apply or save iptables rules, showing a visual indication
     */
    private void applyOrSaveRules() {
        final boolean enabled = Api.isEnabled(this);
        final Context ctx = getApplicationContext();

        Api.generateRules(ctx, Api.getApps(ctx, null), true);

        if (!enabled) {
            Api.setEnabled(ctx, false, true);
            Api.toast(ctx, ctx.getString(R.string.rules_saved));
            setDirty(false);
            return;
        }
        //Api.showNotification(Api.isEnabled(getApplicationContext()), getApplicationContext());
        Api.updateNotification(Api.isEnabled(getApplicationContext()), getApplicationContext());
        new RunApply().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Purge iptables rules, showing a visual indication
     */
    private void purgeRules() {
        new PurgeTask(MainActivity.this).execute();
        menuSetApplyOrSave(mainMenu, Api.isEnabled(getApplicationContext()));
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            /*case R.id.label_mode:
                selectMode();
				break;*/
            case R.id.img_wifi:
                selectActionConfirmation(v.getId());
                break;
            case R.id.img_3g:
                selectActionConfirmation(v.getId());
                break;
            case R.id.img_roam:
                selectActionConfirmation(v.getId());
                break;
            case R.id.img_vpn:
                selectActionConfirmation(v.getId());
                break;
            case R.id.img_lan:
                selectActionConfirmation(v.getId());
                break;
            case R.id.img_tor:
                selectActionConfirmation(v.getId());
                break;
            case R.id.img_invert:
                selectActionConfirmation(getString(R.string.reverse_all), v.getId());
                break;
            case R.id.img_reset:
                selectActionConfirmation(getString(R.string.unselect_all), v.getId());
                break;
            //case R.id.img_invert:
            //	revertApplications();
            //	break;
        }
    }

    private void selectAllLAN(boolean flag) {
        if (this.listview == null) {
            this.listview = (ListView) this.findViewById(R.id.listview);
        }
        ListAdapter adapter = listview.getAdapter();
        if (adapter != null) {
            int count = adapter.getCount(), item;
            for (item = 0; item < count; item++) {
                PackageInfoData data = (PackageInfoData) adapter.getItem(item);
                if (data.uid != Api.SPECIAL_UID_ANY) {
                    data.selected_lan = flag;
                    //addToQueue(data);
                }
                setDirty(true);
            }
            ((BaseAdapter) adapter).notifyDataSetChanged();
        }
    }

    private void selectAllTor(boolean flag) {
        if (this.listview == null) {
            this.listview = (ListView) this.findViewById(R.id.listview);
        }
        ListAdapter adapter = listview.getAdapter();
        if (adapter != null) {
            int count = adapter.getCount(), item;
            for (item = 0; item < count; item++) {
                PackageInfoData data = (PackageInfoData) adapter.getItem(item);
                if (data.uid != Api.SPECIAL_UID_ANY) {
                    data.selected_tor = flag;
                    //addToQueue(data);
                }
                setDirty(true);
            }
            ((BaseAdapter) adapter).notifyDataSetChanged();
        }
    }

    /**
     * Cache any batch event by user
     *
     * @param
     */
    /*public static void addToQueue(@NonNull PackageInfoData data) {
     *//*if (queue == null) {
            queue = new HashSet<>();
        }
        //add or update based on new data
        queue.add(data);
        getFab().setBackgroundTintList(ColorStateList.valueOf(Color.RED));*//*
    }*/
    private void selectAllVPN(boolean flag) {
        if (this.listview == null) {
            this.listview = (ListView) this.findViewById(R.id.listview);
        }
        ListAdapter adapter = listview.getAdapter();
        if (adapter != null) {
            int count = adapter.getCount(), item;
            for (item = 0; item < count; item++) {
                PackageInfoData data = (PackageInfoData) adapter.getItem(item);
                if (data.uid != Api.SPECIAL_UID_ANY) {
                    data.selected_vpn = flag;
                    //addToQueue(data);
                }
                setDirty(true);
            }
            ((BaseAdapter) adapter).notifyDataSetChanged();
        }
    }

    private void selectRevert(int flag) {
        if (this.listview == null) {
            this.listview = (ListView) this.findViewById(R.id.listview);
        }
        ListAdapter adapter = listview.getAdapter();
        if (adapter != null) {
            int count = adapter.getCount(), item;
            for (item = 0; item < count; item++) {
                PackageInfoData data = (PackageInfoData) adapter.getItem(item);
                if (data.uid != Api.SPECIAL_UID_ANY) {
                    switch (flag) {
                        case R.id.img_wifi:
                            data.selected_wifi = !data.selected_wifi;
                            break;
                        case R.id.img_3g:
                            data.selected_3g = !data.selected_3g;
                            break;
                        case R.id.img_roam:
                            data.selected_roam = !data.selected_roam;
                            break;
                        case R.id.img_vpn:
                            data.selected_vpn = !data.selected_vpn;
                            break;
                        case R.id.img_lan:
                            data.selected_lan = !data.selected_lan;
                            break;
                        case R.id.img_tor:
                            data.selected_tor = !data.selected_tor;
                            break;
                    }
                    //addToQueue(data);
                }
                setDirty(true);
            }
            ((BaseAdapter) adapter).notifyDataSetChanged();
        }
    }

    private void selectRevert() {
        if (this.listview == null) {
            this.listview = (ListView) this.findViewById(R.id.listview);
        }
        ListAdapter adapter = listview.getAdapter();
        if (adapter != null) {
            int count = adapter.getCount(), item;
            for (item = 0; item < count; item++) {
                PackageInfoData data = (PackageInfoData) adapter.getItem(item);
                if (data.uid != Api.SPECIAL_UID_ANY) {
                    data.selected_wifi = !data.selected_wifi;
                    data.selected_3g = !data.selected_3g;
                    data.selected_roam = !data.selected_roam;
                    data.selected_vpn = !data.selected_vpn;
                    data.selected_lan = !data.selected_lan;
                    data.selected_tor = !data.selected_tor;
                    //addToQueue(data);
                }
                setDirty(true);
            }
            ((BaseAdapter) adapter).notifyDataSetChanged();
        }
    }

    private void selectAllRoam(boolean flag) {
        if (this.listview == null) {
            this.listview = (ListView) this.findViewById(R.id.listview);
        }
        ListAdapter adapter = listview.getAdapter();
        if (adapter != null) {
            int count = adapter.getCount(), item;
            for (item = 0; item < count; item++) {
                PackageInfoData data = (PackageInfoData) adapter.getItem(item);
                if (data.uid != Api.SPECIAL_UID_ANY) {
                    data.selected_roam = flag;
                    //addToQueue(data);
                }
                setDirty(true);
            }
            ((BaseAdapter) adapter).notifyDataSetChanged();
        }
    }

    private void clearAll() {
        if (this.listview == null) {
            this.listview = (ListView) this.findViewById(R.id.listview);
        }
        ListAdapter adapter = listview.getAdapter();
        if (adapter != null) {
            int count = adapter.getCount(), item;
            for (item = 0; item < count; item++) {
                PackageInfoData data = (PackageInfoData) adapter.getItem(item);
                data.selected_wifi = false;
                data.selected_3g = false;
                data.selected_roam = false;
                data.selected_vpn = false;
                data.selected_lan = false;
                data.selected_tor = false;
                //addToQueue(data);
                setDirty(true);
            }
            ((BaseAdapter) adapter).notifyDataSetChanged();
        }
    }

    private void selectAll3G(boolean flag) {
        if (this.listview == null) {
            this.listview = (ListView) this.findViewById(R.id.listview);
        }
        ListAdapter adapter = listview.getAdapter();
        if (adapter != null) {
            int count = adapter.getCount(), item;
            for (item = 0; item < count; item++) {
                PackageInfoData data = (PackageInfoData) adapter.getItem(item);
                if (data.uid != Api.SPECIAL_UID_ANY) {
                    data.selected_3g = flag;
                    //addToQueue(data);
                }
                // addToQueue(data);
                setDirty(true);
            }
            ((BaseAdapter) adapter).notifyDataSetChanged();
        }

    }

    private void selectAllWifi(boolean flag) {
        if (this.listview == null) {
            this.listview = (ListView) this.findViewById(R.id.listview);
        }
        ListAdapter adapter = listview.getAdapter();
        int count = adapter.getCount(), item;
        if (adapter != null) {
            for (item = 0; item < count; item++) {
                PackageInfoData data = (PackageInfoData) adapter.getItem(item);
                if (data.uid != Api.SPECIAL_UID_ANY) {
                    data.selected_wifi = flag;
                    // addToQueue(data);
                }
                setDirty(true);
            }
            ((BaseAdapter) adapter).notifyDataSetChanged();
        }
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    if (mainMenu != null) {
                        mainMenu.performIdentifierAction(R.id.menu_list_item, 0);
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if (isDirty()) {
                        new MaterialDialog.Builder(this)
                                .title(R.string.confirmation)
                                .cancelable(false)
                                .content(R.string.unsaved_changes_message)
                                .positiveText(R.string.apply)
                                .negativeText(R.string.discard)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        applyOrSaveRules();
                                        dialog.dismiss();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        setDirty(false);
                                        Api.applications = null;
                                        //finish();
                                        //System.exit(0);
                                        //force reload rules.
                                        MainActivity.super.onKeyDown(keyCode, event);
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        return true;

                    } else {
                        setDirty(false);
                        //finish();
                        //System.exit(0);
                    }


            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * @param i
     */

    private void selectActionConfirmation(String displayMessage, final int i) {

        new MaterialDialog.Builder(this)
                .title(R.string.confirmation).content(displayMessage)
                .cancelable(true)
                .positiveText(R.string.OK)
                .negativeText(R.string.Cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (i) {
                            case R.id.img_invert:
                                selectRevert();
                                break;
                            case R.id.img_reset:
                                clearAll();
                        }
                        dialog.dismiss();
                    }
                })

                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void selectActionConfirmation(final int i) {

        new MaterialDialog.Builder(this)
                .title(R.string.select_action)
                .cancelable(true)
                .items(new String[]{
                        getString(R.string.check_all),
                        getString(R.string.invert_all),
                        getString(R.string.uncheck_all)})
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                switch (i) {
                                    case R.id.img_wifi:
                                        dialog.setTitle(text + getString(R.string.wifi));
                                        selectAllWifi(true);
                                        break;
                                    case R.id.img_3g:
                                        dialog.setTitle(text + getString(R.string.data));
                                        selectAll3G(true);
                                        break;
                                    case R.id.img_roam:
                                        dialog.setTitle(text + getString(R.string.roam));
                                        selectAllRoam(true);
                                        break;
                                    case R.id.img_vpn:
                                        dialog.setTitle(text + getString(R.string.vpn));
                                        selectAllVPN(true);
                                        break;
                                    case R.id.img_lan:
                                        dialog.setTitle(text + getString(R.string.lan));
                                        selectAllLAN(true);
                                        break;
                                    case R.id.img_tor:
                                        dialog.setTitle(text + getString(R.string.tor));
                                        selectAllTor(true);
                                        break;
                                }
                                break;
                            case 1:
                                switch (i) {
                                    case R.id.img_wifi:
                                        dialog.setTitle(text + getString(R.string.wifi));
                                        break;
                                    case R.id.img_3g:
                                        dialog.setTitle(text + getString(R.string.data));
                                        break;
                                    case R.id.img_roam:
                                        dialog.setTitle(text + getString(R.string.roam));
                                        break;
                                    case R.id.img_vpn:
                                        dialog.setTitle(text + getString(R.string.vpn));
                                        break;
                                    case R.id.img_lan:
                                        dialog.setTitle(text + getString(R.string.lan));
                                        break;
                                    case R.id.img_tor:
                                        dialog.setTitle(text + getString(R.string.tor));
                                        break;
                                }
                                selectRevert(i);
                                dirty = true;
                                break;
                            case 2:
                                switch (i) {
                                    case R.id.img_wifi:
                                        dialog.setTitle(text + getString(R.string.wifi));
                                        selectAllWifi(false);
                                        break;
                                    case R.id.img_3g:
                                        dialog.setTitle(text + getString(R.string.data));
                                        selectAll3G(false);
                                        break;
                                    case R.id.img_roam:
                                        dialog.setTitle(text + getString(R.string.roam));
                                        selectAllRoam(false);
                                        break;
                                    case R.id.img_vpn:
                                        dialog.setTitle(text + getString(R.string.vpn));
                                        selectAllVPN(false);
                                        break;
                                    case R.id.img_lan:
                                        dialog.setTitle(text + getString(R.string.lan));
                                        selectAllLAN(false);
                                        break;
                                    case R.id.img_tor:
                                        dialog.setTitle(text + getString(R.string.tor));
                                        selectAllTor(false);
                                        break;
                                }
                                break;
                        }
                    }
                }).show();
    }

    protected boolean isSuPackage(PackageManager pm, String suPackage) {
        boolean found = false;
        try {
            PackageInfo info = pm.getPackageInfo(suPackage, 0);
            if (info.applicationInfo != null) {
                found = true;
            }
        } catch (Exception e) {
        }
        return found;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialogLegend != null) {
            dialogLegend.dismiss();
            dialogLegend = null;
        }
        if (uiProgressReceiver != null) {
            unregisterReceiver(uiProgressReceiver);
        }
        if (toastReceiver != null) {
            unregisterReceiver(toastReceiver);
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Api.updateBaseContextLocale(base));
    }

    private static class PurgeTask extends AsyncTask<Void, Void, Boolean> {

        private MaterialDialog progress;
        private Context ctx;

        private PurgeTask(Context context) {
            this.ctx = context;
        }

        @Override
        protected void onPreExecute() {
            progress = new MaterialDialog.Builder(ctx)
                    .title(R.string.working)
                    .cancelable(false)
                    .content(R.string.purging_rules)
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (G.hasRoot() && Shell.SU.available()) {
                Api.purgeIptables(ctx, true, new RootCommand()
                        .setSuccessToast(R.string.rules_deleted)
                        .setFailureToast(R.string.error_purge)
                        .setReopenShell(true)
                        .setCallback(new RootCommand.Callback() {
                            public void cbFunc(RootCommand state) {
                                // error exit -> assume the rules are still enabled
                                // we shouldn't wind up in this situation, but if we do, the user's
                                // best bet is to click Apply then toggle Enabled again
                                try {
                                    progress.dismiss();
                                } catch (Exception ex) {
                                }
                                boolean nowEnabled = state.exitCode != 0;
                                Api.setEnabled(ctx, nowEnabled, true);
                            }
                        }));
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            if (!aVoid) {
                Toast.makeText(ctx, ctx.getString(R.string.error_su_toast), Toast.LENGTH_SHORT).show();
                try {
                    progress.dismiss();
                    progress = null;
                } catch (Exception ex) {
                }
            }
        }
    }

    public class GetAppList extends AsyncTask<Void, Integer, Void> {

        Context context = null;

        public GetAppList setContext(Context context) {
            this.context = context;
            return this;
        }

        @Override
        protected void onPreExecute() {
            plsWait = new MaterialDialog.Builder(context).cancelable(false).
                    title(getString(R.string.reading_apps)).progress(false, getPackageManager().getInstalledApplications(0)
                    .size(), true).show();
            doProgress(0);
        }

        public void doProgress(int value) {
            publishProgress(value);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Api.getApps(MainActivity.this, this);
            if (isCancelled())
                return null;
            //publishProgress(-1);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            selectFilterGroup();
            doProgress(-1);
            try {
                try {
                    if (plsWait != null && plsWait.isShowing()) {
                        plsWait.dismiss();
                    }
                } catch (final IllegalArgumentException e) {
                    // Handle or log or ignore
                } catch (final Exception e) {
                    // Handle or log or ignore
                } finally {
                    plsWait.dismiss();
                    plsWait = null;
                }
                mSwipeLayout.setRefreshing(false);
            } catch (Exception e) {
                // nothing
                if (plsWait != null) {
                    plsWait.dismiss();
                    plsWait = null;
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

            if (progress[0] == 0 || progress[0] == -1) {
                //do nothing
            } else {
                if (plsWait != null) {
                    plsWait.incrementProgress(progress[0]);
                }
            }
        }
    }

    private class RunApply extends AsyncTask<Void, Long, Boolean> {
        boolean enabled = Api.isEnabled(getApplicationContext());

        @Override
        protected void onPreExecute() {
            runProgress = new MaterialDialog.Builder(MainActivity.this)
                    .title(R.string.su_check_title)
                    .cancelable(false)
                    .content(enabled ? R.string.su_check_message
                            : R.string.saving_rules)
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //set the progress
            if (!Shell.SU.available()) return false;

            Api.setRulesUpToDate(false);
            Api.applySavedIptablesRules(getApplicationContext(), true, new RootCommand()
                    .setSuccessToast(R.string.rules_applied)
                    .setFailureToast(R.string.error_apply)
                    .setReopenShell(true)
                    .setCallback(new RootCommand.Callback() {
                        public void cbFunc(RootCommand state) {
                            try {
                                if (runProgress != null) {
                                    runProgress.dismiss();
                                }
                            } catch (Exception ex) {
                            }
                            if (state.exitCode == 0) {
                                setDirty(false);
                            }
                            //queue.clear();
                            runOnUiThread(() -> {
                                setDirty(false);
                                if (state.exitCode != 0) {
                                    Api.errorNotification(ctx);
                                    menuSetApplyOrSave(MainActivity.this.mainMenu, false);
                                    Api.setEnabled(ctx, false, true);
                                } else {
                                    menuSetApplyOrSave(MainActivity.this.mainMenu, enabled);
                                    Api.setEnabled(ctx, enabled, true);
                                }
                            });
                        }
                    }));
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            if (!aVoid) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_su_toast), Toast.LENGTH_SHORT).show();
                disableFirewall();
                try {
                    runProgress.dismiss();
                } catch (Exception ex) {
                }
            }
        }
    }

    private class RootCheck extends AsyncTask<Void, Void, Void> {
        MaterialDialog suDialog = null;
        boolean unsupportedSU = false;
        boolean[] suGranted = {false};
        private Context context = null;
        //private boolean suAvailable = false;

        public RootCheck setContext(Context context) {
            this.context = context;
            return this;
        }

        @Override
        protected void onPreExecute() {
            suDialog = new MaterialDialog.Builder(context).
                    cancelable(false).title(getString(R.string.su_check_title)).progress(true, 0).content(context.getString(R.string.su_check_message))
                    .show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            rootShell = (new Shell.Builder())
                    .useSU()
                    .addCommand("id", 0, (commandCode, exitCode, output) -> {
                        synchronized (suGranted) {
                            suGranted[0] = true;
                        }
                    }).open((commandCode, exitCode, output) -> {
                    });
            rootShell.waitForIdle();
            unsupportedSU = isSuPackage(getPackageManager(), "com.kingouser.com");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if (suDialog != null) {
                    suDialog.dismiss();
                }
            } catch (final Exception e) {
            } finally {
                suDialog = null;
            }
            if (!Api.isNetfilterSupported() && !isFinishing()) {
                new MaterialDialog.Builder(MainActivity.this).cancelable(false)
                        .title(R.string.error_common)
                        .content(R.string.error_netfilter)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                MainActivity.this.finish();
                                android.os.Process.killProcess(android.os.Process.myPid());
                                dialog.dismiss();
                            }
                        })
                        .positiveText(R.string.Continue)
                        .negativeText(R.string.exit)
                        .show();
            }
            /*// more details on https://github.com/ukanth/afwall/issues/501
            if (isSuPackage(getPackageManager(), "com.kingroot.kinguser")) {
                G.kingDetected(true);
            }*/
            if (!suGranted[0] && !unsupportedSU && !isFinishing()) {
                disableFirewall();
                showRootNotFoundMessage();
            } else {
                G.hasRoot(suGranted[0]);
                startRootShell(rootShell);
                new SecurityUtil(MainActivity.this).passCheck();
            }
        }
    }
}

