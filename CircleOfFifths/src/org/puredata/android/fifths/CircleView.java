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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;

public final class CircleView extends View {

	private static enum State { UP, MAJOR, MINOR, SHIFT };
	private static final String[] notesSharp = { "C", "C\u266f", "D", "D\u266f", "E", "F", "F\u266f", "G", "G\u266f", "A", "A\u266f", "B" };
	private static final String[] notesFlat  = { "C", "D\u266d", "D", "E\u266d", "E", "F", "G\u266d", "G", "A\u266d", "A", "B\u266d", "B" };
	private static final int[] shifts =        {  0,   -5,   2,   -3,   4,   -1,  6,    1,   -4,   3,   -2,   5  };
	private static final float R0 = 25;
	private static final float R2 = 92;
	private static final float R1 = FloatMath.sqrt((R0 * R0 + R2 * R2) / 2);  // equal area for major and minor fields
	
	private CircleOfFifths owner;
	private int top = 0;
	private float xCenter, yCenter, xNorm, yNorm;
	private int selectedSegment;
	private State currentState = State.UP;
	private Bitmap keySigs[];
	private Bitmap wheel = null;
	private Bitmap grid = null;
	private Bitmap shadow = null;
	private RectF innerFrame, outerFrame;
	private final Path minorField = new Path();
	private final Path majorField = new Path();
	private final Path rimField = new Path();
	private final Paint labelPaint = new Paint();
	private final Paint selectedPaint = new Paint();

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

	public void setOwner(CircleOfFifths owner) {
		this.owner = owner;
	}

	public void setTopSegment(int top) {
		this.top = top;
		invalidate();
	}

	private void init() {
		RectF r0 = new RectF(-R0, -R0, R0, R0);
		RectF r1 = new RectF(-R1, -R1, R1, R1);
		RectF r2 = new RectF(-R2, -R2, R2, R2);
		outerFrame = new RectF(-100, -100, 100, 100);
		float dy = R0 / 1.8f;
		float dx = dy * 1.38f;
		innerFrame = new RectF(-dx, -dy, dx, dy);
		float phi = 255, dphi = 30;
		
		minorField.arcTo(r1, phi, dphi, true);
		minorField.arcTo(r0, phi+dphi, -dphi);
		minorField.close();
		minorField.setFillType(FillType.WINDING);
		
		majorField.arcTo(r2, phi, dphi, true);
		majorField.arcTo(r1, phi+dphi, -dphi);
		majorField.close();
		majorField.setFillType(FillType.WINDING);
		
		rimField.arcTo(outerFrame, phi, dphi, true);
		rimField.arcTo(r2, phi+dphi, -dphi);
		rimField.close();
		rimField.setFillType(FillType.WINDING);
		
		selectedPaint.setAntiAlias(true);
		selectedPaint.setColor(Color.RED);
		selectedPaint.setStyle(Paint.Style.FILL);
		
		labelPaint.setAntiAlias(true);
		labelPaint.setColor(Color.BLACK);
		labelPaint.setTextAlign(Paint.Align.CENTER);
		labelPaint.setTypeface(Typeface.MONOSPACE);
		labelPaint.setTextSize(16);

		Resources res = getResources();
		keySigs = new Bitmap[] {
				BitmapFactory.decodeResource(res, R.drawable.ks00), BitmapFactory.decodeResource(res, R.drawable.ks01), 
				BitmapFactory.decodeResource(res, R.drawable.ks02), BitmapFactory.decodeResource(res, R.drawable.ks03), 
				BitmapFactory.decodeResource(res, R.drawable.ks04), BitmapFactory.decodeResource(res, R.drawable.ks05), 
				BitmapFactory.decodeResource(res, R.drawable.ks06), BitmapFactory.decodeResource(res, R.drawable.ks07), 
				BitmapFactory.decodeResource(res, R.drawable.ks08), BitmapFactory.decodeResource(res, R.drawable.ks09), 
				BitmapFactory.decodeResource(res, R.drawable.ks10), BitmapFactory.decodeResource(res, R.drawable.ks11)
		};
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
		return (mode == MeasureSpec.UNSPECIFIED) ? 320 : size;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.translate(xCenter, yCenter);
		canvas.scale(xCenter * 0.01f, yCenter * 0.01f);
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(-top * 30);
		canvas.drawBitmap(wheel, null, outerFrame, null);
		canvas.restore();
		int c = (top * 7) % 12;
		canvas.drawBitmap(keySigs[c], null, innerFrame, null);
		int s0 = shifts[c];
		for (int i = 0; i < 12; i++) {
			if (i == selectedSegment) {
				if (currentState == State.MAJOR) {
					canvas.drawPath(majorField, selectedPaint);
				}
				else if (currentState == State.MINOR) {
					canvas.drawPath(minorField, selectedPaint);
				}
				else if (currentState == State.SHIFT) {
					canvas.drawPath(rimField, selectedPaint);
				}
			}
			int s1 = s0 + i;
			if (i > 6) s1 -= 12;
			String label = (s1 >= 0) ? notesSharp[c] : notesFlat[c];
			drawLabel(canvas, label, (R1 + R2) / 2);
			c = (c + 9) % 12;
			label = (s1 >= 0) ? notesSharp[c] : notesFlat[c];
			drawLabel(canvas, label.toLowerCase(), (R0 + R1) / 2);
			c = (c + 10) % 12;
			canvas.rotate(30);
		}
		canvas.drawBitmap(grid, null, outerFrame, null);
		canvas.drawBitmap(shadow, null, outerFrame, null);
	}

	private void drawLabel(Canvas canvas, String label, float r) {
		float d = labelPaint.getTextSize() / 3f - r;
		if (label.length() > 1) {
			// ugly hack to work around unicode spacing problem
			canvas.drawText(label.charAt(0) + " ", 0, d, labelPaint);
			canvas.drawText(" " + label.charAt(1), 0, d, labelPaint);
		} else {
			canvas.drawText(label, 0, d, labelPaint);				
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		xCenter = w / 2;
		xNorm = 100 / xCenter;
		yCenter = h / 2;
		yNorm = 100 / yCenter;
		drawBitmaps(w, h);
	}

	private void drawBitmaps(int w, int h) {
		if (wheel != null) {
			wheel.recycle();
			shadow.recycle();
			grid.recycle();
		}
		wheel = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		shadow = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		grid  = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas();
		canvas.translate(xCenter, yCenter);
		canvas.scale(xCenter * 0.01f, yCenter * 0.01f);
		
		Paint shades[] = new Paint[4];
		for (int i = 0; i < 4; i++) {
			int c = 0x98 + 0x10 * i;
			shades[i] = new Paint();
			shades[i].setStyle(Paint.Style.FILL);
			shades[i].setColor(Color.argb(0xff, c, c, c));
		}
		Paint centerShade = new Paint(shades[0]);
		centerShade.setColor(Color.argb(0xff, 0x78, 0x78, 0x78));
		Paint shadowPaint = new Paint();
		shadowPaint.setShader(new LinearGradient(0, -100, 0, 100, 
				new int[] { 0x00ffffff, 0x77000000 }, null, TileMode.CLAMP));
		Paint gridPaint = new Paint();
		gridPaint.setAntiAlias(true);
		gridPaint.setStrokeWidth(1.6f);
		gridPaint.setStyle(Paint.Style.STROKE);
		gridPaint.setColor(Color.DKGRAY);
		
		canvas.setBitmap(shadow);
		canvas.drawCircle(0, 0, 100, shadowPaint);
		
		canvas.setBitmap(wheel);
		canvas.drawCircle(0, 0, R0, centerShade);
		for (int i = 0; i < 12; i++) {
			Paint shade = shades[i % 4];
			canvas.drawPath(minorField, shade);
			canvas.drawPath(majorField, shade);
			canvas.drawPath(rimField, shade);
			canvas.rotate(30);
		}
		
		canvas.setBitmap(grid);
		canvas.drawCircle(0, 0, R0, gridPaint);
		canvas.drawCircle(0, 0, R1, gridPaint);
		canvas.drawCircle(0, 0, R2, gridPaint);
		canvas.drawCircle(0, 0, 100, gridPaint);
		canvas.rotate(15);
		for (int i = 0; i < 12; i++) {
			canvas.drawLine(0, R0, 0, 100, gridPaint);
			canvas.rotate(30);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = (event.getX() - xCenter) * xNorm;
		float y = (event.getY() - yCenter) * yNorm;
		float radiusSquared = x * x + y * y;
		float angle = (float) (Math.atan2(x, -y) * 6 / Math.PI);
		int segment = (int) (angle + 12.5f) % 12;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (radiusSquared >= R0 * R0) {
				selectedSegment = segment;
				if (radiusSquared >= R2 * R2) {
					currentState = State.SHIFT;
					owner.setTop(top);
				} else {
					int note = (top * 7 + segment * 7) % 12;
					if (radiusSquared >= R1 * R1) {
						currentState = State.MAJOR;
						owner.playChord(true, note);
					} else {
						currentState = State.MINOR;
						note = (note + 9) % 12;
						owner.playChord(false, note);
					}
				}
				invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (currentState == State.SHIFT && radiusSquared >= R0 * R0) {
				int step = (selectedSegment - segment + 12) % 12;
				if (step > 0) {
					top = (top + step) % 12;
					invalidate();
					owner.setTop(top);
				}
				selectedSegment = segment;
			}
			break;
		case MotionEvent.ACTION_UP:
		default:
			if (currentState == State.MAJOR || currentState == State.MINOR) {
				owner.endChord();
			}
			currentState = State.UP;
			invalidate();
			break;
		}
		return true;
	}
}
