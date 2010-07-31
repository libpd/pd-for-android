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

package org.puredata.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.puredata.android.service.IPdClient;
import org.puredata.android.service.IPdListener;
import org.puredata.android.service.IPdService;
import org.puredata.android.service.PdPreferences;
import org.puredata.android.service.PdUtils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class PdServiceTest extends Activity implements OnClickListener, OnEditorActionListener, SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String PD_TEST = "Pd Test";
	private static final int PREFS_ACTIVITY_ID = 1;
	private final Handler handler = new Handler();

	private CheckBox left, right, mic;
	private EditText msg;
	private Button prefs;
	private TextView logs;

	private IPdService proxy = null;
	private String patch = null;
	private boolean hasAudio = false;

	private void toast(final String msg) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), PD_TEST + ": " + msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void post(final String s) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				logs.append(s + ((s.endsWith("\n")) ? "" : "\n"));
			}
		});
	}

	private final IPdClient.Stub client = new IPdClient.Stub() {
		
		@Override
		public void requestUnbind() throws RemoteException {
			toast("Pure Data was stopped externally; finishing now");
			finishActivity(PREFS_ACTIVITY_ID);  // finish preferences activity, if any
			finish();			
		};

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
		public void print(final String s) throws RemoteException {
			post(s);
		}
	};

	private IPdListener.Stub receiver = new IPdListener.Stub() {

		private void pdPost(String msg) {
			toast("Pure Data says, \"" + msg + "\"");
		}

		@Override
		public void receiveBang() throws RemoteException {
			pdPost("bang!");
		}

		@Override
		public void receiveFloat(float x) throws RemoteException {
			pdPost("float: " + x);
		}

		@Override
		public void receiveSymbol(String symbol) throws RemoteException {
			pdPost("symbol: " + symbol);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void receiveList(List args) throws RemoteException {
			pdPost("list: " + args.toString());
		}

		@SuppressWarnings("unchecked")
		@Override
		public void receiveMessage(String symbol, List args)
		throws RemoteException {
			pdPost("symbol: " + symbol + ", args: " + args.toString());
		}
	};

	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public synchronized void onServiceDisconnected(ComponentName name) {
			proxy = null;
			disconnected();
		}

		@Override
		public synchronized void onServiceConnected(ComponentName name, IBinder service) {
			proxy = IPdService.Stub.asInterface(service);
			initPd();
		}
	};

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PdPreferences.initPreferences(getApplicationContext());
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
		initGui();
		bindService(new Intent(PdUtils.LAUNCH_ACTION), connection, BIND_AUTO_CREATE);		
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cleanup();
		unbindService(connection);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		try {
			restartAudio();
		} catch (RemoteException e) {
			Log.e(PD_TEST, e.toString());
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		boolean bl = left.isChecked();
		boolean br = right.isChecked();
		boolean bm = mic.isChecked();
		CharSequence msgc = msg.getText();
		CharSequence logsc = logs.getText();
		initGui();
		left.setChecked(bl);
		right.setChecked(br);
		mic.setChecked(bm);
		msg.setText(msgc);
		logs.setText(logsc);
	}

	private void initGui() {
		setContentView(R.layout.main);
		left = (CheckBox) findViewById(R.id.left_box);
		left.setOnClickListener(this);
		right = (CheckBox) findViewById(R.id.right_box);
		right.setOnClickListener(this);
		mic = (CheckBox) findViewById(R.id.mic_box);
		mic.setOnClickListener(this);
		msg = (EditText) findViewById(R.id.msg_box);
		msg.setOnEditorActionListener(this);
		prefs = (Button) findViewById(R.id.pref_button);
		prefs.setOnClickListener(this);
		logs = (TextView) findViewById(R.id.log_box);
		logs.setMovementMethod(new ScrollingMovementMethod());
	}

	private void initPd() {
		Resources res = getResources();
		File patchFile = null;
		try {
			proxy.addClient(client);
			proxy.subscribe("android", receiver);
			InputStream in = res.openRawResource(R.raw.test);
			patchFile = PdUtils.extractResource(in, ".pd", getCacheDir());
			patch = PdUtils.openPatch(proxy, patchFile);
			restartAudio();
		} catch (RemoteException e) {
			Log.e(PD_TEST, e.toString());
			disconnected();
		} catch (IOException e) {
			Log.e(PD_TEST, e.toString());
			finish();
		} finally {
			if (patchFile != null) patchFile.delete();
		}
	}

	private void restartAudio() throws RemoteException {
		synchronized (connection) {
			if (proxy == null) return;
			if (hasAudio) {
				hasAudio = false;
				proxy.releaseAudio();
			}
			if (proxy.isRunning()) {
				toast("Warning: audio is already running; cannot change parameters");
			}
			int err = proxy.requestAudio(-1, -1, -1, -1);  // negative values stand for defaults/preferences
			hasAudio = err == 0;
			if (!hasAudio) {
				toast("didn't get audio; check preferences");
			}
		}
	}

	private void cleanup() {
		synchronized (connection) {	
			if (proxy == null) return;
			try {
				proxy.removeClient(client);
				if (patch != null) PdUtils.closePatch(proxy, patch);
				proxy.unsubscribe("android", receiver);
				if (hasAudio) {
					hasAudio = false;
					proxy.releaseAudio();
				}
			} catch (RemoteException e) {
				Log.e(PD_TEST, e.toString());
				disconnected();
			}
		}
	}

	@Override
	public void onClick(View v) {
		synchronized (connection) {
			if (proxy == null) return;
			try {
				switch (v.getId()) {
				case R.id.left_box:
					proxy.sendFloat("left", left.isChecked() ? 1 : 0);
					break;
				case R.id.right_box:
					proxy.sendFloat("right", right.isChecked() ? 1 : 0);
					break;
				case R.id.mic_box:
					proxy.sendFloat("mic", mic.isChecked() ? 1 : 0);
					break;
				case R.id.pref_button:
					startActivityForResult(new Intent(this, PdPreferences.class), PREFS_ACTIVITY_ID);
					// we don't really want a result from PdPreferences, but we do want to be able to finish it with finishActivity(PREFS_ACTIVITY_ID)
					break;
				default:
					break;
				}
			} catch (RemoteException e) {
				disconnected();
			}
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		synchronized (connection) {	
			try {
				if (proxy != null) evaluateMessage(msg.getText().toString());
			} catch (RemoteException e) {
				disconnected();
			}
		}
		return true;
	}

	private void evaluateMessage(String s) throws RemoteException {
		String dest = "test", symbol = null;
		boolean isAny = s.length() > 0 && s.charAt(0) == ';';
		Scanner sc = new Scanner(isAny ? s.substring(1) : s);
		if (isAny) {
			if (sc.hasNext()) dest = sc.next();
			else {
				toast("Message not sent (empty recipient)");
				return;
			}
			if (sc.hasNext()) symbol = sc.next();
			else {
				toast("Message not sent (empty symbol)");
			}
		}
		List<Object> list = new ArrayList<Object>();
		while (sc.hasNext()) {
			if (sc.hasNextInt()) {
				list.add(new Float(sc.nextInt()));
			} else if (sc.hasNextFloat()) {
				list.add(sc.nextFloat());
			} else {
				list.add(sc.next());
			}
		}
		if (isAny) {
			proxy.sendMessage(dest, symbol, list);
		} else {
			switch (list.size()) {
			case 0:
				proxy.sendBang(dest);
				break;
			case 1:
				Object x = list.get(0);
				if (x instanceof String) {
					proxy.sendSymbol(dest, (String) x);
				} else {
					proxy.sendFloat(dest, (Float) x);
				}
				break;
			default:
				proxy.sendList(dest, list);
				break;
			}
		}
	}

	private void disconnected() {
		toast("lost connection to Pd Service; finishing now");
		finish();
	}
}
