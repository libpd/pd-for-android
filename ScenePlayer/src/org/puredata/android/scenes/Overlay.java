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
import android.widget.ImageView;

public abstract class Overlay implements Runnable {
	
	protected volatile ImageView container;
	protected volatile float x = 0, y = 0;
	private volatile boolean visible = true;
	
	public void setContainer(ImageView container) {
		this.container = container;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		invalidate();
	}
	
	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
		invalidate();
	}
	
	protected void invalidate() {
		container.getHandler().post(this);
	}
	
	// override this for more sophisticated choice of invalidation
	@Override
	public void run() {
		container.invalidate();
	}
	
	public void draw(Canvas canvas) {
		if (visible) drawImpl(canvas);
	}
	
	protected abstract void drawImpl(Canvas canvas);
}
