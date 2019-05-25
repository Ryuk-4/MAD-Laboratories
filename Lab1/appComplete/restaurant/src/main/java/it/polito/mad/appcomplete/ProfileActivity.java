package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RestaurantLoginActivity.RestaurantLoginInterface {

    private static final String TAG = "ProfileActivity";

    private ImageView im;
    private Toolbar toolbar;
    private TextView name;
    private TextView phone;
    private TextView openingHours;
    private TextView address;
    private TextView email;
    private TextView description;
    private SharedPreferences sharedpref, preferences;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleSignInClient mGoogleSignInClient;

    private Menu mMenu;
    private DatabaseReference database;
    private boolean newOrders;
    private DatabaseReference branchOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_menu_profile);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the UP button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_profile);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        im = findViewById(R.id.imageView1);
        name = findViewById(R.id.textViewName);
        phone = findViewById(R.id.textViewTelephone);
        openingHours = findViewById(R.id.textViewHours);
        address = findViewById(R.id.textViewAddress);
        email = findViewById(R.id.textViewEmail);
        description = findViewById(R.id.textViewDescription);

        Button buttonStats = findViewById(R.id.statisticsButton);
        buttonStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, StatisticsActivity.class));
            }
        });

        Button buttonComm = findViewById(R.id.commentButton);
        buttonComm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, RestaurantReviewActivity.class));
            }
        });

        sharedpref = getSharedPreferences("userinfo", Context.MODE_PRIVATE);

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
                    startActivity(new Intent(ProfileActivity.this, RestaurantLoginActivity.class));
                    finish();
                }
            }
        };

        //mMenu = navigationView.getMenu();
        //mMenu.findItem(R.id.nav_deleteAccount).setVisible(true);

        preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);
        database = FirebaseDatabase.getInstance().getReference();
        branchOrders = database.child("restaurants/" +
                preferences.getString("Uid", "") + "/Orders/IncomingReservationFlag");

        branchOrders.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                newOrders = dataSnapshot.getValue(Boolean.class);
                if(newOrders == true) {
                    Toast.makeText(ProfileActivity.this, "You have a new Reservation.", Toast.LENGTH_LONG)
                            .show();
                }

                invalidateOptionsMenu();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_reservation) {
            finish();
        } else if (id == R.id.nav_dailyMenu) {
            Intent intent = new Intent(this, DailyOfferActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_contactUs) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_profile);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_profile);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        displayData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (newOrders == false) {
            menu.findItem(R.id.new_order_incoming).setVisible(false);
        } else {
            menu.findItem(R.id.new_order_incoming).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.edit_action:
                //This action will happen when is clicked the edit button in the action bar
                Intent intent = new Intent(this, ProfileEditActivity.class);
                startActivity(intent);
                break;

            case R.id.logoutButton:
                logout();
                finish();
                break;

            case R.id.new_order_incoming:

                branchOrders.setValue(false);
                startActivity(new Intent(this, ReservationActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayData() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);
        String Uid = preferences.getString("Uid", " ");
        DatabaseReference branchProfile = database.child("restaurants/" + Uid + "/Profile");

        branchProfile.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: ");

                name.setText(dataSnapshot.child("name").getValue().toString());
                email.setText(dataSnapshot.child("email").getValue().toString());

                if (dataSnapshot.child("firstTime").getValue().equals(false)) {

                    if (dataSnapshot.child("imgUrl").getValue() != null) {
                        Picasso.get().load(dataSnapshot.child("imgUrl").getValue().toString())
                                .fit().centerCrop().into(im);
                    }
                    address.setText(dataSnapshot.child("address").getValue().toString());
                    description.setText(dataSnapshot.child("description").getValue().toString());
                    phone.setText(dataSnapshot.child("phone").getValue().toString());
                    openingHours.setText(dataSnapshot.child("openingHours").getValue().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });

    }

    @Override
    public void logout() {
        SharedPreferences preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("login", false);
        editor.apply();

//        mMenu.findItem(R.id.nav_deleteAccount).setVisible(false);
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
}
