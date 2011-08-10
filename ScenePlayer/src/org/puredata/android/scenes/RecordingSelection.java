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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
	private Cursor cursor = null;

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
		initGui();
		db = new SceneDataBase(this);
	}

	private void initGui() {
		setContentView(R.layout.recording_selection);
		recordingView = (ListView) findViewById(R.id.recording_selection);
		recordingView.setEmptyView(findViewById(R.id.no_recordings));
		recordingView.setOnItemClickListener(this);
		recordingView.setOnItemLongClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateList();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		cursor.close();
		db.close();
	}

	private void updateList() {
		if (cursor != null) {
			cursor.close();
		}
		cursor = db.getAllRecordings();
		RecordingListCursorAdapter adapter = new RecordingListCursorAdapter(RecordingSelection.this, cursor, db);
		recordingView.setAdapter(adapter);
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		Intent intent = new Intent(this, RecordingPlayer.class);
		intent.putExtra(RecordingColumn.ID.getLabel(), id);
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, final long id) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.setTitle(getResources().getString(R.string.delete_recording_title));
		dialog.setMessage(getResources().getString(R.string.delete_recording_message));
		dialog.setPositiveButton(getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					db.deleteRecording(id);
				} catch (IOException e) {
					toast(getResources().getString(R.string.delete_recording_fail));
				}
				updateList();
			}
		});
		dialog.setNegativeButton(getResources().getString(android.R.string.no), null);
		dialog.show();
		return true;
	}
}
