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

import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

public class SceneTabs extends TabActivity {

	private static final String RECORDINGS_TAG = "recordings";
	private static final String SCENES_TAG = "scenes";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		unpackResources();
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		intent = new Intent().setClass(this, SceneSelection.class);
		spec = tabHost.newTabSpec(SCENES_TAG).setIndicator("Scenes", res.getDrawable(R.drawable.tab_scenes_selector)).setContent(intent);
		tabHost.addTab(spec);
		intent = new Intent().setClass(this, RecordingSelection.class);
		spec = tabHost.newTabSpec(RECORDINGS_TAG).setIndicator("Recordings", res.getDrawable(R.drawable.tab_recordings_selector)).setContent(intent);
		tabHost.addTab(spec);
		tabHost.setCurrentTab(0);
	}
	
	private void unpackResources() {
		Resources res = getResources();
		File libDir = getFilesDir();
		try {
			IoUtils.extractZipResource(res.openRawResource(R.raw.abstractions), libDir, true);
		} catch (IOException e) {
			Log.e("Scene Player", e.toString());
		}
		PdBase.addToSearchPath(libDir.getAbsolutePath());
	}

	@Override
	protected void onStart() {
		super.onStart();
		Uri uri = getIntent().getData();
		if (uri != null) {
			getIntent().setData(null);
			Intent intent = new Intent().setClass(this, SceneSelection.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.setData(uri);
			getLocalActivityManager().startActivity(SCENES_TAG, intent);
		}
	}
}
