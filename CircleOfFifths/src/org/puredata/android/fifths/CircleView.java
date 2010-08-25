/**
 * 
 * @author Peter Brinkmann (peter.brinkmann@gmail.com)
 * 
 * Some visual elements adapted from http://mindtherobot.com/blog/534/android-ui-making-an-analog-rotary-knob/
 * 
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 * 
 */

package org.puredata.android.fifths;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public final class CircleView extends View {

	private static final float RIDGE_WIDTH = 0.01f;
	private static final String TAG = "Pd Circle of Fifths";
	private static final String[] notesSharp = { "C", "C\u266f", "D", "D\u266f", "E", "F", "F\u266f", "G", "G\u266f", "A", "A\u266f", "B" };
	private static final String[] notesFlat  = { "C", "D\u266d", "D", "E\u266d", "E", "F", "G\u266d", "G", "A\u266d", "A", "B\u266d", "B" };
	private static final int[] shifts =        {  0,   -5,   2,   -3,   4,   -1,  6,    1,   -4,   3,   -2,   5  };
	private static final float R0 = 0.28f;
	private static final float R1 = (float) Math.sqrt((1 + R0 * R0) / 2);  // equal area for major and minor fields
	private int top = 0;
	private float xCenter, yCenter, xNorm, yNorm;
	private int selectedSegment = -1;
	private boolean selectedMajor;
	private CircleOfFifths owner;

	private Bitmap wheel = null;
	private Bitmap texture;
	private Paint backgroundPaint;
	private Paint ridgePaint;
	private Paint radialShadowPaint;
	private Paint labelPaint;
	private Paint selectedPaint;

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

	private static Paint createDefaultPaint() {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		return paint;
	}

	private void init() {
		texture = BitmapFactory.decodeResource(getResources(), R.drawable.bgtexture);
		backgroundPaint = createDefaultPaint();
		BitmapShader shader = new BitmapShader(texture, TileMode.MIRROR, TileMode.MIRROR);
		Matrix textureMatrix = new Matrix();
		textureMatrix.setScale(2.0f / texture.getWidth(), 2.0f / texture.getHeight());
		textureMatrix.postTranslate(1f, 1f);
		shader.setLocalMatrix(textureMatrix);
		backgroundPaint.setShader(shader);

		ridgePaint = createDefaultPaint();
		ridgePaint.setColor(Color.DKGRAY);
		ridgePaint.setMaskFilter(new BlurMaskFilter(0.005f, Blur.NORMAL));
		ridgePaint.setStyle(Paint.Style.STROKE);
		ridgePaint.setStrokeWidth(RIDGE_WIDTH);

		radialShadowPaint = createDefaultPaint();
		radialShadowPaint.setShader(new RadialGradient(0, 0, R0, new int[] { 0x00ffffff, 0x44000000 }, null, TileMode.CLAMP));
		radialShadowPaint.setMaskFilter(new BlurMaskFilter(0.01f, Blur.NORMAL));

		labelPaint = createDefaultPaint();
		labelPaint.setColor(Color.BLACK);
		labelPaint.setTextAlign(Paint.Align.CENTER);
		labelPaint.setTypeface(Typeface.MONOSPACE);
		labelPaint.setTextSize(0.2f);

		selectedPaint = createDefaultPaint();
		selectedPaint.setColor(Color.RED);
		selectedPaint.setStyle(Paint.Style.FILL);
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
		canvas.translate(xCenter, yCenter);
		canvas.scale(xCenter, yCenter);
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(-top * 30);
		canvas.drawBitmap(wheel, null, new Rect(-1, -1, 1, 1), null);
		canvas.restore();
		int c = (top * 7) % 12;
		int s0 = shifts[c];
		for (int i = 0; i < 12; i++) {
			if (i == selectedSegment) {
				canvas.drawCircle(0, -(R1 + (selectedMajor ? 1 : R0)) / 2f, 0.2f, selectedPaint);
			}
			int s1 = s0 + i;
			if (i > 6) s1 -= 12;
			String label = (s1 >= 0) ? notesSharp[c] : notesFlat[c];
			drawLabel(canvas, label, -(1 + R1) / 2.2f);
			c = (c + 9) % 12;
			label = (s1 >= 0) ? notesSharp[c] : notesFlat[c];
			drawLabel(canvas, label.toLowerCase(), -(R1 + R0) / 2.15f);
			c = (c + 10) % 12;
			canvas.rotate(30);
		}
	}

	// ugly hack to work around unicode spacing problem
	private void drawLabel(Canvas canvas, String label, float r) {
		if (label.length() > 1) {
			canvas.drawText(label.charAt(0) + " ", 0, r, labelPaint);
			canvas.drawText(" " + label.charAt(1), 0, r, labelPaint);
		} else {
			canvas.drawText(label, 0, r, labelPaint);				
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		xCenter = w / 2;
		xNorm = 1 / xCenter;
		yCenter = h / 2;
		yNorm = 1 / yCenter;
		drawWheel(w, h);
	}

	private void drawWheel(int w, int h) {
		if (wheel != null) {
			wheel.recycle();
		}
		Canvas canvas = new Canvas();
		wheel = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		canvas.setBitmap(wheel);
		canvas.translate(xCenter, yCenter);
		canvas.scale(xCenter, yCenter);
		canvas.drawCircle(0, 0, 1, backgroundPaint);
		canvas.drawCircle(0, 0, R0,  radialShadowPaint);
		canvas.drawCircle(0, 0, R0, ridgePaint);
		canvas.drawCircle(0, 0, R1, ridgePaint);
		canvas.drawCircle(0, 0, 1 - RIDGE_WIDTH / 2, ridgePaint);
		canvas.rotate(15);
		for (int i = 0; i < 12; i++) {
			canvas.drawLine(0, R0, 0, 1, ridgePaint);
			canvas.rotate(30);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = (event.getX() - xCenter) * xNorm;
		float y = (event.getY() - yCenter) * yNorm;
		float angle = (float) (Math.atan2(x, -y) * 6 / Math.PI);
		int segment = (int) (angle + 12.5f) % 12;
		float radiusSquared = x * x + y * y;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (radiusSquared > R0 * R0 && radiusSquared < 1) {
				selectedSegment = segment;
				selectedMajor = radiusSquared > R1 * R1;
				int note = (top * 7 + segment * 7 + (selectedMajor ? 0 : 9)) % 12;
				owner.playChord(selectedMajor, note);
				invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (selectedSegment > -1 && radiusSquared > R0 * R0 && radiusSquared < 1) {
				int step = (selectedSegment - segment + 12) % 12;
				if (step > 0) {
					selectedSegment = segment;
					top = (top + step) % 12;
					invalidate();
					owner.shift(step);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		default:
			if (selectedSegment > -1 && radiusSquared > R0 * R0 && radiusSquared < 1) {
				owner.endChord();
			}
			selectedSegment = -1;
			invalidate();
			break;
		}
		return true;
	}
}
