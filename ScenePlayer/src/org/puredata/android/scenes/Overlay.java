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
import android.view.View;

public abstract class Overlay implements Runnable {
	
	protected final int XS = 320, YS = 320; // magic constants dictated by RjDj
	protected volatile View container;
	protected volatile float x = 0, y = 0;
	private volatile boolean visible = false;
	
	public void setContainer(View container) {
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
	
	private void invalidate() {
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
