package dev.ukanth.ufirewall.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import dev.ukanth.ufirewall.Api;
import dev.ukanth.ufirewall.MainActivity;
import dev.ukanth.ufirewall.R;
import dev.ukanth.ufirewall.broadcast.ConnectivityChangeReceiver;
import dev.ukanth.ufirewall.broadcast.PackageBroadcast;
import dev.ukanth.ufirewall.util.G;

public class FirewallService extends Service {

    private static final int NOTIFICATION_ID = 1;
    BroadcastReceiver connectivityReciver;
    BroadcastReceiver packageInstallReceiver;
    BroadcastReceiver packageUninstallReceiver;
    IntentFilter filter;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        addNotification();
    }


    private void addNotification() {
        String NOTIFICATION_CHANNEL_ID = "firewall.service";
        String channelName = getString(R.string.firewall_service);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            assert manager != null;
            if(G.getNotificationPriority() == 0) {
                notificationChannel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            }
            notificationChannel.setSound(null, null);
            notificationChannel.enableLights(false);
            notificationChannel.setShowBadge(false);
            notificationChannel.enableVibration(false);
            manager.createNotificationChannel(notificationChannel);
        }


        Intent appIntent = new Intent(this, MainActivity.class);
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);


        /*TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(appIntent);*/

        int icon;
        String notificationText = "";

        if (Api.isEnabled(this)) {
            if (G.enableMultiProfile()) {
                String profile = "";
                switch (G.storedProfile()) {
                    case "AFWallPrefs":
                        profile = G.gPrefs.getString("default", getString(R.string.defaultProfile));
                        break;
                    case "AFWallProfile1":
                        profile = G.gPrefs.getString("profile1", getString(R.string.profile1));
                        break;
                    case "AFWallProfile2":
                        profile = G.gPrefs.getString("profile2", getString(R.string.profile2));
                        break;
                    case "AFWallProfile3":
                        profile = G.gPrefs.getString("profile3", getString(R.string.profile3));
                        break;
                    default:
                        profile = G.storedProfile();
                        break;
                }
                notificationText = getString(R.string.active) + " (" + profile + ")";
            } else {
                notificationText = getString(R.string.active);
            }
            //notificationText = context.getString(R.string.active);
            icon = R.drawable.notification;
        } else {
            notificationText = getString(R.string.inactive);
            icon = R.drawable.notification_error;
        }


        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setContentIntent(notifyPendingIntent);

        int notifyType = G.getNotificationPriority();
        Notification notification = notificationBuilder
                .setContentTitle(getString(R.string.app_name))
                .setTicker(getString(R.string.app_name))
                .setSound(null)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_SECRET)
                .setContentText(notificationText)
                .setSmallIcon(icon)
                .build();
        switch (notifyType) {
            case 0:
                notification.priority = NotificationCompat.PRIORITY_LOW;
                break;
            case 1:
                notification.priority = NotificationCompat.PRIORITY_MIN;
                break;
        }

        if(G.activeNotification()) {
            notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground(NOTIFICATION_ID, notification);
            } else {
                manager.notify(NOTIFICATION_ID, notification);
            }
        } else {
            //start with empty notification
            startForeground(NOTIFICATION_ID, new Notification());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connectivityReciver = new ConnectivityChangeReceiver();
        filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReciver, filter);


        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        packageInstallReceiver = new PackageBroadcast();
        registerReceiver(packageInstallReceiver, intentFilter);


        intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        packageUninstallReceiver = new PackageBroadcast();
        intentFilter.addDataScheme("package");
        registerReceiver(packageUninstallReceiver, intentFilter);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connectivityReciver != null) {
            unregisterReceiver(connectivityReciver);
            connectivityReciver = null;
        }
        if (packageInstallReceiver != null) {
            unregisterReceiver(packageInstallReceiver);
            packageInstallReceiver = null;
        }
        if (packageUninstallReceiver != null) {
            unregisterReceiver(packageUninstallReceiver);
            packageUninstallReceiver = null;
        }
    }
}
