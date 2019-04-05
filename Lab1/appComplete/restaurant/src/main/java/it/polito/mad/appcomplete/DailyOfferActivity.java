package it.polito.mad.appcomplete;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class DailyOfferActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
                RVAdapter.OnFoodListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener{


    private RecyclerView rv;
    private List<FoodInfo> foodList;
    private int numberOfFood;
    private SharedPreferences sharedpref;
    private Spinner spinner;
    private ArrayAdapter<CharSequence> adapter1;
    private RVAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_offer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the UP button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DailyOfferActivity.this, DailyActivityEdit.class);
                startActivity(intent);
            }
        });

        sharedpref = getSharedPreferences("foodinfo", Context.MODE_PRIVATE);



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

        initializeCardLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initializeCardLayout();
    }

    private void initializeCardLayout() {
        initializeData();

        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

         myAdapter = new RVAdapter(foodList, this, DailyOfferActivity.this);
        rv.setAdapter(myAdapter);

        ItemTouchHelper.SimpleCallback itemSimpleCallback = new RecyclerItemTouchHelper(0,
                ItemTouchHelper.LEFT, this);
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
                if (sharedpref.getString("day" + i, "").compareTo(spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString()) == 0) {
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
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_dailyMenu) {
            Intent intent = new Intent(this, DailyOfferActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_contactUs) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_reservation);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void OnFoodClickFood(int position) {
        SharedPreferences.Editor editor = sharedpref.edit();

        Intent intent = new Intent(DailyOfferActivity.this, ReservationEditActivity.class);
        intent.putExtra("food_selected", foodList.get(position));

        editor.putInt("food_position", position);
        editor.apply();

        startActivity(intent);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

    }
}





