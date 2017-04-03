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
import android.widget.CompoundButton;
import android.widget.RadioButton;
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

public class SocialHabit extends AppCompatActivity {

    private static String LOG_TAG = SocialHabit.class.getSimpleName();

    private static String sDiet, sDrinking, sSmoking;

    ToggleButton toggle_Drinking, toggle_Smoking;
    RadioButton radioButton;
    TextView textView_Socially, textView_Never, textView_Yes, textView_No;
    Button btn_SaveHabits;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_habit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        init();
    }

    private void init() {
        toggle_Drinking = (ToggleButton) findViewById(R.id.toggle_Drinking);
        toggle_Smoking = (ToggleButton) findViewById(R.id.toggle_Smoking);
        textView_Socially = (TextView) findViewById(R.id.textView_Socially);
        textView_Never = (TextView) findViewById(R.id.textView_Never);
        textView_Yes = (TextView) findViewById(R.id.textView_Yes);
        textView_No = (TextView) findViewById(R.id.textView_No);
        btn_SaveHabits = (Button) findViewById(R.id.btn_SaveHabits);

        TextView tv_title = (TextView) findViewById(R.id.toolbar_social_habits);
        String profileName = AppData.getString(SocialHabit.this, "profile_first_name");
        String txtTitle = profileName + "'s Social Habits";
        tv_title.setText(txtTitle);

        setDiet("Vegetarian");
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

        btn_SaveHabits.setOnClickListener(new View.OnClickListener() {
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
        Intent intent = new Intent(SocialHabit.this, ProfileHeritage.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(SocialHabit.this, ProfileHeritage.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
        return true;
    }

    public void setProfSocialHabitsFromContext() {
        String diet = AppData.getString(SocialHabit.this, "diet");
        String drinking = AppData.getString(SocialHabit.this, "drinking");
        String smoking = AppData.getString(SocialHabit.this, "smoking");

        if (diet != null) {
            if (diet.equals("Vegetarian")) {
                radioButton = (RadioButton) findViewById(R.id.radio_Vegetarian);
                radioButton.setChecked(true);
            } else if (diet.equals("Eggetarian")) {
                radioButton = (RadioButton) findViewById(R.id.radio_Eggetarian);
                radioButton.setChecked(true);
            } else if (diet.equals("Non Vegetarian")) {
                radioButton = (RadioButton) findViewById(R.id.radio_NonVegetarian);
                radioButton.setChecked(true);
            } else if (diet.equals("Vegan")) {
                radioButton = (RadioButton) findViewById(R.id.radio_Vegan);
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
            textView_Yes.setTextColor(getResources().getColor(R.color.colorAccent));
            textView_No.setTextColor(getResources().getColor(R.color.light_grey));
        }
    }

    public void setDiet(String diet) {
        sDiet = diet;
    }

    public void setDrinking(String drinking) {
        sDrinking = drinking;
    }

    public void setSmoking(String smoking) {
        sSmoking = smoking;
    }

    public String getDiet() {
        return sDiet;
    }

    public String getDrinking() {
        return sDrinking;
    }

    public String getSmoking() {
        return sSmoking;
    }

    public void onDietClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_Vegetarian:
                if (checked)
                    setDiet("Vegetarian");
                break;
            case R.id.radio_Eggetarian:
                if (checked)
                    setDiet("Eggetarian");
                break;
            case R.id.radio_NonVegetarian:
                if (checked)
                    setDiet("Non Vegetarian");
                break;
            case R.id.radio_Vegan:
                if (checked)
                    setDiet("Vegan");
                break;
        }
    }

    private class SaveSocialHabits extends AsyncTask<Void, Void, String> {

        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(SocialHabit.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(SocialHabit.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.diet, getDiet()));
            parms.add(new BasicNameValuePair(BureauConstants.drinking, getDrinking()));
            parms.add(new BasicNameValuePair(BureauConstants.smoking, getSmoking()));
//            return HttpUtils.doPost(map, BureauConstants.SOCIAL_HERITAGE_URL);
            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.SOCIAL_HERITAGE_URL, parms);

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
                        com.bureau.bureauapp.util.Util.invalidateUserID(SocialHabit.this, SocialHabit.this);
                    } else {
                        final Dialog customDialog = new Dialog(SocialHabit.this);
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
                    AppData.saveString(SocialHabit.this, BureauConstants.diet, getDiet());
                    AppData.saveString(SocialHabit.this, BureauConstants.drinking, getDrinking());
                    AppData.saveString(SocialHabit.this, BureauConstants.smoking, getSmoking());
                    AppData.saveString(SocialHabit.this, BureauConstants.profileStatus, "update_profile_step5");

                    Intent intent = new Intent(SocialHabit.this, Occupation.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}