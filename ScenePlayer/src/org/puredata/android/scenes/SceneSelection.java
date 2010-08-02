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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.puredata.android.ioutils.IoUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SceneSelection extends Activity implements OnItemClickListener {

	private ListView sceneView;
	private final Map<String, String> scenes = new HashMap<String, String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initGui();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		TextView item = (TextView) v;
		String name = item.getText().toString();
		Intent intent = new Intent(this, ScenePlayer.class);
		intent.putExtra(ScenePlayer.SCENE, scenes.get(name));
		startActivity(intent);
	}
	
	private void initGui() {
		setContentView(R.layout.choose);
		sceneView = (ListView) findViewById(R.id.scene_selection);
		new Thread() {
			@Override
			public void run() {
				List<String> list = IoUtils.find(new File("/sdcard"), ".*\\.rj$");
				for (String dir: list) {
					scenes.put(new File(dir).getName(), dir);
				}
				ArrayList<String> keyList = new ArrayList<String>(scenes.keySet());
				Collections.sort(keyList, new Comparator<String>() {
					public int compare(String a, String b) {
						return a.toLowerCase().compareTo(b.toLowerCase());
					}
				});
				final ArrayAdapter<String> adapter = new ArrayAdapter<String>(SceneSelection.this, android.R.layout.simple_list_item_1, keyList);
				sceneView.getHandler().post(new Runnable() {
					@Override
					public void run() {
						sceneView.setAdapter(adapter);
					}
				});
			};
		}.start();
		sceneView.setOnItemClickListener(this);
	}
}
