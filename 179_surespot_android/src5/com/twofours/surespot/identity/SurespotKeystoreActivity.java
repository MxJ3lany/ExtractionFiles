package com.twofours.surespot.identity;

import org.nick.androidkeystore.android.security.KeyStore;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;

import com.twofours.surespot.R;
import com.twofours.surespot.SurespotConstants;
import com.twofours.surespot.SurespotLog;
import com.twofours.surespot.utils.Utils;

public class SurespotKeystoreActivity extends Activity {

	private static final String TAG = "SurespotKeystoreActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Utils.logIntent(TAG, getIntent());			
		this.unlock(this);
	}

	private void unlock(Activity activity) {

		if (IdentityController.USE_PUBLIC_KEYSTORE_M) {
			// nothing we can do - at some point we may want to consider falling back to non-public API and org.nick implementation
			return;
		}

		KeyStore keystore = IdentityController.getKeystore();
		if (keystore == null) {
			finish();
			return;
		}
		if (keystore.state() == KeyStore.State.UNLOCKED) {
			Utils.putSharedPrefsBoolean(activity, SurespotConstants.PrefNames.KEYSTORE_ENABLED, true);
			finish();
			return;
		}

		try {
			Intent intent = null;
			intent = new Intent(IdentityController.UNLOCK_ACTION);

			Utils.putSharedPrefsBoolean(activity, SurespotConstants.PrefNames.KEYSTORE_ENABLED, true);
			this.startActivity(intent);
		}
		catch (ActivityNotFoundException e) {
			SurespotLog.e(TAG, e, "No UNLOCK activity: %s", e.getMessage());
			Utils.makeLongToast(activity, activity.getString(R.string.keystore_not_supported));
			Utils.putSharedPrefsBoolean(activity, SurespotConstants.PrefNames.KEYSTORE_ENABLED, false);
			finish();
			return;
		}
	}
}
