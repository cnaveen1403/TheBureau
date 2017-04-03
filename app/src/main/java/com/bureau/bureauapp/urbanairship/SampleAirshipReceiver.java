package com.bureau.bureauapp.urbanairship;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bureau.bureauapp.HomeActivity;
import com.urbanairship.AirshipReceiver;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushMessage;

import me.leolin.shortcutbadger.ShortcutBadger;

import static com.bureau.bureauapp.helperclasses.BureauConstants.badgeCount;

public class SampleAirshipReceiver extends AirshipReceiver {

    private static final String LOG_TAG = SampleAirshipReceiver.class.getSimpleName();

    @Override
    protected void onChannelCreated(@NonNull Context context, @NonNull String channelId) {
        Log.i(LOG_TAG, "Channel created. Channel Id:" + channelId + ".");
        UAirship.shared().getNamedUser().setId(channelId);
        new SendUAirshipChannel(context, channelId);
    }

    @Override
    protected void onChannelUpdated(@NonNull Context context, @NonNull String channelId) {
        Log.i(LOG_TAG, "Channel updated. Channel Id:" + channelId + ".");
        UAirship.shared().getNamedUser().setId(channelId);
        new SendUAirshipChannel(context, channelId);
    }

    @Override
    protected void onChannelRegistrationFailed(Context context) {
        Log.i(LOG_TAG, "Channel registration failed.");
    }

    @Override
    protected void onPushReceived(@NonNull Context context, @NonNull PushMessage message, boolean notificationPosted) {
        Log.i(LOG_TAG, "Received push message. Alert: " + message.getAlert() + ". posted notification: " + notificationPosted);
        badgeCount++;
        if (badgeCount < 0)
            badgeCount = 0;

        ShortcutBadger.applyCount(context, badgeCount);
    }

    @Override
    protected void onNotificationPosted(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {
        Log.i(LOG_TAG, "Notification posted. Alert: " + notificationInfo.getMessage().getAlert() + ". NotificationId: " + notificationInfo.getNotificationId());
    }

    @Override
    protected boolean onNotificationOpened(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {
        Log.i(LOG_TAG, "Notification opened. Alert: " + notificationInfo.getMessage().getAlert() + ". NotificationId: " + notificationInfo.getNotificationId());
        badgeCount--;
        if (badgeCount < 0)
            badgeCount = 0;
        ShortcutBadger.applyCount(context, badgeCount);

        /*
        * Get the Extra values from the notification bundle and redirect ot corresponding activity
        * */
        Bundle bundle = notificationInfo.getMessage().getPushBundle();
        String extra = bundle.getString("extra");

        if (extra != null) {
            Intent intent = new Intent(context, HomeActivity.class);
            intent.putExtra("pager_position", 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (extra.equalsIgnoreCase("match") || extra.equalsIgnoreCase("bonus") || extra.equalsIgnoreCase("direct")) {
                intent.putExtra("pager_position", 0);
            } else if (extra.equalsIgnoreCase("connected")) {
                intent.putExtra("pager_position", 2);
            } else if (extra.equalsIgnoreCase("gold")) {
                intent.putExtra("pager_position", 3);
            }

            context.startActivity(intent);
        }
        // Return false here to allow Urban Airship to auto launch the launcher activity
        return true;
    }

    @Override
    protected boolean onNotificationOpened(@NonNull Context context, @NonNull NotificationInfo notificationInfo, @NonNull ActionButtonInfo actionButtonInfo) {
        Log.i(LOG_TAG, "Notification action button opened. Button ID: " + actionButtonInfo.getButtonId() + ". NotificationId: " + notificationInfo.getNotificationId());
        // Return false here to allow Urban Airship to auto launch the launcher
        // activity for foreground notification action buttons
        return false;
    }

    @Override
    protected void onNotificationDismissed(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {
        Log.i(LOG_TAG, "Notification dismissed. Alert: " + notificationInfo.getMessage().getAlert() + ". Notification ID: " + notificationInfo.getNotificationId());
    }
}