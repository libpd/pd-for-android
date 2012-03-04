/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.scenes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;


public class TextOverlay extends Overlay {

	private volatile String text = "";
	private final Paint paint;
	
	public TextOverlay(String text) {
		this.text = text;
		paint = new Paint();
	}
	
	public void setText(String text) {
		this.text = text;
		invalidate();
	}
	
	public void setSize(float size) {
		paint.setTextSize(size);
		invalidate();
	}
	
	@Override
	protected void drawImpl(Canvas canvas) {
		int cw = container.getWidth();
		int ch = container.getHeight();
		int xm = (int) (x * cw / SceneView.SIZE);
		int ym = (int) (y * ch / SceneView.SIZE);
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		int xd = (bounds.right + bounds.left) / 2;  // the sign looks wrong, but it turns out to be correct
		int yd = (bounds.top + bounds.bottom) / 2;
		canvas.drawText(text, xm - xd, ym - yd, paint);
	}
}
