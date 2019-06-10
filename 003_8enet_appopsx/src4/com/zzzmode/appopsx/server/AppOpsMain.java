package com.zzzmode.appopsx.server;


import android.app.ActivityThread;
import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.system.Os;
import android.text.TextUtils;
import com.zzzmode.appopsx.common.FLog;
import com.zzzmode.appopsx.common.OpsDataTransfer;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AppOpsMain {

  private static final String APPOPSX_PKG="com.zzzmode.appopsx";

  public static void main(String[] args) {

    try {
      FLog.writeLog = true;
      FLog.log("start ops server args:" + Arrays.toString(args));
      if (args == null) {
        return;
      }

      Looper.prepareMainLooper();
      ActivityThread.systemMain();

      String[] split = args[0].split(",");
      final Map<String, String> params = new HashMap<>();
      for (String s : split) {
        String[] param = s.split(":");
        params.put(param[0], param[1]);
      }
      params.put("type",Process.myUid() == 0?"root":"adb");

      LifecycleAgent.serverRunInfo.startArgs = args[0];
      LifecycleAgent.serverRunInfo.startTime = System.currentTimeMillis();
      LifecycleAgent.serverRunInfo.startRealTime = SystemClock.elapsedRealtime();

      System.out.println("type ---> "+params.get("type")+"    uid: "+Process.myUid());
      System.out.println("params ---> "+params);

      if(args.length > 1 && !TextUtils.isEmpty(args[1])){
        killOld(args[1]);
        SystemClock.sleep(1000);
      }


      Thread thread=new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            new AppOpsMain(params);
          } catch (Exception e) {
            e.printStackTrace();
            FLog.log(e);
          }finally {
            FLog.close();
            stop();
          }
        }
      });
      thread.setName("IPC-appopsx");
      thread.start();

      Looper.loop();
    } catch (Throwable e) {
      e.printStackTrace();
      FLog.log(e);
    } finally {
      FLog.log("close log ... ");
      FLog.close();
    }
    stop();
  }


  private static void killOld(String pid){
    try{
      int oldPid = Integer.parseInt(pid);
      String processName = getProcessName(oldPid);
      if("appopsx_local_server".equals(processName)){
        Process.killProcess(oldPid);

        FLog.log("killed pid : "+pid);
      }
    }catch (Throwable throwable){
      FLog.log(throwable);
    }
  }

  private static void stop(){
    int pid = Process.myPid();
    System.out.println(" STOP: kill myself ---- pid: " + pid);
    killProcess(pid);
  }


  private static void killProcess(int pid){
    try {

      Process.killProcess(pid);
    } catch (Throwable e) {
      e.printStackTrace();
    }finally {
      try {
        //only killProcess fail will be call.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          Os.execve("/system/bin/kill", new String[]{"-9", String.valueOf(pid)}, null);
        } else {
          Runtime.getRuntime().exec("kill -9 " + pid);
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }


  private RemoteHandler mCallHandler;

  private AppOpsMain(Map<String, String> params) throws Exception {

    try {
      mCallHandler = new RemoteHandler(params);
      LifecycleAgent.sParams = new HashMap<>(params);
      System.out.println("AppOpsX server start successful, enjoy it! \uD83D\uDE0E");
      int pid = Process.myPid();
      System.out.println(getProcessName(pid)+"   pid:"+ pid);
      LifecycleAgent.onStarted();
      mCallHandler.start();
    }catch (OpsDataTransfer.ProtocolVersionException e){
      // TODO: 2018/1/12 hard code !!!
      // server side is old version, restart server
      Runtime.getRuntime().exec("sh /sdcard/Android/data/com.zzzmode.appopsx/opsx.sh "+ Process.myPid());
      SystemClock.sleep(1000);

    }catch (Throwable throwable){
      FLog.log(throwable);
    }finally {
      destory();
    }


  }


  private void destory() {
    try {
      if(mCallHandler != null){
        mCallHandler.destory();
      }
      FLog.log("handler destory ----- ");
    } catch (Exception e) {
      e.printStackTrace();
      FLog.log(e);
    }

    LifecycleAgent.onStoped();
  }


  static String getProcessName(int pid){
    FileInputStream fis = null;
    try {
      byte[] buff = new byte[512];
      fis = new FileInputStream("/proc/"+pid+"/cmdline");
      int len = fis.read(buff);
      if (len > 0) {
        int i;
        for (i=0; i<len; i++) {
          if (buff[i] == '\0') {
            break;
          }
        }
        return new String(buff,0,i);
      }
    }catch (Exception e){
      e.printStackTrace();
    }finally {
      try {
        if (fis != null) {
          fis.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return null;
  }

}
