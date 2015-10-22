package com.imojiapp.imoji.sdk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.imojiapp.imoji.sdk.Callback;
import com.imojiapp.imoji.sdk.Imoji;
import com.imojiapp.imoji.sdk.ImojiApi;
import com.imojiapp.imoji.sdk.ui.utils.EditorBitmapCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sajjadtabib on 10/19/15.
 */
public class CreateTaskFragment extends Fragment implements OutlineAsyncTask.OutlinedBitmapReadyListener {
    public static final String FRAGMENT_TAG = CreateTaskFragment.class.getSimpleName();

    public static final String TAGS_BUNDLE_ARG_KEY = "TAGS_BUNDLE_ARG_KEY";
    public static final String CREATE_OUTLINE_BITMAP_BUNDLE_ARG_KEY = "CREATE_OUTLINE_BITMAP_BUNDLE_ARG_KEY";
    private List<String> mTags;
    private boolean mIsDone;
    private Context mAppContext;
    private Imoji mResultImoji;

    public static CreateTaskFragment newInstance(ArrayList<String> tags) {
        return newInstance(tags, true);
    }

    public static CreateTaskFragment newInstance(ArrayList<String> tags, boolean createOutlineBitmap) {
        CreateTaskFragment f = new CreateTaskFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(TAGS_BUNDLE_ARG_KEY, tags);
        args.putBoolean(CREATE_OUTLINE_BITMAP_BUNDLE_ARG_KEY, createOutlineBitmap);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mIsDone) {
            if (mResultImoji == null) {
                notifyFailure(activity);
            } else {
                notifySuccess(mResultImoji, activity);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mTags = getArguments().getStringArrayList(TAGS_BUNDLE_ARG_KEY);

        mAppContext = getActivity().getApplicationContext();

        Bitmap b = EditorBitmapCache.getInstance().get(EditorBitmapCache.Keys.TRIMMED_BITMAP);
        if (b == null) {
            notifyFailure(getActivity());
            return;
        }

        boolean createOutlinedBitmap = getArguments().getBoolean(CREATE_OUTLINE_BITMAP_BUNDLE_ARG_KEY);

        //start the task

        if (createOutlinedBitmap) {
            OutlineAsyncTask task = new OutlineAsyncTask(b, ImojiContants.IMOJI_WIDTH_BOUND, ImojiContants.IMOJI_HEIGHT_BOUND, this);
            if (Build.VERSION.SDK_INT >= 11) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.execute();
            }
        } else {
            Bitmap outlined = EditorBitmapCache.getInstance().get(EditorBitmapCache.Keys.OUTLINED_BITMAP);
            if (outlined == null) {
                notifyFailure(getActivity());
                return;
            }
            createImoji(outlined);
        }
    }


    @Override
    public void onOutlinedBitmapReady(Bitmap outlinedBitmap) {
        EditorBitmapCache.getInstance().put(EditorBitmapCache.Keys.OUTLINED_BITMAP, outlinedBitmap);
        createImoji(outlinedBitmap);
    }

    private void createImoji(Bitmap outlinedBitmap) {
        ImojiApi.with(mAppContext).createImoji(outlinedBitmap, mTags, new Callback<Imoji, String>() {
            @Override
            public void onSuccess(Imoji result) {
                mIsDone = true;
                mResultImoji = result;
                if (isAdded()) {
                    Activity a = getActivity();
                    notifySuccess(result, a);
                }
            }

            @Override
            public void onFailure(String result) {
                mIsDone = true;
                if (isAdded()) {
                    Activity a = getActivity();
                    notifyFailure(a);
                }
            }
        });
    }

    private void notifyFailure(Activity a) {
        EditorBitmapCache.getInstance().clearNonOutlinedBitmaps();
        a.setResult(Activity.RESULT_CANCELED, null);
        a.finish();
    }

    private void notifySuccess(Imoji result, Activity a) {
        EditorBitmapCache.getInstance().clearNonOutlinedBitmaps();
        Intent intent = new Intent();
        intent.putExtra(ImojiEditorActivity.IMOJI_MODEL_BUNDLE_ARG_KEY, result);
        a.setResult(Activity.RESULT_OK, intent);
        a.finish();
    }
}
