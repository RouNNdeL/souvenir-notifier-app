package com.roundel.souvenirnotifier;
/*
 * Created by Krzysiek on 17/07/2017.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

public class FirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService
{
    private static final String TAG = FirebaseInstanceIdService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 100;

    @Override
    public void onTokenRefresh()
    {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        notifyAboutNewToken();
    }

    private void notifyAboutNewToken()
    {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, 0);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setColor(getColor(R.color.colorPrimary))
                .setContentTitle("New Firebase token")
                .setContentText("A new token has been generated, make sure to update it in the config.cfg")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_ticket_24dp);

        Notification notification = builder.build();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }
}
