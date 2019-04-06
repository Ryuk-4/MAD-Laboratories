package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReservationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        RecyclerViewAdapterReservation.OnReservationListener,
        RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String TAG = "ReservationActivity";

    private List<ReservationInfo> reservationInfoList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;
    private SharedPreferences sharedpref_edit;
    private SharedPreferences sharedpref_add;


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

        sharedpref_edit = getSharedPreferences("reservation_info_edit", Context.MODE_PRIVATE);
        sharedpref_add = getSharedPreferences("reservation_info_add", Context.MODE_PRIVATE);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ReservationActivity.this, "Pressed +", Toast.LENGTH_LONG).show();

                Intent intent_add = new Intent(ReservationActivity.this, ReservationAddActivity.class);
                startActivity(intent_add);
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

    private void initializeReservation() {
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

    private void initializeRecyclerViewReservation() {
        recyclerView = findViewById(R.id.recyclerViewReservation);

        Collections.sort(reservationInfoList, ReservationInfo.BY_TIME_ASCENDING);

        myAdapter = new RecyclerViewAdapterReservation(ReservationActivity.this,
                reservationInfoList, this);

        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // adding item touch helper
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    @Override
    public void OnReservationClick(int position) {
        SharedPreferences.Editor editor = sharedpref_edit.edit();

        Intent intent = new Intent(ReservationActivity.this, ReservationEditActivity.class);
        intent.putExtra("reservation_selected", reservationInfoList.get(position));

        editor.putInt("reservation_position", position);
        editor.apply();

        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();

        displayData();

        initializeRecyclerViewReservation();
    }

    /*
     * callback when recycler view is swiped
     * item will be removed on swiped
     * undo option will be provided in snackbar to restore the item
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof RecyclerViewAdapterReservation.ViewHolder) {
            // get the removed item name to display it in snack bar
            String name = reservationInfoList.get(viewHolder.getAdapterPosition()).getNamePerson();

            // backup of removed item for undo purpose
            final ReservationInfo deletedItem = reservationInfoList.get(viewHolder.getAdapterPosition());
            Log.d(TAG, "onSwiped: deletedItem " + deletedItem);
            final int deletedIndex = viewHolder.getAdapterPosition();
            Log.d(TAG, "onSwiped: deletedIndex " + deletedIndex);
            // remove the item from recycler view

            myAdapter.removeItem(viewHolder.getAdapterPosition());

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(recyclerView, name + "\'s reservation removed", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    myAdapter.restoreItem(deletedItem, deletedIndex);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    public void displayData() {
        int position;

        if (sharedpref_edit.getBoolean("firstTime", true) == false) {
            position = sharedpref_edit.getInt("reservation_position", -1);

            if (position != -1) {
                reservationInfoList.get(position).setNamePerson(sharedpref_edit.getString("name", ""));
                reservationInfoList.get(position).setPhonePerson(sharedpref_edit.getString("phone", ""));
                reservationInfoList.get(position).setAddressPerson(sharedpref_edit.getString("address", ""));
                reservationInfoList.get(position).setEmail(sharedpref_edit.getString("email", ""));
                reservationInfoList.get(position).setTimeReservation(sharedpref_edit.getString("timeReservation", ""));
            }
        }

        if (sharedpref_add.getBoolean("firstTime", true) == false) {

            ReservationInfo newReservationInfo = new ReservationInfo();

            newReservationInfo.setNamePerson(sharedpref_add.getString("name", ""));
            newReservationInfo.setPhonePerson(sharedpref_add.getString("phone", ""));
            newReservationInfo.setAddressPerson(sharedpref_add.getString("address", ""));
            newReservationInfo.setEmail(sharedpref_add.getString("email", ""));
            newReservationInfo.setTimeReservation(sharedpref_add.getString("timeReservation", ""));

            myAdapter.addItem(newReservationInfo, myAdapter.getItemCount());
        }
    }
}


/* TODO
*
*   Togliere la edit
*   Tre tab -> In entrata, In preparazione, Da Consegnare
*   2 azioni per lo swipe nel primo tab, 1 nel secondo
*   Togliere il più
*
* */