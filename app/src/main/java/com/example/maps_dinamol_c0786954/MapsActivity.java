package com.example.maps_dinamol_c0786954;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int REQUEST_CODE = 1;
    private Marker homeMarker;
    private Marker destMarker;


    Polyline line;
    Polygon shape;



    private static final int QUADRILATERAL_SIDES = 4;
    List<Marker> markers = new ArrayList<>();

    // location with location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;

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

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setHomeMarker(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();



        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

//getting address by tap on marker
                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                List<Address> address;
                try {
                    address = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);

                    StringBuilder sbAddress = new StringBuilder();
                    sbAddress.append(address.get(0).getAddressLine(0));
                    sbAddress.append(", " + address.get(0).getLocality());
                    sbAddress.append(", " + address.get(0).getPostalCode());
                    sbAddress.append(", " + address.get(0).getAdminArea());

                    Toast.makeText(MapsActivity.this, sbAddress.toString(),Toast.LENGTH_LONG).show();
                }catch (
                        Exception e
                ){
                    e.printStackTrace();
                }


                return false;
            }
        });

        // apply long press gesture
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
//                Location location = new Location("Your Destination");
//                location.setLatitude(latLng.latitude);
//                location.setLongitude(latLng.longitude);
                // set marker
                setMarker(latLng);
            }

            private void setMarker(LatLng latLng) {
                MarkerOptions options = new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)) //custom colour
                        .title("A");

                /*if (destMarker != null) clearMap();
                destMarker = mMap.addMarker(options);
                drawLine();*/

                // check if there are already the same number of markers, we clear the map.
                if (markers.size() == QUADRILATERAL_SIDES)
                    clearMap();

                markers.add(mMap.addMarker(options));
                if (markers.size() == QUADRILATERAL_SIDES)
                    drawShape();
            }

            private void drawShape() {
                PolygonOptions options = new PolygonOptions()
                        .fillColor(0x5900ff00)
                        .strokeColor(Color.RED)
                        .strokeWidth(4);

                for (int i=0; i<QUADRILATERAL_SIDES; i++) {
                    options.add(markers.get(i).getPosition());
                }
                shape = mMap.addPolygon(options);

            }

            private void clearMap() {

                /*if (destMarker != null) {
                    destMarker.remove();
                    destMarker = null;
                }
                line.remove();*/

                for (Marker marker: markers)
                    marker.remove();

                markers.clear();
                shape.remove();
                shape = null;
            }
            /*
            private void drawLine() {
                PolylineOptions options = new PolylineOptions()
                        .color(Color.BLACK)
                        .width(10)
                        .add(homeMarker.getPosition(), destMarker.getPosition());
                line = mMap.addPolyline(options);
            }*/
        });
    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

        /*Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        setHomeMarker(lastKnownLocation);*/
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setHomeMarker(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(userLocation)
                .title("You are here")
                .icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_baseline_person_pin_24)) //customised marker for user location
                .snippet("Your Location");
        homeMarker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorField) {
        Drawable vectorDrawable= ContextCompat.getDrawable(context, vectorField);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap=Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
















}


