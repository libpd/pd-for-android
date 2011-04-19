/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.scenes;

import java.io.File;
import java.io.IOException;

import org.puredata.android.utils.Properties;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

public class SceneTabs extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		unpackResources();
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		intent = new Intent().setClass(this, SceneSelection.class);
		spec = tabHost.newTabSpec("scenes").setIndicator("Scenes", res.getDrawable(R.drawable.tab_scenes_selector)).setContent(intent);
		tabHost.addTab(spec);
//		intent = new Intent().setClass(this, SceneRecordings.class);
//		spec = tabHost.newTabSpec("recordings").setIndicator("Recordings", res.getDrawable(R.drawable.tab_recordings_selector)).setContent(intent);
//		tabHost.addTab(spec);
//		intent = new Intent().setClass(this, RjDjInfo.class);
//		spec = tabHost.newTabSpec("rjdj").setIndicator("RjDj.me", res.getDrawable(R.drawable.tab_rjdj_me_selector)).setContent(intent);
//		tabHost.addTab(spec);
		tabHost.setCurrentTab(0);
	}
	
	private void unpackResources() {
		Resources res = getResources();
		File libDir = getFilesDir();
		try {
			IoUtils.extractZipResource(res.openRawResource(R.raw.abstractions), libDir, true);
			IoUtils.extractZipResource(res.openRawResource(Properties.hasArmeabiV7a ? R.raw.externals_v7a : R.raw.externals), libDir, true);
			IoUtils.extractZipResource(getResources().openRawResource(R.raw.atsuke), new File("/sdcard/pd"), true);
			// many thanks to Frank Barknecht for providing Atsuke as a sample scene for inclusion in this package!
		} catch (IOException e) {
			Log.e("Scene Player", e.toString());
		}
		PdBase.addToSearchPath(libDir.getAbsolutePath());
	}
}
