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

import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;
import org.puredata.core.utils.PdDispatcher;
import org.puredata.core.utils.PdListener;
import org.puredata.core.utils.PdUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class ScenePlayer extends Activity implements SensorEventListener, OnTouchListener, OnClickListener, OnSeekBarChangeListener {

	public static final String SCENE = "SCENE";
	public static final String RECDIR = "RECDIR";
	public static final String RECTAG = "RECTAG"; // key for recording tag, e.g., for geolocation
	public static final String RECSEP = "___";
	private static final String TITLE = "name";
	private static final String AUTHOR = "author";
	private static final String TAG = "Pd Scene Player";
	private static final String RJ_IMAGE_ANDROID = "rj_image_android";
	private static final String RJ_TEXT_ANDROID = "rj_text_android";
	private static final String TRANSPORT = "#transport";
	private static final String ACCELERATE = "#accelerate";
	private static final String MICVOLUME = "#micvolume";
	private static final int SAMPLE_RATE = 22050;
	private final Object lock = new Object();
	private ProgressDialog progress = null;
	private SceneView sceneView;
	private ToggleButton play;
	private ToggleButton record;
	private ImageButton info;
	private SeekBar micVolume;
	private File sceneFolder;
	private File recDir = null;
	private PdService pdService = null;
	private String patch = null;
	private String recTag = null;
	private final Map<String, String> sceneInfo = new HashMap<String, String>();
	private final PdDispatcher dispatcher = new PdDispatcher() {
		@Override
		public void print(String s) {
			post(s);
		}
	};

	private void post(final String msg) {
		Log.i(TAG, msg);
	}

	private void toast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), TAG + ": " + msg, Toast.LENGTH_LONG).show();
			}
		});
	}

	private final PdListener overlayListener = new PdListener.Adapter() {

		private final Map<String, Overlay> overlays = new HashMap<String, Overlay>();

		@Override
		public void receiveList(Object... args) {
			String key = (String) args[0];
			String cmd = (String) args[1];
			if (overlays.containsKey(key)) {
				Overlay overlay = overlays.get(key);
				if (cmd.equals("visible")) {
					boolean flag = ((Float) args[2]).floatValue() > 0.5f;
					overlay.setVisible(flag);
				} else if (cmd.equals("move")) {
					float x = ((Float) args[2]).floatValue();
					float y = ((Float) args[3]).floatValue();
					overlay.setPosition(x, y);
				} else {
					if (overlay instanceof TextOverlay) {
						TextOverlay textOverlay = (TextOverlay) overlay;
						if (cmd.equals("text")) {
							textOverlay.setText((String) args[2]);
						} else if (cmd.equals("size")) {
							textOverlay.setSize(((Float) args[2]).floatValue());
						}
					} else {
						ImageOverlay imgOverlay = (ImageOverlay) overlay;
						float val = ((Float) args[2]).floatValue();
						if (cmd.equals("ref")) {
							boolean flag = val > 0.5f;
							imgOverlay.setCentered(flag);
						} else if (cmd.equals("scale")) {
							float sy = ((Float) args[3]).floatValue();
							imgOverlay.setScale(val, sy);
						} else if (cmd.equals("rotate")) {
							imgOverlay.setAngle(val);
						} else if (cmd.equals("alpha")) {
							imgOverlay.setAlpha(val);
						}
					}
				}
			} else {
				String arg = (String) args[2];
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
	};

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			synchronized(lock) {
				pdService = ((PdService.PdBinder)service).getService();
				initPd();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// this method will never be called
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String scenePath = intent.getStringExtra(SCENE);
		String recDirName = intent.getStringExtra(RECDIR);
		recTag = intent.getStringExtra(RECTAG);
		if (scenePath != null && recDirName != null) {
			progress = new ProgressDialog(this);
			progress.setCancelable(false);
			progress.setIndeterminate(true);
			progress.setMessage("Loading scene...");
			progress.show();
			sceneFolder = new File(scenePath);
			recDir = new File(recDirName);
			if (recDir.isFile() || (!recDir.exists() && !recDir.mkdirs())) recDir = null;
			initGui();
			initSystemServices();
			initPdService();
		} else {
			Log.e(TAG, "launch intent without scene path");
			finish();
		}
	}

	private void initPdService() {
		new Thread() {
			@Override
			public void run() {
				fixScene();
				bindService(new Intent(ScenePlayer.this, PdService.class), serviceConnection, BIND_AUTO_CREATE);
			}
		}.start();
	}

	private void initSystemServices() {
		SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				synchronized (lock) {
					if (pdService == null) return;
					if (state == TelephonyManager.CALL_STATE_IDLE) {
						if (play.isChecked() && !pdService.isRunning()) {
							startAudio();
						}
					} else {
						if (pdService.isRunning()) {
							stopAudio();
						}
					}
				}
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void fixScene() {
		// weird little hack to avoid having our rj_image.pd and such masked by files in the scene
		for (String s: new String[] {"rj_image.pd", "rj_text.pd", "soundinput.pd", "soundoutput.pd"}) {
			List<File> list = IoUtils.find(sceneFolder, s);
			for (File file: list) file.delete();
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		final float q = 1.0f / SensorManager.GRAVITY_EARTH;  // convert acceleration units from m/s^2 to g
		PdBase.sendList(ACCELERATE, event.values[0] * q, -event.values[1] * q, -event.values[2] * q);
		/**
		 * Explanation:  Observation of RjDj patches suggests that the z-axis points
		 * downward on iPhones.  Since I'm pretty sure that the coordinate system is
		 * supposed to be right-handed and that the x-axis points right, I've concluded
		 * that the way to convert between Android and iPhone accelerometer values is to
		 * flip the sign of the y and z coordinates.
		 */
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// don't care
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return (v == sceneView) && VersionedTouch.evaluateTouch(event, sceneView.getWidth(), sceneView.getHeight());
	}

	@Override
	protected void onPause() {
		dismissProgressDialog();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cleanup();
	}

	private void initGui() {
		readInfo();
		setContentView(R.layout.scene_player);
		TextView tv = (TextView) findViewById(R.id.sceneplayer_title);
		tv.setText(sceneInfo.get(TITLE));
		tv = (TextView) findViewById(R.id.sceneplayer_artist);
		tv.setText(sceneInfo.get(AUTHOR));
		sceneView = (SceneView) findViewById(R.id.sceneplayer_pic);
		sceneView.setOnTouchListener(this);
		sceneView.setImageBitmap(BitmapFactory.decodeFile(new File(sceneFolder, "image.jpg").getAbsolutePath()));
		play = (ToggleButton) findViewById(R.id.sceneplayer_pause);
		play.setOnClickListener(this);
		record = (ToggleButton) findViewById(R.id.sceneplayer_record);
		record.setOnClickListener(this);
		info = (ImageButton) findViewById(R.id.sceneplayer_info);
		info.setOnClickListener(this);
		micVolume = (SeekBar) findViewById(R.id.mic_volume);
		micVolume.setOnSeekBarChangeListener(this);
	}

	private void initPd() {
		// run in a separate thread in order to keep the progress wheel spinning
		new Thread() {
			@Override
			public void run() {
				PdBase.setReceiver(dispatcher);
				dispatcher.addListener(RJ_IMAGE_ANDROID, overlayListener);
				dispatcher.addListener(RJ_TEXT_ANDROID, overlayListener);
				startAudio();
				dismissProgressDialog();
			}
		}.start();
	}

	private void dismissProgressDialog() {
		if (progress!= null) progress.dismiss();
	}

	@Override
	public void finish() {
		cleanup();
		super.finish();
	}

	private void cleanup() {
		synchronized (lock) {
			// make sure to release all resources
			stopRecording();
			stopAudio();
			if (patch != null) {
				PdUtils.closePatch(patch);
				patch = null;
			}
			dispatcher.release();
			PdBase.release();
			try {
				unbindService(serviceConnection);
			} catch (IllegalArgumentException e) {
				// already unbound
				pdService = null;
			}
		}
	}

	@Override
	public void onClick(View v) {
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
	}

	private void startRecording() {
		if (recDir == null) {
			record.setChecked(false);
			return;
		}
		String fileName = sceneFolder.getName();
		fileName = fileName.substring(0, fileName.length()-3) + RECSEP + System.currentTimeMillis();
		if (recTag != null) fileName += RECSEP + recTag;
		String path = new File(recDir, fileName + ".wav").getAbsolutePath();
		PdBase.sendMessage(TRANSPORT, "scene", path);
		PdBase.sendMessage(TRANSPORT, "record", 1);
		post("recording to " + path);
	}

	private void stopRecording() {
		if (recDir == null) return;
		PdBase.sendMessage(TRANSPORT, "record", 0);
		post("finished recording");
	}

	private boolean initAudio(int nIn, int nOut) {
		try {
			pdService.initAudio(SAMPLE_RATE, nIn, nOut, -1);   // negative values default to PdService preferences
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		}
		return true;
	}

	private void startAudio() {
		synchronized (lock) {
			if (pdService == null) return;
			if (!initAudio(2, 2) && !initAudio(1, 2)) {
				if (!initAudio(0, 2)) {
					toast("Unable to initialize audio interface");
					finish();
					return;
				} else {
					toast("Warning: No audio input available");
				}
			}
			if (patch == null) {
				try {
					patch = PdUtils.openPatch(new File(sceneFolder, "_main.pd"));
				} catch (IOException e) {
					Log.e(TAG, e.toString());
					toast("Unable to open patch; exiting");
					finish();
					return;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
			pdService.startAudio(new Intent(this, ScenePlayer.class), R.drawable.notification_icon,
					sceneInfo.get(TITLE) + " by " + sceneInfo.get(AUTHOR), "Return to scene.");
			PdBase.sendMessage(TRANSPORT, "play", 1);
		}
	}

	private void stopAudio() {
		synchronized (lock) {
			if (pdService == null) return;
			PdBase.sendMessage(TRANSPORT, "play", 0);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// do nothing
			}
			pdService.stopAudio();
		}
	}

	private void showInfo() {
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		if (sceneInfo.isEmpty()) {
			ad.setTitle("Oops");
			ad.setMessage("Info not available...");
		} else {
			View header = View.inflate(this, R.layout.two_line_dialog_title, null);
			((TextView) header.findViewById(android.R.id.text1)).setText(sceneInfo.get(TITLE));
			((TextView) header.findViewById(android.R.id.text2)).setText(sceneInfo.get(AUTHOR));
			File pic = new File(sceneFolder, "thumb.jpg");
			if (!pic.exists()) pic = new File(sceneFolder, "image.jpg");
			if (pic.exists()) {
				ImageView sceneThumbnail = (ImageView) header.findViewById(android.R.id.selectedIcon);
				sceneThumbnail.setImageDrawable(Drawable.createFromPath(pic.getAbsolutePath()));
			}
			ad.setCustomTitle(header);
			ad.setMessage(sceneInfo.get("description"));
		}
		ad.setNeutralButton(android.R.string.ok, null);
		ad.setCancelable(true);
		ad.show();
	}

	private void readInfo() {
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
						sceneInfo.put(key, val);
					}
				}
			});
		} catch (Exception e) {
			sceneInfo.clear();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		float q = progress * 0.01f;
		float volume = q * q * q * q;  // fourth power of mic volume slider value; somewhere between linear and exponential
		PdBase.sendFloat(MICVOLUME, volume);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// don't care
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// don't care
	}
}
