/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.scenes;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.ImageView;

public class SceneView extends ImageView {

	private final List<Overlay> overlays = new ArrayList<Overlay>();
	
	public SceneView(Context context) {
		super(context);
	}

	public synchronized void addOverlay(Overlay overlay) {
		overlay.setContainer(this);
		overlays.add(overlay);
	}
	
	public synchronized void removeOverlay(Overlay overlay) {
		overlays.remove(overlay);
	}
	
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (Overlay overlay: overlays) {
			overlay.draw(canvas);
		}
	}
}
