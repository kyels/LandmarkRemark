package com.test.landmark;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RemarkActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    private String userId;
    private String username;
    private String placeId;
    private String placeName;
    private LatLng placeLatLng;
    private String remark;

    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remark);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle(R.string.title_activity_remark);
        setSupportActionBar(myToolbar);

        // Retrieve landmark details
        Intent intent = getIntent();
        placeId = intent.getStringExtra(getString(R.string.tag_place_id));
        placeLatLng = intent.getParcelableExtra(getString(R.string.tag_lat_lng));
        placeName = intent.getStringExtra(getString(R.string.tag_place_name));

        // Set landmark title
        TextView landmarkTitleView = findViewById(R.id.titleLandmark);
        landmarkTitleView.setText(placeName);

        // Configure text input area checks and action
        final EditText remarkText = (EditText) findViewById(R.id.editRemark);
        remarkText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    remark = remarkText.getText().toString();
                    if(!remark.equals("")) {
                        showDialog();
                    }
                    else {
                        remarkText.setError(getString(R.string.error_no_remark));
                        remarkText.requestFocus();
                    }
                    handled = true;
                }
                return handled;
            }
        });
        // Get reference to our database
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // Confirm user wants to post remark
    void showDialog() {
        DialogFragment newFragment = new CreateRemarkDialogFragment();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public void doPostRemark() {

        // Create remark with details
        RemarkData remarkData = new RemarkData(userId, username, placeId, placeName, remark);
        Map<String, Object> remarkValues = remarkData.toMap();

        // Get push key from /remarks/$remarkid
        String key = mDatabase.child("remarks").push().getKey();

        // Create simultaneous update
        Map<String, Object> childUpdates = new HashMap<>();

        // Create new remark at /remarks/$remarkid
        childUpdates.put("/remarks/" + key, remark);

        // Create remark with details at /user-remarks/$userid/$remarkid
        // and at /landmark-remarks/$landmarkid/$remarkid
        childUpdates.put("/user-remarks/" + userId + "/" + key, remarkValues);
        childUpdates.put("/landmark-remarks/" + placeId + "/" + key, remarkValues);

        // Update landmark's most recent remark /landmark-recent/$landmarkid/
        LandmarkData landmarkData = new LandmarkData(placeLatLng.latitude, placeLatLng.longitude,
                placeName, username + ": " + remark);
        childUpdates.put("/landmark-recent/" + placeId, landmarkData);

        // Ensure landmark's lat lng is at /landmark-geos/$landmarkid/
        childUpdates.put("/landmark-geos/" + placeId + "/lat", placeLatLng.latitude);
        childUpdates.put("/landmark-geos/" + placeId + "/lng", placeLatLng.longitude);

        // Execute updates
        mDatabase.updateChildren(childUpdates);

        // Return to main activity
        startActivity(new Intent(RemarkActivity.this, MainActivity.class));
    }

    public void stopPostRemark() {
        // remain editing remark
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        userId = firebaseAuth.getUid();
        if(userId == null) {
            // no user account, ask user to sign in
            startActivity(new Intent(this, AuthActivity.class));
        }
        else {
            username = firebaseAuth.getCurrentUser().getDisplayName();
        }
    }

    // Dialog to confirm posting remark
    public static class CreateRemarkDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.alert_dialog_remark)
                    .setPositiveButton(R.string.alert_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((RemarkActivity)getActivity()).doPostRemark();
                                }
                            }
                    )
                    .setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((RemarkActivity)getActivity()).stopPostRemark();
                                }
                            }
                    )
                    .create();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // monitor account changes
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // must remove listener when finished
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }
}
