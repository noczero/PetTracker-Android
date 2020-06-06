package com.zero.pettracker.ui.outdoor;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zero.pettracker.R;

import java.util.ArrayList;

import static java.lang.Double.parseDouble;

public class OutdoorFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private OutdoorViewModel outdoorViewModel;

    private GoogleMap mGoogleMap;
    private Location mLastLocation;
    private Marker mCurrLocationMarker, mPetLocationMarker;
    private Circle mCircle;
    double radiusInMeters = 5;
    int strokeColor = 0xffff0000; //Color Code you want
    int shadeColor = 0x44ff0000; //opaque red fill
    int increment_title = 0;
    private FusedLocationProviderClient fusedLocationClient;

    // array list of LatLon
    private Node petNode;
    private ArrayList<Node> listNode = new ArrayList<>(); // list node


    // Write a message to the database
    private String nodeFirebase = "Device1"; // node
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference firebaseDB = database.getReference().child(nodeFirebase); // child as node

    String latitude, longitude, last_time_updated;

    private Button btnRoute;
    private ProgressBar progressBar;
    private ConstraintLayout outdoorLayout;

    // onCreateView
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        outdoorViewModel = ViewModelProviders.of(this).get(OutdoorViewModel.class);

        View root = inflater.inflate(R.layout.fragment_outdoor, container, false);

        // progressbar
        progressBar = root.findViewById(R.id.loader);
        outdoorLayout = root.findViewById(R.id.main_outdoor_layout);

        getFirebaseData();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        ;
        mapFragment.getMapAsync(this);

        // fusedlocation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            //Place current location marker
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.title("My Position");
                            markerOptions.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_user));
                            mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                            // add position to node list
                            listNode.add(new Node(latLng, "Start")); // add it to node

                            //move map camera
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                        }
                    }
                });

        // button listenete
        btnRoute = root.findViewById(R.id.btn_route);
        btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent routeIntent = new Intent(getActivity(), SelectRouteActivity.class);

                // floydWarshall
                floydWarshall algorithm = new floydWarshall(listNode);
                algorithm.orderNode(); // order node from start node to end node
                double distanceMatrix[][] = algorithm.getDistanceMatrix(); // get distance matrix
                double floydMarshallMatrix[][] = algorithm.getFloyWarshalldMatrix(distanceMatrix);

                // passing listNode
                routeIntent.putParcelableArrayListExtra("listNode", algorithm.getListNode());
                startActivity(routeIntent);
            }
        });


        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

        // check permission
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mGoogleMap.setMyLocationEnabled(true);

                // long click listener
                mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        // Creating marker
                        MarkerOptions markerOptions = new MarkerOptions();

                        // set marker
                        markerOptions.position(latLng);

                        // set latittude and longitude title
                        String markerTitle = "Node " + ++increment_title;
                        markerOptions.title(markerTitle);

                        // add position to node list
                        listNode.add(new Node(latLng, markerTitle)); // add it to node

                        // add marker to maps
                        mGoogleMap.addMarker(markerOptions);


                        //Log.d("floyd", "Distance Matrix: " + distanceMatrix);

                    }

                });
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    void showLoadingLayout() {
        outdoorLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    void hideLoadingLayout() {
        outdoorLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    void getFirebaseData() {

        showLoadingLayout();

        firebaseDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    latitude = String.valueOf(dataSnapshot.child("Latitude").getValue());
                    longitude = String.valueOf(dataSnapshot.child("Longitude").getValue());
                    last_time_updated = String.valueOf(dataSnapshot.child("LastUpdate").getValue());

                    Log.d("Incoming Data", "Lat : " + latitude + " - Lon : " + longitude + " - Last Update : " + last_time_updated);

                    // update camera
                    LatLng petLocation = new LatLng(parseDouble(latitude), parseDouble(longitude));

                    // check marker
                    if (mPetLocationMarker != null && mCircle != null) {
                        mPetLocationMarker.remove(); // remove previous marker
                        mCircle.remove();
                    }

                    // marker
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(petLocation);
                    markerOptions.title("My Pet Position");
                    markerOptions.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_black_paw));
                    mPetLocationMarker = mGoogleMap.addMarker(markerOptions);

                    CircleOptions addCircle = new CircleOptions().center(petLocation).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
                    mCircle = mGoogleMap.addCircle(addCircle);

                    // check node to update itself
                    petNode = findSameNode("End", listNode); // find node on the list with End name.

                    if (petNode == null) {
                        // not found on list
                        listNode.add(new Node(petLocation, "End")); // add it to node
                    } else {
                        // found, set new latitude longitude
                        petNode.setLatLng(petLocation);
                    }

                    // Check Node
//                    for(Node node : listNode){
//                        Log.d("node", "Name : " + node.getName() + " - LatLon : " + node.getLatLng());
//                    }

                    //move map camera
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(petLocation));
                    mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(18));

                    hideLoadingLayout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("My Pet Position");
        markerOptions.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_black_paw));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        CircleOptions addCircle = new CircleOptions().center(latLng).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircle = mGoogleMap.addCircle(addCircle);

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));

    }

    // searh equals name and retrun the node
    public static Node findSameNode(String name, ArrayList<Node> nodes) {
        for (Node node : nodes) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
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

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    // custom bitmap
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_pin_red);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        // inner image
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        int left = (background.getIntrinsicWidth() - vectorDrawable.getIntrinsicWidth()) / 2;
        int top = (background.getIntrinsicHeight() - vectorDrawable.getIntrinsicHeight()) / 3;
        vectorDrawable.setBounds(left, top, vectorDrawable.getIntrinsicWidth() + left, vectorDrawable.getIntrinsicHeight() + top);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        // boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
        // double myDouble = savedInstanceState.getDouble("myDouble");
        // int myInt = savedInstanceState.getInt("MyInt");
        // String myString = savedInstanceState.getString("MyString");

    }
}
