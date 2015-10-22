package com.imojiapp.imoji.sdk.ui.sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.imojiapp.imoji.sdk.Imoji;
import com.imojiapp.imoji.sdk.ui.CreateTaskFragment;
import com.imojiapp.imoji.sdk.ui.ImojiEditorActivity;
import com.imojiapp.imoji.sdk.ui.ImojiEditorFragment;
import com.imojiapp.imoji.sdk.ui.ImojiIntents;
import com.imojiapp.imoji.sdk.ui.utils.EditorBitmapCache;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ImageView mOutlinedImoji;
    private ImojiCreateReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOutlinedImoji = (ImageView) findViewById(R.id.iv_outlined_imoji);

        mReceiver = new ImojiCreateReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(ImojiIntents.Create.IMOJI_CREATE_INTERNAL_INTENT_ACTION));

        if (savedInstanceState == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample, options);
            EditorBitmapCache.getInstance().put(EditorBitmapCache.Keys.INPUT_BITMAP, bitmap);
            Intent intent = new Intent(this, ImojiEditorActivity.class);
            intent.putExtra(ImojiEditorActivity.RETURN_IMMEDIATELY_BUNDLE_ARG_KEY, false);
            intent.putExtra(ImojiEditorActivity.TAG_IMOJI_BUNDLE_ARG_KEY, false);
            startActivityForResult(intent, ImojiEditorActivity.START_EDITOR_REQUEST_CODE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ImojiEditorActivity.START_EDITOR_REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK) {
            Imoji model = data.getParcelableExtra(ImojiEditorActivity.IMOJI_MODEL_BUNDLE_ARG_KEY);
            if (model != null) {
                Log.d(LOG_TAG, "imoji id: " + model.getImojiId());
                mOutlinedImoji.setImageBitmap(EditorBitmapCache.getInstance().get(EditorBitmapCache.Keys.OUTLINED_BITMAP));
            } else {
                String token = data.getStringExtra(ImojiEditorActivity.CREATE_TOKEN_BUNDLE_ARG_KEY);
                Log.d(LOG_TAG, "we got a token: " + token);
                mOutlinedImoji.setImageBitmap(EditorBitmapCache.getInstance().get(EditorBitmapCache.Keys.OUTLINED_BITMAP));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    public class ImojiCreateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean status = intent.getBooleanExtra(ImojiIntents.Create.STATUS_BUNDLE_ARG_KEY, false);
            if (status) { //success?
                Imoji imoji = intent.getParcelableExtra(ImojiIntents.Create.IMOJI_MODEL_BUNDLE_ARG_KEY);
                String token = intent.getStringExtra(ImojiIntents.Create.CREATE_TOKEN_BUNDLE_ARG_KEY);
                Toast.makeText(MainActivity.this, "got imoji: " + imoji.getImojiId(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "imoji creation failed", Toast.LENGTH_LONG).show();
            }
        }
    }
}
