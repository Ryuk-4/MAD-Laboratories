package it.polito.mad.customer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.header.HeaderDesign;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.blurry.Blurry;

public class RestaurantActivity

        extends AppCompatActivity

        implements  MenuFragment.OnFragmentInteractionListener,
                    DailyFoodFragment.OnFragmentInteractionListener,
                    ReviewFragment.OnFragmentInteractionListenerReview{

    private static final int REQUEST_CART = 12;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ImageView imageView, imageViewBlur;
    private Toolbar toolbar;
    private String restId, restName;
    private List<SuggestedFoodInfo> dailyFoodInfoList;
    private List<ReviewInfo> reviewInfoList;
    private myFragmentPageAdapter adapter;
    private MaterialViewPager mViewPager;
    private TextView restaurantNameText;
    private Bitmap result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        dailyFoodInfoList = new ArrayList<>();
        reviewInfoList = new ArrayList<>();

        getLayoutReferences();

        toolbar = mViewPager.getToolbar();

        deleteStatusBarTitle();

        deletePreviousCart(this.getSharedPreferences("orders_info", Context.MODE_PRIVATE));

        getDataFromIntent(savedInstanceState);

        getRestaurantInformation();

        setStatusBarTransparent();
    }


    private void getRestaurantInformation() {
        FirebaseDatabase.getInstance().getReference("restaurants").child(restId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

                getDataDailyFood(dataSnapshot);
                getDataReviews(dataSnapshot);

                adapter = new myFragmentPageAdapter(RestaurantActivity.this, getSupportFragmentManager(), dailyFoodInfoList, reviewInfoList);

                viewPager = mViewPager.getViewPager();
                viewPager.setAdapter(adapter);

                mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());

                if (photoURLrestaurant != "") {
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
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void getLayoutReferences() {
        imageView = findViewById(R.id.htab_header);
        imageViewBlur = findViewById(R.id.htab_header_blur);
        restaurantNameText = findViewById(R.id.restaurant_name_header);
        mViewPager = (MaterialViewPager) findViewById(R.id.materialViewPager);
    }

    private void setStatusBarTransparent() {
        //make translucent statusBar on kitkat devices
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void getDataDailyFood(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.child("Daily_Food").getChildren()) {
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

    private void getDataReviews(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.child("review_description").getChildren())
        {
            String title = ds.child("title").getValue().toString();
            String description = ds.child("description").getValue().toString();
            String date = ds.child("date").getValue().toString();
            String rate = ds.child("stars").getValue().toString();

            reviewInfoList.add(new ReviewInfo(rate, title, description, date));
        }
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

            for (int i = 0; i < n_food; i++) {
                String amount = sharedPreferences.getString("amount" + i, "");
                if ((amount != "") && (Integer.parseInt(amount) != 0)) {
                    String price = sharedPreferences.getString("price" + i, "");
                    String name = sharedPreferences.getString("food" + i, "");

                    orders.add(new OrderRecap(price, amount, name));
                }
            }

            Intent intent = new Intent(this, CartActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("data", (ArrayList<? extends Parcelable>) orders);
            bundle.putString("restId", restId);
            bundle.putString("restName", restName);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_CART);

        } else if (item.getItemId() == android.R.id.home) {
            int nFood = getSharedPreferences("orders_info", Context.MODE_PRIVATE).getInt("n_food", 0);


            if (nFood != 0) {
                AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

                pictureDialog.setTitle("Exit:");
                pictureDialog.setMessage("The content of your cart will be deleted. Are you sure to exit?");
                pictureDialog.setNegativeButton(android.R.string.no, null);
                pictureDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = getSharedPreferences("orders_info", Context.MODE_PRIVATE);

                        sharedPreferences.edit().clear().commit();

                        finish();
                    }
                });

                pictureDialog.show();
            }

            finish();
        }

        return true;
    }


    @Override
    public void onBackPressed() {
        int nFood = getSharedPreferences("orders_info", Context.MODE_PRIVATE).getInt("n_food", 0);

        if (nFood != 0) {
            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

            pictureDialog.setTitle("Exit:");
            pictureDialog.setMessage("The content of your cart will be deleted. Are you sure to exit?");
            pictureDialog.setNegativeButton(android.R.string.no, null);
            pictureDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences sharedPreferences = getSharedPreferences("orders_info", Context.MODE_PRIVATE);

                    sharedPreferences.edit().clear().commit();

                    RestaurantActivity.super.onBackPressed();
                }
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

        restId = this.getSharedPreferences("saved", Context.MODE_PRIVATE).getString("restId", "");

        dailyFoodInfoList = new ArrayList<>();
        reviewInfoList = new ArrayList<>();

        getLayoutReferences();

        toolbar = mViewPager.getToolbar();

        deleteStatusBarTitle();

        getRestaurantInformation();
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
            } else if (resultCode == RESULT_CANCELED) {
                adapter.refreshLayout(0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSharedPreferences("saved_restaurant", MODE_PRIVATE).edit().putString("restId", restId);
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    public void onFragmentInteractionReview(Uri uri) {

    }


    private class GetBitmapFromURLAsync extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            return getBitmapFromURL(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //  return the bitmap by doInBackground and store in result
            Blurry.with(RestaurantActivity.this).radius(10).from(bitmap).into(imageViewBlur);
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
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}