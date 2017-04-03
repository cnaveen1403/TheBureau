package com.bureau.bureauapp.helperclasses;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.bureau.bureauapp.HomeActivity;
import com.bureau.bureauapp.fragments.GoldStoreFragment;
import com.bureau.bureauapp.myapplication.AppData;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class GetAvailableGold {
    private static final String LOG_TAG = GetAvailableGold.class.getSimpleName();
    Context mContext;

    public GetAvailableGold(Context c) {
        this.mContext = c;
        new GetGoldValueAsync().execute();
    }

    public class GetGoldValueAsync extends AsyncTask<String, Void, String> {
        private String LOG_TAG = GetGoldValueAsync.class.getSimpleName();
        String goldValue;

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> parms = new LinkedList<NameValuePair>();

            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(mContext, BureauConstants.userid)));

            String paramString = URLEncodedUtils.format(parms, "utf-8");

            String url = BureauConstants.BASE_URL + BureauConstants.GET_AVAILABLE_GOLD;
            url += "?";
            url += paramString;

            return new ConnectBureau().getDataFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
//            Log.d("Bureau", "Get Available Gold : " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("msg")) {
                    String response = jsonObject.getString("response");
                    AlertDialog alertDialog = new AlertDialog.Builder(
                            mContext).create();

                    alertDialog.setTitle("Error");
                    alertDialog.setMessage(response);
                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Write your code here to execute after dialog closed
                        }
                    });
                    // Showing Alert Message
                    alertDialog.show();
                } else {
                    goldValue = jsonObject.getString(BureauConstants.goldAvailable);
                    HomeActivity.tvGoldAvailable.setText(goldValue);
                    AppData.saveString(mContext, BureauConstants.goldAvailable, goldValue);

                    if (GoldStoreFragment.tvTotalGold != null) {
                        GoldStoreFragment.tvTotalGold.setText(goldValue);
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}
