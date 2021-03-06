package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static it.polito.mad.data_layer_access.FirebaseUtils.*;

public class DailyOfferActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        RVAdapter.OnFoodListener,
        RecyclerItemTouchHelperFood.RecyclerItemTouchHelperListener,
        RestaurantLoginActivity.RestaurantLoginInterface {

    private static final String TAG = "DailyOfferActivity";

    private RecyclerView rv;
    private List<FoodInfo> foodList;
    private SharedPreferences sharedpref, preferences;
    private RVAdapter myAdapter;

    private FloatingActionMenu materialDesignFAM;
    private FloatingActionButton floatingActionButton1, floatingActionButton2;

    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleSignInClient mGoogleSignInClient;

    private boolean newOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_menu_daily_offer);
        Toolbar toolbar = findViewById(R.id.toolbarDailyOffer);
        setSupportActionBar(toolbar);

//      Show the UP button in the action bar
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
                Intent intent = new Intent(DailyOfferActivity.this, DailyFoodEditActivity.class);
                startActivity(intent);
            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(DailyOfferActivity.this, DailyOfferFavoriteActivity.class);
                startActivity(intent);
            }
        });

        preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);

        setupFirebase();

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
                    startActivity(new Intent(DailyOfferActivity.this, RestaurantLoginActivity.class));
                    finish();
                }
            }
        };

        branchOrdersFlag.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try {
                    newOrders = dataSnapshot.getValue(Boolean.class);
                } catch (NullPointerException nEx) {
                    Toast.makeText(DailyOfferActivity.this, "Opss. Try again", Toast.LENGTH_SHORT).show();
                }

                if (newOrders) {
                    Toast.makeText(DailyOfferActivity.this, "You have a new Reservation.", Toast.LENGTH_LONG)
                            .show();
                }
                invalidateOptionsMenu();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DailyOfferActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
            }
        });

        initializeData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if login == false then hide logout button
        if (!preferences.getBoolean("login", true)) {
            menu.findItem(R.id.logoutButton).setVisible(false);
            menu.findItem(R.id.edit_action).setVisible(false);
        } else {
            menu.findItem(R.id.logoutButton).setVisible(true);
            menu.findItem(R.id.edit_action).setVisible(false);
        }

        if (!newOrders) {
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
                branchOrdersFlag.setValue(false);
                startActivity(new Intent(this, ReservationActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeCardLayout() {
        Log.d(TAG, "initializeCardLayout: called");
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        myAdapter = new RVAdapter(foodList, this, DailyOfferActivity.this);
        rv.setAdapter(myAdapter);


        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperFood(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rv);

    }

    private void initializeData() {

        branchDailyFood.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                foodList = new ArrayList<>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    try {
                        FoodInfo value = data.getValue(FoodInfo.class);
                        value.setFoodId(data.getKey());

                        foodList.add(restoreItem(value));
                    } catch (NullPointerException nEx){
                        Toast.makeText(DailyOfferActivity.this, "Opss. Try again", Toast.LENGTH_SHORT).show();
                    }
                }

                initializeCardLayout();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DailyOfferActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public FoodInfo restoreItem(FoodInfo foodInfo) {
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
        } else if (id == R.id.nav_soldOrders) {
            startActivity(new Intent(this, SoldOrderActivity.class));
            finish();
        } else if (id == R.id.nav_contactUs) {

        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout_daily_menu);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void OnFoodClickFood(int position) {

        Intent intent = new Intent(DailyOfferActivity.this, DailyFoodEditActivity.class);
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

            branchDailyFood.child(deletedItemId).removeValue();

            Snackbar snackbar = Snackbar
                    .make(rv, name + " dish removed", Snackbar.LENGTH_LONG);
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

    @Override
    public void logout() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("login", false);
        editor.apply();

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