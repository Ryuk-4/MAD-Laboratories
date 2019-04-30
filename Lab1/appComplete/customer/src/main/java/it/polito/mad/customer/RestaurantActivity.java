package it.polito.mad.customer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaeger.library.StatusBarUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RestaurantActivity

        extends AppCompatActivity

        implements  MenuFragment.OnFragmentInteractionListener,
                    DailyFoodFragment.OnFragmentInteractionListener,
                    ReviewFragment.OnFragmentInteractionListener {

    private static final int REQUEST_CART = 12;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ImageView imageView;
    private Toolbar toolbar;
    private String restId;
    private List<SuggestedFoodInfo> dailyFoodInfoList;
    private myFragmentPageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TAG", "onCreate: RestaurantActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        toolbar = findViewById(R.id.toolbar_restaurant);
        setSupportActionBar(toolbar);

        dailyFoodInfoList = new ArrayList<>();



        tabLayout = findViewById(R.id.htab_tabs);
        imageView = findViewById(R.id.htab_header);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        StatusBarUtil.setTransparent(this);

        if (savedInstanceState == null)
            restId = getIntent().getStringExtra("restaurant_selected");

        FirebaseDatabase.getInstance().getReference("restaurants").child(restId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String photoURLrestaurant = dataSnapshot.child("Profile").child("imgUrl").getValue().toString();

                getDataDailyFood(dataSnapshot);
                getDataMenu(dataSnapshot); //to be implemented
                getDataReviews(dataSnapshot); //to be implemented


                adapter = new myFragmentPageAdapter(RestaurantActivity.this, getSupportFragmentManager(), dailyFoodInfoList);

                viewPager = findViewById(R.id.htab_viewpager);
                viewPager.setAdapter(adapter);
                tabLayout.setupWithViewPager(viewPager);

                Picasso.get().load(photoURLrestaurant).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        StatusBarUtil.setTransparent(this);
    }

    private void getDataDailyFood(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot ds: dataSnapshot.child("Daily_Food").getChildren())
        {
            String name = ds.child("name").getValue().toString();
            String description = ds.child("description").getValue().toString();
            String price = ds.child("price").getValue().toString();
            Object o = ds.child("image").getValue();
            String photoURLfood = new String("");

            if (o != null)
                 photoURLfood = o.toString();

            dailyFoodInfoList.add(new SuggestedFoodInfo(name, description, photoURLfood, price));
        }
    }

    private void getDataMenu(@NonNull DataSnapshot dataSnapshot) {

    }

    private void getDataReviews(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_restaurant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.go_to_cart) {
            SharedPreferences sharedPreferences = this.getSharedPreferences("orders_info", Context.MODE_PRIVATE);
            int n_food = sharedPreferences.getInt("n_food", 0);
            List<OrderRecap> orders = new ArrayList<OrderRecap>();

            for (int i = 0 ; i < n_food ; i++)
            {
                String amount = sharedPreferences.getString("amount"+i, "");
                if ((amount != "") && (Integer.parseInt(amount) != 0))
                {
                    String price = sharedPreferences.getString("price"+i, "");
                    String name = sharedPreferences.getString("food"+i, "");

                    orders.add(new OrderRecap(price, amount, name));
                }
            }

            Intent intent = new Intent(this, CartActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("data", (ArrayList<? extends Parcelable>) orders);
            bundle.putString("restId", restId);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_CART);

        } else if(item.getItemId() == android.R.id.home){
            int nFood = getSharedPreferences("orders_info", Context.MODE_PRIVATE).getInt("n_food", 0);


            if (nFood != 0)
            {
                AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

                pictureDialog.setTitle("Exit:");
                pictureDialog.setMessage("The content of your cart will be deleted. Are you sure to exit?");
                pictureDialog.setNegativeButton(android.R.string.no, null);
                pictureDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        SharedPreferences sharedPreferences = getSharedPreferences("orders_info", Context.MODE_PRIVATE);

                        sharedPreferences.edit().clear().commit();

                        finish();
                    }
                });

                pictureDialog.show();
            }
        }

        return true;
    }


    @Override
    public void onBackPressed() {
        int nFood = getSharedPreferences("orders_info", Context.MODE_PRIVATE).getInt("n_food", 0);

        if (nFood != 0)
        {
            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

            pictureDialog.setTitle("Exit:");
            pictureDialog.setMessage("The content of your cart will be deleted. Are you sure to exit?");
            pictureDialog.setNegativeButton(android.R.string.no, null);
            pictureDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which){
                    SharedPreferences sharedPreferences = getSharedPreferences("orders_info", Context.MODE_PRIVATE);

                    sharedPreferences.edit().clear().commit();

                    RestaurantActivity.super.onBackPressed();
                }
            });

            pictureDialog.show();
        } else
        {
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

        Log.d("TAG", "onRestart: RestaurantAvtivity");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("TAG", "onStart: RestaurantAvtivity");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("TAG", "onActivityResult: RestaurantActivity");

        if (requestCode == REQUEST_CART)
        {
            if (resultCode == RESULT_OK)
            {
                finish();
            } else if (resultCode == RESULT_CANCELED)
            {
                adapter.refreshLayout(0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "onDestroy: RestaurantActivity");
    }
}
