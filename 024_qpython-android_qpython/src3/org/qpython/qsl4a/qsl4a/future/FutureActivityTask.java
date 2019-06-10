/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.qpython.qsl4a.qsl4a.future;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;

/**
 * Encapsulates an {@link Activity} and a {@link FutureObject}.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public abstract class FutureActivityTask<T> {

	final private String TAG = "FutureActivityTask";
	private final FutureResult<T> mResult = new FutureResult<T>();
	private Activity mActivity;

	public void setActivity(Activity activity) {
		mActivity = activity;
	}

	public Activity getActivity() {
		return mActivity;
	}

	public void onCreate() {
		Log.d(TAG, "onCreate");
		mActivity.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
	}

  public void onStart() {
		Log.d(TAG, "onStart");

  }

  public void onResume() {
		Log.d(TAG, "onResume");

  }

  public void onPause() {
		Log.d(TAG, "onPause");

  }

  public void onStop() {
		Log.d(TAG, "onStop");

  }

  public void onDestroy() {
		Log.d(TAG, "onDestroy");

  }

  public void onNewIntent(Intent intent) {
  }
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
  }

  public boolean onPrepareOptionsMenu(Menu menu) {
    return false;
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
  }

  protected void setResult(T result) {
    mResult.set(result);
  }

  public T getResult() throws InterruptedException {
    return mResult.get();
  }

  public void finish() {
    mActivity.finish();
  }

  public void startActivity(Intent intent) {
    mActivity.startActivity(intent);
  }

  public void startActivityForResult(Intent intent, int requestCode) {
    mActivity.startActivityForResult(intent, requestCode);
  }

  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Placeholder.
    return false;
  }
}
