package it.polito.mad.customer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnRestaurantListener{

    private static final int LOCATION_REQUEST = 111;
    private static final int RADIUS = 50; //radius for geoFire query
    private static  final int AUTOCOMPLETE_REQUEST = 222;
    private FirebaseAuth.AuthStateListener authListener;
    private List<String> foodSelected;
    private FirebaseAuth auth;
    private ImageButton buttonSearch;
    private RecyclerView rvSuggested, rvNormal;
    private RVASuggestedRestaurant myAdapterSuggested;
    private RVANormalRestaurant myAdapterNormal;
    private AutoCompleteTextView textSearch;
    private Toolbar toolbar;
    public boolean updating = false;
    private FusedLocationProviderClient fusedLocationClient;
    private MyCustomLocation userLocation = null;
    private ProgressBar progressBar1, progressBar2;
    private Set<String> keyList;
    private View coordinator;
    private TextView textLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_main);

        getSharedPreferences("user_location", MODE_PRIVATE).edit().clear().commit();

        initSystem();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setContentView(R.layout.drawer_main);

        initSystem();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }


    private void initSystem() {

        keyList = new TreeSet<>();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), MainActivity.this.getString(R.string.google_maps_key));
        }

        getLayoutReferences();

        initToolbar();

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        checkIfUserAuthenticated();

        initAutoCompleteTextSearch();

        addListenersToButtons();

        initDrawer();

        manageUserLocation();

        initializeCardLayout();

        StatusBarUtil.setTransparent(this);


    }

    private void initToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void addListenersToButtons() {
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTyped = textSearch.getText().toString();

                foodSelected = new ArrayList<>();

                for (String s : getResources().getStringArray(R.array.type_of_food))
                {
                    if (s.compareTo(searchTyped.toUpperCase()) == 0)
                    {
                        foodSelected.add(s);
                    }
                }
                onUpdateListNormalFiltered(searchTyped, foodSelected);
            }
        });
    }

    private void initAutoCompleteTextSearch() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, R.layout.dropdown, getResources().getStringArray(R.array.type_of_food));

        textSearch.setThreshold(1);
        textSearch.setAdapter(adapter);
        textSearch.setDropDownBackgroundResource(R.color.light_background_color);
    }

    private void checkIfUserAuthenticated() {
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST)
        {
            if (resultCode == RESULT_OK) //the user selected a place
            {

                Place place = Autocomplete.getPlaceFromIntent(data);
                userLocation = new MyCustomLocation(place.getLatLng().latitude, place.getLatLng().longitude);

                SharedPreferences sharedPreferences = getSharedPreferences("user_location", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("lat", Double.toString(userLocation.getLatitude()));
                editor.putString("lon", Double.toString(userLocation.getLongitude()));
                editor.commit();

                textLocation.setText(place.getName());

                initializeData();

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) //some internal error
            {
                //TODO implement this case
            } else if (resultCode == RESULT_CANCELED) //the use pressed back
            {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Missing address");
                alertDialogBuilder.setMessage("Please insert a valid address");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!Places.isInitialized()) {
                            Places.initialize(getApplicationContext(), MainActivity.this.getString(R.string.google_maps_key));
                        }

                        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

                        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(MainActivity.this);
                        startActivityForResult(intent, AUTOCOMPLETE_REQUEST);
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case  LOCATION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "location permission granted", Toast.LENGTH_LONG).show();
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customers_position");
                                        GeoFire geoFire = new GeoFire(ref);
                                        geoFire.setLocation(FirebaseAuth.getInstance().getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {
                                                if (error != null) {
                                                    System.err.println("There was an error saving the location to GeoFire: " + error);
                                                } else {
                                                    System.out.println("Location saved on server successfully!");
                                                }
                                            }
                                        });
                                    }

                                    userLocation = new MyCustomLocation(location.getLatitude(), location.getLongitude());
                                    textLocation.setText(getAddressFromLocation(MainActivity.this, userLocation.getLatitude(), userLocation.getLongitude()));

                                    SharedPreferences sharedPreferences = getSharedPreferences("user_location", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("lat", Double.toString(location.getLatitude()));
                                    editor.putString("lon", Double.toString(location.getLongitude()));
                                    editor.commit();

                                    initializeData();
                                }
                            });
                } else {

                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("Missing address");
                    alertDialogBuilder.setMessage("Your privacy is very important for us but we need a delivery address in order to show all the available restaurants");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

                            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(MainActivity.this);
                            startActivityForResult(intent, AUTOCOMPLETE_REQUEST);
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getLayoutReferences() {
        coordinator = findViewById(R.id.coordinator);
        buttonSearch = findViewById(R.id.button_search);
        textSearch = findViewById(R.id.autoCompleteTextView);
        toolbar = (Toolbar) findViewById(R.id.toolbar_restaurant);
        progressBar1 = findViewById(R.id.progress_bar_favorite);
        progressBar2 = findViewById(R.id.progress_bar_normal);
        textLocation = findViewById(R.id.customer_location);

        progressBar1.setVisibility(View.VISIBLE);
        progressBar2.setVisibility(View.VISIBLE);
    }

    private void initDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(MainActivity.this);
    }

    private void initializeCardLayout() {
        initializeCardLayoutSuggestedRestaurant();
        initializeCardLayoutNormalRestaurant();
    }

    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_restaurant) {

        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_orders) {
            Intent intent = new Intent(MainActivity.this, OrdersActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_contactUs) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initializeCardLayoutSuggestedRestaurant() {
        rvSuggested = (RecyclerView) findViewById(R.id.rvSuggested);
        rvSuggested.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvSuggested.setLayoutManager(llm);

        myAdapterSuggested = new RVASuggestedRestaurant(this, this);
        rvSuggested.setAdapter(myAdapterSuggested);

        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        rvSuggested.setLayoutManager(horizontalLayoutManagaer);
    }

    private void initializeCardLayoutNormalRestaurant() {
        rvNormal = (RecyclerView) findViewById(R.id.rvNormal);
        rvNormal.setHasFixedSize(false);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvNormal.setLayoutManager(llm);

        myAdapterNormal = new RVANormalRestaurant(this, this);
        rvNormal.setAdapter(myAdapterNormal);
    }

    private void initializeData(){
        if (myAdapterNormal.getItemCount() != 0)
        {
            myAdapterNormal.clearAll();
        }

        if (myAdapterSuggested.getItemCount() != 0)
        {
            myAdapterSuggested.clearAll();
        }

        //update list of normal restaurant
        final GeoQuery geoQuery = new GeoFire(FirebaseDatabase.getInstance().getReference("restaurants_position")).queryAtLocation(new GeoLocation(userLocation.getLatitude(), userLocation.getLongitude()), RADIUS);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                keyList.add(key);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        progressBar2.setVisibility(View.GONE);
                        progressBar1.setVisibility(View.GONE);
                        Object o = dataSnapshot.child("Profile").child("name").getValue();
                        String name = "";
                        if (o != null)
                        {
                            name = o.toString();
                        }

                        o = dataSnapshot.child("Profile").child("imgUrl").getValue();
                        String photo = "";

                        if (o != null)
                        {
                            photo = o.toString();
                        }

                        o = dataSnapshot.child("Profile").child("description").getValue();
                        String description = "";

                        if (o != null)
                        {
                            description = o.toString();
                        }

                        String id = dataSnapshot.getKey();

                        int[] votes;
                        int nVotes = 0;
                        votes = new int[5];

                        for (int i = 0 ; i < 5 ; i++)
                        {
                            o = dataSnapshot.child("review").child((i+1)+"star").getValue();

                            if (o != null)
                            {
                                votes[i] = Integer.parseInt(o.toString());
                                nVotes+=votes[i];
                            }
                        }

                        List<String> typeFood = new ArrayList<>();
                        for (DataSnapshot ds1 : dataSnapshot.child("type_food").getChildren())
                        {
                            Object obj = ds1.getValue();
                            if (obj != null)
                                typeFood.add(obj.toString());
                        }
                        myAdapterNormal.restoreItem(new RestaurantInfo(name, nVotes, votes, description, id, typeFood, photo), myAdapterNormal.getItemCount());
                        myAdapterSuggested.restoreItem(new RestaurantInfo(name, nVotes, votes, description, id, typeFood, photo));

                        myAdapterNormal.notifyDataSetChanged();
                        myAdapterSuggested.notifyDataSetChanged();

                        updating = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                for (String key : keyList)
                {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants").child(key);
                    databaseReference.addListenerForSingleValueEvent(valueEventListener);
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void OnRestaurantClick(String id, String name) {
        Intent intent = new Intent(MainActivity.this, RestaurantActivity.class);
        intent.putExtra("restaurant_selected", id);
        intent.putExtra("restaurant_name", name);
        startActivity(intent);
    }


    public void onUpdateListNormalFiltered(final String nameRestaurant, final List<String> typeOfFood)
    {
        myAdapterNormal = new RVANormalRestaurant(this, this);
        rvNormal.setAdapter(myAdapterNormal);


        final GeoQuery geoQuery = new GeoFire(FirebaseDatabase.getInstance().getReference("restaurants_position")).queryAtLocation(new GeoLocation(userLocation.getLatitude(), userLocation.getLongitude()), RADIUS);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        manageNewRestaurant(dataSnapshot, nameRestaurant, typeOfFood);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants").child(key);
                databaseReference.addListenerForSingleValueEvent(valueEventListener);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void manageNewRestaurant(@NonNull DataSnapshot dataSnapshot, String nameRestaurant, List<String> typeOfFood) {
        Object o = dataSnapshot.child("Profile").child("name").getValue();
        String name = "";
        if (o != null)
        {
            name = o.toString();
        }

        o = dataSnapshot.child("Profile").child("imgUrl").getValue();
        String photo = "";

        if (o != null)
        {
            photo = o.toString();
        }

        o = dataSnapshot.child("Profile").child("description").getValue();
        String description = "";

        if (o != null)
        {
            description = o.toString();
        }

        String id = dataSnapshot.getKey();

        int[] votes;
        int nVotes = 0;
        votes = new int[5];

        for (int i = 0 ; i < 5 ; i++)
        {
            o = dataSnapshot.child("review").child((i+1)+"star").getValue();

            if (o != null)
            {
                votes[i] = Integer.parseInt(o.toString());
                nVotes+=votes[i];
            }
        }

        List<String> typeFood = new ArrayList<>();
        for (DataSnapshot ds1 : dataSnapshot.child("type_food").getChildren())
        {
            Object obj = ds1.getValue();
            if (obj != null)
                typeFood.add(obj.toString());
        }
        if (name.toUpperCase().contains(nameRestaurant.toUpperCase()) || nameRestaurant.compareTo("") == 0)
        {
            myAdapterNormal.restoreItem(new RestaurantInfo(name, nVotes, votes, description, id, typeFood, photo), myAdapterNormal.getItemCount());
        } else
        {
            for (String s : typeFood)
            {
                int j;
                for (j = 0 ; j < typeOfFood.size() ; j++)
                {
                    if (s.compareTo(typeOfFood.get(j)) == 0)
                        break;
                }

                if (j != typeOfFood.size())
                {
                    myAdapterNormal.restoreItem(new RestaurantInfo(name, nVotes, votes, description, id, typeFood, photo), myAdapterNormal.getItemCount());
                    break;
                }
            }

        }
    }

    void manageUserLocation()
    {
        SharedPreferences sharedPreferences = this.getSharedPreferences("user_location", MODE_PRIVATE);
        String lat = sharedPreferences.getString("lat", "");
        String lon = sharedPreferences.getString("lon", "");

        if (lat.compareTo("") != 0 && lon.compareTo("") != 0) //if the activity is just restarted nothing need to be done
        {
            userLocation = new MyCustomLocation(Double.parseDouble(lat), Double.parseDouble(lon));
            textLocation.setText(getAddressFromLocation(MainActivity.this, userLocation.getLatitude(), userLocation.getLongitude()));
            initializeData();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        int hasPermissionGallery = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if(hasPermissionGallery == PackageManager.PERMISSION_GRANTED){
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            userLocation = new MyCustomLocation(location.getLatitude(), location.getLongitude());

                            textLocation.setText(getAddressFromLocation(MainActivity.this, userLocation.getLatitude(), userLocation.getLongitude()));

                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customers_position");
                                GeoFire geoFire = new GeoFire(ref);
                                geoFire.setLocation(FirebaseAuth.getInstance().getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {

                                    }
                                });
                            }

                            SharedPreferences sharedPreferences = getSharedPreferences("user_location", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("lat", Double.toString(location.getLatitude()));
                            editor.putString("lon", Double.toString(location.getLongitude()));
                            editor.commit();

                            initializeData();
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
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