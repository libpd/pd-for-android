/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.scenes;

import java.io.IOException;

import org.puredata.android.scenes.SceneDataBase.RecordingColumn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class RecordingSelection extends Activity implements OnItemClickListener, OnItemLongClickListener {

	private ListView recordingView;
	private SceneDataBase db;

	private Toast toast = null;
	
	private void toast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (toast == null) {
					toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
				}
				toast.setText(msg);
				toast.show();
			}
		});
	}
	
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
		Intent intent = new Intent(this, RecordingPlayer.class);
		intent.putExtra(RecordingColumn.ID.getLabel(), id);
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
		try {
			db.deleteRecording(id);
		} catch (IOException e) {
			toast(getResources().getString(R.string.delete_recording_fail));
		}
		updateList();
		return true;
	}
}
