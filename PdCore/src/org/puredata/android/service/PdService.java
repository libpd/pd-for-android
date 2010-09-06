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

import java.io.File;
import java.io.IOException;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;


public class PdService extends Service {

	public class PdBinder extends Binder {
		public PdService getService() {
			return PdService.this;
		}
	}
	private final PdBinder binder = new PdBinder();
	private static final boolean hasEclair = Integer.parseInt(Build.VERSION.SDK) >= 5;
	private final ForegroundManager fgManager = hasEclair ? new ForegroundEclair() : new ForegroundCupcake();

	private static final String PD_SERVICE = "PD Service";
	private volatile int sampleRate = 0;
	private volatile int inputChannels = 0;
	private volatile int outputChannels = 0;
	private volatile float bufferSizeMillis = 0.0f;

	public float getBufferSizeMillis() {
		return bufferSizeMillis;
	}

	public int getInputChannels() {
		return inputChannels;
	}

	public int getOutputChannels() {
		return outputChannels;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public synchronized void startAudio(int srate, int nic, int noc, float millis, Intent intent) throws IOException {
		startAudio(srate, nic, noc, millis);
		fgManager.startForeground(intent);
	}

	public synchronized void startAudio(int srate, int nic, int noc, float millis) throws IOException {
		fgManager.stopForeground();
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
		int tpb = (int) (0.001f * millis * srate / PdBase.blockSize()) + 1;
		PdAudio.startAudio(this, srate, nic, noc, tpb, true);
		sampleRate = srate;
		inputChannels = nic;
		outputChannels = noc;
		bufferSizeMillis = millis;
	}

	public synchronized void stopAudio() {
		PdAudio.stopAudio();
		fgManager.stopForeground();
		sampleRate = 0;
		inputChannels = 0;
		outputChannels = 0;
		bufferSizeMillis = 0.0f;
	}

	public synchronized boolean isRunning() {
		return PdAudio.isRunning();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Resources res = getResources();
		File dir = getFilesDir();
		try {
			IoUtils.extractZipResource(res.openRawResource(R.raw.extra_abs), dir, false);
			IoUtils.extractZipResource(res.openRawResource(IoUtils.hasArmeabiV7a() ? R.raw.extra_ext_v7a : R.raw.extra_ext), dir, false);
		} catch (IOException e) {
			Log.e(PD_SERVICE, "unable to unpack abstractions/extras: " + e.toString());
		}
		PdBase.addToSearchPath(dir.getAbsolutePath());
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopAudio();
		PdBase.release();
	}

	private interface ForegroundManager {
		void startForeground(Intent intent);
		void stopForeground();
	}

	private class ForegroundCupcake implements ForegroundManager {
		protected static final int NOTIFICATION_ID = 1;
		private boolean hasForeground = false;

		protected Notification makeNotification(Intent intent) {
			PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
			Notification notification = new Notification(R.drawable.icon, "Pure Data", System.currentTimeMillis());
			notification.setLatestEventInfo(PdService.this, "Pure Data", "Tap to return to Pd client.", pi);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			return notification;
		}

		@Override
		public void startForeground(Intent intent) {
			stopForeground();
			versionedStart(intent);
			hasForeground = true;
		}
		
		protected void versionedStart(Intent intent) {
			setForeground(true);
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(NOTIFICATION_ID, makeNotification(intent));
		}

		@Override
		public void stopForeground() {
			if (hasForeground) {
				versionedStop();
				hasForeground = false;
			}
		}
		
		protected void versionedStop() {
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(NOTIFICATION_ID);
			setForeground(false);
		}
	}

	private class ForegroundEclair extends ForegroundCupcake {
		@Override
		protected void versionedStart(Intent intent) {
			PdService.this.startForeground(NOTIFICATION_ID, makeNotification(intent));
		}

		@Override
		protected void versionedStop() {
			PdService.this.stopForeground(true);
		}
	}
}
