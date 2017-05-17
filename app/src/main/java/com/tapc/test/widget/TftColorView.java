package com.tapc.test.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class TftColorView extends View {
	public static final int CHECK_ITEM = 5;
	private Activity mActivity;
	private int mCheckColorIndex = 0;

	public TftColorView(Context context) {
		super(context);
	}

	public TftColorView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init(Activity activity) {
		mActivity = activity;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mActivity == null) {
			return;
		}
		Rect viewRect = new Rect();
		mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(viewRect);
		TftColor tftColor = new TftColor();
		int c, w, h;
		switch (mCheckColorIndex) {
		case 0:
			canvas.drawColor(Color.RED);
			break;
		case 1:
			canvas.drawColor(Color.GREEN);
			break;
		case 2:
			canvas.drawColor(Color.BLUE);
			break;
		case 3:
			canvas.drawColor(Color.BLACK);
			break;
		case 4:
			canvas.drawColor(Color.WHITE);
			break;
		case 5:
			c = 255 / 16;
			w = (viewRect.right - viewRect.left) / 16;
			h = (viewRect.bottom - viewRect.top) / 4;
			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 4; j++) {
					Rect gridRect = new Rect();
					gridRect.left = viewRect.left + w * i;
					gridRect.right = gridRect.left + w;
					gridRect.top = viewRect.top + h * j;
					gridRect.bottom = gridRect.top + h;
					tftColor.r = ((j == 0) ? 1 : 0);
					tftColor.g = ((j == 1) ? 1 : 0);
					tftColor.b = ((j == 2) ? 1 : 0);
					if (j == 3) {
						tftColor.r = tftColor.g = tftColor.b = 1;
					}
					Paint paint = new Paint();
					paint.setColor(Color.rgb(tftColor.r * c * i, tftColor.g * c * i, tftColor.b * c * i));
					canvas.drawRect(gridRect, paint);
				}
			}
			break;
		default:
			break;
		}
	}

	public void nextTestColor() {
		mCheckColorIndex++;
		invalidate();
	}

	public int getIndex() {
		return mCheckColorIndex;
	}

	private class TftColor {
		int r;
		int g;
		int b;
	}
}
