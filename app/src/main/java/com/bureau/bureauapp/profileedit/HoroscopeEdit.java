package com.bureau.bureauapp.profileedit;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bureau.bureauapp.R;
import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.helperclasses.ConnectBureau;
import com.bureau.bureauapp.helperclasses.ImageZoomActivity;
import com.bureau.bureauapp.helperclasses.SelectDocument;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.permissions.PermissionsActivity;
import com.bureau.bureauapp.permissions.PermissionsChecker;
import com.bureau.bureauapp.profilesetup.ProfileInfo;
import com.bureau.bureauapp.util.Util;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

public class HoroscopeEdit extends AppCompatActivity {

    String LOG_TAG = HoroscopeEdit.class.getSimpleName();
    private static EditText et_dob, et_tob, et_lob, et_about_me;
    ImageView save, iv_horoscope, iv_add_icon;
    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    PermissionsChecker checker;
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;
    private final CharSequence[] items = {"Take Photo", "From Gallery"};
    Uri mCapturedImageURI;
    String realPath, horoscope_url = "";
    private String[] pager_items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_horoscope_edit);
        init();

        checker = new PermissionsChecker(this);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void init() {
        save = (ImageView) findViewById(R.id.iv_save_horoscope);
        iv_horoscope = (ImageView) findViewById(R.id.iv_horoscope);
        iv_add_icon = (ImageView) findViewById(R.id.iv_add_icon);
        et_dob = (EditText) findViewById(R.id.et_horoscope_dob_edit);
        et_tob = (EditText) findViewById(R.id.et_horoscope_tob_edit);
        et_lob = (EditText) findViewById(R.id.et_horoscope_lob_edit);
        et_about_me = (EditText) findViewById(R.id.et_aboutme_edit);

        findViewById(R.id.backIconIMG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        et_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDOBDateDialogue(v);
            }
        });

        et_dob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDOBDateDialogue(v);
                }
            }
        });

        et_tob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeDialogue(v);
            }
        });

        et_tob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showTimeDialogue(v);
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectBureau.isNetworkAvailable(getApplicationContext())) {
                    if (validateHoroscopeForm())
                        new SaveHoroscope().execute();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_LONG).show();
                }
            }
        });

        iv_horoscope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!horoscope_url.equals("")) {
                    pager_items = new String[1];
                    pager_items[0] = horoscope_url;

                    Intent intent = new Intent(HoroscopeEdit.this, ImageZoomActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("position", 0);
                    intent.putExtra("pager_items", pager_items);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        iv_horoscope.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Dialog dialog = new Dialog(HoroscopeEdit.this);
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

        iv_add_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
                    startPermissionsActivity(PERMISSIONS_READ_STORAGE);
                } else {
                    openFileChooserDialog();
                }
            }
        });

        setHoroscopeDetailsFromContext();
    }

    private boolean validateHoroscopeForm() {
        boolean bStatus = false;

        if (getDateOfBirth().equals("")) {
            et_dob.setError("Please enter date of birth.");
        } else if (getTimeOfBirth().equals("")) {
            et_dob.setError(null);
            et_tob.setError("Plese enter time of birth.");
        } else if (getLocationOfBirth().equals("")) {
            et_dob.setError(null);
            et_lob.setError("Please enter location of birth.");
        } else {
            et_lob.setError(null);
            bStatus = true;
        }

        return bStatus;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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

    private void initCameraIntent() {
        String fileName = "temp.jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void initGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {

            //----- Correct Image Rotation ----//
            Uri correctedUri = null;
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCapturedImageURI);
                imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(HoroscopeEdit.this, mCapturedImageURI));
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
        final Dialog dialog = new Dialog(HoroscopeEdit.this, R.style.progress_dialog);
        dialog.setContentView(R.layout.dialog);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
        msg.setText("Please wait ...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

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
                .addFormDataPart(BureauConstants.userid, AppData.getString(HoroscopeEdit.this, BureauConstants.userid))
                .addFormDataPart(BureauConstants.USER_FILE, filename + ".jpg", fileBody)
                .build();

        Request request = new Request.Builder()
                .url(BureauConstants.BASE_URL + BureauConstants.UPLOAD_HOROSCOPE)
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
                        dialog.dismiss();
                        final Dialog customDialog = new Dialog(HoroscopeEdit.this);
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
                        dialog.dismiss();
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

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(this, 0, permission);
    }

    private void showTimeDialogue(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 1);
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            String time = hourOfDay + ":" + minute;
            et_tob.setText(time);
            et_tob.setFocusable(false);
        }
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
            int year = 1990;
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the chosen date
            String selectedDate = (month + 1) + "-" + day + "-" + year;
            et_dob.setText(selectedDate);
            et_dob.setFocusable(false);
        }
    }

    public void setHoroscopeDetailsFromContext() {
        String dob = AppData.getString(HoroscopeEdit.this, BureauConstants.horoscopeDob);
        String tob = AppData.getString(HoroscopeEdit.this, BureauConstants.horoscopeTob);
        String lob = AppData.getString(HoroscopeEdit.this, BureauConstants.horoscopeLob);
        String about_me = AppData.getString(HoroscopeEdit.this, BureauConstants.aboutMe);
        horoscope_url = AppData.getString(HoroscopeEdit.this, BureauConstants.horoscopePath);

        if (dob != null) {
            et_dob.setText(dob);
        }

        if (tob != null) {
            et_tob.setText(tob);
        }

        if (lob != null) {
            et_lob.setText(lob);
        }

        if (about_me != null) {
            et_about_me.setText(about_me);
        }

        if (!horoscope_url.equals("")) {
            Glide.with(HoroscopeEdit.this).load(horoscope_url).into(iv_horoscope);
            iv_add_icon.setVisibility(View.GONE);
        } else {
            iv_add_icon.setVisibility(View.VISIBLE);
        }
    }

    public String getDateOfBirth() {
        return et_dob.getText().toString();
    }

    public String getTimeOfBirth() {
        return et_tob.getText().toString();
    }

    public String getLocationOfBirth() {
        return ((EditText) findViewById(R.id.et_horoscope_lob_edit)).getText().toString();
    }

    public String getAboutMe() {
        return ((EditText) findViewById(R.id.et_aboutme_edit)).getText().toString();
    }

    public class SaveHoroscope extends AsyncTask<String, Void, String> {
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new Dialog(HoroscopeEdit.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(HoroscopeEdit.this, BureauConstants.userid)));
            parms.add(new BasicNameValuePair(BureauConstants.horoscopeDob, getDateOfBirth()));
            parms.add(new BasicNameValuePair(BureauConstants.horoscopeTob, getTimeOfBirth()));
            parms.add(new BasicNameValuePair(BureauConstants.horoscopeLob, getLocationOfBirth()));
            parms.add(new BasicNameValuePair(BureauConstants.aboutMe, getAboutMe()));

            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.EDIT_UPDATE_URL, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            final Dialog customDialog = new Dialog(HoroscopeEdit.this);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setContentView(R.layout.simple_alert);

            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equals("Error")) {
                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(HoroscopeEdit.this, HoroscopeEdit.this);
                    } else {
                        ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText(msg);
                        ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText(response);
// set the custom dialog components - text, image and button
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
                    AppData.saveString(HoroscopeEdit.this, BureauConstants.horoscopeDob, getDateOfBirth());
                    AppData.saveString(HoroscopeEdit.this, BureauConstants.horoscopeTob, getTimeOfBirth());
                    AppData.saveString(HoroscopeEdit.this, BureauConstants.horoscopeLob, getLocationOfBirth());
                    AppData.saveString(HoroscopeEdit.this, BureauConstants.aboutMe, getAboutMe());

                    ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Success");
                    ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText("Saved Successfully");
// set the custom dialog components - text, image and button
                    TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                    text.setText("OK");

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            customDialog.dismiss();
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    });
                    customDialog.show();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private class ReadUserProfileDetails extends AsyncTask<Void, Void, String> {
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new Dialog(HoroscopeEdit.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(HoroscopeEdit.this, BureauConstants.userid)));

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
                        Util.invalidateUserID(HoroscopeEdit.this, HoroscopeEdit.this);
                    } else {
                        final Dialog customDialog = new Dialog(HoroscopeEdit.this);
                        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        customDialog.setContentView(R.layout.simple_alert);
                        ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                        ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText(response);
// set the custom dialog components - text, image and button
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
                    horoscope_url = AppData.getString(HoroscopeEdit.this, BureauConstants.horoscopePath);
                    if (!horoscope_url.equals("")) {
                        Glide.with(HoroscopeEdit.this).load(horoscope_url).into(iv_horoscope);
                        iv_add_icon.setVisibility(View.GONE);
                    } else {
                        Glide.with(HoroscopeEdit.this).load(horoscope_url).into(iv_horoscope);
                        iv_add_icon.setVisibility(View.VISIBLE);
                    }

                    dialog.dismiss();
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
                AppData.saveString(HoroscopeEdit.this, key, jsonObject.getString(key));
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private class DeleteImageAync extends AsyncTask<String, Void, String> {
        Dialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new Dialog(HoroscopeEdit.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(HoroscopeEdit.this, BureauConstants.userid)));

            return new ConnectBureau().getDataFromUrl(BureauConstants.BASE_URL + BureauConstants.DELETE_HOROSCOPE_IMAGE, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String msg = jsonObject.getString("msg");

                if (msg.equalsIgnoreCase("Error")) {
                    String response = jsonObject.getString("response");
                    if (jsonObject.getString("response").equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(HoroscopeEdit.this, HoroscopeEdit.this);
                    } else {
                        final Dialog customDialog = new Dialog(HoroscopeEdit.this);
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
}
