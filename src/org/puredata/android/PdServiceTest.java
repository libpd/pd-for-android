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

import java.util.Arrays;

import org.puredata.android.service.PdService;
import org.puredata.android.service.PdServiceHub;
import org.puredata.core.PdReceiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class PdServiceTest extends Activity {

	private static final String PD_TEST = "Pd Test";
	private String patch = null;

	private final PdReceiver receiver = new PdReceiver() {

		@Override
		public void receiveSymbol(String source, String symbol) {
			Log.i(PD_TEST, "source: " + source + ", symbol: " + symbol);
		}

		@Override
		public void receiveMessage(String source, String symbol, Object[] args) {
			Log.i(PD_TEST, "source: " + source + ", symbol: " + symbol + ", args: " + Arrays.toString(args));
		}

		@Override
		public void receiveList(String source, Object[] args) {
			Log.i(PD_TEST, "source: " + source + ", args: " + Arrays.toString(args));

		}

		@Override
		public void receiveFloat(String source, float x) {
			Log.i(PD_TEST, "source: " + source + ", float: " + x);
		}

		@Override
		public void receiveBang(String source) {
			Log.i(PD_TEST, "source: " + source + ", bang!");
		}

		@Override
		public void print(String s) {
			// will not be called
		}
	};

	private PdServiceHub.ServiceProxy proxy = null;
	
	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			proxy = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			proxy = new PdServiceHub.ServiceProxy(new Messenger(service));
			try {
				proxy.subscribe("spam", receiver);
				proxy.subscribe("eggs", receiver);
				Resources res = getResources();
				String p = res.getString(R.string.patch);
				proxy.sendAny("pd", "open", new Object[] {p, res.getString(R.string.folder)});
				patch = "pd-" + p;
				proxy.startAudio(res.getInteger(R.integer.sampleRate),
						res.getInteger(R.integer.inChannels), res.getInteger(R.integer.outChannels),
						res.getInteger(R.integer.ticksPerBuffer), false);
				proxy.sendBang("foo");
				proxy.sendFloat("foo", 12345);
				proxy.sendSymbol("bar", "elephant");
				proxy.sendList("bar", new Object[] { new Integer(5), "katze", new Float(1.414) });
				proxy.sendAny("bar", "boing", new Object[] { new Integer(5), "katze", new Float(1.414) });
			} catch (RemoteException e) {
				Log.e(PD_TEST, e.toString());
			}
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		bindService(new Intent(this, PdService.class), connection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (proxy != null) {
			try {
				proxy.sendAny(patch, "menuclose");
				proxy.stopAudio();
			} catch (RemoteException e) {
				Log.e(PD_TEST, e.toString());
			}
		}
		unbindService(connection);
	}
}
