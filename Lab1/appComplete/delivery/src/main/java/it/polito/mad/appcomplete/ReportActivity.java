package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

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
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
