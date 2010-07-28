/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * simple test case for {@link PdService}
 * 
 */

package org.puredata.android.service;

import org.puredata.android.R;
import org.puredata.android.io.AudioParameters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PdPreferences extends PreferenceActivity {

	private IPdService proxy = null;

	private final IPdClient.Stub client = new IPdClient.Stub() {
		@Override
		public void handleStop() throws RemoteException {
			finish();
		}

		@Override
		public void handleStart(int sampleRate, int nIn, int nOut, float bufferSizeMillis) throws RemoteException {
			// do nothing
		}

		@Override
		public void print(final String s) throws RemoteException {
			// do nothing
		}
	};
	
	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			proxy = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			proxy = IPdService.Stub.asInterface(service);
			try {
				proxy.addClient(client);
			} catch (RemoteException e) {
				// do nothing
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initPreferences(getApplicationContext());
		addPreferencesFromResource(R.xml.preferences);
		bindService(new Intent("org.puredata.android.service.LAUNCH"), connection, BIND_AUTO_CREATE);	
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (proxy != null) {
			try {
				proxy.removeClient(client);
			} catch (RemoteException e) {
				// do nothing
			}
		}
		unbindService(connection);
	}
	
	public static void initPreferences(Context context) {
		Resources res = context.getResources();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (!prefs.contains(res.getString(R.string.pref_key_srate))) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(res.getString(R.string.pref_key_srate), "" + AudioParameters.suggestSampleRate());
			editor.putString(res.getString(R.string.pref_key_inchannels), "" + AudioParameters.suggestInputChannels());
			editor.putString(res.getString(R.string.pref_key_outchannels), "" + AudioParameters.suggestOutputChannels());
			editor.putString(res.getString(R.string.pref_key_bufsize_millis), "" + AudioParameters.suggestBufferSizeMillis());
			editor.commit();
		}
	}
}
