package com.bureau.bureauapp.myapplication;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.bureau.bureauapp.BuildConfig;
import com.bureau.bureauapp.R;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.urbanairship.CustomDefaultNotificationFactory;
import com.bureau.bureauapp.util.AuthenticationProvider;
import com.bureau.bureauapp.util.Log;
import com.digits.sdk.android.Digits;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.layer.atlas.messagetypes.text.TextCellFactory;
import com.layer.atlas.messagetypes.threepartimage.ThreePartImageUtils;
import com.layer.atlas.provider.ParticipantProvider;
import com.layer.atlas.util.Util;
import com.layer.atlas.util.picasso.requesthandlers.MessagePartRequestHandler;
import com.layer.sdk.LayerClient;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.urbanairship.UAirship;

import java.util.Arrays;

import io.fabric.sdk.android.Fabric;
import io.smooch.core.Settings;
import io.smooch.core.Smooch;
import io.smooch.core.User;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import static com.bureau.bureauapp.helperclasses.BureauConstants.TWITTER_KEY;
import static com.bureau.bureauapp.helperclasses.BureauConstants.TWITTER_SECRET;

/**
 * MyApplication provides static access to a LayerClient and other Atlas and Messenger context, including
 * AuthenticationProvider, ParticipantProvider, Participant, and Picasso.
 * <p>
 * MyApplication.ProviderrailsFlavor allows build variants to target different environments, such as the Atlas Demo and the
 * open source Rails Identity Provider.  Switch flavors with the Android Studio `Build Variant` tab.
 * When using a flavor besides the Atlas Demo you must manually set your Layer MyApplication ID and GCM Sender
 * ID in that flavor's ProviderrailsFlavor.java.
 *
 * @see LayerClient
 * @see ParticipantProvider
 * @see Picasso
 * @see AuthenticationProvider
 */
public class MyApplication extends Application {

    private final String LOG_TAG = MyApplication.class.getSimpleName();
    String APP_TOKEN = "98vczyf814ei6h4nyyasqhnxv";

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
//    private static final String TWITTER_KEY = "MbG4g1RZmHiSUCezXDPl3CPad";
//    private static final String TWITTER_SECRET = "nLlJ9m6iJwLaFcczpCVuTrWHvkxy2sDluNdDLWiWMzGZnBthgx";

    private static Application sInstance;
    private static Flavor sFlavor = new com.bureau.bureauapp.flavor.Flavor();

    private static LayerClient sLayerClient;
    private static ParticipantProvider sParticipantProvider;
    private static AuthenticationProvider sAuthProvider;
    private static Picasso sPicasso;
    public static UAirship mUAirship;
    //==============================================================================================
    // Application Overrides
    //==============================================================================================

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * For Custom Font
         * */
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Comfortaa-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        /*
        * Urban airship Notification Enable
        * */
        UAirship.takeOff(this, new UAirship.OnReadyCallback() {
            @Override
            public void onAirshipReady(UAirship airship) {
                mUAirship = airship;
                // Enable user notifications
                airship.getPushManager().setUserNotificationsEnabled(true);

                // Create a customized default notification factory
                CustomDefaultNotificationFactory notificationFactory;
                notificationFactory = new CustomDefaultNotificationFactory(UAirship.getApplicationContext());

                // Set the factory on the PushManager
                airship.getPushManager().setNotificationFactory(notificationFactory);
            }
        });

        Settings settings = new Settings(APP_TOKEN);
        settings.setUserId(AppData.getString(MyApplication.this, AppData.getString(this, BureauConstants.userid)));
        Smooch.init(this, settings);

        User.getCurrentUser().setFirstName(AppData.getString(MyApplication.this, BureauConstants.profileFirstName));
        User.getCurrentUser().setLastName(AppData.getString(MyApplication.this, BureauConstants.profileLastName));

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build());
//        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
//        Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build(), new Crashlytics());

        // Enable verbose logging in debug builds
        if (BuildConfig.DEBUG) {
            com.layer.atlas.util.Log.setLoggingEnabled(true);
            Log.setAlwaysLoggable(true);
            LayerClient.setLoggingEnabled(this, true);
        }

        // Allow the LayerClient to track app state
        LayerClient.applicationCreated(this);
        sInstance = this;

        //Facebook Signup process
        FacebookSdk.sdkInitialize(MyApplication.this);
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Application getInstance() {
        return sInstance;
    }


    //==============================================================================================
    // Identity Provider Methods
    //==============================================================================================

    /**
     * Routes the user to the proper Activity depending on their authenticated state.  Returns
     * `true` if the user has been routed to another Activity, or `false` otherwise.
     *
     * @param from Activity to route from.
     * @return `true` if the user has been routed to another Activity, or `false` otherwise.
     */
    public static boolean routeLogin(Activity from) {
        return getAuthenticationProvider().routeLogin(getLayerClient(), getLayerAppId(), from);
    }

    /**
     * Authenticates with the AuthenticationProvider and Layer, returning asynchronous results to
     * the provided callback.
     *
     * @param credentials Credentials associated with the current AuthenticationProvider.
     * @param callback    Callback to receive authentication results.
     */
    @SuppressWarnings("unchecked")
    public static void authenticate(Object credentials, AuthenticationProvider.Callback callback) {
        LayerClient client = getLayerClient();
        if (client == null) return;
        String layerAppId = getLayerAppId();
        if (layerAppId == null) return;
        getAuthenticationProvider()
                .setCredentials(credentials)
                .setCallback(callback);
        client.authenticate();
    }

    /**
     * Deauthenticates with Layer and clears cached AuthenticationProvider credentials.
     *
     * @param callback Callback to receive deauthentication success and failure.
     */
    public static void deauthenticate(final Util.DeauthenticationCallback callback) {
        Util.deauthenticate(getLayerClient(), new Util.DeauthenticationCallback() {
            @Override
            @SuppressWarnings("unchecked")
            public void onDeauthenticationSuccess(LayerClient client) {
                getAuthenticationProvider().setCredentials(null);
                callback.onDeauthenticationSuccess(client);
            }

            @Override
            public void onDeauthenticationFailed(LayerClient client, String reason) {
                callback.onDeauthenticationFailed(client, reason);
            }
        });
    }


    //==============================================================================================
    // Getters / Setters
    //==============================================================================================

    /**
     * Gets or creates a LayerClient, using a default set of LayerClient.Options and flavor-specific
     * MyApplication ID and Options from the `generateLayerClient` method.  Returns `null` if the flavor was
     * unable to create a LayerClient (due to no MyApplication ID, etc.).
     *
     * @return New or existing LayerClient, or `null` if a LayerClient could not be constructed.
     * @see Flavor#generateLayerClient(Context, LayerClient.Options)
     */
    public static LayerClient getLayerClient() {
        if (sLayerClient == null) {
            // Custom options for constructing a LayerClient
            LayerClient.Options options = new LayerClient.Options()

                    /* Fetch the minimum amount per conversation when first authenticated */
                    .historicSyncPolicy(LayerClient.Options.HistoricSyncPolicy.FROM_LAST_MESSAGE)
                    
                    /* Automatically download text and ThreePartImage info/preview */
                    .autoDownloadMimeTypes(Arrays.asList(
                            TextCellFactory.MIME_TYPE,
                            ThreePartImageUtils.MIME_TYPE_INFO,
                            ThreePartImageUtils.MIME_TYPE_PREVIEW))
                    .broadcastPushInForeground(true);

            // Allow flavor to specify Layer MyApplication ID and customize Options.
            sLayerClient = sFlavor.generateLayerClient(sInstance, options);

            // ProviderrailsFlavor was unable to generate Layer Client (no MyApplication ID, etc.)
            if (sLayerClient == null) return null;

            /* Register AuthenticationProvider for handling authentication challenges */
            sLayerClient.registerAuthenticationListener(getAuthenticationProvider());
        }
        return sLayerClient;
    }

    public static void changeLayerInAppNotification(boolean b) {
        // Custom options for constructing a LayerClient
        LayerClient.Options options = new LayerClient.Options();
        options.broadcastPushInForeground(b);
    }


    public static String getLayerAppId() {
        return sFlavor.getLayerAppId();
    }

    public static ParticipantProvider getParticipantProvider() {
        if (sParticipantProvider == null) {
            sParticipantProvider = sFlavor.generateParticipantProvider(sInstance, getAuthenticationProvider());
        }
        return sParticipantProvider;
    }

    public static AuthenticationProvider getAuthenticationProvider() {
        if (sAuthProvider == null) {
            sAuthProvider = sFlavor.generateAuthenticationProvider(sInstance);

            // If we have cached credentials, try authenticating with Layer
            LayerClient layerClient = getLayerClient();
            if (layerClient != null && sAuthProvider.hasCredentials()) layerClient.authenticate();
        }
        return sAuthProvider;
    }

    public static Picasso getPicasso() {
        if (sPicasso == null) {
            // Picasso with custom RequestHandler for loading from Layer MessageParts.
            sPicasso = new Picasso.Builder(sInstance)
                    .addRequestHandler(new MessagePartRequestHandler(getLayerClient()))
                    .build();
        }
        return sPicasso;
    }

    /**
     * ProviderrailsFlavor is used by Atlas Messenger to switch environments.
     *
     * @see com.bureau.bureauapp.flavor.Flavor
     */
    public interface Flavor {
        String getLayerAppId();

        LayerClient generateLayerClient(Context context, LayerClient.Options options);

        AuthenticationProvider generateAuthenticationProvider(Context context);

        ParticipantProvider generateParticipantProvider(Context context, AuthenticationProvider authenticationProvider);
    }
}
