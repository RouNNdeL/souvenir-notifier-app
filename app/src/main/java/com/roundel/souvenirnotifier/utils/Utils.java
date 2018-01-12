package com.roundel.souvenirnotifier.utils;
/*
 * Created by Krzysiek on 21/07/2017.
 */

import com.google.firebase.database.FirebaseDatabase;

public class Utils {
    private static FirebaseDatabase mDatabase;

    //Credit https://stackoverflow.com/a/37551156/4061413
    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

}
