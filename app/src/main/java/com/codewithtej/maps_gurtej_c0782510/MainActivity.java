package com.codewithtej.maps_gurtej_c0782510;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    SupportMapFragment smf;
    FusedLocationProviderClient client;
    private GoogleMap myMap;
    private Polygon shape;
    List<Marker> markers = new ArrayList();
    ArrayList<Float> arr = new ArrayList();
    Map<Marker,Float> nearMarker = new TreeMap<Marker,Float>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smf = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        client = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},101);
        }

    }

    private void getCurrentLocation() {
        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if(location != null){
                    smf.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            myMap = googleMap;
                            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                            MarkerOptions options = new MarkerOptions()
                                    .position(latLng)
                                    .title("Here I am");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,9));
                            googleMap.addMarker(options);

                            myMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){

                                @Override
                                public void onMapLongClick(LatLng latLng) {
                                    addMarker(latLng);
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void addMarker(LatLng latLng) {
        String title;
        if (markers.size() == 0){
            title = "A";
        }
        else if (markers.size() == 1){
            title = "B";
        }
        else{
            title = "C";
        }
        MarkerOptions option  = new MarkerOptions().position(latLng).title(title);
        if(1 == 0){
        //    findNearestMarker(latLng);
        }
        else{
            if (markers.size() != 0){
           //     findNearestMarker(latLng);
            }
            if(markers.size() < 3){
                markers.add(myMap.addMarker(option));
                if(markers.size() == 3)
                    drawTriangle();

            }
            else{
                for (Marker marker: markers)
                    marker.remove();
                markers.clear();
                shape.remove();
                shape = null;
                addMarker(latLng);
            }
        }
    }

    private void drawTriangle() {
        PolygonOptions option = new PolygonOptions()
                .fillColor(0x80FF0000)
                .strokeColor(Color.GREEN)
                .strokeWidth(5);
        for (int i=0; i<3; i++) {
            option.add(markers.get(i).getPosition());
        }

        shape = myMap.addPolygon(option);
    }
//    Marker findNearestMarker(LatLng position){
//        int smallestDistance = 6000;
//        for(Marker mark : markers){
//            float[] distance = new float[1];
//            LatLng markerPosition = mark.getPosition();
//            Location.distanceBetween(markerPosition.latitude,markerPosition.longitude,position.latitude,position.longitude,distance);
//            arr.add(distance[0]);
//            Log.e("Fish","--"+distance[0]);
//            nearMarker.put(mark,distance[0]);
//        }
//        Collections.sort(arr);
//
//        return  arr[0];
//    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }

}