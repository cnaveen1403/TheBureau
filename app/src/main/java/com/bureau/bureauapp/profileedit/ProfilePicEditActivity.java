package com.bureau.bureauapp.profileedit;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class ProfilePicEditActivity extends Activity {

    ImageView ivAddImage, backIconIMG;
    private static String TAG = ProfilePicEditActivity.class.getSimpleName();
    HorizontalListView horizontalScrollView;
    public ArrayList<String> profileImages;
    String pager_items[];
    AddImageAdapter addImageAdapter;
    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    PermissionsChecker checker;
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;
    private final CharSequence[] items = {"Take Photo", "From Gallery"};
    Uri mCapturedImageURI;
    String realPath, deleteImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_profile_pic_edit);
        profileImages = new ArrayList<>();

        checker = new PermissionsChecker(this);

        String image_urls = AppData.getString(ProfilePicEditActivity.this, BureauConstants.imgUrl);

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

        ivAddImage = (ImageView) findViewById(R.id.ivAddImage);
        backIconIMG = (ImageView) findViewById(R.id.backIconIMG);
        horizontalScrollView = (HorizontalListView) findViewById(R.id.userImagesHorizantalList);
        addImageAdapter = new AddImageAdapter(ProfilePicEditActivity.this, profileImages);
        horizontalScrollView.setAdapter(addImageAdapter);

        Log.e("Image urls ", "==>>: " + image_urls);

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
                    Toast.makeText(ProfilePicEditActivity.this, "You are allowed to add only 4 images.", Toast.LENGTH_SHORT).show();
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

                    Intent intent = new Intent(ProfilePicEditActivity.this, ImageZoomActivity.class);
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

                final Dialog dialog = new Dialog(ProfilePicEditActivity.this);
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

        backIconIMG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
                imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(ProfilePicEditActivity.this, mCapturedImageURI));
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
        final Dialog progressDialog = new Dialog(ProfilePicEditActivity.this, R.style.progress_dialog);
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


        Log.e(TAG, "file: " + file.getPath());
        Log.e(TAG, "contentType: " + contentType);

        RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);

        final String filename = "file_" + System.currentTimeMillis() / 1000L;

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(BureauConstants.userid, AppData.getString(ProfilePicEditActivity.this, BureauConstants.userid))
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
                        final Dialog customDialog = new Dialog(ProfilePicEditActivity.this);
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
                        Log.d("NewFarm", "nah it is not done ya");
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

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(this, 0, permission);
    }

    private class DeleteImageAync extends AsyncTask<String, Void, String> {
        Dialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new Dialog(ProfilePicEditActivity.this, R.style.progress_dialog);
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
                    if (response.equalsIgnoreCase(BureauConstants.INVALID_USERID)) {
                        com.bureau.bureauapp.util.Util.invalidateUserID(ProfilePicEditActivity.this, ProfilePicEditActivity.this);
                    } else {
                        final Dialog customDialog = new Dialog(ProfilePicEditActivity.this);
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
            progressDialog = new Dialog(ProfilePicEditActivity.this, R.style.progress_dialog);
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
            parms.add(new BasicNameValuePair(BureauConstants.userid, AppData.getString(ProfilePicEditActivity.this, BureauConstants.userid)));

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
                        Util.invalidateUserID(ProfilePicEditActivity.this, ProfilePicEditActivity.this);
                    } else {
                        final Dialog customDialog = new Dialog(ProfilePicEditActivity.this);
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
                        progressDialog.dismiss();
                        customDialog.show();
                    }
                } else {
                    saveUserProfileDetails(jsonObject);
                    profileImages.clear();

                    JSONArray jsonArray = jsonObject.getJSONArray("img_url");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        profileImages.add(jsonArray.getString(i));
                    }

                    addImageAdapter = new AddImageAdapter(ProfilePicEditActivity.this, profileImages);
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
                AppData.saveString(ProfilePicEditActivity.this, key, jsonObject.getString(key));
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}
