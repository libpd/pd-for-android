package org.puredata.android.service;

import java.util.Arrays;

import org.puredata.android.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class KillPdService extends Activity {

	private IPdService proxy = null;
	
	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			proxy = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			proxy = IPdService.Stub.asInterface(service);
			try {
				proxy.stop();
			} catch (RemoteException e) {
				Log.e("Pd Kill", e.toString());
			}
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		bindService(new Intent("org.puredata.android.service.LAUNCH"), connection, BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unbindService(connection);
	}
}
