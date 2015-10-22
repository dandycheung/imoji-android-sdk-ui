package com.imojiapp.imoji.sdk.ui;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.imojiapp.imoji.sdk.Callback;
import com.imojiapp.imoji.sdk.Imoji;
import com.imojiapp.imoji.sdk.ImojiApi;
import com.imojiapp.imoji.sdk.ui.utils.EditorBitmapCache;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by sajjadtabib on 10/21/15.
 */
public class ImojiCreateService extends IntentService {

    public static final String CREATE_TOKEN_BUNDLE_ARG_KEY = "CREATE_TOKEN_BUNDLE_ARG_KEY";
    public static final String TAGS_BUNDLE_ARG_KEY = "TAGS_BUNDLE_ARG_KEY";
    private static final String LOG_TAG = ImojiCreateService.class.getSimpleName();
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ImojiCreateService() {
        super(ImojiCreateService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String token = intent.getStringExtra(CREATE_TOKEN_BUNDLE_ARG_KEY); //the token that will contain the bitmap in memory and also used to bind to the newly created imoji
        List<String> tags = intent.getStringArrayListExtra(TAGS_BUNDLE_ARG_KEY);

        Bitmap b = EditorBitmapCache.getInstance().get(token);
        if (b == null) {
            //notify failure
            notifyFailure(token);
            return;
        }


        final CountDownLatch latch = new CountDownLatch(1);
        ImojiApi.with(this).createImoji(b, tags, new Callback<Imoji, String>() {
            @Override
            public void onSuccess(Imoji result) {
                notifySuccess(result, token);
                latch.countDown();
            }

            @Override
            public void onFailure(String result) {
                notifyFailure(token);
                latch.countDown();
            }
        });

        waitForCreateToFinish(latch, token);

    }

    private void waitForCreateToFinish(CountDownLatch latch, String token) {
        //wait for the response to come back because we rely on it to notify using the broadcast receivers
        try {
            latch.await(30000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            notifyFailure(token);
        }
    }

    private void notifyFailure(String token) {

        Intent intent = new Intent();
        intent.setAction(ImojiIntents.Create.IMOJI_CREATE_INTERNAL_INTENT_ACTION);
        intent.putExtra(ImojiIntents.Create.STATUS_BUNDLE_ARG_KEY, false);
        intent.putExtra(ImojiIntents.Create.CREATE_TOKEN_BUNDLE_ARG_KEY, token);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);

    }

    private void notifySuccess(Imoji imoji, String token) {

        Intent intent = new Intent();
        intent.setAction(ImojiIntents.Create.IMOJI_CREATE_INTERNAL_INTENT_ACTION);
        intent.putExtra(ImojiIntents.Create.STATUS_BUNDLE_ARG_KEY, true);
        intent.putExtra(ImojiIntents.Create.CREATE_TOKEN_BUNDLE_ARG_KEY, token);

        intent.putExtra(ImojiIntents.Create.IMOJI_MODEL_BUNDLE_ARG_KEY, imoji);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
    }
}
