package it.polito.mad.customer;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.io.IOException;
import java.io.InputStream;

public class EditActivity extends AppCompatActivity{

    private static final int GALLERY_REQ= 2000;
    private static final int CAMERA_REQ = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 1000;

    private ImageView im_edit;
    private EditText name_edit;
    private EditText surname_edit;
    private EditText phone_edit;
    private EditText address_edit;
    private TextView dateOfBirth;
    private RadioGroup radioSex;
    private Button b;
    private FloatingActionButton ib;
    private byte[] photoByteArray;
    private SharedPreferences sharedpref;
    private ImageButton calendarButton;

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
        dateOfBirth = findViewById(R.id.dateOfBirthString);
        surname_edit = findViewById(R.id.editTextSurname);
        radioSex = findViewById(R.id.radioSex);


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

        calendarButton = findViewById(R.id.iconOpenCalendar);

        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog;
                datePickerDialog = new DatePickerDialog(EditActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateOfBirth.setText(dayOfMonth + "/" + month + "/" + year);
                    }
                }, 1970, 0, 1 );
                datePickerDialog.show();
            }
        });

        displayData();

        StatusBarUtil.setTransparent(this);

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

        String s = savedInstanceState.getString("sex");

        if (s.compareTo(getString(R.string.radioMale)) == 0)
        {
            radioSex.check(R.id.radioMale);
        } else
        {
            radioSex.check(R.id.radioFemale);
        }

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

        if(TextUtils.isEmpty(dateOfBirth.getText().toString()) || TextUtils.isEmpty(surname_edit.getText().toString()) || TextUtils.isEmpty(name_edit.getText().toString()) || TextUtils.isEmpty(phone_edit.getText().toString()) ||
                TextUtils.isEmpty(address_edit.getText().toString())){

            AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);

            pictureDialog.setTitle("Warning");
            pictureDialog.setMessage("All the fields must be filled.");
            pictureDialog.setPositiveButton(android.R.string.ok, null);
            pictureDialog.show();
        } else{
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customers").child(user.getUid()).child("Profile");
            int sexId = radioSex.getCheckedRadioButtonId();
            View radioButton = radioSex.findViewById(sexId);
            int idx = radioSex.indexOfChild(radioButton);

            RadioButton r = (RadioButton) radioSex.getChildAt(idx);

            if (sharedpref.getString("name", "").compareTo(name_edit.getText().toString()) != 0)
            {
                editor.putString("name", name_edit.getText().toString());
                databaseReference.child("name").setValue(name_edit.getText().toString());
            }
            if (sharedpref.getString("surname", "").compareTo(surname_edit.getText().toString()) != 0)
            {
                editor.putString("surname", surname_edit.getText().toString());
                databaseReference.child("surname").setValue(surname_edit.getText().toString());
            }
            if (sharedpref.getString("phone", "").compareTo(phone_edit.getText().toString()) != 0)
            {
                editor.putString("phone", phone_edit.getText().toString());
                databaseReference.child("phone").setValue(phone_edit.getText().toString());
            }
            if (sharedpref.getString("address", "").compareTo(address_edit.getText().toString()) != 0)
            {
                editor.putString("address", address_edit.getText().toString());
                databaseReference.child("address").setValue(address_edit.getText().toString());
            }
            if (sharedpref.getString("dateOfBirth", "").compareTo(dateOfBirth.getText().toString()) != 0)
            {
                editor.putString("dateOfBirth", dateOfBirth.getText().toString());
                databaseReference.child("dateOfBirth").setValue(dateOfBirth.getText().toString());
            }
            if (sharedpref.getString("sex", "").compareTo(r.getText().toString()) != 0)
            {
                editor.putString("sex", r.getText().toString());
                databaseReference.child("sex").setValue(r.getText().toString());
            }

            im_edit.setDrawingCacheEnabled(true);
            im_edit.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) im_edit.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            final StorageReference ref = FirebaseStorage.getInstance().getReference().child("profile_images/profile"+user.getUid());
            final UploadTask uploadTask = (UploadTask) ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;

                            databaseReference.child("photo").setValue(downloadUrl.toString());

                            SharedPreferences.Editor editor = sharedpref.edit();
                            editor.putString("imageEncoded", downloadUrl.toString());
                            editor.apply();
                        }


                    });
                }
            });
                    /*
            final StorageReference ref = FirebaseStorage.getInstance().getReference().child("profile"+user.getUid()).child("images/mountains.jpg");
            final UploadTask uploadTask = ref.putBytes(data);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    databaseReference.child("photo").setValue(ref.getDownloadUrl().toString());

                    SharedPreferences.Editor editor = sharedpref.edit();
                    editor.putString("imageEncoded", ref.getDownloadUrl().toString());
                    editor.apply();

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
            */

            editor.putBoolean("saved", true);

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

        String nameEdit = sharedpref.getString("name", "");
        String phoneEdit = sharedpref.getString("phone", "");
        String addressEdit = sharedpref.getString("address", "");
        String surnameEdit = sharedpref.getString("surname", "");
        String dateEdit = sharedpref.getString("dateOfBirth", "");
        String sexEdit = sharedpref.getString("sex", "");

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

    private static byte [] bitmapToByteArray(Bitmap photo) {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 80, streambyte);
        return streambyte.toByteArray();
    }
}