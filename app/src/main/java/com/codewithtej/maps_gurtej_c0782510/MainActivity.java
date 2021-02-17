package com.codewithtej.maps_gurtej_c0782510;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener,GoogleMap.OnMarkerDragListener,GoogleMap.OnPolygonClickListener, GoogleMap.OnPolylineClickListener {
    SupportMapFragment smf;
    FusedLocationProviderClient client;
    private GoogleMap myMap;
    private Polyline shape;
    private Polygon shape2;
    Location userLocation;
    int smallestDistance = 10000;
    ArrayList<Marker> markers = new ArrayList();
    ArrayList<Float> arr = new ArrayList();
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
// TO Get current location of user
    private void getCurrentLocation() {
        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if(location != null){
                    userLocation = location;

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
// Adding marker on long press
    private void addMarker(LatLng latLng) {
        myMap.setOnMarkerClickListener(this);
        myMap.setOnMarkerDragListener(this);
        myMap.setOnPolygonClickListener(this);
        myMap.setOnPolylineClickListener(this);
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
        float[] distance = new float[1];
        Location.distanceBetween(latLng.latitude,latLng.longitude,userLocation.getLatitude(),userLocation.getLongitude(),distance);
        double dis = Math.round((distance[0]/1000) * 100.0) / 100.0;
        MarkerOptions option  = new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet("Distance from user : "+dis + " kms.")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .draggable(true);

            if (markers.size() != 0){
                findNearestMarker(latLng);
                if(smallestDistance >= arr.get(0)){
                    Marker near = nearMarker.get(arr.get(0));
                    assert near != null;
                    near.remove();
                    for (int i=0; i<markers.size(); i++) {
                        if(markers.get(i) == near){
                            markers.get(i).remove();
                            markers.remove(i);
                            if(shape != null){
                                shape.remove();
                                shape = null;
                                shape2.remove();
                                shape2 = null;
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
                    shape2.remove();
                    shape2 = null;
                }

                addMarker(latLng);
            }
        }
// Drawing shape
    private void drawQuadrilateral() {
        PolylineOptions options = new PolylineOptions()
                .clickable(true);
        for (int i=0; i<5; i++) {
            if(i==4){
                options.add(markers.get(0).getPosition());
            }
            else{
                options.add(markers.get(i).getPosition());
            }
        }
        shape = myMap.addPolyline(options);
        shape.setColor(Color.RED);
        shape.setWidth(15);
        PolygonOptions option = new PolygonOptions()
                .fillColor(0x5900FF00)
                .strokeWidth(0)
                .clickable(true);

        for (int i=0; i<4; i++) {
            option.add(markers.get(i).getPosition());
            Log.e("___>", ""+ markers.get(i).getPosition());
        }
        shape2 = myMap.addPolygon(option);
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
    // Getting address for given coordinates
    String retrieveAddress(LatLng coordinate){
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        String completeAddress;
        try {
            addresses = geocoder.getFromLocation(coordinate.latitude, coordinate.longitude, 1);
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String postalCode = addresses.get(0).getPostalCode();
            completeAddress = address + ", "+ city+", "+state+", Postal Code :- "+postalCode;
            //String knownName = addresses.get(0).getFeatureName();// Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            completeAddress = "Not provided";
            e.printStackTrace();
        }
        return completeAddress;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String add = retrieveAddress(marker.getPosition());
        Toast.makeText(this, add,
                Toast.LENGTH_SHORT).show();
//        Log.e("marker",marker.getSnippet());
    return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        markers.remove(marker);
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        markers.add(marker);
        if(shape != null){
            shape.remove();
            shape = null;
            shape2.remove();
            shape2 = null;
        }
        if(markers.size() == 4){
            drawQuadrilateral();
        }

    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        if(markers.size() == 4){
            List<LatLng> locations = polygon.getPoints();
            float[] distanceAB = new float[1];
            float[] distanceBC = new float[1];
            float[] distanceCD = new float[1];
            float[] distanceDA = new float[1];
            Location.distanceBetween(locations.get(0).latitude,locations.get(0).longitude,locations.get(1).latitude,locations.get(1).longitude,distanceAB);
            Location.distanceBetween(locations.get(1).latitude,locations.get(1).longitude,locations.get(2).latitude,locations.get(2).longitude,distanceBC);
            Location.distanceBetween(locations.get(2).latitude,locations.get(2).longitude,locations.get(3).latitude,locations.get(3).longitude,distanceCD);
            Location.distanceBetween(locations.get(3).latitude,locations.get(3).longitude,locations.get(1).latitude,locations.get(1).longitude,distanceDA);
            float totalDistance = distanceAB[0] + distanceBC[0] + distanceCD[0] + distanceDA[0];
            double dis = Math.round((totalDistance/1000) * 100.0) / 100.0;
            Toast.makeText(this, "Total Distance From all four points is "+dis+ " kms",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        List<LatLng> locations = polyline.getPoints();
        float[] distanceAB = new float[1];
        float[] distanceBC = new float[1];
        float[] distanceCD = new float[1];
        float[] distanceDA = new float[1];
        Location.distanceBetween(locations.get(0).latitude,locations.get(0).longitude,locations.get(1).latitude,locations.get(1).longitude,distanceAB);
        Location.distanceBetween(locations.get(1).latitude,locations.get(1).longitude,locations.get(2).latitude,locations.get(2).longitude,distanceBC);
        Location.distanceBetween(locations.get(2).latitude,locations.get(2).longitude,locations.get(3).latitude,locations.get(3).longitude,distanceCD);
        Location.distanceBetween(locations.get(3).latitude,locations.get(3).longitude,locations.get(1).latitude,locations.get(1).longitude,distanceDA);
        double dis1 = Math.round((distanceAB[0]/1000) * 100.0) / 100.0;
        double dis2 = Math.round((distanceBC[0]/1000) * 100.0) / 100.0;
        double dis3 = Math.round((distanceCD[0]/1000) * 100.0) / 100.0;
        double dis4 = Math.round((distanceDA[0]/1000) * 100.0) / 100.0;
        Toast.makeText(this, "A to B " + dis1 + " in kms \nB to C "
                        + dis2 + " in kms \nC to D "
                        + dis3 + " in kms \nD to A "
                        + dis4 + " in kms",
                Toast.LENGTH_LONG).show();
    }
}