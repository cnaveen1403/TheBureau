package com.bureau.bureauapp.accounts;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.SplashScreen;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.myapplication.MyApplication;
import com.digits.sdk.android.Digits;
import com.layer.atlas.util.Util;
import com.layer.sdk.LayerClient;
import com.urbanairship.UAirship;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Result;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ConfigurationActivity extends Activity implements CompoundButton.OnClickListener {

    private final String LOG_TAG = ConfigurationActivity.class.getSimpleName();
    CheckBox deactivateAccountCB, deleteAccountCB, customerServicesCB, blogReleaseCB, dailyMatchCB, chatNotificationsCB, soundsCB;
    String membership, reason;
    Button logoutBtn;
    TextView mProfileStatus, mTermsAndConditions, mPrivacyPolicy;
    boolean customerServices, blogRelease, dailyMatch, chatNotifications, sounds;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_configuration);

        init();
    }

    private void init() {
        deactivateAccountCB = (CheckBox) findViewById(R.id.deactivateAccountCB);
        deleteAccountCB = (CheckBox) findViewById(R.id.deleteAccountCB);
        customerServicesCB = (CheckBox) findViewById(R.id.customerServicesCB);
        blogReleaseCB = (CheckBox) findViewById(R.id.blogReleaseCB);
        dailyMatchCB = (CheckBox) findViewById(R.id.dailyMatchCB);
        chatNotificationsCB = (CheckBox) findViewById(R.id.chatNotificationsCB);
        soundsCB = (CheckBox) findViewById(R.id.soundsCB);
        logoutBtn = (Button) findViewById(R.id.btn_Logout);
        mProfileStatus = (TextView) findViewById(R.id.config_profile_status);
        mTermsAndConditions = (TextView) findViewById(R.id.termsAndConditionsTV);
        mPrivacyPolicy = (TextView) findViewById(R.id.privacyPolicyTV);

        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
            new GetConfigurationsAsync().execute();
        } else {
            Toast.makeText(ConfigurationActivity.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }

        findViewById(R.id.rateUsOnTheAppStoreTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateUsOnAppStoreClicked();
            }
        });

        deactivateAccountCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deactivateAccountCB.isChecked()) {
                    displayDialog(ConfigurationActivity.this, "Deactivate account", "Are you sure you want to deactive your account ? Otherwise you will not be able to view matches until account is reactived.", "Deactive", "deactivateAccount");
                } else {
                    if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                        new ActivateAccAsync().execute();
                    } else {
                        Toast.makeText(ConfigurationActivity.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        deleteAccountCB.setOnClickListener(this);
        customerServicesCB.setOnClickListener(this);
        blogReleaseCB.setOnClickListener(this);
        dailyMatchCB.setOnClickListener(this);
        chatNotificationsCB.setOnClickListener(this);
        soundsCB.setOnClickListener(this);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutBtnClicked();
            }
        });

        mPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog openDialog = new Dialog(ConfigurationActivity.this);
                openDialog.setContentView(R.layout.customdialog_layout);

                ((TextView) openDialog.findViewById(R.id.tv_custom_dialog_title)).setText("Privacy Policy");
                TextView dialogTextContent = (TextView) openDialog.findViewById(R.id.dialog_textview);
                dialogTextContent.setText(getTermsAndConditions("privacypolicy.txt"));
                ImageView dialogCloseButton = (ImageView) openDialog.findViewById(R.id.dialog_close_button);
                dialogCloseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        openDialog.dismiss();
                    }
                });
                openDialog.show();
            }
        });

        mTermsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog openDialog = new Dialog(ConfigurationActivity.this);
                openDialog.setContentView(R.layout.customdialog_layout);
                ((TextView) openDialog.findViewById(R.id.tv_custom_dialog_title)).setText("Terms And Conditions");
                TextView dialogTextContent = (TextView) openDialog.findViewById(R.id.dialog_textview);
                dialogTextContent.setText(getTermsAndConditions("termsandconditions.txt"));
                ImageView dialogCloseButton = (ImageView) openDialog.findViewById(R.id.dialog_close_button);
                dialogCloseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        openDialog.dismiss();
                    }
                });
                openDialog.show();
            }
        });
    }

    public String getTermsAndConditions(String filename) {
        // Open and read the contents of <filename> into
        // a single string then return it

        InputStream is = null;
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = context.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void rateUsOnAppStoreClicked() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private void doDeleteDeactivateTask() {
        if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
            new DeleteDeactivateAsync().execute();
        } else {
            Toast.makeText(ConfigurationActivity.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }
    }

    public void displayDialog(final Context context, String title, String message, String positiveName, final String action) {
        // custom dialog
        final Dialog dialogConfirmation = new Dialog(context);
        dialogConfirmation.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogConfirmation.setContentView(R.layout.alert_dialog);
        ((TextView) dialogConfirmation.findViewById(R.id.dialogTitleTV)).setText(title);
        ((TextView) dialogConfirmation.findViewById(R.id.dialogMessage)).setText(message);
        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialogConfirmation.findViewById(R.id.OKTV);
        text.setText(positiveName);

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.reason_dialog);
                final EditText reasonET = (EditText) dialog.findViewById(R.id.reasonEt);
                if (action.equalsIgnoreCase("deleteAccount")) {
                    ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Reason for delete account");
                } else {
                    ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Reason for deactivate account");
                }

                dialog.findViewById(R.id.OKTV).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (reasonET.getText().toString().length() > 0) {
                            reason = reasonET.getText().toString();
                            if (action.equalsIgnoreCase("deleteAccount")) {
                                deleteAccountCB.setChecked(true);
                                membership = BureauConstants.DELETE_ACCOUNT;
                                doDeleteDeactivateTask();
                            } else if (action.equalsIgnoreCase("deactivateAccount")) {
                                deactivateAccountCB.setChecked(true);
                                membership = BureauConstants.DEACTIVATE_ACCOUNT;
                                doDeleteDeactivateTask();
                            }
                            dialog.dismiss();
                        } else {
                            reasonET.setError("Please enter reason.");
//                            Toast.makeText(context, "Please enter reason", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.findViewById(R.id.cancelTV).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (action.equalsIgnoreCase("deleteAccount")) {
                            deleteAccountCB.setChecked(false);
                        } else if (action.equalsIgnoreCase("deactivateAccount")) {
                            deactivateAccountCB.setChecked(false);
                        }
                        dialog.dismiss();
                    }
                });

                dialog.show();

                dialogConfirmation.dismiss();
            }
        });
        dialogConfirmation.findViewById(R.id.cancelTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (action.equalsIgnoreCase("deleteAccount")) {
                    deleteAccountCB.setChecked(false);
                } else if (action.equalsIgnoreCase("deactivateAccount")) {
                    deactivateAccountCB.setChecked(false);
                }
                dialogConfirmation.dismiss();
            }
        });

        dialogConfirmation.show();
    }

    //Logout button Clicked
    public void logoutBtnClicked() {
        if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {

            final Dialog dialog = new Dialog(ConfigurationActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.yes_no_alert);
            ((TextView) dialog.findViewById(R.id.tv_title_yes_no)).setText("Logout");
            ((TextView) dialog.findViewById(R.id.tv_message_yes_no)).setText("Are you sure you want to logout ?");
// set the custom dialog components - text, image and button
            TextView text = (TextView) dialog.findViewById(R.id.tv_no_btn);
            text.setText("NO");

            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            TextView textView = (TextView) dialog.findViewById(R.id.tv_canael_btn);
            textView.setText("YES");

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    /*
                * Call the Logout Task
                * */
                    new LogoutTask().execute();
                }
            });
            dialog.show();
        } else {
            Toast.makeText(ConfigurationActivity.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.deleteAccountCB:
                if (deleteAccountCB.isChecked())
                    displayDialog(ConfigurationActivity.this, "Delete Account", "Are you sure you want to delete account ? All records will be lost.", "Delete", "deleteAccount");
                break;
            case R.id.customerServicesCB:
                customerServices = customerServicesCB.isChecked();
                blogRelease = blogReleaseCB.isChecked();
                dailyMatch = dailyMatchCB.isChecked();
                chatNotifications = chatNotificationsCB.isChecked();
                sounds = soundsCB.isChecked();
                updateConfiguration();
                break;
            case R.id.blogReleaseCB:
                customerServices = customerServicesCB.isChecked();
                blogRelease = blogReleaseCB.isChecked();
                dailyMatch = dailyMatchCB.isChecked();
                chatNotifications = chatNotificationsCB.isChecked();
                sounds = soundsCB.isChecked();
                updateConfiguration();
                break;
            case R.id.dailyMatchCB:
                customerServices = customerServicesCB.isChecked();
                blogRelease = blogReleaseCB.isChecked();
                dailyMatch = dailyMatchCB.isChecked();
                chatNotifications = chatNotificationsCB.isChecked();
                sounds = soundsCB.isChecked();
                updateConfiguration();
                break;
            case R.id.chatNotificationsCB:
                customerServices = customerServicesCB.isChecked();
                blogRelease = blogReleaseCB.isChecked();
                dailyMatch = dailyMatchCB.isChecked();
                chatNotifications = chatNotificationsCB.isChecked();
                sounds = soundsCB.isChecked();
                updateConfiguration();
                break;
            case R.id.soundsCB:
                customerServices = customerServicesCB.isChecked();
                blogRelease = blogReleaseCB.isChecked();
                dailyMatch = dailyMatchCB.isChecked();
                chatNotifications = chatNotificationsCB.isChecked();
                sounds = soundsCB.isChecked();
                updateConfiguration();
                break;
        }
    }

    private void updateConfiguration() {
        if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
            new AppNotificationAsync().execute();
        } else {
            Toast.makeText(ConfigurationActivity.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }
    }

    private class ActivateAccAsync extends AsyncTask<Void, Void, Result> {
        Dialog dialog;
        String resultStr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(ConfigurationActivity.this, R.style.progress_dialog);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Result doInBackground(Void... params) {
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(ConfigurationActivity.this, BureauConstants.userid)));
            resultStr = new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.ACTIVATE_ACCOUNT, parms);

            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            dialog.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(resultStr);
                if (jsonObject.getString("msg").equalsIgnoreCase("Success")) {
                    final Dialog dialog = new Dialog(ConfigurationActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.simple_alert);
                    ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Success");
                    ((TextView) dialog.findViewById(R.id.dialogMessage)).setText("Your account has been activated.");
// set the custom dialog components - text, image and button
                    TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                    text.setText("Ok");

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            deactivateAccountCB.setChecked(false);
                            mProfileStatus.setText("ACTIVE");
                            finish();
                        }
                    });
                    dialog.show();
                } else {
                    deactivateAccountCB.setChecked(true);
                    mProfileStatus.setText("IN ACTIVE");
                    Toast.makeText(ConfigurationActivity.this, "Sorry we couldn't process your request.", Toast.LENGTH_SHORT).show();
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(ConfigurationActivity.this, ConfigurationActivity.this);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class LogoutTask extends AsyncTask<Void, Void, Result> {
        Dialog dialog;
        String resultStr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(ConfigurationActivity.this, R.style.progress_dialog);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Result doInBackground(Void... params) {
            /*
            * TO DO: Add Device Id to the Second Param
            * */
            /* get Urbanairship channel Id from Urbanairship */
            String channelId = UAirship.shared().getPushManager().getChannelId();

            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(ConfigurationActivity.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.DEVICEID, channelId));
            resultStr = new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.LOGOUT, parms);

            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            dialog.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(resultStr);
                if (jsonObject.getString("msg").equalsIgnoreCase("Success")) {
//                    clear Digits Session
                    Digits.clearActiveSession();
                    deauthenticateLayer();

//                    clear All the User Data
                    AppData.clearPreferences(ConfigurationActivity.this);
//                    ChatData.clearPreferences(ConfigurationActivity.this);

                    finishAffinity();
                    Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("isLoggedOut", "true");
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                } else {
                    Toast.makeText(ConfigurationActivity.this, "Sorry we couldn't logout.", Toast.LENGTH_SHORT).show();
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(ConfigurationActivity.this, ConfigurationActivity.this);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void deauthenticateLayer() {
        MyApplication.deauthenticate(new Util.DeauthenticationCallback() {
            @Override
            public void onDeauthenticationSuccess(LayerClient client) {
//                MyApplication.routeLogin(ConfigurationActivity.this);
                Log.e("ConfigurationActivity", "deauthenticateLayer success >>> ");
            }

            @Override
            public void onDeauthenticationFailed(LayerClient client, String reason) {
                Log.e("ConfigurationActivity", "reason >>> " + reason);
            }
        });
    }

    private class DeleteDeactivateAsync extends AsyncTask<Void, Void, Result> {

        Dialog dialog;
        String resultStr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(ConfigurationActivity.this, R.style.progress_dialog);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Result doInBackground(Void... params) {

            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(ConfigurationActivity.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.REASON, reason));
            resultStr = new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + membership, parms);

            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            dialog.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(resultStr);
                if (jsonObject.getString("msg").equalsIgnoreCase("Success")) {
                    //clear Digits Session
                    Digits.clearActiveSession();
                    deauthenticateLayer();

//                    clear All the User Data
                    AppData.clearPreferences(ConfigurationActivity.this);
//                    ChatData.clearPreferences(ConfigurationActivity.this);

                    finishAffinity();
                    Intent intent = new Intent(ConfigurationActivity.this, SplashScreen.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                } else {
                    Toast.makeText(ConfigurationActivity.this, jsonObject.getString("'response'"), Toast.LENGTH_SHORT).show();
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(ConfigurationActivity.this, ConfigurationActivity.this);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class AppNotificationAsync extends AsyncTask<Void, Void, Result> {

        Dialog dialog;
        String resultStr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(ConfigurationActivity.this, R.style.progress_dialog);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Result doInBackground(Void... params) {

            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(ConfigurationActivity.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.DAILY_MATCH, "" + dailyMatch));
            parms.add(new BasicNameValuePair(BureauConstants.CHAT_NOTIFICATION, "" + chatNotifications));
            parms.add(new BasicNameValuePair(BureauConstants.CUSTOMER_SERVICE, "" + customerServices));
            parms.add(new BasicNameValuePair(BureauConstants.BLOG_RELEASE, "" + blogRelease));
            parms.add(new BasicNameValuePair(BureauConstants.SOUND, "" + sounds));

            resultStr = new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.UPDATE_NOTIFICATIONS, parms);

//            Log.e("Bureau", "AppNotificationAsync " + resultStr);
            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            dialog.dismiss();
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(resultStr);
                if (jsonObject.has("msg")) {
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(ConfigurationActivity.this, ConfigurationActivity.this);
                    } else if (jsonObject.getString("msg").equalsIgnoreCase("Success")) {
                        //successfully update the configuration
                        MyApplication.mUAirship.getPushManager().setSoundEnabled(sounds);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetConfigurationsAsync extends AsyncTask<Void, Void, Result> {

        Dialog dialog;
        String resultStr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(ConfigurationActivity.this, R.style.progress_dialog);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected Result doInBackground(Void... params) {

            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(ConfigurationActivity.this, BureauConstants.userid)));

            String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = BureauConstants.BASE_URL + BureauConstants.NOTIFICATIONS_STATUS;
            url += "?";
            url += paramString;
            resultStr = new ConnectBureau().getDataFromUrl(url);
            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            dialog.dismiss();

            try {
//                Log.d("TheBureau", "GetConfigurationsAsync : " + resultStr);
                JSONObject jsonObjectMain = new JSONObject(resultStr);

                if (jsonObjectMain.has("msg")) {
//                    Log.e("HomeActivity", "Fetching friends count " + jsonObjectMain.getString("response"));
                    if (jsonObjectMain.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(ConfigurationActivity.this, ConfigurationActivity.this);
                    }
                } else {
                    JSONArray jsonArray = jsonObjectMain.getJSONArray("notification");  // new JSONArray(resultStr);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        if (jsonObject.getString("customer_service").equalsIgnoreCase("Disable")) {
                            findViewById(R.id.customerServicesRL).setVisibility(View.GONE);
                        }

                        if (jsonObject.getString("blog_release").equalsIgnoreCase("Disable")) {
                            findViewById(R.id.blogReleaseRL).setVisibility(View.GONE);
                        }
                        if (jsonObject.getString("daily_match").equalsIgnoreCase("Disable")) {
                            findViewById(R.id.dailymatchRL).setVisibility(View.GONE);
                        }

                        if (jsonObject.getString("sound").equalsIgnoreCase("Disable")) {
                            findViewById(R.id.soundsRL).setVisibility(View.GONE);
                        }

                        if (jsonObject.getString("chat_notification").equalsIgnoreCase("Disable")) {
                            findViewById(R.id.chatNotificationRL).setVisibility(View.GONE);
                        }
                    }
                    JSONArray jsonArrayConfiguration = jsonObjectMain.getJSONArray("configuration");

                    for (int i = 0; i < jsonArrayConfiguration.length(); i++) {
                        JSONObject jsonObject = jsonArrayConfiguration.getJSONObject(i);

                        if (jsonObject.getString("customer_service").equalsIgnoreCase("false")) {
                            customerServicesCB.setChecked(false);
                        } else {
                            customerServicesCB.setChecked(true);
                        }

                        if (jsonObject.getString("blog_release").equalsIgnoreCase("false")) {
                            blogReleaseCB.setChecked(false);
                        } else {
                            blogReleaseCB.setChecked(true);
                        }
                        if (jsonObject.getString("daily_match").equalsIgnoreCase("false")) {
                            dailyMatchCB.setChecked(false);
                        } else {
                            dailyMatchCB.setChecked(true);
                        }
                        if (jsonObject.getString("chat_notification").equalsIgnoreCase("false")) {
                            chatNotificationsCB.setChecked(false);
                        } else {
                            chatNotificationsCB.setChecked(true);
                        }

                        if (jsonObject.getString("sound").equalsIgnoreCase("false")) {
                            soundsCB.setChecked(false);
                        } else {
                            soundsCB.setChecked(true);
                        }
                    }

                    JSONObject jsonObjProfileStatus = jsonObjectMain.getJSONObject("profile_status");

                    if (jsonObjProfileStatus.getString("status").equalsIgnoreCase("active")) {
                        deactivateAccountCB.setChecked(false);
                        mProfileStatus.setText("ACTIVE");
                    } else {
                        deactivateAccountCB.setChecked(true);
                        mProfileStatus.setText("IN ACTIVE");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
