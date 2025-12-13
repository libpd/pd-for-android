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
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			Resources res = getResources();
			addPreferencesFromResource(R.xml.preferences);
			((PdPreferences)getActivity()).audioDevices = new AudioDevices(getActivity());
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

			int nic = PdBase.suggestInputChannels();
			editor.putString(res.getString(R.string.pref_key_inchannels), "" + ((nic > 0) ? nic : AudioParameters.suggestInputChannels()));
			int noc = PdBase.suggestOutputChannels();
			editor.putString(res.getString(R.string.pref_key_outchannels), "" + ((noc > 0) ? noc : AudioParameters.suggestOutputChannels()));
			editor.commit();
		}
	}
}
