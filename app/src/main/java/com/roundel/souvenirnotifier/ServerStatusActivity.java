package com.roundel.souvenirnotifier;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roundel.souvenirnotifier.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ServerStatusActivity extends AppCompatActivity {
    @BindView(R.id.server_online)
    TextView mServerOnline;
    @BindView(R.id.server_running)
    TextView mServerRunning;
    @BindView(R.id.control_button)
    Button mControlButton;
    @BindView(R.id.server_rc_status)
    TextView mServerRcStatus;

    private final View.OnClickListener mToggleServer = v -> toggleServerState();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_status);

        ButterKnife.bind(this);

        mControlButton.setOnClickListener(mToggleServer);

        FirebaseDatabase database = Utils.getDatabase();
        database.getReference(MainActivity.DATABASE_CONFIG)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            Boolean online = (Boolean) dataSnapshot
                                    .child(MainActivity.DATABASE_SERVER_ONLINE).getValue();
                            Boolean running = (Boolean) dataSnapshot
                                    .child(MainActivity.DATABASE_SERVER_RUNNING).getValue();
                            Boolean rcEnabled = (Boolean) dataSnapshot
                                    .child(MainActivity.DATABASE_SERVER_RC_ENABLED).getValue();

                            rcEnabled = rcEnabled != null ? rcEnabled : false;

                            if (online != null && running != null) {
                                updateUi(online, running, rcEnabled);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void updateUi(boolean serverOnline, boolean serverRunning, boolean serverRc) {
        if (serverOnline) {
            mControlButton.setEnabled(serverRc);
            mServerOnline.setText("Online");
            mServerOnline.setTextColor(getColor(R.color.on));
        } else {
            mControlButton.setEnabled(false);
            mServerOnline.setText("Offline");
            mServerOnline.setTextColor(getColor(R.color.off));
        }
        if (serverRunning) {
            mControlButton.setText("Stop server");
            mServerRunning.setText("Running");
            mServerRunning.setTextColor(getColor(R.color.on));
        } else {
            mControlButton.setText("Start server");
            mServerRunning.setText("Not running");
            mServerRunning.setTextColor(getColor(R.color.off));
        }
        mServerRcStatus.setVisibility(!serverRc && serverOnline ? View.VISIBLE : View.GONE);
    }

    private void toggleServerState() {
        final FirebaseDatabase database = Utils.getDatabase();
        database.getReference(MainActivity.DATABASE_CONFIG)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            Boolean online = (Boolean) dataSnapshot
                                    .child(MainActivity.DATABASE_SERVER_ONLINE).getValue();
                            Boolean running = (Boolean) dataSnapshot
                                    .child(MainActivity.DATABASE_SERVER_RUNNING).getValue();

                            if (online != null && running != null) {
                                if (online) {
                                    database.getReference(MainActivity.DATABASE_CONFIG)
                                            .child(MainActivity.DATABASE_SERVER_TRIGGER).setValue(!running);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
