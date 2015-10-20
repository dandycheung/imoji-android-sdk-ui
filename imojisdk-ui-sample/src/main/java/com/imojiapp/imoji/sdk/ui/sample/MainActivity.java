package com.imojiapp.imoji.sdk.ui.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.imojiapp.imoji.sdk.Imoji;
import com.imojiapp.imoji.sdk.ui.CreateTaskFragment;
import com.imojiapp.imoji.sdk.ui.ImojiEditorActivity;
import com.imojiapp.imoji.sdk.ui.ImojiEditorFragment;
import com.imojiapp.imoji.sdk.ui.utils.EditorBitmapCache;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ImageView mOutlinedImoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOutlinedImoji = (ImageView) findViewById(R.id.iv_outlined_imoji);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


        if (savedInstanceState == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample, options);
            EditorBitmapCache.getInstance().put(EditorBitmapCache.Keys.INPUT_BITMAP, bitmap);
            Intent intent = new Intent(this, ImojiEditorActivity.class);
//            intent.putExtra(ImojiEditorActivity.TAG_IMOJI_BUNDLE_ARG_KEY, false);
            startActivityForResult(intent, ImojiEditorActivity.START_EDITOR_REQUEST_CODE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ImojiEditorActivity.START_EDITOR_REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK) {
            Imoji model = data.getParcelableExtra(ImojiEditorActivity.IMOJI_MODEL_BUNDLE_ARG_KEY);
            Log.d(LOG_TAG, "imoji id: " + model.getImojiId());
            mOutlinedImoji.setImageBitmap(EditorBitmapCache.getInstance().get(EditorBitmapCache.Keys.OUTLINED_BITMAP));
        }
    }
}
