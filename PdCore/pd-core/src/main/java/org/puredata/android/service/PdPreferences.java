/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.service;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.AudioDevices;
import org.puredata.core.PdBase;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.os.Handler;

/**
 * 
 * PdPreferences is a simple preference activity for choosing audio properties such
 * as sample rate and the number of audio I/O channels.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 */
public class PdPreferences extends PreferenceActivity {

	public AudioDevices audioDevices = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AudioParameters.init(this);
		initPreferences(getApplicationContext());
		getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment(), "prefFragment").commit();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public static class MyPreferenceFragment extends PreferenceFragment
	{
		void updateTitle(Resources res, int pref_key, int pref_title, String titlePost) {
			Preference pref = findPreference(res.getString(pref_key));
			if(pref == null) return;
			pref.setTitle(res.getString(pref_title) + ": " + titlePost);
		}

		void updateTitles(Resources res) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
			AudioDevices audioDevices = ((PdPreferences)getActivity()).audioDevices;
			updateTitle(res, R.string.pref_key_srate, R.string.pref_title_srate,
				PdBase.audioRuntimeInfo("sampleRate"));
			String indeviceValue = prefs.getString(res.getString(R.string.pref_key_indevice), "unknown");
			String indeviceName = audioDevices.getInputDeviceNameForPrefValue(indeviceValue);
			updateTitle(res, R.string.pref_key_indevice, R.string.pref_title_indevice, indeviceName);
			updateTitle(res, R.string.pref_key_inchannels, R.string.pref_title_inchannels,
				prefs.getString(res.getString(R.string.pref_key_inchannels), "unknown"));
			String outdeviceValue = prefs.getString(res.getString(R.string.pref_key_outdevice), "unknown");
			String outdeviceName = audioDevices.getOutputDeviceNameForPrefValue(outdeviceValue);
			updateTitle(res, R.string.pref_key_outdevice, R.string.pref_title_outdevice, outdeviceName);
			updateTitle(res, R.string.pref_key_outchannels, R.string.pref_title_outchannels,
				prefs.getString(res.getString(R.string.pref_key_outchannels), "unknown"));
			updateTitle(res, R.string.pref_key_buffersize, R.string.pref_title_buffersize,
				PdBase.audioRuntimeInfo("latencyMillis") + " ms");
		}

		void setupListener(Resources res, int pref_key) {
			Preference pref = findPreference(res.getString(pref_key));
			if(pref == null) return;
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						new Handler().postDelayed(() -> {
							updateTitles(res);
						}, 500);
						return true;
					}
				});
		}

		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			Resources res = getResources();
			addPreferencesFromResource(R.xml.preferences);
			((PdPreferences)getActivity()).audioDevices = new AudioDevices(getActivity());
			setupListener(res, R.string.pref_key_srate);
			setupListener(res, R.string.pref_key_indevice);
			setupListener(res, R.string.pref_key_inchannels);
			setupListener(res, R.string.pref_key_outdevice);
			setupListener(res, R.string.pref_key_outchannels);
			setupListener(res, R.string.pref_key_buffersize);
			updateTitles(res);
		}
	}

	/**
	 * If no preferences are available, initialize preferences with defaults suggested by {@link PdBase} or {@link AudioParameters}, in that order.
	 * 
	 * @param context  current application context
	 */
	public static void initPreferences(Context context) {
		Resources res = context.getResources();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (!prefs.contains(res.getString(R.string.pref_key_srate))) {
			SharedPreferences.Editor editor = prefs.edit();

			// init samplerate and audio device prefs to "Default":
			editor.putString(res.getString(R.string.pref_key_srate), res.getStringArray(R.array.srate_values)[0]);
			editor.putString(res.getString(R.string.pref_key_indevice), res.getStringArray(R.array.indevice_values)[0]);
			editor.putString(res.getString(R.string.pref_key_outdevice), res.getStringArray(R.array.outdevice_values)[0]);
			editor.putString(res.getString(R.string.pref_key_buffersize), res.getStringArray(R.array.buffersize_values)[0]);

			int nic = PdBase.suggestInputChannels();
			editor.putString(res.getString(R.string.pref_key_inchannels), "" + ((nic > 0) ? nic : AudioParameters.suggestInputChannels()));
			int noc = PdBase.suggestOutputChannels();
			editor.putString(res.getString(R.string.pref_key_outchannels), "" + ((noc > 0) ? noc : AudioParameters.suggestOutputChannels()));
			editor.commit();
		}
	}
}
