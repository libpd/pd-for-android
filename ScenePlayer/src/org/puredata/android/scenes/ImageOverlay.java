/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.scenes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

public class ImageOverlay extends Overlay {

	private final Bitmap image;
	
	public ImageOverlay(String filename) {
		image = BitmapFactory.decodeFile(filename);
	}
	
	@Override
	protected void drawImpl(Canvas canvas) {
		int cw = container.getWidth();
		int ch = container.getHeight();
		int xm = (int) (x * cw / XS);
		int ym = (int) (y * ch / YS);
		int xd = image.getWidth() * cw / XS / 2;
		int yd = image.getHeight() * ch / YS / 2;
		Rect rect = new Rect(xm - xd, ym - yd, xm + xd, ym + yd);
		canvas.drawBitmap(image, null, rect, null);
	}
}
