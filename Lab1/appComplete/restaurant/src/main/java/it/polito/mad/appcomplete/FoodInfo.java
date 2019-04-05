package it.polito.mad.appcomplete;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class FoodInfo implements Parcelable {
    public Bitmap image;
    public String Name;
    public int price;
    public  int quantity;
    public  String description;

    public FoodInfo(Bitmap image, String name, int price, int quantity, String description) {
        this.image = image;
        Name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}