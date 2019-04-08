package it.polito.mad.appcomplete;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DailyActivityEdit extends AppCompatActivity {
    private static final int GALLERY_REQ= 2000;
    private static final int CAMERA_REQ = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 1000;


    private ImageView im_edit;
    private EditText name_edit;
    private EditText editTextPrice;
    private EditText editAvailableQuantity;
    private EditText EditDescription;
    private Button b;
    private ImageButton ib;
    private byte[] photoByteArray;
    private SharedPreferences sharedpref, foodFavorite;
    private CheckBox favoriteFood;
    //private String day;
    //private ArrayAdapter<CharSequence> adapter1;
    //Spinner spinner;

    private FoodInfo foodInfo;

    Bitmap photo;


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
        setContentView(R.layout.activity_daily_edit);

        //day = getIntent().getStringExtra("day");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the UP button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name_edit = findViewById(R.id.editFoodname);
        editTextPrice = findViewById(R.id.editTextPrice);
        editAvailableQuantity = findViewById(R.id.editAvailableQuantity);
        EditDescription = findViewById(R.id.EditDescription);
        favoriteFood = findViewById(R.id.checkFavoriteFood);
        im_edit = findViewById(R.id.foodImage);

        sharedpref = getSharedPreferences("foodinfo", Context.MODE_PRIVATE);
        foodFavorite = getSharedPreferences("foodFav", Context.MODE_PRIVATE);

        if (getIntent().hasExtra("food_selected")) {
            foodInfo = getIntent().getParcelableExtra("food_selected");

            name_edit.setText(foodInfo.Name);
            editTextPrice.setText(Integer.toString(foodInfo.price));
            editAvailableQuantity.setText(Integer.toString(foodInfo.quantity));
            EditDescription.setText(foodInfo.description);
            im_edit.setImageBitmap(foodInfo.image);
        }
        //im_edit = findViewById(R.id.foodImage);
        ib = findViewById(R.id.buttonImageFood);

        ib.setOnClickListener(
                new ImageButton.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        showPictureDialog();
                    }
                }
        );


        b = findViewById(R.id.buttonSaveFood);

        //When I click on the Save button
        b.setOnClickListener(
                new Button.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        saveInfo(v);
                    }
                }
        );

        /*
        spinner = (Spinner) findViewById(R.id.dayForFood);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter1 = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter1);
*/
       // displayData();
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
        outState.putString("surname", editTextPrice.getText().toString());
        outState.putString("phone", editAvailableQuantity.getText().toString());
        outState.putString("address", EditDescription.getText().toString());
        //outState.putString("day", spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString());

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
        editTextPrice.setText(savedInstanceState.getString("surname"));
        editAvailableQuantity.setText(savedInstanceState.getString("phone"));
        EditDescription.setText(savedInstanceState.getString("address"));
        //day = savedInstanceState.getString("day");

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


        if(resultCode == this.RESULT_CANCELED){
            return;
        }

        if(requestCode == GALLERY_REQ && resultCode == this.RESULT_OK){
            if(data != null){
                try{
                    final Uri contentURI = data.getData();
                    final InputStream stream = getContentResolver().openInputStream(contentURI);
                    photo = BitmapFactory.decodeStream(stream);

                    photo = rotateImageIfRequired(photo, contentURI);
                    photo = getResizedBitmap(photo, 500);

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
        SharedPreferences.Editor editorFavorite = foodFavorite.edit();

        if (photo == null || TextUtils.isEmpty(name_edit.getText().toString()) || TextUtils.isEmpty(editTextPrice.getText().toString()) ||
                TextUtils.isEmpty(editAvailableQuantity.getText().toString()) || TextUtils.isEmpty(EditDescription.getText().toString())){

            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

            pictureDialog.setTitle("Warning");
            pictureDialog.setMessage("All the fields must be filled.");
            pictureDialog.setPositiveButton(android.R.string.ok, null);
            pictureDialog.show();
        }else{
            String imageEncoded = Base64.encodeToString(bitmapToByteArray(photo), Base64.DEFAULT);
            int numberOfFood = sharedpref.getInt("numberOfFood", 0);
            int numberOfFoodFavorite = foodFavorite.getInt("numberOfFood", 0);

            if (favoriteFood.isChecked())
            {
                editorFavorite.putString("foodName"+numberOfFoodFavorite, name_edit.getText().toString());
                editorFavorite.putString("foodPrice"+numberOfFoodFavorite, editTextPrice.getText().toString());
                editorFavorite.putString("foodQuantity"+numberOfFoodFavorite, editAvailableQuantity.getText().toString());
                editorFavorite.putString("foodDescription"+numberOfFoodFavorite, EditDescription.getText().toString());
                editorFavorite.putString("foodImage"+numberOfFoodFavorite, imageEncoded);
                //editor.putString("day"+numberOfFood, spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString());

                numberOfFoodFavorite++;
                editorFavorite.putInt("numberOfFood", numberOfFoodFavorite);

                editorFavorite.apply();
            }

            //TODO
            //Use FIREBASE instead of SharedPreferences

            //Store the couple <key, value> into the SharedPreferences
            editor.putString("foodName"+numberOfFood, name_edit.getText().toString());
            editor.putString("foodPrice"+numberOfFood, editTextPrice.getText().toString());
            editor.putString("foodQuantity"+numberOfFood, editAvailableQuantity.getText().toString());
            editor.putString("foodDescription"+numberOfFood, EditDescription.getText().toString());
            editor.putString("foodImage"+numberOfFood, imageEncoded);
            //editor.putString("day"+numberOfFood, spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString());
            editor.putBoolean("saved", true);

            numberOfFood++;
            editor.putInt("numberOfFood", numberOfFood);

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
                    DailyActivityEdit.super.onBackPressed();
                }
            });

            pictureDialog.show();
        }
    }

    public void displayData() {
        String imageDecoded = sharedpref.getString("imageEncoded", "");
        byte[] imageAsBytes = Base64.decode(imageDecoded, Base64.DEFAULT);

        String nameEdit = sharedpref.getString("name", "");
        String phoneEdit = sharedpref.getString("price", "");
        String addressEdit = sharedpref.getString("quantity", "");
        String emailEdit = sharedpref.getString("description", "");

        if(imageAsBytes != null) {
            im_edit.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes,
                    0, imageAsBytes.length));
        }
        name_edit.setText(nameEdit);
        editTextPrice.setText(phoneEdit);
        editAvailableQuantity.setText(addressEdit);
        EditDescription.setText(emailEdit);

    }

    private static byte [] bitmapToByteArray(Bitmap photo) {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 80, streambyte);
        return streambyte.toByteArray();
    }
}
