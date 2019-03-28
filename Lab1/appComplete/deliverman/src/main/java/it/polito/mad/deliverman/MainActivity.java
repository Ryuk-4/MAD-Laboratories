package it.polito.mad.deliverman;

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
import android.widget.RadioButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ImageView im;
    private Toolbar toolbar;
    private TextView name;
    private TextView phone;
    private TextView surname;
    private TextView email;
    private TextView sex;
    private RadioButton radioSex;

    private TextView dateBirth;
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
        surname = findViewById(R.id.textViewSurname);
        email = findViewById(R.id.textViewEmail);
        dateBirth = findViewById(R.id.textViewBirthday);
        sex = findViewById(R.id.textViewSex);

        sharedpref = getSharedPreferences("userinfo", Context.MODE_PRIVATE);

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
            Intent intent = new Intent(MainActivity.this, EditActivity.class);
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
            String surnameEdit = sharedpref.getString("surname", "");
            String emailEdit = sharedpref.getString("email", "");
            int sexEdit = sharedpref.getInt("sex", 0);
            String birthEdit = sharedpref.getString("birthdate", "");

            radioSex = findViewById(sexEdit);

            if (imageAsBytes != null) {
                im.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes,
                        0, imageAsBytes.length));
            }

            name.setText(nameEdit);
            phone.setText(phoneEdit);
            surname.setText(surnameEdit);
            email.setText(emailEdit);
            sex.setText(radioSex.getText().toString());
            dateBirth.setText(birthEdit);
        }
    }
}
