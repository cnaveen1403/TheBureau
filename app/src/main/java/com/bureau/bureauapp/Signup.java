package com.bureau.bureauapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.profilesetup.AccountCreation;
import com.bureau.bureauapp.util.Util;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Signup extends AppCompatActivity {

    private final String LOG_TAG = Signup.class.getSimpleName();

    private static String sPhoneNumber;
    private static String sFirstname = "";
    private static String sLastname = "";
    private static String sBirthday = "";
    private static String sGender = "";
    private static String sEmail = "";
    private String var_RegType = "digits";
    private String fbUserid;

    Button fbLoginBtn;
    public static CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fbLoginBtn = (Button) findViewById(R.id.button_signup_fb);
        fbLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbLoginButtonClicked();
            }
        });

        //Digits Signup process
        TwitterAuthConfig authConfig = new TwitterAuthConfig(BureauConstants.TWITTER_KEY,
                BureauConstants.TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build());
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

    public void digitsButtonClicked() {
        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.button_signup_digits);
        digitsButton.setText(R.string.signup_digits_label);
        digitsButton.setTextSize(14);
        digitsButton.setBackgroundResource(R.drawable.btn_login_mobile);
        digitsButton.setAuthTheme(R.style.CustomDigitsTheme);
//        Digits.getSessionManager().clearActiveSession();
        digitsButton.setCallback(new AuthCallback() {

            @Override
            public void success(DigitsSession session, String phoneNumber) {
                sPhoneNumber = phoneNumber;
                if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                    if (var_RegType.equals("fb"))
                        new RegisterUser().execute("fb", "fb_id", fbUserid, "digits", phoneNumber);
                    else
                        new RegisterUser().execute("digits", "digits", phoneNumber);
                } else {
                    Toast.makeText(Signup.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void failure(DigitsException exception) {
                Log.d("Digits", "Sign in with Digits failure", exception);
            }
        });
    }

    public void fbLoginButtonClicked() {
        List<String> permissionNeeds =
                Arrays.asList(
                        "public_profile", "user_birthday",
                        "user_education_history", "user_work_history",
                        "email");

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(this, permissionNeeds);
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                var_RegType = "fb";

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                // Application code
                                try {
                                    sFirstname = object.getString("first_name");
                                    sLastname = object.getString("last_name");
                                    sBirthday = object.getString("birthday");
                                    sGender = object.getString("gender");
                                    sEmail = object.getString("email");
                                } catch (JSONException jSONException) {
                                    jSONException.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,birthday,gender,education,work,email");
                request.setParameters(parameters);
                request.executeAsync();

                fbUserid = loginResult.getAccessToken().getUserId();
                if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                    DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.button_signup_digits);
                    digitsButton.performClick();
                    digitsButtonClicked();
                } else {
                    Toast.makeText(Signup.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private class RegisterUser extends AsyncTask<String, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(Signup.this, R.style.progress_dialog);
            progressDialog.setContentView(R.layout.dialog);
            progressDialog.setCancelable(true);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            // params comes from the execute() call: params[0] is the url.
            List<NameValuePair> parms = new LinkedList<NameValuePair>();

//            HashMap<String, String> map = new HashMap<>();
            parms.add(new BasicNameValuePair(BureauConstants.regType, params[0]));
            parms.add(new BasicNameValuePair(params[1], params[2]));
            if (var_RegType.equals("fb")) {
                parms.add(new BasicNameValuePair(params[3], params[4]));
            }
            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.REGISTER_URL, parms);//HttpUtils.doPost(map, BureauConstants.BASE_URL+BureauConstants.REGISTER_URL);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            Log.e("TheBureau", "signup result: " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equals("Error")) {
                    String response = jsonObject.getString("response");

                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        Util.invalidateUserID(Signup.this, Signup.this);
                    } else {
                        final Dialog customDialog = new Dialog(Signup.this);
                        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        customDialog.setContentView(R.layout.simple_alert);
                        ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText(msg);
                        ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText(response);
                        TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                                Intent intent = new Intent(Signup.this, LoginSignup.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            }
                        });
                        customDialog.show();
                    }
                } else {
                    AppData.saveString(Signup.this, BureauConstants.referralCode, jsonObject.getString(BureauConstants.referralCode));
                    AppData.saveString(Signup.this, BureauConstants.userid, jsonObject.getString(BureauConstants.userid));
                    AppData.saveString(Signup.this, BureauConstants.profileStatus, "create_account_ws");

                    if (var_RegType.equals("fb")) {
                        AppData.saveString(Signup.this, BureauConstants.firstName, sFirstname);
                        AppData.saveString(Signup.this, BureauConstants.lastName, sLastname);
                        AppData.saveString(Signup.this, BureauConstants.dob, sBirthday);
                        AppData.saveString(Signup.this, BureauConstants.gender, sGender);
                        AppData.saveString(Signup.this, BureauConstants.email, sEmail);
                        AppData.saveString(Signup.this, BureauConstants.regType, "fb");
                        AppData.saveString(Signup.this, BureauConstants.phoneNumber, sPhoneNumber);
                    } else {
                        AppData.saveString(Signup.this, BureauConstants.regType, "digits");
                        AppData.saveString(Signup.this, BureauConstants.phoneNumber, sPhoneNumber);
                    }

                    finishAffinity();
                    Intent intent = new Intent(Signup.this, AccountCreation.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}