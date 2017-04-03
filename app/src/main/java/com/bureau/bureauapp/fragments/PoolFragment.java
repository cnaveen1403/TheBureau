package com.bureau.bureauapp.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pools;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bureau.bureauapp.Details_Activity;
import com.bureau.bureauapp.R;
import com.bureau.bureauapp.adapters.ConversationsRecyclerView;
import com.bureau.bureauapp.adapters.ViewPagerAdapter;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.CirclePageIndicator;
import com.bureau.bureauapp.helperclasses.ClickableViewPager;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.helperclasses.GetAvailableGold;
import com.bureau.bureauapp.model.MatchInfo;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.myapplication.MyApplication;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class PoolFragment extends Fragment {
    private final String LOG_TAG = Pools.Pool.class.getSimpleName();

    RelativeLayout tapToViewProfile, poolRL;
    ClickableViewPager mpager;
    ViewPagerAdapter madapter_viewPager;
    CirclePageIndicator mIndicator;
    String pager_items[];
    TextView errorMsgTV;
    int directCost = 0, anonymousCost = 0;
    String freeProfile = "No";
    Activity activity;
    private static Context mContext;

    ArrayList<MatchInfo> matchInfos = new ArrayList<>();
    View rootView;

    public PoolFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.pool_fragment, container, false);
        tapToViewProfile = (RelativeLayout) rootView.findViewById(R.id.skipLL);
        poolRL = (RelativeLayout) rootView.findViewById(R.id.poolRL);
        errorMsgTV = (TextView) rootView.findViewById(R.id.errorMsgTV);
        mpager = (ClickableViewPager) rootView.findViewById(R.id.pool_pager);

        mContext = getActivity();

        if (ConnectBureau.isNetworkAvailable(getActivity())) {
            new ReadUserPoolDetails().execute();
        } else {
            errorMsgTV.setText(getString(R.string.no_network));
        }

        activity = getActivity();
        mIndicator = (CirclePageIndicator) rootView.findViewById(R.id.indicator);

        tapToViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (matchInfos.size() > 0) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), Details_Activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    EventBus.getDefault().postSticky(matchInfos.get(mpager.getCurrentItem()));
                    intent.putExtra("directCost", directCost);
                    intent.putExtra("anonymousCost", anonymousCost);
                    intent.putExtra("freeProfile", freeProfile);
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        ConversationsRecyclerView conversationsList = (ConversationsRecyclerView) rootView.findViewById(R.id.conversations_list);
        conversationsList.init(MyApplication.getLayerClient(), MyApplication.getParticipantProvider(), MyApplication.getPicasso())
                .setInitialHistoricMessagesToFetch(10);

        if (ConnectBureau.isNetworkAvailable(getActivity())) {
            new GetAvailableGold(getActivity());
        } else {
            errorMsgTV.setText(getString(R.string.no_network));
        }
    }

    private class ReadUserPoolDetails extends AsyncTask<Void, Void, String> {
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

            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(mContext, BureauConstants.userid)));

            String paramString = URLEncodedUtils.format(parms, "utf-8");

            String url = BureauConstants.BASE_URL + BureauConstants.VIEW_POOL_TAB;
            url += "?";
            url += paramString;

            Log.d("Bureau", "match url " + url);

            return new ConnectBureau().getDataFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            matchInfos.clear();
            try {
                if (result.length() > 0) {
                    JSONObject jsonObjectMain = new JSONObject(result);
                    if (jsonObjectMain.has("msg")) {
                        if (jsonObjectMain.getString("msg").equalsIgnoreCase("Error")) {
                            errorMsgTV.setVisibility(View.VISIBLE);
                            poolRL.setVisibility(View.GONE);
                            errorMsgTV.setText(jsonObjectMain.getString("response"));
                            if (jsonObjectMain.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                                com.bureau.bureauapp.util.Util.invalidateUserID(getActivity(), activity);
                            }
                        }
                    } else {
                        tapToViewProfile.setVisibility(View.VISIBLE);
                        errorMsgTV.setVisibility(View.GONE);
                        poolRL.setVisibility(View.VISIBLE);
                        JSONArray jsonArray = jsonObjectMain.getJSONArray("pool");
                        Log.e("Bureau", "jsonArray length " + jsonArray.length());
                        pager_items = new String[jsonArray.length()];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            MatchInfo matchInfo = new MatchInfo(jsonObject);
                            matchInfos.add(matchInfo);
                            String str_images = jsonObject.getString("img_url");
                            str_images = str_images.replace("[", "");
                            str_images = str_images.replace("]", "");
                            String[] pager_item = str_images.split(",");
                            if (pager_item.length > 0) {
                                pager_items[i] = pager_item[0];
                            }
                        }

                        JSONObject jsonObjectGold = jsonObjectMain.getJSONObject("gold_values");
                        directCost = jsonObjectGold.getInt("direct");
                        anonymousCost = jsonObjectGold.getInt("anonymous");
                        freeProfile = jsonObjectGold.getString("free_profile");

                        madapter_viewPager = new ViewPagerAdapter(mContext, pager_items);
                        mpager.setAdapter(madapter_viewPager);

                        mIndicator.setViewPager(mpager);

                        if (jsonArray.length() > 0) {
                            poolRL.setVisibility(View.VISIBLE);
                        } else {
                            poolRL.setVisibility(View.GONE);
                        }

                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}