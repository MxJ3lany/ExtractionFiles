/***
  Copyright (c) 2008-2015 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  Covered in detail in the book _The Busy Coder's Guide to Android Development_
    https://commonsware.com/Android
 */

package com.commonsware.android.weather3;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class WeatherDemo extends AbstractGoogleApiClientActivity {
  private static final String[] PERMS=
    {Manifest.permission.ACCESS_FINE_LOCATION};

  @Override
  protected GoogleApiClient.Builder configureApiClientBuilder(
    GoogleApiClient.Builder b) {
    return(b.addApi(LocationServices.API));
  }

  @Override
  protected String[] getDesiredPermissions() {
    return(PERMS);
  }

  @Override
  protected void handlePermissionDenied() {
    Toast
      .makeText(this, R.string.msg_no_perm, Toast.LENGTH_LONG)
      .show();
    finish();
  }

  @Override
  public void onConnected(Bundle bundle) {
    if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
      getFragmentManager().beginTransaction()
        .add(android.R.id.content,
          new WeatherFragment()).commit();
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    Log.w(((Object)this).getClass().getSimpleName(),
      "onConnectionSuspended() called, whatever that means");
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode==WeatherFragment.SETTINGS_REQUEST_ID) {
      if (resultCode==Activity.RESULT_CANCELED) {
        finish();
      }
      else {
        // this should not be needed, but apparently is in 8.1
        WeatherFragment f=
          (WeatherFragment)getFragmentManager().findFragmentById(android.R.id.content);
        f.requestLocations();
      }
    }
  }
}
