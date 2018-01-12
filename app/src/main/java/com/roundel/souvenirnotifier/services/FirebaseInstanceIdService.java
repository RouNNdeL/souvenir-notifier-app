package com.roundel.souvenirnotifier.services;
/*
 * Created by Krzysiek on 17/07/2017.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.roundel.souvenirnotifier.MainActivity;
import com.roundel.souvenirnotifier.R;


import static com.roundel.souvenirnotifier.MainActivity.DATABASE_TOKEN;
import static com.roundel.souvenirnotifier.MainActivity.DATABASE_USERS;

public class FirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService {
    private static final String TAG = FirebaseInstanceIdService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        refreshTokenInDb(refreshedToken);
    }

    private void refreshTokenInDb(String token) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference usersReference = db.getReference(DATABASE_USERS).child(currentUser.getUid());
            usersReference.child(DATABASE_TOKEN).setValue(token);
        }

    }
}
