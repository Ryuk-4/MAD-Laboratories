package it.polito.mad.customer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jaeger.library.StatusBarUtil;
import com.squareup.picasso.Picasso;

import it.polito.mad.data_layer_access.FirebaseUtils;


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


    /**
     *  ---------------------------
     *  system callbacks
     *  ---------------------------
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initLayout();

        displayData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.edit_action){
            Intent intent = new Intent(this, EditActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_restaurant) {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_orders) {
            Intent intent = new Intent(ProfileActivity.this, OrdersActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_profile);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     *  ------------------------------
     *  programmer defined functions
     *  ------------------------------
     */

    private void initLayout() {
        setContentView(R.layout.drawer_profile);
        toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);

        FirebaseUtils.setupFirebaseCustomer();

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
        im = findViewById(R.id.imageView1);

        sharedpref = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
    }


    public void displayData() {
        SharedPreferences.Editor editor = sharedpref.edit();

        editor.putBoolean("saved", false);
        editor.apply();

        DatabaseReference databaseReference = FirebaseUtils.branchCustomerProfile;
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object o;
                String nameEdit = "";
                String phoneEdit = "";
                String addressEdit = "";
                String emailEdit = "";
                String surnameEdit = "";
                String dateEdit = "";
                String sexString = "";
                String imageURL = "";

                o = dataSnapshot.child("name").getValue();
                if (o != null)
                {
                    nameEdit = o.toString();
                }

                o = dataSnapshot.child("email").getValue();
                if (o != null)
                {
                    emailEdit = o.toString();
                }

                o = dataSnapshot.child("surname").getValue();
                if (o != null)
                {
                    surnameEdit = o.toString();
                }

                o = dataSnapshot.child("phone").getValue();
                if (o != null)
                {
                    phoneEdit = o.toString();
                }

                o = dataSnapshot.child("address").getValue();
                if (o != null)
                {
                    addressEdit = o.toString();
                }

                o = dataSnapshot.child("dateOfBirth").getValue();
                if (o != null)
                {
                    dateEdit = o.toString();
                }

                o = dataSnapshot.child("sex").getValue();
                if (o != null)
                {
                    sexString = o.toString();
                }

                o = dataSnapshot.child("photo").getValue();
                if (o != null)
                {
                    imageURL = o.toString();
                }

                if (imageURL.compareTo("") != 0)
                    Picasso.get().load(imageURL).into(im);

                name.setText(nameEdit);
                phone.setText(phoneEdit);
                address.setText(addressEdit);
                email.setText(emailEdit);
                dateOfBirth.setText(dateEdit);
                surname.setText(surnameEdit);
                sex.setText(sexString);


                sharedpref.edit().clear().commit();
                SharedPreferences.Editor editor = sharedpref.edit();

                editor.putString("name", nameEdit);
                editor.putString("surname", surnameEdit);
                editor.putString("phone", phoneEdit);
                editor.putString("address", addressEdit);
                editor.putString("dateOfBirth", dateEdit);
                editor.putString("sex", sexString);
                editor.putString("imageEncoded", imageURL);

                editor.commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
