package com.bureau.bureauapp.profilesetup;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bureau.bureauapp.HomeActivity;
import com.bureau.bureauapp.R;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.myapplication.AppData;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LegalStatus extends AppCompatActivity {
    private static String LOG_TAG = LegalStatus.class.getSimpleName();

    Context mContext;
    private static String sYearsInUS;
    private static String sLegalStatus;

    RadioButton radioButton;
    Button btn_SaveLegalStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_status);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        init();
    }

    private void init() {
        mContext = this;
        btn_SaveLegalStatus = (Button) findViewById(R.id.btn_SaveLegalStatus);

        TextView tv_title = (TextView) findViewById(R.id.toolbar_Legal_Status_title);
        String profileName = AppData.getString(mContext, "profile_first_name");
        String txtTitle = profileName + "'s Legal Status";
        tv_title.setText(txtTitle);

        setYearsInUS("0 - 2");
        setLegalStatus("Citizen/Green Card");

        btn_SaveLegalStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                    new SaveLegalStatus().execute();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
        });

        setProfLegalStatusFromContext();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.logo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(mContext, Occupation.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(mContext, Occupation.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
        return true;
    }

    public void setProfLegalStatusFromContext() {
        String yearsInUsa = AppData.getString(mContext, "years_in_usa");
        String legalStatus = AppData.getString(mContext, "legal_status");

        if (yearsInUsa != null) {
            sYearsInUS = yearsInUsa;
            setYearsInUI(yearsInUsa);
        }

        if (legalStatus != null) {
            sLegalStatus = legalStatus;
            setLegalStatusUI(legalStatus);
        }
    }

    public void setYearsInUI(String years) {
        if (years.equals("0 - 2")) {
            radioButton = (RadioButton) findViewById(R.id.radio_ZeroToTwoYrs);
            radioButton.setChecked(true);
        } else if (years.equals("2 - 6")) {
            radioButton = (RadioButton) findViewById(R.id.radio_TwoToSixYrs);
            radioButton.setChecked(true);
        } else if (years.equals("6+")) {
            radioButton = (RadioButton) findViewById(R.id.radio_SixPlusYrs);
            radioButton.setChecked(true);
        } else if (years.equals("Born & Raised")) {
            radioButton = (RadioButton) findViewById(R.id.radio_FromBirth);
            radioButton.setChecked(true);
        }
    }

    public void setLegalStatusUI(String status) {

        if (status.equals("Citizen/Green Card")) {
            radioButton = (RadioButton) findViewById(R.id.radio_USCitizen);
            radioButton.setChecked(true);
        } else if (status.equals("Greencard")) {
            radioButton = (RadioButton) findViewById(R.id.radio_Greencard);
            radioButton.setChecked(true);
        } else if (status.equals("Greencard Processing")) {
            radioButton = (RadioButton) findViewById(R.id.radio_GreencardProcessing);
            radioButton.setChecked(true);
        } else if (status.equals("H1 Visa")) {
            radioButton = (RadioButton) findViewById(R.id.radio_H1Visa);
            radioButton.setChecked(true);
        } else if (status.equals("Student Visa")) {
            radioButton = (RadioButton) findViewById(R.id.radio_StudentVisa);
            radioButton.setChecked(true);
        } else if (status.equals("Other")) {
            radioButton = (RadioButton) findViewById(R.id.radio_Others);
            radioButton.setChecked(true);
        }
    }

    public void setYearsInUS(String yearsInUS) {
        sYearsInUS = yearsInUS;
    }

    public void setLegalStatus(String legalStatus) {
        sLegalStatus = legalStatus;
    }

    public String getYearsInUS() {
        return sYearsInUS;
    }

    public String getLegalStatus() {
        return sLegalStatus;
    }

    public void onYearsInUSClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_ZeroToTwoYrs:
                if (checked)
                    setYearsInUS("0 - 2");
                break;
            case R.id.radio_TwoToSixYrs:
                if (checked)
                    setYearsInUS("2 - 6");
                break;
            case R.id.radio_SixPlusYrs:
                if (checked)
                    setYearsInUS("6+");
                break;
            case R.id.radio_FromBirth:
                if (checked)
                    setYearsInUS("Born & Raised");
                break;
        }
    }

    public void onLegalStatusClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();


        switch (view.getId()) {
            case R.id.radio_USCitizen:
                if (checked)
                    setLegalStatus("Citizen/Green Card");
                break;
            case R.id.radio_Greencard:
                if (checked)
                    setLegalStatus("Greencard");
                break;
            case R.id.radio_GreencardProcessing:
                if (checked)
                    setLegalStatus("Greencard Processing");

                break;
            case R.id.radio_H1Visa:
                if (checked)
                    setLegalStatus("H1 Visa");
                break;
            case R.id.radio_StudentVisa:
                if (checked)
                    setLegalStatus("Student Visa");
                break;

            case R.id.radio_Others:
                if (checked)
                    setLegalStatus("Other");
                break;
        }
    }

    private class SaveLegalStatus extends AsyncTask<Void, Void, String> {

        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(LegalStatus.this, R.style.progress_dialog);
            progressDialog.setContentView(R.layout.dialog);
            progressDialog.setCancelable(true);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(mContext, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.yearsInUsa, getYearsInUS()));
            parms.add(new BasicNameValuePair(BureauConstants.legalStatus, getLegalStatus()));
            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.LEGAL_STATUS_URL, parms);

        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equals("Error")) {
                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(LegalStatus.this, LegalStatus.this);
                    } else {
                        final Dialog customDialog = new Dialog(mContext);
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
                            }
                        });
                        customDialog.show();
                    }
                } else {
                    AppData.saveString(mContext, BureauConstants.yearsInUsa, getYearsInUS());
                    AppData.saveString(mContext, BureauConstants.legalStatus, getLegalStatus());

                    final Dialog dialog = new Dialog(mContext);
                    // Include dialog.xml file
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.welcome_dialog);
                    dialog.show();

                    Button continueButton = (Button) dialog.findViewById(R.id.continue_button);
                    // if decline button is clicked, close the custom dialog
                    continueButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Close dialog
                            dialog.dismiss();
                            AppData.saveString(mContext, BureauConstants.profileStatus, "completed");
                            AppData.saveString(mContext, BureauConstants.referralCodeApplied, "n");

                            Intent intent = new Intent(mContext, HomeActivity.class);
                            intent.putExtra("pager_position", 0);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    });
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}