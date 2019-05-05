package it.polito.mad.customer;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OrdersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private List<OrdersInfo> ordersInfoList;
    private RecyclerView rvOrders;
    private RVAOrders myAdapterOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_orders);

        toolbar = findViewById(R.id.toolbar_orders);
        setSupportActionBar(toolbar);

        initDrawer();
        getDataOrders();

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
        ordersInfoList = new ArrayList<>();
        //Log.d("TAG", "onDataChange: ");
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customers").child(FirebaseAuth.getInstance().getUid()).child("previous_order");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    //Log.d("TAGtt", "onDataChange: ");
                    String orderId = ds.getKey();
                    String restName = ds.child("restaurant_name").getValue().toString();
                    String restId = ds.child("restaurant").getValue().toString();
                    String time = ds.child("timeReservation").getValue().toString();
                    String address = ds.child("addressReservation").getValue().toString();
                    String orderState = ds.child("order_status").getValue().toString();

                    Map<String, Integer> foodAmount = new TreeMap<>();
                    Map<String, Float> foodPrice = new TreeMap<>();
                    Map<String, String> foodId = new TreeMap<>();

                    for (DataSnapshot ds1 : ds.child("food").getChildren())
                    {
                        String foodName = ds1.child("foodName").getValue().toString();
                        foodAmount.put(foodName, new Integer(ds1.child("foodQuantity").getValue().toString()));
                        foodPrice.put(foodName, new Float(ds1.child("foodPrice").getValue().toString()));
                        foodId.put(foodName, ds1.getKey().toString());
                    }

                    if (orderState.compareTo("pending") == 0)
                    {
                        //Log.d("TAG", "onDataChange: pending");
                        ordersInfoList.add(new OrdersInfo(restName, restId, time, address, foodAmount, foodPrice, foodId, OrderState.PENDING, orderId));
                    } else if (orderState.compareTo("Ready_for_Delivery") == 0)
                    {
                        //Log.d("TAG", "onDataChange: ready");
                        ordersInfoList.add(new OrdersInfo(restName, restId, time, address, foodAmount, foodPrice, foodId, OrderState.ACCEPTED, orderId));
                    }
                }

                initializeCardLayoutOrders();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeCardLayoutOrders() {
        rvOrders = (RecyclerView) findViewById(R.id.rv_orders);
        rvOrders.setHasFixedSize(false);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvOrders.setLayoutManager(llm);

        myAdapterOrders = new RVAOrders(this, ordersInfoList);
        rvOrders.setAdapter(myAdapterOrders);
    }
}
