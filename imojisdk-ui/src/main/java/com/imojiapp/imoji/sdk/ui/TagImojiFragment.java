package com.imojiapp.imoji.sdk.ui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
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
import com.imojiapp.imoji.sdk.ui.utils.EditorBitmapCache;
import com.imojiapp.imojigraphics.IG;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TagImojiFragment extends Fragment {
    public static final String FRAGMENT_TAG = TagImojiFragment.class.getSimpleName();
    private static final String LOG_TAG = TagImojiFragment.class.getSimpleName();
    private static final String TAGS_BUNDLE_ARG_KEY = "TAGS_BUNDLE_ARG_KEY";

    TextView mTitleTv;
    ImageView mImojiIv;
    GridLayout mTagGrid;
    RelativeLayout mParentView;
    ScrollView mGridScroller;
    EditText mTaggerEt;
    ImageButton mUploadButton;
    ProgressBar mProgress;


    View mTagEditor;

    private boolean mIsProcessing;
    private boolean mIsDone;

    private ImojiEditorFragment.BitmapRetainerFragment mBitmapRetainerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_tag_imoji, container, false);
        return v;
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

        if (savedInstanceState != null) {
            List<String> tags = savedInstanceState.getStringArrayList(TAGS_BUNDLE_ARG_KEY);
            for (String tag : tags) {
                addTagChip(tag);
            }
        }

        //show keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mTaggerEt, InputMethodManager.SHOW_IMPLICIT);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

                //TODO: pull out into a static class
                new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        int igContext = IG.ContextCreate();
                        if (igContext == 0) {
                            System.err.println("Unable to create IG context");
                            return null;
                        }


                        int[] size = BitmapUtils.getSizeWithinBounds(imojiBitmap.getWidth(), imojiBitmap.getHeight(), width, height, true);
                        Bitmap b = Bitmap.createScaledBitmap(imojiBitmap, size[0], size[1], false);
                        int igBorder = IG.BorderCreatePreset(imojiBitmap.getWidth(), imojiBitmap.getHeight(), IG.BORDER_CLASSIC);
                        int padding = IG.BorderGetPadding(igBorder);

                        int igInputImage = IG.ImageFromNative(igContext, imojiBitmap, 1);
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
                        if (isAdded()) {
                            mImojiIv.setImageBitmap(b);
                            //also save it to cache
                            EditorBitmapCache.getInstance().put(EditorBitmapCache.Keys.OUTLINED_BITMAP, b);
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(TAGS_BUNDLE_ARG_KEY, getTags());
        super.onSaveInstanceState(outState);
    }

    private EditText.OnEditorActionListener mKeyActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                //get the text from the tag editor
                String tag = ((TextView)mTagEditor.findViewById(R.id.et_tag)).getText().toString();
                if (!tag.isEmpty()) {
                    addTagChip(tag);
                }
                return true;
            }

            return true;
        }
    };

    private void addTagChip(String tag) {
        //create a view and add it to the gridview
        final View x = LayoutInflater.from(getActivity()).inflate(R.layout.tag_layout, mTagGrid, false);
        if (Build.VERSION.SDK_INT >= 16) {
            x.findViewById(R.id.tag_wrapper).setBackground(createTagDrawable());
        } else {
            x.findViewById(R.id.tag_wrapper).setBackgroundDrawable(createTagDrawable());
        }

        ((TextView)x.findViewById(R.id.tv_tag)).setText(tag);
        ((TextView)x.findViewById(R.id.tv_tag)).setTextColor(Color.WHITE);
        (x.findViewById(R.id.ib_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTagGrid.removeView(x);
            }
        });

        mTagGrid.addView(x, 0);
        ((TextView)mTagEditor.findViewById(R.id.et_tag)).setText("");
    }

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

    private ArrayList<String> getTags() {
        ArrayList<String> tags = new ArrayList<String>();
        int numTags = mTagGrid.getChildCount();
        for(int i = 0; i < numTags; i++){
            TextView tv = (TextView) mTagGrid.getChildAt(i).findViewById(R.id.tv_tag); //unsafe cast, wutever
            String tag = tv.getText().toString();
            tags.add(tag);
        }
        return tags;
    }

    public Drawable createTagDrawable() {

        GradientDrawable d = new GradientDrawable();
        d.setColor(0xB3FFFFFF & getResources().getColor(R.color.colorAccent));
        d.setCornerRadius(getResources().getDimension(R.dimen.dim_8dp));
        d.setShape(GradientDrawable.RECTANGLE);

        GradientDrawable d1 = new GradientDrawable();
        d1.setCornerRadius(getResources().getDimension(R.dimen.dim_8dp));
        d1.setStroke((int)getResources().getDimension(R.dimen.dim_0_5dp), 0x66FFFFFF & Color.BLACK);

        GradientDrawable d2 = new GradientDrawable();
        d2.setStroke((int) getResources().getDimension(R.dimen.dim_1dp), getResources().getColor(R.color.colorAccent));
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

}