package com.imojiapp.imoji.sdk.ui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.imojiapp.imoji.sdk.ui.utils.EditorBitmapCache;
import com.imojiapp.imoji.sdk.ui.utils.ScrimUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class TagImojiFragment extends Fragment implements OutlineAsyncTask.OutlinedBitmapReadyListener {
    public static final String FRAGMENT_TAG = TagImojiFragment.class.getSimpleName();
    private static final String LOG_TAG = TagImojiFragment.class.getSimpleName();
    public static final String TAGS_BUNDLE_ARG_KEY = "TAGS_BUNDLE_ARG_KEY";
    public static final String IS_PROCESSING_BUNDLE_ARG_KEY = "IS_PROCESSING_BUNDLE_ARG_KEY";
    public static final String RETURN_IMMEDIATELY_BUNDLE_ARG_KEY = "RETURN_IMMEDIATELY_BUNDLE_ARG_KEY";


    public static TagImojiFragment newInstance(boolean returnImmediately) {
        TagImojiFragment f = new TagImojiFragment();

        Bundle args = new Bundle();
        args.putBoolean(RETURN_IMMEDIATELY_BUNDLE_ARG_KEY, returnImmediately);
        f.setArguments(args);
        return f;
    }

    public static TagImojiFragment newInstance() {
        return newInstance(false);
    }

    Toolbar mToolbar;
    TextView mTitleTv;
    ImageView mImojiIv;
    GridLayout mTagGrid;
    RelativeLayout mParentView;
    ScrollView mGridScroller;
    EditText mTaggerEt;
    ImageButton mUploadButton;
    ProgressBar mProgress;
    ImageButton mClearInputBt;
    View mTagEditor;

    private boolean mIsProcessing;
    private boolean mReturnImmediately;

    private ImojiEditorFragment.BitmapRetainerFragment mBitmapRetainerFragment;
    private InputMethodManager mInputMethodManager;

    private EditText.OnEditorActionListener mKeyActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                //get the text from the tag editor
                String tag = ((TextView) mTagEditor.findViewById(R.id.et_tag)).getText().toString();
                if (!tag.isEmpty()) {
                    addTagChip(tag);
                }
                return true;
            }

            return true;
        }
    };

    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (isResumed() && !mIsProcessing) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.VISIBLE);
                }
                mIsProcessing = true;
                if (mReturnImmediately) {
                    String token = UUID.randomUUID().toString();
                    EditorBitmapCache.getInstance().put(token, EditorBitmapCache.getInstance().get(EditorBitmapCache.Keys.TRIMMED_BITMAP));

                    Intent intent = new Intent();
                    intent.setClass(getActivity(), ImojiCreateService.class);
                    intent.putExtra(ImojiCreateService.CREATE_TOKEN_BUNDLE_ARG_KEY, token);
                    getActivity().startService(intent);

                    notifySuccess(token);

                } else {

                    CreateTaskFragment f = (CreateTaskFragment) getFragmentManager().findFragmentByTag(CreateTaskFragment.FRAGMENT_TAG);
                    if (f == null) {
                        f = CreateTaskFragment.newInstance(getTags(), false);
                    }
                    getFragmentManager().beginTransaction().add(f, CreateTaskFragment.FRAGMENT_TAG).commit();
                }

            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReturnImmediately = getArguments().getBoolean(RETURN_IMMEDIATELY_BUNDLE_ARG_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tag_imoji, container, false);
    }

    //
    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        mImojiIv = (ImageView) v.findViewById(R.id.iv_imoji);
        mTagEditor = v.findViewById(R.id.tag_editor);
        mTagEditor.setBackgroundDrawable(createTagDrawable());
        mTagGrid = (GridLayout) v.findViewById(R.id.gl_tagbox);
        mTaggerEt = (EditText) v.findViewById(R.id.et_tag);
        mTaggerEt.setOnEditorActionListener(mKeyActionListener);
        mUploadButton = (ImageButton) v.findViewById(R.id.ib_upload);
        mUploadButton.setOnClickListener(mOnDoneClickListener);
        mProgress = (ProgressBar) v.findViewById(R.id.imoji_progress);


        mToolbar = (Toolbar) v.findViewById(R.id.imoji_toolbar);
        mToolbar.setNavigationIcon(R.drawable.create_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isResumed()) {
                    getFragmentManager().popBackStack();
                    mInputMethodManager.hideSoftInputFromWindow(mTaggerEt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                }
            }
        });

        mClearInputBt = (ImageButton) v.findViewById(R.id.ib_cancel);
        mClearInputBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTaggerEt != null) {
                    mTaggerEt.getText().clear();
                }
            }
        });


        View toolbarScrim = v.findViewById(R.id.imoji_toolbar_scrim);
        if (Build.VERSION.SDK_INT >= 16) {
            toolbarScrim.setBackground(ScrimUtil.makeCubicGradientScrimDrawable(0x66000000, 8, Gravity.TOP));
        } else {
            toolbarScrim.setBackgroundDrawable(ScrimUtil.makeCubicGradientScrimDrawable(0x66000000, 8, Gravity.TOP));
        }

        if (savedInstanceState != null) {
            List<String> tags = savedInstanceState.getStringArrayList(TAGS_BUNDLE_ARG_KEY);
            for (String tag : tags) {
                addTagChip(tag);
            }

            mProgress.setVisibility(savedInstanceState.getBoolean(IS_PROCESSING_BUNDLE_ARG_KEY) ? View.VISIBLE : View.GONE);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //show keyboard
        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.showSoftInput(mTaggerEt, InputMethodManager.SHOW_IMPLICIT);

        mBitmapRetainerFragment = (ImojiEditorFragment.BitmapRetainerFragment) getFragmentManager().findFragmentByTag(ImojiEditorFragment.BitmapRetainerFragment.FRAGMENT_TAG);
        if (mBitmapRetainerFragment == null || mBitmapRetainerFragment.mTrimmedBitmap == null) {
            getFragmentManager().popBackStack();
            return;
        }

        final Bitmap imojiBitmap = mBitmapRetainerFragment.mTrimmedBitmap;
        getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT >= 16) {
                    getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                final int width = mImojiIv.getWidth();
                final int height = mImojiIv.getHeight();


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new OutlineAsyncTask(imojiBitmap, width, height, TagImojiFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    new OutlineAsyncTask(imojiBitmap, width, height, TagImojiFragment.this).execute();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(TAGS_BUNDLE_ARG_KEY, getTags());
        outState.putBoolean(IS_PROCESSING_BUNDLE_ARG_KEY, mIsProcessing);
        super.onSaveInstanceState(outState);
    }

    private void addTagChip(String tag) {
        //create a view and add it to the gridview
        final View x = LayoutInflater.from(getActivity()).inflate(R.layout.tag_layout, mTagGrid, false);
        if (Build.VERSION.SDK_INT >= 16) {
            x.findViewById(R.id.tag_wrapper).setBackground(createTagDrawable());
        } else {
            x.findViewById(R.id.tag_wrapper).setBackgroundDrawable(createTagDrawable());
        }

        ((TextView) x.findViewById(R.id.tv_tag)).setText(tag);
        ((TextView) x.findViewById(R.id.tv_tag)).setTextColor(Color.WHITE);
        (x.findViewById(R.id.ib_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTagGrid.removeView(x);
            }
        });

        mTagGrid.addView(x, 0);
        ((TextView) mTagEditor.findViewById(R.id.et_tag)).setText("");
    }

    private ArrayList<String> getTags() {
        ArrayList<String> tags = new ArrayList<String>();
        int numTags = mTagGrid.getChildCount();
        for (int i = 0; i < numTags; i++) {
            TextView tv = (TextView) mTagGrid.getChildAt(i).findViewById(R.id.tv_tag); //unsafe cast, wutever
            String tag = tv.getText().toString();
            tags.add(tag);
        }
        return tags;
    }

    public Drawable createTagDrawable() {

        GradientDrawable d = new GradientDrawable();
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[]{R.attr.colorAccent});
        final int accentColor = a.getColor(0, Color.WHITE);
        a.recycle();
        d.setColor(0xB3FFFFFF & accentColor);
        d.setCornerRadius(getResources().getDimension(R.dimen.dim_8dp));
        d.setShape(GradientDrawable.RECTANGLE);

        GradientDrawable d1 = new GradientDrawable();
        d1.setCornerRadius(getResources().getDimension(R.dimen.dim_8dp));
        d1.setStroke((int) getResources().getDimension(R.dimen.dim_0_5dp), 0x66FFFFFF & Color.BLACK);

        GradientDrawable d2 = new GradientDrawable();
        d2.setStroke((int) getResources().getDimension(R.dimen.dim_1dp), accentColor);
        d2.setCornerRadius(getResources().getDimension(R.dimen.dim_8dp));

        LayerDrawable layer = new LayerDrawable(new Drawable[]{d, d2, d1});

        int halfDp = (int) getResources().getDimension(R.dimen.dim_0_5dp);
        int oneDp = (int) getResources().getDimension(R.dimen.dim_1dp);
        int oneAndHalf = halfDp + oneDp;

        layer.setLayerInset(2, 0, 0, 0, 0);
        layer.setLayerInset(1, halfDp, halfDp, halfDp, halfDp);
        layer.setLayerInset(0, oneAndHalf, oneAndHalf, oneAndHalf, oneAndHalf);

        return layer;
    }

    @Override
    public void onOutlinedBitmapReady(Bitmap outlinedBitmap) {

        if (isAdded()) {

            if (outlinedBitmap == null) {
                notifyFailure();
                return;
            }

            if (mImojiIv != null) {
                mImojiIv.setImageBitmap(outlinedBitmap);
            }

            //also save it to cache
            EditorBitmapCache.getInstance().put(EditorBitmapCache.Keys.OUTLINED_BITMAP, outlinedBitmap);
        }
    }

    private void notifyFailure() {
        //notify of failure
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    private void notifySuccess(String token) {
        Intent intent = new Intent();
        intent.putExtra(ImojiEditorActivity.CREATE_TOKEN_BUNDLE_ARG_KEY, token);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();

    }
}
