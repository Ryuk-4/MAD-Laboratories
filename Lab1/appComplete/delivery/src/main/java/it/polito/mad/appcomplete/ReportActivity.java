package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReportActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener{

    TextView TextViewTotalDistance;
    TextView TextViewIncome;
    private DatabaseReference database;
    private SharedPreferences sharedpref, preferences;
    private DatabaseReference branchOrders;
    private boolean newOrders;
    private Map<String, Integer> restCount;
    private ArrayList dataSets;
    private ArrayList xAxis;

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

        showGraph();

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


    private void showGraph() {
        restCount = new TreeMap<>();
        DatabaseReference d = FirebaseDatabase.getInstance().getReference("delivery").child(FirebaseAuth.getInstance().getUid()).child("Orders").child("finished");
        d.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    Object o = ds.child("restaurantId").getValue();

                    if (o != null)
                    {
                        String id = o.toString();

                        if (restCount.containsKey(id))
                        {
                            Integer i = restCount.get(id);
                            restCount.put(id, ++i);
                        } else
                        {
                            restCount.put(o.toString(), 1);
                        }
                    }
                }

                restCount = sortByValue(restCount);

                DatabaseReference d = FirebaseDatabase.getInstance().getReference("restaurants");
                d.addListenerForSingleValueEvent(new CustomValueListener(restCount));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    public void initGrapf()
    {
        BarChart chart = (BarChart) findViewById(R.id.chart);

        BarData data = new BarData(xAxis, dataSets);
        chart.setData(data);
        chart.getAxisLeft().setDrawGridLines(false);chart.getAxisRight().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);chart.getXAxis().setDrawGridLines(false);
        //chart.setDrawBarShadow(true);
        //chart.setScaleEnabled(false);
        chart.setDescription("My Chart");
        chart.animateXY(1000, 1000);
        chart.invalidate();
    }

    private static Map<String, Integer> sortByValue(Map<String, Integer> hm) {

        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<>(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {

                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> element : list) {
            temp.put(element.getKey(), element.getValue());
        }
        return temp;
    }


    class CustomValueListener implements ValueEventListener
    {
        private Map<String, Integer> map;

        public CustomValueListener(Map<String, Integer> map)
        {
            this.map = map;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            Log.d("TAG", "onDataChange: " + map);
            xAxis = new ArrayList();

            for (DataSnapshot da : dataSnapshot.getChildren())
            {
                if (map.containsKey(da.getKey()))
                {
                    Object o = da.child("Profile").child("name").getValue();

                    if (o != null)
                    {
                        xAxis.add(o.toString());
                    }
                }
            }

            ArrayList valueSet1 = new ArrayList();
            dataSets = new ArrayList<>();

            int i = 0;
            for (String key : map.keySet())
            {
                BarEntry v = new BarEntry(restCount.get(key), i);
                valueSet1.add(v);
                i++;
            }

                /*for (int i = 0 ; i < 5 && i < restCount.size() ; i++)
                {
                    BarEntry v = new BarEntry(restCount.get(i), i);
                    valueSet1.add(v);
                }*/

            BarDataSet barDataSet1 = new BarDataSet(valueSet1, "Brand 1");
            barDataSet1.setColor(Color.rgb(0, 155, 0));

            dataSets.add(barDataSet1);

            initGrapf();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }

}
