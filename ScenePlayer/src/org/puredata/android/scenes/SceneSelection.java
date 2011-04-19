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

import org.puredata.android.scenes.SceneDataBase.SceneColumn;

import com.lamerman.FileDialog;

import android.app.Activity;
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

	private static final int FILE_SELECT_CODE = 1;
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
			Intent intent = new Intent(this, FileDialog.class);
			intent.putExtra(FileDialog.START_PATH, "/sdcard");
			startActivityForResult(intent, FILE_SELECT_CODE);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == FILE_SELECT_CODE) {
			String path = data.getStringExtra(FileDialog.RESULT_PATH);
			try {
				db.addScene(new File(path));
				updateList();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}
	}
}
