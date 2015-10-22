package com.imojiapp.imoji.sdk.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.imojiapp.imoji.sdk.Imoji;
import com.imojiapp.imoji.sdk.ui.utils.EditorBitmapCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sajjadtabib on 10/21/15.
 */
public class OutlineTaskFragment extends Fragment implements OutlineAsyncTask.OutlinedBitmapReadyListener{


    public static final String FRAGMENT_TAG = OutlineTaskFragment.class.getSimpleName();
    private static final String LOG_TAG = OutlineTaskFragment.class.getSimpleName();
    public static final String CREATE_TOKEN_BUNDLE_ARG_KEY = "CREATE_TOKEN_BUNDLE_ARG_KEY";

    public static OutlineTaskFragment newInstance(String token) {
        OutlineTaskFragment f = new OutlineTaskFragment();

        Bundle args = new Bundle();
        args.putString(CREATE_TOKEN_BUNDLE_ARG_KEY, token);

        f.setArguments(args);

        return f;
    }

    private String mToken;
    private List<String> mTags;
    private boolean mIsDone;
    private boolean mSuccess;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mIsDone) {
            if (mSuccess) {
                notifySuccess(mToken, activity);
            } else {
                notifyFailure(activity);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mToken = getArguments().getString(CREATE_TOKEN_BUNDLE_ARG_KEY);

        Bitmap b = EditorBitmapCache.getInstance().get(mToken);
        if (b == null) {
            Log.w(LOG_TAG, "token was not set to create outline");
            notifyFailure(getActivity());
            return;
        }

        OutlineAsyncTask task = new OutlineAsyncTask(b, ImojiContants.IMOJI_WIDTH_BOUND, ImojiContants.IMOJI_HEIGHT_BOUND, this);
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    private void notifyFailure(Activity a) {
        a.setResult(Activity.RESULT_CANCELED, null);
        a.finish();
    }

    private void notifySuccess(String token, Activity a) {
        Intent intent = new Intent();
        intent.putExtra(ImojiEditorActivity.CREATE_TOKEN_BUNDLE_ARG_KEY, token);
        a.setResult(Activity.RESULT_OK, intent);
        a.finish();
    }

    @Override
    public void onOutlinedBitmapReady(Bitmap outlinedBitmap) {

        //remove the trimmed bitmap from the cache that is attached to the token
        EditorBitmapCache.getInstance().remove(mToken);

        if (outlinedBitmap == null) {
            mSuccess = false;
            mIsDone = true;
            if (isAdded()) {
                notifyFailure(getActivity());
            }
            return;
        }

        EditorBitmapCache.getInstance().put(EditorBitmapCache.Keys.OUTLINED_BITMAP, outlinedBitmap);

        mIsDone = true;
        mSuccess = true;

        if (isAdded()) {
            notifySuccess(mToken, getActivity());
        }
    }
}
