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

	private int sampleRate = 0, inputChannels = 0, outputChannels = 0;
	private float bufferSizeMillis = 0.0f;
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
		private final IPdListener listener;

		private RemoteListener(String symbol, IPdListener listener) {
			this.symbol = symbol;
			this.listener = listener;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof RemoteListener) {
				RemoteListener other = (RemoteListener) o;
				return other.symbol.equals(symbol) && other.listener.equals(listener);
			} else {
				return false;
			}
		}

		@Override
		public void receiveBang() {
			try {
				listener.receiveBang();
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}

		@Override
		public void receiveFloat(float x) {
			try {
				listener.receiveFloat(x);
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}

		@Override
		public void receiveSymbol(String symbol) {
			try {
				listener.receiveSymbol(symbol);
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}

		@Override
		public void receiveList(Object... args) {
			try {
				listener.receiveList(Arrays.asList(args));
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}

		@Override
		public void receiveMessage(String symbol, Object... args) {
			try {
				listener.receiveMessage(symbol, Arrays.asList(args));
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}
	}

	private final IPdService.Stub binder = new IPdService.Stub() {
		
		private final Object empty[] = new Object[0];

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
				float bufferSizeMillis) throws RemoteException {
			return PdService.this.requestAudio(sampleRate, nIn, nOut, bufferSizeMillis);
		}

		@Override
		public void releaseAudio() throws RemoteException {
			PdService.this.releaseAudio();
		}

		@Override
		public void stop() throws RemoteException {
			PdService.this.stop();
		};

		@Override
		public boolean isRunning() throws RemoteException {
			return PdAudio.isRunning();
		}

		@Override
		public float getBufferSizeMillis() throws RemoteException {
			return bufferSizeMillis;
		}

		@Override
		public int getInputChannels() throws RemoteException {
			return inputChannels;
		}

		@Override
		public int getOutputChannels() throws RemoteException {
			return outputChannels;
		}

		@Override
		public int getSampleRate() throws RemoteException {
			return sampleRate;
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
		public boolean exists(String symbol) throws RemoteException {
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
			PdBase.sendList(dest, (args == null) ? empty : args.toArray());			
		}

		@SuppressWarnings("unchecked")
		@Override
		public void sendMessage(String dest, String symbol, List args) throws RemoteException {
			PdBase.sendMessage(dest, symbol, (args == null) ? empty : args.toArray());
		}
	};
	
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

	private void audioChanged() { // no sync needed because this will only be called from a synchronized method
		int i = clients.beginBroadcast();
		while (i-- > 0) {
			try {
				clients.getBroadcastItem(i).audioChanged(sampleRate, inputChannels, outputChannels, bufferSizeMillis);
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}
		clients.finishBroadcast();
	}
	
	private void requestUnbind() { // no sync here, either
		int i = clients.beginBroadcast();
		while (i-- > 0) {
			try {
				clients.getBroadcastItem(i).requestUnbind();
			} catch (RemoteException e) {
				Log.e(PD_SERVICE, e.toString());
			}
		}
		clients.finishBroadcast();
	}

	private synchronized int requestAudio(int srate, int nic, int noc, float millis) {
		if (activeCount > 0) {
			if (srate > 0 && srate != sampleRate) return -1; // can't reconcile sample rates
			if (nic > inputChannels) return -1; // can't reconcile number of input channels
			if (noc > outputChannels) return -1; // can't reconcile number of output channels
			// no check for buffer size
			activeCount++;
			return 0;
		}
		Resources res = getResources();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (srate < 0) {
			String s = prefs.getString(res.getString(R.string.pref_key_srate), null);
			srate = (s == null) ? AudioParameters.suggestSampleRate() : Integer.parseInt(s);
		}
		if (nic < 0) {
			String s = prefs.getString(res.getString(R.string.pref_key_inchannels), null);
			nic = (s == null) ? AudioParameters.suggestInputChannels() : Integer.parseInt(s);
		}
		if (noc < 0) {
			String s = prefs.getString(res.getString(R.string.pref_key_outchannels), null);
			noc = (s == null) ? AudioParameters.suggestOutputChannels() : Integer.parseInt(s);
		}
		if (millis < 0) {
			String s = prefs.getString(res.getString(R.string.pref_key_bufsize_millis), null);
			millis = (s == null) ? AudioParameters.suggestBufferSizeMillis() : Float.parseFloat(s);
		}
		try {
			startAudio(srate, nic, noc, millis);
		} catch (Exception e) {
			Log.e(PD_SERVICE, e.toString());
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
	
	private synchronized void stop() {
		requestUnbind();
		stopSelf();
	}

	private void startAudio(int srate, int nic, int noc, float millis) throws IOException {
		if (activeCount > 0) return;
		int tpb = (int) (0.001f * millis * srate / PdBase.blockSize()) + 1;
		PdAudio.startAudio(srate, nic, noc, tpb, true);
		fgManager.startForeground();
		sampleRate = srate;
		inputChannels = nic;
		outputChannels = noc;
		bufferSizeMillis = millis;
		activeCount = 1;
		audioChanged();
	}

	private void stopAudio() {
		if (activeCount <= 0) return;
		PdAudio.stopAudio();
		fgManager.stopForeground();
		sampleRate = inputChannels = outputChannels = activeCount = 0;
		bufferSizeMillis = 0.0f;
		audioChanged();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i(PD_SERVICE, "onStart: " + intent.getAction());
		stop();
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
			Intent intent = new Intent(PdUtils.STOP_ACTION);
			PendingIntent pi = PendingIntent.getService(PdService.this, 0, intent, 0);
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
