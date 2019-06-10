/*
 * Copyright (C) 2016.  BoBoMEe(wbwjx115@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bobomee.android.common.util;

import android.app.Activity;
import java.util.Stack;

/**
 * Created on 16/7/17.下午1:47.
 * activity管理工具类
 *
 * @author bobomee.
 *         wbwjx115@gmail.com
 */
public enum ActivityStack {

  INSTANCE;

  //容器
  private Stack<Activity> mActivityStack;

  public void add(Activity activity) {

    if (null != mActivityStack) {
      mActivityStack = new Stack<>();
    }

    mActivityStack.add(activity);
  }

  public void remove(Activity activity) {
    if (null != mActivityStack && mActivityStack.size() > 0) {
      if (null != activity) {
        mActivityStack.remove(activity);
      }
    }
  }

  //获取栈顶的activity

  public Activity getLastActivity() {
    return mActivityStack.lastElement();
  }

  public void finish(Activity activity) {
    remove(activity);
    if (null != activity) {
      activity.finish();
    }
  }

  public void finsh(Class activity) {
    for (Activity activity1 : mActivityStack) {
      if (activity1.getClass().equals(activity)) {
        finish(activity1);
      }
    }
  }

  public void finishAll() {
    for (Activity activity : mActivityStack) {
      if (null != activity) {
        activity.finish();
      }
    }
    mActivityStack.clear();
  }

  public void appExit() {
    try {
      finishAll();
      //退出JVM（java虚拟机）,释放所占内存资源,0表示正常退出(非0的都为异常退出)
      System.exit(0);
      //从操作系统中结束掉当前程序的进程
      android.os.Process.killProcess(android.os.Process.myPid());
    } catch (Exception e) {
    }
  }

}
