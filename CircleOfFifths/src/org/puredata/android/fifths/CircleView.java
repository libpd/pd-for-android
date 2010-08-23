/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.fifths;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public final class CircleView extends View {

	private static final String TAG = "Pd Circle of Fifths";
	private static final String[] notesSharp = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
	private static final String[] notesFlat  = { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };
	private static final int[] shifts =        {  0,   -5,   2,   -3,   4,   -1,  6,    1,   -4,   3,   -2,   5  };
	private static final float R0 = 0.3f;
	private static final float R1 = (float) Math.sqrt((1 + R0 * R0) / 2);  // equal area for major and minor fields
	private final Paint black = new Paint();
	private final Paint bgMajor = new Paint();
	private final Paint bgMinor = new Paint();
	private final Paint labels = new Paint();
	private int top = 0;
	private float xCenter, yCenter, xNorm, yNorm;
	private int initialSegment;
	private CircleOfFifths owner;

	public CircleView(Context context) {
		super(context);
		init();
	}

	public CircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CircleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		black.setColor(Color.BLACK);
		bgMajor.setColor(Color.CYAN);
		bgMinor.setColor(Color.GRAY);
		labels.setColor(Color.BLACK);
		labels.setStyle(Style.FILL_AND_STROKE);
		labels.setTextAlign(Paint.Align.CENTER);
		labels.setTextSize(0.2f);
	}

	public void setOwner(CircleOfFifths owner) {
		this.owner = owner;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int xDim = getDim(widthMeasureSpec);
		int yDim = getDim(heightMeasureSpec);
		int dim = Math.min(xDim, yDim);
		Log.i(TAG, "dimension: " + dim);
		setMeasuredDimension(dim, dim);
	}

	private int getDim(int widthMeasureSpec) {
		int mode = MeasureSpec.getMode(widthMeasureSpec);
		int size = MeasureSpec.getSize(widthMeasureSpec);
		return (mode == MeasureSpec.UNSPECIFIED) ? 320 : size;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int scale = getWidth() / 2;
		canvas.scale(scale, scale);
		canvas.translate(1, 1);
		canvas.drawOval(new RectF(-1, -1, 1, 1), bgMajor);
		canvas.drawOval(new RectF(-R1, -R1, R1, R1), bgMinor);
		canvas.drawOval(new RectF(-R0, -R0, R0, R0), black);
		int c = top;
		int s0 = shifts[c];
		for (int i = 0; i < 12; i++) {
			int s1 = s0 + i;
			if (i > 6) s1 -= 12;
			String label = (s1 >= 0) ? notesSharp[c] : notesFlat[c];
			canvas.drawText(label, 0, -(1 + R1) / 2.2f, labels);
			c = (c + 9) % 12;
			label = (s1 >= 0) ? notesSharp[c] : notesFlat[c];
			canvas.drawText(label.toLowerCase(), 0, -(R1 + R0) / 2.2f, labels);
			c = (c + 10) % 12;
			canvas.rotate(15);
			canvas.drawLine(0, -1, 0, 1, labels);
			canvas.rotate(15);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		xCenter = w / 2;
		xNorm = 1 / xCenter;
		yCenter = h / 2;
		yNorm = 1 / yCenter;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = (event.getX() - xCenter) * xNorm;
		float y = (event.getY() - yCenter) * yNorm;
		float angle = (float) (Math.atan2(x, -y) * 6 / Math.PI);
		int segment = (int) (angle + 12.5f) % 12;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			float radiusSquared = x * x + y * y;
			if (radiusSquared > R0 * R0) {
				initialSegment = segment;
				boolean major = radiusSquared > R1 * R1;
				int note = (top + segment * 7 + (major ? 0 : 9)) % 12;
				owner.playChord(major ? 1 : 0, note);
			} else {
				initialSegment = -1;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (initialSegment > -1) {
				int step = (initialSegment - segment + 12) % 12;
				if (step > 0) {
					initialSegment = segment;
					top = (top + step * 7 + 24 * 12) % 12;
					invalidate();
					owner.shift(step);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		default:
			if (initialSegment > -1) {
				owner.endChord();
			}
			break;
		}
		return true;
	}
}
