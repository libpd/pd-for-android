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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.puredata.android.service.IPdClient;
import org.puredata.android.service.IPdListener;
import org.puredata.android.service.IPdService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class PdServiceTest extends Activity implements OnClickListener, OnEditorActionListener {

	private static final String PD_TEST = "Pd Test";
	private final Handler handler = new Handler();
	
	private IPdService proxy = null;
	private CheckBox left, right, mic;
	private EditText msg;

	private String folder, filename, patch;
	private int sampleRate, inChannels, outChannels, ticksPerBuffer;

	private void post(final String msg) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private final IPdClient.Stub client = new IPdClient.Stub() {
		@Override
		public void handleStop() throws RemoteException {
			post("Pure Data was stopped externally; quitting now");
			finish();
		}

		@Override
		public void handleStart(int sampleRate, int nIn, int nOut, int ticksPerBuffer) throws RemoteException {
			post("Audio parameters: sample rate: " + sampleRate + ", input channels: " + nIn + ", output channels: " + nOut + 
					", ticks per pd buffer: " + ticksPerBuffer);
		}
	};

	private IPdListener.Stub receiver = new IPdListener.Stub() {

		private void pdPost(String msg) {
			post("Pure Data says, \"" + msg + "\"");
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
		public void onServiceDisconnected(ComponentName name) {
			proxy = null;
			disconnected();
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			proxy = IPdService.Stub.asInterface(service);
			initPd();
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		bindService(new Intent("org.puredata.android.service.LAUNCH"), connection, BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initGui();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		initGui();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		cleanup();
		unbindService(connection);
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
	}
	
	private void initPd() {
		initParameters();
		try {
			proxy.addClient(client);
			proxy.subscribe("android", receiver);
			if (!proxy.objectExists(patch)) {
				proxy.sendMessage("pd", "open", Arrays.asList(new Object[] {filename, folder}));
			}
			int err = proxy.requestAudio(sampleRate, inChannels, outChannels, ticksPerBuffer);
			if (err != 0) {
				post("unable to start audio; quitting now");
				finish();
			}
		} catch (RemoteException e) {
			Log.e(PD_TEST, e.toString());
			disconnected();
		}
	}
	
	private void initParameters() {
		Resources res = getResources();
		sampleRate = res.getInteger(R.integer.sampleRate);
		inChannels = res.getInteger(R.integer.inChannels);
		outChannels = res.getInteger(R.integer.outChannels);
		ticksPerBuffer = res.getInteger(R.integer.ticksPerBuffer);
		try {
			InputStream in = res.openRawResource(R.raw.test);
			int n = in.available();
			byte[] buffer = new byte[n];
			in.read(buffer);
			in.close();
			Log.i(PD_TEST, "read file");
			filename = "_test.pd";
			patch = "pd-" + filename;
			FileOutputStream out = openFileOutput(filename, Context.MODE_PRIVATE);
			out.write(buffer);
			out.close();
			Log.i(PD_TEST, "wrote file");
			folder = getFilesDir().getAbsolutePath();
		} catch (IOException e) {
			post("unable to create test patch; quitting now");
			Log.e(PD_TEST, e.toString());
			finish();
		}
	};

	@SuppressWarnings("unchecked")
	private void cleanup() {
		boolean success = deleteFile(filename);
		Log.i(PD_TEST, success ? "deleted file" : "unable to delete file");
		if (proxy != null) {
			try {
				proxy.removeClient(client);
				proxy.sendMessage(patch, "menuclose", new ArrayList());
				proxy.unsubscribe("android", receiver);
				proxy.releaseAudio();
			} catch (RemoteException e) {
				Log.e(PD_TEST, e.toString());
				disconnected();
			}
		}
	}

	@Override
	public void onClick(View v) {
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
			default:
				break;
			}
		} catch (RemoteException e) {
			disconnected();
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		try {
			evaluateMessage(msg.getText().toString());
		} catch (RemoteException e) {
			disconnected();
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
				post("Message not sent (empty recipient)");
				return;
			}
			if (sc.hasNext()) symbol = sc.next();
			else {
				post("Message not sent (empty symbol)");
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
		post("lost connection to Pd Service; quitting now");
		finish();
	}
}
