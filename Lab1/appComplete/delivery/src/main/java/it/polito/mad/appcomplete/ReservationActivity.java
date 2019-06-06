package it.polito.mad.appcomplete;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReservationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DeliveryLoginActivity.RestaurantLoginInterface {

    private static final String TAG = "ReservationActivity";


    private SectionsPagerAdapter mSectionsPagerAdapter;

    //The {@link ViewPager} that will host the section contents.
    private CustomViewPager mViewPager;

    //private PreparingReservationFragment prepFragment;
    private ReadyToGoReservationFragment endFragment;
    private IncomingReservationFragment incFragment;

    private Menu mMenu;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences preferences;
    private boolean started = false;

    // for notification
    private DatabaseReference branchOrders;
    private DatabaseReference database;
    private Intent locationService;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: called");

        setContentView(R.layout.drawer_menu_reservation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_reservation);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user == null) {
                    startActivity(new Intent(ReservationActivity.this, DeliveryLoginActivity.class));
                    finish();
                }
            }
        };

        mMenu = navigationView.getMenu();
        mMenu.findItem(R.id.nav_deleteAccount).setVisible(true);

        preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);
        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference branchProfile = database.child("delivery/" +
                preferences.getString("Uid", " ") + "/Profile");

        branchProfile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("firstTime").getValue().equals(true)){
                    //showDialogMenu();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });

        incFragment = new IncomingReservationFragment();
        incFragment.setContext(this);

        //prepFragment = new PreparingReservationFragment();

        endFragment = new ReadyToGoReservationFragment();
        endFragment.setContext(this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.containerTabs);
        mViewPager.setPagingEnabled(false);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(mViewPager);




        int permission = ContextCompat.checkSelfPermission(ReservationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {



        }
    }

    private void showDialogMenu() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

        pictureDialog.setTitle("Welcome");
        pictureDialog.setMessage("Before start using our app, please complete your profile.");
        pictureDialog.setCancelable(false);
        pictureDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(ReservationActivity.this, ProfileActivity.class));
            }
        });
        pictureDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.logoutButton:
                logout();
                break;

            case R.id.new_order_incoming:
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("IncomingReservation", false);
                editor.apply();

                branchOrders = database.child("delivery/" +
                        preferences.getString("Uid", "") + "/Orders/IncomingReservationFlag");
                branchOrders.setValue(false);

                invalidateOptionsMenu();
                break;
            case R.id.enable_disable_position:
                if (locationService != null && started) //disable the service
                {
                    Toast.makeText(this,"disabled",Toast.LENGTH_LONG).show();
                    stopService(locationService);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("riders_position")
                            .child(FirebaseAuth.getInstance().getUid());
                    ref.removeValue();
                    started = false;
                } else //enable the service
                {
                    getLocationPermission();
                    started = true;
                    Toast.makeText(this,"enabled",Toast.LENGTH_LONG).show();

                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        started = this.getSharedPreferences("savedStuff", Context.MODE_PRIVATE).getBoolean("started", false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences sharedPreferences = this.getSharedPreferences("savedStuff", Context.MODE_PRIVATE);

        sharedPreferences.edit().putBoolean("started", started).commit();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);

        // if login == false then hide logout button
        if (!preferences.getBoolean("login", true)) {
            menu.findItem(R.id.logoutButton).setVisible(false);
            menu.findItem(R.id.edit_action).setVisible(false);
        } else {
            menu.findItem(R.id.logoutButton).setVisible(true);
            menu.findItem(R.id.edit_action).setVisible(false);
        }

        boolean newOrders = preferences.getBoolean("IncomingReservation", false);


        if (newOrders == false) {
            menu.findItem(R.id.new_order_incoming).setVisible(false);
        } else {
            menu.findItem(R.id.new_order_incoming).setVisible(true);
        }



        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
        Log.d(TAG, "onStart: called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called");
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_reservation);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_dailyMenu) {
            Intent intent = new Intent(ReservationActivity.this, ReportActivity.class);
            startActivity(intent);
            //finish();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_contactUs) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_reservation);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    // It's going to create a section view adapter in which we add the fragments
    private void setupViewPager(@NonNull ViewPager viewPager) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mSectionsPagerAdapter.addFragments(incFragment, "New");
        mSectionsPagerAdapter.addFragments(endFragment, "Finished");

        viewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public void logout() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("login", false);
        editor.apply();

        mMenu.findItem(R.id.nav_deleteAccount).setVisible(false);
        invalidateOptionsMenu();
        auth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    ///////////////////////////////////////////////////For location permission
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private Boolean mLocationPermissionsGranted = false;

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            locationService = new Intent(this, ManageCurrentLocationService.class);
            startService(locationService);
            mLocationPermissionsGranted = true;
            Log.d("TAG", "getLocationPermission: hello helle");
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationService = new Intent(this, ManageCurrentLocationService.class);
                startService(locationService);
            }
        }
    }

    ///////////////////////////////////////////////////

}
