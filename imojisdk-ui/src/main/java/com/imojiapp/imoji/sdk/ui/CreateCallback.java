package com.imojiapp.imoji.sdk.ui;

import android.app.Activity;
import android.content.Intent;

import com.imojiapp.imoji.sdk.Callback;
import com.imojiapp.imoji.sdk.Imoji;

import java.lang.ref.WeakReference;

/**
 * Created by sajjadtabib on 10/19/15.
 */
class CreateCallback implements Callback<Imoji, String> {

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
