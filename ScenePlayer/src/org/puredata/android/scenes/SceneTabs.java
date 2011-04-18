package org.puredata.android.scenes;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TabHost;

public class SceneTabs extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		intent = new Intent().setClass(this, SceneSelection.class);
		spec = tabHost.newTabSpec("scenes").setIndicator("Scenes", res.getDrawable(R.drawable.tab_scenes_selector)).setContent(intent);
		tabHost.addTab(spec);
		intent = new Intent().setClass(this, SceneRecordings.class);
		spec = tabHost.newTabSpec("recordings").setIndicator("Recordings", res.getDrawable(R.drawable.tab_recordings_selector)).setContent(intent);
		tabHost.addTab(spec);
		intent = new Intent().setClass(this, RjDjInfo.class);
		spec = tabHost.newTabSpec("rjdj").setIndicator("RjDj.me", res.getDrawable(R.drawable.tab_rjdj_me_selector)).setContent(intent);
		tabHost.addTab(spec);
		tabHost.setCurrentTab(0);
	}
}
