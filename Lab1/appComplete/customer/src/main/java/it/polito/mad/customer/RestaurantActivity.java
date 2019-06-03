package it.polito.mad.customer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.jaeger.library.StatusBarUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.polito.mad.data_layer_access.FirebaseUtils;

public class RestaurantActivity

        extends AppCompatActivity

        implements DailyFoodFragment.OnFragmentInteractionListener,
        ReviewFragment.OnFragmentInteractionListenerReview{

    private static final int REQUEST_CART = 12;
    private BottomNavigationView bottomNavigationView;
    private ImageView imageView;
    private Toolbar toolbar;
    private String restId, restName;
    private List<SuggestedFoodInfo> dailyFoodInfoList;
    private List<ReviewInfo> reviewInfoList;
    private myFragmentPageAdapter adapter;
    private ViewPager mViewPager;
    private TextView restaurantNameText, restaurantDescription;
    private List<OrderRecap> orders;


    /**
     *  -------------------------------
     *  system callbacks
     *  -------------------------------
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        deletePreviousCart(this.getSharedPreferences("orders_info", Context.MODE_PRIVATE));

        getDataFromIntent(savedInstanceState);

        initSystem();

        StatusBarUtil.setTransparent(this);
    }

    @Override
    public void onBackPressed() {
        int nFood = getSharedPreferences("orders_info", Context.MODE_PRIVATE).getInt("n_food", 0);

        if (nFood != 0) {
            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

            pictureDialog.setTitle("Exit:");
            pictureDialog.setMessage("The content of your cart will be deleted. Are you sure to exit?");
            pictureDialog.setNegativeButton(android.R.string.no, null);
            pictureDialog.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                SharedPreferences sharedPreferences = getSharedPreferences("orders_info", Context.MODE_PRIVATE);

                sharedPreferences.edit().clear().commit();

                RestaurantActivity.super.onBackPressed();
            });

            pictureDialog.show();
        } else {
            RestaurantActivity.super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("food", (ArrayList<? extends Parcelable>) dailyFoodInfoList);
        outState.putString("restId", restId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        dailyFoodInfoList = savedInstanceState.getParcelableArrayList("food");
        restId = savedInstanceState.getString("restId");
    }

    //implement if tabs need to exchange data
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        restId = this.getSharedPreferences("saved", Context.MODE_PRIVATE).getString("id", "");

        initSystem();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CART) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSharedPreferences("saved_restaurant", MODE_PRIVATE).edit().putString("restId", restId).commit();
    }

    @Override
    public void onFragmentInteractionReview(Uri uri) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_restaurant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.go_to_cart) {
            item.setEnabled(false);
            SharedPreferences sharedPreferences = this.getSharedPreferences("orders_info", Context.MODE_PRIVATE);
            int n_food = sharedPreferences.getInt("n_food", 0);
            orders = new ArrayList<>();

            for (int i = 0; i < n_food; i++) {
                String amount = sharedPreferences.getString("amount" + i, "");
                if ((amount != "") && (Integer.parseInt(amount) != 0)) {
                    String price = sharedPreferences.getString("price" + i, "");
                    String name = sharedPreferences.getString("food" + i, "");
                    String key = sharedPreferences.getString("key" + i, "");
                    orders.add(new OrderRecap(price, amount, name, key));
                }
            }

            decreaseQuantityOfFood();

        } else if (item.getItemId() == android.R.id.home) {
            int nFood = getSharedPreferences("orders_info", Context.MODE_PRIVATE).getInt("n_food", 0);

            if (nFood != 0) {
                AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

                pictureDialog.setTitle("Exit:");
                pictureDialog.setMessage("The content of your cart will be deleted. Are you sure to exit?");
                pictureDialog.setNegativeButton(android.R.string.no, null);
                pictureDialog.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("orders_info", Context.MODE_PRIVATE);

                    sharedPreferences.edit().clear().commit();

                    RestaurantActivity.super.onBackPressed();
                });

                pictureDialog.show();
            } else
            {
                RestaurantActivity.super.onBackPressed();
            }
        }

        return true;
    }


    /**
     * --------------------------------------
     * programmer defined functions
     * --------------------------------------
     */

    private void initSystem() {
        dailyFoodInfoList = new ArrayList<>();
        reviewInfoList = new ArrayList<>();

        getLayoutReferences();

        toolbar = findViewById(R.id.toolbar_restaurant);
        setSupportActionBar(toolbar);

        deleteStatusBarTitle();

        initBottomNavigation();

        FirebaseUtils.setupFirebaseCustomer();

        getRestaurantInformation();
    }

    private void initBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId())
            {
                case R.id.daily_food:
                    mViewPager.setCurrentItem(0);
                    break;
                case R.id.reviews:
                    mViewPager.setCurrentItem(1);
                    break;
            }

            return false;
        });
    }


    private void getRestaurantInformation() {

        FirebaseUtils.branchRestaurant.child(restId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dailyFoodInfoList = new ArrayList<>();
                reviewInfoList = new ArrayList<>();

                Object object = dataSnapshot.child("Profile").child("imgUrl").getValue();
                String photoURLrestaurant = "";

                if (object != null)
                    photoURLrestaurant = object.toString();

                object = dataSnapshot.child("Profile").child("name").getValue();
                String name = "";

                if (object != null)
                {
                    name = object.toString();
                    restaurantNameText.setText(name);
                }

                object = dataSnapshot.child("Profile").child("description").getValue();
                StringBuilder description = new StringBuilder("'");

                if (object != null)
                {
                    description.append(object.toString());
                    description.append("'");
                    restaurantDescription.setText(description.toString());
                }

                getDataDailyFood(dataSnapshot);
                getDataReviews(dataSnapshot);

                adapter = new myFragmentPageAdapter(RestaurantActivity.this, getSupportFragmentManager(), dailyFoodInfoList, reviewInfoList);
                mViewPager.setAdapter(adapter);

                if (!photoURLrestaurant.equals("")) {
                    GetBitmapFromURLAsync getBitmapFromURLAsync = new GetBitmapFromURLAsync();
                    getBitmapFromURLAsync.execute(photoURLrestaurant);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deletePreviousCart(SharedPreferences orders_info) {
        orders_info.edit().clear().commit();
    }

    private void getDataFromIntent(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            restId = getIntent().getStringExtra("restaurant_selected");
            restName = getIntent().getStringExtra("restaurant_name");

            this.getSharedPreferences("saved", Context.MODE_PRIVATE).edit().putString("id", restId).commit();
        }
    }

    private void deleteStatusBarTitle() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void getLayoutReferences() {
        imageView = findViewById(R.id.htab_header);
        restaurantNameText = findViewById(R.id.restaurant_name_header);
        restaurantDescription = findViewById(R.id.restaurant_description_header);
        mViewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottom_view);
    }

    private void getDataDailyFood(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.child("Daily_Food").getChildren()) {
            Object o;
            String key = ds.getKey();

            o = ds.child("name").getValue();
            String name = "";

            if (o != null)
            {
                name = o.toString();
            }

            o = ds.child("description").getValue();
            String description = "";

            if (o != null)
            {
                description = o.toString();
            }

            o = ds.child("price").getValue();
            String price = "0";

            if (o != null)
            {
                price = o.toString();
            }

            o = ds.child("image").getValue();
            String photoURLfood = "0";

            if (o != null)
                photoURLfood = o.toString();

            o = ds.child("quantity").getValue();
            String quantity = "0";

            if (o != null)
            {
                quantity = o.toString();
            }

            if (Integer.parseInt(quantity) != 0)
                dailyFoodInfoList.add(new SuggestedFoodInfo(name, description, photoURLfood, price, key, quantity));
        }
    }

    private void getDataReviews(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.child("review_description").getChildren())
        {
            Object o;

            o = ds.child("title").getValue();
            String title = "";

            if (o != null)
            {
                title = o.toString();
            }

            o = ds.child("description").getValue();
            String description = "";

            if (o != null)
            {
                description = o.toString();
            }

            o = ds.child("date").getValue();
            String date = "";

            if (o != null)
            {
                date = o.toString();
            }

            o = ds.child("stars").getValue();
            String rate = "0";

            if (o != null)
            {
                rate = o.toString();
            }

            reviewInfoList.add(new ReviewInfo(rate, title, description, date));
        }
    }



    @SuppressLint("StaticFieldLeak")
    private class GetBitmapFromURLAsync extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            return getBitmapFromURL(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

    private Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void decreaseQuantityOfFood() {
        FirebaseUtils.branchRestaurant.child(restId).child("Daily_Food")
                .runTransaction( new Transaction.Handler(){

                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData){
                        for (OrderRecap o : orders)
                        {
                            for (MutableData mutableData : currentData.getChildren())
                            {
                                String key = mutableData.getKey();

                                if (key.compareTo(o.getKey()) == 0)
                                {
                                    String quantity = "0";
                                    Object obj = mutableData.child("quantity").getValue();

                                    if (obj != null)
                                    {
                                        quantity = obj.toString();
                                    }

                                    if (Integer.parseInt(quantity) < Integer.parseInt(o.getQuantity()))
                                    {
                                        return Transaction.abort();
                                    }

                                    mutableData.child("quantity").setValue(String.valueOf(Integer.parseInt(quantity) - Integer.parseInt(o.getQuantity())));
                                }
                            }
                        }
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot currentData){
                        if (committed)
                        {
                            Intent intent = new Intent(RestaurantActivity.this, CartActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("data", (ArrayList<? extends Parcelable>) orders);
                            bundle.putString("restId", restId);
                            bundle.putString("restName", restName);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, REQUEST_CART);
                        } else
                        {
                            Toast.makeText(RestaurantActivity.this, "The food you've ordered may not be available", Toast.LENGTH_LONG).show();
                            getRestaurantInformation();
                        }
                    }
                });
    }

}