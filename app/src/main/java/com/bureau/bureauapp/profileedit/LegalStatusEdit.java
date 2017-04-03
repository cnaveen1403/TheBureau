package com.bureau.bureauapp.profileedit;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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

public class LegalStatusEdit extends AppCompatActivity {

    ImageView save;
    RadioButton radioButton;
    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_legal_status_edit);
        init();
    }

    private void init() {
        save = (ImageView) findViewById(R.id.iv_save_legal_status);

        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                    new SaveLegalStatusEdit().execute();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
        });

        setProfLegalStatusFromContext();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public String getYearsInUS() {
        radioGroup = (RadioGroup) findViewById(R.id.rg_USAResident_edit);
        int mar_status_selected = radioGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(mar_status_selected);
        String years = radioButton.getText().toString();
        if (years.equals("0 – 2 Years")) {
            years = "0 - 2";
        } else if (years.equals("2 – 6 Years")) {
            years = "2 - 6";
        } else if (years.equals("6+ Years")) {
            years = "6+";
        }

        return years;
    }

    public String getLegalStatus() {
        radioGroup = (RadioGroup) findViewById(R.id.rg_LegalStatus_edit);
        int mar_status_selected = radioGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(mar_status_selected);
        return radioButton.getText().toString();
    }

    public void setProfLegalStatusFromContext() {
        String yearsInUsa = AppData.getString(LegalStatusEdit.this, BureauConstants.yearsInUsa);
        String legalStatus = AppData.getString(LegalStatusEdit.this, BureauConstants.legalStatus);
        if (yearsInUsa != null) {
            setYearsInUI(yearsInUsa);
        }

        if (legalStatus != null) {
            setLegalStatusUI(legalStatus);
        }
    }

    public void setYearsInUI(String years) {
        if (years.equals("0 - 2")) {
            radioButton = (RadioButton) findViewById(R.id.rd_ZeroToTwoYrs_edit);
            radioButton.setChecked(true);
        } else if (years.equals("2 - 6")) {
            radioButton = (RadioButton) findViewById(R.id.rd_TwoToSixYrs_edit);
            radioButton.setChecked(true);
        } else if (years.equals("6+")) {
            radioButton = (RadioButton) findViewById(R.id.rd_SixPlusYrs_edit);
            radioButton.setChecked(true);
        } else if (years.equals("Born & Raised")) {
            radioButton = (RadioButton) findViewById(R.id.rd_FromBirth_edit);
            radioButton.setChecked(true);
        }
    }

    public void setLegalStatusUI(String status) {

        if (status.equals("Citizen/Green Card")) {
            radioButton = (RadioButton) findViewById(R.id.rd_USCitizen_edit);
            radioButton.setChecked(true);
        } else if (status.equals("Greencard")) {
            radioButton = (RadioButton) findViewById(R.id.rd_Greencard_edit);
            radioButton.setChecked(true);
        } else if (status.equals("Greencard Processing")) {
            radioButton = (RadioButton) findViewById(R.id.rd_GreencardProcessing_edit);
            radioButton.setChecked(true);
        } else if (status.equals("H1 Visa")) {
            radioButton = (RadioButton) findViewById(R.id.rd_H1Visa_edit);
            radioButton.setChecked(true);
        } else if (status.equals("Student Visa")) {
            radioButton = (RadioButton) findViewById(R.id.rd_StudentVisa_edit);
            radioButton.setChecked(true);
        } else if (status.equals("Other")) {
            radioButton = (RadioButton) findViewById(R.id.rd_Others_edit);
            radioButton.setChecked(true);
        }
    }

    private class SaveLegalStatusEdit extends AsyncTask<Void, Void, String> {

        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(LegalStatusEdit.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(LegalStatusEdit.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.yearsInUsa, getYearsInUS()));
            parms.add(new BasicNameValuePair(BureauConstants.legalStatus, getLegalStatus()));
            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.EDIT_UPDATE_URL, parms);
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
                        com.bureau.bureauapp.util.Util.invalidateUserID(LegalStatusEdit.this, LegalStatusEdit.this);
                    } else {
                        final Dialog customDialog = new Dialog(LegalStatusEdit.this);
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
                    AppData.saveString(LegalStatusEdit.this, BureauConstants.yearsInUsa, getYearsInUS());
                    AppData.saveString(LegalStatusEdit.this, BureauConstants.legalStatus, getLegalStatus());

                    final Dialog customDialog = new Dialog(LegalStatusEdit.this);
                    customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    customDialog.setContentView(R.layout.simple_alert);
                    ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Success");
                    ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText("Saved Successfully");
                    TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                    text.setText("OK");

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            customDialog.dismiss();
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    });
                    customDialog.show();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}