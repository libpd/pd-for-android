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
import android.util.AttributeSet;
import android.widget.ImageView;

public class SceneView extends ImageView {

	public final static int SIZE = 320;
	
	public SceneView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private final List<Overlay> overlays = new ArrayList<Overlay>();

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

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int xDim = getDim(widthMeasureSpec);
		int yDim = getDim(heightMeasureSpec);
		int dim = Math.min(xDim, yDim);
		setMeasuredDimension(dim, dim);
	}

	private int getDim(int widthMeasureSpec) {
		int mode = MeasureSpec.getMode(widthMeasureSpec);
		int size = MeasureSpec.getSize(widthMeasureSpec);
		return (mode == MeasureSpec.UNSPECIFIED) ? SIZE : size;
	}
}
