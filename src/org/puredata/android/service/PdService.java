/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * service running pd in the background
 * 
 */

package org.puredata.android.service;

import java.io.IOException;

import org.puredata.android.R;
import org.puredata.android.io.PdAudioThread;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.PdDispatcher;
import org.puredata.core.utils.PdListener;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.util.Log;


public class PdService extends Service {

	private WakeLock wakeLock = null;
	
	private static final String PD_SERVICE = "Pd Service";
	
	public static final int START_AUDIO = 1;
	public static final int STOP_AUDIO = 2;
	public static final int SUBSCRIBE = 3;
	public static final int UNSUBSCRIBE = 4;
	public static final int SEND_MESSAGE = 5;
	
	private final PdDispatcher dispatcher = new PdDispatcher() {
		@Override
		public void print(String s) {
			Log.i(PD_SERVICE, s);
		}
	};
	
	private class MsgListener implements PdListener {

		private final String source;
		private final Messenger receiver;
		
		MsgListener(String symbol, Messenger receiver) {
			this.source = symbol;
			this.receiver = receiver;
		}
		
		private void remove() {
			Log.e(PD_SERVICE, "removing dead receiver " + receiver + ", symbol " + source);
			dispatcher.removeListener(source, this);
		}
		
		@Override
		public void receiveBang() {
			try {
				receiver.send(PdMessage.bangMessage(source));
			} catch (RemoteException e) {
				remove();
			}
		}

		@Override
		public void receiveFloat(float x) {
			try {
				receiver.send(PdMessage.floatMessage(source, x));
			} catch (RemoteException e) {
				remove();
			}
		}

		@Override
		public void receiveList(Object[] args) {
			try {
				receiver.send(PdMessage.listMessage(source, args));
			} catch (RemoteException e) {
				remove();
			}
		}

		@Override
		public void receiveMessage(String symbol, Object[] args) {
			try {
				receiver.send(PdMessage.anyMessage(this.source, symbol, args));
			} catch (RemoteException e) {
				remove();
			}
		}

		@Override
		public void receiveSymbol(String symbol) {
			try {
				receiver.send(PdMessage.symbolMessage(source, symbol));
			} catch (RemoteException e) {
				remove();
			}
		}
	}
	
	private final PdReceiver sender = new PdReceiver() {
		@Override
		public void print(String s) {
			// do nothing
		}

		@Override
		public void receiveBang(String source) {
			PdBase.sendBang(source);
		}

		@Override
		public void receiveFloat(String source, float x) {
			PdBase.sendFloat(source, x);
		}

		@Override
		public void receiveList(String source, Object[] args) {
			PdBase.sendList(source, args);
		}

		@Override
		public void receiveMessage(String source, String symbol, Object[] args) {
			PdBase.sendMessage(source, symbol, args);
		}

		@Override
		public void receiveSymbol(String source, String symbol) {
			PdBase.sendSymbol(source, symbol);
		}	
	};
	
	private final Messenger receiver = new Messenger(new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case START_AUDIO:
				PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
				wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "pd wakelock");
				wakeLock.acquire();
				Resources res = getResources();
				try {
					PdAudioThread.startThread(res.getInteger(R.integer.sampleRate), res.getInteger(R.integer.inChannels), 
							res.getInteger(R.integer.outChannels), res.getInteger(R.integer.ticksPerBuffer), false);
				} catch (IOException e) {
					Log.e(PD_SERVICE, e.toString());
				}
				break;
			case STOP_AUDIO:
				PdAudioThread.stopThread();
				if (wakeLock != null) {
					wakeLock.release();
				}
				stopSelf();
				break;
			case SUBSCRIBE:
				Messenger m = msg.replyTo;
				String sym = (String) msg.obj;
				dispatcher.addListener(sym, new MsgListener(sym, m));
				break;
			case UNSUBSCRIBE:
				// TODO: implement this
				break;
			case SEND_MESSAGE:
				PdMessage.evaluateMessage(msg, sender);
				break;
			default:
				break;
			}
		};
	});
	
	@Override
	public void onCreate() {
		super.onCreate();
		PdBase.setReceiver(dispatcher);
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return receiver.getBinder();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		PdAudioThread.stopThread();
		dispatcher.release();
		PdBase.release();
	}
}
