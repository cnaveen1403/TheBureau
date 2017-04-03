package com.bureau.bureauapp.profilesetup;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bureau.bureauapp.R;
import com.bureau.bureauapp.adapters.AddImageAdapter;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.helperclasses.HorizontalListView;
import com.bureau.bureauapp.helperclasses.ImageZoomActivity;
import com.bureau.bureauapp.helperclasses.SelectDocument;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.permissions.PermissionsActivity;
import com.bureau.bureauapp.permissions.PermissionsChecker;
import com.bureau.bureauapp.util.Util;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class ProfileInfo extends AppCompatActivity {
    private static String LOG_TAG = ProfileInfo.class.getSimpleName();

//    MyApplication myApplication;

    LinearLayout tv_okay, tv_cancel;
    EditText editText_Zipcode, editText_Height;
    private static EditText editText_ProfileDOB;
    ImageView image_female, image_male;
    RadioGroup res_country_group, martial_status_group;
    RadioButton radioButton;
    NumberPicker foot_np, inches_np;
    ToggleButton profile_gender;
    Button button_Continue;
    HorizontalListView horizontalScrollView;
    ImageView ivAddImage;

    final String[] footvalues = {"4\'", "5\'", "6\'", "7\'"};
    final String[] inchvalues = {"0\"", "1\"", "2\"", "3\"", "4\"", "5\"", "6\"", "7\""
            , "8\"", "9\"", "10\"", "11\"", "12\""};

    String inchesSelected = "0\"", footSelected = "4\'";

    String sHeightInFoot, sHeightInInches, deleteImageUrl, realPath;
    public ArrayList<String> profileImages;
    private PermissionsChecker checker;
    AddImageAdapter addImageAdapter;
    String[] pager_items;
    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private final CharSequence[] items = {"Take Photo", "From Gallery"};
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;
    Uri mCapturedImageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        init();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void init() {
        editText_Zipcode = (EditText) findViewById(R.id.editText_Zipcode);
        editText_Height = (EditText) findViewById(R.id.editText_Height);
        res_country_group = (RadioGroup) findViewById(R.id.RCountry_radio);
        martial_status_group = (RadioGroup) findViewById(R.id.radioGroup_MaritialStatus);
        editText_ProfileDOB = (EditText) findViewById(R.id.editText_ProfileDOB);
        profile_gender = (ToggleButton) findViewById(R.id.tb_profile_gender);
        image_female = (ImageView) findViewById(R.id.ivFemale);
        image_male = (ImageView) findViewById(R.id.ivMale);
        button_Continue = (Button) findViewById(R.id.button_profileInfo);
        ivAddImage = (ImageView) findViewById(R.id.ivAddProfileImage);

        profileImages = new ArrayList<>();
        checker = new PermissionsChecker(this);
        String image_urls = AppData.getString(ProfileInfo.this, BureauConstants.imgUrl);
        try {
            if (image_urls != null) {
                JSONArray jsonArray = new JSONArray(image_urls);
                for (int i = 0; i < jsonArray.length(); i++) {
                    profileImages.add(jsonArray.getString(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        horizontalScrollView = (HorizontalListView) findViewById(R.id.profileImagesList);
        addImageAdapter = new AddImageAdapter(ProfileInfo.this, profileImages);
        horizontalScrollView.setAdapter(addImageAdapter);

        ivAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileImages.size() < 4) {
                    if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
                        startPermissionsActivity(PERMISSIONS_READ_STORAGE);
                    } else {
                        openFileChooserDialog();
                    }
                } else {
                    Toast.makeText(ProfileInfo.this, "You are allowed to add only 4 images.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        horizontalScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (profileImages.size() > 0) {
                    pager_items = new String[profileImages.size()];
                    for (int i = 0; i < profileImages.size(); i++) {
                        pager_items[i] = profileImages.get(i);
                    }

                    Intent intent = new Intent(ProfileInfo.this, ImageZoomActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("position", position);
                    intent.putExtra("pager_items", pager_items);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        horizontalScrollView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteImageUrl = profileImages.get(position);
                final Dialog dialog = new Dialog(ProfileInfo.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.yes_no_alert);
                ((TextView) dialog.findViewById(R.id.tv_title_yes_no)).setText("Delete Image");
                ((TextView) dialog.findViewById(R.id.tv_message_yes_no)).setText("Are you sure you want to delete this image ?");
                TextView text = (TextView) dialog.findViewById(R.id.tv_no_btn);
                text.setText("NO");

                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                TextView textView = (TextView) dialog.findViewById(R.id.tv_canael_btn);
                textView.setText("YES");
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        new DeleteImageAync().execute();
                    }
                });
                dialog.show();
                return false;
            }
        });

        TextView tv_title = (TextView) findViewById(R.id.toolbar_Profile_info_title);
        String profileName = AppData.getString(ProfileInfo.this, BureauConstants.profileFirstName);
        String txtTitle = profileName + "'s Info";
        tv_title.setText(txtTitle);

        String profilefor = AppData.getString(ProfileInfo.this, BureauConstants.profileFor);
        if (profilefor.equals("Self")) {
            if (AppData.getString(ProfileInfo.this, BureauConstants.gender).equals("Female")) {
                profile_gender.setChecked(true);
            }

            editText_ProfileDOB.setText(AppData.getString(ProfileInfo.this, BureauConstants.dob));
        }

        editText_Height.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showHeightPicker(v);
                }
            }
        });

        editText_Height.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHeightPicker(v);
            }
        });

        editText_ProfileDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDOBDateDialogue(v);
            }
        });

        editText_ProfileDOB.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDOBDateDialogue(v);
                }
            }
        });

        profile_gender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    image_male.setImageResource(R.drawable.img_male_black);
                    image_female.setImageResource(R.drawable.img_female_red);
                } else {
                    image_female.setImageResource(R.drawable.img_female_black);
                    image_male.setImageResource(R.drawable.img_male_red);
                }
            }
        });

        button_Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                        CheckZipcodeTask checkZipcode = new CheckZipcodeTask();
                        checkZipcode.execute();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        setProfInfoFromContext();
    }

    @Override
    public void onBackPressed() {
//        AppData.saveString(ProfileInfo.this, "navigate_back", "yes");
        Intent intent = new Intent(ProfileInfo.this, AccountCreation.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(ProfileInfo.this, AccountCreation.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.logo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void setProfInfoFromContext() {
        String country_residing = AppData.getString(ProfileInfo.this, BureauConstants.countryResiding);
        String zipcode = AppData.getString(ProfileInfo.this, BureauConstants.currentZipCode);
        String martial_status = AppData.getString(ProfileInfo.this, BureauConstants.maritialStatus);
        String profile_dob = AppData.getString(ProfileInfo.this, BureauConstants.profileDob);
        String profile_gender = AppData.getString(ProfileInfo.this, BureauConstants.profileGender);
        String height_feet = AppData.getString(ProfileInfo.this, BureauConstants.heightFeet);
        String height_inch = AppData.getString(ProfileInfo.this, BureauConstants.heightInch);

        if (country_residing != null) {
            if (country_residing.equals("India")) {
                radioButton = (RadioButton) findViewById(R.id.radio_India);
                radioButton.setChecked(true);
            } else {
                radioButton = (RadioButton) findViewById(R.id.radio_USA);
                radioButton.setChecked(true);
            }
        }

        if (zipcode != null) {
            editText_Zipcode.setText(zipcode);
        }

        if (martial_status != null) {
            if (martial_status.equals(R.string.txtNeverMarried)) {
                radioButton = (RadioButton) findViewById(R.id.radio_NeverMarried);
                radioButton.setChecked(true);
            } else if (martial_status.equals(R.string.txtDivorced)) {
                radioButton = (RadioButton) findViewById(R.id.radio_Divorced);
                radioButton.setChecked(true);
            } else if (martial_status.equals(R.string.txtWidowed)) {
                radioButton = (RadioButton) findViewById(R.id.radio_Widowed);
                radioButton.setChecked(true);
            }
        }

        if (profile_dob != null) {
            editText_ProfileDOB.setText(profile_dob);
        }

        if (profile_gender != null) {
            setGender(profile_gender);
        }

        if (height_feet != null && height_inch != null) {
            sHeightInFoot = height_feet;
            sHeightInInches = height_inch;
            String selectedValue = "";
            if (!height_feet.equals("") && !height_inch.equals("")) {
                selectedValue = height_feet + "' " + height_inch + "\"";
            }
            editText_Height.setText(selectedValue);
        }
    }

    public void setGender(String gender) {
        if (gender.equals("Male"))
            profile_gender.setChecked(true);
        else
            profile_gender.setChecked(false);
    }

    public void showHeightPicker(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 1);

        final Dialog dialog = new Dialog(ProfileInfo.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.height_picker);
        foot_np = (NumberPicker) dialog.findViewById(R.id.foot_np);
        inches_np = (NumberPicker) dialog.findViewById(R.id.inch_np);
        tv_cancel = (LinearLayout) dialog.findViewById(R.id.ll_cancel);
        tv_okay = (LinearLayout) dialog.findViewById(R.id.ll_Okay);

        foot_np.setMinValue(0); //from array first value
        foot_np.setMaxValue(footvalues.length - 1);
        foot_np.setDisplayedValues(footvalues);
//        foot_np.setWrapSelectorWheel(true);

        inches_np.setMinValue(0);
        inches_np.setMaxValue(inchvalues.length - 1);
        inches_np.setDisplayedValues(inchvalues);
//        inches_np.setWrapSelectorWheel(true);

        dialog.show();

        foot_np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                footSelected = footvalues[newVal];
            }
        });

        inches_np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                inchesSelected = inchvalues[newVal];
            }
        });

        tv_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String selectedValue = footSelected + " " + inchesSelected;
                sHeightInFoot = footSelected;
                sHeightInInches = inchesSelected;
                editText_Height.setText(selectedValue);
                footSelected = "4\'";
                inchesSelected = "0\"";
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                footSelected = "4\'";
                inchesSelected = "0\"";
            }
        });
    }

    private void showDOBDateDialogue(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 1);
        DialogFragment dFragment = new DatePickerFragment();
        dFragment.show(getFragmentManager(), "date picker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.MONTH) - 18;
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the chosen date
            String selectedDate = (month + 1) + "-" + day + "-" + year;
            editText_ProfileDOB.setText(selectedDate);
        }
    }

    public String getResidingCountry() {
        int country = res_country_group.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(country);
        return radioButton.getText().toString();
    }

    public String getZipCode() {
        return editText_Zipcode.getText().toString();
    }

    public String getSelectedHeightInFoot() {
        return sHeightInFoot;
    }

    public String getSelectedHeightInInches() {
        return sHeightInInches;
    }

    public String getMaritialStatus() {
        int mar_status_selected = martial_status_group.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(mar_status_selected);
        return radioButton.getText().toString();
    }

    public String getProfileGender() {
        String gender = "Male";
        if (profile_gender.isChecked()) {
            gender = "Female";
        }

        return gender;
    }

    public String getProfileDOB() {
        return editText_ProfileDOB.getText().toString();
    }

    public boolean validate() {
        boolean status = false;

        String zipcode = getZipCode();
        String dob = getProfileDOB();
        String heightEditText = editText_Height.getText().toString();

        if (zipcode.equals("")) {
            editText_Zipcode.setError("Please enter country zip code");
        } else if (heightEditText.equals("")) {
            editText_Height.setError("Please select height");
        } else if (dob.equals("")) {
            editText_ProfileDOB.setError("Please enter date of birth !!!");
        } else {
            editText_Zipcode.setError(null);
            editText_Height.setError(null);
            editText_ProfileDOB.setError(null);
            status = true;
        }

        return status;
    }

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(this, 0, permission);
    }

    private void openFileChooserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        initCameraIntent();
                        break;
                    case 1:
                        initGalleryIntent();
                        break;
                    default:
                }
            }
        });
        builder.show();
    }

    private void initGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    private void initCameraIntent() {
        String fileName = "temp.jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {

            //----- Correct Image Rotation ----//
            Uri correctedUri = null;
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCapturedImageURI);
                imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(ProfileInfo.this, mCapturedImageURI));
                correctedUri = getImageUri(imageBitmap);
                realPath = getRealPathFromURI(correctedUri);

                try {
                    execMultipartPost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            realPath = getRealPathFromURI(selectedImageUri);
            try {
                execMultipartPost();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap imageOrientationValidator(Bitmap bitmap, String path) {

        ExifInterface ei;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public String getRealPathFromURI(Uri contentUri) {
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            @SuppressWarnings("deprecation")
            Cursor cursor = managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return contentUri.getPath();
        }
    }

    private Uri getImageUri(Bitmap image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), image, "Title", null);
        return Uri.parse(path);
    }

    private void execMultipartPost() throws Exception {
        final Dialog progressDialog = new Dialog(ProfileInfo.this, R.style.progress_dialog);
        progressDialog.setContentView(R.layout.dialog);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
        msg.setText("Please wait ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        File file = new File(realPath);
        String contentType = getMimeType(realPath);
        if (contentType == null) {
            contentType = "image/jpeg";
        }


        Log.e(LOG_TAG, "file: " + file.getPath());
        Log.e(LOG_TAG, "contentType: " + contentType);

        RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);

        final String filename = "file_" + System.currentTimeMillis() / 1000L;

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(BureauConstants.userid, AppData.getString(ProfileInfo.this, BureauConstants.userid))
                .addFormDataPart(BureauConstants.USER_FILE, filename + ".jpg", fileBody)
                .build();

        Request request = new Request.Builder()
                .url(BureauConstants.BASE_URL + BureauConstants.MULTI_UPLOAD)
                .header("x-api-key", BureauConstants.x_api_key)
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        final Dialog customDialog = new Dialog(ProfileInfo.this);
                        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        customDialog.setContentView(R.layout.simple_alert);
                        ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                        ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText("Error occurred !! Please try again.");
                        TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                            }
                        });
                        customDialog.show();
//                        Log.d("NewFarm", "nah it is not done ya");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        new ReadUserProfileDetails().execute();
                    }
                });
            }
        });
    }

    private String getMimeType(String realPath) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(realPath);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private class CheckZipcodeTask extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(ProfileInfo.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.zipCode, getZipCode()));

            String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = BureauConstants.BASE_URL + BureauConstants.CHECK_ZIPCODE_URL;
            url += "?";
            url += paramString;

            return new ConnectBureau().getDataFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null)
                progressDialog.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");
                String country = jsonObject.getString("country");

                if (msg.equals("Error")) {
                    //redirect user to login page
                    showInvalidZipcodeDialog();
                } else if (country.equals("USA") && getResidingCountry().equals("India")) {
                    showInvalidZipcodeDialog();
                } else if (country.equals("India") && getResidingCountry().equals("USA")) {
                    showInvalidZipcodeDialog();
                } else {
                    ProfileInfoSubmitTask registrationStep2 = new ProfileInfoSubmitTask();
                    registrationStep2.execute();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private void showInvalidZipcodeDialog() {
        final Dialog customDialog = new Dialog(ProfileInfo.this);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.simple_alert);
        ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Error");
        ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText("Invalid Zipcode !!!");
        TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
        text.setText("OK");

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
                editText_Zipcode.setText("");
            }
        });
        customDialog.show();
    }

    private class ProfileInfoSubmitTask extends AsyncTask<Void, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(ProfileInfo.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(ProfileInfo.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.profileGender, getProfileGender()));
            parms.add(new BasicNameValuePair(BureauConstants.profileDob, getProfileDOB()));
            parms.add(new BasicNameValuePair(BureauConstants.countryResiding, getResidingCountry()));
            parms.add(new BasicNameValuePair(BureauConstants.currentZipCode, getZipCode()));
            parms.add(new BasicNameValuePair(BureauConstants.heightFeet, getSelectedHeightInFoot()));
            parms.add(new BasicNameValuePair(BureauConstants.heightInch, getSelectedHeightInInches()));

            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.UPDATE_PROFILE_INFO_URL, parms);
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
                        com.bureau.bureauapp.util.Util.invalidateUserID(ProfileInfo.this, ProfileInfo.this);
                    } else {
                        final Dialog customDialog = new Dialog(ProfileInfo.this);
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
                    AppData.saveString(ProfileInfo.this, BureauConstants.countryResiding, getResidingCountry());
                    AppData.saveString(ProfileInfo.this, BureauConstants.currentZipCode, getZipCode());
                    AppData.saveString(ProfileInfo.this, BureauConstants.maritialStatus, getMaritialStatus());
                    AppData.saveString(ProfileInfo.this, BureauConstants.profileDob, getProfileDOB());
                    AppData.saveString(ProfileInfo.this, BureauConstants.profileGender, getProfileGender());
                    AppData.saveString(ProfileInfo.this, BureauConstants.heightFeet, getSelectedHeightInFoot());
                    AppData.saveString(ProfileInfo.this, BureauConstants.heightInch, getSelectedHeightInInches());
                    AppData.saveString(ProfileInfo.this, BureauConstants.profileStatus, "update_profile_step3");

                    Intent intent = new Intent(ProfileInfo.this, ProfileHeritage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private class DeleteImageAync extends AsyncTask<String, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(ProfileInfo.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.imgUrl, deleteImageUrl));

            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.DELETE_IMAGE_URL, parms);

        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equalsIgnoreCase("Error")) {
                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(ProfileInfo.this, ProfileInfo.this);
                    } else {
                        final Dialog customDialog = new Dialog(ProfileInfo.this);
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
                    new ReadUserProfileDetails().execute();
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
            progressDialog = new Dialog(ProfileInfo.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(ProfileInfo.this, BureauConstants.userid)));

            String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = BureauConstants.BASE_URL + BureauConstants.READ_PROFILE_DETAILS_URL;
            url += "?";
            url += paramString;
            return new ConnectBureau().getDataFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);

                if (jsonObject.has("msg")) {
                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        Util.invalidateUserID(ProfileInfo.this, ProfileInfo.this);
                    } else {
                        final Dialog customDialog = new Dialog(ProfileInfo.this);
                        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        customDialog.setContentView(R.layout.simple_alert);
                        ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Error");
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
                    saveUserProfileDetails(jsonObject);
                    profileImages.clear();

                    JSONArray jsonArray = jsonObject.getJSONArray("img_url");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        profileImages.add(jsonArray.getString(i));
                    }

                    addImageAdapter = new AddImageAdapter(ProfileInfo.this, profileImages);
                    horizontalScrollView.setAdapter(addImageAdapter);
                    progressDialog.dismiss();


                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private void saveUserProfileDetails(JSONObject jsonObject) {
        for (Iterator<String> iter = jsonObject.keys(); iter.hasNext(); ) {
            String key = iter.next();
            try {
                //Save each key value pair received from the Server
//                Log.d(LOG_TAG, "key >> " + key + " >> value >> " + jsonObject.getString(key));
                AppData.saveString(ProfileInfo.this, key, jsonObject.getString(key));
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}