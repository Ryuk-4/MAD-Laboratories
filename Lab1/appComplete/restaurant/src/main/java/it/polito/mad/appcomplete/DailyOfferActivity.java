package it.polito.mad.appcomplete;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class DailyOfferActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        RVAdapter.OnFoodListener,
        RecyclerItemTouchHelperFood.RecyclerItemTouchHelperListener{


    private RecyclerView rv;
    private List<FoodInfo> foodList;
    private int numberOfFood;
    private SharedPreferences sharedpref;
    private RVAdapter myAdapter;
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_menu_daily_offer);//ciao
        Toolbar toolbar = findViewById(R.id.toolbarDailyOffer);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_daily_menu);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Show the UP button in the action bar



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
                Intent intent = new Intent(DailyOfferActivity.this, DailyActivityEditFavorite.class);
                startActivity(intent);
            }
        });

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DailyOfferActivity.this, DailyActivityEdit.class);
                startActivity(intent);
            }
        });*/

        sharedpref = getSharedPreferences("foodinfo", Context.MODE_PRIVATE);


/*
        //For spinner
        spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter1 = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initializeCardLayout();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
*/

        initializeCardLayout();
    }

    //in alternativa on start chiedere a malnati
    @Override
    protected void onResume() {
        super.onResume();

        initializeCardLayout();
    }

    private void initializeCardLayout() {
        initializeData();

        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        myAdapter = new RVAdapter(foodList, this, DailyOfferActivity.this);
        rv.setAdapter(myAdapter);


        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperFood(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rv);

    }

    private void initializeData(){
        foodList = new ArrayList<>();
        numberOfFood = sharedpref.getInt("numberOfFood", 0);

        if (numberOfFood == 0){
            //foodList.add(new FoodInfo(null, "Pasta", 10, 15, "Very Good"));
        } else
        {
            for (int i = 0;i < numberOfFood;i++)
            {
                String foodName = sharedpref.getString("foodName" + i, "");
                String foodQuantity = sharedpref.getString("foodQuantity" + i, "");
                String foodPrice = sharedpref.getString("foodPrice" + i, "");
                String foodDescription = sharedpref.getString("foodDescription" + i, "");
                String foodImage = sharedpref.getString("foodImage" + i, "");

                byte[] imageAsBytes = Base64.decode(foodImage, Base64.DEFAULT);
                Bitmap photo = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

                foodList.add(new FoodInfo(photo, foodName, Integer.parseInt(foodPrice), Integer.parseInt(foodQuantity), foodDescription));

            }
        }
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_reservation) {
            Intent intent = new Intent(this, ReservationActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_contactUs) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_profile);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void OnFoodClickFood(int position) {

        Intent intent = new Intent(DailyOfferActivity.this, DailyActivityEdit.class);
        intent.putExtra("food_selected", "normal");
        intent.putExtra("food_position", position);

        startActivity(intent);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof RVAdapter.FoodInfoHolder) {
            String name = foodList.get(viewHolder.getAdapterPosition()).getName();

            // backup of removed item for undo purpose
            final FoodInfo deletedItem = foodList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            myAdapter.removeItem(viewHolder.getAdapterPosition());

            Snackbar snackbar = Snackbar
                    .make(rv, name + "\'s reservation removed", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    myAdapter.restoreItem(deletedItem, deletedIndex);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();

        }
    }
}





