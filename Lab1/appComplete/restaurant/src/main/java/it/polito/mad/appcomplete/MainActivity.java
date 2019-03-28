package it.polito.mad.appcomplete;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        im = findViewById(R.id.imageView1);
        name = findViewById(R.id.textViewName);
        phone = findViewById(R.id.textViewTelephone);
        address = findViewById(R.id.textViewAddress);
        email = findViewById(R.id.textViewEmail);
        description = findViewById(R.id.textViewDescription);

        sharedpref = getSharedPreferences("userinfo", Context.MODE_PRIVATE);

        //displayData();
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

        if(sharedpref.getBoolean("firstTime", true) == false) {
            byte[] imageAsBytes = Base64.decode(imageDecoded, Base64.DEFAULT);

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
