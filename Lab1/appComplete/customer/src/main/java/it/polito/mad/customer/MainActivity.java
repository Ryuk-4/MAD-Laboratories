package it.polito.mad.customer;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaeger.library.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnRestaurantListener,
        RVANormalRestaurant.updateRestaurantList{

    private FirebaseAuth.AuthStateListener authListener;
    private List<String> foodSelected;
    private FirebaseAuth auth;
    private LinearLayout search_filter_option, type_of_food;
    private ImageButton buttonFilter, buttonSearch;
    private RecyclerView rvSuggested, rvNormal;
    private RVASuggestedRestaurant myAdapterSuggested;
    private RVANormalRestaurant myAdapterNormal;
    private CoordinatorLayout coordinator;
    private EditText textSearch;
    private static final int FOOD_EACH_LINE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getLayoutReferences();
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

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

        addTypeOfFoodFilter();

        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapseExpandFilterView();
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTyped = textSearch.getText().toString();

                foodSelected = new ArrayList<>();
                int count = type_of_food.getChildCount();
                View view = null;
                TextView innerView = null;

                for (int i = 0 ; i < count ; i++)
                {
                    view = type_of_food.getChildAt(i);
                    for (int j = 0 ; j < FOOD_EACH_LINE ; j++)
                    {
                        innerView = (TextView)((LinearLayout)view).getChildAt(j);

                        if (innerView == null)
                            break;

                        if ((int)innerView.getTag() == R.drawable.rounded_corner_green)
                        {
                            foodSelected.add(innerView.getText().toString());
                        }
                    }
                }

                onUpdateListNormalFiltered(searchTyped, foodSelected);
            }
        });

        StatusBarUtil.setTransparent(this);

        initDrawer(toolbar);

        initializeCardLayout();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        setContentView(R.layout.drawer_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getLayoutReferences();
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

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

        addTypeOfFoodFilter();

        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapseExpandFilterView();
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTyped = textSearch.getText().toString();

                foodSelected = new ArrayList<>();
                int count = type_of_food.getChildCount();
                View view = null;
                TextView innerView = null;

                for (int i = 0 ; i < count ; i++)
                {
                    view = type_of_food.getChildAt(i);
                    for (int j = 0 ; j < FOOD_EACH_LINE ; j++)
                    {
                        innerView = (TextView)((LinearLayout)view).getChildAt(j);

                        if (innerView == null)
                            break;

                        if ((int)innerView.getTag() == R.drawable.rounded_corner_green)
                        {
                            foodSelected.add(innerView.getText().toString());
                        }
                    }
                }

                onUpdateListNormalFiltered(searchTyped, foodSelected);
            }
        });

        StatusBarUtil.setTransparent(this);

        initDrawer(toolbar);

        initializeCardLayout();
    }

    private void addTypeOfFoodFilter() {
        int support = 0;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0 ; i < getResources().getStringArray(R.array.type_of_food).length ; i++)
        {
            if (support == FOOD_EACH_LINE)
            {
                type_of_food.addView(linearLayout);
                linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                support = 0;
            }

            TextView textView = new TextView(this);
            textView.setPadding(10, 10, 10, 10);
            //textView.setId(i);
            textView.setTextSize(18);
            textView.setText(getResources().getStringArray(R.array.type_of_food)[i]);
            textView.setTextColor(this.getResources().getColor(R.color.white));
            textView.setBackground(this.getResources().getDrawable(R.drawable.rounded_corner_red));
            textView.setTag(R.drawable.rounded_corner_red);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = (TextView) v;

                    if ((int)tv.getTag() == R.drawable.rounded_corner_red)
                    {
                        tv.setBackground(getResources().getDrawable(R.drawable.rounded_corner_green));
                        tv.setTag(R.drawable.rounded_corner_green);
                    } else
                    {
                        tv.setBackground(getResources().getDrawable(R.drawable.rounded_corner_red));
                        tv.setTag(R.drawable.rounded_corner_red);
                    }
                }
            });

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(0, 10, 16, 10);

            linearLayout.addView(textView, layoutParams);

            support++;
        }

        if (support != 0)
        {
            type_of_food.addView(linearLayout);
        }
    }

    private void getLayoutReferences() {
        coordinator = findViewById(R.id.coordinator);
        search_filter_option = findViewById(R.id.search_filter_option);
        buttonFilter = findViewById(R.id.filter_button);
        type_of_food = findViewById(R.id.type_of_food);
        buttonSearch = findViewById(R.id.button_search);
        textSearch = findViewById(R.id.text_search);
    }

    private void initDrawer(Toolbar toolbar) {
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
        initializeData();

        findViewById(R.id.progress_bar_favorite).setVisibility(View.GONE);
        findViewById(R.id.progress_bar_normal).setVisibility(View.GONE);
    }

    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_restaurant) {

        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

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
        onUpdateListNormal();
        onUpdateListSuggested();
    }

    @Override
    public void OnRestaurantClick(String id) {
        Intent intent = new Intent(MainActivity.this, RestaurantActivity.class);
        intent.putExtra("restaurant_selected", id);
        startActivity(intent);
    }

    @Override
    public void onUpdateListNormal()
    {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String name = ds.child("name").getValue().toString();
                    String photo = ds.child("photo").getValue().toString();
                    String description = ds.child("description").getValue().toString();
                    String id = ds.getKey();

                    int[] votes;
                    int nVotes = 0;
                    votes = new int[5];

                    for (int i = 0 ; i < 5 ; i++)
                    {
                        votes[i] = Integer.parseInt(ds.child("review").child((i+1)+"star").getValue().toString());
                        nVotes+=votes[i];
                    }

                    int i = 1;
                    List<String> typeFood = new ArrayList<>();
                    while (true)
                    {
                        Object o = ds.child("type_food").child("type"+i).getValue();
                        if (o != null)
                            typeFood.add(o.toString());
                        else
                            break;

                        i++;
                    }
                    //Log.d("TAG", "onDataChange: inserted "+myAdapterNormal.getItemCount());
                    myAdapterNormal.restoreItem(new RestaurantInfo(name, nVotes, votes, description, id, typeFood, photo), myAdapterNormal.getItemCount());
                }
                //Log.d("TAG", "onDataChange: finish");
                myAdapterNormal.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants_tmp");
        databaseReference.addValueEventListener(valueEventListener);
    }

    public void onUpdateListNormalFiltered(final String nameRestaurant, final List<String> typeOfFood)
    {
        myAdapterNormal = new RVANormalRestaurant(this, this);
        rvNormal.setAdapter(myAdapterNormal);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String name = ds.child("name").getValue().toString();
                    String photo = ds.child("photo").getValue().toString();
                    String description = ds.child("description").getValue().toString();
                    String id = ds.getKey();

                    int[] votes;
                    int nVotes = 0;
                    votes = new int[5];

                    for (int i = 0 ; i < 5 ; i++)
                    {
                        votes[i] = Integer.parseInt(ds.child("review").child((i+1)+"star").getValue().toString());
                        nVotes+=votes[i];
                    }

                    int i = 1;
                    List<String> typeFood = new ArrayList<>();
                    while (true)
                    {
                        Object o = ds.child("type_food").child("type"+i).getValue();
                        if (o != null)
                            typeFood.add(o.toString());
                        else
                            break;

                        i++;
                    }

                    if (nameRestaurant == "" || name.indexOf(nameRestaurant) != -1)
                    {
                        if (name.indexOf(nameRestaurant) != -1 && typeOfFood.size() == 0)
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
                }
                myAdapterNormal.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants_tmp");
        databaseReference.addValueEventListener(valueEventListener);
    }


    public void onUpdateListSuggested()
    {
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("customers").child(auth.getUid()).child("previous_order");
        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> restaurantId = new ArrayList<>();

                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    restaurantId.add(ds.getValue().toString());
                }

                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String name = dataSnapshot.child("name").getValue().toString();
                        String photo = dataSnapshot.child("photo").getValue().toString();
                        String description = dataSnapshot.child("description").getValue().toString();
                        String id = dataSnapshot.getKey();

                        int[] votes;
                        int nVotes = 0;
                        votes = new int[5];

                        for (int i = 0 ; i < 5 ; i++)
                        {
                            votes[i] = Integer.parseInt(dataSnapshot.child("review").child((i+1)+"star").getValue().toString());
                            nVotes+=votes[i];
                        }

                        int i = 1;
                        List<String> typeFood = new ArrayList<>();
                        while (true)
                        {
                            Object o = dataSnapshot.child("type_food").child("type"+i).getValue();
                            if (o != null)
                                typeFood.add(o.toString());
                            else
                                break;

                            i++;
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
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants_tmp").child(s);
                    databaseReference.addValueEventListener(valueEventListener);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void collapseExpandFilterView() {
        if (search_filter_option.getVisibility() == View.GONE) {
            // it's collapsed - expand it
            search_filter_option.setVisibility(View.VISIBLE);
        } else {
            // it's expanded - collapse it
            search_filter_option.setVisibility(View.GONE);
        }
    }
}