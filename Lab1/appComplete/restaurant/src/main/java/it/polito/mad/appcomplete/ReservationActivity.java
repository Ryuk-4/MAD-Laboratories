package it.polito.mad.appcomplete;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ReservationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

        private List<ReservationInfo> reservationInfoList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_menu_reservation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_reservation);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ReservationActivity.this, "Pressed +", Toast.LENGTH_LONG).show();
            }
        });

        initializeReservation();
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

    private void initializeReservation(){
        reservationInfoList = new ArrayList<>();

        reservationInfoList.add(new ReservationInfo("John", "20:00",
                "Via Roma, 48", "899888899", "john@example.com"));

        reservationInfoList.add(new ReservationInfo("Jane", "20:30",
                "Via Bobbio, 28", "894888849", "jane@example.com"));

        reservationInfoList.add(new ReservationInfo("Lucy", "19:30",
                "Via Cristoforo Colombo, 12", "829888491", "lucy@example.com"));
        reservationInfoList.add(new ReservationInfo("Mary", "19:45",
                "Via Enaudi, 1", "829123491", "mary@example.com"));

        initializeRecyclerViewReservation();
    }

    private void initializeRecyclerViewReservation(){
        RecyclerView recyclerView = findViewById(R.id.recyclerViewReservation);
        RecyclerViewAdapterReservation adapter = new RecyclerViewAdapterReservation(this, reservationInfoList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /* TODO
    *
    *  Add clickListener to a cardView for Edit/Delete one
    *  Add event to floating button to insert a new card
    *
    * */
}
