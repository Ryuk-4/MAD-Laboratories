package it.polito.mad.appcomplete;

import android.Manifest;
import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaeger.library.StatusBarUtil;
import com.squareup.picasso.Picasso;

import java.io.InputStream;

import static it.polito.mad.data_layer_access.FirebaseUtils.*;
import static it.polito.mad.data_layer_access.ImageUtils.*;
import static it.polito.mad.data_layer_access.Costants.CAMERA_REQ;
import static it.polito.mad.data_layer_access.Costants.GALLERY_REQ;
import static it.polito.mad.data_layer_access.Costants.MY_CAMERA_PERMISSION_CODE;

public class DailyActivityEdit extends AppCompatActivity {

    private static final String TAG = "DailyActivityEdit";

    private ImageView im_edit;
    private EditText name_edit;
    private EditText editTextPrice;
    private EditText editAvailableQuantity;
    private EditText EditDescription;
    private Button b;
    private ImageButton ib;
    private byte[] photoByteArray;
    private SharedPreferences sharedpref, foodFavorite, preferences;
    private CheckBox favoriteFood;
    private boolean favorite, editFood;
    private int i = -1;

    private FoodInfo foodInfo;

    private Bitmap photo;

    private String id;
//    private DatabaseReference database;
//    private DatabaseReference branchDailyFood;
    private DatabaseReference targetFavouriteFood;
    private DatabaseReference targetDailyFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_edit);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Show the UP button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        StatusBarUtil.setTransparent(this);

        name_edit = findViewById(R.id.editFoodname);
        editTextPrice = findViewById(R.id.editTextPrice);
        editAvailableQuantity = findViewById(R.id.editAvailableQuantity);
        EditDescription = findViewById(R.id.EditDescription);
        favoriteFood = findViewById(R.id.checkFavoriteFood);
        im_edit = findViewById(R.id.foodImage);

        sharedpref = getSharedPreferences("foodinfo", Context.MODE_PRIVATE);
        foodFavorite = getSharedPreferences("foodFav", Context.MODE_PRIVATE);
//        database = FirebaseDatabase.getInstance().getReference();
//        preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);

        favorite = true;

        SharedPreferences.Editor e = sharedpref.edit();
        e.putBoolean("saved", false);
        e.apply();

        setupFirebase();

        initLayout();

        ib = findViewById(R.id.buttonImageFood);

        ib.setOnClickListener(
                new ImageButton.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        showPictureDialog(DailyActivityEdit.this);
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
    }

    private void initLayout() {
//        String Uid = preferences.getString("Uid", "");
//        branchDailyFood = database.child("restaurants/" + Uid + "/Daily_Food/");
//        branchFavouriteFood = database.child("restaurants/" + Uid + "/Favourites_Food/");


        if (getIntent().hasExtra("food_selected")) {
            if (getIntent().getStringExtra("food_selected").compareTo("normal") == 0)
            {
                initLayoutModifyFood(sharedpref);
            } else if(getIntent().getStringExtra("food_selected").compareTo("favourite") == 0)
            {
                initLayoutFavoriteFood(foodFavorite);
            }
            favoriteFood.setVisibility(View.GONE);
        }
    }

    private void initLayoutFavoriteFood(SharedPreferences foodFavorite) {

        if ( (id = getIntent().getStringExtra("food_position")) != null) {
            Log.d(TAG, "initLayoutFavoriteFood: id " + id);
//            String Uid = preferences.getString("Uid", "");
//            branchFavouriteFood = database.child("restaurants/" + Uid + "/Favourites_Food/" + id);

            targetFavouriteFood = branchFavouriteFood.child(id);

            targetFavouriteFood.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    name_edit.setText(dataSnapshot.child("name").getValue().toString());
                    editTextPrice.setText(dataSnapshot.child("price").getValue().toString());
                    editAvailableQuantity.setText(dataSnapshot.child("quantity").getValue().toString());
                    EditDescription.setText(dataSnapshot.child("description").getValue().toString());

                    if ((dataSnapshot.child("image").getValue() != null)) {
                        Picasso.get().load(dataSnapshot.child("image").getValue().toString())
                                .fit().centerCrop().into(im_edit);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
                }
            });

            favoriteFood.setVisibility(View.GONE);
            favorite = false;
            editFood = false;
        }
    }

    private void initLayoutModifyFood(SharedPreferences sharedpref) {
        if ( (id = getIntent().getStringExtra("food_position")) != null) {
//            String Uid = preferences.getString("Uid", "");
//            branchDailyFood = database.child("restaurants/" + Uid + "/Daily_Food/" + id);
            targetDailyFood = branchDailyFood.child(id);
            targetDailyFood.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    name_edit.setText(dataSnapshot.child("name").getValue().toString());
                    editTextPrice.setText(dataSnapshot.child("price").getValue().toString());
                    editAvailableQuantity.setText(dataSnapshot.child("quantity").getValue().toString());
                    EditDescription.setText(dataSnapshot.child("description").getValue().toString());

                    if ((dataSnapshot.child("image").getValue() != null)) {
                        Picasso.get().load(dataSnapshot.child("image").getValue().toString())
                                .fit().centerCrop().into(im_edit);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
                }
            });
        }

        favorite = false;
        editFood = true;
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
        outState.putBoolean("favorite", favorite);
        outState.putBoolean("editFood", editFood);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        favorite = savedInstanceState.getBoolean("favorite");
        editFood = savedInstanceState.getBoolean("editFood");

        photoByteArray = savedInstanceState.getByteArray("profilePicture");

        if(photoByteArray != null){
            im_edit.setImageBitmap(BitmapFactory.decodeByteArray(photoByteArray,
                    0, photoByteArray.length));
        }

        name_edit.setText(savedInstanceState.getString("name"));
        editTextPrice.setText(savedInstanceState.getString("surname"));
        editAvailableQuantity.setText(savedInstanceState.getString("phone"));
        EditDescription.setText(savedInstanceState.getString("address"));

        if (favorite || editFood)
            favoriteFood.setVisibility(View.GONE);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        switch (requestCode){

            case  MY_CAMERA_PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();

                    startActivityForResult(chooseFromCamera(), CAMERA_REQ);
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }
                break;

            case GALLERY_REQ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "gallery permission granted", Toast.LENGTH_LONG).show();
                    startActivityForResult(chooseFromGallery(), GALLERY_REQ);
                } else {
                    Toast.makeText(this, "gallery permission denied", Toast.LENGTH_LONG).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: called");
        if(resultCode == this.RESULT_CANCELED){
            return;
        }

        if(requestCode == GALLERY_REQ && resultCode == this.RESULT_OK){
            if(data != null){
                try{
                    final Uri contentURI = data.getData();
                    final InputStream stream = getContentResolver().openInputStream(contentURI);
                    photo = BitmapFactory.decodeStream(stream);

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


    public void saveInfo(View v){
        SharedPreferences.Editor editor = sharedpref.edit();

        if (im_edit.getDrawable() == null || TextUtils.isEmpty(name_edit.getText().toString()) || TextUtils.isEmpty(editTextPrice.getText().toString()) ||
                TextUtils.isEmpty(editAvailableQuantity.getText().toString()) || TextUtils.isEmpty(EditDescription.getText().toString())){

            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

            pictureDialog.setTitle("Warning");
            pictureDialog.setMessage("All the fields must be filled.");
            pictureDialog.setPositiveButton(android.R.string.ok, null);
            pictureDialog.show();
        }else{
            b.setEnabled(false);

            if (favorite)
            {
                saveNewFood();
            } else
            {
                Log.d(TAG, "saveInfo: update");
                im_edit.setDrawingCacheEnabled(true);
                im_edit.buildDrawingCache();
                Bitmap picture = ((BitmapDrawable) im_edit.getDrawable()).getBitmap();

                FoodInfo newFood = new FoodInfo();

                newFood.setName(name_edit.getText().toString());
                newFood.setPrice(editTextPrice.getText().toString());
                newFood.setQuantity(editAvailableQuantity.getText().toString());
                newFood.setDescription(EditDescription.getText().toString());
                newFood.setFoodId(id);



                final StorageReference ref = FirebaseStorage.getInstance().getReference()
                        .child("restaurants/food_images/food" + id);
                final UploadTask uploadTask = (UploadTask) ref.putBytes(bitmapToByteArray(picture))
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG, "onSuccess: called");
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Uri downloadUrl = uri;

                                        newFood.setImage(downloadUrl.toString());
                                        branchDailyFood.setValue(newFood);
                                    }
                                });
                            }
                        });


                editor.putBoolean("saved", true);

                editor.apply();

                Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();

            }
            b.setEnabled(true);
            finish();
        }
    }

    private void saveNewFood() {
        im_edit.setDrawingCacheEnabled(true);
        im_edit.buildDrawingCache();
        Bitmap picture = ((BitmapDrawable) im_edit.getDrawable()).getBitmap();

        FoodInfo newFood = new FoodInfo();

        newFood.setName(name_edit.getText().toString());
        newFood.setPrice(editTextPrice.getText().toString());
        newFood.setQuantity(editAvailableQuantity.getText().toString());
        newFood.setDescription(EditDescription.getText().toString());

        final String foodId = branchDailyFood.push().getKey();
        newFood.setFoodId(foodId);

        branchDailyFood.child(foodId).setValue(newFood);

        if (favoriteFood.isChecked()) {
            branchFavouriteFood.child(foodId).setValue(newFood);
        }

        final StorageReference ref1 = FirebaseStorage.getInstance().getReference()
                .child("restaurants/food_images/food" + foodId);
        final UploadTask uploadTask = (UploadTask) ref1.putBytes(bitmapToByteArray(picture))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: called");
                        ref1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUrl = uri;

                                branchDailyFood.child(foodId + "/image").setValue(downloadUrl.toString());
                                if (favoriteFood.isChecked()) {
                                    branchFavouriteFood.child(foodId + "/image").setValue(downloadUrl.toString());
                                }
                            }
                        });
                    }
                });

        Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
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

}
