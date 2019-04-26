package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ReservationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RestaurantLoginActivity.RestaurantLoginInterface {

    private static final String TAG = "ReservationActivity";

    /*
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    //The {@link ViewPager} that will host the section contents.
    private ViewPager mViewPager;

    private PreparingReservationFragment prepFragment;
    private ReadyToGoReservationFragment endFragment;
    private IncomingReservationFragment incFragment;

    private Menu mMenu;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_menu_reservation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_reservation);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user == null) {
                    startActivity(new Intent(ReservationActivity.this, RestaurantLoginActivity.class));
                    finish();
                }
            }
        };

        mMenu = navigationView.getMenu();
        mMenu.findItem(R.id.nav_deleteAccount).setVisible(true);

       // if (savedInstanceState == null) {
            incFragment = new IncomingReservationFragment();

            prepFragment = new PreparingReservationFragment();

            endFragment = new ReadyToGoReservationFragment();
        /*} else {
            incFragment = (IncomingReservationFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:2131296316:0");
            if (incFragment == null) {
                incFragment = new IncomingReservationFragment();
            }

            prepFragment = (PreparingReservationFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:2131296316:1");
            if (prepFragment == null) {
                prepFragment = new PreparingReservationFragment();
            }

            endFragment = (ReadyToGoReservationFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:2131296316:2");
            if (endFragment == null) {
                endFragment = new ReadyToGoReservationFragment();
            }

        }*/

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.containerTabs);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == R.id.logoutButton) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);

        // if login == false then hide logout button
        if (!preferences.getBoolean("login", false)) {
            menu.findItem(R.id.logoutButton).setVisible(false);
            menu.findItem(R.id.edit_action).setVisible(false);
            menu.findItem(R.id.nav_deleteAccount).setVisible(false);
        } else {
            menu.findItem(R.id.logoutButton).setVisible(true);
            menu.findItem(R.id.edit_action).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_reservation);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_dailyMenu) {
            Intent intent = new Intent(this, DailyOfferActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_contactUs) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_reservation);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    // It's going to create a section view adapter in which we add the fragments
    private void setupViewPager(@NonNull ViewPager viewPager) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mSectionsPagerAdapter.addFragments(incFragment, "Incoming");
        mSectionsPagerAdapter.addFragments(prepFragment, "Cooking");
        mSectionsPagerAdapter.addFragments(endFragment, "Ready To Go");

        viewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public void logout() {
        SharedPreferences preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("login", false);
        editor.apply();

        mMenu.findItem(R.id.nav_deleteAccount).setVisible(false);
        invalidateOptionsMenu();
        auth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }
}
