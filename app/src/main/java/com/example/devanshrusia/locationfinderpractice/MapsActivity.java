package com.example.devanshrusia.locationfinderpractice;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    public static String latConstant = "lat";
    public static String longConstant = "long";
    public static String accuracyConstant = "accuracy";

    public double lat;
    public double lng;
    public double accuracy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void setupStaticPoints(GoogleMap mMap) {
        CircleOptions eZone = new CircleOptions().center(new LatLng(12.933799, 77.623751)).radius(15).strokeColor(Color.BLUE)
                .fillColor(Color.BLUE);

        CircleOptions ccd = new CircleOptions().center(new LatLng(12.932534, 77.623174)).radius(15).strokeColor(Color.YELLOW)
                .fillColor(Color.YELLOW);

        mMap.addCircle(eZone);
        mMap.addCircle(ccd);
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

        setupStaticPoints(mMap);

//        Log.i("LocFinder", "Inside map ready");
//        double workLat = 0.0000;
//        double workLong = 0.0000;
        if (getIntent() != null) {
//            Log.i("LocFinder", "Intent not null");
            Intent intent = getIntent();
            lat = intent.getDoubleExtra(latConstant, 0.0000);
            lng = intent.getDoubleExtra(longConstant, 0.0000);
            accuracy = intent.getIntExtra(accuracyConstant,0);

//            Log.i("LocFinder", "Lat : " + workLat + " long : " + workLong);
        }
        // Add a marker in Sydney and move the camera
        LatLng userLatLong = new LatLng(lat, lng);

        CircleOptions circleOptions = new CircleOptions();
        mMap.addMarker(new MarkerOptions().position(userLatLong).title("Your location with accuracy:" + accuracy));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLong, 17.0f));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(workLat, workLong), 17.0f));
        mMap.getUiSettings().setZoomGesturesEnabled(true);
    }

    public void fetchWifiData(View view) {
        Log.i("LocFinder", "Inside func");
        Intent intent = new Intent(this, WifiActivity.class);
        intent.putExtra(MapsActivity.latConstant, lat);
        intent.putExtra(MapsActivity.longConstant, lng);
        intent.putExtra(MapsActivity.accuracyConstant,accuracy);
        startActivity(intent);
    }
}
