package com.roundel.souvenirnotifier.services;
/*
 * Created by Krzysiek on 17/07/2017.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.roundel.souvenirnotifier.R;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = MessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        final Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, data.toString());

        final String username = data.get("username");
        final String price = data.get("price");
        final String team1 = data.get("team1");
        final String team2 = data.get("team2");
        final String event = data.get("event");
        final String year = data.get("year");
        final String map = data.get("map");
        final String url = data.get("url");

        String title = String.format(Locale.getDefault(), "%s just got a drop from %s", username, map);
        String text = String.format(Locale.getDefault(), "The package was dropped in the %s vs. %s match and is worth %s", team1, team2, price);

        sendNotification(title, text, url);
    }

    private void sendNotification(String title, String text, String itemUrl) {
        Intent showItemIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemUrl));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, showItemIntent, 0);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_gift_24dp)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(text);

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBuilder(builder);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final int id = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
        final Notification notification = bigTextStyle.build();

        manager.notify(id, notification);
    }
}
