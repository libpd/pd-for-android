/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.scenes;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.puredata.android.ioutils.IoUtils;
import org.puredata.android.service.IPdClient;
import org.puredata.android.service.IPdListener;
import org.puredata.android.service.IPdService;
import org.puredata.android.service.PdUtils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;


public class ScenePlayer extends Activity implements SensorEventListener, OnTouchListener {

	public static final String SCENE = "SCENE";
	private static final String TAG = "Pd Scene Player";
	private static final String RJDJ_ANDROID = "rjdj_android";
	private static final String RJ_IMAGE_ANDROID = "rj_image_android";
	private final Handler handler = new Handler();
	private ImageView img;
	private TextView logs;
	private File folder;
	private IPdService pdServiceProxy = null;
	private boolean hasAudio = false;
	private String patch;  // the path to the patch is defined in res/values/strings.xml
	private final File libDir = new File("/sdcard/pd/.scenes");

	private void post(final String msg) {
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
			post(s);
		}
	};
	
	private final IPdListener.Stub imageListener = new IPdListener.Stub() {
		
		@SuppressWarnings("unchecked")
		@Override
		public void receiveList(List args) throws RemoteException {
			post("rj_image: " + args);
		}
		
		// the remaining methods will never be called
		@SuppressWarnings("unchecked")
		@Override public void receiveMessage(String symbol, List args) throws RemoteException {}
		@Override public void receiveSymbol(String symbol) throws RemoteException {}
		@Override public void receiveFloat(float x) throws RemoteException {}
		@Override public void receiveBang() throws RemoteException {}
	};
	
	private final IPdListener.Stub rjdjListener = new IPdListener.Stub() {
		
		@Override
		public void receiveSymbol(String symbol) throws RemoteException {
			// TODO Auto-generated method stub
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void receiveMessage(String symbol, List args) throws RemoteException {
			// TODO Auto-generated method stub
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void receiveList(List args) throws RemoteException {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void receiveFloat(float x) throws RemoteException {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void receiveBang() throws RemoteException {
			// TODO Auto-generated method stub
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
		Intent intent = getIntent();
		String path = intent.getStringExtra(SCENE);
		if (path != null) {
			folder = new File(path);
		} else {
			Resources res = getResources();
			File common = new File(res.getString(R.string.scene_path_common));
			folder = new File(common, res.getString(R.string.scene_name));
		}
		initGui();
		try {
			IoUtils.extractZipResource(getResources().openRawResource(R.raw.abstractions), libDir);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		bindService(new Intent(PdUtils.LAUNCH_ACTION), serviceConnection, BIND_AUTO_CREATE);
		SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (serviceConnection) {
			if (pdServiceProxy != null) {
				try {
					PdUtils.sendList(pdServiceProxy, "#accelerate", event.values[0], event.values[1], event.values[2]);
				} catch (RemoteException e) {
					Log.e(TAG, e.toString());
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// don't care
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean flag = false;
		if (v == img) {
			synchronized (serviceConnection) {
				if (pdServiceProxy != null) {
					try {
						flag = VersionedTouch.evaluateTouch(pdServiceProxy, event, img.getWidth(), img.getHeight());
					} catch (RemoteException e) {
						Log.e(TAG, e.toString());
					}
				}
			}
		}
		return flag;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cleanup();
	}

	private void initGui() {
		setContentView(R.layout.main);
		TextView tv = (TextView) findViewById(R.id.scene_title);
		tv.setText(folder.getName());
		logs = (TextView) findViewById(R.id.scene_logs);
		logs.setMovementMethod(new ScrollingMovementMethod());
		img = (ImageView) findViewById(R.id.scene_image);
		img.setOnTouchListener(this);
		img.setImageBitmap(BitmapFactory.decodeFile(new File(folder, "image.jpg").getAbsolutePath()));
	}

	private void initPd() {
		try {
			pdServiceProxy.addClient(statusWatcher);
			pdServiceProxy.addToSearchPath(libDir.getAbsolutePath());
			pdServiceProxy.subscribe(RJDJ_ANDROID, rjdjListener);
			pdServiceProxy.subscribe(RJ_IMAGE_ANDROID, imageListener);
			patch = PdUtils.openPatch(pdServiceProxy, new File(folder, "_main.pd"));
			int err = pdServiceProxy.requestAudio(22050, 1, 2, -1); // negative values default to PdService preferences
			hasAudio = (err == 0);
			if (!hasAudio) {
				post("unable to start audio; exiting now");
				finish();
			} else {
				PdUtils.sendMessage(pdServiceProxy, "#volume", "set", 1);
				PdUtils.sendMessage(pdServiceProxy, "#transport", "play", 1);
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.toString());
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
				pdServiceProxy.unsubscribe(RJDJ_ANDROID, rjdjListener);
				pdServiceProxy.unsubscribe(RJ_IMAGE_ANDROID, imageListener);
				PdUtils.closePatch(pdServiceProxy, patch);
				if (hasAudio) {
					hasAudio = false;
					pdServiceProxy.releaseAudio();  // only release audio if you actually have it...
				}
			} catch (RemoteException e) {
				Log.e(TAG, e.toString());
			}
		}
		try {
			unbindService(serviceConnection);
		} catch (IllegalArgumentException e) {
			// already unbound
			pdServiceProxy = null;
		}
	}
}
