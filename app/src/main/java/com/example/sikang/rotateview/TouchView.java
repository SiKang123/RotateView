package com.example.sikang.rotateview;

/**
 * Created by SiKang on 2016/3/1.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.example.sikang.util.Constants;
import com.example.sikang.util.TouchActionController;
import com.example.sikang.util.ViewTouchActionListener;


class TouchView extends View implements ViewTouchActionListener {
    private final String TAG = "RotateViewDebug";
    private int mWidth, mHeight, mSrcWidth, mSrcHeight;
    private boolean mEndle, isSrcFilp, normalDestory;//编辑状态`
    private Paint mLinePaint, mButtonPaint, mTextPaint, mSrcPaint;
    private Path mPath;
    private Point mCancleBtnPoint, mMoveBtnPoint, mRotateBtnPoint, mScaleBtnPoint, mPointInParent;//按钮坐标
    private PointF mCenterPoint, mParentCenterPoint;
    private float mButtonRadius, srcAngle, srcScale;
    private double nowAngle;
    private int mButtonCode, mTouchSlop, mLeft, mTop, mRight, mBottom;
    private Bitmap mSrcBm, mCancleBm, mMoveBm, mRotateBm, mScaleBm;
    private Matrix mSrcMatrix;
    private TouchActionController mActionController;

    public TouchView(Context context) {
        super(context);
        init(context);
    }

    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void init(Context context) {
        mActionController = TouchActionController.getInstance();
        mActionController.registerTouchActionListener(this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mEndle = false;
        mLinePaint = new Paint();
        mButtonPaint = new Paint();
        mTextPaint = new Paint();
        mSrcPaint = new Paint();
        mPath = new Path();
        //init path
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStrokeWidth(4);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mButtonPaint.setAntiAlias(true);
        mButtonPaint.setColor(Color.BLUE);
        mSrcPaint.setAlpha(30);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(10);

        //init Point
        mCancleBtnPoint = new Point();
        mRotateBtnPoint = new Point();
        mMoveBtnPoint = new Point();
        mScaleBtnPoint = new Point();
        mCenterPoint = new PointF();
        startPoint = new PointF();
        endPoint = new PointF();
        startRawPoint = new PointF();
        startForParentPoint = new PointF();
        endForParentPoint = new PointF();
        //init Bitmap
        Resources res = context.getResources();
        mCancleBm = zoomImage(BitmapFactory.decodeResource(res, R.mipmap.cancle), getDimension(R.dimen.x16), getDimension(R.dimen.x16));
        mRotateBm = zoomImage(BitmapFactory.decodeResource(res, R.mipmap.rotate), getDimension(R.dimen.x16), getDimension(R.dimen.x16));
        mMoveBm = zoomImage(BitmapFactory.decodeResource(res, R.mipmap.move), getDimension(R.dimen.x16), getDimension(R.dimen.x16));
        mScaleBm = zoomImage(BitmapFactory.decodeResource(res, R.mipmap.scale), getDimension(R.dimen.x16), getDimension(R.dimen.x16));
        mButtonRadius = getDimension(R.dimen.x8);
        mSrcMatrix = new Matrix();
        mButtonCode = Constants.ACTION_VIEW_CLICK;

        nowTranX = getTranslationX();
        nowTranY = getTranslationY();
        setSrc(1);
        isSrcFilp = false;
        normalDestory = false;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWidth = right - left;
        mHeight = bottom - top;
        resetPoint((int) mButtonRadius);
        super.onLayout(changed, left, top, right, bottom);
    }

    //init matrix
    public void initMatrix(float angle, float width, float height) {
        if (mSrcBm != null) {
            this.srcAngle = angle;
            mSrcBm = Bitmap.createBitmap(mSrcBm, 0, 0, mSrcBm.getWidth(), mSrcBm.getHeight(), mSrcMatrix, false);
            srcScale = width < height ? width / (float) mSrcWidth : height / (float) mSrcHeight;
            setMatrix(width / 2, height / 2);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSrcBm != null) {
            //绘制主图
            canvas.drawBitmap(mSrcBm, mSrcMatrix, mSrcPaint);
        }
        if (mEndle) {
            //绘制编辑框
            mPath.reset();
            mPath.moveTo(mCancleBtnPoint.x, mCancleBtnPoint.y);
            mPath.lineTo(mMoveBtnPoint.x, mMoveBtnPoint.y);
            mPath.lineTo(mScaleBtnPoint.x, mScaleBtnPoint.y);
            mPath.lineTo(mRotateBtnPoint.x, mRotateBtnPoint.y);
            mPath.lineTo(mCancleBtnPoint.x, mCancleBtnPoint.y);
            canvas.drawPath(mPath, mLinePaint);

            //绘制功能按钮
            canvas.drawBitmap(mCancleBm, 0, 0, mButtonPaint);

            canvas.drawBitmap(mMoveBm, 0, mHeight - mButtonRadius * 2, mButtonPaint);

            canvas.drawBitmap(mScaleBm, mWidth - mButtonRadius * 2, mHeight - mButtonRadius * 2, mButtonPaint);

            canvas.drawBitmap(mRotateBm, mWidth - mButtonRadius * 2, 0, mButtonPaint);

        }

    }

    private PointF startPoint, startRawPoint, endPoint, startForParentPoint, endForParentPoint;
    private float nowTranX, nowTranY;
    private boolean isClick;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float moveX;
        float moveY;
        switch (action) {
            //判断触点坐标所属区域（旋转、移动、缩放、取消、内容）
            case MotionEvent.ACTION_DOWN:
                isClick = true;
                //记录相对坐标
                startPoint.x = event.getX();
                startPoint.y = event.getY();
                //记录绝对坐标
                startRawPoint.x = event.getRawX();
                startRawPoint.y = event.getRawY();

                startForParentPoint.x = mCenterPoint.x + nowTranX + getLeft();
                startForParentPoint.y = mCenterPoint.y + nowTranY + getTop();
                mButtonCode = whichOneTouched(event);
                updateLayout();
                mActionController.notifyListener(this, Constants.ACTION_SCALE_FINISHED);
                break;
            //移动手指时
            case MotionEvent.ACTION_MOVE:
                moveX = event.getRawX() - startRawPoint.x;
                moveY = event.getRawY() - startRawPoint.y;
                if (Math.abs(moveX) > 10 || Math.abs(moveY) > 10) {
                    isClick = false;
                }
                switch (mButtonCode) {
                    //操作旋转按钮时，旋转
                    case Constants.ACTION_ROTATE:
                        endPoint.x = event.getX();
                        endPoint.y = event.getY();
                        //得到目标角度
                        float angle = (this.getRotation() + getAngle(mCenterPoint, startPoint, endPoint)) % 360;
                        setRotation(angle);
                        //通知其他View
                        mActionController.notifyListener(this, Constants.ACTION_ROTATE, angle);
                        break;
                    //操作缩放按钮时
                    case Constants.ACTION_SCALE:
                        //计算缩放比例
                        int moveWidth = (int) (moveX > moveY ? moveX : moveY);
                        updateScale(moveWidth);
                        mActionController.notifyListener(this, Constants.ACTION_SCALE, moveWidth);
                        break;
                    case Constants.ACTION_MOVE:
                        //计算开始和结束的触摸点坐标，设置偏移量
                        endForParentPoint.x = startForParentPoint.x + moveX;
                        endForParentPoint.y = startForParentPoint.y + moveY;
                        double moveAngle = getAngle(mParentCenterPoint, startForParentPoint, endForParentPoint) % 360;
                        double radius = pointDistance(mParentCenterPoint, endForParentPoint);
                        updatePoint(moveAngle, radius);
                        //通知其他view
                        mActionController.notifyListener(this, Constants.ACTION_MOVE, moveAngle, radius);
                        startRawPoint.x = event.getRawX();
                        startRawPoint.y = event.getRawY();
                        startForParentPoint.x = endForParentPoint.x;
                        startForParentPoint.y = endForParentPoint.y;
                        break;
                }

                break;
            //抬起手指
            case MotionEvent.ACTION_UP:
                moveX = event.getRawX() - startRawPoint.x;
                moveY = event.getRawY() - startRawPoint.y;
                switch (mButtonCode) {
                    //从布局中删除当前View
                    case Constants.ACTION_CANCLE:
                        if (Math.abs(moveX) < mTouchSlop || Math.abs(moveY) < mTouchSlop)
                            normalDestory = true;
                        ((ViewGroup) getParent()).removeView(this);
                        break;
                    case Constants.ACTION_MOVE:
                        if (isClick) {
                            //如果是点击则显示编辑框
                            mEndle = !mEndle;
                            if (mEndle) {
                                mActionController.notifyListener(this, Constants.ACTION_UNEDIT);
                                bringToFront();
                            }
                            invalidate();
                        } else {
                            nowTranX = getTranslationX();
                            nowTranY = getTranslationY();
                        }
                        break;
                    case Constants.ACTION_FLIP:
                        //镜像翻转
                        if (Math.abs(moveX) < mTouchSlop || Math.abs(moveY) < mTouchSlop) {
                            isSrcFilp = !isSrcFilp;
                            setMatrix(mCenterPoint.x, mCenterPoint.y);
                            invalidate();
                            mActionController.notifyListener(this, Constants.ACTION_FLIP);
                        }
                    case Constants.ACTION_SCALE:
                        //记录最新Layout值
                        updateLayout();
                        mActionController.notifyListener(this, Constants.ACTION_SCALE_FINISHED);
                        break;
                }
                break;
        }
        return true;
    }

    /**
     *  指定bitmap大小
     */
    public Bitmap zoomImage(Bitmap bitmap, double newWidth,
                            double newHeight) {
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, (int) width,
                (int) height, matrix, true);
    }

    //设置缩放值
    private void updateScale(int moveWidth) {
        if (mWidth < getDimension(R.dimen.x50) || mHeight < getDimension(R.dimen.x50)) {
            if (moveWidth <= 0)
                return;
        }
        layout(mLeft - moveWidth, mTop - moveWidth, mRight + moveWidth, mBottom + moveWidth);
        srcScale = mWidth < mHeight ? (float) mWidth / (float) mSrcWidth : (float) mHeight / (float) mSrcHeight;
        setMatrix(mCenterPoint.x, mCenterPoint.y);
    }


    //更新布局参数
    private void updateLayout() {
        mLeft = getLeft();
        mRight = getRight();
        mTop = getTop();
        mBottom = getBottom();
    }

    private void setMatrix(float x, float y) {
        mSrcMatrix.setScale(srcScale, srcScale);
        if (isSrcFilp) {
            mSrcMatrix.postScale(-1, 1);
            mSrcMatrix.postTranslate(x * 2, 0);
            isSrcFilp = true;
        }
        mSrcMatrix.postRotate(srcAngle, x, y);
    }

    /**
     *  判断触摸点在哪个区域，返回ButtonCode
     */
    private int whichOneTouched(MotionEvent event) {
        if (!mEndle) {
            return Constants.ACTION_MOVE;
        }
        int buttonCode = Constants.ACTION_MOVE;
        float x = event.getX();
        float y = event.getY();
        if (x < mButtonRadius * 2) {
            if (y < mButtonRadius * 2) {
                buttonCode = Constants.ACTION_CANCLE;
            } else if (y > mHeight - (mButtonRadius * 2)) {
                buttonCode = Constants.ACTION_FLIP;

            }
        } else if (x > mWidth - (mButtonRadius * 2)) {
            if (y < mButtonRadius * 2) {
                buttonCode = Constants.ACTION_ROTATE;
            } else if (y > mHeight - (mButtonRadius * 2)) {
                buttonCode = Constants.ACTION_SCALE;
            }
        }
        return buttonCode;
    }


    /**
     * 计算两点之间的距离
     */
    private double pointDistance(PointF p1, PointF p2) {
        float x = p2.x - p1.x;
        float y = p2.y - p1.y;
        return Math.sqrt(x * x + y * y);
    }

    /**
     * 设置图片
     */
    public void setSrc(int id) {
        mSrcBm = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.img);
        mSrcWidth = mSrcBm.getWidth();
        mSrcHeight = mSrcBm.getHeight();
    }

    /**
     * 根据圆心和角度计算下一个view的位置
     */
    private void setNewPoint(PointF center, double angle, double radius) {
        double angleHude = angle * Math.PI / 180;
        mPointInParent.x = (int) (radius * Math.cos(angleHude) + center.x);
        mPointInParent.y = (int) (radius * Math.sin(angleHude) + center.y);
    }

    /**
     *  得到两点之间的角度
     */
    public float getAngle(PointF centerPoint, PointF start, PointF end) {
        //根据起始点和结束点以及View中心点求出三角形的三条边长
        double side1 = pointDistance(centerPoint, start);
        double side2 = pointDistance(start, end);
        double side3 = pointDistance(centerPoint, end);

        //得出弧度
        double cosb = (side1 * side1 + side3 * side3 - side2 * side2) / (2 * side1 * side3);
        if (cosb >= 1) {
            cosb = 1f;
        }
        double radian = Math.acos(cosb);

        //弧度换算为角度
        float angle = (float) (radian * 180 / Math.PI);

        float beforeX = start.x - centerPoint.x;
        float beforeY = start.y - centerPoint.y;
        float afterX = end.x - centerPoint.x;
        float afterY = end.y - centerPoint.y;
        //向量叉乘结果, 如果结果为负数， 表示为逆时针， 结果为正数表示顺时针
        float result = beforeX * afterY - beforeY * afterX;

        if (result < 0) {
            angle = -angle;
        }
        return angle;

    }

    /**
     *  更新当前View位置
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void updatePoint(double moveAngle, double radius) {
        nowAngle = (nowAngle + moveAngle) % 360;
        setNewPoint(mParentCenterPoint, nowAngle, radius);
        nowTranX = mPointInParent.x - getLeft() - mCenterPoint.x;
        nowTranY = mPointInParent.y - getTop() - mCenterPoint.y;
        setTranslationX(nowTranX);
        setTranslationY(nowTranY);
    }

    private float getDimension(int id) {
        return getResources().getDimension(id);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onTouchAction(int ACTION_ID, Object... args) {
        switch (ACTION_ID) {
            case Constants.ACTION_ROTATE:
                setRotation((float) args[0]);
                break;
            case Constants.ACTION_MOVE:
                updatePoint((double) args[0], (double) args[1]);
                break;
            case Constants.ACTION_UNEDIT:
                if (mEndle) {
                    mEndle = false;
                    invalidate();
                }
                break;
            case Constants.ACTION_SCALE:
                updateScale((Integer) args[0]);
                break;
            case Constants.ACTION_SCALE_FINISHED:
                updateLayout();
                break;
            case Constants.ACTION_FLIP:
                isSrcFilp = !isSrcFilp;
                setMatrix(mCenterPoint.x, mCenterPoint.y);
                invalidate();
                break;
        }
    }

    public void destory() {
        // 销毁时调用
        mActionController.unRegisterActionListener(this);
        if (mSrcBm != null && !mSrcBm.isRecycled()) {
            mSrcBm.recycle();
            mSrcBm = null;
        }
        if (mCancleBm != null && !mCancleBm.isRecycled()) {
            mCancleBm.recycle();
            mCancleBm = null;
        }
        if (mMoveBm != null && !mMoveBm.isRecycled()) {
            mMoveBm.recycle();
            mMoveBm = null;
        }
        if (mRotateBm != null && !mRotateBm.isRecycled()) {
            mRotateBm.recycle();
            mRotateBm = null;
        }
        if (mScaleBm != null && !mScaleBm.isRecycled()) {
            mScaleBm.recycle();
            mScaleBm = null;
        }
    }

    private void resetPoint(int radius) {
        mCancleBtnPoint.set(0 + radius, 0 + radius);
        mMoveBtnPoint.set(0 + radius, mHeight - radius);
        mScaleBtnPoint.set(mWidth - radius, mHeight - radius);
        mRotateBtnPoint.set(mWidth - radius, 0 + radius);
        mCenterPoint.set(mWidth / 2, mHeight / 2);
    }

    public void setmParentCenterPoint(PointF mParentCenterPoint) {
        this.mParentCenterPoint = mParentCenterPoint;
    }

    public void setmPointInParent(Point mPointInParent) {
        this.mPointInParent = mPointInParent;
    }

    public void setNowAngle(double nowAngle) {
        this.nowAngle = nowAngle;
    }

    public boolean isNormalDestory() {
        return normalDestory;
    }
}