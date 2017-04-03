package com.bureau.bureauapp.flavor;

import android.content.Context;

import com.bureau.bureauapp.myapplication.MyApplication;
import com.layer.atlas.provider.ParticipantProvider;
import com.bureau.bureauapp.util.AuthenticationProvider;
import com.bureau.bureauapp.util.Log;
import com.layer.sdk.LayerClient;

public class Flavor implements MyApplication.Flavor {
    // Set your Layer MyApplication ID from your Layer developer dashboard to bypass the QR-Code scanner.
//    private final static String LAYER_APP_ID = "layer:///apps/staging/3ef201c8-3e25-11e6-b784-0c5100000e8e";
//    private final static String LAYER_APP_ID = "layer:///apps/staging/238530d8-995f-11e5-9461-6ac9d8033a8c";
    private final static String LAYER_APP_ID = "layer:///apps/staging/48d4dfb0-d671-11e5-aaf6-b68b07004518";
    //    private final static String GCM_SENDER_ID = "748607264448";  // Sekhar
    private final static String GCM_SENDER_ID = "145321124612";
    private String mLayerAppId;


    //==============================================================================================
    // Layer MyApplication ID (from LAYER_APP_ID constant or set by QR-Code scanning AppIdScanner Activity
    //==============================================================================================

    @Override
    public String getLayerAppId() {
        // In-memory cached MyApplication ID?
        if (mLayerAppId != null) {
            return mLayerAppId;
        }

        // Constant MyApplication ID?
        if (LAYER_APP_ID != null) {
            if (Log.isLoggable(Log.VERBOSE)) {
                Log.v("Using constant `MyApplication.LAYER_APP_ID` MyApplication ID: " + LAYER_APP_ID);
            }
            mLayerAppId = LAYER_APP_ID;
            return mLayerAppId;
        }

        // Saved MyApplication ID?
        String saved = MyApplication.getInstance()
                .getSharedPreferences("layerAppId", Context.MODE_PRIVATE)
                .getString("layerAppId", null);
        if (saved == null) return null;
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Loaded Layer MyApplication ID: " + saved);
        mLayerAppId = saved;
        return mLayerAppId;
    }

    /**
     * Sets the current Layer MyApplication ID, and saves it for use next time (to bypass QR code scanner).
     *
     * @param appId Layer MyApplication ID to use when generating a LayerClient.
     */
    protected static void setLayerAppId(String appId) {
        appId = appId.trim();
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Saving Layer MyApplication ID: " + appId);
        MyApplication.getInstance().getSharedPreferences("layerAppId", Context.MODE_PRIVATE).edit()
                .putString("layerAppId", appId).commit();
    }


    //==============================================================================================
    // Generators
    //==============================================================================================

    @Override
    public LayerClient generateLayerClient(Context context, LayerClient.Options options) {
        // If no MyApplication ID is set yet, return `null`; we'll launch the AppIdScanner to get one.
        String appId = getLayerAppId();
        if (appId == null) return null;

        options.googleCloudMessagingSenderId(GCM_SENDER_ID);
//        options.broadcastPushInForeground(true);
        return LayerClient.newInstance(context, appId, options);
    }

    @Override
    public ParticipantProvider generateParticipantProvider(Context context, AuthenticationProvider authenticationProvider) {
        return new DemoParticipantProvider(context).setLayerAppId(getLayerAppId());
    }

    @Override
    public AuthenticationProvider generateAuthenticationProvider(Context context) {
        return new DemoAuthenticationProvider(context);
    }
}