package it.polito.mad.appcomplete;

import android.graphics.Bitmap;

public class FoodInfo {
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
}