package com.imojiapp.imoji.sdk.ui.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by sajjadtabib on 9/23/15.
 */
public final class EditorBitmapCache extends LruCache<String, Bitmap> {

    public interface Keys{
        String INPUT_BITMAP = "INPUT_BITMAP";
        String TRIMMED_BITMAP = "TRIMMED_BITMAP";
        String OUTLINED_BITMAP = "OUTLINED_BITMAP";
    }

    private static EditorBitmapCache sEditorBitmapCache;

    public static EditorBitmapCache getInstance() {
        if (sEditorBitmapCache == null) {
            synchronized (EditorBitmapCache.class) {
                if (sEditorBitmapCache == null) {
                    sEditorBitmapCache = new EditorBitmapCache(3);
                }
            }
        }

        return sEditorBitmapCache;
    }

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    private EditorBitmapCache(int maxSize) {
        super(maxSize);
    }

}
