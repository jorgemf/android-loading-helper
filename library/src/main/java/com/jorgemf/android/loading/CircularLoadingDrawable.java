package com.jorgemf.android.loading;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

public class CircularLoadingDrawable extends Drawable {

	private static final int MAXIMUM_LEVEL = 10000;

	private float mProgress = 0;

	private static final int LINE_WIDTH_DP = 5;

	private Paint mPaint;

	private RectF mArcBounds;

	public CircularLoadingDrawable(Context context) {
		mPaint = new Paint();
		Resources resources = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, LINE_WIDTH_DP, resources.getDisplayMetrics());
		mPaint.setStrokeWidth(px);
		mPaint.setColor(Color.BLACK);
		mArcBounds = new RectF();
	}

	@Override
	public void draw(Canvas canvas) {
		Rect bounds = getBounds();
		int radius = Math.min(bounds.width(), bounds.height()) / 2;
		mArcBounds.set(bounds.centerX() - radius,
				bounds.centerY() - radius,
				bounds.centerX() + radius,
				bounds.centerY() + radius);
		canvas.drawArc(mArcBounds, 0f, 360 * mProgress, false, mPaint);
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return android.graphics.PixelFormat.OPAQUE;
	}

	@Override
	protected boolean onLevelChange(int level) {
		if (level >= 0) {
			mProgress = (float) level / MAXIMUM_LEVEL;
			invalidateSelf();
			return true;
		}
		return false;
	}
}
