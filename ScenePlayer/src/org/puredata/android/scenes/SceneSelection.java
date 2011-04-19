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
import java.util.List;

import org.puredata.android.scenes.SceneDataBase.SceneColumn;
import org.puredata.core.utils.IoUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class SceneSelection extends Activity implements OnItemClickListener, OnItemLongClickListener, OnClickListener {

	private static final String TAG = "Scene Selection";
	private ListView sceneView;
	private Button updateButton;
	private SceneDataBase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new SceneDataBase(this);
		initGui();
	}

	private void initGui() {
		setContentView(R.layout.scene_selection);
		sceneView = (ListView) findViewById(R.id.scene_selection);
		updateButton = new Button(this);
		updateButton.setText(getResources().getString(R.string.update_label));
		sceneView.addFooterView(updateButton);
		sceneView.setOnItemClickListener(this);
		sceneView.setOnItemLongClickListener(this);
		updateButton.setOnClickListener(this);
		updateList();
	}

	private void updateList() {
		SceneListCursorAdapter adapter = new SceneListCursorAdapter(SceneSelection.this, db.getAllScenes());
		sceneView.setAdapter(adapter);
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		Intent intent = new Intent(this, ScenePlayer.class);
		intent.putExtra(ScenePlayer.RECDIR, "/sdcard/pd");
		Cursor cursor = db.getScene(id);
		for (String column : cursor.getColumnNames()) {
			if (!column.equals(SceneColumn.ID.toString())) {
				intent.putExtra(column, SceneDataBase.getString(cursor, column));
			}
		}
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
		try {
			db.deleteScene(id);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		updateList();
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.equals(updateButton)) {
			updateDataBase();
		}
	}

	private void updateDataBase() {
		final ProgressDialog progress = new ProgressDialog(this);
		progress.setMessage("Loading scenes.  Please wait...");
		progress.setCancelable(false);
		progress.setIndeterminate(true);
		progress.show();
		new Thread() {
			@Override
			public void run() {
				List<File> list = IoUtils.find(new File("/sdcard"), ".*\\.rj$");
				for (File dir: list) {
					if (dir.isDirectory()) {
						try {
							db.addScene(dir);
						} catch (IOException e) {
							Log.e("Scene Player", e.toString());
						}
					}
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateList();
						progress.dismiss();
					}
				});
			};
		}.start();
	}
}
