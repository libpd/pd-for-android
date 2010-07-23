/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * This activity kills the Pd service when the users taps on the service notification item.
 * 
 */

package org.puredata.android.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
			finish();
		}
	};
	
	@Override
	protected void onStart() {
		super.onStart();
		bindService(new Intent("org.puredata.android.service.LAUNCH"), connection, BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		unbindService(connection);
	}
}
