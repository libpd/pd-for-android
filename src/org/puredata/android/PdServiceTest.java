package org.puredata.android;

import org.puredata.android.service.PdInterface;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class PdServiceTest extends Activity {

	private PdInterface pd = null;
	private String patch = null;
	private WakeLock wakeLock = null;
	
	private final ServiceConnection connection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// do nothing, for now...
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			pd = PdInterface.Stub.asInterface(service);
			Resources res = getResources();
			try {
				pd.openAudio(res.getInteger(R.integer.sampleRate),
						res.getInteger(R.integer.inChannels),
						res.getInteger(R.integer.outChannels),
						res.getInteger(R.integer.ticksPerBuffer));
				patch = pd.openPatch(res.getString(R.string.patch), res.getString(R.string.folder));
				pd.sendBang("foo");
				pd.sendFloat("foo", 42);
				pd.sendSymbol("foo", "test");
			} catch (RemoteException e) {
				Log.e("Pd Client", e.toString());
			}
		}
	};
	
	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	};

	@Override
	protected void onStart() {
		super.onStart();
		bindService(new Intent(PdInterface.class.getName()), connection, BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "pd wakelock");
		wakeLock.acquire();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (pd != null) {
			if (patch != null) {
				try {
					pd.closePatch(patch);
					pd.closeAudio();
				} catch (RemoteException e) {
					Log.e("Pd Client", e.toString());
				}
			}
		}
		wakeLock.release();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		unbindService(connection);
	}
}
