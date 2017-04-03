package com.bureau.bureauapp.profilesetup;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.helperclasses.FamilyOriginAdapter;
import com.bureau.bureauapp.helperclasses.FamilyOriginInfo;
import com.bureau.bureauapp.helperclasses.MotherToungueAdapter;
import com.bureau.bureauapp.helperclasses.MotherToungueInfo;
import com.bureau.bureauapp.helperclasses.ReligionAdapter;
import com.bureau.bureauapp.helperclasses.ReligionInfo;
import com.bureau.bureauapp.helperclasses.SpecificationAdapter;
import com.bureau.bureauapp.helperclasses.SpecificationInfo;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.profileedit.HeritageEdit;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileHeritage extends AppCompatActivity {

    private static String LOG_TAG = ProfileInfo.class.getSimpleName();

    private String sReligionId, sMotherToungueId, sFamilyOriginId, sSpecificationId, familyOriginResult, specificationResult;

    InputMethodManager imm;
    EditText edittext_Religion, edittext_MotherToungue, edittext_FamilyOrigin, edittext_Specification, edittext_Gothra;
    TextView textView_Religion, textView_MotherToungue, textView_FamilyOrigin;
    Button btnContinue;

    Dialog religionDialog, motherTongueDialog, familyOriginDialog, specificationDialog;

    ListView religionListView, motherTongueListView, familyOriginListView, specificationListView;

    private ReligionAdapter religionAdapter;
    private MotherToungueAdapter motherToungueAdapter;
    private FamilyOriginAdapter familyOriginAdapter;
    private SpecificationAdapter specificationAdapter;

    private ArrayList<ReligionInfo> religionInfoList;
    private ArrayList<MotherToungueInfo> motherToungueInfoList;
    private ArrayList<FamilyOriginInfo> familyOriginInfoList;
    private ArrayList<SpecificationInfo> specificationInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_heritage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        init();
    }

    private void init() {
//        myApplication = (MyApplication) getApplication();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        religionInfoList = new ArrayList<>();
        motherToungueInfoList = new ArrayList<>();

        edittext_Religion = (EditText) findViewById(R.id.editText_Religion);
        edittext_MotherToungue = (EditText) findViewById(R.id.editText_MotherToungue);
        edittext_FamilyOrigin = (EditText) findViewById(R.id.editText_FamilyOrigin);
        edittext_Specification = (EditText) findViewById(R.id.editText_Specification);
        edittext_Gothra = (EditText) findViewById(R.id.editText_Gothra);
        textView_Religion = (TextView) findViewById(R.id.Religion_tv);
        textView_MotherToungue = (TextView) findViewById(R.id.MotherToungue_tv);
        textView_FamilyOrigin = (TextView) findViewById(R.id.FamilyOrigin_tv);
        btnContinue = (Button) findViewById(R.id.btn_SaveHeritage);

        String strReligion = getString(R.string.txtReligion) + "<font color='red'>*</font>";
        textView_Religion.setText(Html.fromHtml(strReligion), TextView.BufferType.SPANNABLE);

        String strMotherToungue = getString(R.string.txtMotherToungue) + "<font color='red'>*</font>";
        textView_MotherToungue.setText(Html.fromHtml(strMotherToungue), TextView.BufferType.SPANNABLE);

        String strFamilyOrigin = getString(R.string.txtFamilyOrigin) + "<font color='red'>*</font>";
        textView_FamilyOrigin.setText(Html.fromHtml(strFamilyOrigin), TextView.BufferType.SPANNABLE);

        TextView tv_title = (TextView) findViewById(R.id.toolbar_Profile_heritage_title);
        String profileName = AppData.getString(ProfileHeritage.this, "profile_first_name");
        String txtTitle = profileName + "'s Heritage";
        tv_title.setText(txtTitle);

        if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
            new DownloadReligionInfo().execute();
            new DownloadMotherToungue().execute();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }

        edittext_Religion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                    showReligionDialog(v);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
        });

        edittext_MotherToungue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                    showMotherTongueDialog(v);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
        });

        edittext_FamilyOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (familyOriginInfoList != null) {
                    showFamilyOriginDialog(v);
                }
            }
        });

        edittext_Specification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (specificationInfoList != null) {
                    showSpecificationDialog(v);
                }
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validProfileHeritage()) {
                    if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                        new SaveProfileHeritageInfo().execute();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        setProfHeritageFromContext();
    }

    /*Custom font */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.logo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProfileHeritage.this, ProfileInfo.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(ProfileHeritage.this, ProfileInfo.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
        return true;
    }

    public void setProfHeritageFromContext() {
        String rel_name = AppData.getString(ProfileHeritage.this, BureauConstants.religionName);
        String mother_tongue = AppData.getString(ProfileHeritage.this, BureauConstants.motherTongue);
        String family_origin_name = AppData.getString(ProfileHeritage.this, BureauConstants.familyOriginName);
        String specification_name = AppData.getString(ProfileHeritage.this, BureauConstants.specificationName);
        String gothra = AppData.getString(ProfileHeritage.this, BureauConstants.gothra);
        String religionId = AppData.getString(ProfileHeritage.this, BureauConstants.religionId);
        String motherToungueId = AppData.getString(ProfileHeritage.this, BureauConstants.motherTongueId);
        String familyOriginId = AppData.getString(ProfileHeritage.this, BureauConstants.familyOriginId);
        String specificationId = AppData.getString(ProfileHeritage.this, BureauConstants.specificationId);

        if (rel_name != null) {
            edittext_Religion.setText(rel_name);
        }

        if (mother_tongue != null) {
            edittext_MotherToungue.setText(mother_tongue);
        }

        if (family_origin_name != null) {
            edittext_FamilyOrigin.setText(family_origin_name);
        }

        if (specification_name != null) {
            edittext_Specification.setText(specification_name);
        }

        if (gothra != null) {
            edittext_Gothra.setText(gothra);
        }

        if (religionId != null) {
            sReligionId = religionId;
        }

        if (motherToungueId != null) {
            sMotherToungueId = motherToungueId;
        }

        if (familyOriginId != null) {
            sFamilyOriginId = familyOriginId;
        }

        if (specificationId != null) {
            sSpecificationId = specificationId;
        }
    }

    private void showReligionDialog(View view) {

        imm.hideSoftInputFromWindow(view.getWindowToken(), 1);

        religionDialog = new Dialog(this);
//        religionDialog.setTitle("Select Religion");
        LayoutInflater li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = li.inflate(R.layout.dialog_list_item, null, false);
        religionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        religionDialog.setContentView(v);
        religionDialog.setCancelable(true);
        religionDialog.findViewById(R.id.iv_close_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                religionDialog.dismiss();
            }
        });
        ((TextView) religionDialog.findViewById(R.id.titleMsgTV)).setText("Select Religion");
        religionListView = (ListView) religionDialog.findViewById(R.id.listView_Heritage);
        religionAdapter = new ReligionAdapter(ProfileHeritage.this, R.layout.custom_spinner_item, religionInfoList);
        religionListView.setAdapter(religionAdapter);
        religionDialog.show();

        religionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ReligionInfo religion = religionInfoList.get(position);//religionAdapter.getItem(position);
                String strRrelegionName = religion.getReligionName();
                sReligionId = religion.getReligionId();

                edittext_Religion.setText(strRrelegionName);
                religionDialog.dismiss();

                if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                    new DownloadFamilyOrigin().execute();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }

                edittext_FamilyOrigin.setText("");
                edittext_Specification.setText("");
                edittext_Gothra.setText("");
            }
        });
    }

    private void showMotherTongueDialog(View view) {

        imm.hideSoftInputFromWindow(view.getWindowToken(), 1);

        motherTongueDialog = new Dialog(this);
//        motherTongueDialog.setTitle("Select Mother Tongue");
        LayoutInflater li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = li.inflate(R.layout.dialog_list_item, null, false);
        motherTongueDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        motherTongueDialog.setContentView(v);
        motherTongueDialog.setCancelable(true);
        motherTongueDialog.findViewById(R.id.iv_close_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                motherTongueDialog.dismiss();
            }
        });
        ((TextView) motherTongueDialog.findViewById(R.id.titleMsgTV)).setText("Select Mother Tongue");
        motherTongueListView = (ListView) motherTongueDialog.findViewById(R.id.listView_Heritage);
        motherToungueAdapter = new MotherToungueAdapter(ProfileHeritage.this, R.layout.custom_spinner_item, motherToungueInfoList);
        motherTongueListView.setAdapter(motherToungueAdapter);
        motherTongueDialog.show();

        motherTongueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MotherToungueInfo motherTongue = motherToungueAdapter.getItem(position);
                String strMotherToungueName = motherTongue.getMothertoungue();
                sMotherToungueId = motherTongue.getMothertoungueId();

                edittext_MotherToungue.setText(strMotherToungueName);
                motherTongueDialog.dismiss();
            }
        });
    }

    private void showFamilyOriginDialog(View view) {

        imm.hideSoftInputFromWindow(view.getWindowToken(), 1);
        familyOriginDialog = new Dialog(this);
//        familyOriginDialog.setTitle("Select Family Origin");
        LayoutInflater li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = li.inflate(R.layout.dialog_list_item, null, false);
        familyOriginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        familyOriginDialog.setContentView(v);
        familyOriginDialog.setCancelable(true);
        familyOriginDialog.findViewById(R.id.iv_close_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                familyOriginDialog.dismiss();
            }
        });
        ((TextView) familyOriginDialog.findViewById(R.id.titleMsgTV)).setText("Select Family Origin");
        familyOriginListView = (ListView) familyOriginDialog.findViewById(R.id.listView_Heritage);
        familyOriginAdapter = new FamilyOriginAdapter(ProfileHeritage.this, R.layout.custom_spinner_item, familyOriginInfoList);
        familyOriginListView.setAdapter(familyOriginAdapter);
        familyOriginDialog.show();

        familyOriginListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                FamilyOriginInfo familyOrigin = familyOriginAdapter.getItem(position);
                String strFamilyOriginName = familyOrigin.getFamilyOriginName();
                sFamilyOriginId = familyOrigin.getFamilyOriginId();

                edittext_FamilyOrigin.setText(strFamilyOriginName);
                familyOriginDialog.dismiss();

                if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                    new DownloadSpecification().execute();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }

                edittext_Specification.setText("");
            }
        });
    }

    private void showSpecificationDialog(View view) {

        imm.hideSoftInputFromWindow(view.getWindowToken(), 1);
        specificationDialog = new Dialog(this);
//        specificationDialog.setTitle("Select Specification");
        LayoutInflater li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = li.inflate(R.layout.dialog_list_item, null, false);
        specificationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        specificationDialog.setContentView(v);
        specificationDialog.setCancelable(true);
        specificationDialog.findViewById(R.id.iv_close_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                specificationDialog.dismiss();
            }
        });
        ((TextView) specificationDialog.findViewById(R.id.titleMsgTV)).setText("Select Specification");
        specificationListView = (ListView) specificationDialog.findViewById(R.id.listView_Heritage);
        specificationAdapter = new SpecificationAdapter(ProfileHeritage.this, R.layout.custom_spinner_item, specificationInfoList);
        specificationListView.setAdapter(specificationAdapter);
        specificationDialog.show();

        specificationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SpecificationInfo specification = specificationAdapter.getItem(position);
                String strSpecificationName = specification.getSpecificationName();
                sSpecificationId = specification.getSpecificationId();

                edittext_Specification.setText(strSpecificationName);
                specificationDialog.dismiss();
            }
        });
    }

    private class DownloadReligionInfo extends AsyncTask<Void, Void, String> {
        String religionResult;

        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(ProfileHeritage.this, R.style.progress_dialog);
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

            religionResult = new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.HERITAGE_RELIGION_URL);
            try {
                JSONArray jsonArray = new JSONArray(religionResult);

                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {

                        ReligionInfo religionObj = new ReligionInfo();
                        religionObj.setReligionId(jsonArray.getJSONObject(i).getString(BureauConstants.religionId));
                        religionObj.setReligionName((jsonArray.getJSONObject(i).getString(BureauConstants.religionName)));
                        religionInfoList.add(religionObj);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return religionResult;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equals("Error")) {
//                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(ProfileHeritage.this, ProfileHeritage.this);
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private class DownloadMotherToungue extends AsyncTask<Void, Void, String> {
        String motherToungueResult;

        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(ProfileHeritage.this, R.style.progress_dialog);
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
            motherToungueResult = new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.HERITAGE_MOTHERTONGUE_URL);

            try {

                JSONArray jsonArray = new JSONArray(motherToungueResult);

                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        MotherToungueInfo motherTounguesObj = new MotherToungueInfo();
                        motherTounguesObj.setMothertoungue(jsonArray.getJSONObject(i).getString(BureauConstants.motherTongue));
                        motherTounguesObj.setMothertoungueid(jsonArray.getJSONObject(i).getString(BureauConstants.motherTongueId));
                        motherToungueInfoList.add(motherTounguesObj);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return motherToungueResult;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equals("Error")) {
//                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(ProfileHeritage.this, ProfileHeritage.this);
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private class DownloadFamilyOrigin extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(ProfileHeritage.this, R.style.progress_dialog);
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
            familyOriginInfoList = new ArrayList<>();

            try {
                List<NameValuePair> parms = new LinkedList<NameValuePair>();
                parms.add(new BasicNameValuePair(BureauConstants.religionId, sReligionId));

                String paramString = URLEncodedUtils.format(parms, "utf-8");
                String url = BureauConstants.BASE_URL + BureauConstants.HERITAGE_FAMILY_ORIGIN_URL;
                url += "?";
                url += paramString;

                familyOriginResult = new ConnectBureau().getDataFromUrl(url);


                JSONArray jsonArray = new JSONArray(familyOriginResult);
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        FamilyOriginInfo familyOriginObj = new FamilyOriginInfo();
                        familyOriginObj.setFamilyOriginId(jsonArray.getJSONObject(i).getString(BureauConstants.familyOriginId));
                        familyOriginObj.setFamilyOriginName(jsonArray.getJSONObject(i).getString(BureauConstants.familyOriginName));
                        familyOriginInfoList.add(familyOriginObj);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return familyOriginResult;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equals("Error")) {
//                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(ProfileHeritage.this, ProfileHeritage.this);
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private class DownloadSpecification extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(ProfileHeritage.this, R.style.progress_dialog);
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

            specificationInfoList = new ArrayList<>();

            try {
                List<NameValuePair> parms = new LinkedList<NameValuePair>();
                parms.add(new BasicNameValuePair(BureauConstants.familyOriginId, sFamilyOriginId));
                String paramString = URLEncodedUtils.format(parms, "utf-8");

                String url = BureauConstants.BASE_URL + BureauConstants.HERITAGE_SPECIFICATION_URL;
                url += "?";
                url += paramString;

                specificationResult = new ConnectBureau().getDataFromUrl(url);


                JSONArray jsonArray = new JSONArray(specificationResult);
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        SpecificationInfo specificationOgj = new SpecificationInfo();
                        specificationOgj.setSpecificationId(jsonArray.getJSONObject(i).getString(BureauConstants.specificationId));
                        specificationOgj.setSpecificationName(jsonArray.getJSONObject(i).getString(BureauConstants.specificationName));
                        specificationInfoList.add(specificationOgj);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return specificationResult;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equals("Error")) {
//                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(ProfileHeritage.this, ProfileHeritage.this);
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private class SaveProfileHeritageInfo extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(ProfileHeritage.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(ProfileHeritage.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.religionId, sReligionId));
            parms.add(new BasicNameValuePair(BureauConstants.motherTongueId, sMotherToungueId));
            parms.add(new BasicNameValuePair(BureauConstants.familyOriginId, sFamilyOriginId));

            if (sSpecificationId != null) {
                parms.add(new BasicNameValuePair(BureauConstants.specificationId, sSpecificationId));
            }

            if (!getGothra().equals("") && getGothra() != null) {
                parms.add(new BasicNameValuePair(BureauConstants.gothra, getGothra()));
            }
            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.PROFILE_HERITAGE_URL, parms);
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
                        com.bureau.bureauapp.util.Util.invalidateUserID(ProfileHeritage.this, ProfileHeritage.this);
                    } else {
                        final Dialog customDialog = new Dialog(ProfileHeritage.this);
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
                    AppData.saveString(ProfileHeritage.this, BureauConstants.religionName, getReligionName());
                    AppData.saveString(ProfileHeritage.this, BureauConstants.motherTongue, getMotherTongueName());
                    AppData.saveString(ProfileHeritage.this, BureauConstants.familyOriginName, getFamilyOriginName());
                    AppData.saveString(ProfileHeritage.this, BureauConstants.specificationName, getSpecificationName());
                    AppData.saveString(ProfileHeritage.this, BureauConstants.religionId, sReligionId);
                    AppData.saveString(ProfileHeritage.this, BureauConstants.motherTongueId, sMotherToungueId);
                    AppData.saveString(ProfileHeritage.this, BureauConstants.familyOriginId, sFamilyOriginId);
                    AppData.saveString(ProfileHeritage.this, BureauConstants.specificationId, sSpecificationId);
                    AppData.saveString(ProfileHeritage.this, BureauConstants.gothra, getGothra());
                    AppData.saveString(ProfileHeritage.this, BureauConstants.profileStatus, "update_profile_step4");

                    Intent intent = new Intent(ProfileHeritage.this, SocialHabit.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    public String getReligionName() {
        return edittext_Religion.getText().toString();
    }

    public String getMotherTongueName() {
        return edittext_MotherToungue.getText().toString();
    }

    public String getFamilyOriginName() {
        return edittext_FamilyOrigin.getText().toString();
    }

    public String getSpecificationName() {
        return edittext_Specification.getText().toString();
    }

    public String getGothra() {
        return edittext_Gothra.getText().toString();
    }

    private boolean validProfileHeritage() {
        boolean bStatus = false;
        String religion = getReligionName();
        String motherTongue = getMotherTongueName();
        String familyOrigin = getFamilyOriginName();

        if (religion.equals("")) {
            edittext_Religion.setError("Please Select Religion");
        } else if (motherTongue.equals("")) {
            edittext_MotherToungue.setError("Please Select Mother Tongue");
        } else if (familyOrigin.equals("")) {
            edittext_FamilyOrigin.setError("Please Select Family Origin");
        } else
            bStatus = true;

        return bStatus;
    }
}