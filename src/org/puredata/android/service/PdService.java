/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * remote service running pd in the background
 * 
 */

package org.puredata.android.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.puredata.android.R;
import org.puredata.android.io.PdAudioThread;
import org.puredata.core.PdBase;
import org.puredata.core.utils.PdDispatcher;
import org.puredata.core.utils.PdListener;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


public class PdService extends Service {
	
	private static final boolean hasEclair = Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.ECLAIR;
	private final ForegroundManager fgManager = hasEclair ? new ForegroundEclair() : new ForegroundCupcake();

	private static final String PD_SERVICE = "Pd Service";
	private static final String PREFIX = "org.puredata.android.service.";
	public static final String START_ACTION = PREFIX + "START_AUDIO";
	public static final String STOP_ACTION = PREFIX + "STOP_AUDIO";
	public static final String IN_CHANNELS = PREFIX + "IN_CHANNELS";
	public static final String OUT_CHANNELS = PREFIX + "OUT_CHANNELS";
	public static final String SRATE = PREFIX + "SRATE";
	public static final String TICKS = PREFIX + "TICKS";

	private int sampleRate = 0, nIn = 0, nOut = 0;
	private int ticksPerBuffer = Integer.MAX_VALUE;
	private int clientCount = 0;

	private final PdDispatcher dispatcher = new PdDispatcher() {
		@Override
		public void print(String s) {
			Log.i(PD_SERVICE, s);
		}
	};

	private class RemoteListener implements PdListener {

		private final String symbol;
		private final IPdListener client;

		private RemoteListener(String symbol, IPdListener client) {
			this.symbol = symbol;
			this.client = client;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof RemoteListener) {
				RemoteListener other = (RemoteListener) o;
				return other.symbol.equals(symbol) && other.client.equals(client);
			} else {
				return false;
			}
		}

		private void removeSelf() {
			dispatcher.removeListener(symbol, this);
		}

		@Override
		public void receiveBang() {
			try {
				client.receiveBang();
			} catch (RemoteException e) {
				removeSelf();
			}
		}

		@Override
		public void receiveFloat(float x) {
			try {
				client.receiveFloat(x);
			} catch (RemoteException e) {
				removeSelf();
			}
		}

		@Override
		public void receiveSymbol(String symbol) {
			try {
				client.receiveSymbol(symbol);
			} catch (RemoteException e) {
				removeSelf();
			}
		}

		@Override
		public void receiveList(Object... args) {
			try {
				client.receiveList(Arrays.asList(args));
			} catch (RemoteException e) {
				removeSelf();
			}
		}

		@Override
		public void receiveMessage(String symbol, Object... args) {
			try {
				client.receiveMessage(symbol, Arrays.asList(args));
			} catch (RemoteException e) {
				removeSelf();
			}
		}
	}

	private final IPdService.Stub binder = new IPdService.Stub() {

		@Override
		public int requestAudio(int sampleRate, int nIn, int nOut,
				int ticksPerBuffer) throws RemoteException {
			return PdService.this.requestAudio(sampleRate, nIn, nOut, ticksPerBuffer);
		}

		@Override
		public void releaseAudio() throws RemoteException {
			PdService.this.releaseAudio();
		}
		
		@Override
		public void stop() throws RemoteException {
			stopAudio();
		};
		
		@Override
		public boolean isRunning() throws RemoteException {
			return PdAudioThread.isRunning();
		}

		@Override
		public void subscribe(String symbol, IPdListener client) throws RemoteException {
			if (symbol != null && client != null) {
				dispatcher.addListener(symbol, new RemoteListener(symbol, client));
			}
		}

		@Override
		public void unsubscribe(String symbol, IPdListener client)
		throws RemoteException {
			dispatcher.removeListener(symbol, new RemoteListener(symbol, client)); // works because of RemoteListener.equals
		}

		@Override
		public boolean objectExists(String symbol) throws RemoteException {
			return PdBase.exists(symbol);
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
		public void sendMessage(String dest, String symbol, List args) throws RemoteException {
			PdBase.sendMessage(dest, symbol, args.toArray());
		}
	};

	private void announceStart() {
		Intent intent = new Intent(START_ACTION);
		intent.putExtra(SRATE, sampleRate);
		intent.putExtra(IN_CHANNELS, nIn);
		intent.putExtra(OUT_CHANNELS, nOut);
		intent.putExtra(TICKS, ticksPerBuffer);
		sendBroadcast(intent);
	}
	
	private void announceStop() {
		Intent intent = new Intent(STOP_ACTION);
		sendBroadcast(intent);
	}
	
	private void startAudio(int sampleRate, int nIn, int nOut, int ticksPerBuffer) throws IOException {
		fgManager.startForeground();
		PdAudioThread.startThread(sampleRate, nIn, nOut, ticksPerBuffer, true);
		this.sampleRate = sampleRate;
		this.nIn = nIn;
		this.nOut = nOut;
		this.ticksPerBuffer = ticksPerBuffer;
		announceStart();
	}

	private synchronized void stopAudio() {
		if (!PdAudioThread.isRunning()) return;
		PdAudioThread.stopThread();
		sampleRate = nIn = nOut = clientCount = 0;
		ticksPerBuffer = Integer.MAX_VALUE;
		announceStop();
		fgManager.stopForeground();
	}

	private synchronized int requestAudio(int sr, int nic, int noc, int tpb) {
		boolean restart = false;
		if (sr > sampleRate) restart = true;
		else sr = sampleRate;
		if (nic > nIn) restart = true;
		else nic = nIn;
		if (noc > nOut) restart = true;
		else noc = nOut;
		if (tpb < ticksPerBuffer) restart = true;
		else tpb = ticksPerBuffer;
		try {
			if (restart) {
				startAudio(sr, nic, noc, tpb);
			}
			++clientCount;
			return 0;
		} catch (IOException e) {
			if (sampleRate > 0) {
				try {
					PdAudioThread.startThread(sampleRate, nIn, nOut, ticksPerBuffer, true);
				} catch (IOException e1) {
					stopAudio();
				}
			}
			return -1;
		}
	}

	private synchronized void releaseAudio() {
		if (clientCount > 0 && --clientCount == 0) {
			stopAudio();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		PdBase.setReceiver(dispatcher);
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopAudio();
		dispatcher.release();
		PdBase.release();
	}
	
	private interface ForegroundManager {
		void startForeground();
		void stopForeground();
	}
	
	private class ForegroundCupcake implements ForegroundManager {
		@Override
		public void startForeground() {
			setForeground(true);
		}

		@Override
		public void stopForeground() {
			setForeground(false);
		}
	}
	
	private class ForegroundEclair implements ForegroundManager {
		@Override
		public void startForeground() {
			Intent intent = new Intent(PdService.this, KillPdService.class);
			PendingIntent pi = PendingIntent.getActivity(PdService.this, 0, intent, 0);
			Notification notification = new Notification(R.drawable.icon, "Pure Data", System.currentTimeMillis());
			notification.setLatestEventInfo(PdService.this, "Pure Data", "Tap to stop Pure Data.", pi);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			PdService.this.startForeground(1, notification);
		}
		
		@Override
		public void stopForeground() {
			PdService.this.stopForeground(true);
		}
	}
}
