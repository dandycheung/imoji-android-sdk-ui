# Imoji UI SDK Alpha 
The imoji ui sdk for Android is a library project that will provide out of the box ui elements for displaying and creating imojis. Development on the ui sdk recently started, and at this time, an editor that lets you create imojis is available for integration. More components will be added soon.

## SDK Integration
1. Register [here.](https://developer.imoji.io/developer/sdk#/home) to get a client id and secret

2. Add the Imoji UI SDK as a dependency in your build.gradle file.
    ```
    compile('com.imojiapp:imoji-sdk-ui:+@aar'){
       transitive=true
    }
    ``` 

3. Create an Application class or update your current application:
```java
public class MyApplication extends Application {
 @Override
    public void onCreate() {
        super.onCreate();
        ImojiApi.init(this, "YOUR_CLIENT_ID_HERE", "YOUR_CLIENT_SECRET_HERE");
    }
}
```
4. Add the ImojiEditorActivity to your `AndroidManifest.xml` file. You may apply any theme you like, but make sure the theme is xxx.NoActionBar. In other words, it does not use the ActionBar. More on theming in the next item.
    ```xml
    <activity
        android:windowSoftInputMode="adjustNothing"
        android:name="com.imojiapp.imoji.sdk.ui.ImojiEditorActivity"
        android:theme="@style/AppTheme.NoActionBar"></activity>
    ```

5. Editor theming.
The editor requires that you have  appropriately set `colorPrimary`, `colorPrimaryDark`, and `colorAccent` attributes because the default coloring of the editor will depend on those. Therefore, in your styles.xml where you define your style, make sure that these are set:
    ```xml
       <style name="AppTheme.NoActionBar" parent="Theme.AppCompat.Light.NoActionBar">
            <!-- Customize your theme here. -->
            <item name="colorPrimary">@color/colorPrimary</item>
            <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
            <item name="colorAccent">@color/colorAccent</item>
            <item name="windowActionBar">false</item>
            <item name="windowNoTitle">true</item>
            <item name="imoji__editorToolbarTheme">@style/ThemeOverlay.AppCompat.Dark.ActionBar</item>
        </style>
    ```
    You can also customize the toolbars used in the editor by setting the `imoji__editorToolbarTheme` attribute to whatever style you wish. You can see this in the code snippet above.

6. SDK Integration Complete

## Imoji Creator
Now that you have integrated the SDK, you can use the imoji creator in your app. To create an imoji, follow these steps:

1. Create a `Bitmap` of the image you would like to convert into an imoji.

2. Put the `Bitmap` into the EditorBitmapCache using the `EditorBitmapCache.Keys.INPUT_BITMAP` cache key.
 ```java
 EditorBitmapCache.getInstance().put(EditorBitmapCache.Keys.INPUT_BITMAP, bitmap);
 ```

3. Launch the `ImojiEditorActivity` using the `START_EDITOR_REQUEST_CODE`
 ```java
 Intent intent = new Intent(this, ImojiEditorActivity.class);
 startActivityForResult(intent, ImojiEditorActivity.START_EDITOR_REQUEST_CODE);
 ```
4. Optional: Skip tagging
    You can optionally skip tagging by setting the intent key `ImojiEditorActivity.TAG_IMOJI_BUNDLE_ARG_KEY` to `false` when starting ImojiEditorActivity.
    Note, however, that the imoji will NOT be searchable, so your users will not be able to find the imoji by searching tags.

5. Optional: Return Outlined Image Immediately, while returning the Imoji object Asynchronously
    You can optionally receive the outlined imoji bitmap immediately by setting the intent key `ImojiEditorActivity.RETURN_IMMEDIATELY_BUNDLE_ARG_KEY` to `true` when starting ImojiEditorActivity.
    The imoji object will be returned to you in a LocalBroadcast. You will need to register a receiver like such:
    
    ```java
    public void onCreate(Bundle savedInstanceState){
       super.onCreate(savedInstanceState);
       
       ...
       
       ImojiCreateReceiver mReceiver = new ImojiCreateReceiver(); 
       LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(ImojiIntents.Create.IMOJI_CREATE_INTERNAL_INTENT_ACTION));
    
    }
    ```
    
    Here's a sample receiver:
    
    ```java
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
    ```
 
6. Override `onActivityResult` in your Activity. If the Activity result was `RESULT_OK`, you can obtained the outlined imoji bitmap from the `EditorBitmapCache` using the `EditorBitmapCache.Keys.OUTLINED_BITMAP` cache key.
   Note that if you set the `ImojiEditorActivity.RETURN_IMMEDIATELY_BUNDLE_ARG_KEY` to `true` then you will not receive the imoji object but will instead get a token to match with your receiver from step 5. 
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (ImojiEditorActivity.START_EDITOR_REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK) {
        Log.d(LOG_TAG, "imoji id: " + model.getImojiId());
        mOutlinedImoji.setImageBitmap(EditorBitmapCache.getInstance().get(EditorBitmapCache.Keys.OUTLINED_BITMAP));
    }
}
```
