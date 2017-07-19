package com.roundel.souvenirnotifier;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.roundel.souvenirnotifier.adapters.SteamUsersAdapter;
import com.roundel.souvenirnotifier.entities.SteamUser;
import com.roundel.souvenirnotifier.entities.UserData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String DATABASE_USERS = "users";
    private static final String DATABASE_TOKEN = "token";
    private static final String DATABASE_STEAM_ACCOUNTS = "steamAccounts";

    @BindView(R.id.floatingActionButton) FloatingActionButton mFab;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;

    private SteamUsersAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private FirebaseAuth mAuth;
    private List<SteamUser> mSteamUsers = new ArrayList<>();
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        mToken = FirebaseInstanceId.getInstance().getToken();

        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new SteamUsersAdapter(this, mSteamUsers);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
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
        //TODO: Show a custom dialog that will try to add a new Steam Account
    }

    private void addUser(SteamUser steamUser, boolean notify)
    {
        if(!mSteamUsers.contains(steamUser))
        {
            mSteamUsers.add(steamUser);
            saveData();
            updateUi();
        }
        else if(notify)
        {
            Toast.makeText(this, "This Steam account is already in the list", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void saveData()
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference usersReference = db.getReference(DATABASE_USERS).child(mAuth.getCurrentUser().getUid());
        usersReference.child(DATABASE_TOKEN).setValue(mToken);
        usersReference.child(DATABASE_STEAM_ACCOUNTS).removeValue((err, ref) -> {
            for(SteamUser account : mSteamUsers)
            {
                ref.child(String.valueOf(account.getSteamId64())).setValue(account.getUsername());
            }
        });

    }

    @SuppressWarnings("ConstantConditions")
    private void registerOnDataChangeListener()
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(DATABASE_USERS).child(mAuth.getCurrentUser().getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@Nullable DataSnapshot dataSnapshot)
            {
                if(dataSnapshot != null)
                {
                    DataSnapshot steamUsers = dataSnapshot.child(DATABASE_STEAM_ACCOUNTS);
                    for(DataSnapshot snapshot : steamUsers.getChildren())
                    {
                        SteamUser.fromSteamId64(Long.parseLong(snapshot.getKey()), steamUser -> addUser(steamUser, false));
                    }
                }
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
        mAdapter.swapData(mSteamUsers);
    }
}
