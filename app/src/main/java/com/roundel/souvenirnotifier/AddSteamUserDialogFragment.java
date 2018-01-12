package com.roundel.souvenirnotifier;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.transition.TransitionManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.roundel.souvenirnotifier.entities.SteamUser;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Created by Krzysiek on 20/07/2017.
 */

public class AddSteamUserDialogFragment extends DialogFragment {
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.text_input_layout)
    TextInputLayout mTextInputLayout;
    @BindView(R.id.edit_text)
    TextInputEditText mTextInputEditText;
    @BindView(R.id.button1)
    Button mPositiveButton;
    @BindView(R.id.button2)
    Button mNegativeButton;
    @BindView(R.id.button3)
    Button mNeutralButton;
    @BindView(R.id.alertTitle)
    TextView mTitle;
    @BindView(R.id.contentPanel)
    LinearLayout mContent;

    private OnUserAddedListener mListener;

    public static AddSteamUserDialogFragment newInstance() {
        return new AddSteamUserDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_steam_dialog, container, false);

        ButterKnife.bind(this, v);

        mTitle.setText("Add Steam account");
        mPositiveButton.setText("Add");
        mNegativeButton.setText("Cancel");
        mNeutralButton.setVisibility(View.GONE);

        mNegativeButton.setOnClickListener(v1 ->
        {
            if (getDialog() != null) {
                getDialog().dismiss();
            }
        });
        mPositiveButton.setOnClickListener(v1 -> checkUser());

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnUserAddedListener) {
            mListener = (OnUserAddedListener) context;
        } else {
            throw new RuntimeException("Activity must implement OnUserAddedListener to use this DialogFragment");
        }
    }

    private void checkUser() {
        String input = mTextInputEditText.getText().toString();
        TransitionManager.beginDelayedTransition(mContent);
        mProgressBar.setVisibility(View.VISIBLE);
        mTextInputLayout.setErrorEnabled(false);
        SteamUser.autoDetect(input, steamUser ->
        {
            TransitionManager.beginDelayedTransition(mContent);
            mProgressBar.setVisibility(View.GONE);
            if (steamUser != null) {
                steamUser.checkInventory(accessible ->
                {
                    if (mListener.onUserAdded(steamUser, accessible)) {
                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }
                    }
                });
            } else {
                mTextInputLayout.setErrorEnabled(true);
                mTextInputLayout.setError("Invalid Steam64ID, Custom ID or url");
            }
        });
    }

    interface OnUserAddedListener {
        boolean onUserAdded(SteamUser user, boolean inventoryAccessible);
    }
}
