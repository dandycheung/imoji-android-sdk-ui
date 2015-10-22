package com.imojiapp.imoji.sdk.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.imojiapp.imoji.sdk.ImojiApi;
import com.imojiapp.imoji.sdk.ui.utils.EditorBitmapCache;

import java.util.Collections;

public class ImojiEditorActivity extends AppCompatActivity{

    public static final int START_EDITOR_REQUEST_CODE = 1001;
    public static final String IMOJI_MODEL_BUNDLE_ARG_KEY = "IMOJI_MODEL_BUNDLE_ARG_KEY";
    public static final String CREATE_TOKEN_BUNDLE_ARG_KEY = "CREATE_TOKEN_BUNDLE_ARG_KEY";


    public static final String TAG_IMOJI_BUNDLE_ARG_KEY = "TAG_IMOJI_BUNDLE_ARG_KEY";
    public static final String RETURN_IMMEDIATELY_BUNDLE_ARG_KEY = "RETURN_IMMEDIATELY_BUNDLE_ARG_KEY";


    private static final String LOG_TAG = ImojiEditorActivity.class.getSimpleName();
    private ImojiEditorFragment mImojiEditorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imoji_editor);

        if (savedInstanceState == null) {
            boolean tagImojis = getIntent().getBooleanExtra(TAG_IMOJI_BUNDLE_ARG_KEY, true);
            Bitmap inputBitmap = EditorBitmapCache.getInstance().get(EditorBitmapCache.Keys.INPUT_BITMAP);
            boolean returnImmediately = getIntent().getBooleanExtra(RETURN_IMMEDIATELY_BUNDLE_ARG_KEY, false);
            if (inputBitmap == null) { //no need to continue

                setResult(Activity.RESULT_CANCELED, null);
                finish();
                return;
            }
            mImojiEditorFragment = ImojiEditorFragment.newInstance(tagImojis, returnImmediately);
            mImojiEditorFragment.setEditorBitmap(inputBitmap);
            getSupportFragmentManager().beginTransaction().add(R.id.container, mImojiEditorFragment, ImojiEditorFragment.FRAGMENT_TAG).commit();
        } else {
            mImojiEditorFragment = (ImojiEditorFragment) getSupportFragmentManager().findFragmentByTag(ImojiEditorFragment.FRAGMENT_TAG);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

//        if (Build.VERSION.SDK_INT >= 19) {
//            View decorView = getWindow().getDecorView();
//            int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
//            decorView.setSystemUiVisibility(uiOptions);
//
//        }else if (Build.VERSION.SDK_INT >= 16) {
//            View decorView = getWindow().getDecorView();
//// Hide the status bar.
//            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//            decorView.setSystemUiVisibility(uiOptions);
//        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {


        }
    }

    ImojiEditorFragment getImojiEditorFragment() {
        return mImojiEditorFragment;
    }


}
