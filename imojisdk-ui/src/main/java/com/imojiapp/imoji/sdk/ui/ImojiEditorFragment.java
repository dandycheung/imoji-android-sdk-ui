package com.imojiapp.imoji.sdk.ui;


import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;

import com.imojiapp.imoji.sdk.BitmapUtils;
import com.imojiapp.imoji.sdk.ui.utils.ScrimUtil;
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
    private Toolbar mToolbar;
    private View mToolbarScrim;
    private View mBottomBarScrim;
    private byte[] mStateData;


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

    private IGEditorView.UndoListener mUndoListener = new IGEditorView.UndoListener() {
        @Override
        public void onUndone(boolean canUndo) {
            if (!canUndo) {
                if (mUndoButton != null && mIGEditorView != null) {
                    mUndoButton.setVisibility(View.GONE);
                }
            }
        }
    };

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {

        configureToolbar(v);


        mUndoButton = (ImageButton) v.findViewById(R.id.imoji_ib_undo);
        mUndoButton.setVisibility(View.GONE);
        mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIGEditorView.canUndo()) {
                    mIGEditorView.undo(mUndoListener);

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

    private void configureToolbar(View v) {
        //configure the toolbar
        mToolbar = (Toolbar) v.findViewById(R.id.imoji_toolbar);
        mToolbar.setNavigationIcon(R.drawable.create_back);
        mToolbar.inflateMenu(R.menu.menu_imoji_editor_fragment);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.imoji_mi_editor_help) {
                    if (isResumed()) {
                        TipsFragment f = TipsFragment.newInstance();
                        getFragmentManager().beginTransaction().addToBackStack(null).setCustomAnimations(R.anim.abc_fade_in, -1, -1, R.anim.imoji_fade_out).add(R.id.imoji_tag_container, f).commit();
                    }
                    return true;
                }
                return false;
            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdded()) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
            }
        });

        mToolbarScrim = v.findViewById(R.id.imoji_toolbar_scrim);
        mBottomBarScrim = v.findViewById(R.id.imoji_bottom_bar_scrim);

        Drawable scrim = ScrimUtil.makeCubicGradientScrimDrawable(0x66000000, 32, Gravity.TOP);
        Drawable bottomBarScrim = ScrimUtil.makeCubicGradientScrimDrawable(0x66000000, 32, Gravity.BOTTOM);
        if (Build.VERSION.SDK_INT >= 16) {
            mToolbarScrim.setBackground(scrim);
            mBottomBarScrim.setBackground(bottomBarScrim);
        } else {
            mToolbarScrim.setBackgroundDrawable(scrim);
            mBottomBarScrim.setBackgroundDrawable(bottomBarScrim);
        }
    }

    private void initEditor(Bundle savedInstanceState) {

        TypedArray typedArray = getActivity().obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
        int windowBackground = typedArray.getColor(0, Color.BLACK);
        typedArray.recycle();
        mIGEditorView.setGLBackgroundColor(windowBackground);
        mIGEditorView.gravitateTo(0, -1);
//        mIGEditorView.setGLBackgroundColor(Color.parseColor("#00000000"));
        mIGEditorView.setBackgroundColor(Color.TRANSPARENT);
        mIGEditorView.setImageAlpha(225);

        mIGEditorView.setStateListener(new IGEditorView.StateListener() {
            @Override
            public void onStateChanged(int igEditorState, int igEditorSubstate) {
                switch (igEditorState) {
                    case IG.EDITOR_DRAW:
                        mTagButton.setVisibility(View.GONE);
                        mIGEditorView.setImageAlpha(225);
                        break;
                    case IG.EDITOR_NUDGE:
                        mIGEditorView.setImageAlpha(0x66);
                        mTagButton.setVisibility(View.VISIBLE);
                        break;
                }

                if (mIGEditorView.canUndo()) {
                    mUndoButton.setVisibility(View.VISIBLE);
                } else {
                    mUndoButton.setVisibility(View.GONE);
                }
                Log.d(LOG_TAG, "state changed to: " + igEditorState + " subState: " + igEditorSubstate);

                //if there's a state change, lets serialize the data
                if (mIGEditorView != null) {
                    mIGEditorView.serialize(new IGEditorView.DataListener() {
                        @Override
                        public void onDataReady(byte[] data) {
                            mStateData = data;
                        }
                    });
                }
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
            mStateData = savedInstanceState.getByteArray(EDITOR_STATE_BUNDLE_ARG_KEY);
            if (mStateData != null) {
                mIGEditorView.deserialize(mStateData);
            }

            if (mIGEditorView.canUndo()) {
                mUndoButton.setVisibility(View.VISIBLE);
            }

        }
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
    public void onSaveInstanceState(Bundle outState) {
        if (mPreScaleBitmap != null && mBitmapRetainerFragment != null) {
            mBitmapRetainerFragment.mPreScaledBitmap = mPreScaleBitmap;
        }

        outState.putByteArray(EDITOR_STATE_BUNDLE_ARG_KEY, mStateData);
        
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

    private void hideSystemUiVisibility() {
        if (Build.VERSION.SDK_INT >= 19) {
            final View decorView = getActivity().getWindow().getDecorView();
            int uiOptions =
                    View.SYSTEM_UI_FLAG_FULLSCREEN
//                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int flags) {
                    if ((flags & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            decorView.setSystemUiVisibility(
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        }
                    }
                }
            });


        }
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
            Bitmap outBitmap = Bitmap.createScaledBitmap(p.mSource, sizeInfo[0], sizeInfo[1], false);
            if (outBitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                Bitmap tmp = outBitmap.copy(Bitmap.Config.ARGB_8888, true);
                outBitmap.recycle();
                outBitmap = tmp;
            }
            return outBitmap;
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
