package com.example.sikang.rotateview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by SiKang on 2016/3/2.
 */
public class CircleLayout extends ViewGroup {
    private final String TAG = "CircleLayoutDebug";
    private int mWidth;
    private int mHeight;
    private double mLayoutRadius;
    private PointF mCenterPoint;

    public CircleLayout(Context context) {
        super(context);
        init();
    }

    public CircleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mCenterPoint = new PointF();
    }

    @Override
    public void onViewAdded(View child) {
        if (getChildCount() <= 12) {
            super.onViewAdded(child);
        } else {
            removeViewAt(getChildCount() - 1);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = measureHanlder(widthMeasureSpec);
        mHeight = measureHanlder(heightMeasureSpec);
        mCenterPoint.x = mWidth / 2;
        mCenterPoint.y = mHeight / 2;
        mLayoutRadius = mWidth < mHeight ? mWidth / 2 : mHeight / 2;
        Log.d(TAG, "onMeasure()");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!changed)
            return;
        setChildLayout();
    }

    //设置所有子View位置
    private void setChildLayout() {
        int childCount = getChildCount();
        double childAngle = 360 / childCount;
        double layoutAngle = 270;
        double rotateAngle = 0;
        Log.d(TAG, "onLayout()  " + childAngle);
        for (int i = 0; i < childCount; i++) {
            //得到子View
            TouchView childView = (TouchView) getChildAt(i);
            //为子View设置宽高
            int childHalfWidth = (int) (getDimension(R.dimen.x100) / 2);
            int childHalfHeight = (int) (getDimension(R.dimen.x100) / 2);
            Log.d("RotateViewDebug", "fathre onLayout" + childHalfWidth);
            //计算半径
            double radius = mLayoutRadius - (childHalfWidth < childHalfHeight ? childHalfWidth : childHalfHeight);
            //计算元素的坐标
            Point viewPoint = getPoint(mCenterPoint, layoutAngle, radius);
            //初始化必要参数
            childView.setmParentCenterPoint(mCenterPoint);
            childView.setmPointInParent(viewPoint);
            childView.setNowAngle(layoutAngle);
            childView.initMatrix((float) rotateAngle, childHalfWidth * 2, childHalfHeight * 2);
            //设置子View的位置
            childView.layout(viewPoint.x - childHalfWidth, viewPoint.y - childHalfHeight, viewPoint.x + childHalfWidth, viewPoint.y + childHalfHeight);
            //更新角度
            layoutAngle = (layoutAngle + childAngle) % 360;
            rotateAngle = (rotateAngle + childAngle) % 360;
        }
    }

    @Override
    public void onViewRemoved(View child) {
        TouchView view = (TouchView) child;
        view.destory();
        if (view.isNormalDestory()) {
            setChildCount(getChildCount());
        }
        super.onViewRemoved(child);
    }

    public void setChildCount(int count) {
        removeAllViews();
        for (int i = 0; i < count; i++) {
            addView(new TouchView(getContext()));
        }
        setChildLayout();
    }

    /**
     * 根据圆心和角度计算下一个view的位置
     */
    private Point getPoint(PointF center, double angle, double radius) {
        Point point = new Point();
        double angleHude = angle * Math.PI / 180;
        point.x = (int) (radius * Math.cos(angleHude) + center.x);
        point.y = (int) (radius * Math.sin(angleHude) + center.y);
        return point;
    }

    //处理Spec
    private int measureHanlder(int measureSpec) {

        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(100, specSize);
        } else {
            result = 100;
        }
        return result;
    }

    private float getDimension(int id) {
        return getResources().getDimension(id);

    }


}
