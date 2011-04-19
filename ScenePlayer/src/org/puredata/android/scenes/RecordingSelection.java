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

import org.puredata.android.scenes.SceneDataBase.RecordingColumn;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class RecordingSelection extends Activity implements OnItemClickListener, OnItemLongClickListener {

	private static final String TAG = "Recording Selection";
	private ListView recordingView;
	private SceneDataBase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new SceneDataBase(this);
		initGui();
	}

	private void initGui() {
		setContentView(R.layout.recording_selection);
		recordingView = (ListView) findViewById(R.id.recording_selection);
		recordingView.setOnItemClickListener(this);
		recordingView.setOnItemLongClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateList();
	}

	private void updateList() {
		RecordingListCursorAdapter adapter = new RecordingListCursorAdapter(RecordingSelection.this, db.getAllRecordings());
		recordingView.setAdapter(adapter);
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		Cursor cursor = db.getRecording(id);
		String path = SceneDataBase.getString(cursor, RecordingColumn.RECORDING_PATH);
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		File file = new File(path);
		intent.setDataAndType(Uri.fromFile(file), "audio/*");
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
		try {
			db.deleteRecording(id);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		updateList();
		return true;
	}
}
