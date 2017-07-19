package com.roundel.souvenirnotifier;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.roundel.souvenirnotifier.entities.SteamUser;
import com.roundel.souvenirnotifier.entities.UserData;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.floatingActionButton) FloatingActionButton mFab;
    private FirebaseAuth mAuth;
    private UserData mUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        ButterKnife.bind(this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
        {
            signIn();
        }
        else
        {
            registerOnDataChangeListener();
        }
    }

    private void signIn()
    {
        mAuth.signInAnonymously().addOnCompleteListener(this, task ->
        {
            if(task.isSuccessful())
            {
                Log.d(TAG, "signInAnonymously:success");
                registerOnDataChangeListener();
            }
            else
            {
                Log.w(TAG, "signInAnonymously:failure", task.getException());
            }
        });
    }

    private void showAddUserDialog()
    {

    }

    private void addUser(SteamUser steamUser)
    {
        if(mUserData == null)
        {
            List<SteamUser> steamUsers = new ArrayList<>();
            steamUsers.add(steamUser);

            String token = FirebaseInstanceId.getInstance().getToken();

            mUserData = new UserData(steamUsers, token);
        }
        else
        {
            mUserData.addSteamUser(steamUser);
        }
        saveData();
    }

    @SuppressWarnings("ConstantConditions")
    private void saveData()
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("users").child(mAuth.getCurrentUser().getUid());
        ref.setValue(mUserData);
    }

    @SuppressWarnings("ConstantConditions")
    private void registerOnDataChangeListener()
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("users").child(mAuth.getCurrentUser().getUid());
        ref.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mUserData = dataSnapshot.getValue(UserData.class);
                updateUi();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void updateUi()
    {
        Log.d(TAG, "Updating UI");
    }
}
