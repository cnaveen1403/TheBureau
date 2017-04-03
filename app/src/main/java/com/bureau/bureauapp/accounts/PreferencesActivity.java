package com.bureau.bureauapp.accounts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.BureauRangeSeekbar;
import com.bureau.bureauapp.helperclasses.BureauSeekbar;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.helperclasses.MotherToungueInfo;
import com.bureau.bureauapp.helperclasses.ReligionInfo;
import com.bureau.bureauapp.interfaces.OnRangeSeekbarChangeListener;
import com.bureau.bureauapp.interfaces.OnSeekbarChangeListener;
import com.bureau.bureauapp.interfaces.OnSeekbarFinalValueListener;
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

import javax.xml.transform.Result;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PreferencesActivity extends Activity {

    private static final String LOG_TAG = PreferencesActivity.class.getSimpleName();
    BureauRangeSeekbar ageRangeSeekbar, heightRangeSeekbar;
    BureauSeekbar radiusSeekbar;
    ToggleButton tb_profile_gender;
    CheckBox vegetarianCB, nonVegetarianCB, veganCB, dietNoPreferenceCB, eggetarianCB, neverMarriedCB, divorcedCB, widowedCB, maritalNoPreferenceCB;
    //    CheckBox checkBox;
    final String[] array_Education = {"Doctorate", "Masters", "Bachelors", "Associates", "Grade School"};
    final String[] array_accountCreatedBy = {"Self", "Other", "Both"};
    String sHighLvlEducation = "";
    InputMethodManager imm;
    Spinner spinner_profile;
    private ArrayList<ReligionInfo> religionInfoList;
    private ArrayList<MotherToungueInfo> motherToungueInfoList;
    EditText edittext_MotherToungue, edittext_Religion, et_HighLvlEducation_edit;
    TextView tvAgeMin, tvAgeMax, tvHeightMin, tvHeightMax, tvLocationRadius;
    ImageView ivSave;
    ArrayList<String> religionList, motherTongueList;
    List<String> selectedReligionListIds, selectedMotherTongueListIds, selectedReligionNamesList, selectedMotherTongueNamesList;
    AlertDialog alert;
    boolean[] religionBoolean, mothertongueBoolean;
    boolean isDietNoPreferenceTOuch, isMatialNoPreferanceTouch;
//    public static int locationRangeValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        tb_profile_gender = (ToggleButton) findViewById(R.id.tb_profile_gender);
        et_HighLvlEducation_edit = (EditText) findViewById(R.id.et_HighLvlEducation_edit);
        edittext_Religion = (EditText) findViewById(R.id.et_religion_edit);
        edittext_MotherToungue = (EditText) findViewById(R.id.et_mother_toungue_edit);
        vegetarianCB = (CheckBox) findViewById(R.id.rd_vegetarian_edit);
        eggetarianCB = (CheckBox) findViewById(R.id.rd_eggetarian_edit);
        nonVegetarianCB = (CheckBox) findViewById(R.id.rd_nonvegetarian_edit);
        veganCB = (CheckBox) findViewById(R.id.rd_vegan_edit);
        dietNoPreferenceCB = (CheckBox) findViewById(R.id.rd_dieNoPreference);
        neverMarriedCB = (CheckBox) findViewById(R.id.rd_NeverMarried);
        divorcedCB = (CheckBox) findViewById(R.id.rd_Divorced);
        widowedCB = (CheckBox) findViewById(R.id.rd_Widowed);
        maritalNoPreferenceCB = (CheckBox) findViewById(R.id.rd_nopreference);
        ivSave = (ImageView) findViewById(R.id.iv_save_preferences);
        // get min and max age text view
        tvAgeMin = (TextView) findViewById(R.id.tvAgeMin);
        tvAgeMax = (TextView) findViewById(R.id.tvAgeMax);
        // get min and max height text view
        tvHeightMin = (TextView) findViewById(R.id.textMinHeight);
        tvHeightMax = (TextView) findViewById(R.id.textMaxHeight);
        tvLocationRadius = (TextView) findViewById(R.id.tvLocationRadius);

        religionInfoList = new ArrayList<>();
        motherToungueInfoList = new ArrayList<>();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        spinner_profile = (Spinner) findViewById(R.id.spinner_profilefor);
        religionList = new ArrayList<>();
        motherTongueList = new ArrayList<>();
        selectedReligionListIds = new ArrayList<>();
        selectedReligionNamesList = new ArrayList<>();
        selectedMotherTongueListIds = new ArrayList<>();
        selectedMotherTongueNamesList = new ArrayList<>();

//        checkbox click listneres
        checkBoxClickListeners();

//        init Agerange radiusSeekbar
        setAgeRangeSeekbar();

        //Init Height Rangebar
        setHeightRangeSeekbar();

        //init location radius radiusSeekbar
        setLocationRadiusSeekbar();


        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        tb_profile_gender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    ((ImageView) findViewById(R.id.ivFemale)).setColorFilter(Color.argb(255, 254, 12, 44));
                    ((ImageView) findViewById(R.id.ivMale)).setColorFilter(Color.argb(255, 0, 0, 0));
                } else {
                    ((ImageView) findViewById(R.id.ivMale)).setColorFilter(Color.argb(255, 254, 12, 44));
                    ((ImageView) findViewById(R.id.ivFemale)).setColorFilter(Color.argb(255, 0, 0, 0));
                }
            }
        });

        spinner_profile.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, array_accountCreatedBy));

        edittext_Religion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReligionDialog(v);
            }
        });

        edittext_MotherToungue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMotherTongueDialog(v);
            }
        });

        et_HighLvlEducation_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHighLvlEducationDialog();
            }
        });

        if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
            new ReadPreferenceAsync().execute();
        } else {
            Toast.makeText(PreferencesActivity.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }

        ivSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                    new SavePreferences().execute();
                } else {
                    Toast.makeText(PreferencesActivity.this, getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setAgeRangeSeekbar() {
        // get radiusSeekbar from view
        ageRangeSeekbar = (BureauRangeSeekbar) findViewById(R.id.ageRangeSeekbar);

        // set listener
        ageRangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                tvAgeMin.setText(String.valueOf(minValue));
                tvAgeMax.setText(String.valueOf(maxValue));
            }
        });
    }

    private void setHeightRangeSeekbar() {
        // get radiusSeekbar from view
        heightRangeSeekbar = (BureauRangeSeekbar) findViewById(R.id.heightRangeSeekbar);

        // set listener
        heightRangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                int mintext = minValue.intValue() / 12;
                int mintext1 = minValue.intValue() % 12;

                String mim = Math.round(mintext) + "." + Math.round(mintext1);

                int maxtext = maxValue.intValue() / 12;
                int maxtext1 = maxValue.intValue() % 12;
                String mio = maxtext + "." + maxtext1;

                tvHeightMin.setText(String.valueOf(mim));
                tvHeightMax.setText(String.valueOf(mio));
            }
        });
    }

    private void setLocationRadiusSeekbar() {

        // get radiusSeekbar from view
        radiusSeekbar = (BureauSeekbar) findViewById(R.id.radiusSeekbar);
        radiusSeekbar.setMinStartValue(325).apply();

        // set listener
        radiusSeekbar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue) {
                String selectedValue = String.valueOf(minValue);
                if (minValue.intValue() > 300) {
                    selectedValue = "No Limit";
                }
                tvLocationRadius.setText(String.valueOf(selectedValue));
            }
        });

        // set final value listener
        radiusSeekbar.setOnSeekbarFinalValueListener(new OnSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number value) {
                Log.d("CRS=>", String.valueOf(value));
//                tvMax.setText(String.valueOf(value));
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

    private void checkBoxClickListeners() {
        maritalNoPreferenceCB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isMatialNoPreferanceTouch = true;
                return false;
            }
        });

        neverMarriedCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isMatialNoPreferanceTouch = false;
                if (!isChecked) {
                    maritalNoPreferenceCB.setChecked(false);
                } else if (divorcedCB.isChecked() && widowedCB.isChecked()) {
                    maritalNoPreferenceCB.setChecked(true);
                }
            }
        });

        widowedCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isMatialNoPreferanceTouch = false;
                if (!isChecked) {
                    maritalNoPreferenceCB.setChecked(false);
                } else if (divorcedCB.isChecked() && neverMarriedCB.isChecked()) {
                    maritalNoPreferenceCB.setChecked(true);
                }
            }
        });

        divorcedCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isMatialNoPreferanceTouch = false;
                if (!isChecked) {
                    maritalNoPreferenceCB.setChecked(false);
                } else if (neverMarriedCB.isChecked() && widowedCB.isChecked()) {
                    maritalNoPreferenceCB.setChecked(true);
                }
            }
        });

        maritalNoPreferenceCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    neverMarriedCB.setChecked(true);
                    widowedCB.setChecked(true);
                    divorcedCB.setChecked(true);
                } else {
                    if (isMatialNoPreferanceTouch) {
                        neverMarriedCB.setChecked(false);
                        widowedCB.setChecked(false);
                        divorcedCB.setChecked(false);
                    }
                }
            }
        });

        dietNoPreferenceCB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isDietNoPreferenceTOuch = true;
                return false;
            }
        });

        vegetarianCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDietNoPreferenceTOuch = false;
                if (!isChecked) {
                    dietNoPreferenceCB.setChecked(false);
                } else if (nonVegetarianCB.isChecked() && eggetarianCB.isChecked() && veganCB.isChecked()) {
                    dietNoPreferenceCB.setChecked(true);
                }
            }
        });
        eggetarianCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDietNoPreferenceTOuch = false;
                if (!isChecked) {
                    dietNoPreferenceCB.setChecked(false);
                } else if (nonVegetarianCB.isChecked() && vegetarianCB.isChecked() && veganCB.isChecked()) {
                    dietNoPreferenceCB.setChecked(true);
                }
            }
        });
        veganCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDietNoPreferenceTOuch = false;
                if (!isChecked) {
                    dietNoPreferenceCB.setChecked(false);
                } else if (nonVegetarianCB.isChecked() && eggetarianCB.isChecked() && vegetarianCB.isChecked()) {
                    dietNoPreferenceCB.setChecked(true);
                }
            }
        });
        nonVegetarianCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDietNoPreferenceTOuch = false;
                if (!isChecked) {
                    dietNoPreferenceCB.setChecked(false);
                } else if (vegetarianCB.isChecked() && eggetarianCB.isChecked() && veganCB.isChecked()) {
                    dietNoPreferenceCB.setChecked(true);
                }
            }
        });

        dietNoPreferenceCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    veganCB.setChecked(true);
                    vegetarianCB.setChecked(true);
                    nonVegetarianCB.setChecked(true);
                    eggetarianCB.setChecked(true);
                } else {
                    if (isDietNoPreferenceTOuch) {
                        veganCB.setChecked(false);
                        vegetarianCB.setChecked(false);
                        nonVegetarianCB.setChecked(false);
                        eggetarianCB.setChecked(false);
                    }
                }
            }
        });
    }

    private void showMotherTongueDialog(View view) {

        imm.hideSoftInputFromWindow(view.getWindowToken(), 1);

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle("Select Religions")
                .setMultiChoiceItems(motherTongueList.toArray(new String[motherTongueList.size()]), mothertongueBoolean, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if (isChecked) {
                            mothertongueBoolean[which] = true;
                            selectedMotherTongueListIds.add(motherToungueInfoList.get(which).getMothertoungueId());
                            selectedMotherTongueNamesList.add(motherToungueInfoList.get(which).getMothertoungue());
                        } else {
                            mothertongueBoolean[which] = false;
                            selectedMotherTongueListIds.remove(motherToungueInfoList.get(which).getMothertoungueId());
                            selectedMotherTongueNamesList.remove(motherToungueInfoList.get(which).getMothertoungue());
                        }
                        if (which == motherToungueInfoList.size() - 1) {
                            selectedMotherTongueListIds = new ArrayList<String>();
                            selectedMotherTongueNamesList = new ArrayList<String>();
                            if (isChecked) {
                                alert.getListView().setItemChecked(motherToungueInfoList.size() - 1, true);
                                for (int i = 1; i < motherToungueInfoList.size(); i++) {
                                    mothertongueBoolean[i] = true;
                                    alert.getListView().setItemChecked(i, true);
                                    selectedMotherTongueListIds.add(motherToungueInfoList.get(i).getMothertoungueId());
                                    selectedMotherTongueNamesList.add(motherToungueInfoList.get(i).getMothertoungue());
                                }

                            } else {
                                for (int i = 1; i < motherToungueInfoList.size(); i++) {
                                    mothertongueBoolean[i] = false;
                                    alert.getListView().setItemChecked(i, false);
                                    selectedMotherTongueListIds.remove(motherToungueInfoList.get(i).getMothertoungueId());
                                    selectedMotherTongueNamesList.remove(motherToungueInfoList.get(i).getMothertoungue());
                                }
                            }
                        }
                    }
                });
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String motherTongues = selectedMotherTongueNamesList.toString();
                        motherTongues = motherTongues.replace("[", "");
                        motherTongues = motherTongues.replace("]", "");
                        edittext_MotherToungue.setText(motherTongues);
                    }
                });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        alert = builder.create();
        alert.show();
    }

    private void showReligionDialog(View view) {

        imm.hideSoftInputFromWindow(view.getWindowToken(), 1);

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle("Select Religions")
                .setMultiChoiceItems(religionList.toArray(new String[religionList.size()]), religionBoolean, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if (isChecked) {
                            religionBoolean[which] = true;
                            selectedReligionListIds.add(religionInfoList.get(which).getReligionId());
                            selectedReligionNamesList.add(religionInfoList.get(which).getReligionName());
                        } else {
                            religionBoolean[which] = false;
                            selectedReligionListIds.remove(religionInfoList.get(which).getReligionId());
                            selectedReligionNamesList.remove(religionInfoList.get(which).getReligionName());
                        }
                        if (which == religionList.size() - 1) {
                            selectedReligionListIds = new ArrayList<String>();
                            selectedReligionNamesList = new ArrayList<String>();
                            if (isChecked) {
                                alert.getListView().setItemChecked(religionList.size() - 1, true);
                                for (int i = 0; i < religionList.size(); i++) {
                                    religionBoolean[i] = true;
                                    alert.getListView().setItemChecked(i, true);
                                    selectedReligionListIds.add(religionInfoList.get(i).getReligionId());
                                    selectedReligionNamesList.add(religionInfoList.get(i).getReligionName());
                                }

                            } else {
                                for (int i = 0; i < religionList.size(); i++) {
                                    religionBoolean[i] = false;
                                    alert.getListView().setItemChecked(i, false);
                                    selectedReligionListIds.remove(religionInfoList.get(i).getReligionId());
                                    selectedReligionNamesList.remove(religionInfoList.get(i).getReligionName());
                                }
                            }
                        }


                    }
                });
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String religions = selectedReligionNamesList.toString();
                        religions = religions.replace("[", "");
                        religions = religions.replace("]", "");
                        edittext_Religion.setText(religions);
                    }
                });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        alert = builder.create();

        alert.show();
    }

    public void showHighLvlEducationDialog() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(array_Education, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                sHighLvlEducation = array_Education[position];
                et_HighLvlEducation_edit.setText(sHighLvlEducation);
            }
        });

        builder.create();
        builder.show();
    }

    private class ReadPreferenceAsync extends AsyncTask<Void, Void, Result> {

        Dialog progressDialog;
        String resultStr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new Dialog(PreferencesActivity.this, R.style.progress_dialog);
            progressDialog.setContentView(R.layout.dialog);
            progressDialog.setCancelable(true);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Result doInBackground(Void... params) {

            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(PreferencesActivity.this, BureauConstants.userid)));

            String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = BureauConstants.BASE_URL + BureauConstants.READ_PREFERENCES;
            url += "?";
            url += paramString;

            resultStr = new ConnectBureau().getDataFromUrl(url);
            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            religionList.clear();
            try {
                Log.e("TheBureau", "READ_PREFERENCES " + resultStr);
                JSONObject jsonObjectmain = new JSONObject(resultStr);
                JSONObject jsonObject = jsonObjectmain.getJSONObject("preferences");//new JSONObject(resultStr);

                float minHeight = (float) ((intergerDefaultvalid("" + jsonObject.getString("height_from_feet")) * 12)) + (intergerDefaultvalid("" + jsonObject.getString("height_from_inch")));
                float maxHeight = (float) ((intergerDefaultvalid("" + jsonObject.getString("height_to_feet")) * 12)) + (intergerDefaultvalid("" + jsonObject.getString("height_to_inch")));
                float minAge = (float) Integer.parseInt(jsonObject.getString("age_from"));
                float maxAge = (float) Integer.parseInt(jsonObject.getString("age_to"));

                ageRangeSeekbar.setMinStartValue(minAge).setMaxStartValue(maxAge).setGap(4).apply();
                heightRangeSeekbar.setMinStartValue(minHeight).setMaxStartValue(maxHeight).setGap(4).apply();

                if (jsonObject.getString("gender").equalsIgnoreCase("Female")) {
                    tb_profile_gender.setChecked(true);
                } else {
                    tb_profile_gender.setChecked(false);
                }

                radiusSeekbar.setMinStartValue(intergerDefaultvalid(jsonObject.getString("location_radius"))).apply();

                JSONArray jsonArrayMotherToungue = jsonObjectmain.getJSONArray("mother_tongue");// new JSONArray(familyOriginResult);

                if (jsonArrayMotherToungue.length() > 0) {
                    for (int i = 0; i < jsonArrayMotherToungue.length(); i++) {
                        MotherToungueInfo motherTounguesObj = new MotherToungueInfo();
                        motherTounguesObj.setMothertoungue(jsonArrayMotherToungue.getJSONObject(i).getString(BureauConstants.motherTongue));
                        motherTounguesObj.setMothertoungueid(jsonArrayMotherToungue.getJSONObject(i).getString(BureauConstants.motherTongueId));
                        motherToungueInfoList.add(motherTounguesObj);
                        motherTongueList.add(jsonArrayMotherToungue.getJSONObject(i).getString(BureauConstants.motherTongue));
                    }
                }
                mothertongueBoolean = new boolean[motherTongueList.size()];
                JSONArray jsonArrayReligion = jsonObjectmain.getJSONArray("religion");// new JSONArray(familyOriginResult);

                if (jsonArrayReligion.length() > 0) {
                    for (int i = 0; i < jsonArrayReligion.length(); i++) {
                        ReligionInfo religionObj = new ReligionInfo();
                        religionObj.setReligionId(jsonArrayReligion.getJSONObject(i).getString(BureauConstants.religionId));
                        religionObj.setReligionName((jsonArrayReligion.getJSONObject(i).getString(BureauConstants.religionName)));
                        religionInfoList.add(religionObj);
                        religionList.add(jsonArrayReligion.getJSONObject(i).getString(BureauConstants.religionName));
                    }
                }
                religionBoolean = new boolean[religionList.size()];


                JSONArray religionDataArray = jsonObject.getJSONArray("religion_data");
                for (int i = 0; i < religionDataArray.length(); i++) {
                    JSONObject relegionDataObj = religionDataArray.getJSONObject(i);
                    selectedReligionListIds.add(relegionDataObj.getString("religion_id"));
                    selectedReligionNamesList.add(relegionDataObj.getString("religion_name"));
                }

                String religions = selectedReligionNamesList.toString();
                religions = religions.replace("[", "");
                religions = religions.replace("]", "");
                edittext_Religion.setText(religions);

                JSONArray motherTongueDataArray = jsonObject.getJSONArray("mother_tongue_data");
                for (int i = 0; i < motherTongueDataArray.length(); i++) {
                    JSONObject motherTongueDataObj = motherTongueDataArray.getJSONObject(i);
                    selectedMotherTongueListIds.add(motherTongueDataObj.getString("mother_tongue_id"));
                    selectedMotherTongueNamesList.add(motherTongueDataObj.getString("mother_tongue"));
                }

                String motherTongues = selectedMotherTongueNamesList.toString();
                motherTongues = motherTongues.replace("[", "");
                motherTongues = motherTongues.replace("]", "");
                edittext_MotherToungue.setText(motherTongues);

                for (int i = 0; i < religionInfoList.size(); i++) {
                    if (selectedReligionListIds.contains(religionInfoList.get(i).getReligionId())) {
                        religionBoolean[i] = true;
                    }
                }
                for (int i = 0; i < motherToungueInfoList.size(); i++) {
                    if (selectedMotherTongueListIds.contains(motherToungueInfoList.get(i).getMothertoungueId())) {
                        mothertongueBoolean[i] = true;
                    }
                }

                et_HighLvlEducation_edit.setText(jsonObject.getString("minimum_education_requirement"));

                Log.e("Bureau", "spinner index val " + jsonObject.getString("account_created_by"));


                int index = 0;
                for (int i = 0; i < array_accountCreatedBy.length; i++) {
                    if (array_accountCreatedBy[i].equalsIgnoreCase(jsonObject.getString("account_created_by"))) {
                        index = i;
                        Log.e("Bureau", "spinner index inside " + index);
                    }
                }
                spinner_profile.setSelection(index);

                Log.e("Bureau", "spinner index " + index);
                String diet = jsonObject.getString("diets");
                String maritial_status = jsonObject.getString("marital_status");

                if (diet != null) {


                    diet = diet.replace("[", "");
                    diet = diet.replace("]", "");
                    diet = diet.replace("\"", "");

                    String[] dietArray = diet.split(",");
                    for (int i = 0; i < dietArray.length; i++) {
                        if (dietArray[i].equalsIgnoreCase("Vegetarian") || dietArray[i].equalsIgnoreCase("No Preference")) {
                            vegetarianCB = (CheckBox) findViewById(R.id.rd_vegetarian_edit);
                            vegetarianCB.setChecked(true);
                        }
                        if (dietArray[i].equalsIgnoreCase("Eggetarian") || dietArray[i].equalsIgnoreCase("No Preference")) {
                            eggetarianCB = (CheckBox) findViewById(R.id.rd_eggetarian_edit);
                            eggetarianCB.setChecked(true);
                        }
                        if (dietArray[i].equalsIgnoreCase("Non Vegetarian") || dietArray[i].equalsIgnoreCase("No Preference")) {
                            nonVegetarianCB = (CheckBox) findViewById(R.id.rd_nonvegetarian_edit);
                            nonVegetarianCB.setChecked(true);
                        }
                        if (dietArray[i].equalsIgnoreCase("Vegan") || dietArray[i].equalsIgnoreCase("No Preference")) {
                            veganCB = (CheckBox) findViewById(R.id.rd_vegan_edit);
                            veganCB.setChecked(true);
                        }
                        if (dietArray[i].equalsIgnoreCase("No Preference")) {
                            dietNoPreferenceCB = (CheckBox) findViewById(R.id.rd_dieNoPreference);
                            dietNoPreferenceCB.setChecked(true);
                        }
                    }
                }

                if (maritial_status != null) {
                    if (maritial_status.contains("Never Married") || maritial_status.contains("No Preference")) {
                        neverMarriedCB = (CheckBox) findViewById(R.id.rd_NeverMarried);
                        neverMarriedCB.setChecked(true);
                    }
                    if (maritial_status.contains("Divorced") || maritial_status.contains("No Preference")) {
                        divorcedCB = (CheckBox) findViewById(R.id.rd_Divorced);
                        divorcedCB.setChecked(true);
                    }
                    if (maritial_status.contains("Widowed") || maritial_status.contains("No Preference")) {
                        widowedCB = (CheckBox) findViewById(R.id.rd_Widowed);
                        widowedCB.setChecked(true);
                    }
                    if (maritial_status.contains("No Preference")) {
                        maritalNoPreferenceCB = (CheckBox) findViewById(R.id.rd_nopreference);
                        maritalNoPreferenceCB.setChecked(true);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> getDiet() {

        ArrayList<String> diets = new ArrayList<>();
        if (((CheckBox) findViewById(R.id.rd_vegetarian_edit)).isChecked()) {
            diets.add("Vegetarian");
        }
        if (((CheckBox) findViewById(R.id.rd_eggetarian_edit)).isChecked()) {
            diets.add("Eggetarian");
        }
        if (((CheckBox) findViewById(R.id.rd_nonvegetarian_edit)).isChecked()) {
            diets.add("Non Vegetarian");
        }
        if (((CheckBox) findViewById(R.id.rd_vegan_edit)).isChecked()) {
            diets.add("Vegan");
        }
        if (((CheckBox) findViewById(R.id.rd_dieNoPreference)).isChecked()) {
            diets.add("No Preference");
        }

        return diets;
    }


    public ArrayList<String> getMaritalStatus() {

        ArrayList<String> maritalStatus = new ArrayList<>();

        if (((CheckBox) findViewById(R.id.rd_NeverMarried)).isChecked()) {
            maritalStatus.add("Never Married");
        }
        if (((CheckBox) findViewById(R.id.rd_Divorced)).isChecked()) {
            maritalStatus.add("Divorced");
        }
        if (((CheckBox) findViewById(R.id.rd_Widowed)).isChecked()) {
            maritalStatus.add("Widowed");
        }
        if (((CheckBox) findViewById(R.id.rd_dieNoPreference)).isChecked()) {
            maritalStatus.add("No Preference");
        }

        return maritalStatus;
    }

    private String getProfileGender() {
        String gender = "Male";
        if (tb_profile_gender.isChecked()) {
            gender = "Female";
        }

        return gender;
    }

    public static int intergerDefaultvalid(String text) {
        if (TextUtils.isEmpty(text)) {
            return 0;
        }
        if (text == "") {
            return 0;
        }
        return Integer.parseInt(text);
    }

    private class SavePreferences extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;
        List<NameValuePair> parms;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(PreferencesActivity.this, R.style.progress_dialog);
            progressDialog.setContentView(R.layout.dialog);
            progressDialog.setCancelable(true);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(PreferencesActivity.this, BureauConstants.userid)));
            if (selectedReligionListIds.size() > 0) {
                for (int i = 0; i < selectedReligionListIds.size(); i++) {
                    parms.add(new BasicNameValuePair(BureauConstants.religionId + "[" + i + "]", selectedReligionListIds.get(i)));
                }
            } else {
                parms.add(new BasicNameValuePair(BureauConstants.religionId + "[" + 0 + "]", ""));
            }
            if (selectedMotherTongueListIds.size() > 0) {
                for (int i = 0; i < selectedMotherTongueListIds.size(); i++) {
                    parms.add(new BasicNameValuePair(BureauConstants.motherTongueId + "[" + i + "]", selectedMotherTongueListIds.get(i)));
                }
            } else {
                parms.add(new BasicNameValuePair(BureauConstants.motherTongueId + "[" + 0 + "]", ""));

            }
            if (getMaritalStatus().size() > 0) {
                for (int i = 0; i < getMaritalStatus().size(); i++) {
                    parms.add(new BasicNameValuePair(BureauConstants.maritialStatus + "[" + i + "]", getMaritalStatus().get(i)));
                }
            } else {
                parms.add(new BasicNameValuePair(BureauConstants.maritialStatus + "[" + 0 + "]", ""));

            }
            if (getDiet().size() > 0) {
                for (int i = 0; i < getDiet().size(); i++) {
                    parms.add(new BasicNameValuePair(BureauConstants.diet + "[" + i + "]", getDiet().get(i)));
                }
            } else {
                parms.add(new BasicNameValuePair(BureauConstants.diet + "[" + 0 + "]", ""));

            }

            parms.add(new BasicNameValuePair(BureauConstants.gender, getProfileGender()));
            parms.add(new BasicNameValuePair(BureauConstants.locationRadius, getLocationRadius()));
            parms.add(new BasicNameValuePair(BureauConstants.ageFrom, getSelectedMinAge()));
            parms.add(new BasicNameValuePair(BureauConstants.ageTo, getSelectedMaxAge()));
            parms.add(new BasicNameValuePair(BureauConstants.heightFromFeet, getSelectedMinHeightFoot()));
            parms.add(new BasicNameValuePair(BureauConstants.heightFromInch, getSelectedMinHeightInch()));
            parms.add(new BasicNameValuePair(BureauConstants.heightToFeet, getSelectedMaxHeightFoot()));
            parms.add(new BasicNameValuePair(BureauConstants.heightToInch, getSelectedMaxHeightInch()));
            parms.add(new BasicNameValuePair(BureauConstants.accountCreatedBy, spinner_profile.getSelectedItem().toString()));
            parms.add(new BasicNameValuePair(BureauConstants.minimumEducationRequirement, et_HighLvlEducation_edit.getText().toString()));

        }

        @Override
        protected String doInBackground(Void... params) {


            String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = BureauConstants.BASE_URL + BureauConstants.CREATE_PREFERENCES;
            url += "?";
            url += paramString;

            Log.e("Bureau", "create PREF " + url);
            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.CREATE_PREFERENCES, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            final Dialog dialog = new Dialog(PreferencesActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.simple_alert);

            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equalsIgnoreCase("Error")) {
                    String response = jsonObject.getString("response");
                    ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText(msg);
                    ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(response);
// set the custom dialog components - text, image and button
                    TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                    text.setText(android.R.string.ok);

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else {

                    ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Success");
                    ((TextView) dialog.findViewById(R.id.dialogMessage)).setText("Saved Successfully");
// set the custom dialog components - text, image and button
                    TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                    text.setText(android.R.string.ok);

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    });
                    dialog.show();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private String getLocationRadius() {
        String radius = tvLocationRadius.getText().toString();
        if (radius.equalsIgnoreCase("No Limit")) {
            radius = "325";
        }

        return radius;
    }

    private String getSelectedMaxHeightInch() {
        String height = tvHeightMax.getText().toString();

        String[] parts = height.split("\\.");
        int temp = Integer.parseInt(parts[1]);
        String inches;
        if (temp < 10) {
            inches = "0" + temp;
        } else {
            inches = Integer.toString(temp);
        }

        return inches;
    }

    private String getSelectedMaxHeightFoot() {
        String height = tvHeightMax.getText().toString();

        String[] parts = height.split("\\.");
        return parts[0];
    }

    private String getSelectedMinHeightInch() {
        String height = tvHeightMin.getText().toString();
        String[] parts = height.split("\\.");
        int temp = Integer.parseInt(parts[1]);
        String inches;
        if (temp < 10) {
            inches = "0" + temp;
        } else {
            inches = Integer.toString(temp);
        }

        return inches;
    }

    private String getSelectedMinHeightFoot() {
        String height = tvHeightMin.getText().toString();
        String[] parts = height.split("\\.");
        return parts[0];
    }

    private String getSelectedMaxAge() {
        return tvAgeMax.getText().toString();
    }

    private String getSelectedMinAge() {
        return tvAgeMin.getText().toString();
    }
}