package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ReservationEditActivity extends AppCompatActivity {

    private EditText name_edit;
    private EditText time_edit;
    private EditText address_edit;
    private EditText email_edit;
    private EditText phone_edit;
    private Button save_button;
    private SharedPreferences sharedpref;

    private ReservationInfo reservationInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name_edit = findViewById(R.id.person_name_edit);
        time_edit = findViewById(R.id.reservation_time_edit);
        address_edit = findViewById(R.id.person_address_edit);
        email_edit = findViewById(R.id.person_email_edit);
        phone_edit = findViewById(R.id.person_phone_edit);
        save_button = findViewById(R.id.saveReservationButton);

        sharedpref = getSharedPreferences("reservation_info", Context.MODE_PRIVATE);

        if (getIntent().hasExtra("reservation_selected")) {
            reservationInfo = getIntent().getParcelableExtra("reservation_selected");


            name_edit.setText(reservationInfo.getNamePerson());
            time_edit.setText(reservationInfo.getTimeReservation());
            address_edit.setText(reservationInfo.getAddressPerson());
            email_edit.setText(reservationInfo.getEmail());
            phone_edit.setText(reservationInfo.getPhonePerson());
        }

        save_button.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveInfo(v);
                    }
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getItemId() == android.R.id.home) {
            if (sharedpref.getBoolean("saved", false) == false) {
                Toast.makeText(this, "Changes not saved!", Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (sharedpref.getBoolean("saved", false) == false) {
            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

            pictureDialog.setTitle("Exit:");
            pictureDialog.setMessage("The changes have not been saved. Are you sure to exit?");
            pictureDialog.setNegativeButton(android.R.string.no, null);
            pictureDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ReservationEditActivity.super.onBackPressed();
                }
            });

            pictureDialog.show();
        } else {
            super.onBackPressed();
        }
    }

    public void saveInfo(View v) {
        SharedPreferences.Editor editor = sharedpref.edit();

        if (TextUtils.isEmpty(name_edit.getText().toString()) || TextUtils.isEmpty(phone_edit.getText().toString()) ||
                TextUtils.isEmpty(address_edit.getText().toString()) || TextUtils.isEmpty(email_edit.getText().toString())) {

            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

            pictureDialog.setTitle("Warning");
            pictureDialog.setMessage("All the fields must be filled.");
            pictureDialog.setPositiveButton(android.R.string.ok, null);
            pictureDialog.show();
        } else {
            //TODO
            //Use FIREBASE instead of SharedPreferences

            //Store the couple <key, value> into the SharedPreferences
            editor.putString("name", name_edit.getText().toString());
            editor.putString("phone", phone_edit.getText().toString());
            editor.putString("address", address_edit.getText().toString());
            editor.putString("email", email_edit.getText().toString());
            editor.putString("timeReservation", time_edit.getText().toString());
            editor.putBoolean("saved", true);

            if(sharedpref.getBoolean("firstTime", true) == true){
                editor.putBoolean("firstTime", false);
            }

            editor.apply();

            Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("name", name_edit.getText().toString());
        outState.putString("phone", phone_edit.getText().toString());
        outState.putString("address", address_edit.getText().toString());
        outState.putString("email", email_edit.getText().toString());
        outState.putString("timeReservation", time_edit.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        name_edit.setText(savedInstanceState.getString("name"));
        phone_edit.setText(savedInstanceState.getString("phone"));
        address_edit.setText(savedInstanceState.getString("address"));
        email_edit.setText(savedInstanceState.getString("email"));
        time_edit.setText(savedInstanceState.getString("timeReservation"));

    }

}
