/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
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
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * 
 * PdService allows applications to run Pure Data as a (local) service, with foreground priority if desired.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 */
public class PdService extends Service {

	public class PdBinder extends Binder {
		public PdService getService() {
			return PdService.this;
		}
	}

	private static final String TAG = "PD Service";
	private static final int NOTIFICATION_ID = 1;
	private static boolean abstractionsInstalled = false;

	private final PdBinder binder = new PdBinder();
	private boolean hasForeground = false;

	private volatile int sampleRate = 0;
	private volatile int inputChannels = 0;
	private volatile int outputChannels = 0;
	private volatile float bufferSizeMillis = 0.0f;

	/**
	 * @return the current audio buffer size in milliseconds (approximate value;
	 * the exact value is a multiple of the Pure Data tick size (64 samples))
	 */
	public float getBufferSizeMillis() {
		return bufferSizeMillis;
	}

	/**
	 * @return number of input channels
	 */
	public int getInputChannels() {
		return inputChannels;
	}

	/**
	 * @return number of output channels
	 */
	public int getOutputChannels() {
		return outputChannels;
	}

	/**
	 * @return current sample rate
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * Initialize Pure Data and audio thread
	 * 
	 * @param srate   sample rate
	 * @param nic     number of input channels
	 * @param noc     number of output channels
	 * @param millis  audio buffer size in milliseconds; for Java audio only (Android 2.2 or earlier),
	 *                will be ignored by OpenSL components
	 * @throws IOException  if the audio parameters are not supported by the device
	 */
	public synchronized void initAudio(int srate, int nic, int noc, float millis) throws IOException {
		stopForeground();
		Resources res = getResources();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (srate < 0) {
			String s = prefs.getString(res.getString(R.string.pref_key_srate), null);
			if (s != null) {
				srate = Integer.parseInt(s);
			} else {
				srate = PdBase.suggestSampleRate();
				if (srate < 0) {
					srate = AudioParameters.suggestSampleRate();
				}
			}
		}
		if (nic < 0) {
			String s = prefs.getString(res.getString(R.string.pref_key_inchannels), null);
			if (s != null) {
				nic = Integer.parseInt(s);
			} else {
				nic = PdBase.suggestInputChannels();
				if (nic < 0) {
					nic = AudioParameters.suggestInputChannels();
				}
			}
		}
		if (noc < 0) {
			String s = prefs.getString(res.getString(R.string.pref_key_outchannels), null);
			if (s != null) {
				noc = Integer.parseInt(s);
			} else {
				noc = PdBase.suggestOutputChannels();
				if (noc < 0) {
					noc = AudioParameters.suggestOutputChannels();
				}
			}
		}
		if (millis < 0) {
			millis = 50.0f;  // conservative choice
		}
		int tpb = (int) (0.001f * millis * srate / PdBase.blockSize()) + 1;
		PdAudio.initAudio(srate, nic, noc, tpb, true);
		sampleRate = srate;
		inputChannels = nic;
		outputChannels = noc;
		bufferSizeMillis = millis;
	}

	/**
	 * Start the audio thread without foreground privileges
	 */
	public synchronized void startAudio() {
		PdAudio.startAudio(this);
	}

	/**
	 * Start the audio thread with foreground privileges
	 * 
	 * @param intent       intent to be triggered when the user selects the notification of the service
	 * @param icon         icon representing the notification
	 * @param title        title of the notification
	 * @param description  description of the notification
	 */
	public synchronized void startAudio(Intent intent, int icon, String title, String description) {
		startForeground(intent, icon, title, description);
		PdAudio.startAudio(this);
	}

	/**
	 * Stop the audio thread
	 */
	public synchronized void stopAudio() {
		PdAudio.stopAudio();
		stopForeground();
	}

	/**
	 * @return true if and only if the audio thread is running
	 */
	public synchronized boolean isRunning() {
		return PdAudio.isRunning();
	}

	/**
	 * Releases all resources
	 */
	public synchronized void release() {
		stopAudio();
		PdAudio.release();
		PdBase.release();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		release();
		return false;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		AudioParameters.init(this);
		if (!abstractionsInstalled) {
			try {
				File dir = getFilesDir();
				IoUtils.extractZipResource(getResources().openRawResource(R.raw.extra_abs), dir, true);
				abstractionsInstalled = true;
				PdBase.addToSearchPath(dir.getAbsolutePath());
				PdBase.addToSearchPath(getApplicationInfo().nativeLibraryDir);  // Location of standard externals.
			} catch (IOException e) {
				Log.e(TAG, "unable to unpack abstractions:" + e.toString());
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		release();
	}

	private Notification makeNotification(Intent intent, int icon, String title, String description) {
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
		return new NotificationCompat.Builder(PdService.this)
				.setSmallIcon(icon)
				.setContentTitle(title)
				.setTicker(title)
				.setContentText(description)
				.setOngoing(true)
				.setContentIntent(pi)
				.setWhen(System.currentTimeMillis())
				.build();
	}

	private void startForeground(Intent intent, int icon, String title, String description) {
		stopForeground();
		startForeground(NOTIFICATION_ID, makeNotification(intent, icon, title, description));
		hasForeground = true;
	}

	private void stopForeground() {
		if (hasForeground) {
			stopForeground(true);
			hasForeground = false;
		}
	}
}
