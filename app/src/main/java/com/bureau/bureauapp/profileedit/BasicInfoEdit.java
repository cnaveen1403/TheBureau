package com.bureau.bureauapp.profileedit;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.myapplication.AppData;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BasicInfoEdit extends AppCompatActivity {

    private static String LOG_TAG = BasicInfoEdit.class.getSimpleName();

    ToggleButton tb_gender;
    RadioGroup rg_martial_status;
    RadioButton radioButton;
    ImageView image_female, image_male, iv_save_basic_info;
    private static EditText et_ProfileDOB;
    EditText et_Zipcode, et_Height;
    NumberPicker foot_np, inches_np;
    LinearLayout tv_okay, tv_cancel;

    String sHeightInFoot, sHeightInInches;

    final String[] footvalues = {"4\'", "5\'", "6\'", "7\'"};
    final String[] inchvalues = {"0\"", "1\"", "2\"", "3\"", "4\"", "5\"", "6\"", "7\""
            , "8\"", "9\"", "10\"", "11\"", "12\""};

    String footSelected = "4\'";
    String inchesSelected = "0\"";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_basic_edit);

        init();
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

    public void init() {
        tb_gender = (ToggleButton) findViewById(R.id.tb_gender_edit);
        image_female = (ImageView) findViewById(R.id.iv_female_edit);
        image_male = (ImageView) findViewById(R.id.iv_male_edit);
        iv_save_basic_info = (ImageView) findViewById(R.id.iv_save_basic_info);
        et_ProfileDOB = (EditText) findViewById(R.id.et_dob_edit);
        et_Zipcode = (EditText) findViewById(R.id.et_zip_code_edit);
        et_Height = (EditText) findViewById(R.id.et_height_edit);
        rg_martial_status = (RadioGroup) findViewById(R.id.rg_MaritialStatus);

        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        /*
        * Set Basic Info to edit
        * */
        setProfileDetails();

        tb_gender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    image_male.setImageResource(R.drawable.img_male_black);
                    image_female.setImageResource(R.drawable.img_female_red);
                } else {
                    image_female.setImageResource(R.drawable.img_female_black);
                    image_male.setImageResource(R.drawable.img_male_red);
                }
            }
        });

        et_ProfileDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDOBDateDialogue(v);
            }
        });

        et_ProfileDOB.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDOBDateDialogue(v);
                }
            }
        });

        et_Zipcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String zipcode = getZipCode();
                if (!hasFocus) {
                    if (zipcode.length() > 0) {
                        if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                            CheckZipcodeTask checkZipcode = new CheckZipcodeTask();
                            checkZipcode.execute();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });

        et_Height.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHeightPicker(v);
            }
        });

        iv_save_basic_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateBasicInfo()) {
                    if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                        SaveEditedBasicDetails saveProfileBasicDetails = new SaveEditedBasicDetails();
                        saveProfileBasicDetails.execute();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void showDOBDateDialogue(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 1);
        DialogFragment dFragment = new DatePickerFragment();
        dFragment.show(getFragmentManager(), "date picker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int year = (calendar.get(Calendar.YEAR) - 20);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the chosen date
            String selectedDate = (month + 1) + "-" + day + "-" + year;
            et_ProfileDOB.setText(selectedDate);
        }
    }

    public void showHeightPicker(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 1);

        final Dialog dialog = new Dialog(BasicInfoEdit.this);
        dialog.setContentView(R.layout.height_picker);
        foot_np = (NumberPicker) dialog.findViewById(R.id.foot_np);
        inches_np = (NumberPicker) dialog.findViewById(R.id.inch_np);
        tv_cancel = (LinearLayout) dialog.findViewById(R.id.ll_cancel);
        tv_okay = (LinearLayout) dialog.findViewById(R.id.ll_Okay);

        foot_np.setMinValue(0); //from array first value
        foot_np.setMaxValue(footvalues.length - 1);
        foot_np.setDisplayedValues(footvalues);
        foot_np.setWrapSelectorWheel(true);

        inches_np.setMinValue(0);
        inches_np.setMaxValue(inchvalues.length - 1);
        inches_np.setDisplayedValues(inchvalues);
        inches_np.setWrapSelectorWheel(true);

        dialog.show();

        foot_np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                footSelected = footvalues[newVal];
            }
        });

        inches_np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                inchesSelected = inchvalues[newVal];
            }
        });

        tv_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String selectedValue = footSelected + " " + inchesSelected;
                sHeightInFoot = footSelected;
                sHeightInInches = inchesSelected;
                et_Height.setText(selectedValue);
                footSelected = "4\'";
                inchesSelected = "0\"";
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                footSelected = "4\'";
                inchesSelected = "0\"";
            }
        });
    }

    private String getProfileGender() {
        String gender = "Male";
        if (tb_gender.isChecked()) {
            gender = "Female";
        }

        return gender;
    }

    private String getProfileFirstName() {
        return ((EditText) findViewById(R.id.et_first_name_edit)).getText().toString();
    }

    private String getProfileLastName() {
        return ((EditText) findViewById(R.id.et_last_name_edit)).getText().toString();
    }

    private String getProfileDOB() {
        return et_ProfileDOB.getText().toString();
    }

    private String getZipCode() {
        return et_Zipcode.getText().toString();
    }

    public String getSelectedHeightInFoot() {
        return sHeightInFoot;
    }

    public String getSelectedHeightInInches() {
        return sHeightInInches;
    }

    public void setProfileDetails() {
        String gender = AppData.getString(BasicInfoEdit.this, BureauConstants.profileGender);
        String maritial_status = AppData.getString(BasicInfoEdit.this, BureauConstants.maritialStatus);
        String height_feet = AppData.getString(BasicInfoEdit.this, BureauConstants.heightFeet);
        String height_inch = AppData.getString(BasicInfoEdit.this, BureauConstants.heightInch);

        ((EditText) findViewById(R.id.et_first_name_edit)).setText(AppData.getString(BasicInfoEdit.this, BureauConstants.profileFirstName));
        ((EditText) findViewById(R.id.et_last_name_edit)).setText(AppData.getString(BasicInfoEdit.this, BureauConstants.profileLastName));
        ((EditText) findViewById(R.id.et_dob_edit)).setText(AppData.getString(BasicInfoEdit.this, BureauConstants.profileDob));
        ((EditText) findViewById(R.id.et_zip_code_edit)).setText(AppData.getString(BasicInfoEdit.this, BureauConstants.currentZipCode));
        ((EditText) findViewById(R.id.et_height_edit)).setText(getHeightFormat());
        setGender(gender);
        setMaritialStatus(maritial_status);

        if (height_feet != null && height_inch != null) {
            sHeightInFoot = height_feet;
            sHeightInInches = height_inch;
            String selectedValue = "";
            if (!height_feet.equals("") && !height_inch.equals("")) {
                selectedValue = height_feet + "' " + height_inch;
            }
            et_Height.setText(selectedValue);
        }
    }

    public String getHeightFormat() {
        String foot = AppData.getString(BasicInfoEdit.this, BureauConstants.heightFeet);
        String inches = AppData.getString(BasicInfoEdit.this, BureauConstants.heightInch);
        return foot + "' " + inches + "\"";
    }

    public void setGender(String gender) {
        if (gender.equalsIgnoreCase("Male")) {
            tb_gender.setChecked(true);
        } else {
            tb_gender.setChecked(false);
        }
    }

    public void setMaritialStatus(String maritial_status) {
        if (maritial_status != null) {
            if (maritial_status.equalsIgnoreCase("Never Married")) {
                radioButton = (RadioButton) findViewById(R.id.rd_NeverMarried);
                radioButton.setChecked(true);
            } else if (maritial_status.equalsIgnoreCase("Divorced")) {
                radioButton = (RadioButton) findViewById(R.id.rd_Divorced);
                radioButton.setChecked(true);
            } else if (maritial_status.equalsIgnoreCase("Widowed")) {
                radioButton = (RadioButton) findViewById(R.id.rd_Widowed);
                radioButton.setChecked(true);
            }
        }
    }

    public String getMaritialStatus() {
        int mar_status_selected = rg_martial_status.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(mar_status_selected);
        return radioButton.getText().toString();
    }

    public boolean validateBasicInfo() {
        boolean status = false;

        String zipcode = getZipCode();
        String dob = getProfileDOB();
        String heightEditText = et_Height.getText().toString();

        if (zipcode.equals("")) {
            et_Zipcode.setError("Please enter country zip code");
        } else if (heightEditText.equals("")) {
            et_Zipcode.setError(null);
            et_Height.setError("Please select height");
        } else if (dob.equals("")) {
            et_Height.setError(null);
            et_ProfileDOB.setError("Please enter date of birth !!!");
        } else {
            et_ProfileDOB.setError(null);
            status = true;
        }

        return status;
    }

    private class CheckZipcodeTask extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(BasicInfoEdit.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.zipCode, getZipCode()));

            String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = BureauConstants.BASE_URL + BureauConstants.CHECK_ZIPCODE_URL;
            url += "?";
            url += paramString;

            return new ConnectBureau().getDataFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                if (msg.equalsIgnoreCase("Error")) {
                    String response = jsonObject.getString("response");

                    if (response.equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(BasicInfoEdit.this, BasicInfoEdit.this);
                    } else {

                        final Dialog dialog = new Dialog(BasicInfoEdit.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.simple_alert);
                        ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText(msg);
                        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText("Invalid Zipcode !!!");
// set the custom dialog components - text, image and button
                        TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                et_Zipcode.setText("");
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

    private class SaveEditedBasicDetails extends AsyncTask<String, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(BasicInfoEdit.this, R.style.progress_dialog);
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

            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(BasicInfoEdit.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.profileFirstName, getProfileFirstName()));
            parms.add(new BasicNameValuePair(BureauConstants.profileLastName, getProfileLastName()));
            parms.add(new BasicNameValuePair(BureauConstants.profileDob, getProfileDOB()));
            parms.add(new BasicNameValuePair(BureauConstants.profileGender, getProfileGender()));
            parms.add(new BasicNameValuePair(BureauConstants.currentZipCode, getZipCode()));
            parms.add(new BasicNameValuePair(BureauConstants.heightFeet, getSelectedHeightInFoot()));
            parms.add(new BasicNameValuePair(BureauConstants.heightInch, getSelectedHeightInInches()));
            parms.add(new BasicNameValuePair(BureauConstants.maritialStatus, getMaritialStatus()));

            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.EDIT_UPDATE_URL, parms);

        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            final Dialog dialog = new Dialog(BasicInfoEdit.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.simple_alert);

            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equalsIgnoreCase("Error")) {
                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(BasicInfoEdit.this, BasicInfoEdit.this);
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
                    AppData.saveString(BasicInfoEdit.this, BureauConstants.profileFirstName, getProfileFirstName());
                    AppData.saveString(BasicInfoEdit.this, BureauConstants.profileLastName, getProfileLastName());
                    AppData.saveString(BasicInfoEdit.this, BureauConstants.profileDob, getProfileDOB());
                    AppData.saveString(BasicInfoEdit.this, BureauConstants.profileGender, getProfileGender());
                    AppData.saveString(BasicInfoEdit.this, BureauConstants.currentZipCode, getZipCode());
                    AppData.saveString(BasicInfoEdit.this, BureauConstants.heightFeet, getSelectedHeightInFoot());
                    AppData.saveString(BasicInfoEdit.this, BureauConstants.heightInch, getSelectedHeightInInches());
                    AppData.saveString(BasicInfoEdit.this, BureauConstants.maritialStatus, getMaritialStatus());

                    ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Success");
                    ((TextView) dialog.findViewById(R.id.dialogMessage)).setText("Saved Successfully");
// set the custom dialog components - text, image and button
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
