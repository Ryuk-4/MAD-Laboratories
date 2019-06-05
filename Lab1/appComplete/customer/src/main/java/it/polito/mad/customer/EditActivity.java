package it.polito.mad.customer;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaeger.library.StatusBarUtil;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;

import it.polito.mad.data_layer_access.Costants;
import it.polito.mad.data_layer_access.FirebaseUtils;
import it.polito.mad.data_layer_access.ImageUtils;

public class EditActivity extends AppCompatActivity{

    private ImageView im_edit;
    private EditText name_edit;
    private EditText surname_edit;
    private EditText phone_edit;
    private EditText address_edit;
    private TextView dateOfBirth;
    private RadioGroup radioSex;
    private byte[] photoByteArray;
    private SharedPreferences sharedPreferences;

    /**
     *  ----------------------
     *  system callbacks part
     *  ----------------------
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case  Costants.MY_CAMERA_PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                    startActivityForResult(ImageUtils.chooseFromCamera(), Costants.CAMERA_REQ);
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }
                break;

            case Costants.GALLERY_REQ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "gallery permission granted", Toast.LENGTH_LONG).show();
                    startActivityForResult(ImageUtils.chooseFromGallery(), Costants.GALLERY_REQ);
                } else {
                    Toast.makeText(this, "gallery permission denied", Toast.LENGTH_LONG).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        initSystem();

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        initSystem();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            if(!sharedPreferences.getBoolean("saved", false)){
                Toast.makeText(this, "Changes not saved!", Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putByteArray("profilePicture", photoByteArray);

        outState.putString("name", name_edit.getText().toString());
        outState.putString("surname", surname_edit.getText().toString());
        outState.putString("phone", phone_edit.getText().toString());
        outState.putString("address", address_edit.getText().toString());
        outState.putString("dateOfBirth", dateOfBirth.getText().toString());

        int sexId = radioSex.getCheckedRadioButtonId();
        View radioButton = radioSex.findViewById(sexId);
        int idx = radioSex.indexOfChild(radioButton);

        RadioButton r = (RadioButton) radioSex.getChildAt(idx);
        outState.putString("sex", r.getText().toString());
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("TAG", "onRestoreInstanceState: ");

        photoByteArray = savedInstanceState.getByteArray("profilePicture");

        if(photoByteArray != null){
            im_edit.setImageBitmap(BitmapFactory.decodeByteArray(photoByteArray,
                    0, photoByteArray.length));
        }

        name_edit.setText(savedInstanceState.getString("name"));
        surname_edit.setText(savedInstanceState.getString("surname"));
        phone_edit.setText(savedInstanceState.getString("phone"));
        address_edit.setText(savedInstanceState.getString("address"));
        dateOfBirth.setText(savedInstanceState.getString("dateOfBirth"));

        String s = savedInstanceState.getString("sex", "");

        if (s.compareTo(getString(R.string.radioMale)) == 0)
        {
            radioSex.check(R.id.radioMale);
        } else
        {
            radioSex.check(R.id.radioFemale);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photo;

        if(requestCode == Costants.GALLERY_REQ && resultCode == RESULT_OK){
            if(data != null){
                try{
                    final Uri contentURI = data.getData();
                    final InputStream stream;
                    if (contentURI != null) {
                        stream = getContentResolver().openInputStream(contentURI);

                        photo = BitmapFactory.decodeStream(stream);

                        photo = getResizedBitmap(photo, 500);

                        photoByteArray = ImageUtils.bitmapToByteArray(photo);
                        im_edit.setImageBitmap(photo);

                        Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        } else if( requestCode == Costants.CAMERA_REQ && resultCode == RESULT_OK){
            if (data != null) {
                photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                im_edit.setImageBitmap(photo);

                if (photo != null) {
                    photoByteArray = ImageUtils.bitmapToByteArray(photo);
                    Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        if(!sharedPreferences.getBoolean("saved", false)){
            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

            pictureDialog.setTitle("Exit:");
            pictureDialog.setMessage("The changes have not been saved. Are you sure to exit?");
            pictureDialog.setNegativeButton(android.R.string.no, null);
            pictureDialog.setPositiveButton(android.R.string.yes, (dialog, which) -> EditActivity.super.onBackPressed());

            pictureDialog.show();
        }
    }


    /**
     *  ----------------------------
     *  programmer defined functions
     *  ----------------------------
     */

    private void initSystem() {
        initToolbar();

        FirebaseUtils.setupFirebaseCustomer();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        getLayoutReferences();

        sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);

        addListenersToButton();

        displayData();

        StatusBarUtil.setTransparent(this);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_restaurant);
        setSupportActionBar(toolbar);
    }

    private void getLayoutReferences() {
        name_edit = findViewById(R.id.editTextName);
        phone_edit = findViewById(R.id.editTextTelephone);
        address_edit = findViewById(R.id.editTextAddress);
        dateOfBirth = findViewById(R.id.dateOfBirthString);
        surname_edit = findViewById(R.id.editTextSurname);
        radioSex = findViewById(R.id.radioSex);
        im_edit = findViewById(R.id.imageView1);
    }

    private void addListenersToButton() {
        FloatingActionButton ib = findViewById(R.id.imageButton);

        ib.setOnClickListener(
                v -> ImageUtils.showPictureDialog(this)
        );

        Button buttonSave = findViewById(R.id.button);
        buttonSave.setOnClickListener(
                v -> saveInfo(v)
        );

        ImageButton calendarButton = findViewById(R.id.iconOpenCalendar);
        calendarButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog;
            datePickerDialog = new DatePickerDialog(EditActivity.this, (view, year, month, dayOfMonth) -> dateOfBirth.setText(String.format("%d/%d/%d", dayOfMonth, month, year)), 1970, 0, 1 );
            datePickerDialog.show();
        });
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void saveInfo(View v){
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if(TextUtils.isEmpty(dateOfBirth.getText().toString()) || TextUtils.isEmpty(surname_edit.getText().toString()) || TextUtils.isEmpty(name_edit.getText().toString()) || TextUtils.isEmpty(phone_edit.getText().toString()) ||
                TextUtils.isEmpty(address_edit.getText().toString())){

            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

            pictureDialog.setTitle("Warning");
            pictureDialog.setMessage("All the fields must be filled.");
            pictureDialog.setPositiveButton(android.R.string.ok, null);
            pictureDialog.show();
        } else{
            final DatabaseReference databaseReference = FirebaseUtils.branchCustomerProfile;
            int sexId = radioSex.getCheckedRadioButtonId();
            View radioButton = radioSex.findViewById(sexId);
            int idx = radioSex.indexOfChild(radioButton);

            RadioButton r = (RadioButton) radioSex.getChildAt(idx);

            if (sharedPreferences.getString("name", "").compareTo(name_edit.getText().toString()) != 0)
            {
                editor.putString("name", name_edit.getText().toString());
                databaseReference.child("name").setValue(name_edit.getText().toString());
            }
            if (sharedPreferences.getString("surname", "").compareTo(surname_edit.getText().toString()) != 0)
            {
                editor.putString("surname", surname_edit.getText().toString());
                databaseReference.child("surname").setValue(surname_edit.getText().toString());
            }
            if (sharedPreferences.getString("phone", "").compareTo(phone_edit.getText().toString()) != 0)
            {
                editor.putString("phone", phone_edit.getText().toString());
                databaseReference.child("phone").setValue(phone_edit.getText().toString());
            }
            if (sharedPreferences.getString("address", "").compareTo(address_edit.getText().toString()) != 0)
            {
                editor.putString("address", address_edit.getText().toString());
                databaseReference.child("address").setValue(address_edit.getText().toString());
            }
            if (sharedPreferences.getString("dateOfBirth", "").compareTo(dateOfBirth.getText().toString()) != 0)
            {
                editor.putString("dateOfBirth", dateOfBirth.getText().toString());
                databaseReference.child("dateOfBirth").setValue(dateOfBirth.getText().toString());
            }
            if (sharedPreferences.getString("sex", "").compareTo(r.getText().toString()) != 0)
            {
                editor.putString("sex", r.getText().toString());
                databaseReference.child("sex").setValue(r.getText().toString());
            }

            Bitmap bitmap = ((BitmapDrawable) im_edit.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            final StorageReference ref = FirebaseUtils.storageCustomerProfileImage;
            final UploadTask uploadTask = (UploadTask) ref.putBytes(data).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                databaseReference.child("photo").setValue(uri.toString());

                SharedPreferences.Editor editor1 = sharedPreferences.edit();
                editor1.putString("imageEncoded", uri.toString());
                editor1.apply();
            }));

            editor.putBoolean("saved", true);

            editor.apply();

            Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
        }
    }


    public void displayData() {
        String imageDecoded = sharedPreferences.getString("imageEncoded", "");

        String nameEdit = sharedPreferences.getString("name", "");
        String phoneEdit = sharedPreferences.getString("phone", "");
        String addressEdit = sharedPreferences.getString("address", "");
        String surnameEdit = sharedPreferences.getString("surname", "");
        String dateEdit = sharedPreferences.getString("dateOfBirth", "");
        String sexEdit = sharedPreferences.getString("sex", "");

        if(imageDecoded.compareTo("") != 0) {
            Picasso.get().load(imageDecoded).into(im_edit);
        }
        name_edit.setText(nameEdit);
        phone_edit.setText(phoneEdit);
        address_edit.setText(addressEdit);
        dateOfBirth.setText(dateEdit);
        surname_edit.setText(surnameEdit);

        if (sexEdit.compareTo(getString(R.string.radioMale)) == 0)
        {
            radioSex.check(R.id.radioMale);
        } else
        {
            radioSex.check(R.id.radioFemale);
        }
    }

}