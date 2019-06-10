package com.battlelancer.seriesguide.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.TaskStackBuilder;
import androidx.fragment.app.FragmentManager;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.SgApp;
import com.battlelancer.seriesguide.appwidget.ListWidgetProvider;
import com.battlelancer.seriesguide.backend.CloudSetupActivity;
import com.battlelancer.seriesguide.backend.settings.HexagonSettings;
import com.battlelancer.seriesguide.billing.BillingActivity;
import com.battlelancer.seriesguide.billing.amazon.AmazonBillingActivity;
import com.battlelancer.seriesguide.dataliberation.DataLiberationActivity;
import com.battlelancer.seriesguide.provider.SeriesGuideContract;
import com.battlelancer.seriesguide.provider.SeriesGuideContract.Episodes;
import com.battlelancer.seriesguide.service.NotificationService;
import com.battlelancer.seriesguide.settings.DisplaySettings;
import com.battlelancer.seriesguide.settings.NotificationSettings;
import com.battlelancer.seriesguide.settings.UpdateSettings;
import com.battlelancer.seriesguide.streaming.StreamingSearch;
import com.battlelancer.seriesguide.streaming.StreamingSearchConfigureDialog;
import com.battlelancer.seriesguide.sync.SgSyncAdapter;
import com.battlelancer.seriesguide.traktapi.ConnectTraktActivity;
import com.battlelancer.seriesguide.traktapi.TraktCredentials;
import com.battlelancer.seriesguide.ui.dialogs.NotificationSelectionDialogFragment;
import com.battlelancer.seriesguide.ui.dialogs.NotificationThresholdDialogFragment;
import com.battlelancer.seriesguide.ui.dialogs.TimeOffsetDialogFragment;
import com.battlelancer.seriesguide.util.DBUtils;
import com.battlelancer.seriesguide.util.DialogTools;
import com.battlelancer.seriesguide.util.Shadows;
import com.battlelancer.seriesguide.util.ThemeUtils;
import com.battlelancer.seriesguide.util.Utils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Allows tweaking of various SeriesGuide settings. Does NOT inherit from {@link
 * com.battlelancer.seriesguide.ui.BaseActivity} to avoid handling actions which might be confusing
 * while adjusting settings.
 */
public class SeriesGuidePreferences extends AppCompatActivity {

    public static class UpdateSummariesEvent {
    }

    private static final String EXTRA_SETTINGS_SCREEN = "settingsScreen";

    // Preference keys
    private static final String KEY_CLEAR_CACHE = "clearCache";
    //    public static final String KEY_SECURE = "com.battlelancer.seriesguide.secure";
    private static final String KEY_ABOUT = "aboutPref";
//    public static final String KEY_TAPE_INTERVAL = "com.battlelancer.seriesguide.tapeinterval";

    // links
    private static final String LINK_BASE_KEY = "com.battlelancer.seriesguide.settings.";
    private static final String LINK_KEY_UPGRADE = LINK_BASE_KEY + "upgrade";
    private static final String LINK_KEY_CLOUD = LINK_BASE_KEY + "cloud";
    private static final String LINK_KEY_TRAKT = LINK_BASE_KEY + "trakt";
    private static final String LINK_KEY_AUTOBACKUP = LINK_BASE_KEY + "autobackup";
    private static final String LINK_KEY_DATALIBERATION = LINK_BASE_KEY + "dataliberation";

    public static @StyleRes int THEME = R.style.Theme_SeriesGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(SeriesGuidePreferences.THEME);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupActionBar();

        if (savedInstanceState == null) {
            Fragment f = new SettingsFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.containerSettings, f);
            ft.commit();

            // open a sub settings screen if requested
            String settingsScreen = getIntent().getStringExtra(EXTRA_SETTINGS_SCREEN);
            if (settingsScreen != null) {
                switchToSettings(settingsScreen);
            }
        }
    }

    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.sgToolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        // Because we use the platform fragment manager we need to pop fragments on our own
        if (!getFragmentManager().popBackStackImmediate()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void switchToSettings(String settingsId) {
        Bundle args = new Bundle();
        args.putString(EXTRA_SETTINGS_SCREEN, settingsId);
        Fragment f = new SettingsFragment();
        f.setArguments(args);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.containerSettings, f);
        ft.addToBackStack(null);
        ft.commit();
    }

    private static OnPreferenceChangeListener sNoOpChangeListener = (preference, newValue) -> {
        Utils.advertiseSubscription(preference.getContext());
        // prevent value from getting saved
        return false;
    };

    /**
     * Resets and runs the notification service to take care of potential time shifts when e.g.
     * changing the time offset.
     */
    private static void resetAndRunNotificationsService(Context context) {
        NotificationService.resetLastEpisodeAirtime(PreferenceManager
                .getDefaultSharedPreferences(context));
        NotificationService.trigger(context);
    }

    public static void setListPreferenceSummary(@Nullable ListPreference listPref) {
        if (listPref == null) {
            return;
        }
        // Set summary to be the user-description for the selected value
        CharSequence entry = listPref.getEntry();
        listPref.setSummary(entry == null ? "" : entry.toString().replaceAll("%", "%%"));
    }

    public static class SettingsFragment extends PreferenceFragment implements
            OnSharedPreferenceChangeListener {

        public static final String KEY_SCREEN_BASIC = "screen_basic";
        private static final String KEY_SCREEN_NOTIFICATIONS = "screen_notifications";

        private static final int REQUEST_CODE_RINGTONE = 0;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            String settings = getArguments() == null ? null
                    : getArguments().getString(EXTRA_SETTINGS_SCREEN);
            if (settings == null) {
                addPreferencesFromResource(R.xml.settings_root);
                setupRootSettings();
            } else if (settings.equals(KEY_SCREEN_BASIC)) {
                addPreferencesFromResource(R.xml.settings_basic);
                setupBasicSettings();
            } else if (settings.equals(KEY_SCREEN_NOTIFICATIONS)) {
                addPreferencesFromResource(R.xml.settings_notifications);
                setupNotificationSettings();
            }
        }

        private void setupRootSettings() {
            // Clear image cache
            findPreference(KEY_CLEAR_CACHE)
                    .setOnPreferenceClickListener(preference -> {
                        // try to open app info where user can clear app cache folders
                        Intent intent = new Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                        if (!Utils.tryStartActivity(getActivity(), intent, false)) {
                            // try to open all apps view if detail view not available
                            intent = new Intent(
                                    Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                            Utils.tryStartActivity(getActivity(), intent, true);
                        }

                        return true;
                    });

            // display version as About summary
            findPreference(KEY_ABOUT).setSummary(Utils.getVersionString(getActivity()));
        }

        private void updateRootSettings() {
            // unlock all link
            Preference unlock = findPreference(LINK_KEY_UPGRADE);
            boolean hasAccessToX = Utils.hasAccessToX(getActivity());
            unlock.setSummary(hasAccessToX ? getString(R.string.upgrade_success) : null);

            // notifications link
            Preference notifications = findPreference(KEY_SCREEN_NOTIFICATIONS);
            if (hasAccessToX && NotificationSettings.isNotificationsEnabled(getActivity())) {
                notifications.setSummary(
                        NotificationSettings.getLatestToIncludeTresholdValue(getActivity()));
            } else {
                notifications.setSummary(R.string.pref_notificationssummary);
            }

            // SeriesGuide Cloud link
            Preference cloud = findPreference(LINK_KEY_CLOUD);
            if (hasAccessToX && HexagonSettings.isEnabled(getActivity())) {
                cloud.setSummary(HexagonSettings.getAccountName(getActivity()));
            } else {
                cloud.setSummary(R.string.hexagon_description);
            }

            // trakt link
            Preference trakt = findPreference(LINK_KEY_TRAKT);
            if (TraktCredentials.get(getActivity()).hasCredentials()) {
                trakt.setSummary(TraktCredentials.get(getActivity()).getUsername());
            } else {
                trakt.setSummary(null);
            }

            // Theme switcher
            Preference themePref = findPreference(DisplaySettings.KEY_THEME);
            if (hasAccessToX) {
                themePref.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (DisplaySettings.KEY_THEME.equals(preference.getKey())) {
                        ThemeUtils.updateTheme((String) newValue);
                        Shadows.getInstance().resetShadowColor();

                        // restart to apply new theme, go back to this settings screen
                        TaskStackBuilder.create(getActivity())
                                .addNextIntent(new Intent(getActivity(), ShowsActivity.class))
                                .addNextIntent(getActivity().getIntent())
                                .startActivities();
                    }
                    return true;
                });
                setListPreferenceSummary((ListPreference) themePref);
            } else {
                themePref.setOnPreferenceChangeListener(sNoOpChangeListener);
                themePref.setSummary(R.string.onlyx);
            }

            // show currently set values for list prefs
            setListPreferenceSummary(
                    (ListPreference) findPreference(DisplaySettings.KEY_NUMBERFORMAT));

            // set current value of auto-update pref
            ((SwitchPreference) findPreference(UpdateSettings.KEY_AUTOUPDATE)).setChecked(
                    SgSyncAdapter.isSyncAutomatically(getActivity()));
        }

        private void setupNotificationSettings() {
            Preference enabledPref = findPreference(NotificationSettings.KEY_ENABLED);
            final Preference thresholdPref = findPreference(NotificationSettings.KEY_THRESHOLD);
            final Preference selectionPref = findPreference(NotificationSettings.KEY_SELECTION);
            final Preference hiddenPref = findPreference(NotificationSettings.KEY_IGNORE_HIDDEN);
            // only visible pre-O
            final Preference vibratePref = findPreference(NotificationSettings.KEY_VIBRATE);
            final Preference ringtonePref = findPreference(NotificationSettings.KEY_RINGTONE);
            // only visible O+
            final Preference channelsPref = findPreference(NotificationSettings.KEY_CHANNELS);

            // allow supporters to enable notifications
            if (Utils.hasAccessToX(getActivity())) {
                enabledPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isChecked = (boolean) newValue;

                    thresholdPref.setEnabled(isChecked);
                    selectionPref.setEnabled(isChecked);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        channelsPref.setEnabled(isChecked);
                    } else {
                        vibratePref.setEnabled(isChecked);
                        ringtonePref.setEnabled(isChecked);
                    }

                    NotificationService.trigger(getActivity());
                    return true;
                });
                // disable advanced notification settings if notifications are disabled
                boolean isNotificationsEnabled = NotificationSettings.isNotificationsEnabled(
                        getActivity());
                thresholdPref.setEnabled(isNotificationsEnabled);
                selectionPref.setEnabled(isNotificationsEnabled);
                hiddenPref.setEnabled(isNotificationsEnabled);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    channelsPref.setEnabled(isNotificationsEnabled);
                } else {
                    vibratePref.setEnabled(isNotificationsEnabled);
                    ringtonePref.setEnabled(isNotificationsEnabled);
                }
            } else {
                enabledPref.setOnPreferenceChangeListener(sNoOpChangeListener);
                ((SwitchPreference) enabledPref).setChecked(false);
                enabledPref.setSummary(R.string.onlyx);
                thresholdPref.setEnabled(false);
                selectionPref.setEnabled(false);
                hiddenPref.setEnabled(false);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    channelsPref.setEnabled(false);
                } else {
                    vibratePref.setEnabled(false);
                    ringtonePref.setEnabled(false);
                }
            }

            updateThresholdSummary(thresholdPref);
            updateNotificationSettings();
        }

        private void updateNotificationSettings() {
            updateSelectionSummary(findPreference(NotificationSettings.KEY_SELECTION));
        }

        private void setupBasicSettings() {
            // show currently set values for some prefs
            updateStreamSearchServiceSummary(findPreference(StreamingSearch.KEY_SETTING_SERVICE));
            setListPreferenceSummary(
                    (ListPreference) findPreference(DisplaySettings.KEY_LANGUAGE_FALLBACK));
            updateTimeOffsetSummary(findPreference(DisplaySettings.KEY_SHOWS_TIME_OFFSET));
        }

        @Override
        public void onStart() {
            super.onStart();

            // update summary values not handled by onSharedPreferenceChanged
            String settings = getArguments() == null ? null
                    : getArguments().getString(EXTRA_SETTINGS_SCREEN);
            if (settings == null) {
                updateRootSettings();
            }

            final SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            prefs.registerOnSharedPreferenceChangeListener(this);
            EventBus.getDefault().register(this);
        }

        @Override
        public void onStop() {
            super.onStop();

            final SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            prefs.unregisterOnSharedPreferenceChangeListener(this);
            EventBus.getDefault().unregister(this);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                @NonNull Preference preference) {
            String key = preference.getKey();
            if (key == null) {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }

            // other screens
            if (key.startsWith("screen_")) {
                ((SeriesGuidePreferences) getActivity()).switchToSettings(key);
                return true;
            }

            // links
            switch (key) {
                case LINK_KEY_UPGRADE:
                    startActivity(new Intent(getActivity(), Utils.isAmazonVersion()
                            ? AmazonBillingActivity.class
                            : BillingActivity.class));
                    return true;
                case LINK_KEY_CLOUD:
                    startActivity(new Intent(getActivity(), CloudSetupActivity.class));
                    return true;
                case LINK_KEY_TRAKT:
                    startActivity(new Intent(getActivity(), ConnectTraktActivity.class));
                    return true;
                case LINK_KEY_AUTOBACKUP:
                    startActivity(new Intent(getActivity(), DataLiberationActivity.class).putExtra(
                            DataLiberationActivity.InitBundle.EXTRA_SHOW_AUTOBACKUP, true));
                    return true;
                case LINK_KEY_DATALIBERATION:
                    startActivity(new Intent(getActivity(), DataLiberationActivity.class));
                    return true;
                default:
                    // fall through
            }

            // settings
            FragmentManager supportFragmentManager = ((AppCompatActivity) getActivity())
                    .getSupportFragmentManager();
            if (NotificationSettings.KEY_THRESHOLD.equals(key)) {
                DialogTools.safeShow(new NotificationThresholdDialogFragment(),
                        supportFragmentManager, "notification-threshold");
                return true;
            }
            if (NotificationSettings.KEY_SELECTION.equals(key)) {
                DialogTools.safeShow(new NotificationSelectionDialogFragment(),
                                supportFragmentManager, "notification-selection");
                return true;
            }
            if (StreamingSearch.KEY_SETTING_SERVICE.equals(key)) {
                DialogTools.safeShow(new StreamingSearchConfigureDialog(),
                        supportFragmentManager, "streaming-service");
                return true;
            }
            if (DisplaySettings.KEY_SHOWS_TIME_OFFSET.equals(key)) {
                DialogTools.safeShow(new TimeOffsetDialogFragment(),
                        supportFragmentManager, "time-offset");
                return true;
            }
            if (NotificationSettings.KEY_RINGTONE.equals(key)) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                        RingtoneManager.TYPE_NOTIFICATION);
                // show silent and default options
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                        Settings.System.DEFAULT_NOTIFICATION_URI);

                // restore selected sound or silent (empty string)
                String existingValue = NotificationSettings.getNotificationsRingtone(getActivity());
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        TextUtils.isEmpty(existingValue) ? null : Uri.parse(existingValue));

                startActivityForResult(intent, REQUEST_CODE_RINGTONE);
                return true;
            }
            if (NotificationSettings.KEY_CHANNELS.equals(key)) {
                // launch system settings app at settings for episodes channel
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, SgApp.NOTIFICATION_CHANNEL_EPISODES);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());
                    // at least NVIDIA Shield (8.0.0) can not handle this, so guard
                    Utils.tryStartActivity(getActivity(), intent, true);
                }
                return true;
            }
            if (KEY_ABOUT.equals(key)) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.containerSettings, new AboutSettingsFragment());
                ft.addToBackStack(null);
                ft.commit();
                return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (REQUEST_CODE_RINGTONE == requestCode) {
                if (data != null) {
                    Uri ringtoneUri = data.getParcelableExtra(
                            RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        // Xiaomi devices incorrectly return file:// uris on N
                        // protect against FileUriExposedException
                        if (ringtoneUri != null /* not silent */
                                && !"content".equals(ringtoneUri.getScheme())) {
                            ringtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                        }
                    }
                    // map silent (null) to empty string
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                            .putString(NotificationSettings.KEY_RINGTONE,
                                    ringtoneUri == null ? "" : ringtoneUri.toString())
                            .apply();
                }
                return;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            if (pref != null) {
                new BackupManager(pref.getContext()).dataChanged();

                // update pref summary text
                if (DisplaySettings.KEY_LANGUAGE_FALLBACK.equals(key)
                        || DisplaySettings.KEY_NUMBERFORMAT.equals(key)
                        || DisplaySettings.KEY_THEME.equals(key)) {
                    setListPreferenceSummary((ListPreference) pref);
                }
                if (DisplaySettings.KEY_SHOWS_TIME_OFFSET.equals(key)) {
                    updateTimeOffsetSummary(pref);
                }
                if (NotificationSettings.KEY_THRESHOLD.equals(key)) {
                    updateThresholdSummary(pref);
                }
                if (NotificationSettings.KEY_VIBRATE.equals(key)
                        && NotificationSettings.isNotificationVibrating(pref.getContext())) {
                    // demonstrate vibration pattern used by SeriesGuide
                    Vibrator vibrator = (Vibrator) getActivity().getSystemService(
                            Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(NotificationService.VIBRATION_PATTERN, -1);
                    }
                }
                if (StreamingSearch.KEY_SETTING_SERVICE.equals(key)) {
                    updateStreamSearchServiceSummary(pref);
                }
            }

            // pref changes that require the notification service to be reset
            if (DisplaySettings.KEY_SHOWS_TIME_OFFSET.equals(key)
                    || NotificationSettings.KEY_THRESHOLD.equals(key)) {
                resetAndRunNotificationsService(getActivity());
            }

            // pref changes that require the widgets to be updated
            if (DisplaySettings.KEY_SHOWS_TIME_OFFSET.equals(key)
                    || DisplaySettings.KEY_HIDE_SPECIALS.equals(key)
                    || DisplaySettings.KEY_DISPLAY_EXACT_DATE.equals(key)
                    || DisplaySettings.KEY_PREVENT_SPOILERS.equals(key)) {
                // update any widgets
                ListWidgetProvider.notifyDataChanged(getActivity());
            }

            if (DisplaySettings.KEY_LANGUAGE_FALLBACK.equals(key)) {
                // reset last edit date of all episodes so they will get updated
                new Thread(() -> {
                    android.os.Process
                            .setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    ContentValues values = new ContentValues();
                    values.put(Episodes.LAST_UPDATED, 0);
                    getActivity().getContentResolver()
                            .update(Episodes.CONTENT_URI, values, null, null);
                }).start();
            }

            // Toggle auto-update on SyncAdapter
            if (UpdateSettings.KEY_AUTOUPDATE.equals(key)) {
                if (pref != null) {
                    //noinspection ConstantConditions
                    SwitchPreference autoUpdatePref = (SwitchPreference) pref;
                    SgSyncAdapter.setSyncAutomatically(getActivity(), autoUpdatePref.isChecked());
                }
            }
        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEvent(UpdateSummariesEvent event) {
            if (!isResumed()) {
                return;
            }
            // update summary values not handled by onSharedPreferenceChanged
            String settings = getArguments() == null ? null
                    : getArguments().getString(EXTRA_SETTINGS_SCREEN);
            if (settings != null && settings.equals(KEY_SCREEN_NOTIFICATIONS)) {
                updateNotificationSettings();
            }
        }

        private void updateThresholdSummary(Preference thresholdPref) {
            thresholdPref.setSummary(NotificationSettings.getLatestToIncludeTresholdValue(
                    thresholdPref.getContext()));
        }

        private void updateSelectionSummary(Preference selectionPref) {
            int countOfShowsNotifyOn = DBUtils.getCountOf(getActivity().getContentResolver(),
                    SeriesGuideContract.Shows.CONTENT_URI,
                    SeriesGuideContract.Shows.SELECTION_NOTIFY, null, 0);
            selectionPref.setSummary(getString(R.string.pref_notifications_select_shows_summary,
                    countOfShowsNotifyOn));
        }

        private void updateStreamSearchServiceSummary(Preference pref) {
            String serviceOrEmptyOrNull = StreamingSearch.getServiceOrEmptyOrNull(getActivity());
            if (serviceOrEmptyOrNull == null) {
                pref.setSummary(null);
            } else if (serviceOrEmptyOrNull.length() == 0) {
                pref.setSummary(R.string.action_turn_off);
            } else {
                pref.setSummary(StreamingSearch.getServiceDisplayName(serviceOrEmptyOrNull));
            }
        }

        private void updateTimeOffsetSummary(Preference offsetListPref) {
            offsetListPref.setSummary(getString(R.string.pref_offsetsummary,
                    DisplaySettings.getShowsTimeOffset(getActivity()))
            );
        }
    }
}
