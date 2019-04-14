package it.polito.mad.customer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.jaeger.library.StatusBarUtil;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ImageView im;
    private Toolbar toolbar;
    private TextView surname;
    private TextView sex;
    private TextView dateOfBirth;
    private TextView name;
    private TextView phone;
    private TextView address;
    private TextView email;
    private SharedPreferences sharedpref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initLayout();

        displayData();
    }

    private void initLayout() {
        setContentView(R.layout.drawer_profile);
        toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);

        getLayoutReference();

        StatusBarUtil.setTransparent(this);

        initDrawer();
    }

    private void initDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_profile);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(ProfileActivity.this);
    }

    private void getLayoutReference() {
        name = findViewById(R.id.textViewName);
        phone = findViewById(R.id.textViewTelephone);
        address = findViewById(R.id.textViewAddress);
        email = findViewById(R.id.textViewEmail);
        surname = findViewById(R.id.textViewSurname);
        sex = findViewById(R.id.textViewSex);
        dateOfBirth = findViewById(R.id.textViewDateOfBirth);
        //im = findViewById(R.id.imageView1);

        sharedpref = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
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
            Intent intent = new Intent(this, EditActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayData() {
        String imageDecoded = sharedpref.getString("imageEncoded", "");
        byte[] imageAsBytes = Base64.decode(imageDecoded, Base64.DEFAULT);

        SharedPreferences.Editor editor = sharedpref.edit();

        editor.putBoolean("saved", false);
        editor.apply();

        String nameEdit = sharedpref.getString("name", "");
        String phoneEdit = sharedpref.getString("phone", "");
        String addressEdit = sharedpref.getString("address", "");
        String emailEdit = sharedpref.getString("email", "");
        String surnameEdit = sharedpref.getString("surname", "");
        String dateEdit = sharedpref.getString("dateOfBirth", "");
        String sexString = sharedpref.getString("sex", "");

        //if(imageAsBytes != null) {
        //    im.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes,
        //            0, imageAsBytes.length));
        //}

        name.setText(nameEdit);
        phone.setText(phoneEdit);
        address.setText(addressEdit);
        email.setText(emailEdit);
        dateOfBirth.setText(dateEdit);
        surname.setText(surnameEdit);
        sex.setText(sexString);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_restaurant) {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_contactUs) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_profile);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
