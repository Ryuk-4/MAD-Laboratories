package it.polito.mad.appcomplete;

import android.content.Intent;
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
import android.util.Log;
import android.view.MenuItem;

public class ReservationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ReservationActivityInterface {

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


        if(savedInstanceState == null) {
            incFragment = new IncomingReservationFragment();

            prepFragment = new PreparingReservationFragment();

            endFragment = new ReadyToGoReservationFragment();
        } else {
            incFragment = (IncomingReservationFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:2131296316:0");
            if(incFragment == null) {   incFragment = new IncomingReservationFragment();    }

            prepFragment = (PreparingReservationFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:2131296316:1");
            if(prepFragment == null) {  prepFragment = new PreparingReservationFragment();  }

            endFragment = (ReadyToGoReservationFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:2131296316:2");
            if (endFragment == null) {  endFragment = new ReadyToGoReservationFragment();   }

            }

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.containerTabs);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(mViewPager);
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
    public void processReservation(String fragmentTag, ReservationInfo reservation) {
        if (fragmentTag.equals(getString(R.string.tab_incoming))) {
            if (reservation != null) {
                Log.d(TAG, "processReservation: " + getString(R.string.tab_incoming));
                prepFragment.newReservationHasSent(reservation);
            }
        } else if (fragmentTag.equals(getString(R.string.tab_preparation))) {
            Log.d(TAG, "processReservation: " + getString(R.string.tab_preparation));
            if (reservation != null) {
                endFragment.newReservationHasSent(reservation);
            }
        }
    }

    @Override
    public void undoOperation(String fragmentTag) {
        Log.d(TAG, "undoOperation: called");
        if (fragmentTag.equals(getString(R.string.tab_incoming))) {
            prepFragment.removeItem();
        } else if (fragmentTag.equals(getString(R.string.tab_preparation))) {
            endFragment.removeItem();
        }
    }
}
