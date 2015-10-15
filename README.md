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

```
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
4. Override `onActivityResult` in your Activity. If the Activity result was `RESULT_OK`, you can obtained the outlined imoji bitmap from the `EditorBitmapCache` using the `EditorBitmapCache.Keys.OUTLINED_BITMAP` cache key.
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
