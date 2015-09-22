package com.imojiapp.imoji.sdk.ui.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by sajjadtabib on 9/18/14.
 */
public class BitmapUtils {
    private static final String LOG_TAG = BitmapUtils.class.getSimpleName();

    public static int[] getSizeWithinBounds(int width, int height, int boundsWidth, int boundsHeight, boolean expandToFitBounds){
        int[] size = new int[2];

        //if we fit within the bounds then don't scale
        if(!expandToFitBounds && (width <= boundsWidth && height <= boundsHeight)){
            size[0] = width;
            size[1] = height;
            return size;
        }

        //get the aspect ratio of the original size
        float originalAspectRatio = (float)width / (float) height;
        float boundsAspectRatio = (float)boundsWidth / (float) boundsHeight;

        if(originalAspectRatio > boundsAspectRatio){
            size[0] = boundsWidth;
            size[1] = (int)( (float)boundsWidth / originalAspectRatio);
        }else{
            size[1] = boundsHeight;
            size[0] = (int)( (float)boundsHeight * originalAspectRatio);
        }

        Log.d(LOG_TAG, "original [" + width + ", " + height + "] " + "bounds [" + boundsWidth + ", " + boundsHeight + "] " + "new [" + size[0] + ", " + size[1]+ "]"  );

        return size;
    }

    public static boolean saveAsPng(Context context, final Bitmap bitmap, final String fileName, final int widthBound, final int heightBound, int[] resizeInfo ) throws FileNotFoundException {
        int retries = 0;
        boolean status = false;
        Bitmap target = bitmap;

        int[] newSize = null;
        if(widthBound > 0 && heightBound > 0){
            newSize = BitmapUtils.getSizeWithinBounds(bitmap.getWidth(), bitmap.getHeight(), widthBound, heightBound, false);
            resizeInfo[0] = newSize[0];
            resizeInfo[1] = newSize[1];
        }

        do {
            File f = new File(fileName);
            FileOutputStream fs = context.openFileOutput(f.getName(), Context.MODE_PRIVATE);

            if (newSize != null) {
                target = Bitmap.createScaledBitmap(bitmap, newSize[0], newSize[1], false);
            }

            status = target.compress(Bitmap.CompressFormat.PNG, 100, fs);

            if (!status) {
                Log.e(LOG_TAG, "couldn't compress image");
            } else {
                break;
            }

            ++retries;
        }while(retries < 2); //save the original in png

        return status;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int[] getImageSize(Uri file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //Returns null, sizes are in the options variable
        BitmapFactory.decodeFile(file.getPath(), options);
        int width = options.outWidth;
        int height = options.outHeight;

        int[] size = new int[2];
        size[0] = width;
        size[1] = height;

        return size;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromUri(Uri imageUri,
                                                    int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        File file = new File(imageUri.getPath());
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    public static class ResizeTask extends AsyncTask<Bitmap, Integer, Bitmap>{


        private volatile int mWidth;
        private volatile int mHeight;

        public ResizeTask(int newWidth, int newHeight) {
            mWidth = newWidth;
            mHeight = newHeight;
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            return Bitmap.createScaledBitmap(params[0], mWidth, mHeight, false);
        }
    }

}
