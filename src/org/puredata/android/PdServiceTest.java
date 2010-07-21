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

import org.puredata.android.service.PdMessage;
import org.puredata.android.service.PdService;
import org.puredata.core.PdReceiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class PdServiceTest extends Activity {

	private static final String PD_TEST = "Pd Test";
	private String patch = null;

	private final PdReceiver evaluator = new PdReceiver() {

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
			// will not be called...
		}
	};

	private Messenger sender = null;

	private final Messenger receiver = new Messenger(new Handler() {
		public void handleMessage(android.os.Message msg) {
			PdMessage.evaluateMessage(msg, evaluator);
		};
	});

	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			sender = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			sender = new Messenger(service);
			try {
				Message message = Message.obtain(null, PdService.SUBSCRIBE);
				message.obj = "spam";
				message.replyTo = receiver;
				sender.send(message);
				message = Message.obtain(null, PdService.SUBSCRIBE);
				message.obj = "eggs";
				message.replyTo = receiver;
				sender.send(message);
				Resources res = getResources();
				String p = res.getString(R.string.patch);
				message = PdMessage.anyMessage("pd", "open", new Object[] {p, res.getString(R.string.folder)});
				patch = "pd-" + p;
				sender.send(message);
				message = Message.obtain(null, PdService.START_AUDIO);
				sender.send(message);
				message = PdMessage.bangMessage("foo");
				sender.send(message);
				message = PdMessage.floatMessage("foo", 12345);
				sender.send(message);
				message = PdMessage.symbolMessage("bar", "elephant");
				sender.send(message);
				message = PdMessage.listMessage("bar", new Object[] { new Integer(5), "katze", new Float(1.414) });
				sender.send(message);
				message = PdMessage.anyMessage("bar", "boing", new Object[] { new Integer(5), "katze", new Float(1.414) });
				sender.send(message);
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
		if (sender != null) {
			try {
				Message message;
				message = PdMessage.anyMessage(patch, "menuclose");
				sender.send(message);
				message = Message.obtain(null, PdService.STOP_AUDIO);
				sender.send(message);
			} catch (RemoteException e) {
				Log.e(PD_TEST, e.toString());
			}
		}
		unbindService(connection);
	}
}
