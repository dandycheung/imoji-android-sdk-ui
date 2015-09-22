package com.imojiapp.imoji.sdk.ui;


import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.imojiapp.imoji.sdk.ui.utils.BitmapUtils;
import com.imojiapp.imojigraphics.IG;

/**
 * A simple {@link Fragment} subclass.
 */
public class TagImojiFragment extends Fragment {
    public static final String FRAGMENT_TAG = TagImojiFragment.class.getSimpleName();
    private static final String LOG_TAG = TagImojiFragment.class.getSimpleName();


    TextView mTitleTv;
    ImageView mImojiIv;
    CheckBox mPrivateCb;
    CheckBox mPublicCb;
    GridLayout mTagGrid;
    RelativeLayout mParentView;
    ScrollView mGridScroller;
    EditText mTaggerEt;
    Button mUploadButton;


    View mTagEditor;
    private Bitmap mImojiBitmap;

    private boolean mIsProcessing;
    private boolean mIsDone;
    //
//    private View.OnClickListener mToolbarNavigationOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            hideKeyboard();
//            //finish the fragment, go back
//            getFragmentManager().popBackStack();
//        }
//    };
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        if(savedInstanceState != null) {
//            mIsDone = savedInstanceState.getBoolean(IS_DONE_BUNDLE_ARG_KEY);
//            if(mIsDone){
//                Intent resultIntent = new Intent();
//                resultIntent.putExtra(MainActivity.CREATED_IMOJI_ID_BUNDLE_ARG_KEY, mCreatedImojiId);
//                getActivity().setResult(Activity.RESULT_OK, resultIntent);
//                getActivity().finish(); //finish this activity
//            }
//        }
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        outState.putBoolean(IS_DONE_BUNDLE_ARG_KEY, mIsDone);
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    protected void initViews() {
//        mTitleTv.setText(getActivity().getString(R.string.tag));
//
//        //create the property animator from the edittext
////        mTagEditor.setAlpha(0f); //transparent view
////        mTagGrid.setAlpha(0f);
//
//        mTagEditor.findViewById(R.id.ib_cancel).setOnClickListener(mXButtonListener);
//        ((TextView)mTagEditor.findViewById(R.id.tv_tag)).setOnEditorActionListener(mKeyActionListener);
//        ((TextView)mTagEditor.findViewById(R.id.tv_tag)).requestFocus();
//        showKeyboard();
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        ImojiApplication.getInstance().getImojiAnalytics().trackPageView(TagImojiFragment.FRAGMENT_TAG);
//
//    }
//
//    @OnClick({R.id.bt_action_center})
//    public void onActionButtonClick(View v) {
//        switch (v.getId()) {
//            case R.id.bt_action_center:
//                if(!mHasUserCreated) {
//                    Log.d(LOG_TAG, "create imoji");
//                    mHasUserCreated = true;
//                    createImoji();
//
//                    //move back to the main fragment
//                }
//                break;
//        }
//    }
//
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton button, boolean isChecked) {
            Log.d(LOG_TAG, "on checked changed: " + isChecked);
            int i = button.getId();
            if (i == R.id.cb_tag_private) {
                if (isChecked) {
                    mPublicCb.setChecked(false);
                    mTagEditor.animate().cancel(); //cancel the animationm
                    mTagEditor.animate().alpha(0f);
                    mTagEditor.animate().alpha(0f);
//                    hideKeyboard();

                    //also remove any tags
                    mTagGrid.removeAllViews();

                } else if (!mPublicCb.isChecked()) {
                    button.setChecked(true);
                }

            } else if (i == R.id.cb_tag_public) {
                if (isChecked) {
                    mPrivateCb.setChecked(false);
                    mTagEditor.animate().alpha(1f);
                    mTagGrid.animate().alpha(1f);
                    mTagEditor.findViewById(R.id.et_tag).requestFocus();
//                    showKeyboard();
                } else if (!mPrivateCb.isChecked()) {
                    button.setChecked(true);
                }

            }
        }
    };

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
        mPrivateCb = (CheckBox) v.findViewById(R.id.cb_tag_private);
        mPublicCb = (CheckBox) v.findViewById(R.id.cb_tag_public);
        mTagEditor = v.findViewById(R.id.tag_editor);
        mTagGrid = (GridLayout) v.findViewById(R.id.gl_tagbox);
        mPrivateCb.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mPublicCb.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mTaggerEt = (EditText) v.findViewById(R.id.et_tag);
        mTaggerEt.setOnEditorActionListener(mKeyActionListener);
        mUploadButton.setOnClickListener(mOnDoneClickListener);


        //inflate the initial menu items
//        mToolbar.inflateMenu(R.menu.menu_fragment_tag_imoji);
//        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                if (menuItem.getItemId() == R.id.mi_done) {
//                    ArrayList<String> tags = getTags();
//                    if (mPublicCb.isChecked() && (tags == null || tags.isEmpty())) {
//                        Crouton.makeText(getActivity(), getString(R.string.please_add_a_tag), Utils.getCroutonStyle(Style.ALERT)).show();
//                        return true;
//                    }
//
//                    if(!mHasUserCreated) {
//                        Log.d(LOG_TAG, "create imoji");
//                        mHasUserCreated = true;
//                        createImoji();
//
//                        //move back to the main fragment
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });
//        mToolbar.setNavigationIcon(R.drawable.create_imoji_back);
//        mToolbar.setNavigationOnClickListener(mToolbarNavigationOnClickListener);
//        mCenterActionBt.setVisibility(View.VISIBLE);
//        if (Build.VERSION.SDK_INT >= 16) {
//            mCenterActionBt.setBackground(Utils.getCameraShutterDrawable());
//        } else {
//            mCenterActionBt.setBackgroundDrawable(Utils.getCameraShutterDrawable());
//        }

//        mTagEditor.setBackgroundDrawable(Utils.createTagDrawable());


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImojiEditorFragment.BitmapRetainerFragment bitmapRetainerFragment = (ImojiEditorFragment.BitmapRetainerFragment) getFragmentManager().findFragmentByTag(ImojiEditorFragment.BitmapRetainerFragment.FRAGMENT_TAG);
        if (bitmapRetainerFragment == null || bitmapRetainerFragment.mTrimmedBitmap == null) {
            //bye bye
            Log.w(LOG_TAG, "retained fragment not available, or trimmed bitmap is null; let's just leave then");
            getFragmentManager().popBackStack();
            return;
        }

        mImojiBitmap = bitmapRetainerFragment.mTrimmedBitmap;
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


                        int[] size = BitmapUtils.getSizeWithinBounds(mImojiBitmap.getWidth(), mImojiBitmap.getHeight(), width, height, true);
                        Bitmap b = Bitmap.createScaledBitmap(mImojiBitmap, size[0], size[1], false);
                        int igBorder = IG.BorderCreatePreset(mImojiBitmap.getWidth(), mImojiBitmap.getHeight(), IG.BORDER_CLASSIC);
                        int padding = IG.BorderGetPadding(igBorder);

                        // Example of how to extract and apply an IMVC vector chunk from a WebP image
                        //     int igPaths = IG.WebPGetPaths(webpImageData, padding, padding, mImojiBitmap.getWidth(), mImojiBitmap.getHeight());
                        //     IG.BorderSetEdgePaths(igBorder, igPaths);

                        int igInputImage = IG.ImageFromNative(igContext, mImojiBitmap, 1);
                        int igOutputImage = IG.ImageCreate(igContext, IG.ImageGetWidth(igInputImage) + padding * 2, IG.ImageGetHeight(igInputImage) + padding * 2);
                        IG.BorderRender(igBorder, igInputImage, igOutputImage, padding - 1, padding - 1, 1, 1);
                        Bitmap outputBitmap = IG.ImageToNative(igOutputImage);
                        IG.ImageDestroy(igOutputImage);
                        IG.ImageDestroy(igInputImage);
                        IG.BorderDestroy(igBorder, true);
                        long end = System.currentTimeMillis();
                        IG.ContextDestroy(igContext);

                        return outputBitmap;

                    }


                    @Override
                    protected void onPostExecute(Bitmap b) {
                        if (isAdded()) {
                            mImojiIv.setImageBitmap(b);
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        //let's go ahead and apply a border to this
    }

//
    private EditText.OnEditorActionListener mKeyActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                //get the text from the tag editor
                String tag = ((TextView)mTagEditor.findViewById(R.id.et_tag)).getText().toString();
                if (!tag.isEmpty()) {

                    //create a view and add it to the gridview
                    final View x = LayoutInflater.from(getActivity()).inflate(R.layout.tag_layout, mTagGrid, false);
                    if (Build.VERSION.SDK_INT >= 16) {
//                        x.findViewById(R.id.tag_wrapper).setBackground(Utils.createTagDrawable());
                    } else {
//                        x.findViewById(R.id.tag_wrapper).setBackgroundDrawable(Utils.createTagDrawable());
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
                return true;
            }

            return true;
        }
    };

    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIsProcessing = true;

            //upload the image and the original

        }
    };

//
//    private void createImoji() {
//        mSmoothProgressBar.setVisibility(View.VISIBLE);
//        mSmoothProgressBar.progressiveStart();
//
//        //get tags
//        ArrayList<String> tags = getTags();
//
//        //create the imoji
//        Imoji imoji = new Imoji(tags);
//        long groupId = ((ImojiCreatorActivity) getActivity()).getGroupId();
//        ImojiGroup group = Model.load(ImojiGroup.class, groupId);
//        imoji.imojiGroup = group;
//        imoji.updatedAt = System.currentTimeMillis();
//        imoji.save();
//
//        mCreatedImojiId = imoji.getId();
//
//        //create the image info of the imoji
//
//        if (mWebImage != null) {
//            imoji.originalWebUrl = mWebImage.fullImage.url;
//            imoji.originalWidth = Integer.valueOf(mImojiBitmap.getWidth());
//            imoji.originalHeight = Integer.valueOf(mImojiBitmap.getHeight());
//        }
//        imoji.save();
//
//        Log.d(LOG_TAG, "saved imoji: " + imoji.toString());
//
//        final ImojiImageService.EditorPayload payload = new ImojiImageService.EditorPayload();
//        payload.mPreScaledBitmap = mImojiBitmap;
//        payload.mImoji = imoji;
//        Events.Editor.OnEditorFinishedEvent event = new Events.Editor.OnEditorFinishedEvent();
//        event.bitmap = mImojiBitmap;
//        event.imoji = imoji;
//
//        //notify that an imoji has been created
//        EventBus.getDefault().post(event);
//
//        mIsDone = true;
//
//        JSONObject props = new JSONObject();
//        try {
//            props.put(ImojiAnalytics.Property.ImojiCreateSource.SOURCE, ((ImojiCreatorActivity) getActivity()).getCreationSource());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        //log imoji created
//        ImojiAnalytics.getMixpanel().track(ImojiAnalytics.Property.IMOJI_CREATED, props);
//    }
//
//    private ArrayList<String> getTags() {
//        ArrayList<String> tags = new ArrayList<String>();
//        int numTags = mTagGrid.getChildCount();
//        for(int i = 0; i < numTags; i++){
//            TextView tv = (TextView) mTagGrid.getChildAt(i).findViewById(R.id.tv_tag); //unsafe cast, wutever
//            String tag = tv.getText().toString();
//            tags.add(tag);
//        }
//        return tags;
//    }
//
//    public void onEventMainThread(Events.ImojiEvent.ImojiRegistered event) {
//        if(isAdded()) {
//            Intent resultIntent = new Intent();
//            resultIntent.putExtra(MainActivity.CREATED_IMOJI_ID_BUNDLE_ARG_KEY, mCreatedImojiId);
//            getActivity().setResult(Activity.RESULT_OK, resultIntent);
//            getActivity().finish();
//        }
//    }
//
//    public void onEventMainThread(Events.ImojiEvent.OnImojiCreatedEvent event){
//        if(isAdded()) {
//            Intent resultIntent = new Intent();
//            resultIntent.putExtra(MainActivity.CREATED_IMOJI_ID_BUNDLE_ARG_KEY, mCreatedImojiId);
//            getActivity().setResult(Activity.RESULT_OK, resultIntent);
//            getActivity().finish();
//        }
//    }
//
//    public void onEventMainThread(Events.ImojiEvent.ImojiCreateFailEvent event) {
//        if (isAdded()) {
//
//            mSmoothProgressBar.progressiveStop();
//            Intent resultIntent = new Intent();
//            resultIntent.putExtra(MainActivity.CREATED_IMOJI_ID_BUNDLE_ARG_KEY, mCreatedImojiId);
//            getActivity().setResult(Activity.RESULT_CANCELED, resultIntent);
//            getActivity().finish();
//        }
//    }
//
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        ButterKnife.reset(this);
//    }
//
//    private View.OnClickListener mXButtonListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            ((TextView)mTagEditor.findViewById(R.id.tv_tag)).setText(""); //clear it
//        }
//    };
//
//    @Override
//    protected String getLogTag() {
//        return LOG_TAG;
//    }

}
