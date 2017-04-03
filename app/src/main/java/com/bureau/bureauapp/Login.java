package com.bureau.bureauapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.profilesetup.AccountCreation;
import com.bureau.bureauapp.profilesetup.LegalStatus;
import com.bureau.bureauapp.profilesetup.Occupation;
import com.bureau.bureauapp.profilesetup.ProfileHeritage;
import com.bureau.bureauapp.profilesetup.ProfileInfo;
import com.bureau.bureauapp.profilesetup.SocialHabit;
import com.bureau.bureauapp.urbanairship.SendUAirshipChannel;
import com.bureau.bureauapp.util.Util;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.urbanairship.UAirship;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Login extends AppCompatActivity {

    private final String LOG_TAG = Login.class.getSimpleName();

    private String login_type, login_key, login_value;
    Context mContext;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mContext = Login.this;

        //Facebook Signup process
        mCallbackManager = CallbackManager.Factory.create();

        Button fbLoginBtn = (Button) findViewById(R.id._btn_login_fb);
        fbLoginBtn.setTextSize(14);

        fbLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbLoginButtonClicked();
            }
        });

        //Digits Login process
        TwitterAuthConfig authConfig = new TwitterAuthConfig(BureauConstants.TWITTER_KEY,
                BureauConstants.TWITTER_SECRET);
        Digits.Builder digitsBuilder = new Digits.Builder().withTheme(R.style.CustomDigitsTheme);
        Fabric.with(this, new TwitterCore(authConfig), digitsBuilder.build());
        digitsButtonClicked();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void digitsButtonClicked() {
        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.btn_login_mobile);
        digitsButton.setText(R.string.login_digits_label);
        digitsButton.setTextSize(14);
        digitsButton.setBackgroundResource(R.drawable.btn_login_mobile);
        digitsButton.setAuthTheme(R.style.CustomDigitsTheme);
//        Digits.getSessionManager().clearActiveSession();

        digitsButton.setCallback(new AuthCallback() {

            @Override
            public void success(DigitsSession session, String phoneNumber) {
                login_type = "digits";
                login_key = "digits";
                login_value = phoneNumber;
                login(login_type, login_key, login_value);
            }

            @Override
            public void failure(DigitsException exception) {
                Log.d("Digits", "Sign in with Digits failure", exception);
            }
        });
    }

    public void fbLoginButtonClicked() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        String fbToken = loginResult.getAccessToken().getUserId();

                        if (ConnectBureau.isNetworkAvailable(mContext)) {
                            login_type = "fb";
                            login_key = "fb_id";
                            login_value = fbToken;
                            login(login_type, login_key, login_value);
                        } else {
                            Toast.makeText(Login.this, "No internet connection available.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancel() {
                        // MyApplication code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        exception.printStackTrace();
                    }
                });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void login(String login_type, String login_key, String login_value) {
        if (ConnectBureau.isNetworkAvailable(mContext)) {
            new LoginTask().execute(login_type, login_key, login_value);
        } else {
            Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        String phoneNum;
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new Dialog(Login.this, R.style.progress_dialog);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            phoneNum = params[2];
            parms.add(new BasicNameValuePair(BureauConstants.loginType, params[0]));
            parms.add(new BasicNameValuePair(params[1], phoneNum));

            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.LOGIN_URL, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equals("Error")) {
                    String response = jsonObject.getString("response");

                    if (response.equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        Util.invalidateUserID(Login.this, Login.this);
                    } else {
                        final Dialog dialog = new Dialog(Login.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.simple_alert);
                        ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText(msg);
                        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(response);
                        // set the custom dialog components - text, image and button
                        TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                } else {
                    String profilestatus = jsonObject.getString(BureauConstants.profileStatus);
                    String userId = jsonObject.getString(BureauConstants.userid);
                    AppData.saveString(Login.this, BureauConstants.userid, userId);
                    AppData.saveString(Login.this, BureauConstants.phoneNumber, phoneNum);
                    AppData.saveString(Login.this, BureauConstants.loginType, login_type);
                    AppData.saveString(Login.this, BureauConstants.loginValue, login_value);
                    AppData.saveString(Login.this, BureauConstants.profileStatus, profilestatus);
                    AppData.saveString(Login.this, BureauConstants.referralCode, jsonObject.getString(BureauConstants.referralCode));
                    AppData.saveString(Login.this, BureauConstants.referralCodeApplied, jsonObject.getString(BureauConstants.referralCodeApplied));

                    afterSuccessfulLogin();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private void afterSuccessfulLogin() {
//             clear Digits Session
//        Digits.clearActiveSession();

        /* get Urbanairship channel Id from Urbanairship */
        String channelId = UAirship.shared().getPushManager().getChannelId();
//            Logger.info("My Application Channel ID: " + channelId);

        // Associate the channel to a Named User ID.
        UAirship.shared().getNamedUser().setId(channelId);

        new SendUAirshipChannel(this, channelId);
        readProfile();
    }

    private void readProfile() {
        if (ConnectBureau.isNetworkAvailable(mContext)) {
            new ReadUserProfileDetails().execute();
        } else {
            Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }
    }

    private class ReadUserProfileDetails extends AsyncTask<Void, Void, String> {
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new Dialog(Login.this, R.style.progress_dialog);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(Login.this, BureauConstants.userid)));

            String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = BureauConstants.BASE_URL + BureauConstants.READ_PROFILE_DETAILS_URL;
            url += "?";
            url += paramString;
            return new ConnectBureau().getDataFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String profileStatus = AppData.getString(Login.this, BureauConstants.profileStatus);

                if (jsonObject.has("msg") && !profileStatus.equals("create_account_ws")) {
                    String response = jsonObject.getString("response");
                    String msg = jsonObject.getString("msg");
                    //redirect user to login page
                    if (response.equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        Util.invalidateUserID(Login.this, Login.this);
                    } else {
                        final Dialog dialog = new Dialog(Login.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.simple_alert);
                        ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText(msg);
                        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(response);
// set the custom dialog components - text, image and button
                        TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                } else {
                    saveUserProfileDetails(jsonObject);
                    dialog.dismiss();

                    if (profileStatus.equals("completed")) {
                        finishAffinity();
                        Intent intent = new Intent(Login.this, HomeActivity.class);
                        intent.putExtra("pager_position", 0);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    } else {
                        final Dialog dialog = new Dialog(Login.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.simple_alert);
                        ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Info");
                        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText("please complete the Registration process to continue with this app !!!");
// set the custom dialog components - text, image and button
                        TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                        text.setText("Ok");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                String profileStatus = AppData.getString(Login.this, BureauConstants.profileStatus);

                                if (profileStatus.equals("create_account_ws")) {
                                    Intent intent = new Intent(Login.this, AccountCreation.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                } else if (profileStatus.equals("update_profile_step2")) {
                                    Intent intent = new Intent(Login.this, ProfileInfo.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                } else if (profileStatus.equals("update_profile_step3")) {
                                    Intent intent = new Intent(Login.this, ProfileHeritage.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                } else if (profileStatus.equals("update_profile_step4")) {
                                    Intent intent = new Intent(Login.this, SocialHabit.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                } else if (profileStatus.equals("update_profile_step5")) {
                                    Intent intent = new Intent(Login.this, Occupation.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                } else if (profileStatus.equals("update_profile_step6")) {
                                    Intent intent = new Intent(Login.this, LegalStatus.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            }
                        });
                        dialog.show();
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    public void saveUserProfileDetails(JSONObject jsonObject) {
        for (Iterator<String> iter = jsonObject.keys(); iter.hasNext(); ) {
            String key = iter.next();
            try {
                //Save each key value pair received from the Server
//                Log.d(LOG_TAG, "key >> " + key + " >> value >> " + jsonObject.getString(key));
                AppData.saveString(Login.this, key, jsonObject.getString(key));
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}