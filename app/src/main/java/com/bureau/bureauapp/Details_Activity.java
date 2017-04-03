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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bureau.bureauapp.adapters.HorizantalListAdapter;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.helperclasses.HorizontalListView;
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

public class Details_Activity extends Activity {

    private static final String LOG_TAG = Details_Activity.class.getSimpleName();
    Context mContext;
    String pager_items[];
    HorizontalListView horizontalScrollView;
    MatchInfo matchInfo = null;
    TextView profileCreatedByTV, ageTV, locationTV, heightTV, religionTV, familyOriginTV, educationTV, occupationTV, tvGoldValue;
    Button anonymousBTN, directBTN;
    int directCost = 0, anonymousCost = 0;
    String freeProfile = "No", goldAmount, accessType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_activty);
        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        mContext = Details_Activity.this;

        directCost = getIntent().getExtras().getInt("directCost", 0);
        anonymousCost = getIntent().getExtras().getInt("anonymousCost", 0);
        freeProfile = getIntent().getExtras().getString("freeProfile", "No");

        anonymousBTN = (Button) findViewById(R.id.anonymousBTN);
        directBTN = (Button) findViewById(R.id.directBTN);
        profileCreatedByTV = (TextView) findViewById(R.id.profileCreatedByTV);
        ageTV = (TextView) findViewById(R.id.ageTV);
        locationTV = (TextView) findViewById(R.id.locationTV);
        heightTV = (TextView) findViewById(R.id.heightTV);
        religionTV = (TextView) findViewById(R.id.religionTV);
        familyOriginTV = (TextView) findViewById(R.id.familyOriginTV);
        educationTV = (TextView) findViewById(R.id.educationTV);
        occupationTV = (TextView) findViewById(R.id.occupationTV);
        tvGoldValue = (TextView) findViewById(R.id.tv_gold_pool_detail);

        matchInfo = EventBus.getDefault().removeStickyEvent(MatchInfo.class);

        tvGoldValue.setText(AppData.getString(getApplicationContext(), BureauConstants.goldAvailable));

        if (matchInfo != null) {
            profileCreatedByTV.setText(matchInfo.getProfileCreatedBy());
            ageTV.setText(matchInfo.getAge());
            locationTV.setText(matchInfo.getLocation());
            heightTV.setText(matchInfo.getHeight_feet() + "\'" + matchInfo.getHeight_inch() + "\"");
            religionTV.setText(matchInfo.getReligion());
            familyOriginTV.setText(matchInfo.getFamilyOrigin());
            educationTV.setText(matchInfo.getEducation());
            occupationTV.setText(matchInfo.getOccupation());
            JSONArray jsonArray = matchInfo.getProfileImage();
            pager_items = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    pager_items[i] = jsonArray.getString(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        horizontalScrollView = (HorizontalListView) findViewById(R.id.userImagesHorizantalList);
        horizontalScrollView.setAdapter(new HorizantalListAdapter(Details_Activity.this, pager_items));

        horizontalScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Details_Activity.this, ImageZoomActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("position", position);
                EventBus.getDefault().postSticky(matchInfo);
                intent.putExtra("pager_items", pager_items);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        directBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (directBTN.getText().toString().equalsIgnoreCase("Direct")) {
                    Drawable img = getResources().getDrawable(R.drawable.img_gold_action_item);
                    img.setBounds(0, 0, 70, 70);
                    directBTN.setCompoundDrawables(img, null, null, null);
                    if (freeProfile.equalsIgnoreCase("Yes")) {
                        directBTN.setText("Free");
                        goldAmount = "0";
                    } else {
                        directBTN.setText("" + directCost);
                        goldAmount = "" + directCost;
                    }
                    anonymousBTN.setText("Anonymous");
                    anonymousBTN.setCompoundDrawables(null, null, null, null);
                } else {
                    accessType = "Direct";
//                    Toast.makeText(Details_Activity.this, directBTN.getText().toString(), Toast.LENGTH_SHORT).show();
                    getPoolProfile();
                }
            }
        });

        anonymousBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (anonymousBTN.getText().toString().equalsIgnoreCase("Anonymous")) {

                    Drawable img = getResources().getDrawable(R.drawable.img_gold_action_item);
                    img.setBounds(0, 0, 70, 70);
                    anonymousBTN.setCompoundDrawables(img, null, null, null);
                    if (freeProfile.equalsIgnoreCase("Yes")) {
                        anonymousBTN.setText("Free");
                        goldAmount = "0";
                    } else {
                        anonymousBTN.setText("" + anonymousCost);
                        goldAmount = "" + anonymousCost;
                    }
                    directBTN.setText("Direct");
                    directBTN.setCompoundDrawables(null, null, null, null);
                } else {
                    accessType = "Anonymous";
//                    Toast.makeText(Details_Activity.this, anonymousBTN.getText().toString(), Toast.LENGTH_SHORT).show();
                    getPoolProfile();
                }
            }
        });
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

    private void getPoolProfile() {
        if (ConnectBureau.isNetworkAvailable(mContext)) {
            new AccessPoolProfile().execute();
        } else {
            Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }
    }

    public class AccessPoolProfile extends AsyncTask<String, String, String> {

        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(Details_Activity.this, R.style.progress_dialog);
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

            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(Details_Activity.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.visitedUserid, matchInfo.getProfileUserId()));
            parms.add(new BasicNameValuePair(BureauConstants.accessType, accessType));
            parms.add(new BasicNameValuePair(BureauConstants.goldToConsume, goldAmount));

            String paramString = URLEncodedUtils.format(parms, "utf-8");

            String url = BureauConstants.BASE_URL + BureauConstants.VIEW_POOL_ACCESS;
            url += "?";
            url += paramString;

            Log.e("Bureau", "Access pool url " + url);
            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.VIEW_POOL_ACCESS, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Log.e("Bureau", "Access pool details " + result);
            try {

                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject != null) {
                    if (jsonObject.getString("msg").equalsIgnoreCase("Error")) {
                        if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                            Util.invalidateUserID(Details_Activity.this, Details_Activity.this);
                        }
                    } else {
                        final Dialog dialog = new Dialog(Details_Activity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.simple_alert);
                        ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText(jsonObject.getString("msg"));
                        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(jsonObject.getString("response"));
                        // set the custom dialog components - text, image and button
                        TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                        text.setText("Ok");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                Intent intent = new Intent(Details_Activity.this, HomeActivity.class);
                                intent.putExtra("pager_position", 1);

                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
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
}
