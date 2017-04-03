package com.bureau.bureauapp.helperclasses;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ConnectBureau {
    private final static String LOG_TAG = ConnectBureau.class.getSimpleName();
    static InputStream is = null;
    static String json = "";

    public String getDataFromUrl(String url, List<NameValuePair> parms) {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            httpPost.setHeader("x-api-key", BureauConstants.x_api_key);
            httpPost.setEntity(new UrlEncodedFormEntity(parms));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch (Exception e) {
            // TODO: handle exception
        }
        //read data from url
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.e("PMC", "WEb call res " + json);

        } catch (Exception e) {
            // TODO: handle exception
            Log.e("PMC", "WEb call res exception " + e.getLocalizedMessage());
        }

        return json;
    }

    public String getDataFromUrl(String url) {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();

            Log.e("PMC", "WEb call url " + url);

            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("x-api-key", BureauConstants.x_api_key);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(LOG_TAG, "e.toString() >>>> " + e.toString());
            json = "{\"msg\":\"Error\",\"response\":\"Check your internet connection and try again.\",\"preference_set\":true}";
        }
        //read data from url
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.e("PMC", "WEb call res " + json);

        } catch (Exception e) {
            Log.e(LOG_TAG, "e.toString() two >>>> " + e.toString());
            json = "{\"msg\":\"Error\",\"response\":\"Check your internet connection and try again.\",\"preference_set\":true}";
            // TODO: handle exception
        }

        return json;
    }

    /*
    * Returns true if the network is available or about to become available.
    *
    * @param c Context used to get the ConnectivityManager
    * @return true if the network is available
    * */
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}




