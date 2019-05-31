package it.polito.mad.customer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.jaeger.library.StatusBarUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import it.polito.mad.data_layer_access.FirebaseUtils;

public class CartActivity extends AppCompatActivity{

    private static final int AUTOCOMPLETE_REQUEST = 1;
    private LinearLayout cart;
    private TextView totalAmount;
    private TextView userLocation;
    private String restId;
    private Button buttonSend, buttonDiscard;
    private ImageView imageLocation;
    private List<OrderRecap> list;
    private Spinner spinnerTime;
    private String restName;
    private Toolbar toolbar;

    /**
     *  -----------------------------
     *  default system callbacks part
     *  -----------------------------
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        getInfoFromExtra();

        initSystem();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //setContentView(R.layout.activity_cart);

        initSystem();
    }

    @Override
    public void onBackPressed() {
        increaseQuantityOfFood();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        increaseQuantityOfFood();
        return true;
    }

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST)
        {
            if (resultCode == RESULT_OK) //the user selected a place
            {
                if (data != null)
                {
                    Place place = Autocomplete.getPlaceFromIntent(data);

                    if (place.getLatLng() != null)
                    {
                        SharedPreferences sharedPreferences = getSharedPreferences("user_location", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("lat", Double.toString(place.getLatLng().latitude));
                        editor.putString("lon", Double.toString(place.getLatLng().longitude));
                        editor.commit();

                        userLocation.setText(place.getName());
                    }
                }
            }
        }
    }

    /**
     *  ----------------------------
     *  programmer defined functions
     *  ----------------------------
     */

    /**
     *  initializes all the CartActivity system
     */
    private void initSystem() {
        initLayoutReferences();

        FirebaseUtils.setupFirebaseCustomer();

        setupActionBar();

        int partial = createCart();

        totalAmount.setText(String.format("%s€", Integer.toString(partial)));

        setTextviewLocation();

        addListenerToButtons();

        //StatusBarUtil.setTransparent(this);
        StatusBarUtil.setColor(this, this.getColor(R.color.colorPrimary));
    }


    private void setupActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private int createCart() {
        int partial = 0;
        cart.removeAllViews();

        for (OrderRecap orderRecap : list)
        {
            partial = addFoodOrder(partial, orderRecap);
        }

        return partial;
    }


    private void getInfoFromExtra() {
        Bundle bundle = getIntent().getExtras();

        if (bundle != null)
        {
            list = bundle.getParcelableArrayList("data");
            restId = bundle.getString("restId");
            restName = bundle.getString("restName");
        }
    }


    @SuppressLint("ApplySharedPref")
    private void addListenerToButtons() {
        buttonSend.setOnClickListener(v -> {
            String orderId = saveOrderToRestaurant();
            saveOrderToCustomer(orderId);

            setResult(RESULT_OK);
            finish();
        });


        buttonDiscard.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("orders_info", Context.MODE_PRIVATE);

            sharedPreferences.edit().clear().commit();
            increaseQuantityOfFood();
        });

        imageLocation.setOnClickListener(v -> {
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), CartActivity.this.getString(R.string.google_maps_key));
            }

            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

            SharedPreferences sharedPreferences1 = getSharedPreferences("user_location", MODE_PRIVATE);
            String lat1 = sharedPreferences1.getString("lat", "0.0");
            String lon1 = sharedPreferences1.getString("lon", "0.0");

            RectangularBounds bounds = RectangularBounds.newInstance(
                    new LatLng(Double.parseDouble(lat1)-0.03, Double.parseDouble(lon1)-0.03),
                    new LatLng(Double.parseDouble(lat1)+0.03, Double.parseDouble(lon1)+0.03));

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).setLocationRestriction(bounds).build(CartActivity.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST);
        });

        userLocation.setOnClickListener(v -> {
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), CartActivity.this.getString(R.string.google_maps_key));
            }

            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

            SharedPreferences sharedPreferences1 = getSharedPreferences("user_location", MODE_PRIVATE);
            String lat1 = sharedPreferences1.getString("lat", "0.0");
            String lon1 = sharedPreferences1.getString("lon", "0.0");

            RectangularBounds bounds = RectangularBounds.newInstance(
                    new LatLng(Double.parseDouble(lat1)-0.03, Double.parseDouble(lon1)-0.03),
                    new LatLng(Double.parseDouble(lat1)+0.03, Double.parseDouble(lon1)+0.03));

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).setLocationRestriction(bounds).build(CartActivity.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST);
        });
    }


    /**
     *  get all the references from the layout
     */
    private void initLayoutReferences() {
        cart = findViewById(R.id.customer_cart);
        totalAmount = findViewById(R.id.total_amount);
        buttonSend = findViewById(R.id.button_send);
        spinnerTime = findViewById(R.id.spinner_time);
        buttonDiscard = findViewById(R.id.button_discard);
        userLocation = findViewById(R.id.user_location);
        imageLocation = findViewById(R.id.image_location);
        toolbar = findViewById(R.id.toolbar_cart);
    }


    private int addFoodOrder(int partial, OrderRecap orderRecap) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        String name = orderRecap.getName();
        String quantity = orderRecap.getQuantity();
        String price = orderRecap.getPrice();

        partial += Integer.parseInt(price) * Integer.parseInt(quantity);

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTag(orderRecap.getKey());
        tvName.setPadding(10, 0, 0, 0);
        tvName.setTextSize(22);
        tvName.setTypeface(null, Typeface.BOLD);
        TextView tvQuantity = new TextView(this);
        tvQuantity.setText(String.format("%spcs", quantity));
        tvQuantity.setTextSize(18);
        tvQuantity.setPadding(20, 0, 0, 0);
        TextView tvPrice = new TextView(this);
        tvPrice.setText(String.format("%sx%s€", quantity, price));
        tvPrice.setTextSize(18);
        tvPrice.setGravity(Gravity.END);
        tvPrice.setPadding(0, 0, 10, 0);

        View v = new View(this);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        v.setBackgroundColor(this.getColor(android.R.color.black));

        linearLayout.addView(tvName);
        linearLayout.addView(tvQuantity);
        linearLayout.addView(tvPrice);
        linearLayout.addView(v);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(0, 10, 0, 10);

        cart.addView(linearLayout, layoutParams);

        return partial;
    }


    private String saveOrderToRestaurant() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants").child(restId).child("Orders").child("Incoming").push();

        //StringBuffer totalOrder = new StringBuffer("");
        for (OrderRecap o : list)
        {
            DatabaseReference dr = databaseReference.child("OrderList").child(o.getKey());
            dr.child("Name").setValue(o.getName());
            dr.child("quantity").setValue(o.getQuantity());
        }

        databaseReference.child("idPerson").setValue(FirebaseAuth.getInstance().getUid());
        databaseReference.child("note").setValue(" ");
        databaseReference.child("timeReservation").setValue(spinnerTime.getSelectedItem().toString());

        String name = CartActivity.this.getSharedPreferences("userinfo", Context.MODE_PRIVATE).getString("name", "") +
                CartActivity.this.getSharedPreferences("userinfo", Context.MODE_PRIVATE).getString("surname", "");

        if (name != null && name.compareTo("") != 0)
        {
            databaseReference.child("namePerson").setValue(name);
        }
        else
        {
            databaseReference.child("namePerson").setValue(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());
        }

        SharedPreferences sharedPreferences = getSharedPreferences("user_location", MODE_PRIVATE);
        String lat = sharedPreferences.getString("lat", "");
        String lon = sharedPreferences.getString("lon", "");

        databaseReference.child("cLatitude").setValue(lat);
        databaseReference.child("cLongitude").setValue(lon);

        return databaseReference.getKey();
    }


    private void saveOrderToCustomer(String orderId)
    {
        DatabaseReference databaseReference = FirebaseUtils.branchCustomerPreviousOrder.child(orderId);

        for (OrderRecap o : list)
        {
            String key = databaseReference.child("food").child(o.getKey()).getKey();

            databaseReference.child("food").child(key).child("foodName").setValue(o.getName());
            databaseReference.child("food").child(key).child("foodPrice").setValue(o.getPrice());
            databaseReference.child("food").child(key).child("foodQuantity").setValue(o.getQuantity());
        }

        databaseReference.child("timeReservation").setValue(spinnerTime.getSelectedItem().toString());
        databaseReference.child("addressReservation").setValue(userLocation.getText().toString());
        databaseReference.child("restaurant").setValue(restId);
        databaseReference.child("restaurant_name").setValue(restName);
        databaseReference.child("order_status").setValue("pending");
    }


    private void setTextviewLocation()
    {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), CartActivity.this.getString(R.string.google_maps_key));
        }

        SharedPreferences sharedPreferences = getSharedPreferences("user_location", MODE_PRIVATE);
        String lat = sharedPreferences.getString("lat", "0.0");
        String lon = sharedPreferences.getString("lon", "0.0");

        String address = null;
        if (lat != null && lon != null) {
            address = getAddressFromLocation(this, Double.parseDouble(lat), Double.parseDouble(lon));
        }

        userLocation.setText(address);
    }

    /**
     *  given a latitude and longitude it uses the Geocoer class to get the corresponding String address
     */
    public String getAddressFromLocation(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            String add = obj.getAddressLine(0);
            add = add + "," + obj.getAdminArea();
            add = add + "," + obj.getCountryName();

            return add;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    public void increaseQuantityOfFood()
    {
        FirebaseDatabase.getInstance().getReference("restaurants").child(restId).child("Daily_Food")
                .runTransaction( new Transaction.Handler(){

                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData){
                        for (OrderRecap o : list)
                        {
                            for (MutableData mutableData : currentData.getChildren())
                            {
                                String key = mutableData.getKey();

                                if (key != null && key.compareTo(o.getKey()) == 0)
                                {
                                    String quantity = "0";
                                    Object obj = mutableData.child("quantity").getValue();

                                    if (obj != null)
                                    {
                                        quantity = obj.toString();
                                    }

                                    mutableData.child("quantity").setValue(Integer.parseInt(quantity) + Integer.parseInt(o.getQuantity()));
                                }
                            }
                        }
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot currentData){
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
    }

}
