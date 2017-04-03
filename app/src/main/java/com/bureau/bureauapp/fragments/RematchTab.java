package com.bureau.bureauapp.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.RematchActivity;
import com.bureau.bureauapp.adapters.DataAdapter;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.helperclasses.GetAvailableGold;
import com.bureau.bureauapp.model.MatchInfo;
import com.bureau.bureauapp.helperclasses.RecyclerItemClickListener;
import com.bureau.bureauapp.myapplication.AppData;

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

public class RematchTab extends Fragment implements RecyclerItemClickListener.OnItemClickListener {
    private final String LOG_TAG = RematchTab.class.getSimpleName();
    ArrayList<MatchInfo> matchInfos;
    TextView errorMsgTV;
    RecyclerView recyclerView;
    int goldUsage = 0;
    String mUserId;
    Context mContext;
    Activity activity;

    public RematchTab() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.rematch_fragment, container, false);

        mUserId = AppData.getString(getActivity(), BureauConstants.userid);
        mContext = this.getActivity();
        activity = getActivity();
        matchInfos = new ArrayList<>();
        errorMsgTV = (TextView) rootView.findViewById(R.id.errorMsgTV);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.card_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ConnectBureau.isNetworkAvailable(getActivity())) {
            new ReadRematchProfiles().execute();
            new GetAvailableGold(getActivity());
            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), this));
        } else {
            errorMsgTV.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            errorMsgTV.setText(getString(R.string.no_network));
        }
    }

    @Override
    public void onItemClick(View childView, int position) {

        if (matchInfos.size() > 0) {
            Intent intent = new Intent(getActivity(), RematchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            EventBus.getDefault().postSticky(matchInfos.get(position));
            intent.putExtra("goldUsage", goldUsage);
            intent.putExtra("fromActivity", "rematch");
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public void onItemLongPress(View childView, int position) {

    }

    private class ReadRematchProfiles extends AsyncTask<Void, Void, String> {
//        Dialog dialog;

        @Override
        protected void onPreExecute() {
            /*dialog = new Dialog(getActivity(),R.style.progress_dialog);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();*/
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, mUserId));
            String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = BureauConstants.BASE_URL + BureauConstants.VIEW_REMATCH;
            url += "?";
            url += paramString;

            Log.d("Bureau", "Rematch url " + url);
            return new ConnectBureau().getDataFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            /*if(dialog!=null)
                dialog.dismiss();*/

            Log.e("Bureau", "Get Rematch details " + result);
            matchInfos.clear();
            try {

                if (result.length() > 0) {
                    JSONObject jsonObjectMain = new JSONObject(result);
                    if (jsonObjectMain.has("msg")) {
                        if (jsonObjectMain.getString("msg").equalsIgnoreCase("Error")) {
                            errorMsgTV.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            errorMsgTV.setText(jsonObjectMain.getString("response"));
                            if (jsonObjectMain.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                                com.bureau.bureauapp.util.Util.invalidateUserID(mContext, activity);
                            }
                        }
                    } else {
                        errorMsgTV.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        JSONArray jsonArray = jsonObjectMain.getJSONArray("rematch_profiles");
                        Log.e("Bureau", "jsonArray length " + jsonArray.length());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            MatchInfo matchInfo = new MatchInfo(jsonObject);
                            matchInfos.add(matchInfo);
                        }

                        JSONObject jsonObject = jsonObjectMain.getJSONObject("goldUsage");
                        goldUsage = jsonObject.getInt("rematch");

                        DataAdapter adapter = new DataAdapter(mContext, matchInfos);
                        recyclerView.setAdapter(adapter);
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}
