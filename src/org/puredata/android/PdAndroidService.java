/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * just a placeholder for now...
 */

package org.puredata.android;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class PdAndroidService extends Service {

	private final IBinder binder = new PdBinder();
	
	public class PdBinder extends Binder {
		public PdAndroidService getService() {
			return PdAndroidService.this;
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}
}
