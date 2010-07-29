package foo.bar;

import java.io.IOException;

import org.puredata.android.service.IPdClient;
import org.puredata.android.service.IPdService;
import org.puredata.android.service.PdUtils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;


public class PdClient extends Activity {

	private static final String PD_CLIENT = "Pd Client";
	private final Handler handler = new Handler();
	private IPdService pdServiceProxy = null;
	private boolean hasAudio = false;
	private String patch;  // the path to the patch is defined in res/values/strings.xml

	private void post(final String msg) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), PD_CLIENT + ": " + msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private final IPdClient.Stub statusWatcher = new IPdClient.Stub() {
		@Override
		public void handleStop() throws RemoteException {
			hasAudio = false;
			post("Pure Data was stopped externally; exiting now");
			finish();
		}

		@Override
		public void handleStart(int sampleRate, int nIn, int nOut, float bufferSizeMillis) throws RemoteException {
			post("Audio parameters: sample rate: " + sampleRate + ", input channels: " + nIn + ", output channels: " + nOut + 
					", buffer size: " + bufferSizeMillis + "ms");
		}

		@Override
		public void print(String s) throws RemoteException {
			Log.i(PD_CLIENT, s);
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
		unbindService(serviceConnection);
	}

	private void initGui() {
		setContentView(R.layout.main);
	}

	private void initPd() {
		Resources res = getResources();
		String path = res.getString(R.string.path_to_patch);
		try {
			pdServiceProxy.addClient(statusWatcher);
			patch = PdUtils.openPatch(pdServiceProxy, path);
			int err = pdServiceProxy.requestAudio(-1, 1, 2, -1); // negative values default to PdService preferences
			hasAudio = (err == 0);
			if (!hasAudio) {
				post("unable to start audio; exiting now");
				finish();
			}
		} catch (RemoteException e) {
			Log.e(PD_CLIENT, e.toString());
			disconnected();
		} catch (IOException e) {
			post(e.toString() + "; exiting now");
			finish();
		}
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
				Log.e(PD_CLIENT, e.toString());
				disconnected();
			}
		}
	}

	private void disconnected() {
		post("lost connection to Pd Service; exiting now");
		finish();
	}
}