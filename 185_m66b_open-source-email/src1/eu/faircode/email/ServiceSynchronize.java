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

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import com.bugsnag.android.BreadcrumbType;
import com.bugsnag.android.Bugsnag;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.ReadOnlyFolderException;
import javax.mail.Session;
import javax.mail.StoreClosedException;
import javax.mail.UIDFolder;
import javax.mail.event.ConnectionAdapter;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.FolderAdapter;
import javax.mail.event.FolderEvent;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class ServiceSynchronize extends LifecycleService {
    private ConnectionHelper.NetworkState networkState = new ConnectionHelper.NetworkState();
    private Core.State state;
    private boolean started = false;
    private int queued = 0;
    private long lastLost = 0;
    private TupleAccountStats lastStats = new TupleAccountStats();
    private ExecutorService queue = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    private static boolean booted = false;
    private static boolean oneshot = false;

    private static final int CONNECT_BACKOFF_START = 8; // seconds
    private static final int CONNECT_BACKOFF_MAX = 64; // seconds (totally 2 minutes)
    private static final int CONNECT_BACKOFF_AlARM = 15; // minutes
    private static final long RECONNECT_BACKOFF = 90 * 1000L; // milliseconds
    private static final int ACCOUNT_ERROR_AFTER = 60; // minutes
    private static final int BACKOFF_ERROR_AFTER = 16; // seconds
    private static final long ONESHOT_DURATION = 90 * 1000L; // milliseconds
    private static final long STOP_DELAY = 5000L; // milliseconds

    static final int PI_ALARM = 1;
    static final int PI_ONESHOT = 2;

    @Override
    public void onCreate() {
        Log.i("Service create version=" + BuildConfig.VERSION_NAME);
        super.onCreate();

        // Listen for network changes
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        // Removed because of Android VPN service
        // builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        DB db = DB.getInstance(this);

        db.account().liveStats().observe(this, new Observer<TupleAccountStats>() {
            private TupleAccountStats lastStats = null;

            @Override
            public void onChanged(@Nullable TupleAccountStats stats) {
                if (stats != null && !stats.equals(lastStats)) {
                    try {
                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.notify(Helper.NOTIFICATION_SYNCHRONIZE, getNotificationService(stats).build());
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                    if (oneshot && stats.operations > 0)
                        onOneshot(true);
                }

                lastStats = stats;
            }
        });

        final TwoStateOwner cowner = new TwoStateOwner(this, "liveUnseenNotify");

        db.folder().liveSynchronizing().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                Log.i("Synchronizing folders=" + count);
                if (count == null || count == 0)
                    cowner.start();
                else
                    cowner.stop();
            }
        });

        db.message().liveUnseenNotify().observe(cowner, new Observer<List<TupleMessageEx>>() {
            private Map<String, List<Long>> notifying = new HashMap<>();

            @Override
            public void onChanged(List<TupleMessageEx> messages) {
                try {
                    Core.notifyMessages(ServiceSynchronize.this, notifying, messages);
                } catch (SecurityException ex) {
                    android.util.Log.e(Log.TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });

        WorkerCleanup.queue(this);
    }

    @Override
    public void onDestroy() {
        Log.i("Service destroy");
        EntityLog.log(this, "Service destroy");

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);

        Core.notifyReset(this);

        WorkerCleanup.cancel(this);

        stopForeground(true);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(Helper.NOTIFICATION_SYNCHRONIZE);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = (intent == null ? null : intent.getAction());
        Log.i("Service command intent=" + intent + " action=" + action);
        Log.logExtras(intent);

        startForeground(Helper.NOTIFICATION_SYNCHRONIZE, getNotificationService(null).build());

        super.onStartCommand(intent, flags, startId);

        if (action != null)
            try {
                switch (action) {
                    case "init":
                        onInit();
                        break;

                    case "alarm":
                        onAlarm();
                        break;

                    case "reload":
                        onReload(
                                intent.getBooleanExtra("clear", false),
                                intent.getStringExtra("reason"));
                        break;

                    case "reset":
                        onReset();
                        break;

                    case "oneshot_start":
                        onOneshot(true);
                        break;

                    case "oneshot_end":
                        onOneshot(false);
                        break;

                    case "watchdog":
                        onWatchdog();
                        break;

                    default:
                        Log.w("Unknown action: " + action);
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }

        return START_STICKY;
    }

    private NotificationCompat.Builder getNotificationService(TupleAccountStats stats) {
        if (stats != null)
            lastStats = stats;

        // Build pending intent
        Intent why = new Intent(this, ActivityView.class);
        why.setAction("why");
        PendingIntent piWhy = PendingIntent.getActivity(this, ActivityView.REQUEST_WHY, why, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "service");

        builder
                .setSmallIcon(R.drawable.baseline_compare_arrows_white_24)
                .setContentTitle(getResources().getQuantityString(
                        R.plurals.title_notification_synchronizing, lastStats.accounts, lastStats.accounts))
                .setContentIntent(piWhy)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET);

        if (lastStats.operations > 0)
            builder.setContentText(getResources().getQuantityString(
                    R.plurals.title_notification_operations, lastStats.operations, lastStats.operations));

        return builder;
    }

    private void onInit() {
        EntityLog.log(this, "Service init");
        // Network events will manage the service
    }

    private void onAlarm() {
        schedule(this);
        onReload(true, "alarm");
    }

    private void onReload(boolean clear, String reason) {
        synchronized (this) {
            try {
                queue_reload(true, clear, reason);
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }
    }

    private void onReset() {
        lastLost = 0;
        onReload(true, "reset");
    }

    private void onOneshot(boolean start) {
        Log.i("Oneshot start=" + start);

        Intent alarm = new Intent(this, ServiceSynchronize.class);
        alarm.setAction("oneshot_end");
        PendingIntent piOneshot;

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            piOneshot = PendingIntent.getService(this, PI_ONESHOT, alarm, PendingIntent.FLAG_UPDATE_CURRENT);
        else
            piOneshot = PendingIntent.getForegroundService(this, PI_ONESHOT, alarm, PendingIntent.FLAG_UPDATE_CURRENT);

        am.cancel(piOneshot);

        oneshot = start;

        if (start) {
            // Network events will manage the service
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ONESHOT_DURATION, piOneshot);
            else
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ONESHOT_DURATION, piOneshot);
        } else
            onReload(true, "oneshot end");
    }

    private void onWatchdog() {
        EntityLog.log(this, "Service watchdog");
        // Network events will manage the service
    }

    private void queue_reload(final boolean start, final boolean clear, final String reason) {
        final boolean doStop = started;
        final boolean doStart = (start && isEnabled() && networkState.isSuitable());

        EntityLog.log(this, "Queue reload" +
                " doStop=" + doStop + " doStart=" + doStart + " queued=" + queued + " reason=" + reason);

        started = doStart;

        try {
            queued++;
            queue.submit(new Runnable() {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":manage");

                @Override
                public void run() {
                    DB db = DB.getInstance(ServiceSynchronize.this);

                    try {
                        wl.acquire();

                        EntityLog.log(ServiceSynchronize.this, "Reload" +
                                " stop=" + doStop + " start=" + doStart + " queued=" + queued + " " + reason);

                        Map<String, String> crumb = new HashMap<>();
                        crumb.put("oneshot", Boolean.toString(oneshot));
                        crumb.put("started", Boolean.toString(started));
                        crumb.put("doStop", Boolean.toString(doStop));
                        crumb.put("doStart", Boolean.toString(doStart));
                        crumb.put("queued", Integer.toString(queued));
                        crumb.put("reason", reason == null ? "" : reason);
                        crumb.put("connected", Boolean.toString(networkState.isConnected()));
                        crumb.put("suitable", Boolean.toString(networkState.isSuitable()));
                        crumb.put("unmetered", Boolean.toString(networkState.isUnmetered()));
                        crumb.put("roaming", Boolean.toString(networkState.isRoaming()));
                        crumb.put("lastLost", new Date(lastLost).toString());
                        Bugsnag.leaveBreadcrumb("reload", BreadcrumbType.LOG, crumb);

                        if (doStop)
                            stop();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            for (EntityAccount account : db.account().getAccountsTbd())
                                nm.deleteNotificationChannel(EntityAccount.getNotificationChannelId(account.id));
                        }

                        int accounts = db.account().deleteAccountsTbd();
                        int identities = db.identity().deleteIdentitiesTbd();
                        if (accounts > 0 || identities > 0)
                            Log.i("Deleted accounts=" + accounts + " identities=" + identities);

                        if (doStart) {
                            if (clear)
                                db.account().clearAccountConnected();
                            start();
                        }

                    } catch (Throwable ex) {
                        Log.e(ex);
                    } finally {
                        queued--;
                        EntityLog.log(ServiceSynchronize.this, "Reload done queued=" + queued);

                        if (!doStart && queued == 0 && !isEnabled()) {
                            try {
                                Thread.sleep(STOP_DELAY);
                            } catch (InterruptedException ignored) {
                            }
                            if (!doStart && queued == 0 && !isEnabled()) {
                                queue.shutdownNow();
                                stopService();
                            }
                        }

                        wl.release();
                    }
                }
            });
        } catch (RejectedExecutionException ex) {
            Log.w(ex);
        }
    }

    private boolean isEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = prefs.getBoolean("enabled", true);
        int pollInterval = prefs.getInt("poll_interval", 0);
        return ((enabled && pollInterval == 0) || oneshot);
    }

    private void start() {
        EntityLog.log(this, "Main start");

        state = new Core.State(networkState);
        state.runnable(new Runnable() {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":main");
            private List<Core.State> threadState = new ArrayList<>();

            @Override
            public void run() {
                try {
                    wl.acquire();

                    final DB db = DB.getInstance(ServiceSynchronize.this);

                    long ago = new Date().getTime() - lastLost;
                    if (ago < RECONNECT_BACKOFF)
                        try {
                            long backoff = RECONNECT_BACKOFF - ago;
                            EntityLog.log(ServiceSynchronize.this, "Main backoff=" + (backoff / 1000));
                            if (state.acquire(backoff))
                                return;
                        } catch (InterruptedException ex) {
                            Log.w("main backoff " + ex.toString());
                        }

                    // Start monitoring accounts
                    List<EntityAccount> accounts = db.account().getSynchronizingAccounts();
                    for (final EntityAccount account : accounts) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (account.notify)
                                account.createNotificationChannel(ServiceSynchronize.this);
                            else
                                account.deleteNotificationChannel(ServiceSynchronize.this);
                        }

                        Log.i(account.host + "/" + account.user + " run");
                        final Core.State astate = new Core.State(state);
                        astate.runnable(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    monitorAccount(account, astate);
                                } catch (Throwable ex) {
                                    Log.e(account.name, ex);
                                }
                            }
                        }, "sync.account." + account.id);
                        astate.start();
                        threadState.add(astate);
                    }

                    EntityLog.log(ServiceSynchronize.this, "Main started");

                    try {
                        wl.release();
                        state.acquire();
                    } catch (InterruptedException ex) {
                        Log.w("main wait " + ex.toString());
                    } finally {
                        wl.acquire();
                    }

                    // Stop monitoring accounts
                    for (Core.State astate : threadState)
                        astate.stop();
                    for (Core.State astate : threadState)
                        astate.join();
                    threadState.clear();

                    EntityLog.log(ServiceSynchronize.this, "Main exited");
                } catch (Throwable ex) {
                    // Fail-safe
                    Log.e(ex);
                } finally {
                    wl.release();
                    EntityLog.log(ServiceSynchronize.this, "Start wake lock=" + wl.isHeld());
                }
            }
        }, "sync.main");
        state.start();
    }

    private void stop() {
        EntityLog.log(this, "Main stop");

        state.stop();
        state.join();

        EntityLog.log(this, "Main stopped");

        state = null;
    }

    private void stopService() {
        EntityLog.log(this, "Service stop");

        DB db = DB.getInstance(this);
        List<EntityOperation> ops = db.operation().getOperations(EntityOperation.SYNC);
        for (EntityOperation op : ops)
            db.folder().setFolderSyncState(op.folder, null);

        stopSelf();
    }

    private void monitorAccount(final EntityAccount account, final Core.State state) throws NoSuchProviderException {
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wlAccount = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id);
        final PowerManager.WakeLock wlFolder = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id + ".folder");
        final PowerManager.WakeLock wlMessage = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":account." + account.id + ".message");
        try {
            wlAccount.acquire();

            final DB db = DB.getInstance(this);

            int backoff = CONNECT_BACKOFF_START;
            while (state.running()) {
                state.reset();
                Log.i(account.name + " run");

                Handler handler = new Handler(getMainLooper());
                final List<TwoStateOwner> cowners = new ArrayList<>();

                // Debug
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.DEBUG);
                //System.setProperty("mail.socket.debug", Boolean.toString(debug));

                // Get properties
                Properties props = MessageHelper.getSessionProperties(account.auth_type, account.realm, account.insecure);

                // Create session
                final Session isession = Session.getInstance(props, null);
                isession.setDebug(debug);
                // adb -t 1 logcat | grep "fairemail\|System.out"

                final IMAPStore istore = (IMAPStore) isession.getStore(account.getProtocol());

                final Map<EntityFolder, IMAPFolder> mapFolders = new HashMap<>();
                List<Thread> idlers = new ArrayList<>();
                try {
                    // Listen for store events
                    istore.addStoreListener(new StoreListener() {
                        @Override
                        public void notification(StoreEvent e) {
                            if (e.getMessageType() == StoreEvent.NOTICE)
                                Log.i(account.name + " notice: " + e.getMessage());
                            else
                                try {
                                    wlFolder.acquire();
                                    String message = e.getMessage();
                                    Log.w(account.name + " alert: " + message);
                                    db.account().setAccountError(account.id, message);
                                    if (BuildConfig.DEBUG ||
                                            (message != null && !message.startsWith("Too many simultaneous connections")))
                                        Core.reportError(ServiceSynchronize.this, account, null,
                                                new Core.AlertException(message));
                                    state.error(null);
                                } finally {
                                    wlFolder.release();
                                }
                        }
                    });

                    // Listen for folder events
                    istore.addFolderListener(new FolderAdapter() {
                        @Override
                        public void folderCreated(FolderEvent e) {
                            try {
                                wlFolder.acquire();

                                String name = e.getFolder().getFullName();
                                Log.i("Folder created=" + name);
                                if (db.folder().getFolderByName(account.id, name) == null)
                                    reload(ServiceSynchronize.this, "folder created");
                            } finally {
                                wlFolder.release();
                            }
                        }

                        @Override
                        public void folderRenamed(FolderEvent e) {
                            try {
                                wlFolder.acquire();

                                String old = e.getFolder().getFullName();
                                String name = e.getNewFolder().getFullName();
                                Log.i("Folder renamed from=" + old + " to=" + name);

                                int count = db.folder().renameFolder(account.id, old, name);
                                Log.i("Renamed to " + name + " count=" + count);
                                if (count == 0)
                                    reload(ServiceSynchronize.this, "folder renamed");
                            } finally {
                                wlFolder.release();
                            }
                        }

                        @Override
                        public void folderDeleted(FolderEvent e) {
                            try {
                                wlFolder.acquire();

                                String name = e.getFolder().getFullName();
                                Log.i("Folder deleted=" + name);
                                if (db.folder().getFolderByName(account.id, name) != null)
                                    reload(ServiceSynchronize.this, "folder deleted");
                            } finally {
                                wlFolder.release();
                            }
                        }
                    });

                    // Listen for connection events
                    istore.addConnectionListener(new ConnectionAdapter() {
                        @Override
                        public void opened(ConnectionEvent e) {
                            Log.i(account.name + " opened event");
                        }

                        @Override
                        public void disconnected(ConnectionEvent e) {
                            Log.e(account.name + " disconnected event");
                        }

                        @Override
                        public void closed(ConnectionEvent e) {
                            Log.e(account.name + " closed event");
                        }
                    });

                    List<EntityFolder> folders = db.folder().getFolders(account.id);
                    Collections.sort(folders, new Comparator<EntityFolder>() {
                        @Override
                        public int compare(EntityFolder f1, EntityFolder f2) {
                            int s1 = EntityFolder.FOLDER_SORT_ORDER.indexOf(f1.type);
                            int s2 = EntityFolder.FOLDER_SORT_ORDER.indexOf(f2.type);
                            int s = Integer.compare(s1, s2);
                            if (s != 0)
                                return s;

                            return f1.name.compareTo(f2.name);
                        }
                    });

                    // Initiate connection
                    EntityLog.log(this, account.name + " connecting");
                    for (EntityFolder folder : folders)
                        db.folder().setFolderState(folder.id, null);
                    db.account().setAccountState(account.id, "connecting");

                    try {
                        ConnectionHelper.connect(this, istore, account);
                    } catch (Throwable ex) {
                        // Report account connection error
                        if (account.last_connected != null && !ConnectionHelper.airplaneMode(this)) {
                            EntityLog.log(this, account.name + " last connected: " + new Date(account.last_connected));
                            long now = new Date().getTime();
                            long delayed = now - account.last_connected - account.poll_interval * 60 * 1000L;
                            if (delayed > ACCOUNT_ERROR_AFTER * 60 * 1000L && backoff > BACKOFF_ERROR_AFTER) {
                                Log.i("Reporting sync error after=" + delayed);
                                Throwable warning = new Throwable(
                                        getString(R.string.title_no_sync,
                                                SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                                        .format(account.last_connected)), ex);
                                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                nm.notify("receive", account.id.intValue(),
                                        Core.getNotificationError(this, "warning", account.name, warning, false)
                                                .build());
                            }
                        }

                        throw ex;
                    }

                    final boolean capIdle = istore.hasCapability("IDLE");
                    Log.i(account.name + " idle=" + capIdle);

                    db.account().setAccountState(account.id, "connected");
                    db.account().setAccountError(account.id, null);
                    db.account().setAccountWarning(account.id, null);
                    EntityLog.log(this, account.name + " connected");

                    // Update folder list
                    Core.onSynchronizeFolders(this, account, istore, state);

                    // Open synchronizing folders
                    final ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);
                    for (final EntityFolder folder : folders) {
                        if (folder.synchronize && !folder.poll && capIdle) {
                            Log.i(account.name + " sync folder " + folder.name);

                            db.folder().setFolderState(folder.id, "connecting");

                            final IMAPFolder ifolder = (IMAPFolder) istore.getFolder(folder.name);
                            try {
                                //if ("Postausgang".equals(folder.name))
                                //    throw new ReadOnlyFolderException(ifolder);
                                ifolder.open(Folder.READ_WRITE);
                                db.folder().setFolderReadOnly(folder.id, false);
                            } catch (ReadOnlyFolderException ex) {
                                Log.w(folder.name + " read only");
                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                                try {
                                    ifolder.open(Folder.READ_ONLY);
                                    db.folder().setFolderReadOnly(folder.id, true);
                                } catch (MessagingException ex1) {
                                    Log.w(ex1);
                                    db.folder().setFolderState(folder.id, null);
                                    db.folder().setFolderError(folder.id, Helper.formatThrowable(ex1, true));
                                    continue;
                                }
                            } catch (FolderNotFoundException ex) {
                                Log.w(ex);
                                db.folder().deleteFolder(folder.id);
                                continue;
                            } catch (MessagingException ex) {
                                Log.w(ex);
                                db.folder().setFolderState(folder.id, null);
                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                                continue;
                            } catch (Throwable ex) {
                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                                throw ex;
                            }
                            mapFolders.put(folder, ifolder);

                            db.folder().setFolderState(folder.id, "connected");
                            db.folder().setFolderError(folder.id, null);

                            int count = ifolder.getMessageCount();
                            db.folder().setFolderTotal(folder.id, count < 0 ? null : count);

                            Log.i(account.name + " folder " + folder.name + " flags=" + ifolder.getPermanentFlags());

                            // Listen for new and deleted messages
                            ifolder.addMessageCountListener(new MessageCountAdapter() {
                                @Override
                                public void messagesAdded(MessageCountEvent e) {
                                    try {
                                        wlMessage.acquire();
                                        Log.i(folder.name + " messages added");

                                        FetchProfile fp = new FetchProfile();
                                        fp.add(FetchProfile.Item.ENVELOPE);
                                        fp.add(FetchProfile.Item.FLAGS);
                                        fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
                                        fp.add(UIDFolder.FetchProfileItem.UID);
                                        fp.add(IMAPFolder.FetchProfileItem.HEADERS);
                                        //fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
                                        fp.add(FetchProfile.Item.SIZE);
                                        fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
                                        ifolder.fetch(e.getMessages(), fp);

                                        for (Message imessage : e.getMessages())
                                            try {
                                                EntityMessage message = Core.synchronizeMessage(
                                                        ServiceSynchronize.this,
                                                        account, folder,
                                                        ifolder, (IMAPMessage) imessage,
                                                        false,
                                                        db.rule().getEnabledRules(folder.id));

                                                if (db.folder().getFolderDownload(folder.id))
                                                    Core.downloadMessage(ServiceSynchronize.this,
                                                            folder, ifolder,
                                                            (IMAPMessage) imessage, message.id, state);
                                            } catch (MessageRemovedException ex) {
                                                Log.w(folder.name, ex);
                                            } catch (FolderClosedException ex) {
                                                throw ex;
                                            } catch (IOException ex) {
                                                if (ex.getCause() instanceof MessagingException) {
                                                    Log.w(folder.name, ex);
                                                    db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                                                } else
                                                    throw ex;
                                            } catch (Throwable ex) {
                                                Log.e(folder.name, ex);
                                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                                            }

                                        int count = ifolder.getMessageCount();
                                        db.folder().setFolderTotal(folder.id, count < 0 ? null : count);
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        Core.reportError(ServiceSynchronize.this, account, folder, ex);
                                        state.error(ex);
                                    } finally {
                                        wlMessage.release();
                                    }
                                }

                                @Override
                                public void messagesRemoved(MessageCountEvent e) {
                                    try {
                                        wlMessage.acquire();
                                        Log.i(folder.name + " messages removed");
                                        for (Message imessage : e.getMessages())
                                            try {
                                                long uid = ifolder.getUID(imessage);

                                                DB db = DB.getInstance(ServiceSynchronize.this);
                                                int count = db.message().setMessageUiHide(folder.id, uid, true);
                                                // Will be deleted on next sync

                                                Log.i(folder.name + " deleted uid=" + uid + " count=" + count);
                                            } catch (MessageRemovedException ex) {
                                                Log.w(folder.name, ex);
                                            }

                                        int count = ifolder.getMessageCount();
                                        db.folder().setFolderTotal(folder.id, count < 0 ? null : count);
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        Core.reportError(ServiceSynchronize.this, account, folder, ex);
                                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                                        state.error(ex);
                                    } finally {
                                        wlMessage.release();
                                    }
                                }
                            });

                            // Flags (like "seen") at the remote could be changed while synchronizing

                            // Listen for changed messages
                            ifolder.addMessageChangedListener(new MessageChangedListener() {
                                @Override
                                public void messageChanged(MessageChangedEvent e) {
                                    try {
                                        wlMessage.acquire();
                                        try {
                                            Log.i(folder.name + " message changed");

                                            FetchProfile fp = new FetchProfile();
                                            fp.add(UIDFolder.FetchProfileItem.UID);
                                            fp.add(IMAPFolder.FetchProfileItem.FLAGS);
                                            ifolder.fetch(new Message[]{e.getMessage()}, fp);

                                            EntityMessage message = Core.synchronizeMessage(
                                                    ServiceSynchronize.this,
                                                    account, folder,
                                                    ifolder, (IMAPMessage) e.getMessage(),
                                                    false,
                                                    db.rule().getEnabledRules(folder.id));

                                            if (db.folder().getFolderDownload(folder.id))
                                                Core.downloadMessage(ServiceSynchronize.this,
                                                        folder, ifolder,
                                                        (IMAPMessage) e.getMessage(), message.id, state);
                                        } catch (MessageRemovedException ex) {
                                            Log.w(folder.name, ex);
                                        } catch (FolderClosedException ex) {
                                            throw ex;
                                        } catch (IOException ex) {
                                            if (ex.getCause() instanceof MessagingException) {
                                                Log.w(folder.name, ex);
                                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                                            } else
                                                throw ex;
                                        } catch (Throwable ex) {
                                            Log.e(folder.name, ex);
                                            db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        Core.reportError(ServiceSynchronize.this, account, folder, ex);
                                        state.error(ex);
                                    } finally {
                                        wlMessage.release();
                                    }
                                }
                            });

                            // Idle folder
                            Thread idler = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Log.i(folder.name + " start idle");
                                        while (state.running() && state.recoverable()) {
                                            Log.i(folder.name + " do idle");
                                            ifolder.idle(false);
                                        }
                                    } catch (Throwable ex) {
                                        Log.e(folder.name, ex);
                                        state.error(new FolderClosedException(ifolder, "IDLE"));
                                    } finally {
                                        Log.i(folder.name + " end idle");
                                    }
                                }
                            }, "idler." + folder.id);
                            idler.setPriority(THREAD_PRIORITY_BACKGROUND);
                            idler.start();
                            idlers.add(idler);

                            EntityOperation.sync(this, folder.id, false);
                        } else
                            mapFolders.put(folder, null);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TwoStateOwner cowner = new TwoStateOwner(ServiceSynchronize.this, folder.name);
                                cowners.add(cowner);
                                cowner.start();

                                db.operation().liveOperations(folder.id).observe(cowner, new Observer<List<EntityOperation>>() {
                                    private List<Long> handling = new ArrayList<>();
                                    private final PowerManager.WakeLock wlFolder = pm.newWakeLock(
                                            PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":folder." + folder.id);

                                    @Override
                                    public void onChanged(final List<EntityOperation> operations) {
                                        boolean process = false;
                                        List<Long> ops = new ArrayList<>();
                                        for (EntityOperation op : operations) {
                                            if (!handling.contains(op.id))
                                                process = true;
                                            ops.add(op.id);
                                        }
                                        handling = ops;

                                        if (handling.size() > 0 && process) {
                                            Log.i(folder.name + " operations=" + operations.size() +
                                                    " init=" + folder.initialize + " poll=" + folder.poll);

                                            executor.submit(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        wlFolder.acquire();
                                                        Log.i(folder.name + " process");

                                                        // Get folder
                                                        Folder ifolder = mapFolders.get(folder); // null when polling
                                                        final boolean shouldClose = (ifolder == null);

                                                        try {
                                                            Log.i(folder.name + " run " + (shouldClose ? "offline" : "online"));

                                                            if (ifolder == null) {
                                                                // Prevent unnecessary folder connections
                                                                if (db.operation().getOperationCount(folder.id, null) == 0)
                                                                    return;

                                                                db.folder().setFolderState(folder.id, "connecting");

                                                                ifolder = istore.getFolder(folder.name);
                                                                ifolder.open(Folder.READ_WRITE);

                                                                db.folder().setFolderState(folder.id, "connected");

                                                                db.folder().setFolderError(folder.id, null);
                                                            }

                                                            Core.processOperations(ServiceSynchronize.this,
                                                                    account, folder,
                                                                    isession, istore, ifolder,
                                                                    state);

                                                        } catch (Throwable ex) {
                                                            Log.e(folder.name, ex);
                                                            Core.reportError(ServiceSynchronize.this, account, folder, ex);
                                                            db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                                                            state.error(ex);
                                                        } finally {
                                                            if (shouldClose) {
                                                                if (ifolder != null && ifolder.isOpen()) {
                                                                    db.folder().setFolderState(folder.id, "closing");
                                                                    try {
                                                                        ifolder.close(false);
                                                                    } catch (MessagingException ex) {
                                                                        Log.w(folder.name, ex);
                                                                    }
                                                                }
                                                                if (folder.synchronize && (folder.poll || !capIdle))
                                                                    db.folder().setFolderState(folder.id, "waiting");
                                                                else
                                                                    db.folder().setFolderState(folder.id, null);
                                                            }
                                                        }
                                                    } finally {
                                                        wlFolder.release();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    }

                    // Keep alive alarm receiver
                    BroadcastReceiver alarm = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            // Receiver runs on main thread
                            // Receiver has a wake lock for ~10 seconds
                            EntityLog.log(context, account.name + " keep alive wake lock=" + wlAccount.isHeld());
                            state.release();
                        }
                    };

                    String id = BuildConfig.APPLICATION_ID + ".POLL." + account.id;
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(id), 0);
                    registerReceiver(alarm, new IntentFilter(id));

                    // Keep alive
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    try {
                        while (state.running()) {
                            if (!state.recoverable())
                                throw new StoreClosedException(istore, "Unrecoverable");
                            if (!istore.isConnected()) // Sends store NOOP
                                throw new StoreClosedException(istore, "NOOP");

                            for (EntityFolder folder : mapFolders.keySet())
                                if (folder.synchronize)
                                    if (!folder.poll && capIdle) {
                                        if (!mapFolders.get(folder).isOpen()) // Sends folder NOOP
                                            throw new FolderClosedException(mapFolders.get(folder));
                                    } else
                                        EntityOperation.sync(this, folder.id, false);

                            // Successfully connected: reset back off time
                            backoff = CONNECT_BACKOFF_START;

                            // Record successful connection
                            account.last_connected = new Date().getTime();
                            EntityLog.log(this, account.name + " set last_connected=" + new Date(account.last_connected));
                            db.account().setAccountConnected(account.id, account.last_connected);
                            db.account().setAccountWarning(account.id, capIdle ? null : getString(R.string.title_no_idle));

                            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nm.cancel("receive", account.id.intValue());

                            // Schedule keep alive alarm
                            EntityLog.log(this, account.name + " wait=" + account.poll_interval);
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                                am.set(
                                        AlarmManager.RTC_WAKEUP,
                                        System.currentTimeMillis() + account.poll_interval * 60 * 1000L,
                                        pi);
                            else
                                am.setAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        System.currentTimeMillis() + account.poll_interval * 60 * 1000L,
                                        pi);

                            try {
                                wlAccount.release();
                                state.acquire();
                            } catch (InterruptedException ex) {
                                EntityLog.log(this, account.name + " waited state=" + state);
                            } finally {
                                wlAccount.acquire();
                            }
                        }
                    } finally {
                        // Cleanup
                        am.cancel(pi);
                        unregisterReceiver(alarm);
                    }

                    Log.i(account.name + " done state=" + state);
                } catch (StoreClosedException ex) {
                    Log.w(ex);
                } catch (Throwable ex) {
                    Log.e(account.name, ex);
                    Core.reportError(this, account, null, ex);
                    db.account().setAccountError(account.id, Helper.formatThrowable(ex));
                } finally {
                    // Stop watching for operations
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (TwoStateOwner owner : cowners)
                                owner.destroy();
                        }
                    });

                    // Update state
                    EntityLog.log(this, account.name + " closing");
                    db.account().setAccountState(account.id, "closing");
                    for (EntityFolder folder : mapFolders.keySet())
                        if (folder.synchronize && !folder.poll)
                            db.folder().setFolderState(folder.id, "closing");

                    // Close store
                    try {
                        EntityLog.log(this, account.name + " store closing");
                        istore.close();
                        EntityLog.log(this, account.name + " store closed");
                    } catch (Throwable ex) {
                        Log.w(account.name, ex);
                    } finally {
                        EntityLog.log(this, account.name + " closed");
                        db.account().setAccountState(account.id, null);
                    }

                    // Stop idlers
                    for (Thread idler : idlers)
                        state.join(idler);
                    idlers.clear();

                    // Update state
                    for (EntityFolder folder : mapFolders.keySet())
                        if (folder.synchronize && !folder.poll)
                            db.folder().setFolderState(folder.id, null);
                }

                if (state.running())
                    try {
                        if (backoff <= CONNECT_BACKOFF_MAX) {
                            // Short back-off period, keep device awake
                            EntityLog.log(this, account.name + " backoff=" + backoff);
                            state.acquire(backoff * 1000L);
                        } else {
                            // Long back-off period, let device sleep
                            EntityLog.log(this, account.name + " backoff alarm=" + CONNECT_BACKOFF_AlARM);

                            BroadcastReceiver alarm = new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    state.release();
                                }
                            };

                            String id = BuildConfig.APPLICATION_ID + ".BACKOFF." + account.id;
                            PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(id), 0);
                            registerReceiver(alarm, new IntentFilter(id));

                            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            try {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                                    am.set(
                                            AlarmManager.RTC_WAKEUP,
                                            System.currentTimeMillis() + CONNECT_BACKOFF_AlARM * 60 * 1000L,
                                            pi);
                                else
                                    am.setAndAllowWhileIdle(
                                            AlarmManager.RTC_WAKEUP,
                                            System.currentTimeMillis() + CONNECT_BACKOFF_AlARM * 60 * 1000L,
                                            pi);

                                try {
                                    wlAccount.release();
                                    state.acquire(2 * CONNECT_BACKOFF_AlARM * 60 * 1000L);
                                } finally {
                                    wlAccount.acquire();
                                }
                            } finally {
                                // Cleanup
                                am.cancel(pi);
                                unregisterReceiver(alarm);
                            }
                        }

                        if (backoff <= CONNECT_BACKOFF_MAX)
                            backoff *= 2;
                    } catch (InterruptedException ex) {
                        Log.w(account.name + " backoff " + ex.toString());
                    }
            }
        } finally {
            EntityLog.log(this, account.name + " stopped");
            wlAccount.release();
        }
    }

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            networkState.update(ConnectionHelper.getNetworkState(ServiceSynchronize.this));

            synchronized (ServiceSynchronize.this) {
                try {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    EntityLog.log(ServiceSynchronize.this, "Available " + network + " capabilities " + cm.getNetworkCapabilities(network));

                    if (networkState.isSuitable())
                        if (started) {
                            EntityLog.log(ServiceSynchronize.this, "Checking account states");

                            Thread check = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        DB db = DB.getInstance(ServiceSynchronize.this);

                                        boolean disconnected = false;
                                        List<EntityAccount> accounts = db.account().getSynchronizingAccounts();
                                        for (EntityAccount account : accounts)
                                            if (!"connected".equals(account.state)) {
                                                disconnected = true;
                                                break;
                                            }

                                        if (disconnected)
                                            new Handler(getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        synchronized (ServiceSynchronize.this) {
                                                            queue_reload(true, false, "Some accounts disconnected");
                                                        }
                                                    } catch (Throwable ex) {
                                                        Log.e(ex);
                                                    }
                                                }
                                            });
                                    } catch (Throwable ex) {
                                        Log.e(ex);
                                    }
                                }
                            }, "synchronize:connectivity");
                            check.setPriority(THREAD_PRIORITY_BACKGROUND);
                            check.start();
                        } else
                            queue_reload(true, false, "connect " + network);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
            networkState.update(ConnectionHelper.getNetworkState(ServiceSynchronize.this));

            synchronized (ServiceSynchronize.this) {
                try {
                    if (!started) {
                        EntityLog.log(ServiceSynchronize.this, "Network " + network + " capabilities " + capabilities);
                        if (networkState.isSuitable())
                            queue_reload(true, false, "capabilities " + network);
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }

        @Override
        public void onLost(Network network) {
            networkState.update(ConnectionHelper.getNetworkState(ServiceSynchronize.this));

            synchronized (ServiceSynchronize.this) {
                try {
                    EntityLog.log(ServiceSynchronize.this, "Lost " + network);

                    if (started && !networkState.isSuitable()) {
                        lastLost = new Date().getTime();
                        queue_reload(false, false, "disconnect " + network);
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }
    };

    static void boot(final Context context) {
        if (!booted) {
            booted = true;

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DB db = DB.getInstance(context);

                        // Restore snooze timers
                        for (EntityMessage message : db.message().getSnoozed())
                            EntityMessage.snooze(context, message.id, message.ui_snoozed);

                        // Restore schedule
                        schedule(context);

                        // Conditionally init service
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean enabled = prefs.getBoolean("enabled", true);
                        int pollInterval = prefs.getInt("poll_interval", 0);
                        int accounts = db.account().getSynchronizingAccounts().size();
                        if (enabled && pollInterval == 0 && accounts > 0)
                            ContextCompat.startForegroundService(context,
                                    new Intent(context, ServiceSynchronize.class)
                                            .setAction("init"));
                        else {
                            for (EntityAccount account : db.account().getAccounts())
                                db.account().setAccountState(account.id, null);

                            for (EntityFolder folder : db.folder().getFolders()) {
                                db.folder().setFolderState(folder.id, null);
                                db.folder().setFolderSyncState(folder.id, null);
                            }
                        }
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            }, "synchronize:boot");
            thread.setPriority(THREAD_PRIORITY_BACKGROUND);
            thread.start();
        }
    }

    private static void schedule(Context context) {
        Intent alarm = new Intent(context, ServiceSynchronize.class);
        alarm.setAction("alarm");
        PendingIntent piAlarm;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            piAlarm = PendingIntent.getService(context, PI_ALARM, alarm, PendingIntent.FLAG_UPDATE_CURRENT);
        else
            piAlarm = PendingIntent.getForegroundService(context, PI_ALARM, alarm, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(piAlarm);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("schedule", false))
            return;

        int minuteStart = prefs.getInt("schedule_start", 0);
        int minuteEnd = prefs.getInt("schedule_end", 0);

        if (minuteEnd <= minuteStart)
            minuteEnd += 24 * 60;

        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.HOUR_OF_DAY, minuteStart / 60);
        calStart.set(Calendar.MINUTE, minuteStart % 60);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        Calendar calEnd = Calendar.getInstance();
        calEnd.set(Calendar.HOUR_OF_DAY, minuteEnd / 60);
        calEnd.set(Calendar.MINUTE, minuteEnd % 60);
        calEnd.set(Calendar.SECOND, 0);
        calEnd.set(Calendar.MILLISECOND, 0);

        long now = new Date().getTime();
        if (now > calEnd.getTimeInMillis()) {
            calStart.set(Calendar.DAY_OF_MONTH, calStart.get(Calendar.DAY_OF_MONTH) + 1);
            calEnd.set(Calendar.DAY_OF_MONTH, calEnd.get(Calendar.DAY_OF_MONTH) + 1);
        }

        long start = calStart.getTimeInMillis();
        long end = calEnd.getTimeInMillis();
        long next = (now < start ? start : end);

        Log.i("Schedule now=" + new Date(now));
        Log.i("Schedule start=" + new Date(start));
        Log.i("Schedule end=" + new Date(end));
        Log.i("Schedule next=" + new Date(next));

        boolean enabled = (now >= start && now < end);
        prefs.edit().putBoolean("enabled", enabled).apply();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, piAlarm);
        else
            am.set(AlarmManager.RTC_WAKEUP, next, piAlarm);

        WorkerPoll.init(context);
    }

    static void reschedule(Context context) {
        ContextCompat.startForegroundService(context,
                new Intent(context, ServiceSynchronize.class)
                        .setAction("alarm"));
    }

    static void reload(Context context, String reason) {
        reload(context, false, reason);
    }

    static void reload(Context context, boolean clear, String reason) {
        ContextCompat.startForegroundService(context,
                new Intent(context, ServiceSynchronize.class)
                        .setAction("reload")
                        .putExtra("clear", clear)
                        .putExtra("reason", reason));
    }

    static void reset(Context context) {
        ContextCompat.startForegroundService(context,
                new Intent(context, ServiceSynchronize.class)
                        .setAction("reset"));
    }

    static void process(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        int pollInterval = prefs.getInt("poll_interval", 0);
        if (!enabled || pollInterval > 0) {
            oneshot = true;
            ContextCompat.startForegroundService(context,
                    new Intent(context, ServiceSynchronize.class)
                            .setAction("oneshot_start"));
        }
    }

    static void watchdog(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        int pollInterval = prefs.getInt("poll_interval", 0);
        if (enabled && pollInterval == 0)
            ContextCompat.startForegroundService(context,
                    new Intent(context, ServiceSynchronize.class)
                            .setAction("watchdog"));
    }
}
