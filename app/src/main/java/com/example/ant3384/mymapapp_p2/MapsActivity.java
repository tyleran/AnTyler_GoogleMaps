package com.example.ant3384.mymapapp_p2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static java.lang.String.valueOf;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;

    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean notTrackingMyLocation = true;

    private boolean gotMyLocationOneTime;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATE = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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


        //Add a marker on the map that shows place of birth
        //displays the message "born here"
        LatLng GrandRapids = new LatLng(42.9634, -85.67);
        mMap.addMarker(new MarkerOptions().position(GrandRapids).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(GrandRapids));

        //   if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) {
        //        Log.d("MyMapsApp", "Failed FINE Permission check");
        //      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);

        //   }
        //    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) {
        //        Log.d("MyMapsApp", "Failed COARSE Permission check");
        //         ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        //    }

        //     if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == (PackageManager.PERMISSION_GRANTED) || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED)) {
        //        mMap.setMyLocationEnabled(true);
        //        locationSearch = (EditText) findViewById(R.id.editText_addr);
//
        gotMyLocationOneTime = false;
        getLocation();


        //   }
    }

    public void changeView(View view) {
        if (mMap.getMapType() != mMap.MAP_TYPE_SATELLITE) {
            mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(mMap.MAP_TYPE_NORMAL);
        }
    }


    public void onSearch(View v) {
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;
        List<Address> addressListZip = null;

        //Use LocationManager for user location
        //Implement the LocationListener interface to setup location services
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);


        Log.d("MyMapsApp","onSearch: location = " + location);
        Log.d("MyMapsApp","onSearch: provider " + provider);

        LatLng userLocation = null;

        //Check the last known location, need to specifically list the provider (network or gps)

        try{
            if(locationManager != null) {
                Log.d("MyMapsApp","onSearch: locationManager is not null");

                if((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp","onSearch: using NETWORK_PROVIDER userLocation is "
                            + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this,"UserLoc " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else if((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp","onSearch: using GPS_PROVIDER userLocation is "
                            + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this,"UserLoc " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else{
                    Log.d("MyMapsApp","onSearch: myLocation is null from getLastKnownLocation");
                }
            } else {
                Log.d("MyMapsApp","onSearch: locationManager is null");
            }
        }
        catch(SecurityException | IllegalArgumentException e){
            Log.d("MyMapsApp","onSearch: Exception getLastKnownLocation");
            Toast.makeText(this,"onSearch: Exception getLastKnownLocation", Toast.LENGTH_SHORT);
        }

        //Get the location if it exists
        if(!location.matches("")){
            Log.d("MyMapsApp","onSearch: location field is populated");
            Geocoder geocoder = new Geocoder(this, Locale.US);
            Log.d("MyMapsApp","onSearch: created Geocoder");
            try{
                //Get a List of the addresses
                addressList = geocoder.getFromLocationName(location, 100,
                        userLocation.latitude - (5.0/60),
                        userLocation.longitude - (5.0/60),
                        userLocation.latitude + (5.0/60),
                        userLocation.longitude + (5.0/60));
                Log.d("MyMapsApp","onSearch: addressList is created");

            }
            catch(IOException e){
                e.printStackTrace();
            }

            if(!addressList.isEmpty()) {
                Log.d("MyMapsApp", "onSearch: addressList size is " + addressList.size());
                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    //Place a marker on the map
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i+ ": " + address.getSubThoroughfare()
                            + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                }
            }
        }
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            //isProvider enabled returns true if user has enabled gps
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                Log.d("MyMapsApp", "getLocation: GPS is enabled");
            }

            //get network status
            //isProvider enabled returns true if user has enabled network
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) {
                Log.d("MyMapsApp", "getLocation: NETWORK is enabled");
            }

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("MyMapsApp", "getLocation: No provider");
            } else {
                if (isNetworkEnabled) {
                    if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                            (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListenerNetwork);
                }
                if (isGPSEnabled) {
                    //launch locationListenerGps
                    // code here
                    if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                            (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListenerGPS);
                }

            }
        } catch (Exception e) {
            Log.d("MyMapsApp", "getLocation: caught exception");
            e.printStackTrace();
        }
    }

    //LocationListener is an anonymous inner class
    //Setup for callbacks from the requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            dropAMarker(LocationManager.NETWORK_PROVIDER);

            //check if doing one time via onMapReady, if so remove updates to both gps and network
            if (gotMyLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
                gotMyLocationOneTime = true;
            } else {
                if ((ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListenerNetwork);
            }
            //if here then tracking so relaunch request for network

        }

        @Override
        public void onStatusChanged(String provider, int i, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerNetwork: status change");

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAMarker(LocationManager.GPS_PROVIDER);
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerGPS);
        }

        @Override
        public void onStatusChanged(String provider, int i, Bundle bundle) {
//switch(1)
            // case LocationProvider.Available
            // printout.log.d and toast message
            //caseLocation.OUT_OF_SERVICE
            //enable network updates
            // break
            //case LocationProvider.Temporarily_UNAVAILABLE
            //enable both network and GPS
            // break
            // default
            // enable both network and gps
            Log.d("MyMapsApp", "locationListenerNetwork: status change");
            Toast.makeText(MapsActivity.this, "status change", Toast.LENGTH_LONG).show();
            switch (i) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMapsApp", "locationListenerNetwork: GPS available");
                    Toast.makeText(MapsActivity.this, "location provider available", Toast.LENGTH_LONG).show();
                    break;

                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMapsApp", "locationListenerNetwork: GPS out of service");
                    Toast.makeText(MapsActivity.this, "status change", Toast.LENGTH_LONG).show();
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListenerNetwork);

                    break;

                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListenerNetwork);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListenerNetwork);
                    break;
                default:


            }
        }


        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }


    };


    public void dropAMarker(String provider) {
        //if (locationManager!= null)
        // if (checkSelfPermission fails)
        // return
        //myLocation = locationManager.getLastKnownLocation(provider)
        //LatLng userLocation
        // if (myLocation == null) print log or toast message
        //else
        //userLocation = new LatLng(myLocation.getLatitude(), nyLocation.getLongitude());
        // CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM)
        // if (provider == LocationManager.GPS_PROVIDER)
        //  add circle for the marker with two outer rings (red)
        // mMap.addCircle(newCircleOptions())
        //.center(userLocation)
        //.radius(1)
        //.strokeColor(Color.red)
        //.strokeWidth(2)
        //.fillColor(Color.RED)
        //else add circle for the for the marker with two outer rings blue
        //mMap.animateCamera(update)
        if (locationManager != null) {
            if ((ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                return;
            } else {
                myLocation = locationManager.getLastKnownLocation(provider);
            }
            LatLng userLocation = null;

            if (myLocation == null) {
                Log.d("MyMapsApp", "dropAmarker: myLocation is null");
            } else {
                Log.d("MyMapsApp", "dropAmarker: WORKS");

                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
                if (provider == LocationManager.GPS_PROVIDER) {
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(1)
                            .strokeColor(Color.RED)
                            .strokeWidth(2)
                            .fillColor(Color.RED)
                    );
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(3)
                            .strokeColor(Color.RED)
                            .strokeWidth(2)
                            .fillColor(Color.TRANSPARENT)
                    );
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(5)
                            .strokeColor(Color.RED)
                            .strokeWidth(2)
                            .fillColor(Color.TRANSPARENT)
                    );
                } else {
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(1)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2)
                            .fillColor(Color.BLUE)
                    );
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(3)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2)
                            .fillColor(Color.TRANSPARENT)
                    );
                    mMap.addCircle(new CircleOptions()
                            .center(userLocation)
                            .radius(5)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2)
                            .fillColor(Color.TRANSPARENT)
                    );
                    mMap.animateCamera(update);
                }
            }
        }
    }


        public void trackMyLocation (View view){
            //kick off the location tracker using getLocation to start the LocationListener
            //if (notTrackingmyLocation) (getLocation(); notTrackingmyLocation = false;)
            // else (removeUpdates for both networks and gps, notTrackingmyLocation = true
            if (notTrackingMyLocation) {
                getLocation();
                notTrackingMyLocation = false;
                Log.d("MyMapsApp", "trackMyLocation: tracking location");

            } else {
                //removeUpdates for both network and gps; n
                locationManager.removeUpdates(locationListenerNetwork);
                locationManager.removeUpdates(locationListenerGPS);
                notTrackingMyLocation = true;
                Log.d("MyMapsApp", "trackMyLocation: not tracking location + disabled gps + network");

            }

        }

        public void clear (View v){
            mMap.clear();
        }

    }








