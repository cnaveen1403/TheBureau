package com.bureau.bureauapp.profilesetup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class Occupation extends AppCompatActivity {
    private static String LOG_TAG = Occupation.class.getSimpleName();
    final String[] array_Education = {"Doctorate", "Masters", "Bachelors", "Associates", "Grade School"};

    Context mContext;
    private static String sEmploymentStatus;
    private static String sHighLvlEducation;
    private static String s2ndLvlEducation;

    private static String sCountry;

    boolean bSecondLevel = false;

    RadioButton radio_Employed, radio_Unemployed, radio_Student, radio_Others;
    EditText editText_Position, editText_Company, editText_HighLvlEducation, editText_HighLvlHonor,
            editText_HighLvlMajor, editText_HighLvlCollege, editText_HighLvlYear,
            editText_2ndLvlEducation, editText_2ndLvlHonor, editText_2ndLvlMajor,
            editText_2ndLvlCollege, editText_2ndLvlYear;
    TextView textViewAdd2ndLvl;
    Button btn_SaveOccupation;
    LinearLayout layout_2ndLvlEducation, /*layout_EmployerPosition, layout_EmployerCompany,*/
            layout_EditEmployerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_occupation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        init();
    }

    private void init() {
        mContext = this;

        radio_Employed = (RadioButton) findViewById(R.id.radio_Employed);
        radio_Unemployed = (RadioButton) findViewById(R.id.radio_Unemployed);
        radio_Student = (RadioButton) findViewById(R.id.radio_Student);
        radio_Others = (RadioButton) findViewById(R.id.radio_OtherEmpStatus);
        editText_Position = (EditText) findViewById(R.id.editText_JobTitle);
        editText_Company = (EditText) findViewById(R.id.editText_Company);
        editText_HighLvlEducation = (EditText) findViewById(R.id.editText_HighLvlEducation);
        editText_HighLvlHonor = (EditText) findViewById(R.id.editText_HighLvlHonor);
        editText_HighLvlMajor = (EditText) findViewById(R.id.editText_HighLvlMajor);
        editText_HighLvlCollege = (EditText) findViewById(R.id.editText_HighLvlCollege);
        editText_HighLvlYear = (EditText) findViewById(R.id.editText_HighLvlYear);
        editText_2ndLvlEducation = (EditText) findViewById(R.id.editText_2ndLvlEducation);
        editText_2ndLvlHonor = (EditText) findViewById(R.id.editText_2ndLvlHonor);
        editText_2ndLvlMajor = (EditText) findViewById(R.id.editText_2ndLvlMajor);
        editText_2ndLvlCollege = (EditText) findViewById(R.id.editText_2ndLvlCollege);
        editText_2ndLvlYear = (EditText) findViewById(R.id.editText_2ndLvlYear);
        textViewAdd2ndLvl = (TextView) findViewById(R.id.textView_Add2Lvl);
        btn_SaveOccupation = (Button) findViewById(R.id.btn_SaveOccupation);
        layout_2ndLvlEducation = (LinearLayout) findViewById(R.id.layout_2ndLvlEducation);
        layout_EditEmployerInfo = (LinearLayout) findViewById(R.id.layout_EditEmplyerInfo);

        TextView tv_title = (TextView) findViewById(R.id.toolbar_Occupation_title);
        String profileName = AppData.getString(mContext, "profile_first_name");
        String txtTitle = profileName + "'s Occupation";
        tv_title.setText(txtTitle);

        sCountry = AppData.getString(mContext, "country_residing");
        if (sCountry.equals("USA")) {
            btn_SaveOccupation.setText(R.string.txtContinue);
        }

        setEmploymentStatus("Employed");

        editText_HighLvlEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHighLvlEducationDialog();
            }
        });

        textViewAdd2ndLvl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layout_2ndLvlEducation.getVisibility() == View.VISIBLE) {
                    layout_2ndLvlEducation.setVisibility(View.GONE);
                    bSecondLevel = false;
                } else {
                    layout_2ndLvlEducation.setVisibility(View.VISIBLE);
                    textViewAdd2ndLvl.setVisibility(View.GONE);

                    bSecondLevel = true;
                }
            }
        });

        editText_2ndLvlEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show2ndLvlEducationDialog();
            }
        });

        btn_SaveOccupation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validOccupationInputs()) {
                    if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                        new SaveOccupation().execute();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        setProfOccupationFromContext();
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
        Intent intent = new Intent(mContext, SocialHabit.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(mContext, SocialHabit.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
        return true;
    }

    public void setProfOccupationFromContext() {
        String emp_status = AppData.getString(mContext, "employment_status");
        String pos_title = AppData.getString(mContext, "position_title");
        String comp_name = AppData.getString(mContext, "company");
        String high_edu = AppData.getString(mContext, "highest_education");
        String honors = AppData.getString(mContext, "honors");
        String major = AppData.getString(mContext, "major");
        String college = AppData.getString(mContext, "college");
        String grad_year = AppData.getString(mContext, "graduated_year");

        if (AppData.containsKey(mContext, "education_second")) {
            String edu_sec = AppData.getString(mContext, "education_second");
            String honors_sec = AppData.getString(mContext, "honors_second");
            String majors_sec = AppData.getString(mContext, "majors_second");
            String coll_sec = AppData.getString(mContext, "college_second");
            String grad_years_sec = AppData.getString(mContext, "graduation_years_second");

            Log.e("TheBureau", "Occupation edu sec " + edu_sec);

            if (edu_sec != null && !edu_sec.equals("")) {
                s2ndLvlEducation = edu_sec;
                layout_2ndLvlEducation.setVisibility(View.VISIBLE);
                textViewAdd2ndLvl.setVisibility(View.GONE);
                editText_2ndLvlEducation.setText(edu_sec);

                if (honors_sec != null) {
                    editText_2ndLvlHonor.setText(honors_sec);
                }

                if (majors_sec != null) {
                    editText_2ndLvlMajor.setText(majors_sec);
                }

                if (coll_sec != null) {
                    editText_2ndLvlCollege.setText(coll_sec);
                }

                if (grad_years_sec != null) {
                    editText_2ndLvlYear.setText(grad_years_sec);
                }

            } else {
                layout_2ndLvlEducation.setVisibility(View.GONE);
                textViewAdd2ndLvl.setVisibility(View.VISIBLE);
            }
        }

        if (emp_status != null) {
            setEmploymentStatus(emp_status);
            setEmploymentStatusinUI(emp_status, pos_title, comp_name);
        }

        if (high_edu != null) {
            sHighLvlEducation = high_edu;
            editText_HighLvlEducation.setText(high_edu);
        }

        if (honors != null) {
            editText_HighLvlHonor.setText(honors);
        }

        if (major != null) {
            editText_HighLvlMajor.setText(major);
        }

        if (college != null) {
            editText_HighLvlCollege.setText(college);
        }

        if (grad_year != null) {
            editText_HighLvlYear.setText(grad_year);
        }
    }

    public void setEmploymentStatusinUI(String emp_status, String pos, String comp) {
        if (emp_status.equals("Employed")) {
            employInfoVisibe();
            radio_Employed.setChecked(true);
            if (pos != null) {
                editText_Position.setText(pos);
            }

            if (comp != null) {
                editText_Company.setText(comp);
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

    public void showHighLvlEducationDialog() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(array_Education, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                editText_HighLvlEducation.setError(null);
                sHighLvlEducation = array_Education[position];
                editText_HighLvlEducation.setText(sHighLvlEducation);
            }
        });

        builder.create();
        builder.show();
    }


    public void show2ndLvlEducationDialog() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(array_Education, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                s2ndLvlEducation = array_Education[position];
                editText_2ndLvlEducation.setText(s2ndLvlEducation);
            }
        });

        builder.create();
        builder.show();
    }

    public void onEmpStatusClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_Employed:
                if (checked) {
                    employInfoVisibe();
                    sEmploymentStatus = "Employed";
                }
                break;
            case R.id.radio_Unemployed:
                if (checked) {
                    employInfoVisibilityGone();
                    sEmploymentStatus = "Unemployed";
                }
                break;
            case R.id.radio_Student:
                if (checked) {
                    employInfoVisibilityGone();
                    sEmploymentStatus = "Student";
                }
                break;
            case R.id.radio_OtherEmpStatus:
                if (checked) {
                    employInfoVisibilityGone();
                    sEmploymentStatus = "Others";
                }
                break;
        }
    }

    public void employInfoVisibe() {
        findViewById(R.id.positionTitleTV).setVisibility(View.VISIBLE);
        findViewById(R.id.companyTV).setVisibility(View.VISIBLE);
        findViewById(R.id.collon1TV).setVisibility(View.VISIBLE);
        findViewById(R.id.collon2TV).setVisibility(View.VISIBLE);
        layout_EditEmployerInfo.setVisibility(View.VISIBLE);
    }

    public void employInfoVisibilityGone() {
        findViewById(R.id.positionTitleTV).setVisibility(View.GONE);
        findViewById(R.id.companyTV).setVisibility(View.GONE);
        findViewById(R.id.collon1TV).setVisibility(View.GONE);
        findViewById(R.id.collon2TV).setVisibility(View.GONE);
        layout_EditEmployerInfo.setVisibility(View.GONE);
    }

    public void setEmploymentStatus(String empStatus) {
        sEmploymentStatus = empStatus;
    }

    public String getEmploymentStatus() {
        return sEmploymentStatus;
    }

    public String getPosition() {
        return editText_Position.getText().toString();
    }

    public String getCompanyName() {
        return editText_Company.getText().toString();
    }

    public String getHighLvlEducation() {
        return editText_HighLvlEducation.getText().toString();
    }

    public String getHighLvlHonor() {
        return editText_HighLvlHonor.getText().toString();
    }

    public String getHighLvlMajor() {
        return editText_HighLvlMajor.getText().toString();
    }

    public String getHighLvlCollege() {
        return editText_HighLvlCollege.getText().toString();
    }

    public String getHighLvlYear() {
        return editText_HighLvlYear.getText().toString();
    }

    public String get2ndLvlEducation() {
        return editText_2ndLvlEducation.getText().toString();
    }

    public String get2ndLvlHonor() {
        return editText_2ndLvlHonor.getText().toString();
    }

    public String get2ndLvlMajor() {
        return editText_2ndLvlMajor.getText().toString();
    }

    public String get2ndLvlCollege() {
        return editText_2ndLvlCollege.getText().toString();
    }

    public String get2ndLvlYear() {
        return editText_2ndLvlYear.getText().toString();
    }

    private class SaveOccupation extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(Occupation.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.employmentStatus, getEmploymentStatus()));
            parms.add(new BasicNameValuePair(BureauConstants.positionTitle, getPosition()));
            parms.add(new BasicNameValuePair(BureauConstants.company, getCompanyName()));
            parms.add(new BasicNameValuePair(BureauConstants.highestEducation, getHighLvlEducation()));
            parms.add(new BasicNameValuePair(BureauConstants.honors, getHighLvlHonor()));
            parms.add(new BasicNameValuePair(BureauConstants.major, getHighLvlMajor()));

            parms.add(new BasicNameValuePair(BureauConstants.college, getHighLvlCollege()));
            parms.add(new BasicNameValuePair(BureauConstants.graduatedYear, getHighLvlYear()));
            parms.add(new BasicNameValuePair(BureauConstants.educationSecond, get2ndLvlEducation()));
            parms.add(new BasicNameValuePair(BureauConstants.honorsSecond, get2ndLvlHonor()));
            parms.add(new BasicNameValuePair(BureauConstants.majorsSecond, get2ndLvlMajor()));
            parms.add(new BasicNameValuePair(BureauConstants.collegeSecond, get2ndLvlCollege()));
            parms.add(new BasicNameValuePair(BureauConstants.graduatedYearSecond, get2ndLvlYear()));

//            return HttpUtils.doPost(map, BureauConstants.OCCUPATION_URL);
            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.OCCUPATION_URL, parms);

        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                String response = jsonObject.getString("response");

                if (msg.equals("Error")) {
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(Occupation.this, Occupation.this);
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
                    AppData.saveString(mContext, BureauConstants.employmentStatus, getEmploymentStatus());
                    AppData.saveString(mContext, BureauConstants.positionTitle, getPosition());
                    AppData.saveString(mContext, BureauConstants.company, getCompanyName());
                    AppData.saveString(mContext, BureauConstants.highestEducation, getHighLvlEducation());
                    AppData.saveString(mContext, BureauConstants.honors, getHighLvlHonor());
                    AppData.saveString(mContext, BureauConstants.major, getHighLvlMajor());
                    AppData.saveString(mContext, BureauConstants.college, getHighLvlCollege());
                    AppData.saveString(mContext, BureauConstants.graduatedYear, getHighLvlYear());
                    AppData.saveString(mContext, BureauConstants.educationSecond, get2ndLvlEducation());
                    AppData.saveString(mContext, BureauConstants.honorsSecond, get2ndLvlHonor());
                    AppData.saveString(mContext, BureauConstants.majorsSecond, get2ndLvlMajor());
                    AppData.saveString(mContext, BureauConstants.collegeSecond, get2ndLvlCollege());
                    AppData.saveString(mContext, BureauConstants.graduatedYearSecond, get2ndLvlYear());

                    if (sCountry.equals("USA")) {
                        AppData.saveString(mContext, BureauConstants.profileStatus, "update_profile_step6");
                        Intent intent = new Intent(mContext, LegalStatus.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
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
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        });
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private boolean validOccupationInputs() {
        boolean bStatus = false;

        if (sHighLvlEducation == null) {
            editText_HighLvlEducation.setError("Please enter heigh level education");
        } else {
            editText_HighLvlEducation.setError(null);
            bStatus = true;
        }

        return bStatus;
    }
}
