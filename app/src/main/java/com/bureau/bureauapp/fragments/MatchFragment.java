package com.bureau.bureauapp.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bureau.bureauapp.helperclasses.ImageZoomActivity;
import com.bureau.bureauapp.R;
import com.bureau.bureauapp.adapters.ConversationsRecyclerView;
import com.bureau.bureauapp.adapters.ViewPagerAdapter;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.CirclePageIndicator;
import com.bureau.bureauapp.helperclasses.ClickableViewPager;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.myapplication.MyApplication;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class MatchFragment extends Fragment {

    private final String LOG_TAG = MatchFragment.class.getSimpleName();

    Context mContext;
    Activity activity;
    String mUserid;
    RelativeLayout matchProfileRL, passProfileRl;
    LinearLayout matchPassLL, ll_dob, ll_tob, ll_lob, ll_years_in_usa, ll_legal_status;
    TextView likedPassedTV, errorMsgTV;
    ClickableViewPager mpager;
    ViewPagerAdapter madapter_viewPager;
    CirclePageIndicator mIndicator;
    String pager_items[];
    String userid_passed, action_taken;
    ScrollView scrollView;
    TextView createdByTV, nameTV, ageTV, locationTV, heightTV, motherTongueTV, religionTV, familyOriginTV, specificationTV, educationTV, honorsTV, majorTV, schoolTV, yearTV,
            occupationTV, employerTV, dietTV, smokeTV, drinkTV, yearsInUSATV, legalStatusTV, dateOfBirthTV, timeOfBirthTV, locationOfBirthTV, aboutMeTV;
    View rootView;

    public MatchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.match_fragment, container, false);

        mContext = this.getActivity();
        activity = getActivity();
        matchProfileRL = (RelativeLayout) rootView.findViewById(R.id.matchRL);
        passProfileRl = (RelativeLayout) rootView.findViewById(R.id.passRL);
        matchPassLL = (LinearLayout) rootView.findViewById(R.id.matchPassLL);
        ll_dob = (LinearLayout) rootView.findViewById(R.id.ll_dob);
        ll_tob = (LinearLayout) rootView.findViewById(R.id.ll_tob);
        ll_lob = (LinearLayout) rootView.findViewById(R.id.ll_lob);
        likedPassedTV = (TextView) rootView.findViewById(R.id.likedPassedTV);
        scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);
        errorMsgTV = (TextView) rootView.findViewById(R.id.errorMsgTV);
        createdByTV = (TextView) rootView.findViewById(R.id.createdByTV);
        nameTV = (TextView) rootView.findViewById(R.id.nameTV);
        ageTV = (TextView) rootView.findViewById(R.id.ageTV);
        locationTV = (TextView) rootView.findViewById(R.id.locationTV);
        heightTV = (TextView) rootView.findViewById(R.id.heightTV);
        motherTongueTV = (TextView) rootView.findViewById(R.id.motherToungueTV);
        religionTV = (TextView) rootView.findViewById(R.id.religionTV);
        familyOriginTV = (TextView) rootView.findViewById(R.id.familyOriginTV);
        specificationTV = (TextView) rootView.findViewById(R.id.specificationTV);
        educationTV = (TextView) rootView.findViewById(R.id.educationTV);
        honorsTV = (TextView) rootView.findViewById(R.id.honorsTV);
        majorTV = (TextView) rootView.findViewById(R.id.majorTV);
        schoolTV = (TextView) rootView.findViewById(R.id.schoolTV);
        yearTV = (TextView) rootView.findViewById(R.id.yearTV);
        occupationTV = (TextView) rootView.findViewById(R.id.occupationTV);
        employerTV = (TextView) rootView.findViewById(R.id.employerTV);
        dietTV = (TextView) rootView.findViewById(R.id.dietTV);
        smokeTV = (TextView) rootView.findViewById(R.id.smokeTV);
        drinkTV = (TextView) rootView.findViewById(R.id.drinkTV);
        yearsInUSATV = (TextView) rootView.findViewById(R.id.yearsInUSATV);
        legalStatusTV = (TextView) rootView.findViewById(R.id.legalStatusTV);
        dateOfBirthTV = (TextView) rootView.findViewById(R.id.dateOfBirthTV);
        timeOfBirthTV = (TextView) rootView.findViewById(R.id.timeOfBirthTV);
        locationOfBirthTV = (TextView) rootView.findViewById(R.id.locationOfBirthTV);
        aboutMeTV = (TextView) rootView.findViewById(R.id.aboutMeTV);
        mpager = (ClickableViewPager) rootView.findViewById(R.id.pager);
        mIndicator = (CirclePageIndicator) rootView.findViewById(R.id.indicator);
        ll_years_in_usa = (LinearLayout) rootView.findViewById(R.id.ll_years_in_usa);
        ll_legal_status = (LinearLayout) rootView.findViewById(R.id.ll_legal_status);

        //Set User Id here as below line produces null in AsyncTask
        mUserid = AppData.getString(getActivity(), BureauConstants.userid);

        //Get Match Of the Day from the Bureau Server
        if (ConnectBureau.isNetworkAvailable(getActivity())) {
            new GetMatchOfTheDay().execute();
        } else {
            errorMsgTV.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
            errorMsgTV.setText(getString(R.string.no_network));
        }

        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, r.getDisplayMetrics());
        LinearLayout.LayoutParams layout_description = new LinearLayout.LayoutParams(width,
                height - ((int) px * 2));

        rootView.findViewById(R.id.viewpagerRL).setLayoutParams(layout_description);

        matchProfileRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action_taken = "Liked";
                if (ConnectBureau.isNetworkAvailable(getActivity())) {
                    new PassLikeAsync().execute();
                } else {
                    Toast.makeText(getParentFragment().getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
        });

        passProfileRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action_taken = "Passed";
                if (ConnectBureau.isNetworkAvailable(getActivity())) {
                    new PassLikeAsync().execute();
                } else {
                    Toast.makeText(getParentFragment().getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
        });

        mpager.setOnItemClickListener(new ClickableViewPager.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                position = mpager.getCurrentItem();
                Intent intent = new Intent(getActivity(), ImageZoomActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("position", position);
                intent.putExtra("pager_items", pager_items);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        ConversationsRecyclerView conversationsList = (ConversationsRecyclerView) rootView.findViewById(R.id.conversations_list);
        conversationsList.init(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), MyApplication.getPicasso())
                .setInitialHistoricMessagesToFetch(10);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    void showDialog(Context context, String title, String message, String buttonText) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.simple_alert);
        ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText(title);
        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(message);
        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
        text.setText(buttonText);

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (ConnectBureau.isNetworkAvailable(getActivity())) {
                    new GetMatchOfTheDay().execute();
                } else {
                    Toast.makeText(getParentFragment().getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.show();
    }

    public class GetMatchOfTheDay extends AsyncTask<Void, Void, String> {
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new Dialog(getActivity(), R.style.progress_dialog);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, mUserid));

            String paramString = URLEncodedUtils.format(parms, "utf-8");

            String url = BureauConstants.BASE_URL + BureauConstants.VIEW_MATCH_TAB;
            url += "?";
            url += paramString;
            return new ConnectBureau().getDataFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            Log.e("Bureau", "Get match details " + result);

            try {
                if (result.length() > 0) {
                    if (result.substring(0, 1).equalsIgnoreCase("{")) {
                        JSONObject jsonObjectMain = new JSONObject(result);
                        if (jsonObjectMain.has("msg")) {
                            if (jsonObjectMain.getString("msg").equalsIgnoreCase("Error")) {
                                if (jsonObjectMain.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                                    com.bureau.bureauapp.util.Util.invalidateUserID(mContext, activity);
                                }

                                errorMsgTV.setVisibility(View.VISIBLE);
                                scrollView.setVisibility(View.GONE);
                                errorMsgTV.setText(jsonObjectMain.getString("response"));
                            }
                        }
                    } else {
                        errorMsgTV.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                        JSONArray jsonArray = new JSONArray(result);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String userAction = jsonObject.getString(BureauConstants.USER_ACTION);
                            String employmentStatus = jsonObject.getString(BureauConstants.employmentStatus);

                            userid_passed = jsonObject.getString(BureauConstants.userid);
                            if (userAction.equalsIgnoreCase("Liked") || userAction.equalsIgnoreCase("Passed") || userAction.equalsIgnoreCase("")) {
                                nameTV.setText("Private Until Connected");
                                ll_dob.setVisibility(View.GONE);
                                ll_tob.setVisibility(View.GONE);
                                ll_lob.setVisibility(View.GONE);
                            } else {
                                nameTV.setText(jsonObject.getString("first_name"));// + " " + jsonObject.getString("last_name"));
                                ll_dob.setVisibility(View.VISIBLE);
                                ll_tob.setVisibility(View.VISIBLE);
                                ll_lob.setVisibility(View.VISIBLE);
                                dateOfBirthTV.setText(jsonObject.getString(BureauConstants.horoscopeDob));
                                timeOfBirthTV.setText(jsonObject.getString(BureauConstants.horoscopeTob));
                                locationOfBirthTV.setText(jsonObject.getString(BureauConstants.horoscopeLob));
                            }

                            createdByTV.setText("Created by : " + jsonObject.getString(BureauConstants.createdBy));
                            ageTV.setText(jsonObject.getString(BureauConstants.age));
                            locationTV.setText(jsonObject.getString(BureauConstants.location));
                            heightTV.setText(jsonObject.getString(BureauConstants.heightFeet) + "'" + jsonObject.getString(BureauConstants.heightInch) + "\"");
                            motherTongueTV.setText(jsonObject.getString(BureauConstants.motherTongue));
                            religionTV.setText(jsonObject.getString(BureauConstants.religionName));
                            familyOriginTV.setText(jsonObject.getString(BureauConstants.familyOriginName));
                            specificationTV.setText(jsonObject.getString(BureauConstants.specificationName));
                            educationTV.setText(jsonObject.getString(BureauConstants.highestEducation));
                            honorsTV.setText(jsonObject.getString(BureauConstants.honors));
                            majorTV.setText(jsonObject.getString(BureauConstants.major));
                            schoolTV.setText(jsonObject.getString(BureauConstants.college));
                            yearTV.setText(jsonObject.getString(BureauConstants.graduatedYear));

                            if (employmentStatus.equalsIgnoreCase(BureauConstants.employed)) {
                                employerTV.setText(jsonObject.getString(BureauConstants.positionTitle));
                            } else if (employmentStatus.equalsIgnoreCase(BureauConstants.unemployed)) {
                                occupationTV.setText(jsonObject.getString(employmentStatus));
                            } else if (employmentStatus.equalsIgnoreCase(BureauConstants.student)) {

                            } else if (employmentStatus.equalsIgnoreCase(BureauConstants.other)) {

                            }

                            dietTV.setText(jsonObject.getString(BureauConstants.diet));
                            smokeTV.setText(jsonObject.getString(BureauConstants.smoking));
                            drinkTV.setText(jsonObject.getString(BureauConstants.drinking));

                            String residingCountry = jsonObject.getString(BureauConstants.countryResiding);
                            if (residingCountry.equals(BureauConstants.INDIA)) {
                                ll_years_in_usa.setVisibility(View.GONE);
                                ll_legal_status.setVisibility(View.GONE);
                            } else {
                                ll_years_in_usa.setVisibility(View.VISIBLE);
                                ll_legal_status.setVisibility(View.VISIBLE);
                                yearsInUSATV.setText(jsonObject.getString(BureauConstants.yearsInUsa));
                                legalStatusTV.setText(jsonObject.getString(BureauConstants.legalStatus));
                            }

                            aboutMeTV.setText("About me: " + jsonObject.getString(BureauConstants.aboutMe));


                            if (userAction.equalsIgnoreCase("Liked")) {
                                matchPassLL.setVisibility(View.GONE);
                                likedPassedTV.setVisibility(View.VISIBLE);
                                likedPassedTV.setBackgroundResource(R.drawable.btn_liked);
                            }

                            if (userAction.equalsIgnoreCase("Passed")) {
                                matchPassLL.setVisibility(View.GONE);
                                likedPassedTV.setVisibility(View.VISIBLE);
                                likedPassedTV.setBackgroundResource(R.drawable.btn_passed);
                            }

                            if (userAction.equalsIgnoreCase("") || userAction.equalsIgnoreCase(null)) {
                                matchPassLL.setVisibility(View.VISIBLE);
                                likedPassedTV.setVisibility(View.GONE);
                                likedPassedTV.setVisibility(View.GONE);
                            }

                            String str_images = jsonObject.getString(BureauConstants.imgUrl);
                            str_images = str_images.replace("[", "");
                            str_images = str_images.replace("]", "");
                            pager_items = str_images.split(",");

                            madapter_viewPager = new ViewPagerAdapter(mContext, pager_items);
                            mpager.setAdapter(madapter_viewPager);

                            mIndicator.setViewPager(mpager);
                        }
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private class PassLikeAsync extends AsyncTask<String, String, String> {
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new Dialog(getActivity(), R.style.progress_dialog);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.PASS_BY, AppData.getString(getActivity(), BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.USERID_PASSED, userid_passed));
            parms.add(new BasicNameValuePair(BureauConstants.ACTION_TOKEN, action_taken));

            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.LIKE_PASS_MATCH, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getString("show_dialog").equalsIgnoreCase("yes")) {
                    if (action_taken.equalsIgnoreCase("Liked")) {
                        showDialog(getActivity(), "Hooray", jsonObject.getString("response"), "Ok");
                    } else {
                        showDialog(getActivity(), "Sorry to hear it!", jsonObject.getString("response"), "Ok");
                    }
                } else {
                    if (ConnectBureau.isNetworkAvailable(getActivity())) {
                        new GetMatchOfTheDay().execute();
                    } else {
                        Toast.makeText(getParentFragment().getActivity(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}