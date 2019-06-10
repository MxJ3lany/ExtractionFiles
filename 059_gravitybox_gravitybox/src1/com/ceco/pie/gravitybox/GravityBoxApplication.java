/*
 * Copyright (C) 2018 Peter Gregus for GravityBox Project (C3C076@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ceco.pie.gravitybox;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

public class GravityBoxApplication extends Application {
    public static final String NOTIF_CHANNEL_SERVICES = "services";
    public static final String NOTIF_CHANNEL_UNC = "unc";

    @Override
    public void onCreate() {
        super.onCreate();

        setupNotificationChannels();
    }

    private void setupNotificationChannels() {
        NotificationChannel services = new NotificationChannel(
                NOTIF_CHANNEL_SERVICES, getString(R.string.notif_channel_services_title),
                NotificationManager.IMPORTANCE_LOW);
        services.setDescription(getString(R.string.notif_channel_services_desc));
        services.enableLights(false);
        services.enableVibration(false);
        services.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        services.setShowBadge(false);

        NotificationChannel unc = new NotificationChannel(
                NOTIF_CHANNEL_UNC, getString(R.string.notif_channel_unc_title),
                NotificationManager.IMPORTANCE_DEFAULT);
        unc.setDescription(getString(R.string.notif_channel_unc_desc));
        unc.enableLights(true);
        unc.enableVibration(true);
        unc.setShowBadge(false);
        unc.setBypassDnd(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(services);
        manager.createNotificationChannel(unc);
    }
}
