package com.imojiapp.imoji.sdk.ui;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;

import com.imojiapp.imoji.sdk.ui.utils.BitmapUtils;
import com.imojiapp.imojigraphics.IGEditorView;

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImojiEditorFragment extends Fragment implements ViewTreeObserver.OnGlobalLayoutListener{
    public static final String FRAGMENT_TAG = ImojiEditorFragment.class.getSimpleName();

    private Bitmap mPreScaleBitmap;
    private Bitmap mPostScaleBitmap;
    private IGEditorView mIGEditorView;
    private BitmapRetainerFragment mBitmapRetainerFragment;
    private boolean mIsTagging;
    private int mWidthBound = 0;
    private int mHeightBound = 0;
    private boolean mPendingScale;

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
        mIGEditorView = (IGEditorView) v.findViewById(R.id.imoji_editor_view);
        mIGEditorView.setZoomInOnAspectFit(false);
        ImageButton undoButton = (ImageButton) v.findViewById(R.id.imoji_ib_undo);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIGEditorView.canUndo()) {
                    mIGEditorView.undo();
                }
            }
        });

        ImageButton tagButton = (ImageButton) v.findViewById(R.id.imoji_ib_tag);
        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIGEditorView.isImojiReady()) {
                    mIsTagging = true;
                    mIGEditorView.getTrimmedOutputBitmap(new IGEditorView.OnBitmapReady() {
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
            getFragmentManager().beginTransaction().add(bitmapRetainerFragment, BitmapRetainerFragment.FRAGMENT_TAG).commit();
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
                Log.d("t", "bitmap size: " + bitmap.getWidth() + " " + bitmap.getHeight());
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
