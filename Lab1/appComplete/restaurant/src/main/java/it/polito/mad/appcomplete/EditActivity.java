package it.polito.mad.appcomplete;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditActivity extends AppCompatActivity {

    private static final int GALLERY_REQ= 2000;
    private static final int CAMERA_REQ = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 1000;

    private ImageView im_edit;
    private EditText name_edit;
    private EditText phone_edit;
    private EditText address_edit;
    private EditText email_edit;
    private EditText description_edit;
    private Button b;
    private ImageButton ib;
    private byte[] photoByteArray;
    private SharedPreferences sharedpref;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case  MY_CAMERA_PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                    chooseFromCamera();
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }
                break;

            case GALLERY_REQ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "gallery permission granted", Toast.LENGTH_LONG).show();
                    chooseFromGallery();
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the UP button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name_edit = findViewById(R.id.editTextName);
        phone_edit = findViewById(R.id.editTextTelephone);
        address_edit = findViewById(R.id.editTextAddress);
        email_edit = findViewById(R.id.editTextEmail);
        description_edit = findViewById((R.id.editTextDescription));

        sharedpref = getSharedPreferences("userinfo", Context.MODE_PRIVATE);

        im_edit = findViewById(R.id.imageView1);
        ib = findViewById(R.id.imageButton);

        ib.setOnClickListener(
                new ImageButton.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        showPictureDialog();
                    }
                }
        );


        b = findViewById(R.id.button);

        //When I click on the Save button
        b.setOnClickListener(
                new Button.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        saveInfo(v);
                    }
                }
        );

        displayData();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if(item.getItemId() == android.R.id.home){
            if(sharedpref.getBoolean("saved", false) == false){
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
        outState.putString("phone", phone_edit.getText().toString());
        outState.putString("address", address_edit.getText().toString());
        outState.putString("email", email_edit.getText().toString());
        outState.putString("description", description_edit.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        photoByteArray = savedInstanceState.getByteArray("profilePicture");

        if(photoByteArray != null){
            im_edit.setImageBitmap(BitmapFactory.decodeByteArray(photoByteArray,
                    0, photoByteArray.length));
        }

        name_edit.setText(savedInstanceState.getString("name"));
        phone_edit.setText(savedInstanceState.getString("phone"));
        address_edit.setText(savedInstanceState.getString("address"));
        email_edit.setText(savedInstanceState.getString("email"));
        description_edit.setText(savedInstanceState.getString("description"));

    }

    public void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action:");
        pictureDialog.setCancelable(true);

        String[] picDialogItems = {"Gallery", "Camera"};

        pictureDialog.setItems(picDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                chooseFromGalleryPermission();
                                break;

                            case 1:
                                chooseFromCameraPermission();
                                break;
                        }
                    }
                });

        pictureDialog.show();
    }

    public void chooseFromGalleryPermission(){
        int hasPermissionGallery = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if(hasPermissionGallery == PackageManager.PERMISSION_GRANTED){
            chooseFromGallery();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    GALLERY_REQ);
        }
    }

    public void chooseFromCameraPermission(){
        int hasPermissionCamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if(hasPermissionCamera == PackageManager.PERMISSION_GRANTED){
            chooseFromCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_PERMISSION_CODE);
        }
    }

    public void chooseFromGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQ);
    }

    public void  chooseFromCamera(){
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photo;

        if(resultCode == this.RESULT_CANCELED){
            return;
        }

        if(requestCode == GALLERY_REQ && resultCode == this.RESULT_OK){
            if(data != null){
                try{
                    final Uri contentURI = data.getData();
                    final InputStream stream = getContentResolver().openInputStream(contentURI);
                    photo = BitmapFactory.decodeStream(stream);

                    //photo = rotateImageIfRequired(photo, contentURI);
                    //photo = getResizedBitmap(photo, 500);

                    photoByteArray = bitmapToByteArray(photo);
                    im_edit.setImageBitmap(photo);

                    Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        } else if( requestCode == CAMERA_REQ && resultCode == this.RESULT_OK){
            photo = (Bitmap) data.getExtras().get("data");
            im_edit.setImageBitmap(photo);


            photoByteArray = bitmapToByteArray(photo);
            Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
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
        SharedPreferences.Editor editor = sharedpref.edit();

        if(TextUtils.isEmpty(name_edit.getText().toString()) || TextUtils.isEmpty(phone_edit.getText().toString()) ||
                TextUtils.isEmpty(address_edit.getText().toString()) || TextUtils.isEmpty(email_edit.getText().toString()) ||
                TextUtils.isEmpty(description_edit.getText().toString())){

            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

            pictureDialog.setTitle("Warning");
            pictureDialog.setMessage("All the fields must be filled.");
            pictureDialog.setPositiveButton(android.R.string.ok, null);
            pictureDialog.show();
        }else{
            im_edit.buildDrawingCache();

            Bitmap picture = im_edit.getDrawingCache();

            String imageEncoded = Base64.encodeToString(bitmapToByteArray(picture), Base64.DEFAULT);

            editor.putString("imageEncoded", imageEncoded);

            //TODO
            //Use FIREBASE instead of SharedPreferences

            //Store the couple <key, value> into the SharedPreferences
            editor.putString("name", name_edit.getText().toString());
            editor.putString("phone", phone_edit.getText().toString());
            editor.putString("address", address_edit.getText().toString());
            editor.putString("email", email_edit.getText().toString());
            editor.putString("description", description_edit.getText().toString());
            editor.putBoolean("saved", true);

            if(sharedpref.getBoolean("firstTime", true) == true){
                editor.putBoolean("firstTime", false);
            }
            editor.apply();

            Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if(sharedpref.getBoolean("saved", false) == false){
            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

            pictureDialog.setTitle("Exit:");
            pictureDialog.setMessage("The changes have not been saved. Are you sure to exit?");
            pictureDialog.setNegativeButton(android.R.string.no, null);
            pictureDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which){
                    EditActivity.super.onBackPressed();
                }
            });

            pictureDialog.show();
        }
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
                im_edit.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes,
                        0, imageAsBytes.length));
            }
            name_edit.setText(nameEdit);
            phone_edit.setText(phoneEdit);
            address_edit.setText(addressEdit);
            email_edit.setText(emailEdit);
            description_edit.setText(descriptionEdit);
        }
    }

    private static byte [] bitmapToByteArray(Bitmap photo) {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 80, streambyte);
        return streambyte.toByteArray();
    }
}