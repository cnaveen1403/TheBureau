package com.bureau.bureauapp.profilesetup;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.myapplication.AppData;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AccountCreation extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static String LOG_TAG = AccountCreation.class.getSimpleName();

    EditText editText_Firstname, editText_Lastname, editText_Phone, editText_Email,
            editText_ProfileFirstName, editText_ProfileLastName;
    private static EditText editText_Dob;
    ImageView image_female, image_male;
    ToggleButton toggleButton_gender;
    Spinner spinner_profile;
    Button button_continue;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_creation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        init();
    }

    public void init() {

        editText_Firstname = (EditText) findViewById(R.id.etFirstName);
        editText_Lastname = (EditText) findViewById(R.id.etLastName);
        editText_Dob = (EditText) findViewById(R.id.etDOB);
        image_female = (ImageView) findViewById(R.id.ivFemale);
        image_male = (ImageView) findViewById(R.id.ivMale);
        toggleButton_gender = (ToggleButton) findViewById(R.id.toggleButton_gender);
        editText_Phone = (EditText) findViewById(R.id.etPhoneNo);
        editText_Email = (EditText) findViewById(R.id.etEmail);
        spinner_profile = (Spinner) findViewById(R.id.spinner_profilefor);
        editText_ProfileFirstName = (EditText) findViewById(R.id.etProfileFirstName);
        editText_ProfileLastName = (EditText) findViewById(R.id.etProfileLastName);
        button_continue = (Button) findViewById(R.id.button_continue);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.profilefor_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_profile.setAdapter(adapter);

        String regType = AppData.getString(AccountCreation.this, BureauConstants.regType);
        if (regType != null) {
            if (regType.equals("fb")) {
                editText_Firstname.setText(AppData.getString(AccountCreation.this, BureauConstants.firstName));
                editText_Lastname.setText(AppData.getString(AccountCreation.this, BureauConstants.lastName));
                editText_Email.setText(AppData.getString(AccountCreation.this, BureauConstants.email));
                editText_Dob.setText(AppData.getString(AccountCreation.this, BureauConstants.dob));
                editText_Phone.setText(AppData.getString(AccountCreation.this, BureauConstants.phoneNumber));

                if (AppData.getString(AccountCreation.this, BureauConstants.gender).equals("male")) {
                    toggleButton_gender.setChecked(false);
                } else {
                    toggleButton_gender.setChecked(true);
                }
            } else {
                editText_Phone.setText(AppData.getString(AccountCreation.this, BureauConstants.phoneNumber));
            }
        }

        String profilefor = spinner_profile.getSelectedItem().toString();
        if (profilefor.equals("Self")) {
            editText_ProfileFirstName.setText(getFirstName());
            editText_ProfileLastName.setText(getLastName());
        }

        editText_Firstname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String profile = spinner_profile.getSelectedItem().toString();
                if (profile.equals("Self")) {
                    editText_ProfileFirstName.setText(editText_Firstname.getText().toString());
                }
            }
        });

        editText_Lastname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String profile = spinner_profile.getSelectedItem().toString();
                if (profile.equals("Self")) {
                    editText_ProfileLastName.setText(editText_Lastname.getText().toString());
                }
            }
        });

        /* ----- open Date Dialog ----- */
        editText_Dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialogFragment(v);
            }
        });

        editText_Dob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDateDialogFragment(v);
                }
            }
        });

        /* ----- Dropdown Changed ----- */
        spinner_profile.setOnItemSelectedListener(this);

        /*----- Toggle button clicked ----- */
        toggleButton_gender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

        /*----- Profile Setup 1 submit ----- */
        button_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                        new AccountCreationAsyncTask().execute();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        setAccInfoFromContext();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void setAccInfoFromContext() {
        String first_name = AppData.getString(AccountCreation.this, "first_name");
        String last_name = AppData.getString(AccountCreation.this, "last_name");
        String dob = AppData.getString(AccountCreation.this, "dob");
        String gender = AppData.getString(AccountCreation.this, "gender");
        String profile_for = AppData.getString(AccountCreation.this, "profile_for");
        String phone_number = AppData.getString(AccountCreation.this, "phone_number");
        String email = AppData.getString(AccountCreation.this, "email");
        String profile_first_name = AppData.getString(AccountCreation.this, "profile_first_name");
        String profile_last_name = AppData.getString(AccountCreation.this, "profile_last_name");

        if (first_name != null) {
            editText_Firstname.setText(first_name);
        }

        if (first_name != null) {
            editText_Lastname.setText(last_name);
        }

        if (dob != null) {
            editText_Dob.setText(dob);
        }

        if (gender != null) {
            setGender(gender);
        }

        if (phone_number != null) {
            editText_Phone.setText(phone_number);
        }

        if (email != null) {
            editText_Email.setText(email);
        }

        if (profile_for != null) {
            setProfileFor(profile_for, profile_first_name, profile_last_name);
        }
    }

    public void setGender(String gender) {
        if (gender.equals("Male"))
            toggleButton_gender.setChecked(false);
        else
            toggleButton_gender.setChecked(true);
    }

    public void setProfileFor(String profileFor, String profile_first_name, String profile_last_name) {
        spinner_profile.setSelection(getIndex(spinner_profile, profileFor));
        if (profile_first_name != null) {
            editText_ProfileFirstName.setText(profile_first_name);
        }

        if (profile_last_name != null) {
            editText_ProfileLastName.setText(profile_last_name);
        }
    }

    private int getIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }

        return index;
    }

    public void onItemSelected(AdapterView<?> parentView, View v, int position, long id) {
        String profilefor = spinner_profile.getSelectedItem().toString();
        if (profilefor.equals("Self")) {
            editText_ProfileFirstName.setText(getFirstName());
            editText_ProfileLastName.setText(getLastName());
        } else {
            editText_ProfileFirstName.setText("");
            editText_ProfileLastName.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    public void onNothingSelected(AdapterView<?> parentView) {
        //
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // your code
            finishAffinity();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void showDateDialogFragment(View v) {
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
            int year = 1990;
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the chosen date
            String selectedDate = (month + 1) + "-" + day + "-" + year;
            editText_Dob.setText(selectedDate);
        }
    }

    public String getFirstName() {
        return editText_Firstname.getText().toString().trim();
    }

    public String getLastName() {
        return editText_Lastname.getText().toString().trim();
    }

    public String getDOB() {
        return editText_Dob.getText().toString().trim();
    }

    public String getPhoneNo() {
        return editText_Phone.getText().toString().trim();
    }

    public String getEmail() {
        String email = editText_Email.getText().toString().trim();
        return email;
    }

    public String getProfileFirstName() {
        return editText_ProfileFirstName.getText().toString().trim();
    }

    public String getProfileLastName() {
        return editText_ProfileLastName.getText().toString().trim();
    }

    public String getGender() {
        String gender = "Male";
        toggleButton_gender = (ToggleButton) findViewById(R.id.toggleButton_gender);
        if (toggleButton_gender.isChecked()) {
            gender = "Female";
        }

        return gender;
    }

    public String getProfileFor() {
        return spinner_profile.getSelectedItem().toString().trim();
    }

    public String getURL() {
        String URL = BureauConstants.BASE_URL + BureauConstants.ACCOUNT_UPDATE_URL;
        String profile_status = AppData.getString(AccountCreation.this, BureauConstants.profileStatus);

        if (profile_status != null) {
            if (profile_status.equals("create_account_ws")) {
                URL = BureauConstants.BASE_URL + BureauConstants.ACCOUNT_CREATION_URL;
            }
        }

        return URL;
    }

    public boolean validate() {
        boolean status = false;
        String firstname = getFirstName();
        String dob = getDOB();
        String phone = getPhoneNo();
        String email = getEmail();
        String profile_firstname = getProfileFirstName();
        String profile_lastname = getProfileLastName();

        if (firstname.equals("")) {
            editText_Firstname.setError("Please Enter first name");
        } else if (dob.equals("")) {
            editText_Dob.setError("Please Enter Date Of Birth !!!");
        } else if (phone.equals("")) {
            editText_Phone.setError("Please Enter Phone No !!!");
        } else if (email.equals("")) {
            editText_Email.setError("please enter your email !!!");
        } else if (!email.equals("") && !email.matches(emailPattern)) {
            editText_Email.setError("please enter your email !!!");
        } else if (profile_firstname.equals("")) {
            editText_ProfileFirstName.setError("please enter Profile's first name !!!");
        } else if (profile_lastname.equals("")) {
            editText_ProfileLastName.setError("please enter Profile's last name !!!");
        } else status = true;

        return status;
    }

    private class AccountCreationAsyncTask extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(AccountCreation.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(AccountCreation.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.firstName, getFirstName()));
            parms.add(new BasicNameValuePair(BureauConstants.lastName, getLastName()));
            parms.add(new BasicNameValuePair(BureauConstants.dob, getDOB()));
            parms.add(new BasicNameValuePair(BureauConstants.gender, getGender()));
            parms.add(new BasicNameValuePair(BureauConstants.phoneNumber, getPhoneNo()));
            parms.add(new BasicNameValuePair(BureauConstants.email, getEmail()));
            parms.add(new BasicNameValuePair(BureauConstants.profileFor, getProfileFor()));
            parms.add(new BasicNameValuePair(BureauConstants.profileFirstName, getProfileFirstName()));
            parms.add(new BasicNameValuePair(BureauConstants.profileLastName, getProfileLastName()));

            return new ConnectBureau().getDataFromUrl(getURL(), parms);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();

//            Log.e("Bereau","Account Creation result: "+result);

            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equals("Error")) {
                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(AccountCreation.this, AccountCreation.this);
                    } else {
                        final Dialog customDialog = new Dialog(AccountCreation.this);
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
                    AppData.saveString(AccountCreation.this, BureauConstants.firstName, getFirstName());
                    AppData.saveString(AccountCreation.this, BureauConstants.lastName, getLastName());
                    AppData.saveString(AccountCreation.this, BureauConstants.dob, getDOB());
                    AppData.saveString(AccountCreation.this, BureauConstants.gender, getGender());
                    AppData.saveString(AccountCreation.this, BureauConstants.profileFor, getProfileFor());
                    AppData.saveString(AccountCreation.this, BureauConstants.phoneNumber, getPhoneNo());
                    AppData.saveString(AccountCreation.this, BureauConstants.email, getEmail());
                    AppData.saveString(AccountCreation.this, BureauConstants.profileFirstName, getProfileFirstName());
                    AppData.saveString(AccountCreation.this, BureauConstants.profileLastName, getProfileLastName());
                    AppData.saveString(AccountCreation.this, BureauConstants.profileStatus, "update_profile_step2");

                    Intent intent = new Intent(AccountCreation.this, ProfileInfo.class);
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