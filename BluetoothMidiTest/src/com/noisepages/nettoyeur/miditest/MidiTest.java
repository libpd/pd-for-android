/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * simple test case for {@link PdService}
 * 
 */

package com.noisepages.nettoyeur.miditest;

import java.io.IOException;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.noisepages.nettoyeur.bluetooth.BluetoothSppManager;
import com.noisepages.nettoyeur.bluetooth.DeviceListActivity;
import com.noisepages.nettoyeur.bluetooth.midi.BluetoothMidiReceiver;
import com.noisepages.nettoyeur.bluetooth.midi.BluetoothMidiService;

public class MidiTest extends Activity implements OnClickListener {

	private static final String TAG = "Midi Test";

	private static final int CONNECT = 1;

	private Button connect;
	private Button play;
	private TextView logs;

	private BluetoothMidiService midiService = null;

	private void toast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), TAG + ": " + msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void post(final String s) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				logs.append(s + ((s.endsWith("\n")) ? "" : "\n"));
			}
		});
	}

	private final BluetoothMidiReceiver receiver = new BluetoothMidiReceiver() {
		@Override
		public void onDeviceConnected(BluetoothDevice device) {
			post("device connected: " + device);
		}

		@Override
		public void onConnectionLost() {
			post("connection lost");
		}

		@Override
		public void onConnectionFailed() {
			post("connection failed");
		}

		@Override
		public void onNoteOff(int channel, int key, int velocity) {
			post("note off: " + channel + ", " + key + ", " + velocity);
		}

		@Override
		public void onNoteOn(int channel, int key, int velocity) {
			post("note on: " + channel + ", " + key + ", " + velocity);
		}

		@Override
		public void onAftertouch(int channel, int velocity) {
			post("aftertouch: " + channel + ", " + velocity);
		}

		@Override
		public void onControlChange(int channel, int controller, int value) {
			post("control change: " + channel + ", " + controller + ", " + value);
		}

		@Override
		public void onPitchBend(int channel, int value) {
			post("pitch bend: " + channel + ", " + value);
		}

		@Override
		public void onPolyAftertouch(int channel, int key, int velocity) {
			post("polyphonic aftertouch: " + channel + ", " + key + ", " + velocity);
		}

		@Override
		public void onProgramChange(int channel, int program) {
			post("program change: " + channel + ", " + program);
		}
	};

	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			midiService = ((BluetoothMidiService.BluetoothMidiBinder)service).getService();
			try {
				midiService.init();
				midiService.setReceiver(receiver);
			} catch (IOException e) {
				toast("MIDI not available");
				finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// this method will never be called
		}
	};

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initGui();
		bindService(new Intent(this, BluetoothMidiService.class), connection, BIND_AUTO_CREATE);		
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cleanup();
	}

	private void initGui() {
		setContentView(R.layout.main);
		connect = (Button) findViewById(R.id.connect_button);
		connect.setOnClickListener(this);
		play = (Button) findViewById(R.id.play_button);
		play.setOnClickListener(this);
		logs = (TextView) findViewById(R.id.log_box);
		logs.setMovementMethod(new ScrollingMovementMethod());
	}

	private void cleanup() {
		try {
			unbindService(connection);
		} catch (IllegalArgumentException e) {
			// already unbound
			midiService = null;
		}
	}

	@Override
	public void finish() {
		cleanup();
		super.finish();
	}

	private int note = 60;
	private boolean on = false;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.connect_button:
			if (midiService.getState() == BluetoothSppManager.State.NONE) {
				startActivityForResult(new Intent(this, DeviceListActivity.class), CONNECT);
			} else {
				midiService.stop();
			}
			break;
		case R.id.play_button:
			try {
				if (!on) {
					midiService.sendNoteOn(0, note, 80);
				} else {
					midiService.sendNoteOff(0, note, 64);
					note++;
				}
				on = !on;
			} catch (IOException e) {
				toast(e.getMessage());
			}
		default:
			break;
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CONNECT:
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				try {
					midiService.connect(address);
				} catch (IOException e) {
					toast(e.getMessage());
				}                
			}
			break;
		}
	}
}
