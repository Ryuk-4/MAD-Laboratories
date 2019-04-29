package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DailyOfferActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        RVAdapter.OnFoodListener,
        RecyclerItemTouchHelperFood.RecyclerItemTouchHelperListener{

    private static final String TAG = "DailyOfferActivity";

    private RecyclerView rv;
    private List<FoodInfo> foodList;
    private SharedPreferences sharedpref, preferences;
    private RVAdapter myAdapter;

    private DatabaseReference database;
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_menu_daily_offer);//ciao
        Toolbar toolbar = findViewById(R.id.toolbarDailyOffer);
        setSupportActionBar(toolbar);

        //Show the UP button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_daily_menu);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);

        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(DailyOfferActivity.this, DailyActivityEdit.class);
                startActivity(intent);
            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(DailyOfferActivity.this, DailyOfferFavoriteActivity.class);
                startActivity(intent);
            }
        });

        sharedpref = getSharedPreferences("foodinfo", Context.MODE_PRIVATE);

        initializeData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        initializeCardLayout();
    }

    private void initializeCardLayout() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        myAdapter = new RVAdapter(foodList, this, DailyOfferActivity.this);
        rv.setAdapter(myAdapter);


        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperFood(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rv);

    }

    private void initializeData(){
        database = FirebaseDatabase.getInstance().getReference();
        preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);

        String Uid = preferences.getString("Uid", "");
        DatabaseReference branchDailyFood = database.child("restaurants/" + Uid + "/Daily_Food");

        branchDailyFood.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                foodList = new ArrayList<>();

                for (DataSnapshot data :  dataSnapshot.getChildren()){
                    FoodInfo value = data.getValue(FoodInfo.class);
                    value.setFoodId(data.getKey());

                    foodList.add(restoreItem(value));
                }

                initializeCardLayout();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });
    }

    public FoodInfo restoreItem(FoodInfo foodInfo){
        FoodInfo res = new FoodInfo();

        res.setFoodId(foodInfo.getFoodId());
        res.setImage(foodInfo.getImage());
        res.setName(foodInfo.getName());
        res.setPrice(foodInfo.getPrice());
        res.setQuantity(foodInfo.getQuantity());
        res.setDescription(foodInfo.getDescription());

        return res;
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
      
        Log.d(TAG, "onNavigationItemSelected: ");
        if (id == R.id.nav_reservation) {
            finish();
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(DailyOfferActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_contactUs) {

        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout_daily_menu);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void OnFoodClickFood(int position) {

        Intent intent = new Intent(DailyOfferActivity.this, DailyActivityEdit.class);
        intent.putExtra("food_selected", "normal");
        intent.putExtra("food_position", foodList.get(position).getFoodId());

        startActivity(intent);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof RVAdapter.FoodInfoHolder) {
            String name = foodList.get(viewHolder.getAdapterPosition()).getName();

            // backup of removed item for undo purpose
            final FoodInfo deletedItem = foodList.get(viewHolder.getAdapterPosition());
            final String deletedItemId = deletedItem.getFoodId();

            final DatabaseReference branchDailyFood = database.child("restaurants/" +
                    preferences.getString("Uid", " ") + "/Daily_Food");

            branchDailyFood.child(deletedItemId).removeValue();

            Snackbar snackbar = Snackbar
                    .make(rv, name + "dish removed", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  
                    // undo is selected, restore the deleted item
                    branchDailyFood.child(deletedItemId).setValue(restoreItem(deletedItem));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_daily_menu);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }
}





