/*
 * Copyright (C) 2017 Peng fei Pan <sky@panpf.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.panpf.tool4a.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Process;
import android.widget.Toast;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;
import java.util.GregorianCalendar;

import me.panpf.tool4a.content.LaunchAppReceiver;
import me.panpf.tool4j.lang.StringUtils;

/**
 * 重启线程异常处理器，当发生未知异常时会提示异常信息并在一秒钟后重新启动应用
 * <br>使用此功能的第一步需要你在 AndroidManifest.xml 中注册 {@link LaunchAppReceiver} 广播（注意不要任何的 filter）
 * <br>第二步就是在你的 Application 的 onCreate() 方法中执行 new {@link RebootThreadExceptionHandler}(getBaseContext()) 即可
 */
public class RebootThreadExceptionHandler implements UncaughtExceptionHandler {
    private Context context;
    private String hintText;

    public RebootThreadExceptionHandler(Context context, String hintText) {
        this.context = context;
        this.hintText = hintText;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public RebootThreadExceptionHandler(Context context) {
        this(context, null);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();//输出异常信息到控制台

        if (StringUtils.isNotEmpty(hintText)) {
            /* 启动新线程提示程序异常 */
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(context, hintText, Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }).start();

			/* 主线程等待1秒钟，让提示信息显示出来 */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

		/* 设置定时器，在1秒钟后发出启动程序的广播 */
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.SECOND, 1);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, LaunchAppReceiver.class), 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Process.killProcess(Process.myPid());    //结束程序
    }
}