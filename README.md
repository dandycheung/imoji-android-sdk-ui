# Imoji UI SDK Alpha 
The imoji ui sdk for Android is a library project that will provide out of the box ui elements for displaying and creating imojis. Development on the ui sdk recently started, and at this time, an editor that lets you create imojis is available for integration. More components will be added soon.

## SDK Integration
###1. Register [here.](https://developer.imoji.io/developer/sdk#/home) to get a client id and secret

###2. Import the project as a module into your app. *Note: a maven artificat will be released soon, but for now you will have to import*

###3. Create an Application class or update your current application:
```java
public class MyApplication extends Application {
 @Override
    public void onCreate() {
        super.onCreate();
        ImojiApi.init(this, "YOUR_CLIENT_ID_HERE", "YOUR_CLIENT_SECRET_HERE");
    }
}
```
###4. SDK Integration Complete

## Imoji Creator
Now that you have integrated the SDK, you can use the imoji creator in your app. To create an imoji, follow these steps:

###1. Create a `Bitmap` of the image you would like to convert into an imoji.

###2. Put the `Bitmap` into the EditorBitmapCache using the `EditorBitmapCache.Keys.INPUT_BITMAP` cache key.
```java
EditorBitmapCache.getInstance().put(EditorBitmapCache.Keys.INPUT_BITMAP, bitmap);
```

###3. Launch the `ImojiEditorActivity` using the `START_EDITOR_REQUEST_CODE`
```java
Intent intent = new Intent(this, ImojiEditorActivity.class);
startActivityForResult(intent, ImojiEditorActivity.START_EDITOR_REQUEST_CODE);
```

###4. Override `onActivityResult` in your Activity. If the Activity result was `RESULT_OK`, you can obtained the outlined imoji bitmap from the `EditorBitmapCache` using the `EditorBitmapCache.Keys.OUTLINED_BITMAP` cache key.
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
