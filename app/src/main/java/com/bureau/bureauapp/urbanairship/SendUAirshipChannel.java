package com.bureau.bureauapp.urbanairship;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.myapplication.AppData;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.LinkedList;
import java.util.List;

public class SendUAirshipChannel {
    Context mContext;

    public SendUAirshipChannel(Context c, String channelId) {
        this.mContext = c;
        new SendUAirshipAsync().execute(channelId);
    }

    public class SendUAirshipAsync extends AsyncTask<String, Void, String> {
        private String LOG_TAG = SendUAirshipChannel.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            String channelId = params[0];
            List<NameValuePair> parms = new LinkedList<NameValuePair>();

            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(mContext, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.DEVICEID, channelId));
            parms.add(new BasicNameValuePair("device_type", "android"));

            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.REGISTER_CHANNEL_ID, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("Bureau", "Update Channel" + result);
        }
    }
}