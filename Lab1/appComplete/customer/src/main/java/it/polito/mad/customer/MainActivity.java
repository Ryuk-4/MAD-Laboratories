package it.polito.mad.customer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaeger.library.StatusBarUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnRestaurantListener,
        RVANormalRestaurant.updateRestaurantList {

    private static final int LOCATION_REQUEST = 111;
    private FirebaseAuth.AuthStateListener authListener;
    private List<String> foodSelected;
    private FirebaseAuth auth;
    private ImageButton buttonSearch;
    private RecyclerView rvSuggested, rvNormal;
    private RVASuggestedRestaurant myAdapterSuggested;
    private RVANormalRestaurant myAdapterNormal;
    private CoordinatorLayout coordinator;
    private AutoCompleteTextView textSearch;
    private Toolbar toolbar;
    public boolean updating = false;
    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_main);

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

                                    userLocation = location;
                                    initializeData();

                                }
                            });
                } else {
                    Toast.makeText(this, "location permission denied", Toast.LENGTH_LONG).show();
                    initializeData();
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        findViewById(R.id.progress_bar_favorite).setVisibility(View.GONE);
        findViewById(R.id.progress_bar_normal).setVisibility(View.GONE);
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

        myAdapterNormal = new RVANormalRestaurant(this, this, this);
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
        if (userLocation == null)
        {
            onUpdateListNormal();
        } else
        {
            Log.d("TAG", "initializeData: geoUpdate");
            final GeoQuery geoQuery = new GeoFire(FirebaseDatabase.getInstance().getReference("restaurants_position")).queryAtLocation(new GeoLocation(userLocation.getLatitude(), userLocation.getLongitude()), 300);
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            Object o = dataSnapshot.child("Profile").child("name").getValue();
                            String name = "";
                            if (o != null)
                            {
                                name = new String(o.toString());
                            }

                            o = dataSnapshot.child("Profile").child("imgUrl").getValue();
                            String photo = new String("");

                            if (o != null)
                            {
                                photo = new String(o.toString());
                            }

                            o = dataSnapshot.child("Profile").child("description").getValue();
                            String description = new String("");

                            if (o != null)
                            {
                                description = new String(o.toString());
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
                            //Log.d("TAG", "onDataChange: inserted "+myAdapterNormal.getItemCount());
                            myAdapterNormal.restoreItem(new RestaurantInfo(name, nVotes, votes, description, id, typeFood, photo), myAdapterNormal.getItemCount());

                            //Log.d("TAG", "onDataChange: finish");
                            myAdapterNormal.notifyDataSetChanged();
                            updating = false;
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

        //update list of suggested restaurant
        onUpdateListSuggested();
    }

    @Override
    public void OnRestaurantClick(String id, String name) {
        Intent intent = new Intent(MainActivity.this, RestaurantActivity.class);
        intent.putExtra("restaurant_selected", id);
        intent.putExtra("restaurant_name", name);
        startActivity(intent);
    }

    @Override
    public void onUpdateListNormal()
    {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long nRest = dataSnapshot.getChildrenCount();
                int count = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    Object o;

                    String name = ds.child("Profile").child("name").getValue().toString();

                    o = ds.child("Profile").child("imgUrl").getValue();
                    String photo = new String("");

                    if (o != null)
                    {
                        photo = new String(o.toString());
                    }

                    o = ds.child("Profile").child("description").getValue();
                    String description = new String("");

                    if (o != null)
                    {
                        description = new String(o.toString());
                    }

                    String id = ds.getKey();

                    int[] votes;
                    int nVotes = 0;
                    votes = new int[5];

                    for (int i = 0 ; i < 5 ; i++)
                    {
                        o = ds.child("review").child((i+1)+"star").getValue();

                        if (o != null)
                        {
                            votes[i] = Integer.parseInt(o.toString());
                            nVotes+=votes[i];
                        }
                    }

                    List<String> typeFood = new ArrayList<>();
                    for (DataSnapshot ds1 : ds.child("type_food").getChildren())
                    {
                        Object obj = ds1.getValue();
                        if (obj != null)
                            typeFood.add(obj.toString());
                    }
                    //Log.d("TAG", "onDataChange: inserted "+myAdapterNormal.getItemCount());
                    myAdapterNormal.restoreItem(new RestaurantInfo(name, nVotes, votes, description, id, typeFood, photo), myAdapterNormal.getItemCount());
                }
                //Log.d("TAG", "onDataChange: finish");
                myAdapterNormal.notifyDataSetChanged();
                updating = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants");
        databaseReference.limitToFirst(100).addListenerForSingleValueEvent(valueEventListener);

    }


    public void onUpdateListNormalFiltered(final String nameRestaurant, final List<String> typeOfFood)
    {
        myAdapterNormal = new RVANormalRestaurant(this, this, this);
        rvNormal.setAdapter(myAdapterNormal);

        if (userLocation == null)
        {
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        manageNewRestaurant(ds, nameRestaurant, typeOfFood);
                    }
                    myAdapterNormal.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants");
            databaseReference.addListenerForSingleValueEvent(valueEventListener);
        } else
        {
            final GeoQuery geoQuery = new GeoFire(FirebaseDatabase.getInstance().getReference("restaurants_position")).queryAtLocation(new GeoLocation(userLocation.getLatitude(), userLocation.getLongitude()), 300);
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

    }

    private void manageNewRestaurant(@NonNull DataSnapshot dataSnapshot, String nameRestaurant, List<String> typeOfFood) {
        Object o = dataSnapshot.child("Profile").child("name").getValue();
        String name = "";
        if (o != null)
        {
            name = new String(o.toString());
        }

        o = dataSnapshot.child("Profile").child("imgUrl").getValue();
        String photo = new String("");

        if (o != null)
        {
            photo = new String(o.toString());
        }

        o = dataSnapshot.child("Profile").child("description").getValue();
        String description = new String("");

        if (o != null)
        {
            description = new String(o.toString());
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
        if (name.toUpperCase().indexOf(nameRestaurant.toUpperCase()) != -1 || nameRestaurant.compareTo("") == 0)
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


    public void onUpdateListSuggested()
    {
        Log.d("TAG", "onUpdateListSuggested: "+FirebaseAuth.getInstance().getUid());
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("customers").child(FirebaseAuth.getInstance().getUid()).child("previous_order");
        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> restaurantId = new TreeSet<>();

                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    Object o = ds.child("restaurant").getValue();
                    if (o != null)
                    {
                        restaurantId.add(o.toString());
                    } else
                    {
                        break;
                    }

                }

                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String name = dataSnapshot.child("Profile").child("name").getValue().toString();
                        String photo = dataSnapshot.child("Profile").child("imgUrl").getValue().toString();
                        String description = dataSnapshot.child("Profile").child("description").getValue().toString();
                        String id = dataSnapshot.getKey();

                        int[] votes;
                        int nVotes = 0;
                        votes = new int[5];

                        for (int i = 0 ; i < 5 ; i++)
                        {
                            Object o = dataSnapshot.child("review").child((i+1)+"star").getValue();

                            if (o != null)
                            {
                                votes[i] = Integer.parseInt(o.toString());
                                nVotes+=votes[i];
                            }
                        }

                        int i = 1;
                        List<String> typeFood = new ArrayList<>();
                        for (DataSnapshot ds : dataSnapshot.child("type_food").getChildren())
                        {
                            Object o = ds.getValue();
                            if (o != null)
                                typeFood.add(o.toString());
                        }
                        //Log.d("TAG", "onDataChange: inserted "+myAdapterNormal.getItemCount());
                        myAdapterSuggested.restoreItem(new RestaurantInfo(name, nVotes, votes, description, id, typeFood, photo), myAdapterSuggested.getItemCount());

                        //Log.d("TAG", "onDataChange: finish");
                        myAdapterSuggested.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                for (String s : restaurantId)
                {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants").child(s);
                    databaseReference.addListenerForSingleValueEvent(valueEventListener);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void manageUserLocation()
    {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        int hasPermissionGallery = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if(hasPermissionGallery == PackageManager.PERMISSION_GRANTED){
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            userLocation = location;

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

                            initializeData();
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
        }
    }
}