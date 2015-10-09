package com.imojiapp.imoji.sdk.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.imojiapp.imoji.sdk.ui.R;

/**
 * TODO: document your custom view class.
 */
public class DotDotView extends View {

    private float mRadius;
    private float mInnerRadius;
    private int mGravity;
    private float mDotMargin;
    private int mNumDots;
    private Paint mPaint;
    private int mSelectedDotIndex;
    private int mSelectedDotColor;
    private int mUnselectedDotColor;
    private boolean mTransitioning;

    public DotDotView(Context context) {
        super(context);
        init(null, 0);
    }

    public DotDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DotDotView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.DotDotView, defStyle, 0);
        mRadius = a.getDimension(R.styleable.DotDotView_radius, getResources().getDimension(R.dimen.dot_dot_radius));
        mDotMargin = a.getDimension(R.styleable.DotDotView_dot_margin, getResources().getDimension(R.dimen.dot_dot_margin));
        mNumDots = a.getInteger(R.styleable.DotDotView_num_dots, 2);
        mSelectedDotColor = a.getColor(R.styleable.DotDotView_selected_dot_color, getResources().getColor(R.color.dotdotview_selected_color));
        mUnselectedDotColor = a.getColor(R.styleable.DotDotView_unselected_dot_color, getResources().getColor(R.color.dotdotview_unselected_color));

        mInnerRadius = 0.80f * mRadius;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mSelectedDotIndex = 0;


        a.recycle();

        a = getContext().obtainStyledAttributes(new int[]{android.R.attr.gravity});
        mGravity = a.getInteger(0, Gravity.CENTER);
        a.recycle();

    }

    @Override
    protected void onDraw(Canvas canvas) {

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        Rect contentRect = new Rect(paddingLeft, paddingTop, paddingLeft + contentWidth, paddingTop + contentHeight);

        float drawWidth = getDrawWidth();
        float drawHeight = mRadius * 2;
        float centerX = contentRect.centerX() - drawWidth / 2 + mRadius;
        float centerY = contentRect.centerY();


        for (int i = 0; i < mNumDots; i++) {
            //start drawing
            if (i == mSelectedDotIndex) { //draw a filled circle
                mPaint.setColor(mSelectedDotColor);
            } else {
                mPaint.setColor(mUnselectedDotColor);
            }

            canvas.drawCircle(centerX, centerY, mRadius, mPaint);

            //update draw locations
            centerX += mRadius * 2 + mDotMargin;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        float drawWidth = getDrawWidth() + mPaint.getStrokeWidth() * 2 + getPaddingLeft() + getPaddingRight();
        float drawHeight = mRadius * 2 + mPaint.getStrokeWidth() * 2 + getPaddingTop() + getPaddingBottom();

        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                width = Math.min((int) drawWidth, width);
                break;
            case MeasureSpec.UNSPECIFIED:
                width = getSuggestedMinimumWidth();
                break;
        }

        switch (heightMode) {
            case MeasureSpec.AT_MOST:
                height = Math.min((int) drawHeight, height);
                break;
            case MeasureSpec.UNSPECIFIED:
                height = getSuggestedMinimumHeight();
                break;
        }

        setMeasuredDimension(width, height);
    }

    private float getDrawWidth() {
        return mNumDots * (mRadius * 2) + mDotMargin * (mNumDots - 1);
    }

    public void nextDot() {
        if (mSelectedDotIndex < mNumDots - 1) {
            ++mSelectedDotIndex;
            invalidate();
        }
    }

    public void previousDot() {
        if (mSelectedDotIndex > 0) {
            --mSelectedDotIndex;
            invalidate();
        }
    }

    public void setIndex(int index) {
        if (index >= 0 && index < mNumDots) {
            mSelectedDotIndex = index;
            invalidate();
        }
    }

}
