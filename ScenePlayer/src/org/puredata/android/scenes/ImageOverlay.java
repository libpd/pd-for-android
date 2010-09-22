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
import android.graphics.Paint;
import android.graphics.Rect;

public class ImageOverlay extends Overlay {

	private final Bitmap image;
	private boolean centered = true;
	private float scaleX = 1.0f;
	private float scaleY = 1.0f;
	private float angle = 0.0f;
	private final Paint paint = new Paint();
	
	public ImageOverlay(String filename) {
		image = BitmapFactory.decodeFile(filename);
	}
	
	public void setCentered(boolean flag) {
		centered = flag;
		invalidate();
	}
	
	public void setScale(float valx, float valy) {
		scaleX = valx;
		scaleY = valy;
		invalidate();
		
	}
	
	public void setAngle(float val) {
		angle = val;
		invalidate();
	}
	
	public void setAlpha(float val) {
		paint.setAlpha((int) (val * 255));
		invalidate();
	}
	
	@Override
	protected void drawImpl(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		int cw = container.getWidth();
		int ch = container.getHeight();
		int xm = (int) (x * cw / SceneView.SIZE);
		int ym = (int) (y * ch / SceneView.SIZE);
		canvas.translate(xm, ym);
		canvas.rotate(angle);
		canvas.scale(scaleX, scaleY);
		int xd = image.getWidth() * cw / SceneView.SIZE / 2;
		int yd = image.getHeight() * ch / SceneView.SIZE / 2;
		Rect rect = centered ? new Rect(-xd, -yd, xd, yd)
							 : new Rect(0, 0, 2 * xd, 2 * yd);
		canvas.drawBitmap(image, null, rect, paint);
		canvas.restore();
	}
}
