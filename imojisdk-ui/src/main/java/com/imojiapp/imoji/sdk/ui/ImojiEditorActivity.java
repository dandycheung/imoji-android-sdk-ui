package com.imojiapp.imoji.sdk.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.imojiapp.imoji.sdk.ui.utils.EditorBitmapCache;

public class ImojiEditorActivity extends AppCompatActivity {

    public static final int START_EDITOR_REQUEST_CODE = 1001;
    public static final String IMOJI_MODEL_BUNDLE_ARG_KEY = "IMOJI_MODEL_BUNDLE_ARG_KEY";
    private static final String LOG_TAG = ImojiEditorActivity.class.getSimpleName();
    private ImojiEditorFragment mImojiEditorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imoji_editor);

        if (savedInstanceState == null) {
            Bitmap inputBitmap = EditorBitmapCache.getInstance().get(EditorBitmapCache.Keys.INPUT_BITMAP);
            if (inputBitmap == null) { //no need to continue

                setResult(Activity.RESULT_CANCELED, null);
                finish();
                return;
            }
            mImojiEditorFragment = new ImojiEditorFragment();
            mImojiEditorFragment.setEditorBitmap(inputBitmap);
            getSupportFragmentManager().beginTransaction().add(R.id.container, mImojiEditorFragment, ImojiEditorFragment.FRAGMENT_TAG).commit();
        } else {
            mImojiEditorFragment = (ImojiEditorFragment) getSupportFragmentManager().findFragmentByTag(ImojiEditorFragment.FRAGMENT_TAG);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
    }
    
    ImojiEditorFragment getImojiEditorFragment() {
        return mImojiEditorFragment;
    }
}
