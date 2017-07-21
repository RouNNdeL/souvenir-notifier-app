package com.roundel.souvenirnotifier;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements AddSteamUserDialogFragment.OnUserAddedListener, SteamUsersAdapter.OnItemLongClickListener
{
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String DATABASE_USERS = "users";
    public static final String DATABASE_TOKEN = "token";
    public static final String DATABASE_STEAM_ACCOUNTS = "steamAccounts";

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
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        database.getReference(DATABASE_USERS).keepSynced(true);

        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new SteamUsersAdapter(this, mSteamUsers);
        mAdapter.setOnItemLongClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(mLayoutManager);

        mFab.setOnClickListener(v -> showAddUserDialog());
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
            fetchDataFromDb();
        }
    }

    @Override
    public boolean onUserAdded(SteamUser user, boolean inventoryAccessible)
    {
        addUser(user, true);
        if(!inventoryAccessible)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                   .setTitle("Inventory error")
                   .setMessage("We were unable to retrieve contents of you inventory. It's probably set to private, " +
                           "change it's visibility to public or you might not receive the notifications for this account.");
            builder.create().show();
        }

        return true;
    }

    @Override
    public void onItemLongClick(int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to remove this account from your list?")
               .setTitle("Remove account")
               .setPositiveButton("Remove", (dialog, which) ->
               {
                   dialog.dismiss();
                   removeAccountFromDb(mSteamUsers.get(position).getSteamId64());
                   mSteamUsers.remove(position);
                   updateUi();
               })
               .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void signIn()
    {
        mAuth.signInAnonymously().addOnCompleteListener(this, task ->
        {
            if(task.isSuccessful())
            {
                Log.d(TAG, "signInAnonymously:success");
                fetchDataFromDb();
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

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if(prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = AddSteamUserDialogFragment.newInstance();
        newFragment.show(ft, "dialog");
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
        for(SteamUser account : mSteamUsers)
        {
            usersReference.child(DATABASE_STEAM_ACCOUNTS).child(String.valueOf(account.getSteamId64())).setValue(account.getUsername());
        }
    }

    private void removeAccountFromDb(long steam64Id)
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference usersReference = db.getReference(DATABASE_USERS).child(mAuth.getCurrentUser().getUid());
        usersReference.child(DATABASE_STEAM_ACCOUNTS).child(String.valueOf(steam64Id)).removeValue();
    }

    @SuppressWarnings("ConstantConditions")
    private void fetchDataFromDb()
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
