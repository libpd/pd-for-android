/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 *
 * simple test case for {@link PdService}
 * 
 */

package org.puredata.android.service;

import org.puredata.android.R;
import org.puredata.android.io.AudioParameters;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PdPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initPreferences();
		addPreferencesFromResource(R.xml.preferences);
	}
	
	private void initPreferences() {
		Resources res = getResources();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (!prefs.contains(res.getString(R.string.pref_key_srate))) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(res.getString(R.string.pref_key_srate), "" + AudioParameters.getSampleRate());
			editor.putString(res.getString(R.string.pref_key_inchannels), "" + AudioParameters.getInputChannels());
			editor.putString(res.getString(R.string.pref_key_outchannels), "" + AudioParameters.getOutputChannels());
			editor.putString(res.getString(R.string.pref_key_tpb), "" + AudioParameters.getTicksPerBuffer());
			editor.commit();
		}
	}
}
