package com.imojiapp.imoji.sdk.ui;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.imojiapp.imoji.sdk.BitmapUtils;
import com.imojiapp.imojigraphics.IG;
import com.imojiapp.imojigraphics.IGEditorView;

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImojiEditorFragment extends Fragment implements ViewTreeObserver.OnGlobalLayoutListener{
    private static final String LOG_TAG = ImojiEditorFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG = ImojiEditorFragment.class.getSimpleName();
    public static final String EDITOR_STATE_BUNDLE_ARG_KEY = "EDITOR_STATE_BUNDLE_ARG_KEY";
    private Bitmap mPreScaleBitmap;
    private IGEditorView mIGEditorView;
    private BitmapRetainerFragment mBitmapRetainerFragment;
    private boolean mIsTagging;
    private int mWidthBound = 0;
    private int mHeightBound = 0;
    private Handler mHandler = new Handler();
    private ImageButton mUndoButton;
    private ImageButton mTagButton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setEditorBitmap(Bitmap bitmap) {
        mPreScaleBitmap = bitmap;
        launchBitmapScaleTask();
    }

    private void launchBitmapScaleTask() {
        if (mPreScaleBitmap != null && mWidthBound != 0 && mHeightBound != 0) {
            new BitmapScaleAsyncTask(this).execute(new BitmapScaleAsyncTask.Params(mPreScaleBitmap, mWidthBound, mHeightBound));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_imoji_editor, container, false);
        v.getViewTreeObserver().addOnGlobalLayoutListener(this); //listen for layout changes to get info on the parent view width/height
        return v;
    }

    @Override
    public void onGlobalLayout() {
        if (Build.VERSION.SDK_INT >= 16) {
            getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        mWidthBound = getView().getWidth();
        mHeightBound = getView().getHeight();

        launchBitmapScaleTask();

    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {

        mUndoButton = (ImageButton) v.findViewById(R.id.imoji_ib_undo);
        mUndoButton.setVisibility(View.GONE);
        mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIGEditorView.canUndo()) {
                    mIGEditorView.undo();
                    if (!mIGEditorView.canUndo()) {
                        mIGEditorView.setVisibility(View.GONE);
                    }
                }
            }
        });

        mTagButton = (ImageButton) v.findViewById(R.id.imoji_ib_tag);
        mTagButton.setVisibility(View.GONE);
        mTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIGEditorView.isImojiReady() && !mIsTagging) {
                    mIsTagging = true;
                    mIGEditorView.getTrimmedOutputBitmap(new IGEditorView.BitmapListener() {
                        @Override
                        public void onBitmapOutputReady(Bitmap bitmap) {
                            if (isAdded()) {
                                BitmapRetainerFragment f = findOrCreateRetainedFragment();
                                f.mTrimmedBitmap = bitmap;

                                if (isResumed()) {
                                    TagImojiFragment tagImojiFragment = new TagImojiFragment();
                                    getFragmentManager().beginTransaction().addToBackStack(null).add(R.id.imoji_tag_container, tagImojiFragment).commit();
                                }

                            }
                            mIsTagging = false;
                        }
                    });

                }
            }
        });

        mIGEditorView = (IGEditorView) v.findViewById(R.id.imoji_editor_view);
        initEditor(savedInstanceState);

    }

    private void initEditor(Bundle savedInstanceState) {

        TypedArray typedArray = getContext().obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
        int windowBackground = typedArray.getColor(0, Color.BLACK);
        mIGEditorView.setGLBackgroundColor(windowBackground);
        mIGEditorView.setZoomInOnAspectFit(false);
        mIGEditorView.setImageAlpha(200);

        mIGEditorView.setStateListener(new IGEditorView.StateListener() {
            @Override
            public void onStateChanged(int igEditorState, int igEditorSubstate) {
                switch (igEditorState) {
                    case IG.EDITOR_DRAW:
                        if (mIGEditorView.canUndo()) {
                            mUndoButton.setVisibility(View.VISIBLE);
                        } else {
                            mUndoButton.setVisibility(View.GONE);
                        }
                        mTagButton.setVisibility(View.GONE);
                        break;
                    case IG.EDITOR_NUDGE:
                        mTagButton.setVisibility(View.VISIBLE);
                        break;
                }
                Log.d(LOG_TAG, "state changed to: " + igEditorState + " subState: " + igEditorSubstate);
            }
        });

//        if (savedInstanceState != null) {
//            if (savedInstanceState.containsKey(EDITOR_STATE_BUNDLE_ARG_KEY)) {
//                mIGEditorView.deserialize(savedInstanceState.getByteArray(EDITOR_STATE_BUNDLE_ARG_KEY));
//            }
//        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBitmapRetainerFragment = findOrCreateRetainedFragment();
        if (savedInstanceState != null) {
            mPreScaleBitmap = mBitmapRetainerFragment.mPreScaledBitmap;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPreScaleBitmap != null && mBitmapRetainerFragment != null) {
            mBitmapRetainerFragment.mPreScaledBitmap = mPreScaleBitmap;
//            outState.putByteArray(EDITOR_STATE_BUNDLE_ARG_KEY, mIGEditorView.serialize());
        }
        
        super.onSaveInstanceState(outState);

    }


    public static class BitmapRetainerFragment extends Fragment {
        public static final String FRAGMENT_TAG = BitmapRetainerFragment.class.getSimpleName();

        Bitmap mPreScaledBitmap; //store the bitmap across config changes
        Bitmap mTrimmedBitmap;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

    public BitmapRetainerFragment findOrCreateRetainedFragment() {
        BitmapRetainerFragment bitmapRetainerFragment = (BitmapRetainerFragment) getFragmentManager().findFragmentByTag(BitmapRetainerFragment.FRAGMENT_TAG);
        if (bitmapRetainerFragment == null) {
            bitmapRetainerFragment = new BitmapRetainerFragment();
            getFragmentManager().beginTransaction().add(bitmapRetainerFragment, BitmapRetainerFragment.FRAGMENT_TAG).commitAllowingStateLoss();
        }

        return bitmapRetainerFragment;
    }


    static class BitmapScaleAsyncTask extends AsyncTask<BitmapScaleAsyncTask.Params, Void, Bitmap> {

        private WeakReference<ImojiEditorFragment> mFragmentWeakReference;

        public BitmapScaleAsyncTask(ImojiEditorFragment fragment) {
            mFragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        final protected Bitmap doInBackground(Params... params) {
            Params p = params[0];
            int[] sizeInfo = BitmapUtils.getSizeWithinBounds(p.mSource.getWidth(), p.mSource.getHeight(), p.mWidthBound, p.mHeightBound, true);
            return Bitmap.createScaledBitmap(p.mSource, sizeInfo[0], sizeInfo[1], false);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImojiEditorFragment f = mFragmentWeakReference.get();
            if (f != null && f.mIGEditorView != null) {
//                ViewGroup.LayoutParams params = f.mIGEditorView.getLayoutParams();
//                params.width = bitmap.getWidth();
//                params.height = bitmap.getHeight();
//                f.mIGEditorView.setLayoutParams(params);
                f.mIGEditorView.setInputBitmap(bitmap);
            }
        }

        public static class Params{
            public Bitmap mSource;
            public int mWidthBound;
            public int mHeightBound;

            public Params(Bitmap source, int widthBound, int heightBound) {
                mSource = source;
                mWidthBound = widthBound;
                mHeightBound = heightBound;
            }
        }

    }

}
