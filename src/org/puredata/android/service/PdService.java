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
import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.core.PdBase;
import org.puredata.core.utils.PdDispatcher;
import org.puredata.core.utils.PdListener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;


public class PdService extends Service {

	private static final boolean hasEclair = Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.ECLAIR;
	private final ForegroundManager fgManager = hasEclair ? new ForegroundEclair() : new ForegroundCupcake();

	private static final String PD_SERVICE = "Pd Service";

	private int sampleRate = 0, nIn = 0, nOut = 0;
	private int ticksPerBuffer = Integer.MAX_VALUE;
	private int activeCount = 0;

	private final RemoteCallbackList<IPdClient> clients = new RemoteCallbackList<IPdClient>();

	private final PdDispatcher dispatcher = new PdDispatcher() {
		@Override
		public void print(String s) {
			printToClients(s);
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
		public void addClient(IPdClient client) throws RemoteException {
			clients.register(client);
		}

		@Override
		public void removeClient(IPdClient client) throws RemoteException {
			clients.unregister(client);
		}

		@Override
		public int requestAudio(int sampleRate, int nIn, int nOut,
				int ticksPerBuffer) throws RemoteException {
			return PdService.this.requestAudio(sampleRate, nIn, nOut, ticksPerBuffer);
		}
		
		@Override
		public int adjustAudio(int sampleRate, int nIn, int nOut,
				int ticksPerBuffer) throws RemoteException {
			return PdService.this.adjustAudio(sampleRate, nIn, nOut, ticksPerBuffer);
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
			return PdAudio.isRunning();
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
	
	private synchronized int requestAudio(int srate, int nic, int noc, int tpb) {
		int err = changeAudio(srate, nic, noc, tpb);
		if (err == 0) {
			activeCount++;
		}
		return err;
	}
	
	private synchronized int adjustAudio(int srate, int nic, int noc, int tpb) {
		if (activeCount > 0) {
			return changeAudio(srate, nic, noc, tpb);
		} else {
			return -10;  // nothing to adjust
		}
	}

	private synchronized void stopAudio() {
		if (activeCount <= 0) return;
		PdAudio.stopAudio();
		fgManager.stopForeground();
		sampleRate = nIn = nOut = activeCount = 0;
		ticksPerBuffer = Integer.MAX_VALUE;
		announceStop();
	}
	
	private synchronized void printToClients(String s) {
		int i = clients.beginBroadcast();
		while (i-- > 0) {
			try {
				clients.getBroadcastItem(i).print(s);
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}
		clients.finishBroadcast();
	}
	
	private synchronized void announceStart() {
		int i = clients.beginBroadcast();
		while (i-- > 0) {
			try {
				clients.getBroadcastItem(i).handleStart(sampleRate, nIn, nOut, ticksPerBuffer);
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}
		clients.finishBroadcast();
	}
	
	private synchronized void announceStop() {
		int i = clients.beginBroadcast();
		while (i-- > 0) {
			try {
				clients.getBroadcastItem(i).handleStop();
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}
		clients.finishBroadcast();
	}
	
	private void startAudio(int sampleRate, int nIn, int nOut, int ticksPerBuffer) throws IOException {
		PdAudio.startAudio(sampleRate, nIn, nOut, ticksPerBuffer, true);
		if (activeCount == 0) fgManager.startForeground();
		this.sampleRate = sampleRate;
		this.nIn = nIn;
		this.nOut = nOut;
		this.ticksPerBuffer = ticksPerBuffer;
		announceStart();
	}

	private int changeAudio(int sr, int nic, int noc, int tpb) {
		Resources res = getResources();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (sr < 0) {
			String srs = prefs.getString(res.getString(R.string.pref_key_srate), null);
			sr = (srs == null) ? AudioParameters.suggestSampleRate() : Integer.parseInt(srs);
		}
		if (nic < 0) {
			String ics = prefs.getString(res.getString(R.string.pref_key_inchannels), null);
			nic = (ics == null) ? AudioParameters.suggestInputChannels() : Integer.parseInt(ics);
		}
		if (noc < 0) {
			String ocs = prefs.getString(res.getString(R.string.pref_key_outchannels), null);
			noc = (ocs == null) ? AudioParameters.suggestOutputChannels() : Integer.parseInt(ocs);
		}
		if (tpb < 0) {
			String tpbs = prefs.getString(res.getString(R.string.pref_key_tpb), null);
			tpb = (tpbs == null) ? AudioParameters.suggestTicksPerBuffer() : Integer.parseInt(tpbs);
		}
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
			if (restart) startAudio(sr, nic, noc, tpb);
		} catch (Exception e) {
			Log.e(PD_SERVICE, e.toString());
			if (activeCount > 0 && !PdAudio.isRunning()) {
				try {
					PdAudio.startAudio(sampleRate, nIn, nOut, ticksPerBuffer, false);
				} catch (Exception e1) {
					Log.i(PD_SERVICE, e1.toString());
					stopAudio();
				}
			}
			return -1;
		}
		return 0;
	}

	private synchronized void releaseAudio() {
		if (activeCount == 1) {
			stopAudio();
		} else if (activeCount > 1){
			activeCount--;
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
		
		protected static final int NOTIFICATION_ID = 1;

		protected Notification makeNotification() {
			Intent intent = new Intent(PdService.this, KillPdService.class);
			PendingIntent pi = PendingIntent.getActivity(PdService.this, 0, intent, 0);
			Notification notification = new Notification(R.drawable.icon, "Pure Data", System.currentTimeMillis());
			notification.setLatestEventInfo(PdService.this, "Pure Data", "Tap to stop Pure Data.", pi);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			return notification;
		}
		
		@Override
		public void startForeground() {
			setForeground(true);
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(NOTIFICATION_ID, makeNotification());
		}

		@Override
		public void stopForeground() {
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(NOTIFICATION_ID);
			setForeground(false);
		}
	}

	private class ForegroundEclair extends ForegroundCupcake {
		@Override
		public synchronized void startForeground() {
			PdService.this.startForeground(NOTIFICATION_ID, makeNotification());
		}

		@Override
		public synchronized void stopForeground() {
			PdService.this.stopForeground(true);
		}
	}
}
