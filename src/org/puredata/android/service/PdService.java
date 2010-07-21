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

import org.puredata.android.io.PdAudioThread;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.PdDispatcher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;


public class PdService extends Service {

	private WakeLock wakeLock = null;

	private static final String PD_SERVICE = "Pd Service";

	private final PdDispatcher dispatcher = new PdDispatcher() {
		@Override
		public void print(String s) {
			Log.i(PD_SERVICE, s);
		}
	};

	private PdReceiver sender = new PdReceiver() {
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

	private final Messenger receiver = new Messenger(new PdServiceHub.ServiceHandler(this, sender));

	public void startAudio(int sampleRate, int nIn, int nOut, int ticksPerBuffer, boolean restart) {
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

	public void stopAudio() {
		PdAudioThread.stopThread();
		dispatcher.release();
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	public void addSubscription(String symbol, Messenger messenger) {
		dispatcher.addListener(symbol, new PdServiceHub.ClientLink(symbol, messenger));
	}

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
		stopAudio();
		PdBase.release();
	}
}
