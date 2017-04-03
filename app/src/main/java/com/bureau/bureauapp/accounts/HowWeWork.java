package com.bureau.bureauapp.accounts;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
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

public class HowWeWork extends Activity {
    private final String LOG_TAG = HowWeWork.class.getSimpleName();
    String aboutTheBureau, howTheAppWorks, whyItWasCreated, howItIsDifferent,
            historyOfTheApplication = "Check your internet connection and try again.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_we_work);

        if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
            new GetHowWeWorkAsync().execute();
        } else {
            Toast.makeText(HowWeWork.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }

        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        findViewById(R.id.aboutTheBureauRL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HowWeWork.this, HowWeWorkDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("title", "About The Bureau");
                intent.putExtra("message", aboutTheBureau);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        findViewById(R.id.howTheAppWorksRL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HowWeWork.this, HowWeWorkDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("title", "How the app works");
                intent.putExtra("message", howTheAppWorks);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        findViewById(R.id.whyTheBureauRL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HowWeWork.this, HowWeWorkDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("title", "Why  it was created");
                intent.putExtra("message", whyItWasCreated);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        findViewById(R.id.howItIsDifferentRL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HowWeWork.this, HowWeWorkDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("title", "How it is different");
                intent.putExtra("message", howItIsDifferent);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        findViewById(R.id.theBureauHistoryRL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HowWeWork.this, HowWeWorkDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("title", "History of the application");
                intent.putExtra("message", historyOfTheApplication);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

    private class GetHowWeWorkAsync extends AsyncTask<Void, Void, Result> {

        Dialog dialog;
        String resultStr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(HowWeWork.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(HowWeWork.this, BureauConstants.userid)));

            String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = BureauConstants.BASE_URL + BureauConstants.HOW_WE_WORK;
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
                JSONObject jsonObject = new JSONObject(resultStr);
                if (jsonObject.has("msg")) {
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(HowWeWork.this, HowWeWork.this);
                    }
                } else {
                    aboutTheBureau = jsonObject.getString(BureauConstants.ABOUT_THE_BUREAU);
                    howTheAppWorks = jsonObject.getString(BureauConstants.HOW_THE_APP_WORKS);
                    whyItWasCreated = jsonObject.getString(BureauConstants.WHY_IT_WAS_CREATED);
                    howItIsDifferent = jsonObject.getString(BureauConstants.HOW_IS_IT_DIFFERENT);
                    historyOfTheApplication = jsonObject.getString(BureauConstants.HISTORY_OF_THE_APPLICATION);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}