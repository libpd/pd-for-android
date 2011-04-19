/**
 * @author Martin Roth (mhroth@rjdj.me)
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.scenes;

import java.io.File;
import java.util.Date;

import org.puredata.android.scenes.SceneDataBase.RecordingColumn;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Simple adapter for displaying recordings.
 */
public class RecordingListCursorAdapter extends CursorAdapter {

  public RecordingListCursorAdapter(Context context, Cursor cursor) {
    super(context, cursor);
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    TextView textView1 = (TextView) view.findViewById(android.R.id.text1);
    textView1.setText(SceneDataBase.getString(cursor, RecordingColumn.SCENE_TITLE));
    TextView textView2 = (TextView) view.findViewById(android.R.id.text2);
    textView2.setText(new Date(SceneDataBase.getLong(cursor, RecordingColumn.RECORDING_TIMESTAMP)).toString());
    ImageView imageView = (ImageView) view.findViewById(android.R.id.selectedIcon);
    String sceneFolder = SceneDataBase.getString(cursor, RecordingColumn.SCENE_DIRECTORY);
	File file = new File(sceneFolder, "thumb.jpg");
	Drawable icon = Drawable.createFromPath(file.getAbsolutePath());
    if (icon == null) {
    	file = new File(sceneFolder, "image.jpg");
    	icon = Drawable.createFromPath(file.getAbsolutePath());
    }
    if (icon != null) {
    	imageView.setImageDrawable(icon);
    } else {
    	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.default_thumb));
    }
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    View view = View.inflate(context, R.layout.two_line_list_item, null);
    bindView(view, context, cursor);
    return view;
  }
}
