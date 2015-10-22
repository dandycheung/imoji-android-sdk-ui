package com.imojiapp.imoji.sdk.ui;

/**
 * Created by sajjadtabib on 10/21/15.
 */
public class ImojiIntents {

    public interface Create{

        //Intent Action
        String IMOJI_CREATE_INTERNAL_INTENT_ACTION = "IMOJI_CREATE_INTERNAL_INTENT_ACTION";

        // Intent Extra Argument Key - Type: Boolean - true for success
        String STATUS_BUNDLE_ARG_KEY = "STATUS_BUNDLE_ARG_KEY";

        // Intent Extra Argument Key - Type: Imoji, retrieve as a parcelable
        String IMOJI_MODEL_BUNDLE_ARG_KEY = "IMOJI_MODEL_BUNDLE_ARG_KEY";

        String CREATE_TOKEN_BUNDLE_ARG_KEY = "CREATE_TOKEN_BUNDLE_ARG_KEY";
    }
}
