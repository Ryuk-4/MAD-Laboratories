package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView im;
    private Toolbar toolbar;
    private TextView name;
    private TextView phone;
    private TextView address;
    private TextView email;
    private TextView description;
    private SharedPreferences sharedpref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_menu_profile);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the UP button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_profile);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        im = findViewById(R.id.foodImage);
        name = findViewById(R.id.textViewName);
        phone = findViewById(R.id.textViewTelephone);
        address = findViewById(R.id.textViewAddress);
        email = findViewById(R.id.textViewEmail);
        description = findViewById(R.id.textViewDescription);

        sharedpref = getSharedPreferences("userinfo", Context.MODE_PRIVATE);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_reservation) {
            Intent intent = new Intent(this, ReservationActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_dailyMenu) {
            Intent intent = new Intent(this, DailyOfferActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_contactUs) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_profile);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_profile);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        displayData();
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

        if(id == R.id.edit_action){
            //This action will happen when is clicked the edit button in the action bar
            Intent intent = new Intent(this, ProfileEditActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayData() {
        String imageDecoded = sharedpref.getString("imageEncoded", "");

        if(sharedpref.getBoolean("firstTime", true) == false) {
            byte[] imageAsBytes = Base64.decode(imageDecoded, Base64.DEFAULT);
            SharedPreferences.Editor editor = sharedpref.edit();

            editor.putBoolean("saved", false);
            editor.apply();

            String nameEdit = sharedpref.getString("name", "");
            String phoneEdit = sharedpref.getString("phone", "");
            String addressEdit = sharedpref.getString("address", "");
            String emailEdit = sharedpref.getString("email", "");
            String descriptionEdit = sharedpref.getString("description", "");

            if (imageAsBytes != null) {
                im.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes,
                        0, imageAsBytes.length));
            }

            name.setText(nameEdit);
            phone.setText(phoneEdit);
            address.setText(addressEdit);
            email.setText(emailEdit);
            description.setText(descriptionEdit);
        }
    }
}
