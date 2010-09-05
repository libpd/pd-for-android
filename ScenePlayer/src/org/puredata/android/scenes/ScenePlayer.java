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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.puredata.android.service.IPdClient;
import org.puredata.android.service.IPdListener;
import org.puredata.android.service.IPdService;
import org.puredata.android.service.PdUtils;
import org.puredata.core.utils.IoUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;


public class ScenePlayer extends Activity implements SensorEventListener, OnTouchListener, OnClickListener {

	public static final String SCENE = "SCENE";
	private static final String TAG = "Pd Scene Player";
	private static final String RJ_IMAGE_ANDROID = "rj_image_android";
	private static final String RJ_TEXT_ANDROID = "rj_text_android";
	private static final String TRANSPORT = "#transport";
	private static final String ACCELERATE = "#accelerate";
	private final Handler handler = new Handler();
	private SceneView sceneView;
	private TextView logs;
	private ToggleButton play;
	private ToggleButton record;
	private Button info;
	private File sceneFolder;
	private IPdService pdServiceProxy = null;
	private boolean hasAudio = false;
	private String patch;
	private final File libDir = new File("/sdcard/pd/.scenes");
	private final File recDir = new File("/sdcard/pd");
	private final Map<String, String> infoEntries = new HashMap<String, String>();

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

	private final IPdListener.Stub overlayListener = new IPdListener.Stub() {

		private final Map<String, Overlay> overlays = new HashMap<String, Overlay>();

		@SuppressWarnings("unchecked")
		@Override
		public synchronized void receiveList(List args) throws RemoteException {
			String key = (String) args.get(0);
			String cmd = (String) args.get(1);
			if (overlays.containsKey(key)) {
				Overlay overlay = overlays.get(key);
				if (cmd.equals("visible")) {
					boolean flag = ((Float) args.get(2)).floatValue() > 0.5f;
					overlay.setVisible(flag);
				} else if (cmd.equals("move")) {
					float x = ((Float) args.get(2)).floatValue();
					float y = ((Float) args.get(3)).floatValue();
					overlay.setPosition(x, y);
				} else {
					if (!(overlay instanceof TextOverlay)) return;
					TextOverlay textOverlay = (TextOverlay) overlay;
					if (cmd.equals("text")) {
						textOverlay.setText((String) args.get(2));
					} else if (cmd.equals("size")) {
						textOverlay.setSize(((Float) args.get(2)).floatValue());
					}
				}
			} else {
				String arg = (String) args.get(2);
				Overlay overlay;
				if (cmd.equals("load")) {
					overlay = new ImageOverlay(new File(sceneFolder, arg).getAbsolutePath());
				} else if (cmd.equals("text")) {
					overlay = new TextOverlay(arg);
				} else return;
				sceneView.addOverlay(overlay);
				overlays.put(key, overlay);
			}
		}

		// the remaining methods will never be called
		@SuppressWarnings("unchecked")
		@Override public void receiveMessage(String symbol, List args) throws RemoteException {}
		@Override public void receiveSymbol(String symbol) throws RemoteException {}
		@Override public void receiveFloat(float x) throws RemoteException {}
		@Override public void receiveBang() throws RemoteException {}
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
			sceneFolder = new File(path);
			fixScene();
			initGui();
			try {
				Resources res = getResources();
				IoUtils.extractZipResource(res.openRawResource(R.raw.abstractions), libDir);
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
			bindService(new Intent(PdUtils.LAUNCH_ACTION), serviceConnection, BIND_AUTO_CREATE);
			SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
			sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
		} else {
			Log.e(TAG, "launch intent without scene path");
			finish();
		}
	}

	private void fixScene() {
		// weird little hack to avoid having our rj_image.pd and such masked by files in the scene
		for (String s: new String[] {"rj_image", "rj_text", "soundinput", "soundoutput"}) {
			new File(sceneFolder, s + ".pd").delete();
			new File(sceneFolder, "rj/" + s + ".pd").delete();
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (serviceConnection) {
			if (pdServiceProxy != null) {
				try {
					PdUtils.sendList(pdServiceProxy, ACCELERATE, event.values[0], event.values[1], event.values[2]);
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
		if (v == sceneView) {
			synchronized (serviceConnection) {
				if (pdServiceProxy != null) {
					try {
						flag = VersionedTouch.evaluateTouch(pdServiceProxy, event, sceneView.getWidth(), sceneView.getHeight());
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
		tv.setText(sceneFolder.getName());
		sceneView = (SceneView) findViewById(R.id.scene_pic);
		sceneView.setOnTouchListener(this);
		sceneView.setImageBitmap(BitmapFactory.decodeFile(new File(sceneFolder, "image.jpg").getAbsolutePath()));
		play = (ToggleButton) findViewById(R.id.scene_pause);
		play.setOnClickListener(this);
		record = (ToggleButton) findViewById(R.id.scene_record);
		record.setOnClickListener(this);
		info = (Button) findViewById(R.id.scene_info);
		info.setOnClickListener(this);
		logs = (TextView) findViewById(R.id.scene_logs);
		logs.setMovementMethod(new ScrollingMovementMethod());
	}

	private void initPd() {
		try {
			pdServiceProxy.addClient(statusWatcher);
			pdServiceProxy.installExternals(IoUtils.hasArmeabiV7a() ? "content://org.puredata.android.scenes/res/raw/externals_v7a.zip" :
																		"content://org.puredata.android.scenes/res/raw/externals.zip");
			pdServiceProxy.addToSearchPath(libDir.getAbsolutePath());
			pdServiceProxy.subscribe(RJ_IMAGE_ANDROID, overlayListener);
			pdServiceProxy.subscribe(RJ_TEXT_ANDROID, overlayListener);
			patch = PdUtils.openPatch(pdServiceProxy, new File(sceneFolder, "_main.pd"));
			startAudio();
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
				stopRecording();
				pdServiceProxy.removeClient(statusWatcher);
				pdServiceProxy.unsubscribe(RJ_IMAGE_ANDROID, overlayListener);
				pdServiceProxy.unsubscribe(RJ_TEXT_ANDROID, overlayListener);
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

	@Override
	public void onClick(View v) {
		synchronized (serviceConnection) {
			if (pdServiceProxy == null) return;
			try {
				if (v.equals(play)) {
					if (play.isChecked()) {
						startAudio();
					} else {
						stopAudio();
					}
				} else if (v.equals(record)) {
					if (record.isChecked()) {
						startRecording();
					} else {
						stopRecording();
					}
				} else if (v.equals(info)) {
					showInfo();
				}
			} catch (RemoteException e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	private void startRecording() throws RemoteException {
		String name = sceneFolder.getName();
		String filename = new File(recDir, name.substring(0, name.length()-3) + "-" + System.currentTimeMillis() + ".wav").getAbsolutePath();
		PdUtils.sendMessage(pdServiceProxy, TRANSPORT, "scene", filename);
		post("recording to " + filename);
		PdUtils.sendMessage(pdServiceProxy, TRANSPORT, "record", 1);
	}

	private void stopRecording() throws RemoteException {
		PdUtils.sendMessage(pdServiceProxy, TRANSPORT, "record", 0);
		post("finished recording");
	}

	private void startAudio() throws RemoteException {
		PdUtils.sendMessage(pdServiceProxy, TRANSPORT, "play", 1);
		int err = pdServiceProxy.requestAudio(22050, 1, 2, -1); // negative values default to PdService preferences
		hasAudio = (err == 0);
		if (!hasAudio) {
			post("unable to start audio; exiting now");
			finish();
		}
	}
	
	private void stopAudio() throws RemoteException {
		PdUtils.sendMessage(pdServiceProxy, TRANSPORT, "play", 0);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// do nothing
		}
		hasAudio = false;
		pdServiceProxy.releaseAudio();
	}

	private void showInfo() {
		readInfo();
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		if (infoEntries.isEmpty()) {
			ad.setTitle("Oops");
			ad.setMessage("Info not available...");
		} else {
			ad.setTitle("" + infoEntries.get("name") + "\n" + infoEntries.get("author"));
			ad.setMessage("" + infoEntries.get("description") + "\n\nCategory: " + infoEntries.get("category"));
		}
		ad.setNeutralButton(android.R.string.ok, null);
		ad.setCancelable(true);
		ad.show();
	}

	private void readInfo() {
		if (!infoEntries.isEmpty()) return;
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			sp.parse(new File(sceneFolder, "Info.plist"), new DefaultHandler() {
				private String key = "", val = "";
				private boolean expectKey;
				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes) throws SAXException {
					expectKey = localName.equalsIgnoreCase("key");
					if (expectKey) {
						key = val = "";
					}
				}

				@Override
				public void characters(char[] ch, int start, int length) throws SAXException {
					String s = new String(ch, start, length);
					if (expectKey) {
						key += s;
					} else {
						val += s;
					}
				}

				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException {
					key = key.trim();
					val = val.trim();
					if (key.length() > 0) {
						infoEntries.put(key, val);
					}
				}
			});
		} catch (Exception e) {
			infoEntries.clear();
		}
	}
}
