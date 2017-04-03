package com.bureau.bureauapp.layerclasses;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.permissions.PermissionsActivity;
import com.bureau.bureauapp.permissions.PermissionsChecker;
import com.layer.atlas.messagetypes.AttachmentSender;
import com.layer.atlas.messagetypes.threepartimage.ThreePartImageUtils;
import com.layer.atlas.util.Log;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.PushNotificationPayload;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by hi on 3/7/2017.
 */

public class BureauCameraSender extends AttachmentSender {
    public static final int ACTIVITY_REQUEST_CODE = 20;

    private WeakReference<Activity> mActivity = new WeakReference<Activity>(null);
    Activity activity;

    private final AtomicReference<String> mPhotoFilePath = new AtomicReference<String>(null);
    private String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
    PermissionsChecker checker;

    public BureauCameraSender(int titleResId, Integer iconResId, Activity activity) {
        this(activity.getString(titleResId), iconResId, activity);
    }

    public BureauCameraSender(String title, Integer iconResId, Activity activity) {
        super(title, iconResId);
        mActivity = new WeakReference<Activity>(activity);
        checker = new PermissionsChecker(mActivity.get());
        this.activity = activity;
    }

    private void startCameraIntent(Activity activity) {
        String fileName = "cameraOutput" + System.currentTimeMillis() + ".jpg";
        File file = new File(getContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), fileName);
        mPhotoFilePath.set(file.getAbsolutePath());
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final Uri outputUri = Uri.fromFile(file);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        activity.startActivityForResult(cameraIntent, ACTIVITY_REQUEST_CODE);
    }

    @Override
    public boolean requestSend() {
        Activity activity = mActivity.get();
        if (activity == null) return false;
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Sending camera image");
        if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
            startPermissionsActivity(PERMISSIONS_READ_STORAGE);
        } else {
            startCameraIntent(activity);
        }
        return true;
    }

    @Override
    public boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != ACTIVITY_REQUEST_CODE) return false;
        if (resultCode != Activity.RESULT_OK) {
            if (Log.isLoggable(Log.ERROR)) Log.e("Result: " + requestCode + ", data: " + data);
            return true;
        }
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Received camera response");
        try {
//            String myName = getParticipantProvider().getParticipant(getLayerClient().getAuthenticatedUserId()).getName();
            Message message = ThreePartImageUtils.newThreePartImageMessage(activity, getLayerClient(), new File(mPhotoFilePath.get()));

            PushNotificationPayload payload = new PushNotificationPayload.Builder()
                    .text(getContext().getString(com.layer.atlas.R.string.atlas_notification_image, AppData.getString(getContext(), BureauConstants.firstName)))
                    .build();
            message.getOptions().defaultPushNotificationPayload(payload);
            send(message);
        } catch (IOException e) {
            if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
        }
        return true;
    }

    /**
     * Saves photo file path during e.g. screen rotation
     */
    @Override
    public Parcelable onSaveInstanceState() {
        String path = mPhotoFilePath.get();
        if (path == null) return null;
        Bundle bundle = new Bundle();
        bundle.putString("photoFilePath", path);
        return bundle;
    }

    /**
     * Restores photo file path during e.g. screen rotation
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null) return;
        String path = ((Bundle) state).getString("photoFilePath");
        mPhotoFilePath.set(path);
    }

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(activity, 0, permission);
    }
}
