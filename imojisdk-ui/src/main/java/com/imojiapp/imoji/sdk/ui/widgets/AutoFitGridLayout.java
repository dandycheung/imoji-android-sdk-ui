package com.imojiapp.imoji.sdk.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

/**
 * Created by sajjadtabib on 9/30/14.
 */
public class AutoFitGridLayout extends GridLayout {
    private static final String LOG_TAG = AutoFitGridLayout.class.getSimpleName();

    private boolean mFirstPass;

    public AutoFitGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mFirstPass = true;
    }

    public AutoFitGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitGridLayout(Context context) {
        this(context, null);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        mFirstPass = true;
        super.addView(child, index, params);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        if(mFirstPass){
            mFirstPass = false;
            int width = getMeasuredWidth();
            int columnCount = getColumnCount();

            //update each of the child views
            int column = 0; //currentColumn
            int row = 0;    //currentRow
            int spaceAvailable = width;

            int specWidth = spaceAvailable / columnCount; //The spec width

            int childCount = getChildCount();

            for(int i = 0; i < childCount; i++){
                View child = getChildAt(i);
                if(child.getVisibility() == GONE)
                    continue;

                int childWidth = child.getMeasuredWidth(); //get the child's measure width

                //figure out the spec & whether we have available space
                int spec = Math.min(Math.max((int)Math.ceil((float)childWidth / (float)specWidth), 1), columnCount);

                //do we have childWidth
                if(childWidth > spaceAvailable || (column + spec) > columnCount){
                    ++row;
                    column = 0;
                    spaceAvailable = width;
                }

                LayoutParams params = (LayoutParams)child.getLayoutParams();

                //column spec
                Spec columnSpec = GridLayout.spec(column, spec);
                params.columnSpec = columnSpec;

                //row spec
                Spec rowSpec = GridLayout.spec(row);
                params.rowSpec = rowSpec;

                child.setLayoutParams(params);

                //update the avialable space
                spaceAvailable -= childWidth;
                column += spec;

                if(column == columnCount){
                    column = 0; //reset the column to the first column
                    spaceAvailable = width; //reset the amount of space available
                    ++row; //increase the row number
                }

            }
            requestLayout();
        }
    }

}
