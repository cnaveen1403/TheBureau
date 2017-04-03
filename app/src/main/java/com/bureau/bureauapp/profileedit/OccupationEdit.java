package com.bureau.bureauapp.profileedit;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class OccupationEdit extends AppCompatActivity {

    private static String LOG_TAG = BasicInfoEdit.class.getSimpleName();
    RadioGroup rg_Occupation;
    RadioButton radioButton, radio_Employed, radio_Unemployed, radio_Student, radio_Others;
    EditText et_position, et_company;
    ImageView save_details;
    LinearLayout layout_EmployerPosition, layout_EmployerCompany, layout_EditEmployerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_occupation_edit);
        init();
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

    public void init() {
        et_position = (EditText) findViewById(R.id.et_position);
        et_company = (EditText) findViewById(R.id.et_company);
        rg_Occupation = (RadioGroup) findViewById(R.id.rg_EmpStatus_edit);
        radio_Employed = (RadioButton) findViewById(R.id.rd_Employed_edit);
        radio_Unemployed = (RadioButton) findViewById(R.id.rd_Unemployed_edit);
        radio_Student = (RadioButton) findViewById(R.id.rd_Student_edit);
        radio_Others = (RadioButton) findViewById(R.id.rd_OtherEmpStatus_edit);
        layout_EmployerPosition = (LinearLayout) findViewById(R.id.ll_EmployerPosition_edit);
        layout_EmployerCompany = (LinearLayout) findViewById(R.id.ll_EmployerCompany_edit);
        layout_EditEmployerInfo = (LinearLayout) findViewById(R.id.ll_EditEmplyerInfo_edit);
        save_details = (ImageView) findViewById(R.id.iv_save_occupation);

        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        save_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                    SaveEditedOccupationDetails saveEditedOccupationDetails = new SaveEditedOccupationDetails();
                    saveEditedOccupationDetails.execute();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
        });

        setOccupationDetails();
    }

    public void setOccupationDetails() {
        String emp_status = AppData.getString(OccupationEdit.this, BureauConstants.employmentStatus);
        String pos_title = AppData.getString(OccupationEdit.this, BureauConstants.positionTitle);
        String comp_name = AppData.getString(OccupationEdit.this, BureauConstants.company);

        if (emp_status != null) {
            setEmploymentStatusinUI(emp_status, pos_title, comp_name);
        }
    }

    public void setEmploymentStatusinUI(String emp_status, String pos, String comp) {
        if (emp_status.equals("Employed")) {
            employInfoVisibe();
            radio_Employed.setChecked(true);
            if (pos != null) {
                et_position.setText(pos);
            }

            if (comp != null) {
                et_company.setText(comp);
            }
        } else {
            employInfoVisibilityGone();
            if (emp_status.equals("Unemployed")) {
                radio_Unemployed.setChecked(true);
            }

            if (emp_status.equals("Student")) {
                radio_Student.setChecked(true);
            }

            if (emp_status.equals("Others")) {
                radio_Others.setChecked(true);
            }
        }
    }

    public void employInfoVisibilityGone() {
        layout_EmployerPosition.setVisibility(View.GONE);
        layout_EmployerCompany.setVisibility(View.GONE);
        layout_EditEmployerInfo.setVisibility(View.GONE);
    }

    public void employInfoVisibe() {
        layout_EmployerPosition.setVisibility(View.VISIBLE);
        layout_EmployerCompany.setVisibility(View.VISIBLE);
        layout_EditEmployerInfo.setVisibility(View.VISIBLE);
    }

    public void onEmpStatusClicked(View view) {
        int country = rg_Occupation.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(country);

        switch (radioButton.getText().toString()) {
            case "Employed":
                employInfoVisibe();
                break;
            case "Unemployed":
                employInfoVisibilityGone();
                break;
            case "Student":
                employInfoVisibilityGone();
                break;
            case "Others":
                employInfoVisibilityGone();
                break;
        }
    }

    public String getEmploymentStatus() {
        int country = rg_Occupation.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(country);
        return radioButton.getText().toString();
    }

    public String getPosition() {
        return et_position.getText().toString();
    }

    public String getCompany() {
        return et_position.getText().toString();
    }

    public class SaveEditedOccupationDetails extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(OccupationEdit.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(OccupationEdit.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.employmentStatus, getEmploymentStatus()));
            parms.add(new BasicNameValuePair(BureauConstants.positionTitle, getPosition()));
            parms.add(new BasicNameValuePair(BureauConstants.company, getCompany()));
            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.EDIT_UPDATE_URL, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            final Dialog customDialog = new Dialog(OccupationEdit.this);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setContentView(R.layout.simple_alert);

            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                if (msg.equals("Error")) {
                    String response = jsonObject.getString("response");
                    if (response.equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(OccupationEdit.this, OccupationEdit.this);
                    } else {

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
                    AppData.saveString(OccupationEdit.this, BureauConstants.employmentStatus, getEmploymentStatus());
                    AppData.saveString(OccupationEdit.this, BureauConstants.positionTitle, getPosition());
                    AppData.saveString(OccupationEdit.this, BureauConstants.company, getCompany());

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
