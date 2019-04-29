package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DailyActivityEditFavorite extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        RVAdapter.OnFoodListener, RecyclerItemTouchHelperFood.RecyclerItemTouchHelperListener{

    private SharedPreferences sharedpref;
    private List<FoodInfo> foodList;
    private RVAdapter myAdapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_edit_favorite);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the UP button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeCardLayout();

    }

    private void getFavoriteFoodInfo()
    {
        sharedpref = getSharedPreferences("foodFav", Context.MODE_PRIVATE);

        foodList = new ArrayList<>();
        int numberOfFood = sharedpref.getInt("numberOfFood", 0);

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

                foodList.add(new FoodInfo(" ", " ", foodName, Integer.parseInt(foodPrice), Integer.parseInt(foodQuantity), foodDescription));

            }
        }
    }

    private void initializeCardLayout() {
        getFavoriteFoodInfo();

        rv = (RecyclerView) findViewById(R.id.rvFavFood);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        myAdapter = new RVAdapter(foodList, this, DailyActivityEditFavorite.this);
        rv.setAdapter(myAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperFood(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rv);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    @Override
    public void OnFoodClickFood(int position) {

        Intent intent = new Intent(DailyActivityEditFavorite.this, DailyActivityEdit.class);
        intent.putExtra("food_selected", "favorite");
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
