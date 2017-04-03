package com.bureau.bureauapp.profileedit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class EducationEdit extends AppCompatActivity {
    private static String sHighLvlEducation;
    private static String s2ndLvlEducation;

    EditText et_HighLvlEducation_edit, et_HighLvlHonor_edit,
            et_HighLvlMajor_edit, et_HighLvlCollege_edit, et_HighLvlYear_edit,
            et_2ndLvlEducation_edit, et_2ndLvlHonor_edit, et_2ndLvlMajor_edit,
            et_2ndLvlCollege_edit, et_2ndLvlYear_edit;
    ImageView ivSaveEducation;
    TextView tv_Add2Lvl_edit;
    LinearLayout ll_2ndLvlEducation_edit;

    final String[] array_Education = {"Doctorate", "Masters", "Bachelors", "Associates", "Grade School"};
    boolean bSecondLevel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_education_edit);
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
        et_HighLvlEducation_edit = (EditText) findViewById(R.id.et_HighLvlEducation_edit);
        et_HighLvlHonor_edit = (EditText) findViewById(R.id.et_HighLvlHonor_edit);
        et_HighLvlMajor_edit = (EditText) findViewById(R.id.et_HighLvlMajor_edit);
        et_HighLvlCollege_edit = (EditText) findViewById(R.id.et_HighLvlCollege_edit);
        et_HighLvlYear_edit = (EditText) findViewById(R.id.et_HighLvlYear_edit);
        et_2ndLvlEducation_edit = (EditText) findViewById(R.id.et_2ndLvlEducation_edit);
        et_2ndLvlHonor_edit = (EditText) findViewById(R.id.et_2ndLvlHonor_edit);
        et_2ndLvlMajor_edit = (EditText) findViewById(R.id.et_2ndLvlMajor_edit);
        et_2ndLvlCollege_edit = (EditText) findViewById(R.id.et_2ndLvlCollege_edit);
        et_2ndLvlYear_edit = (EditText) findViewById(R.id.et_2ndLvlYear_edit);
        tv_Add2Lvl_edit = (TextView) findViewById(R.id.tv_Add2Lvl_edit);
        ll_2ndLvlEducation_edit = (LinearLayout) findViewById(R.id.ll_2ndLvlEducation_edit);
        ivSaveEducation = (ImageView) findViewById(R.id.iv_save_education);

        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        et_HighLvlEducation_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHighLvlEducationDialog();
            }
        });

        tv_Add2Lvl_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_2ndLvlEducation_edit.getVisibility() == View.VISIBLE) {
                    ll_2ndLvlEducation_edit.setVisibility(View.GONE);
                    bSecondLevel = false;
                } else {
                    ll_2ndLvlEducation_edit.setVisibility(View.VISIBLE);
                    tv_Add2Lvl_edit.setVisibility(View.GONE);

                    bSecondLevel = true;
                }
            }
        });

        et_2ndLvlEducation_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show2ndLvlEducationDialog();
            }
        });

        ivSaveEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateEducationDetails()) {
                    if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                        new SaveEditEducationDetails().execute();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        setEducationDetails();
    }

    public void showHighLvlEducationDialog() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(array_Education, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                sHighLvlEducation = array_Education[position];
                et_HighLvlEducation_edit.setText(sHighLvlEducation);
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
                et_2ndLvlEducation_edit.setText(s2ndLvlEducation);
            }
        });

        builder.create();
        builder.show();
    }

    public void setEducationDetails() {
        String high_edu = AppData.getString(EducationEdit.this, BureauConstants.highestEducation);
        String honors = AppData.getString(EducationEdit.this, BureauConstants.honors);
        String major = AppData.getString(EducationEdit.this, BureauConstants.major);
        String college = AppData.getString(EducationEdit.this, BureauConstants.college);
        String grad_year = AppData.getString(EducationEdit.this, BureauConstants.graduatedYear);
        String edu_sec = AppData.getString(EducationEdit.this, BureauConstants.educationSecond);
        String honors_sec = AppData.getString(EducationEdit.this, BureauConstants.honorsSecond);
        String majors_sec = AppData.getString(EducationEdit.this, BureauConstants.majorsSecond);
        String coll_sec = AppData.getString(EducationEdit.this, BureauConstants.collegeSecond);
        String grad_years_sec = AppData.getString(EducationEdit.this, BureauConstants.graduatedYearSecond);

        if (high_edu != null) {
            sHighLvlEducation = high_edu;
            et_HighLvlEducation_edit.setText(high_edu);
        }

        if (honors != null) {
            et_HighLvlHonor_edit.setText(honors);
        }

        if (major != null) {
            et_HighLvlMajor_edit.setText(major);
        }

        if (college != null) {
            et_HighLvlCollege_edit.setText(college);
        }

        if (grad_year != null) {
            et_HighLvlYear_edit.setText(grad_year);
        }

        if (edu_sec != null) {
            if (!edu_sec.equals("")) {
                s2ndLvlEducation = edu_sec;
                ll_2ndLvlEducation_edit.setVisibility(View.VISIBLE);
                tv_Add2Lvl_edit.setVisibility(View.GONE);
                et_2ndLvlEducation_edit.setText(edu_sec);

                if (honors_sec != null) {
                    et_2ndLvlHonor_edit.setText(honors_sec);
                }

                if (majors_sec != null) {
                    et_2ndLvlMajor_edit.setText(majors_sec);
                }

                if (coll_sec != null) {
                    et_2ndLvlCollege_edit.setText(coll_sec);
                }

                if (grad_years_sec != null) {
                    et_2ndLvlYear_edit.setText(grad_years_sec);
                }
            }
        } else {
            ll_2ndLvlEducation_edit.setVisibility(View.GONE);
            tv_Add2Lvl_edit.setVisibility(View.VISIBLE);
        }
    }

    public String getHighLvlEducation() {
        return et_HighLvlEducation_edit.getText().toString();
    }

    public String getHighLvlHonor() {
        return et_HighLvlHonor_edit.getText().toString();
    }

    public String getHighLvlMajor() {
        return et_HighLvlMajor_edit.getText().toString();
    }

    public String getHighLvlCollege() {
        return et_HighLvlCollege_edit.getText().toString();
    }

    public String getHighLvlYear() {
        return et_HighLvlYear_edit.getText().toString();
    }

    public String get2ndLvlEducation() {
        return et_2ndLvlEducation_edit.getText().toString();
    }

    public String get2ndLvlHonor() {
        return et_2ndLvlHonor_edit.getText().toString();
    }

    public String get2ndLvlMajor() {
        return et_2ndLvlMajor_edit.getText().toString();
    }

    public String get2ndLvlCollege() {
        return et_2ndLvlCollege_edit.getText().toString();
    }

    public String get2ndLvlYear() {
        return et_2ndLvlYear_edit.getText().toString();
    }

    private boolean validateEducationDetails() {
        boolean bStatus = false;

        if (sHighLvlEducation == null) {
            et_HighLvlEducation_edit.setError("Please enter heigh level education");
        } else {
            et_HighLvlEducation_edit.setError(null);
            bStatus = true;
        }

        return bStatus;
    }

    private class SaveEditEducationDetails extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(EducationEdit.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(EducationEdit.this, BureauConstants.userid)));
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

            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.EDIT_UPDATE_URL, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            final Dialog dialog = new Dialog(EducationEdit.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.simple_alert);

            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                String response = jsonObject.getString("response");
                if (msg.equals("Error")) {
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(EducationEdit.this, EducationEdit.this);
                    } else {

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
                    AppData.saveString(EducationEdit.this, BureauConstants.highestEducation, getHighLvlEducation());
                    AppData.saveString(EducationEdit.this, BureauConstants.honors, getHighLvlHonor());
                    AppData.saveString(EducationEdit.this, BureauConstants.major, getHighLvlMajor());
                    AppData.saveString(EducationEdit.this, BureauConstants.college, getHighLvlCollege());
                    AppData.saveString(EducationEdit.this, BureauConstants.graduatedYear, getHighLvlYear());
                    AppData.saveString(EducationEdit.this, BureauConstants.educationSecond, get2ndLvlEducation());
                    AppData.saveString(EducationEdit.this, BureauConstants.honorsSecond, get2ndLvlHonor());
                    AppData.saveString(EducationEdit.this, BureauConstants.majorsSecond, get2ndLvlMajor());
                    AppData.saveString(EducationEdit.this, BureauConstants.collegeSecond, get2ndLvlCollege());
                    AppData.saveString(EducationEdit.this, BureauConstants.graduatedYearSecond, get2ndLvlYear());

                    ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Success");
                    ((TextView) dialog.findViewById(R.id.dialogMessage)).setText("Saved Successfully");
                    TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                    text.setText("OK");

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    });
                    dialog.show();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}
