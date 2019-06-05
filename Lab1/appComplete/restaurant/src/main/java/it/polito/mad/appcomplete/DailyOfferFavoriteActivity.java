package it.polito.mad.appcomplete;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static it.polito.mad.data_layer_access.FirebaseUtils.*;

public class DailyOfferFavoriteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        RVAdapter.OnFoodListener, RecyclerItemTouchHelperFood.RecyclerItemTouchHelperListener {

    private static final String TAG = "DailyOfferFavoriteActiv";

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

        setupFirebase();
        getFavoriteFoodInfo();
    }

    private void getFavoriteFoodInfo() {
        Log.d(TAG, "getFavoriteFoodInfo: called");

        branchFavouriteFood.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: called");
                foodList = new ArrayList<>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    try{
                        FoodInfo value = data.getValue(FoodInfo.class);
                        value.setFoodId(data.getKey());

                        foodList.add(restoreItem(value));
                    } catch (NullPointerException nEx){
                        Log.w(TAG, "onDataChange: ", nEx);
                    }
                }

                initializeCardLayout();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });
    }

    private void initializeCardLayout() {
        rv = (RecyclerView) findViewById(R.id.rvFavFood);

        Log.d(TAG, "initializeCardLayout: called");

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        myAdapter = new RVAdapter(foodList, this, DailyOfferFavoriteActivity.this);
        rv.setAdapter(myAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperFood(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rv);

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    @Override
    public void OnFoodClickFood(int position) {

        Intent intent = new Intent(DailyOfferFavoriteActivity.this, DailyFoodEditActivity.class);
        intent.putExtra("food_selected", "favourite");
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

            branchFavouriteFood.child(deletedItemId).removeValue();

            Snackbar snackbar = Snackbar
                    .make(rv, name + " removed", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    branchFavouriteFood.child(deletedItemId).setValue(restoreItem(deletedItem));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
