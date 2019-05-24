package it.polito.mad.customer;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaeger.library.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OrdersActivity

        extends AppCompatActivity

        implements NavigationView.OnNavigationItemSelectedListener,
        OrdersCompletedFragment.OnFragmentInteractionListenerComplete,
        OrdersPendingFragment.OnFragmentInteractionListenerPending{

    private Toolbar toolbar;
    private List<OrdersInfo> ordersInfoListPending, ordersInfoListCompleted;
    private myFragmentPageAdapterOrders adapter;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_orders);

        toolbar = findViewById(R.id.toolbar_orders);
        setSupportActionBar(toolbar);

        initDrawer();
        getDataOrders();

        StatusBarUtil.setTransparent(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        toolbar = findViewById(R.id.toolbar_orders);
        setSupportActionBar(toolbar);

        initDrawer();
        getDataOrders();

        StatusBarUtil.setTransparent(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_restaurant) {
            Intent intent = new Intent(OrdersActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(OrdersActivity.this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_contactUs) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_orders);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_orders);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(OrdersActivity.this);
    }

    private void getDataOrders()
    {
        ordersInfoListPending = new ArrayList<>();
        ordersInfoListCompleted = new ArrayList<>();
        viewPager = (ViewPager) findViewById(R.id.containerTabsOrders);


        //Log.d("TAG", "onDataChange: ");
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customers").child(FirebaseAuth.getInstance().getUid()).child("previous_order");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersInfoListPending.clear();
                ordersInfoListCompleted.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String orderId = ds.getKey();

                    Object o = ds.child("restaurant_name").getValue();
                    String restName = "";

                    if (o != null)
                    {
                        restName = o.toString();
                    }

                    o = ds.child("restaurant").getValue();
                    String restId = "";

                    if (o != null)
                    {
                        restId = o.toString();
                    }

                    o = ds.child("timeReservation").getValue();
                    String time = "";

                    if (o != null)
                    {
                        time = o.toString();
                    }

                    o = ds.child("order_status").getValue();
                    String orderState = "";

                    if (o != null)
                    {
                        orderState = o.toString();
                    }

                    o = ds.child("addressReservation").getValue();
                    String address = "";

                    if (o != null)
                    {
                        address = o.toString();
                    }


                    Map<String, Integer> foodAmount = new TreeMap<>();
                    Map<String, Float> foodPrice = new TreeMap<>();
                    Map<String, String> foodId = new TreeMap<>();
                    Map<String, String> foodKey = new TreeMap<>();

                    for (DataSnapshot ds1 : ds.child("food").getChildren())
                    {
                        String foodName = "";
                        o = ds1.child("foodName").getValue();

                        if (o != null)
                        {
                            foodName = o.toString();
                        }

                        foodId.put(foodName, ds1.getKey());

                        String quantity = "0";
                        o = ds1.child("foodQuantity").getValue();

                        if (o != null)
                        {
                            quantity = o.toString();
                        }

                        foodAmount.put(foodName, new Integer(quantity));

                        String price = "0";
                        o = ds1.child("foodPrice").getValue();

                        if (o != null)
                        {
                            price = o.toString();
                        }
                        foodPrice.put(foodName, new Float(price));

                    }

                    o = ds.child("riderId").getValue();
                    String riderId = "";

                    if (o != null)
                        riderId = o.toString();

                    o = ds.child("reviewed").getValue();
                    String review = "false";

                    if (o != null)
                        review = o.toString();

                    if (orderState.compareTo("pending") == 0)
                    {
                        ordersInfoListPending.add(new OrdersInfo(restName, restId, time, address, foodAmount, foodPrice, foodId, OrderState.PENDING, orderId, null, false));
                    } else if (orderState.compareTo("Ready_for_Delivery") == 0)
                    {
                        ordersInfoListPending.add(new OrdersInfo(restName, restId, time, address, foodAmount, foodPrice, foodId, OrderState.DELIVERING, orderId, riderId, false));
                    } else if (orderState.compareTo("In_Preparation") == 0)
                    {
                        ordersInfoListPending.add(new OrdersInfo(restName, restId, time, address, foodAmount, foodPrice, foodId, OrderState.ACCEPTED, orderId, null, false));
                    } else if (orderState.compareTo("Completed") == 0)
                    {
                        ordersInfoListCompleted.add(new OrdersInfo(restName, restId, time, address, foodAmount, foodPrice, foodId, OrderState.DELIVERED, orderId, null, Boolean.parseBoolean(review)));
                    } else if (orderState.compareTo("Rejected") == 0)
                    {
                        ordersInfoListCompleted.add(new OrdersInfo(restName, restId, time, address, foodAmount, foodPrice, foodId, OrderState.CANCELLED, orderId, null, false));
                    }
                }

                adapter = new myFragmentPageAdapterOrders(OrdersActivity.this, getSupportFragmentManager(), ordersInfoListPending, ordersInfoListCompleted);
                viewPager.setAdapter(adapter);
                ((TabLayout)findViewById(R.id.tabs_orders)).setupWithViewPager(viewPager);
                //viewPager.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onFragmentInteractionComplete(Uri uri) {

    }

    @Override
    public void onFragmentInteractionPending(Uri uri) {

    }
}
