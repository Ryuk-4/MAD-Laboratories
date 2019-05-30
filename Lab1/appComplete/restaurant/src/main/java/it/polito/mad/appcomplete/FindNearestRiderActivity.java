package it.polito.mad.appcomplete;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static it.polito.mad.data_layer_access.FirebaseUtils.*;
import static it.polito.mad.data_layer_access.Costants.*;

public class FindNearestRiderActivity extends AppCompatActivity
        implements RecyclerViewAdapterRider.OnRiderClickListener {

    private static final String TAG = "FindNearestRiderActivit";
    private boolean mLocationPermissionGranted = false;

    private FusedLocationProviderClient mFusedLocationRestaurant;
    private GeoLocation myLocation;
    private Location me;

    private Map<String, Location> ridersLocation = new HashMap<>();
    private List<Riders> riders = new ArrayList<>();
    private ValueEventListener RiderValueListener;
    private List<String> ridersWithListener = new ArrayList<>();
    private boolean fetchedRiderIds;
    private Set<GeoQuery> geoQueries = new HashSet<>();

    private RecyclerViewAdapterRider adapter;
    private RecyclerView recyclerView;
    private int initialListSize;
    private int iterationCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_nearest_rider);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupFirebase();

        mFusedLocationRestaurant = LocationServices.getFusedLocationProviderClient(this);

        setupListeners();

        setupAdapter();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupAdapter() {
        recyclerView = findViewById(R.id.recyclerViewRiders);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        adapter = new RecyclerViewAdapterRider(FindNearestRiderActivity.this, riders, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        RiderValueListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Riders r = new Riders();
                Geocoder geocoder = new Geocoder(FindNearestRiderActivity.this, Locale.getDefault());
                List<Address> addresses = new ArrayList<>();

                r.setId(dataSnapshot.getKey());

                if (dataSnapshot.child("Profile/imgUrl").getValue() != null) {

                    r.setPic(dataSnapshot.child("Profile/imgUrl").getValue().toString());
                }
                r.setName(dataSnapshot.child("Profile/name").getValue().toString());

                Location location = ridersLocation.get(dataSnapshot.getKey());
                r.setLocation(location);

                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (Exception e){
                    Log.w(TAG, "onDataChange: ", e);
                }

                String[] address = addresses.get(0).getAddressLine(0).split(", ");
                r.setAddress(address[0] + ", " + address[1]);

                if (containRider(r)) {
                    riderUpdated(r);
                } else {
                    newRider(r);
                }
            }

            private boolean containRider(Riders r) {
                for (Riders rider : riders) {
                    if (rider.getId().equals(r.getId())) {
                        return true;
                    }
                }
                return false;
            }

            private void newRider(Riders r) {
                Log.d(TAG, "newRider: inside onDataChange");

                iterationCount++;
                riders.add(0, r);

                if (!fetchedRiderIds && iterationCount == initialListSize) {
                    fetchedRiderIds = true;

                    sortByDistanceFromMe();

                    adapter.setRiders(riders);
                } else if (fetchedRiderIds) {
                    sortByDistanceFromMe();

                    adapter.notifyItemInserted(getIndexOfNewRider(r));
                }
            }

            private void riderUpdated(Riders r) {
                Log.d(TAG, "riderUpdated: inside onDataChange");

                int position = getRiderPosition(r.getId());
                riders.set(position, r);
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        };
    }

    private void sortByDistanceFromMe() {
        Collections.sort(riders, (r1, r2) -> {
            Location loc1 = new Location("");
            loc1.setLatitude(r1.getLatitude());
            loc1.setLongitude(r1.getLongitude());

            Location loc2 = new Location("");
            loc2.setLatitude(r2.getLatitude());
            loc2.setLongitude(r2.getLongitude());

            double dist1 = CalculationByDistance(me.getLatitude(), me.getLongitude(),
                    r1.getLatitude(), r1.getLongitude());

            double dist2 = CalculationByDistance(me.getLatitude(), me.getLongitude(),
                    r2.getLatitude(), r2.getLongitude());

            if (dist1 > dist2) {
                return 1;
            } else if (dist1 < dist2) {
                return -1;
            } else {
                return 0;
            }
        });
    }

    public double CalculationByDistance(double initialLat, double initialLong,
                                        double finalLat, double finalLong) {
        int R = 6371; // km (Earth radius)
        double dLat = Math.toRadians(finalLat - initialLat);
        double dLon = Math.toRadians(finalLong - initialLong);
        initialLat = Math.toRadians(initialLat);
        finalLat = Math.toRadians(finalLat);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(initialLat) * Math.cos(finalLat);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private int getIndexOfNewRider(Riders r) {
        int i;

        for (i = 0; i < riders.size(); i++) {

            if (riders.get(i).getId().equals(r.getId())) {
                Log.d(TAG, "getIndexOfNewUser: " + i);
                break;
            }
        }
        return i;
    }

    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    /*
        This method is responsible to prompt the user with a dialog message in which it will ask
        to the user whether or not activate the GPS
    */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    // Checks whether or not the current application has GPS enabled on the device
    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getLastKnownLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // Checks whether or not the device is able to use google services
    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(FindNearestRiderActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(FindNearestRiderActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    getLastKnownLocation();
                } else {
                    getLocationPermission();
                }
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                getLastKnownLocation();
            } else {
                getLocationPermission();
            }
        }
    }

    private void fetchRider() {

        if (myLocation != null) {
            Log.d(TAG, "fetchRider: called");
            // creates a new query around myLocation with a radius of 5 kilometers
            GeoQuery geoQuery = geofireRider.queryAtLocation(myLocation, 5);

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    Log.d(TAG, "onKeyEntered: called");

                    Location secondLocation = new Location("second");
                    secondLocation.setLatitude(location.latitude);
                    secondLocation.setLongitude(location.longitude);

                    if (!fetchedRiderIds) {
                        ridersLocation.put(key, secondLocation);
                    } else {
                        ridersLocation.put(key, secondLocation);
                        addRiderListener(key);
                    }

                }

                @Override
                public void onKeyExited(String key) {
                    Log.d(TAG, "onKeyExited: called");
                    if (ridersLocation.containsKey(key)) {
                        int position = getRiderPosition(key);
                        Log.d(TAG, "onKeyExited: " + position);
                        if (position > 0) {
                            riders.remove(position);
                            adapter.notifyItemRemoved(position);
                        }
                    }
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    Log.d(TAG, "onKeyMoved: called");
                }

                @Override
                public void onGeoQueryReady() {
                    Log.d(TAG, "onGeoQueryReady: called");

                    initialListSize = ridersLocation.size();
                    if (initialListSize == 0) {
                        fetchedRiderIds = true;
                    }

                    iterationCount = 0;

                    for (String key : ridersLocation.keySet()) {
                        addRiderListener(key);
                    }

                }

                private void addRiderListener(String riderId) {
                    branchDeliveryMan.child(riderId).addValueEventListener(RiderValueListener);
                    ridersWithListener.add(riderId);
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Log.w(TAG, "onGeoQueryError: ", error.toException());
                }
            });

            geoQueries.add(geoQuery);
        }
    }

    private int getRiderPosition(String key) {
        for (int i = 0; i < riders.size(); i++) {
            if (riders.get(i).getId().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    private void getLastKnownLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mFusedLocationRestaurant.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {

            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {

                    Location location = task.getResult();

                    myLocation = new GeoLocation(location.getLatitude(), location.getLongitude());

                    me = new Location("");
                    me.setLatitude(myLocation.latitude);
                    me.setLongitude(myLocation.longitude);

                    geofireRestaurant.setLocation(Uid, myLocation, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            Log.d(TAG, "Location set: myPosition(" +
                                    myLocation.latitude + ", " + myLocation.longitude + ")");
                        }
                    });

                    fetchRider();
                }
            }

        });
    }

    private void removeListeners() {
        for (GeoQuery geoQuery : geoQueries) {
            geoQuery.removeAllListeners();
        }

        for (String riderId : ridersWithListener) {
            database.child("riders_position").child(riderId)
                    .removeEventListener(RiderValueListener);
        }
    }

    @Override
    protected void onDestroy() {
        removeListeners();
        super.onDestroy();
    }

    @Override
    public void riderClickListener(int position) {
        Log.d(TAG, "OnRiderClickListener: called");
        requestRider(position);
    }

    private void requestRider(int position) {
        if (getIntent().hasExtra("reservationId")) {

            final String orderId = getIntent().getStringExtra("reservationId");

            branchOrdersReady.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: called");
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        ReservationInfo value = data.getValue(ReservationInfo.class);

                        if (value.getOrderID().equals(orderId)) {
                            ReservationInfo res = new ReservationInfo(value.getIdPerson(), value.getNamePerson(), value.getcLatitude(),
                                    value.getcLongitude(), value.getRestaurantId(), Double.toString(me.getLatitude()),
                                    Double.toString(me.getLongitude()));

                            res.setTimeReservation(value.getTimeReservation());

                            String riderId = riders.get(position).getId();

                            branchDeliveryMan.child(riderId).child("/Orders/Incoming")
                                    .child(value.getOrderID()).setValue(res);

                            branchDeliveryMan.child(riderId).child("/Orders")
                                    .child("IncomingReservationFlag").setValue(true);

                            branchCustomer.child(value.getIdPerson()).child("previous_order").child(value.getOrderID())
                                    .child("order_status").setValue("Ready_for_Delivery");
                            branchCustomer.child(value.getIdPerson()).child("previous_order").child(value.getOrderID())
                                    .child("riderId").setValue(riderId);

                            branchOrdersReady.child(orderId + "/status_order").setValue("ready");

                            Toast.makeText(FindNearestRiderActivity.this, "Your order's been placed",
                                    Toast.LENGTH_LONG).show();

                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
                }
            });

            Toast.makeText(this, "Called the Rider", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
