package com.bureau.bureauapp.layerclasses;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.bureau.bureauapp.helperclasses.BureauConstants;
import com.bureau.bureauapp.myapplication.AppData;
import com.bureau.bureauapp.permissions.PermissionsActivity;
import com.bureau.bureauapp.permissions.PermissionsChecker;
import com.layer.atlas.messagetypes.AttachmentSender;
import com.layer.atlas.messagetypes.threepartimage.ThreePartImageUtils;
import com.layer.atlas.util.Log;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.PushNotificationPayload;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static android.support.v4.app.ActivityCompat.requestPermissions;
import static android.support.v4.content.ContextCompat.checkSelfPermission;

/**
 * Created by hi on 3/7/2017.
 */

public class BureauGallerySender extends AttachmentSender {
    private static final String PERMISSION = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) ? Manifest.permission.READ_EXTERNAL_STORAGE : null;
    public static final int ACTIVITY_REQUEST_CODE = 10;
    public static final int PERMISSION_REQUEST_CODE = 11;
    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    PermissionsChecker checker;


    private WeakReference<Activity> mActivity = new WeakReference<Activity>(null);

    public BureauGallerySender(int titleResId, Integer iconResId, Activity activity) {
        this(activity.getString(titleResId), iconResId, activity);
    }

    public BureauGallerySender(String title, Integer iconResId, Activity activity) {
        super(title, iconResId);
        mActivity = new WeakReference<Activity>(activity);
        checker = new PermissionsChecker(mActivity.get());
    }

    private void startGalleryIntent(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(Intent.createChooser(intent, getContext().getString(com.layer.atlas.R.string.atlas_gallery_sender_chooser)), ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE) return;
        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            if (Log.isLoggable(Log.VERBOSE)) Log.v("Gallery permission denied");
            return;
        }
        Activity activity = mActivity.get();
        if (activity == null) return;
        startGalleryIntent(activity);
    }

    @Override
    public boolean requestSend() {
        Activity activity = mActivity.get();
        if (activity == null) return false;
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Sending gallery image");
       /* if (PERMISSION != null && checkSelfPermission(activity, PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(activity, new String[]{PERMISSION}, PERMISSION_REQUEST_CODE);
            return true;
        }
        startGalleryIntent(activity);*/

        if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
            startPermissionsActivity(PERMISSIONS_READ_STORAGE);
        } else {
            startGalleryIntent(activity);
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
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Received gallery response");
        try {
//            String myName = getParticipantProvider().getParticipant(getLayerClient().getAuthenticatedUserId()).getName();
            Message message = ThreePartImageUtils.newThreePartImageMessage(activity, getLayerClient(), data.getData());

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

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(mActivity.get(), 0, permission);
    }
}