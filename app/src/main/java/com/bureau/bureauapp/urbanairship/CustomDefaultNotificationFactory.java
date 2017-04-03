package com.bureau.bureauapp.urbanairship;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.bureau.bureauapp.R;
import com.urbanairship.push.PushMessage;
import com.urbanairship.push.notifications.DefaultNotificationFactory;

public class CustomDefaultNotificationFactory extends DefaultNotificationFactory {

    public CustomDefaultNotificationFactory(@NonNull Context context) {
        super(context);
    }

    @Override
    public NotificationCompat.Builder extendBuilder(@NonNull NotificationCompat.Builder builder, @NonNull PushMessage message, int notificationId) {
        builder.setSmallIcon(R.drawable.ic_notification)
                .setColor(NotificationCompat.COLOR_DEFAULT);
        return builder;
    }
}
