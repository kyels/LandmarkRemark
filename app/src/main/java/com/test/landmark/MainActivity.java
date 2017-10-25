package com.test.landmark;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        FirebaseAuth.AuthStateListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabase;

    private Location mLastLocation;
    private String userId;
    private Index algoliaIndex;

    private final int PERMISSIONS_REQUEST_UPDATE_LOCATION = 100;
    private final int PERMISSIONS_REQUEST_MY_LOCATION = 101;
    private final int PLACE_REQUEST_LOCAL = 102;
    private final float DEFAULT_MAP_ZOOM = 14;
    private final double DEFAULT_LATITUDE = -37.8139949;
    private final double DEFAULT_LONGITUDE = 144.9739887;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle(R.string.title_activity_main);
        setSupportActionBar(myToolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Get reference to our database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get reference to our search index
        Client client = new Client(getString(R.string.algolia_application_id), getString(R.string.algolia_search_only_api_key));
        algoliaIndex = client.getIndex("remarks");

        // Configure search input area checks and action
        final EditText searchText = (EditText) findViewById(R.id.searchText);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String search = searchText.getText().toString();
                    if(!search.equals("")) {
                        searchRemarks(search);
                    }
                    handled = true;
                }
                return handled;
            }
        });
    }

    // Perform search
    private void searchRemarks(String search) {
        com.algolia.search.saas.Query query = new com.algolia.search.saas.Query(search)
                .setAttributesToRetrieve("remarks", "username")
                .setHitsPerPage(5);
        algoliaIndex.searchAsync(query, new CompletionHandler() {
            @Override
            public void requestCompleted(JSONObject content, AlgoliaException error) {
                // TODO return results
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        // Set my location on map
        setMyLocation();
        // Set map listeners
        mMap.setOnCameraIdleListener(this);
        mMap.setOnInfoWindowClickListener(this);

    }

    private void updateRemarkPins(LatLngBounds latLngBounds) {

        // Retrieve 100 most recent pins inside lat lng bounds
        Query myLandmarksQuery = mDatabase.child("landmark-recent").orderByKey(); //TODO add filter
        myLandmarksQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot landmarkSnapshot: dataSnapshot.getChildren()) {
                    LandmarkData landmarkData = landmarkSnapshot.getValue(LandmarkData.class);
                    mMap.addMarker(new MarkerOptions().position(landmarkData.getLatLng())
                            .title(landmarkData.landmarkName).snippet(landmarkData.lastRemark));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d("loadPost:onCancelled", databaseError.toException().getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.navigation_my_remarks:
                // TODO show pins with my remarks
                return true;
            case R.id.navigation_sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                startActivity(new Intent(MainActivity.this, AuthActivity.class));
                                finish();
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getLandmark(View view) {
        // Enable picker to choose landmark
        PlacePicker.IntentBuilder picker = new PlacePicker.IntentBuilder();
        picker.setLatLngBounds(LatLngBounds.builder().include(mMap.getCameraPosition().target).build());
        try {
            startActivityForResult(picker.build(this), PLACE_REQUEST_LOCAL);
        } catch (GooglePlayServicesRepairableException e) {
            startRemarkActivity(null);
        } catch (GooglePlayServicesNotAvailableException e) {
            startRemarkActivity(null);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // When a user has selected a place or position
        if (requestCode == PLACE_REQUEST_LOCAL) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                startRemarkActivity(place);
            }
        }
    }

    // pass landmark details to remark activity
    private void startRemarkActivity(Place place) {
        Intent intent = new Intent(MainActivity.this, RemarkActivity.class);
        if(place != null) {
            intent.putExtra(getString(R.string.tag_place_id), place.getId());
            intent.putExtra(getString(R.string.tag_lat_lng), place.getLatLng());
            intent.putExtra(getString(R.string.tag_place_name), place.getName());
            startActivity(intent);
        }
    }

    // updates mLastLocation with best effort location
    private void updateLocation() {
        // check if we can access fine location
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // if denied, request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_UPDATE_LOCATION);
        }
        // if yes, then update location
        else {
            Location updatedLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (updatedLocation != null) {
                mLastLocation = updatedLocation;
            }
            // if last location isn't available
            else {
                if(mLastLocation == null) {
                    mLastLocation = getDefaultLocation();
                }
            }
        }
    }

    // show blue dot of current location
    private void setMyLocation() {
        // check if we can access fine location
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // if denied, request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_MY_LOCATION);
        }
        else {
            // enable my location in map
            mMap.setMyLocationEnabled(true);
            // focus map on my location
            Location updatedLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(updatedLocation != null) {
                focusMapCamera(new LatLng(updatedLocation.getLatitude(),
                        updatedLocation.getLongitude()));
            }
            else {
                // if no last location, set default
                focusMapCamera(new LatLng(getDefaultLocation().getLatitude(), getDefaultLocation().getLongitude()));
            }
        }
    }

    // Focus camera to position on map
    private void focusMapCamera(LatLng position) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, DEFAULT_MAP_ZOOM);
        mMap.animateCamera(cameraUpdate);
    }

    // Default location used to centre maps if location unavailable
    private Location getDefaultLocation() {
        Location defaultLocation = new Location(getString(R.string.location_provider_name_default));
        defaultLocation.setLatitude(DEFAULT_LATITUDE);
        defaultLocation.setLongitude(DEFAULT_LONGITUDE);
        return defaultLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_UPDATE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // try updating location again
                    updateLocation();

                } else {
                    if(mLastLocation == null) {
                        mLastLocation = getDefaultLocation();
                    }
                }
                return;
            }
            case PERMISSIONS_REQUEST_MY_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // try setting my location again
                    setMyLocation();

                } else {
                    // set default location
                    focusMapCamera(new LatLng(getDefaultLocation().getLatitude(), getDefaultLocation().getLongitude()));
                }
                return;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d("GoogleAPI", connectionResult.getErrorMessage());

    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        userId = firebaseAuth.getUid();
        if(userId == null) {
            // no user account, ask user to sign in
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        // monitor account changes
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        // must remove listener when finished
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    @Override
    public void onCameraIdle() {
        // update pins inside camera view
        updateRemarkPins(null);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // TODO Show additional remarks
    }
}
