package com.example.rclocator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import static android.view.View.VISIBLE;

public class Map_MainPage extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPoiClickListener {

    private GoogleMap mMap;

    // Saved Strings to save re-writing the long classification
    private static final String TAG = "Map_MainPage";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;

    // Variables
    private Boolean mLocationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Place mPlaceInfo;
    private Marker mMarker;
    public LatLng latLng;

    // Widgets
    private EditText mSearchText;
    private ImageView mGPS, mInfo;
    private ImageView mWeather;
    private ImageView mLogout;
    private RelativeLayout mBottomLayout;
    private TextView POI_Name;
    private TextView POI_Address;
    private TextView POI_phone_number;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialising all of the programatically edited fields
        setContentView(R.layout.activity_map__main_page);
        mSearchText = (EditText) findViewById(R.id.input_search);
        mGPS = (ImageView) findViewById(R.id.ic_centerLocation);
        mInfo = (ImageView) findViewById(R.id.placeInfo);
        mWeather = (ImageView) findViewById(R.id.weatherInfo);
        mLogout = (ImageView) findViewById(R.id.logout);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_info_bar);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.api_key));
        }
        PlacesClient placesClient = Places.createClient(getApplicationContext());
        getLocationPermissions();
        initialiseUIElements();
    }

    private void init() {
        Log.d(TAG, "init: Initializing");
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    // Execute  method for searching
                    geoLocate();
                }
                return false;
            }
        });
        // This is to add functionality to the Recenter icon. It will send the camera back to the current location
        mGPS.setOnClickListener(view -> {
            Log.d(TAG, "onClick: Clicked GPS icon");
            getDeviceLocation();
        });
        mInfo.setOnClickListener(v -> {
            setToVisible();
            //updateUIElements();

            if (mBottomLayout.getVisibility() == VISIBLE) {
                mBottomLayout.setVisibility(View.INVISIBLE);
            }
        });
        mWeather.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), weatherActivity.class);
            intent.putExtra("Lat", latLng.latitude);
            intent.putExtra("Lon", latLng.longitude);
            startActivity(intent);
        });
        mLogout.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        });
        // Method call to hide the phones keyboard
        hideSoftKeyboard();
    }

    private void geoLocate() {
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(Map_MainPage.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IO Exception: " + e.getMessage());
        }
        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: Found a location: " + address.toString());
            Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }
    }

    public void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Found Location!");
                        Location currentLocation = (Location) task.getResult();
                        latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        moveCamera(latLng, DEFAULT_ZOOM, "My Location");
                    } else {
                        Log.d(TAG, "onComplete: Current Location is null");
                        Toast.makeText(Map_MainPage.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: Security Exception: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latlng, float zoom, String title) {
        Log.d(TAG, "moveCamera: Moving the camera to: Lat: " + latlng.latitude + ", lng: " + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

        // Code for dropping a marker
        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latlng)
                    .title(title);
            mMarker = mMap.addMarker(options);
        }
        // This hides the keyboard after every search
        hideSoftKeyboard();
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(Map_MainPage.this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnPoiClickListener(this);

        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success) {
                Log.e(TAG, "onMapReady: Style parsing failed");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "onMapReady: Cant find style. Error: ", e);
        }

        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            //Disables the re-centre button as search bar will hide the button. Will be adding a custom one.
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            // This call for the init method allows the search bar to do something, called here after the map is initialized
            init();
            Log.d(TAG, "onMapReady: Init has been called.");
        }
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID,
                Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS,
                Place.Field.PHONE_NUMBER));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                hideSoftKeyboard();
                mPlaceInfo = place;
                moveCamera(place.getLatLng(), DEFAULT_ZOOM, place.getName());
                setToVisible();
                // Setting the text element the Text Views
                POI_Name.setText("Name: " + mPlaceInfo.getName());
                POI_Address.setText("Address: " + mPlaceInfo.getAddress());
                POI_phone_number.setText("Phone Number: " + mPlaceInfo.getPhoneNumber());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(Map_MainPage.this, "Error on place selection, please try again", Toast.LENGTH_SHORT).show();
            }
        });
        mBottomLayout.setOnClickListener(v -> {
            if (mBottomLayout.getVisibility() == VISIBLE) {
                mBottomLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void onPoiClick(PointOfInterest poi) {
        setToVisible();
        Toast.makeText(getApplicationContext(), "PlaceID?" + poi.placeId, Toast.LENGTH_SHORT).show();
        Marker POI = mMap.addMarker(new MarkerOptions()
                .position(poi.latLng)
                .title(poi.name));
        POI.showInfoWindow();
    }

    // Getting the device location permissions
    private void getLocationPermissions() {
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    // Initialize our map
                }
            }
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void setToVisible() {
        mBottomLayout.setVisibility(View.INVISIBLE);
        mBottomLayout.setVisibility(View.VISIBLE);
        Toast.makeText(Map_MainPage.this, "set to visible has been called", Toast.LENGTH_SHORT).show();
    }

    private void initialiseUIElements() {
        // Linking the POI layout views
        POI_Name = (TextView) findViewById(R.id.POI_name);
        POI_Address = (TextView) findViewById(R.id.POI_Address);
        POI_phone_number = (TextView) findViewById(R.id.POI_phone_number);
    }
}