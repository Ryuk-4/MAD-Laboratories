package it.polito.mad.madlab1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ImageView im;
    private Toolbar toolbar;
    private TextView name;
    private TextView email;
    private TextView description;
    private SharedPreferences sharedpref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        name = findViewById(R.id.textViewName);
        email = findViewById(R.id.textViewEmail);
        description = findViewById(R.id.textViewDescription);

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
        sharedpref = getSharedPreferences("userinfo", Context.MODE_PRIVATE);

        String nameEdit = sharedpref.getString("name", "");
        String emailEdit = sharedpref.getString("email", "");
        String descriptionEdit = sharedpref.getString("description", "");

        name.setText(nameEdit);
        email.setText(emailEdit);
        description.setText(descriptionEdit);
    }
}
