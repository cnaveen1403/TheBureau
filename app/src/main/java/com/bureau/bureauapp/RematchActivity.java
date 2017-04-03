package com.bureau.bureauapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bureau.bureauapp.adapters.ViewPagerAdapter;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.CirclePageIndicator;
import com.bureau.bureauapp.helperclasses.ClickableViewPager;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.helperclasses.GetAvailableGold;
import com.bureau.bureauapp.helperclasses.ImageZoomActivity;
import com.bureau.bureauapp.model.MatchInfo;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.util.Util;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RematchActivity extends Activity {
    TextView createdByTV, nameTV, ageTV, locationTV, heightTV, motherTongueTV, religionTV, familyOriginTV,
            specificationTV, educationTV, honorsTV, majorTV, schoolTV, yearTV, occupationTV, employerTV,
            dietTV, smokeTV, drinkTV, yearsInUSATV, legalStatusTV, dateOfBirthTV, timeOfBirthTV,
            locationOfBirthTV, aboutMeTV, tvGoldAvailable, titleTV;
    LinearLayout ll_dob, ll_tob, ll_lob;
    int goldUsage = 0;
    Button rematchBTN;
    ClickableViewPager mpager;
    ViewPagerAdapter madapter_viewPager;
    String[] pager_items;
    CirclePageIndicator mIndicator;
    String userId = null;
    MatchInfo matchInfo;
    ImageView backImage;
    String fromActivity = "rematch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_rematch);

        rematchBTN = (Button) findViewById(R.id.rematchBTN);
        createdByTV = (TextView) findViewById(R.id.createdByTV);
        nameTV = (TextView) findViewById(R.id.nameTV);
        ageTV = (TextView) findViewById(R.id.ageTV);
        locationTV = (TextView) findViewById(R.id.locationTV);
        heightTV = (TextView) findViewById(R.id.heightTV);
        motherTongueTV = (TextView) findViewById(R.id.motherToungueTV);
        religionTV = (TextView) findViewById(R.id.religionTV);
        familyOriginTV = (TextView) findViewById(R.id.familyOriginTV);
        specificationTV = (TextView) findViewById(R.id.specificationTV);
        educationTV = (TextView) findViewById(R.id.educationTV);
        honorsTV = (TextView) findViewById(R.id.honorsTV);
        majorTV = (TextView) findViewById(R.id.majorTV);
        schoolTV = (TextView) findViewById(R.id.schoolTV);
        yearTV = (TextView) findViewById(R.id.yearTV);
        occupationTV = (TextView) findViewById(R.id.occupationTV);
        employerTV = (TextView) findViewById(R.id.employerTV);
        dietTV = (TextView) findViewById(R.id.dietTV);
        smokeTV = (TextView) findViewById(R.id.smokeTV);
        drinkTV = (TextView) findViewById(R.id.drinkTV);
        yearsInUSATV = (TextView) findViewById(R.id.yearsInUSATV);
        legalStatusTV = (TextView) findViewById(R.id.legalStatusTV);
        dateOfBirthTV = (TextView) findViewById(R.id.dateOfBirthTV);
        timeOfBirthTV = (TextView) findViewById(R.id.timeOfBirthTV);
        locationOfBirthTV = (TextView) findViewById(R.id.locationOfBirthTV);
        aboutMeTV = (TextView) findViewById(R.id.aboutMeTV);
        mpager = (ClickableViewPager) findViewById(R.id.rematch_pager);
        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        tvGoldAvailable = (TextView) findViewById(R.id.tv_gold_count_rematch);
        backImage = (ImageView) findViewById(R.id.backIconIMG);
        titleTV = (TextView) findViewById(R.id.title_rematch);
        ll_dob = (LinearLayout) findViewById(R.id.ll_dob);
        ll_tob = (LinearLayout) findViewById(R.id.ll_tob);
        ll_lob = (LinearLayout) findViewById(R.id.ll_lob);

        /*
        * Update Gold Value if Changed
        * */
        new GetAvailableGold(this);

        String goldAvailable = AppData.getString(getApplicationContext(), BureauConstants.goldAvailable);
        if (goldAvailable == null) {
            new GetAvailableGold(this);
            goldAvailable = AppData.getString(getApplicationContext(), BureauConstants.goldAvailable);
        }

        tvGoldAvailable.setText(goldAvailable);

        fromActivity = getIntent().getExtras().getString("fromActivity");
        if (fromActivity.equalsIgnoreCase("rematch")) {
            titleTV.setText("Rematch");
            rematchBTN.setVisibility(View.VISIBLE);
            goldUsage = getIntent().getExtras().getInt("goldUsage", 0);
            matchInfo = EventBus.getDefault().removeStickyEvent(MatchInfo.class);
            uiUpdateWithValues(matchInfo);
        } else {
            titleTV.setText("Profile Details");
            if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                userId = getIntent().getExtras().getString("userId", null);
                if (userId != null) {
                    new ReadUserProfileDetails().execute();
                }
                rematchBTN.setVisibility(View.GONE);
            } else {
                Toast.makeText(RematchActivity.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
            }
        }

        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void uiUpdateWithValues(final MatchInfo matchInfo) {

        createdByTV.setText("Created by : " + matchInfo.getProfileCreatedBy());

        if (!fromActivity.equals("rematch")) {
            nameTV.setText(matchInfo.getProfileName());
            ll_dob.setVisibility(View.VISIBLE);
            ll_tob.setVisibility(View.VISIBLE);
            ll_lob.setVisibility(View.VISIBLE);
            dateOfBirthTV.setText(matchInfo.getProfileDOB());
            timeOfBirthTV.setText(matchInfo.getTimeOfBirth());
            locationOfBirthTV.setText(matchInfo.getBirthLocation());
        } else {
            nameTV.setText("Private until connected");
            ll_dob.setVisibility(View.GONE);
            ll_tob.setVisibility(View.GONE);
            ll_lob.setVisibility(View.GONE);
        }

        ageTV.setText(matchInfo.getAge());
        locationTV.setText(matchInfo.getLocation());
        heightTV.setText(matchInfo.getHeight_feet() + "'" + matchInfo.getHeight_inch() + "\"");
        motherTongueTV.setText(matchInfo.getMotherToungue());
        religionTV.setText(matchInfo.getReligion());
        familyOriginTV.setText(matchInfo.getFamilyOrigin());
        specificationTV.setText(matchInfo.getSpecification());
        educationTV.setText(matchInfo.getEducation());
        honorsTV.setText(matchInfo.getHonor());
        majorTV.setText(matchInfo.getMajor());
        schoolTV.setText(matchInfo.getCollege());
        yearTV.setText(matchInfo.getYear());
        occupationTV.setText(matchInfo.getOccupation());
        employerTV.setText(matchInfo.getEmployer());
        dietTV.setText(matchInfo.getDiet());
        smokeTV.setText(matchInfo.getSmoking());
        drinkTV.setText(matchInfo.getDrinking());
        yearsInUSATV.setText(matchInfo.getYearsInUS());
        legalStatusTV.setText(matchInfo.getLegalStatus());
        aboutMeTV.setText("About me: " + matchInfo.getAboutMe());

        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        JSONArray jsonArray = matchInfo.getProfileImage();
        pager_items = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                pager_items[i] = jsonArray.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        madapter_viewPager = new ViewPagerAdapter(RematchActivity.this.getApplicationContext(), pager_items);
        mpager.setAdapter(madapter_viewPager);

        mIndicator.setViewPager(mpager);

        rematchBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (rematchBTN.getText().toString().equalsIgnoreCase("")) {
                    Drawable img = getResources().getDrawable(R.drawable.img_gold_action_item);
                    img.setBounds(0, 0, 70, 70);
                    rematchBTN.setCompoundDrawables(img, null, null, null);
                    rematchBTN.setBackgroundResource(R.drawable.btn_pool_conn_s2);
                    rematchBTN.setText("" + goldUsage);
                } else {
                    goldUsage = Integer.parseInt(rematchBTN.getText().toString());
//                    Toast.makeText(RematchActivity.this, rematchBTN.getText().toString(), Toast.LENGTH_SHORT).show();
                    if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                        new AccessRematchProfile().execute();
                    } else {
                        Toast.makeText(RematchActivity.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    }
//                    rematchBTN.setBackgroundResource(0);
                }
            }
        });

        mpager.setOnItemClickListener(new ClickableViewPager.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(RematchActivity.this, ImageZoomActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("position", position);
                EventBus.getDefault().postSticky(matchInfo);
                intent.putExtra("pager_items", pager_items);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
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

    public class AccessRematchProfile extends AsyncTask<String, String, String> {

        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(RematchActivity.this, R.style.progress_dialog);
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

            parms.add(new BasicNameValuePair("userid1", AppData.getString(RematchActivity.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair("userid2", matchInfo.getProfileUserId()));
            parms.add(new BasicNameValuePair(BureauConstants.goldToConsume, "" + goldUsage));

            String paramString = URLEncodedUtils.format(parms, "utf-8");

            String url = BureauConstants.BASE_URL + BureauConstants.VIEW_POOL_ACCESS;
            url += "?";
            url += paramString;

            Log.e("Bureau", "Access Rematch url " + url);
            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.ACCESS_REMATCH, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
//            Log.e("Bureau", "Access Rematch details " + result);
            try {

                JSONObject jsonObject = new JSONObject(result);

                final Dialog dialog = new Dialog(RematchActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.simple_alert);

                String responseStatus = jsonObject.getString("msg");
                if (responseStatus.equalsIgnoreCase("Error")) {
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        Util.invalidateUserID(RematchActivity.this, RematchActivity.this);
                    }
                } else {
                    if (responseStatus.equalsIgnoreCase("Success")) {
                        ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText(responseStatus);
                        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText("Good Luck ! Your match has been notified.");
                    } else {
                        ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText(responseStatus);
                        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText("Sorry we encountered a problem, please try again.");
                    }
                    // set the custom dialog components - text, image and button
                    TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                    text.setText("Ok");

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog.show();
                }

            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private class ReadUserProfileDetails extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(RematchActivity.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, userId));

            String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = BureauConstants.BASE_URL + BureauConstants.READ_PROFILE_DETAILS_URL;
            url += "?";
            url += paramString;
            return new ConnectBureau().getDataFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String profileStatus = AppData.getString(RematchActivity.this, BureauConstants.profileStatus);

                if (jsonObject.has("msg")) {
                    String response = jsonObject.getString("response");
                    //redirect user to login page
                } else {
                    MatchInfo matchInfo = new MatchInfo(jsonObject);
                    uiUpdateWithValues(matchInfo);
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}