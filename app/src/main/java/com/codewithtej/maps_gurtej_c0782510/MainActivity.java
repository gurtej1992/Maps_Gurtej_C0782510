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
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    SupportMapFragment smf;
    FusedLocationProviderClient client;
    private GoogleMap myMap;
    private Polygon shape;

    int smallestDistance = 6000;
    List<Marker> markers = new ArrayList();
    List<Float> arr = new ArrayList();
    Map<Float,Marker> nearMarker = new HashMap<>();
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
                                    .title("Here I am")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.userlocation));
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
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("xx",""+e.getLocalizedMessage());
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
        else if (markers.size() == 2){
            title = "C";
        }
        else{
            title = "D";
        }
        MarkerOptions option  = new MarkerOptions().position(latLng).title(title);
        if(1 == 0){
        //    findNearestMarker(latLng);
        }
        else{
            if (markers.size() != 0){
                findNearestMarker(latLng);
                if(smallestDistance >= arr.get(0)){
                    Marker near = nearMarker.get(arr.get(0));
                    near.remove();
                    for (int i=0; i<markers.size(); i++) {
                        if(markers.get(i) == near){
                            markers.get(i).remove();
                            markers.remove(i);
                            if(shape != null){
                                shape.remove();
                                shape = null;
                            }
                        }
                    }
                    return;
                }

            }
            if(markers.size() < 4){
                markers.add(myMap.addMarker(option));
                if(markers.size() == 4)
                    drawQuadrilateral();

            }
            else{
                for (Marker marker: markers)
                    marker.remove();
                markers.clear();
                if(shape != null){
                    shape.remove();
                    shape = null;
                }

                addMarker(latLng);
            }
        }
    }

    private void drawQuadrilateral() {
        PolygonOptions option = new PolygonOptions()
                .fillColor(0x5900FF00)
                .strokeColor(Color.RED)
                .strokeWidth(5);
        for (int i=0; i<4; i++) {
            option.add(markers.get(i).getPosition());
        }

        shape = myMap.addPolygon(option);
    }
    void findNearestMarker(LatLng position){
        nearMarker.clear();
        arr.clear();
        for(Marker mark : markers){
            float[] distance = new float[1];
            LatLng markerPosition = mark.getPosition();
            Location.distanceBetween(markerPosition.latitude,markerPosition.longitude,position.latitude,position.longitude,distance);
            nearMarker.put(distance[0],mark);
            arr.add(distance[0]);
        }
        Collections.sort(arr);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
            else{
                Toast.makeText(this, "This is my Toast message!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

}