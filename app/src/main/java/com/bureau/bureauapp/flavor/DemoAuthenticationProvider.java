package com.bureau.bureauapp.flavor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;

import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.layerclasses.ResumeActivity;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.profilesetup.AccountCreation;
import com.bureau.bureauapp.util.AuthenticationProvider;
import com.bureau.bureauapp.util.Log;
import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static com.bureau.bureauapp.util.Util.streamToString;

public class DemoAuthenticationProvider implements AuthenticationProvider<DemoAuthenticationProvider.Credentials> {
    private final SharedPreferences mPreferences;
    private Callback mCallback;
    private LayerClient mLayerClient;

    public DemoAuthenticationProvider(Context context) {
        mPreferences = context.getSharedPreferences(DemoAuthenticationProvider.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    @Override
    public AuthenticationProvider<Credentials> setCredentials(Credentials credentials) {
        if (credentials == null) {
            mPreferences.edit().clear().commit();
            return this;
        }
        mPreferences.edit()
                .putString("appId", credentials.getLayerAppId())
                .putString("name", credentials.getUserName())
                .commit();
        return this;
    }

    @Override
    public boolean hasCredentials() {
        return mPreferences.contains("appId");
    }

    @Override
    public AuthenticationProvider<Credentials> setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    @Override
    public void onAuthenticated(LayerClient layerClient, String userId) {
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Authenticated with Layer, user ID: " + userId);
        layerClient.connect();
        if (mCallback != null) {
            mCallback.onSuccess(this, userId);
        }
    }

    @Override
    public void onDeauthenticated(LayerClient layerClient) {
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Deauthenticated with Layer");
    }

    @Override
    public void onAuthenticationChallenge(LayerClient layerClient, String nonce) {
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Received challenge: " + nonce);
        respondToChallenge(layerClient, nonce);
    }

    @Override
    public void onAuthenticationError(LayerClient layerClient, LayerException e) {
        String error = "Failed to authenticate with Layer: " + e.getMessage();
        if (Log.isLoggable(Log.ERROR)) Log.e(error, e);
        if (mCallback != null) {
            mCallback.onError(this, error);
        }
    }

    @Override
    public boolean routeLogin(LayerClient layerClient, String layerAppId, Activity from) {
        if (layerAppId == null) {
            // No MyApplication ID: must scan from QR code.
            if (Log.isLoggable(Log.VERBOSE)) Log.v("Routing to QR Code scanning Activity");
            Intent intent = new Intent(from, DemoAtlasIdScannerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            from.startActivity(intent);
            return true;
        }

        if ((layerClient != null) && layerClient.isAuthenticated()) {
            // The LayerClient is authenticated: no action required.
            if (Log.isLoggable(Log.VERBOSE)) Log.v("No authentication routing required");
            return false;
        }

        if ((layerClient != null) && hasCredentials()) {
            // With a LayerClient and cached provider credentials, we can resume.
            if (Log.isLoggable(Log.VERBOSE)) {
                Log.v("Routing to resume Activity using cached credentials");
            }
            Intent intent = new Intent(from, ResumeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(ResumeActivity.EXTRA_LOGGED_IN_ACTIVITY_CLASS_NAME, from.getClass().getName());
            intent.putExtra(ResumeActivity.EXTRA_LOGGED_OUT_ACTIVITY_CLASS_NAME, DemoLoginActivity.class.getName());
            from.startActivity(intent);
            return true;
        }

        // We have a Layer MyApplication ID but no cached provider credentials: routing to Login required.
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Routing to login Activity");
        Intent intent = new Intent(from, DemoLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        from.startActivity(intent);
        return true;
    }

    private void respondToChallenge(LayerClient layerClient, String nonce) {
        Credentials credentials = new Credentials(mPreferences.getString("appId", null), mPreferences.getString("name", null));
        if (credentials.getUserName() == null || credentials.getLayerAppId() == null) {
            if (Log.isLoggable(Log.WARN)) {
                Log.w("No stored credentials to respond to challenge with");
            }
            return;
        }
        mLayerClient = layerClient;
        android.util.Log.e("DemoAuthentication", "identityToken userid: " + credentials.getUserName());
        android.util.Log.e("DemoAuthentication", "identityToken nonce: " + nonce);
        new GetIdentityToken().execute(credentials.getUserName(), nonce);

      /*  try {
            // Post request
//            String url = "https://layer-identity-provider.herokuapp.com/apps/" + credentials.getLayerAppId() + "/atlas_identities";
            String url = "http://dev.thebureauapp.com/layer/public/authenticate.php";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setRequestProperty("Accept", "application/json");
//            connection.setRequestProperty("X_LAYER_APP_ID", credentials.getLayerAppId());

            // Credentials
            JSONObject rootObject = new JSONObject()
                    .put("userid", credentials.getUserName())
                    .put("nonce", nonce);

            android.util.Log.e("DemoAuthentication","identityToken userid: "+credentials.getUserName());
            android.util.Log.e("DemoAuthentication","identityToken nonce: "+nonce);


            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            OutputStream os = connection.getOutputStream();
            os.write(rootObject.toString().getBytes("UTF-8"));
            os.close();

            // Handle failure
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED) {
                String error = String.format("Got status %d when requesting authentication for '%s' with nonce '%s' from '%s'",
                        statusCode, credentials.getUserName(), nonce, url);
                if (Log.isLoggable(Log.ERROR)) Log.e(error);
                if (mCallback != null) mCallback.onError(this, error);
                return;
            }

            // Parse response
            InputStream in = new BufferedInputStream(connection.getInputStream());
            String result = streamToString(in);
            in.close();
            connection.disconnect();
            JSONObject json = new JSONObject(result);
            if (json.has("error")) {
                String error = json.getString("error");
                if (Log.isLoggable(Log.ERROR)) Log.e(error);
                if (mCallback != null) mCallback.onError(this, error);
                return;
            }

            // Answer authentication challenge.
            String identityToken = json.optString("identity_token", null);

            android.util.Log.e("DemoAuthentication","identityToken: "+identityToken);


            if (Log.isLoggable(Log.VERBOSE)) Log.v("Got identity token: " + identityToken);

        } catch (Exception e) {
            String error = "Error when authenticating with provider: " + e.getMessage();
            if (Log.isLoggable(Log.ERROR)) Log.e(error, e);
            if (mCallback != null) mCallback.onError(this, error);
        }*/


    }

    public static class Credentials {
        private final String mLayerAppId;
        private final String mUserName;

        public Credentials(Uri layerAppId, String userName) {
            this(layerAppId == null ? null : layerAppId.getLastPathSegment(), userName);
        }

        public Credentials(String layerAppId, String userName) {
            mLayerAppId = layerAppId == null ? null : (layerAppId.contains("/") ? layerAppId.substring(layerAppId.lastIndexOf("/") + 1) : layerAppId);
            mUserName = userName;
        }

        public String getUserName() {
            return mUserName;
        }

        public String getLayerAppId() {
            return mLayerAppId;
        }
    }

    public class GetIdentityToken extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair("userid", params[0]));
            parms.add(new BasicNameValuePair("nonce", params[1]));
            return new ConnectBureau().getDataFromUrl(BureauConstants.AUTHENTICATE_URL, parms);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                if (s != null) {
                    android.util.Log.e("DemoAuthentication", "identityToken Response: " + s);
                    JSONObject jsonObject = new JSONObject(s);
                    mLayerClient.answerAuthenticationChallenge(jsonObject.getString("identity_token"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}

