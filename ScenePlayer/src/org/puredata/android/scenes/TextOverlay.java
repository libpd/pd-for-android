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

public class TextOverlay extends Overlay {

	private volatile String text = "";
	private volatile int size = 0;
	private final Paint paint;
	
	public TextOverlay(String text) {
		setText(text);
		paint = new Paint();
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	@Override
	protected void drawImpl(Canvas canvas) {
		int cw = container.getWidth();
		int ch = container.getHeight();
		int xm = (int) (x * cw / XS);
		int ym = (int) (y * ch / YS);
		canvas.drawText(text, xm, ym, paint);
	}
}
