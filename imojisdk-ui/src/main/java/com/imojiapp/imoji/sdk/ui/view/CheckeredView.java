package com.imojiapp.imoji.sdk.ui.view;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.imojiapp.imoji.sdk.ui.R;

/**
 * Creates a checkered background
 */
public class CheckeredView extends View {

    private static final int DEFAULT_CHECKER_SIZE = 16;

    private int mFirstColor;
    private int mSecondColor;
    private int mSize;
    private Paint mPaint;
    private int mContentWidth;
    private int mContentHeight;

    public CheckeredView(Context context) {
        super(context);
        init(null, 0);
    }

    public CheckeredView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CheckeredView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ImojiCheckeredView, defStyle, 0);

        mFirstColor = a.getColor(R.styleable.ImojiCheckeredView_imoji__firstCheckerColor, Color.BLUE);
        mSecondColor = a.getColor(R.styleable.ImojiCheckeredView_imoji__secondCheckerColor, Color.CYAN);
        mSize = a.getDimensionPixelSize(R.styleable.ImojiCheckeredView_imoji__checkerSize, DEFAULT_CHECKER_SIZE);

        //make sure that the size is even
        mSize = ((float) mSize / 2f) == 0 ? mSize : mSize + 1;

        a.recycle();

        createShaderPaint();
    }

    private void createShaderPaint(){
        Bitmap bm = Bitmap.createBitmap(mSize, mSize, Bitmap.Config.ARGB_8888);
        for(int i = 0; i < mSize; i++){
            for(int j = 0; j < mSize; j++){
                if( (i < mSize / 2 && j < mSize / 2) || (i >= mSize / 2 && j >= mSize / 2) ) {
                    bm.setPixel(i, j, mFirstColor);
                }else{
                    bm.setPixel(i, j, mSecondColor);
                }
            }
        }

        BitmapShader shader = new BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        mPaint = new Paint();
        mPaint.setShader(shader);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        mContentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        mContentHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        // in your draw method
        canvas.drawRect(getPaddingLeft(), getPaddingTop(), mContentWidth, mContentHeight, mPaint);

    }


}
