/**
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.service;

import org.puredata.android.io.AudioParameters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * 
 * PdPreferences is a simple preference activity for choosing audio properties such
 * as sample rate and the number of audio I/O channels.
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 */
public class PdPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initPreferences(getApplicationContext());
		addPreferencesFromResource(R.xml.preferences);	
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	/**
	 * If no preferences are available, initialize preferences with defaults suggested by {@link AudioParameters}
	 * 
	 * @param context  current application context
	 */
	public static void initPreferences(Context context) {
		Resources res = context.getResources();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (!prefs.contains(res.getString(R.string.pref_key_srate))) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(res.getString(R.string.pref_key_srate), "" + AudioParameters.suggestSampleRate());
			editor.putString(res.getString(R.string.pref_key_inchannels), "" + AudioParameters.suggestInputChannels());
			editor.putString(res.getString(R.string.pref_key_outchannels), "" + AudioParameters.suggestOutputChannels());
			editor.putString(res.getString(R.string.pref_key_bufsize_millis), "" + AudioParameters.suggestBufferSizeMillis());
			editor.commit();
		}
	}
}
