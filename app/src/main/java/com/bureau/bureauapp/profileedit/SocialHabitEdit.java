package com.bureau.bureauapp.profileedit;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
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
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SocialHabitEdit extends AppCompatActivity {

    private static String LOG_TAG = SocialHabitEdit.class.getSimpleName();
    private static String sDrinking, sSmoking;

    ToggleButton toggle_Drinking, toggle_Smoking;
    RadioButton radioButton;
    RadioGroup radioGroup;
    ImageView ivSaveHabits;
    TextView textView_Socially, textView_Never, textView_Yes, textView_No;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_social_habit_edit);
        init();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void init() {
        toggle_Drinking = (ToggleButton) findViewById(R.id.tb_drinking_edit);
        toggle_Smoking = (ToggleButton) findViewById(R.id.tb_smoking_edit);
        textView_Socially = (TextView) findViewById(R.id.tv_socially_edit);
        textView_Never = (TextView) findViewById(R.id.tv_never_edit);
        textView_Yes = (TextView) findViewById(R.id.tv_yes_edit);
        textView_No = (TextView) findViewById(R.id.tv_no_edit);
        ivSaveHabits = (ImageView) findViewById(R.id.iv_save_habits);

        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        setDrinking("Socially");
        setSmoking("No");

        toggle_Drinking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setDrinking("Never");

                    textView_Socially.setTextColor(getResources().getColor(R.color.light_grey));
                    textView_Never.setTextColor(getResources().getColor(R.color.colorAccent));

                } else {
                    setDrinking("Socially");

                    textView_Socially.setTextColor(getResources().getColor(R.color.colorAccent));
                    textView_Never.setTextColor(getResources().getColor(R.color.light_grey));
                }
            }
        });

        toggle_Smoking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    setSmoking("Yes");

                    textView_No.setTextColor(getResources().getColor(R.color.light_grey));
                    textView_Yes.setTextColor(getResources().getColor(R.color.colorAccent));
                } else {
                    setSmoking("No");

                    textView_Yes.setTextColor(getResources().getColor(R.color.light_grey));
                    textView_No.setTextColor(getResources().getColor(R.color.colorAccent));
                }
            }
        });

        ivSaveHabits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                    new SaveSocialHabits().execute();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
        });

        setProfSocialHabitsFromContext();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void setProfSocialHabitsFromContext() {
        String diet = AppData.getString(SocialHabitEdit.this, BureauConstants.diet);
        String drinking = AppData.getString(SocialHabitEdit.this, BureauConstants.drinking);
        String smoking = AppData.getString(SocialHabitEdit.this, BureauConstants.smoking);

        if (diet != null) {
            if (diet.equals("Vegetarian")) {
                radioButton = (RadioButton) findViewById(R.id.rd_vegetarian_edit);
                radioButton.setChecked(true);
            } else if (diet.equals("Eggetarian")) {
                radioButton = (RadioButton) findViewById(R.id.rd_eggetarian_edit);
                radioButton.setChecked(true);
            } else if (diet.equals("Non Vegetarian")) {
                radioButton = (RadioButton) findViewById(R.id.rd_nonvegetarian_edit);
                radioButton.setChecked(true);
            } else if (diet.equals("Vegan")) {
                radioButton = (RadioButton) findViewById(R.id.rd_vegan_edit);
                radioButton.setChecked(true);
            }
        }

        if (drinking != null) {
            setDrinkingHabit(drinking);
        }

        if (smoking != null) {
            setSmokingHabit(smoking);
        }
    }

    public void setDrinkingHabit(String habit) {
        if (habit.equals("Never")) {
            toggle_Drinking.setChecked(true);
            textView_Socially.setTextColor(getResources().getColor(R.color.light_grey));
            textView_Never.setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            toggle_Drinking.setChecked(false);
            textView_Socially.setTextColor(getResources().getColor(R.color.colorAccent));
            textView_Never.setTextColor(getResources().getColor(R.color.light_grey));
        }
    }

    public void setSmokingHabit(String habit) {
        if (habit.equals("Yes")) {
            toggle_Smoking.setChecked(true);
            textView_No.setTextColor(getResources().getColor(R.color.light_grey));
            textView_Yes.setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            toggle_Smoking.setChecked(false);
            textView_Yes.setTextColor(getResources().getColor(R.color.light_grey));
            textView_No.setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }

    public void setDrinking(String drinking) {
        sDrinking = drinking;
    }

    public void setSmoking(String smoking) {
        sSmoking = smoking;
    }

    public String getDiet() {
        radioGroup = (RadioGroup) findViewById(R.id.rg_Diet_edit);
        int diet = radioGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(diet);
        return radioButton.getText().toString();
    }

    public String getDrinking() {
        return sDrinking;
    }

    public String getSmoking() {
        return sSmoking;
    }

    private class SaveSocialHabits extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(SocialHabitEdit.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(SocialHabitEdit.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.diet, getDiet()));
            parms.add(new BasicNameValuePair(BureauConstants.drinking, getDrinking()));
            parms.add(new BasicNameValuePair(BureauConstants.smoking, getSmoking()));
            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.EDIT_UPDATE_URL, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            final Dialog customDialog = new Dialog(SocialHabitEdit.this);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setContentView(R.layout.simple_alert);

            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equals("Error")) {
                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(SocialHabitEdit.this, SocialHabitEdit.this);
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
                    AppData.saveString(SocialHabitEdit.this, BureauConstants.diet, getDiet());
                    AppData.saveString(SocialHabitEdit.this, BureauConstants.drinking, getDrinking());
                    AppData.saveString(SocialHabitEdit.this, BureauConstants.smoking, getSmoking());

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
