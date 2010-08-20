/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.fifths;

import java.io.File;
import java.io.IOException;

import org.puredata.android.ioutils.IoUtils;
import org.puredata.android.service.IPdClient;
import org.puredata.android.service.IPdService;
import org.puredata.android.service.PdUtils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


public class CircleOfFifths extends Activity {

	private static final String PD_CIRCLE = "Pd Circle Of Fifths";
	private final Handler handler = new Handler();
	private IPdService pdServiceProxy = null;
	private boolean hasAudio = false;
	private TextView logs;
	private File patchFile;
	private String patch;

	private void post(final String msg) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), PD_CIRCLE + ": " + msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void log(final String msg) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				logs.append(msg + ((msg.endsWith("\n")) ? "" : "\n"));
			}
		});
	}

	private final IPdClient.Stub statusWatcher = new IPdClient.Stub() {
		@Override
		public void requestUnbind() throws RemoteException {
			post("Pure Data was stopped externally; exiting now");
			finish();
		}

		@Override
		public void audioChanged(int sampleRate, int nIn, int nOut, float bufferSizeMillis) throws RemoteException {
			if (sampleRate > 0) {
				post("Audio parameters: sample rate: " + sampleRate + ", input channels: " + nIn + ", output channels: " + nOut + 
						", buffer size: " + bufferSizeMillis + "ms");
			} else {
				post("Audio stopped");
			}
		}

		@Override
		public void print(String s) throws RemoteException {
			log(s);
		}
	};

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public synchronized void onServiceDisconnected(ComponentName name) {
			pdServiceProxy = null;
			disconnected();
		}

		@Override
		public synchronized void onServiceConnected(ComponentName name, IBinder service) {
			pdServiceProxy = IPdService.Stub.asInterface(service);
			initPd();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initGui();
		try {
			File dir = new File("/sdcard/pd");
			IoUtils.extractZipResource(getResources().openRawResource(R.raw.patch), dir, true);
			patchFile = new File(dir, "chords.pd");
		} catch (IOException e) {
			post(e.toString());
			finish();
		}
		bindService(new Intent(PdUtils.LAUNCH_ACTION), serviceConnection, BIND_AUTO_CREATE);
	}

	// this callback makes sure that we handle orientation changes without audio glitches
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		initGui();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cleanup();
	}

	private void initGui() {
		setContentView(R.layout.main);
		CircleView circle = (CircleView) findViewById(R.id.circleview);
		circle.setOwner(this);
		logs = (TextView) findViewById(R.id.logview);
		logs.setMovementMethod(new ScrollingMovementMethod());
	}

	private void initPd() {
		try {
			pdServiceProxy.addClient(statusWatcher);
			patch = PdUtils.openPatch(pdServiceProxy, patchFile.getAbsoluteFile());
			int err = pdServiceProxy.requestAudio(44100, 1, 2, 50);
			hasAudio = (err == 0);
			if (!hasAudio) {
				post("unable to start audio; exiting now");
				finish();
			}
		} catch (RemoteException e) {
			Log.e(PD_CIRCLE, e.toString());
			disconnected();
		} catch (IOException e) {
			post(e.toString() + "; exiting now");
			finish();
		}
	}

	@Override
	public void finish() {
		cleanup();
		super.finish();
	}

	private void disconnected() {
		post("lost connection to Pd Service; exiting now");
		finish();
	}

	private void cleanup() {
		synchronized (serviceConnection) {  // on the remote chance that service gets disconnected while we're here
			if (pdServiceProxy == null) return;
			try {
				// make sure to release all resources
				pdServiceProxy.removeClient(statusWatcher);
				PdUtils.closePatch(pdServiceProxy, patch);
				if (hasAudio) pdServiceProxy.releaseAudio();  // only release audio if you actually have it...
			} catch (RemoteException e) {
				Log.e(PD_CIRCLE, e.toString());
			}
		}
		try {
			unbindService(serviceConnection);
		} catch (IllegalArgumentException e) {
			// already unbound
			pdServiceProxy = null;
		}
	}

	public void playChord(int n, boolean b) {
		synchronized (serviceConnection) {
			if (pdServiceProxy == null) return;
			try {
				PdUtils.sendList(pdServiceProxy, "playchord", n, b ? 1 : 0);
			} catch (RemoteException e) {
				post(e.toString());
				finish();
			}
		}
	}

	public void shift(int d) {
		synchronized (serviceConnection) {
			if (pdServiceProxy == null) return;
			try {
				pdServiceProxy.sendFloat("shift", d);
			} catch (RemoteException e) {
				post(e.toString());
				finish();
			}
		}
	}

	public void endChord() {
		synchronized (serviceConnection) {
			if (pdServiceProxy == null) return;
			try {
				pdServiceProxy.sendBang("endchord");
			} catch (RemoteException e) {
				post(e.toString());
				finish();
			}
		}
	}
}