package com.roundel.souvenirnotifier;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.roundel.souvenirnotifier.entities.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
{
     private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        String token = FirebaseInstanceId.getInstance().getToken();

        User.autoDetect("roundel", "roundel", (user) -> {
            Log.d(TAG, user.toString());
            user.loadSummary(summary -> Log.d(TAG, summary.username));
        });
    }
}
