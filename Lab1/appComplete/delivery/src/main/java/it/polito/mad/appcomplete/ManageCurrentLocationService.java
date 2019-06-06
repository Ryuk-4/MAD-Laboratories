package it.polito.mad.appcomplete;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.location.Location;

import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class ManageCurrentLocationService extends IntentService {

    private boolean terminated;

    private static final String TAG = "ManageCurrentLocationSe";
    public ManageCurrentLocationService()
    {
        super("ManageCurrentLocationService");
        terminated = false;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onHandleIntent(Intent intent) {


        new TrackerService().requestLocationUpdates();


    }

    @Override
    public void onDestroy() {
        Log.d("TAG", "onDestroy: ");
        terminated = true;
        super.onDestroy();
    }

    public class TrackerService {

        @SuppressLint("MissingPermission")
        private void requestLocationUpdates() {
            Log.d("TAG", "requestLocationUpdates: ");

            while (true) {

                LocationServices.getFusedLocationProviderClient(ManageCurrentLocationService.this).flushLocations();
                LocationServices.getFusedLocationProviderClient(ManageCurrentLocationService.this).getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                Log.d(TAG, "Location: \n" + location.getLatitude() +"\n" + location.getLongitude());

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("riders_position");

                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    GeoFire geoFire = new GeoFire(ref);
                                    geoFire.setLocation(FirebaseAuth.getInstance().getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                        @Override
                                        public void onComplete(String key, DatabaseError error) {

                                        }
                                    });
                                }
                            }
                        });

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}


