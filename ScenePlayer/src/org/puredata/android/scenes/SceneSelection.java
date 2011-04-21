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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.lamerman.FileDialog;

public class SceneSelection extends Activity implements OnItemClickListener, OnItemLongClickListener, OnClickListener {

	private static final int FILE_SELECT_CODE = 1;
	private ListView sceneView;
	private Button updateButton;
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

	private void initGui() {
		setContentView(R.layout.scene_selection);
		sceneView = (ListView) findViewById(R.id.scene_selection);
		updateButton = new Button(this);
		updateButton.setText(getResources().getString(R.string.update_label));
		sceneView.addFooterView(updateButton);
		sceneView.setOnItemClickListener(this);
		sceneView.setOnItemLongClickListener(this);
		updateButton.setOnClickListener(this);
	}

	private void updateList() {
		if (cursor != null) {
			cursor.close();
		}
		cursor = db.getAllScenes();
		SceneListCursorAdapter adapter = new SceneListCursorAdapter(SceneSelection.this, cursor);
		sceneView.setAdapter(adapter);
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		Intent intent = new Intent(this, ScenePlayer.class);
		intent.putExtra(SceneColumn.ID.getLabel(), id);
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, final long id) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.setTitle(getResources().getString(R.string.delete_scene_title));
		dialog.setMessage(getResources().getString(R.string.delete_scene_message));
		dialog.setPositiveButton(getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					db.deleteScene(id);
				} catch (IOException e) {
					toast(getResources().getString(R.string.delete_scene_fail));
				}
				updateList();
			}
		});
		dialog.setNegativeButton(getResources().getString(android.R.string.no), null);
		dialog.show();
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
				toast(getResources().getString(R.string.open_scene_fail) + " " + path);
			}
		}
	}
}
