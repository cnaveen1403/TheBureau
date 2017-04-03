package com.bureau.bureauapp.accounts;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.myapplication.AppData;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Result;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HelpFeedbackActivity extends Activity {

    EditText sendFeedBackMessageET;
    String sendFeedBackMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_help_feedback);

        sendFeedBackMessageET = (EditText) findViewById(R.id.sendFeedBackMessageET);

        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        findViewById(R.id.submitFeedbackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (sendFeedBackMessageET.getText().toString().trim().length() > 0) {
                    sendFeedBackMessage = sendFeedBackMessageET.getText().toString().trim();
                    if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                        new SendFeedBackAsync().execute();
                    } else {
                        Toast.makeText(HelpFeedbackActivity.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(HelpFeedbackActivity.this, "Please enter message", Toast.LENGTH_SHORT).show();
                }

            }
        });
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

    private class SendFeedBackAsync extends AsyncTask<Void, Void, Result> {

        Dialog progressDialog;
        String resultStr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new Dialog(HelpFeedbackActivity.this, R.style.progress_dialog);
            progressDialog.setContentView(R.layout.dialog);
            progressDialog.setCancelable(true);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Result doInBackground(Void... params) {

            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(HelpFeedbackActivity.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.FEEB_BACK, sendFeedBackMessage));
            resultStr = new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.SEND_FEED_BACK_API, parms);

            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(resultStr);
                if (jsonObject.getString("msg").equalsIgnoreCase("Success")) {
                    final Dialog dialog = new Dialog(HelpFeedbackActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.simple_alert);
                    ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Thank you!");
                    ((TextView) dialog.findViewById(R.id.dialogMessage)).setText("We received your message and will get back to you shortly");
                    // set the custom dialog components - text, image and button
                    TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                    text.setText("Close");

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    });
                    dialog.show();
                } else {
                    Toast.makeText(HelpFeedbackActivity.this, jsonObject.getString("'response'"), Toast.LENGTH_SHORT).show();
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(HelpFeedbackActivity.this, HelpFeedbackActivity.this);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
