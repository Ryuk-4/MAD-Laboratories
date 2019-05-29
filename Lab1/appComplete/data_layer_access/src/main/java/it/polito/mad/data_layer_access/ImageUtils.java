package it.polito.mad.data_layer_access;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import static it.polito.mad.data_layer_access.Costants.CAMERA_REQ;
import static it.polito.mad.data_layer_access.Costants.GALLERY_REQ;
import static it.polito.mad.data_layer_access.Costants.MY_CAMERA_PERMISSION_CODE;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    
    public static void showPictureDialog(final Context context){
        Log.d(TAG, "showPictureDialog: called");
        
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(context);
        pictureDialog.setTitle("Select Action:");
        pictureDialog.setCancelable(true);

        String[] picDialogItems = {"Gallery", "Camera"};

        pictureDialog.setItems(picDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                Log.d(TAG, "onClick: gallery");
                                chooseFromGalleryPermission(context);
                                break;

                            case 1:
                                Log.d(TAG, "onClick: camera");
                                chooseFromCameraPermission(context);
                                break;
                        }
                    }
                });

        pictureDialog.show();
    }

    public static void chooseFromGalleryPermission(Context context){
        Log.d(TAG, "chooseFromGalleryPermission: called");
        int hasPermissionGallery = ContextCompat.checkSelfPermission((Activity) context,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if(hasPermissionGallery == PackageManager.PERMISSION_GRANTED){
            Activity activity = (Activity)context;
            activity.startActivityForResult(chooseFromGallery(), GALLERY_REQ);
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    GALLERY_REQ);
        }
    }

    public static void chooseFromCameraPermission(Context context){
        int hasPermissionCamera = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);

        if(hasPermissionCamera == PackageManager.PERMISSION_GRANTED){
            Activity activity = (Activity)context;
            activity.startActivityForResult(chooseFromCamera(), CAMERA_REQ);
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_PERMISSION_CODE);
        }

    }

    public static Intent chooseFromGallery(){
        Log.d(TAG, "chooseFromGallery: called");
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");

        return galleryIntent;
    }

    public static Intent  chooseFromCamera(){
        Log.d(TAG, "chooseFromCamera: ");
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        return intent;
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
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

    public static byte [] bitmapToByteArray(Bitmap photo) {
        ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 80, streambyte);
        return streambyte.toByteArray();
    }

}
