package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;

public class ReportActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener{

    TextView TextViewTotalDistance;
    TextView TextViewIncome;
    private DatabaseReference database;
    private SharedPreferences sharedpref, preferences;
    private DatabaseReference branchOrders;
    private boolean newOrders;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleSignInClient mGoogleSignInClient;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_report);
        setContentView(R.layout.drawer_menu_reportactivity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_report);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextViewTotalDistance = findViewById(R.id.textViewTotalDistance);
        TextViewIncome = findViewById(R.id.TextViewIncome);

        // For new incoming notification:
        preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);
        database = FirebaseDatabase.getInstance().getReference();
        branchOrders = database.child("delivery/" +
                preferences.getString("Uid", "") + "/Orders/IncomingReservationFlag");

        branchOrders.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                newOrders = dataSnapshot.getValue(Boolean.class);
                if(newOrders == true)
                    Toast.makeText(ReportActivity.this, "You have a new Reservation.", Toast.LENGTH_LONG).show();

                invalidateOptionsMenu();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
               // Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });


        initializeData();

    }



    private void initializeData(){
        database = FirebaseDatabase.getInstance().getReference();
        preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);

        String Uid = preferences.getString("Uid", "");
        DatabaseReference branchDailyFood = database.child("delivery/" + Uid );

        branchDailyFood.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                try {
                    float totaldistance = Float.parseFloat(dataSnapshot.child("totaldistance").getValue().toString());
                    TextViewTotalDistance.setText(Float.toString(totaldistance) + " km");

                    double paymentPerKm = 1.5;


                    BigDecimal income = round((float) paymentPerKm * totaldistance, 2);
                    //int incomInt=Integer..parseInt(income);
                    //TextViewIncome.setText(Convert.bigdeci .toString(income));
                    TextViewIncome.setText(income.toString() + " Euro");
                }catch(Exception e){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });
    }


    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
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
                //logout();
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(this,ProfileActivity.class);
            startActivity(intent);
            finish();

        }else if (id == R.id.nav_reservation) {
            Intent intent = new Intent(this,ReservationActivity.class);
            startActivity(intent);
            finish();

        }
        else if (id == R.id.nav_dailyMenu) {
            //Intent intent = new Intent(this, ReportActivity.class);
            //Intent intent = new Intent(this, ReportActivity.class);

           // startActivity(intent);
            finish();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_contactUs) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_report);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
