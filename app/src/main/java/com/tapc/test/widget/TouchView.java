package com.tapc.test.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class TouchView extends SurfaceView implements SurfaceHolder.Callback {
    private int mScreenHeight;
    private int mScreenWidth;
    private List<TouchWhere> mTouchList;
    private float mCurX = -1;
    private float mCurY = -1;
    private Paint mPaintStroke;
    private Paint mPaintRect;
    private Paint mPaintCircle;

    // 曲线
    private float mX;
    private float mY;
    private SurfaceHolder sfh;
    private Canvas canvas;
    private float mCurveEndX;
    private float mCurveEndY;

    private final Paint mGesturePaint = new Paint();
    private final Path mPath = new Path();
    private final Rect mInvalidRect = new Rect();

    private boolean isDrawing;

    public TouchView(Context context) {
        super(context);
    }

    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchList = new ArrayList<TouchWhere>();

        mPaintStroke = new Paint();
        mPaintStroke.setColor(Color.BLACK);
        mPaintStroke.setStyle(Paint.Style.STROKE);

        mPaintRect = new Paint();
        mPaintRect.setColor(Color.BLACK);
        mPaintRect.setStyle(Paint.Style.FILL);

        mPaintCircle = new Paint();
        mPaintCircle.setColor(Color.RED);
        mPaintCircle.setStyle(Paint.Style.FILL);

        sfh = this.getHolder();
        sfh.addCallback(this);
        mGesturePaint.setAntiAlias(true);
        mGesturePaint.setStyle(Style.STROKE);
        mGesturePaint.setStrokeWidth(5);
        mGesturePaint.setColor(Color.RED);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mScreenWidth = w;
        mScreenHeight = h;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_UP) {
            if (mTouchList != null) {
                TouchWhere touchWhere = new TouchWhere();
                touchWhere.x = (int) event.getX();
                touchWhere.y = (int) event.getY();
                mTouchList.add(touchWhere);
                invalidate();
            }
            if (event.getAction() != MotionEvent.ACTION_UP) {
                mCurX = (int) event.getX();
                mCurY = (int) event.getY();
            } else {
                mCurX = -1;
                mCurY = -1;
            }
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isDrawing) {
                    Rect rect = touchMove(event);
                    if (rect != null) {
                        invalidate(rect);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isDrawing) {
                    touchUp(event);
                    invalidate();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private class TouchWhere {
        int x;
        int y;
    }

    public void drawCanvas() {
        try {
            canvas = sfh.lockCanvas();
            canvas.drawColor(Color.WHITE);
            if (canvas != null) {
                // 画列分割线
                int colCount = 15;
                float mCol = mScreenWidth / colCount;
                for (int i = 0; i < colCount; i++) {
                    canvas.drawLine(mCol * i, 0, mCol * i, mScreenHeight, mPaintStroke);
                }
                // 画行分割线
                int rowCount = 8;
                float mRow = mScreenHeight / rowCount;
                for (int j = 1; j < rowCount; j++) {
                    canvas.drawLine(0, mRow * j, mScreenWidth, mRow * j, mPaintStroke);
                }

                for (TouchWhere index : mTouchList) {
                    int xCount = (int) (index.x / mCol);
                    int yCount = (int) (index.y / mRow);
                    if (xCount == (colCount - 1) && yCount == (rowCount - 1)) {
                        canvas.drawRect(xCount * mCol, yCount * mRow, mScreenWidth, mScreenHeight, mPaintRect);
                    }
                    if (xCount == (colCount - 1)) {
                        canvas.drawRect(xCount * mCol, yCount * mRow, mScreenWidth, (yCount + 1) * mRow, mPaintRect);
                    } else if (yCount == (rowCount - 1)) {
                        canvas.drawRect(xCount * mCol, yCount * mRow, (xCount + 1) * mCol, mScreenHeight, mPaintRect);
                    } else {
                        canvas.drawRect(xCount * mCol, yCount * mRow, (xCount + 1) * mCol, (yCount + 1) * mRow,
                                mPaintRect);
                    }
                }
//                 for (TouchWhere index : mTouchList) {
//                 canvas.drawCircle(index.x, index.y, 5, mPaintCircle);
//                 }
//				if (mCurX != -1 && mCurY != -1) {
//					canvas.drawCircle(mCurX, mCurY, 15, mPaintCircle);
//				}
//                canvas.drawPath(mPath, mGesturePaint);
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (canvas != null) {
                sfh.unlockCanvasAndPost(canvas);
                canvas = null;
            }
        }
    }

    private void touchDown(MotionEvent event) {
        isDrawing = true;
        // mPath.reset();
        float x = event.getX();
        float y = event.getY();

        mX = x;
        mY = y;

        mPath.moveTo(x, y);

        mInvalidRect.set((int) x, (int) y, (int) x, (int) y);
        mCurveEndX = x;
        mCurveEndY = y;

        mCurX = x;
        mCurY = y;
        drawCanvas();
    }

    private Rect touchMove(MotionEvent event) {
        Rect areaToRefresh = null;

        final float x = event.getX();
        final float y = event.getY();

        final float previousX = mX;
        final float previousY = mY;

        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);

        if (dx >= 3 || dy >= 3) {
            areaToRefresh = mInvalidRect;
            areaToRefresh.set((int) mCurveEndX, (int) mCurveEndY, (int) mCurveEndX, (int) mCurveEndY);

            // 设置贝塞尔曲线的操作点为起点和终点的一半
            float cX = mCurveEndX = (x + previousX) / 2;
            float cY = mCurveEndY = (y + previousY) / 2;

            // 实现绘制贝塞尔平滑曲线；previousX, previousY为操作点，cX, cY为终点
            mPath.quadTo(previousX, previousY, cX, cY);
            // mPath.lineTo(x, y);

            // union with the control point of the new curve
            /*
             * areaToRefresh矩形扩大了border(宽和高扩大了两倍border)， border值由设置手势画笔粗细值决定
			 */
            areaToRefresh.union((int) previousX, (int) previousY, (int) previousX, (int) previousY);
            /*
             * areaToRefresh.union((int) x, (int) y, (int) x, (int) y);
			 */

            // union with the end point of the new curve
            areaToRefresh.union((int) cX, (int) cY, (int) cX, (int) cY);

            // 第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
            mX = x;
            mY = y;
            mCurX = x;
            mCurY = y;
            drawCanvas();
        }
        return areaToRefresh;
    }

    private void touchUp(MotionEvent event) {
        isDrawing = false;
        mCurX = -1;
        mCurY = -1;
        drawCanvas();
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        drawCanvas();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO Auto-generated method stub

    }
}
