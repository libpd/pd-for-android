/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com) 
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package at.or.at.voiceorama;

import org.puredata.android.utils.Properties;
import org.puredata.core.PdBase;

import android.view.MotionEvent;


// Cute little hack to support multiple versions of the Android API, based on an idea
// from http://android-developers.blogspot.com/2010/07/how-to-have-your-cupcake-and-eat-it-too.html
public final class VersionedTouch {

	private static final String TOUCH_SYMBOL = "#touch", DOWN = "down", UP = "up", XY = "xy";
	private static final boolean hasEclair = Properties.version >= 5;
	private static final float XS = 319.0f, YS = 319.0f;

	private VersionedTouch() {
		// do nothing
	}
	
	public static boolean evaluateTouch(MotionEvent event, int xImg, int yImg) {
		return (hasEclair) ? TouchEclair.evaluateTouch(event, xImg, yImg) : TouchCupcake.evaluateTouch(event, xImg, yImg);
	}

	private static class TouchEclair {

		public static boolean evaluateTouch(MotionEvent event, int xImg, int yImg) {
			int action = event.getAction();
			String actionTag = null;
			switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_POINTER_DOWN:
				actionTag = DOWN;
			case MotionEvent.ACTION_POINTER_UP:
				if (actionTag == null) actionTag = UP;
				int pointerIndex = (action & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT; // funny misnomer in Eclair...
				int pointerId = event.getPointerId(pointerIndex);
				float x = normalize(event.getX(pointerIndex), XS, xImg);
				float y = normalize(event.getY(pointerIndex), YS, yImg);
				sendMessage(actionTag, pointerId, x, y);
				break;
			case MotionEvent.ACTION_DOWN:
				actionTag = DOWN;
			case MotionEvent.ACTION_MOVE:
				if (actionTag == null) actionTag = XY;
			default:
				if (actionTag == null) actionTag = UP;
				for (int i = 0; i < event.getPointerCount(); i++) {
					x = normalize(event.getX(i), XS, xImg);
					y = normalize(event.getY(i), YS, yImg);
					sendMessage(actionTag, event.getPointerId(i), x, y);
				}
				break;
			}
			return true;
		}
	}

	private static class TouchCupcake {

		public static boolean evaluateTouch(MotionEvent event, int xImg, int yImg) {
			String actionTag;
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				actionTag = DOWN;
				break;
			case MotionEvent.ACTION_MOVE:
				actionTag = XY;
				break;
			default:
				actionTag = UP;
				break;
			}
			float x = normalize(event.getX(), XS, xImg);
			float y = normalize(event.getY(), YS, yImg);
			sendMessage(actionTag, 0, x, y);
			return true;
		}
	}
	
	private static float normalize(float v, float vm, int dim) {
		float t = v * vm / dim;
		if (t < 0) t = 0;
		else if (t > vm) t = vm;
		return t;
	}
	
	private static void sendMessage(String actionTag, int pointerId, float x, float y) {
		PdBase.sendMessage(TOUCH_SYMBOL, actionTag, pointerId + 1, x, y);
	}
}
