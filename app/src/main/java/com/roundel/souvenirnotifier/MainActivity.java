package com.roundel.souvenirnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
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
import android.view.Menu;
import android.view.MenuItem;
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
import com.roundel.souvenirnotifier.utils.Connectivity;
import com.roundel.souvenirnotifier.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements AddSteamUserDialogFragment.OnUserAddedListener, SteamUsersAdapter.OnItemLongClickListener
{
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String DATABASE_DATA = "data";
    public static final String DATABASE_CONFIG = "config";
    public static final String DATABASE_SERVER_RUNNING = "server_running";
    public static final String DATABASE_SERVER_ONLINE = "server_online";
    public static final String DATABASE_SERVER_TRIGGER = "server_trigger";
    public static final String DATABASE_SERVER_RC_ENABLED = "server_remote_control_enabled";
    public static final String DATABASE_USERS = DATABASE_DATA + "/users";
    public static final String DATABASE_TOKEN = "token";
    public static final String DATABASE_STEAM_ACCOUNTS = "steamAccounts";

    private final ConnectivityChangeBroadcastReceiver mConnectivityBroadcastReceiver =
            new ConnectivityChangeBroadcastReceiver();
    @BindView(R.id.floatingActionButton) FloatingActionButton mFab;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    private SteamUsersAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseAuth mAuth;
    private List<SteamUser> mSteamUsers = new ArrayList<>();
    private String mToken;
    private boolean mHasInternetAccess;
    private MenuItem mStartServerItem;

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
        mAdapter.setOnItemLongClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(mLayoutManager);

        mFab.setOnClickListener(v ->
        {
            if(mHasInternetAccess && Connectivity.isConnected(this))
                showAddUserDialog();
            else
                Toast.makeText(this, "You need to have internet access to add a Steam account", Toast.LENGTH_SHORT).show();
        });

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectivityBroadcastReceiver, filter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mConnectivityBroadcastReceiver);
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
            updateMenuItem();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        checkInternetAccess(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mStartServerItem = menu.findItem(R.id.start_server_status);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.start_server_status:
            {
                Intent intent = new Intent(this, ServerStatusActivity.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
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

    private void checkInternetAccess()
    {
        checkInternetAccess(false);
    }

    private void checkInternetAccess(boolean notify)
    {
        Connectivity.hasAccess(new Connectivity.onHasAccessResponse()
        {
            @Override
            public void onConnectionCheckStart()
            {

            }

            @Override
            public void onConnectionAvailable(Long responseTime)
            {
                mHasInternetAccess = true;
            }

            @Override
            public void onConnectionUnavailable()
            {
                mHasInternetAccess = false;
                if(notify)
                {
                    Toast.makeText(
                            MainActivity.this,
                            "Internet is not available, your accounts may not appear",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    private void signIn()
    {
        mAuth.signInAnonymously().addOnCompleteListener(this, task ->
        {
            if(task.isSuccessful())
            {
                Log.d(TAG, "signInAnonymously:success");
                fetchDataFromDb();
                updateMenuItem();
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
        if(steamUser == null)
        {
            throw new IllegalStateException("steamUser is null");
        }
        if(!mSteamUsers.contains(steamUser))
        {
            mSteamUsers.add(steamUser);
            saveData();
            updateUi();
        }
        else if(notify)
        {
            updateUser(steamUser);
            Toast.makeText(this, "This Steam account is already in the list", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUser(SteamUser steamUser)
    {
        int updatedPosition = mSteamUsers.indexOf(steamUser);
        if(updatedPosition != -1)
        {
            mAdapter.notifyItemChanged(updatedPosition);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void saveData()
    {
        FirebaseDatabase db = Utils.getDatabase();
        DatabaseReference usersReference = db.getReference(DATABASE_USERS).child(mAuth.getCurrentUser().getUid());
        usersReference.child(DATABASE_TOKEN).setValue(mToken);
        for(SteamUser account : mSteamUsers)
        {
            usersReference.child(DATABASE_STEAM_ACCOUNTS)
                          .child(String.valueOf(account.getSteamId64()))
                          .setValue(account.getUsername());
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void removeAccountFromDb(long steam64Id)
    {
        FirebaseDatabase db = Utils.getDatabase();
        DatabaseReference usersReference = db.getReference(DATABASE_USERS).child(mAuth.getCurrentUser().getUid());
        usersReference.child(DATABASE_STEAM_ACCOUNTS).child(String.valueOf(steam64Id)).removeValue();
    }

    @SuppressWarnings("ConstantConditions")
    private void fetchDataFromDb()
    {
        FirebaseDatabase db = Utils.getDatabase();
        DatabaseReference ref = db.getReference(DATABASE_USERS).child(mAuth.getCurrentUser().getUid());
        ref.keepSynced(true);
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
                        if(snapshot.getKey() != null && snapshot.getValue() != null)
                        {

                            addUser(new SteamUser(
                                    Long.parseLong(snapshot.getKey()),
                                    (String) snapshot.getValue()
                            ), false);
                            SteamUser.fromSteamId64(Long.parseLong(snapshot.getKey()), steamUser ->
                            {
                                if(steamUser != null)
                                {
                                    updateUser(steamUser);
                                }

                            });
                        }
                    }
                    updateUi();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void updateMenuItem()
    {
        FirebaseDatabase database = Utils.getDatabase();
        database.getReference(MainActivity.DATABASE_CONFIG).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(mStartServerItem != null)
                {
                    mStartServerItem.setVisible(dataSnapshot != null);
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

    class ConnectivityChangeBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, intent.getExtras().toString());
            checkInternetAccess();
        }
    }
}
