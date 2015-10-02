package com.imojiapp.imoji.sdk.ui.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by sajjadtabib on 10/1/15.
 */
public class DisplayUtils {


    public static float getAspectRatio(Point point) {
        return point.x / point.y;
    }

    public static float getWindowAspectRatio(Context context) {
        Point p = getWindowSize(context);
        return getAspectRatio(p);
    }

    public static Point getWindowSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point windowDisplaySize = new Point();
        display.getSize(windowDisplaySize);
        return windowDisplaySize;
    }

}
