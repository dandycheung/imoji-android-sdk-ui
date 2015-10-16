package com.imojiapp.imoji.sdk.ui.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.imojiapp.imoji.sdk.ui.R;
import com.imojiapp.imoji.sdk.ui.utils.animator.ArgbEvaluator;

/**
 * TODO: document your custom view class.
 */
public class DotDotView extends View {
    public static final String SELECTED_INDEX_BUNDLE_ARG_KEY = "SELECTED_INDEX_BUNDLE_ARG_KEY";

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
    private Dot[] mDots;

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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldw);

        if (oldw == 0 && oldh == 0) {
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


            mDots = new Dot[mNumDots];
            for (int i = 0; i < mNumDots; i++) {
                mDots[i] = new Dot(mUnselectedDotColor, centerX, centerY, mRadius, mPaint);
                centerX += mRadius * 2 + mDotMargin;
            }

            setIndex(mSelectedDotIndex);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mNumDots; i++) {
            mDots[i].onDraw(canvas);
        }
    }

    private float getDrawWidth() {
        return mNumDots * (mRadius * 2) + mDotMargin * (mNumDots - 1);
    }

    public void nextDot() {
        if (mSelectedDotIndex < mNumDots - 1) {
            setIndex(mSelectedDotIndex + 1);
        }
    }

    public void previousDot() {
        if (mSelectedDotIndex > 0) {
            setIndex(mSelectedDotIndex - 1);
        }
    }

    public void setIndex(int index) {
        if (mDots == null || mDots.length == 0) {
            if (index >= 0 && index < mNumDots) {
                mSelectedDotIndex = index;
            }
            return;
        }

        if (index >= 0 && index < mNumDots) {

            if (Build.VERSION.SDK_INT >= 21) {
                //animate the currently selected item back to gray
                ObjectAnimator.ofArgb(mDots[mSelectedDotIndex], "color", mSelectedDotColor, mUnselectedDotColor).start();
            } else if (Build.VERSION.SDK_INT >= 11) {
                ObjectAnimator animator = ObjectAnimator.ofInt(mDots[mSelectedDotIndex], "color", mSelectedDotColor, mUnselectedDotColor);
                animator.setEvaluator(ArgbEvaluator.getInstance());
                animator.start();
            } else {
                mDots[mSelectedDotIndex].setColor(mUnselectedDotColor);
            }

            mSelectedDotIndex = index;

            if (Build.VERSION.SDK_INT >= 21) {
                ObjectAnimator.ofArgb(mDots[mSelectedDotIndex], "color", mUnselectedDotColor, mSelectedDotColor).start();
            } else if (Build.VERSION.SDK_INT >= 11) {
                ObjectAnimator animator = ObjectAnimator.ofInt(mDots[mSelectedDotIndex], "color", mUnselectedDotColor, mSelectedDotColor);
                animator.setEvaluator(ArgbEvaluator.getInstance());
                animator.start();
            } else {
                mDots[mSelectedDotIndex].setColor(mSelectedDotColor);
            }
        }
    }

    class Dot {

        private int mColor;
        private float mCenterX;
        private float mCenterY;
        private float mRadius;
        private Paint mPaint;

        public Dot(int color, float centerX, float centerY, float radius, Paint paint) {
            mColor = color;
            mCenterX = centerX;
            mCenterY = centerY;
            mRadius = radius;
            mPaint = paint;
        }

        public void onDraw(Canvas canvas) {
            mPaint.setColor(mColor);
            canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
        }

        public int getColor() {
            return mColor;
        }

        public void setColor(int color) {
            mColor = color;
            invalidate();
        }

    }

}
