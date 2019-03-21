package com.example.lab1_customers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 1000;
    private static final int PICK_FROM_GALLERY = 1;
    private Bitmap b;

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode)
        {
            case MY_CAMERA_PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                    useCamera();
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }
                break;

            case PICK_FROM_GALLERY:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "gallery permission granted", Toast.LENGTH_LONG).show();
                    accessGallery();
                } else {
                    Toast.makeText(this, "gallery permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                handleCameraPermission();
            }
        });

        Button galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                handleGalleryPermission();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ((EditText)(findViewById(R.id.textfieldName))).setText(savedInstanceState.getString("name"));
        ((EditText)(findViewById(R.id.textfieldAddress))).setText(savedInstanceState.getString("address"));
        ((EditText)(findViewById(R.id.textfieldEmail))).setText(savedInstanceState.getString("email"));
        ((EditText)(findViewById(R.id.textfieldDescription))).setText(savedInstanceState.getString("description"));

        //((ImageView)findViewById(R.id.profilePicture)).setImageBitmap(BitmapFactory.decodeByteArray(savedInstanceState.getByteArray("image"), 0, savedInstanceState.getByteArray("image").length));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("name", ((EditText)findViewById(R.id.textfieldName)).getText().toString());
        outState.putString("address", ((EditText)findViewById(R.id.textfieldAddress)).getText().toString());
        outState.putString("email", ((EditText)findViewById(R.id.textfieldEmail)).getText().toString());
        outState.putString("description", ((EditText)findViewById(R.id.textfieldDescription)).getText().toString());

        //ImageView profilePicture = (ImageView) findViewById(R.id.profilePicture);
        //b = ((BitmapDrawable)profilePicture.getDrawable()).getBitmap();
        //outState.putSerializable("bitmap", b);


        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //byte[] byteArray = stream.toByteArray();

        //outState.putByteArray("image", byteArray);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    b = (Bitmap) extras.get("data");
                    ((ImageView) findViewById(R.id.profilePicture)).setImageBitmap(b);
                }
                break;
            case PICK_FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    // Get the path from the Uri
                    final String path = getPathFromURI(selectedImageUri);
                    if (path != null) {
                        File f = new File(path);
                        selectedImageUri = Uri.fromFile(f);
                    }
                    // Set the image in ImageView
                    ((ImageView) findViewById(R.id.profilePicture)).setImageURI(selectedImageUri);
                }
                break;
        }
    }

    private void handleCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            useCamera();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }
    }

    private void handleGalleryPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            accessGallery();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
        }
    }

    private void useCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void accessGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
}
