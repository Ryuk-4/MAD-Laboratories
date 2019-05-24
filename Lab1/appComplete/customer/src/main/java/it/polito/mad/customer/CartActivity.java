package it.polito.mad.customer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaeger.library.StatusBarUtil;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
    //private EditText orderAddress;
    private String restName;

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

    private void initSystem() {
        Toolbar toolbar = findViewById(R.id.toolbar_cart);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initLayoutReferences();

        int partial = createCart();

        totalAmount.setText(Integer.toString(partial)+"€");

        setTextviewLocation();

        addListenerToButtons();

        //StatusBarUtil.setTransparent(this);
        StatusBarUtil.setColor(this, this.getColor(R.color.colorPrimary));
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
        list = bundle.getParcelableArrayList("data");
        restId = bundle.getString("restId");
        restName = bundle.getString("restName");

        Log.d("TAG", "getInfoFromExtra: "+restName);
    }

    private void addListenerToButtons() {
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String orderId = saveOrderToRestaurant();
                saveOrderToCustomer(orderId);

                setResult(RESULT_OK);
                finish();
            }
        });

        buttonDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("orders_info", Context.MODE_PRIVATE);

                sharedPreferences.edit().clear().commit();
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        imageLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Places.isInitialized()) {
                    Places.initialize(getApplicationContext(), CartActivity.this.getString(R.string.google_maps_key));
                }

                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(CartActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST);
            }
        });
    }

    private void initLayoutReferences() {
        cart = findViewById(R.id.customer_cart);
        totalAmount = findViewById(R.id.total_amount);
        buttonSend = findViewById(R.id.button_send);
        spinnerTime = findViewById(R.id.spinner_time);
        buttonDiscard = findViewById(R.id.button_discard);
        userLocation = findViewById(R.id.user_location);
        imageLocation = findViewById(R.id.image_location);
        //orderAddress = findViewById(R.id.order_address);
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
        tvQuantity.setText(quantity+"pcs");
        tvQuantity.setTextSize(18);
        tvQuantity.setPadding(20, 0, 0, 0);
        TextView tvPrice = new TextView(this);
        tvPrice.setText(quantity+"x"+price+"€");
        tvPrice.setTextSize(18);
        tvPrice.setGravity(Gravity.RIGHT);
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
        //linearLayout.setBackground(this.getResources().getDrawable(R.drawable.rounded_corner_white));

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
        databaseReference.child("namePerson").setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        //databaseReference.child("personOrder").setValue(totalOrder.toString());
        databaseReference.child("note").setValue(" ");
        databaseReference.child("timeReservation").setValue(spinnerTime.getSelectedItem().toString());
        //databaseReference.child("addressOrder").setValue(orderAddress.getText().toString());

        SharedPreferences sharedPreferences = getSharedPreferences("user_location", MODE_PRIVATE);
        String lat = sharedPreferences.getString("lat", "");
        String lon = sharedPreferences.getString("lon", "");

        databaseReference.child("cLatitude").setValue(lat);
        databaseReference.child("cLongitude").setValue(lon);

        return databaseReference.getKey();
    }

    private void saveOrderToCustomer(String orderId)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customers").child(FirebaseAuth.getInstance().getUid()).child("previous_order").child(orderId);

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

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        super.onBackPressed();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

    private void setTextviewLocation()
    {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), CartActivity.this.getString(R.string.google_maps_key));
        }

        SharedPreferences sharedPreferences = getSharedPreferences("user_location", MODE_PRIVATE);
        String lat = sharedPreferences.getString("lat", "0.0");
        String lon = sharedPreferences.getString("lon", "0.0");

        String address = getAddressFromLocation(this, Double.parseDouble(lat), Double.parseDouble(lon));
        userLocation.setText(address);

        userLocation.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (!Places.isInitialized()) {
                    Places.initialize(getApplicationContext(), CartActivity.this.getString(R.string.google_maps_key));
                }

                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(CartActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST)
        {
            if (resultCode == RESULT_OK) //the user selected a place
            {
                Place place = Autocomplete.getPlaceFromIntent(data);

                SharedPreferences sharedPreferences = getSharedPreferences("user_location", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("lat", Double.toString(place.getLatLng().latitude));
                editor.putString("lon", Double.toString(place.getLatLng().longitude));
                editor.commit();

                userLocation.setText(place.getName());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) //some internal error
            {
                //TODO implement this case
            } else if (resultCode == RESULT_CANCELED) //the use pressed back
            {

            }
        }
    }

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
}
