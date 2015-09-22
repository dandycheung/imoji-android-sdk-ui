package com.imojiapp.imoji.sdk.ui.sample;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.imojiapp.imoji.sdk.ui.ImojiEditorFragment;

public class MainActivity extends AppCompatActivity {

    private ImojiEditorFragment mImojiEditorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (savedInstanceState == null) {
            mImojiEditorFragment = new ImojiEditorFragment();
            mImojiEditorFragment.setEditorBitmap(((BitmapDrawable)ContextCompat.getDrawable(getBaseContext(), R.drawable.sample)).getBitmap());
            getSupportFragmentManager().beginTransaction().add(R.id.container, mImojiEditorFragment, ImojiEditorFragment.FRAGMENT_TAG).commit();
        } else {
            mImojiEditorFragment = (ImojiEditorFragment) getSupportFragmentManager().findFragmentByTag(ImojiEditorFragment.FRAGMENT_TAG);
        }
    }

}
