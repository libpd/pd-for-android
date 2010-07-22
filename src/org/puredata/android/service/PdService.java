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
import java.util.Arrays;
import java.util.List;

import org.puredata.android.io.PdAudioThread;
import org.puredata.core.PdBase;
import org.puredata.core.utils.PdDispatcher;
import org.puredata.core.utils.PdListener;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.util.Log;


public class PdService extends Service {

	private static final String PD_SERVICE = "Pd Service";
	
	private WakeLock wakeLock = null;

	private final PdDispatcher dispatcher = new PdDispatcher() {
		@Override
		public void print(String s) {
			Log.i(PD_SERVICE, s);
		}
	};
	
	private static class ListenerClient implements PdListener {

		private final IPdClient client;
		
		private ListenerClient(IPdClient client) {
			this.client = client;
		}
		
		@Override
		public boolean equals(Object o) {
			return (o == null) ? (client == null) : client.equals(o);
		}
		
		@Override
		public void receiveBang() {
			try {
				client.receiveBang();
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}

		@Override
		public void receiveFloat(float x) {
			try {
				client.receiveFloat(x);
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}
		
		@Override
		public void receiveSymbol(String symbol) {
			try {
				client.receiveSymbol(symbol);
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}

		@Override
		public void receiveList(Object... args) {
			try {
				client.receiveList(Arrays.asList(args));
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}

		@Override
		public void receiveMessage(String symbol, Object... args) {
			try {
				client.receiveMessage(symbol, Arrays.asList(args));
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}
		
	}

	private final IPdService.Stub binder = new IPdService.Stub() {
		
		@Override
		public void startAudio(int sampleRate, int nIn, int nOut,
				int ticksPerBuffer, boolean restart) throws RemoteException {
			PdService.this.startAudio(sampleRate, nIn, nOut, ticksPerBuffer, restart);
		}
		
		@Override
		public void stopAudio() throws RemoteException {
			PdService.this.stopAudio();
		}
		
		@Override
		public void subscribe(String symbol, IPdClient client)
				throws RemoteException {
			dispatcher.addListener(symbol, new ListenerClient(client));
		}
		
		@Override
		public void unsubscribe(String symbol, IPdClient client)
		throws RemoteException {
			dispatcher.removeListener(symbol, new ListenerClient(client)); // works because of ListenerClient.equals
		}
		
		@Override
		public void sendBang(String dest) throws RemoteException {
			PdBase.sendBang(dest);
		}
		
		@Override
		public void sendFloat(String dest, float x) throws RemoteException {
			PdBase.sendFloat(dest, x);
		}
		
		@Override
		public void sendSymbol(String dest, String symbol) throws RemoteException {
			PdBase.sendSymbol(dest, symbol);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void sendList(String dest, List args) throws RemoteException {
			PdBase.sendList(dest, args.toArray());			
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void sendMessage(String dest, String symbol, List args)
				throws RemoteException {
			PdBase.sendMessage(dest, symbol, args.toArray());
		}
	};

	private void startAudio(int sampleRate, int nIn, int nOut, int ticksPerBuffer, boolean restart) {
		if (wakeLock == null) {
			PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "pd wakelock");
			wakeLock.acquire();
		}
		try {
			PdAudioThread.startThread(sampleRate, nIn, nOut, ticksPerBuffer, restart);
		} catch (IOException e) {
			Log.e(PD_SERVICE, e.toString());
		}
	}

	private void stopAudio() {
		PdAudioThread.stopThread();
		dispatcher.release();
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		PdBase.setReceiver(dispatcher);
	};

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopAudio();
		PdBase.release();
	}
}
