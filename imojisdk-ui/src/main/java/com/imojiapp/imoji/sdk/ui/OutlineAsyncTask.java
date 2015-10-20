package com.imojiapp.imoji.sdk.ui;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.imojiapp.imoji.sdk.BitmapUtils;
import com.imojiapp.imojigraphics.IG;

import java.lang.ref.WeakReference;

/**
 * Created by sajjadtabib on 10/19/15.
 */
class OutlineAsyncTask extends AsyncTask<Void, Void, Bitmap> {
    private final Bitmap mImojiBitmap;
    private final int mWidth;
    private final int mHeight;
    private WeakReference<OutlinedBitmapReadyListener> mListenerWeakReference;

    public OutlineAsyncTask(Bitmap imojiBitmap, int width, int height, OutlinedBitmapReadyListener f) {
        mListenerWeakReference = new WeakReference<>(f);
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
        OutlinedBitmapReadyListener outlinedBitmapReadyListener = mListenerWeakReference.get();

        if (outlinedBitmapReadyListener != null) {
            outlinedBitmapReadyListener.onOutlinedBitmapReady(b);
        }
    }

    public interface OutlinedBitmapReadyListener {
        void onOutlinedBitmapReady(Bitmap outlinedBitmap);
    }
}
