package com.example.ant3384.mymapapp_p2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) {
            Log.d("MyMapsApp", "Failed FINE Permission check");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);

        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) {
            Log.d("MyMapsApp", "Failed COARSE Permission check");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == (PackageManager.PERMISSION_GRANTED) || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
            locationSearch = (EditText) findViewById(R.id.editText_addr);


        }
    }
    boolean count = true;

    public void changeView(){
        if(count== true){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            count = false;
            Log.d("changeview", "count "+ count);

        }
        else if(count==false){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            count = true;

    }

    }

    public void onSearch(View v){
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        // use LocationManager for user location info
        LocationManager service = (LocationManager)getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        Log.d("MyMapsApp", "OnSearch: Location = " + location);
        Log.d("MyMapsApp", "OnSearch: Location = " + provider);

        LatLng userLocation = null;
        try {
            //check last known location, specifically list provider (network or GPS)
            if (locationManager!=null){
                if((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                Log.d("MyMapsApp","OnSearch: Using NETWORK_PROVIDER userLocation is: "+ myLocation.getLatitude()+" "+ myLocation.getLongitude());
                Toast.makeText(this, "userloc: "+myLocation.getLatitude()+" "+ myLocation.getLongitude(), Toast.LENGTH_LONG);
                }
                else if((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp","OnSearch: Using GPS_PROVIDER userLocation is: "+ myLocation.getLatitude()+" "+ myLocation.getLongitude());
                    Toast.makeText(this, "userloc: "+myLocation.getLatitude()+" "+ myLocation.getLongitude(), Toast.LENGTH_LONG);
                }
                else {
                    Log.d("MyMapsApp", "Location is null");
                }
        }

    } catch (SecurityException | IllegalArgumentException e){
            Log.d("MyMapsApp", "exception of getLastKnownLocation");
        }
        if (location.matches("")){
            //create geocoder
            Geocoder geocoder = new Geocoder(this, Locale.US);

            try {
                //get a list of addresses
                addressList = geocoder.getFromLocationName(location, 100, userLocation.latitude - (5.0/60.0), userLocation.longitude - (5.0/60.0),userLocation.latitude + (5.0/60.0), userLocation.longitude + (5.0/60.0));
                Log.d("MyMapsApp", "created addressList");

            }catch (IOException e){
                e.printStackTrace();
            }
            if (addressList.isEmpty()){
                Log.d("MyMapsApp", "my address list size: "+ addressList.size());

                for (int i = 0; i<addressList.size(); i++){
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i + ": " + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                }
            }

        }
    }
}
