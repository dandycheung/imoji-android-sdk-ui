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

import com.imojiapp.imoji.sdk.BitmapUtils;
import com.imojiapp.imoji.sdk.Callback;
import com.imojiapp.imoji.sdk.Imoji;
import com.imojiapp.imoji.sdk.ImojiApi;
import com.imojiapp.imojigraphics.IG;
import com.imojiapp.imoji.sdk.ui.utils.EditorBitmapCache;
import com.imojiapp.imoji.sdk.ui.utils.ScrimUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TagImojiFragment extends Fragment {
    public static final String FRAGMENT_TAG = TagImojiFragment.class.getSimpleName();
    private static final String LOG_TAG = TagImojiFragment.class.getSimpleName();
    private static final String TAGS_BUNDLE_ARG_KEY = "TAGS_BUNDLE_ARG_KEY";

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
    private boolean mIsDone;

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

            if (isAdded() && !mIsProcessing) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.VISIBLE);
                }
                mIsProcessing = true;
                ImojiApi.with(getActivity()).createImoji(mBitmapRetainerFragment.mTrimmedBitmap, getTags(), new CreateCallback(getActivity()));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    new OutlineAsyncTask(TagImojiFragment.this, imojiBitmap, width, height).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    new OutlineAsyncTask(TagImojiFragment.this, imojiBitmap, width, height).execute();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(TAGS_BUNDLE_ARG_KEY, getTags());
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

    private static class CreateCallback implements Callback<Imoji, String> {

        private WeakReference<Activity> mActivityWeakReference;

        public CreateCallback(Activity activity) {
            mActivityWeakReference = new WeakReference<Activity>(activity);
        }

        @Override
        public void onSuccess(Imoji result) {
            Activity a = mActivityWeakReference.get();
            if (a != null) {
                Intent intent = new Intent();
                intent.putExtra(ImojiEditorActivity.IMOJI_MODEL_BUNDLE_ARG_KEY, result);
                a.setResult(Activity.RESULT_OK, intent);
                a.finish();
            }
        }

        @Override
        public void onFailure(String result) {
            Activity a = mActivityWeakReference.get();
            if (a != null) {
                a.setResult(Activity.RESULT_CANCELED, null);
                a.finishActivity(ImojiEditorActivity.START_EDITOR_REQUEST_CODE);
            }
        }
    }

    private static class OutlineAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        private final Bitmap mImojiBitmap;
        private final int mWidth;
        private final int mHeight;
        private WeakReference<TagImojiFragment> mFragmentWeakRef;

        public OutlineAsyncTask(TagImojiFragment f, Bitmap imojiBitmap, int width, int height) {
            mFragmentWeakRef = new WeakReference<>(f);
            mImojiBitmap = imojiBitmap;
            mWidth = width;
            mHeight = height;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            int igContext = IG.ContextCreate();
            if (igContext == 0) {
                System.err.println("Unable to create IG context");
                return null;
            }


            int[] size = BitmapUtils.getSizeWithinBounds(mImojiBitmap.getWidth(), mImojiBitmap.getHeight(), mWidth, mHeight, true);
            Bitmap b = Bitmap.createScaledBitmap(mImojiBitmap, size[0], size[1], false);
            int igBorder = IG.BorderCreatePreset(mImojiBitmap.getWidth(), mImojiBitmap.getHeight(), IG.BORDER_CLASSIC);
            int padding = IG.BorderGetPadding(igBorder);

            int igInputImage = IG.ImageFromNative(igContext, mImojiBitmap, 1);
            int igOutputImage = IG.ImageCreate(igContext, IG.ImageGetWidth(igInputImage) + padding * 2, IG.ImageGetHeight(igInputImage) + padding * 2);
            IG.BorderRender(igBorder, igInputImage, igOutputImage, padding - 1, padding - 1, 1, 1);
            Bitmap outputBitmap = IG.ImageToNative(igOutputImage);
            IG.ImageDestroy(igOutputImage);
            IG.ImageDestroy(igInputImage);
            IG.BorderDestroy(igBorder, true);
            IG.ContextDestroy(igContext);

            return outputBitmap;

        }


        @Override
        protected void onPostExecute(Bitmap b) {
            TagImojiFragment tagImojiFragment = mFragmentWeakRef.get();
            if (tagImojiFragment != null && tagImojiFragment.mImojiIv != null) {
                tagImojiFragment.mImojiIv.setImageBitmap(b);
                //also save it to cache
                EditorBitmapCache.getInstance().put(EditorBitmapCache.Keys.OUTLINED_BITMAP, b);
            }
        }
    }
}
