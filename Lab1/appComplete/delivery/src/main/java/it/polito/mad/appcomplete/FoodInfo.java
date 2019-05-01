package it.polito.mad.appcomplete;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class FoodInfo implements Parcelable {
    private String foodId;
    private String image;
    private String Name;
    private String price;
    private  String quantity;
    private  String description;

    public FoodInfo(String foodId, String image, String name, String price, String quantity, String description) {
        this.foodId = foodId;
        this.image = image;
        this.Name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
    }

    public FoodInfo(){
        this.foodId = "";
        this.image = null;
        this.Name = "";
        this.price = "";
        this.quantity = "";
        this.description = "";
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    protected FoodInfo(Parcel in) {
        foodId = in.readString();
        image = in.readString();
        Name = in.readString();
        price = in.readString();
        quantity = in.readString();
        description = in.readString();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public static final Creator<FoodInfo> CREATOR = new Creator<FoodInfo>() {
        @Override
        public FoodInfo createFromParcel(Parcel in) {
            return new FoodInfo(in);
        }

        @Override
        public FoodInfo[] newArray(int size) {
            return new FoodInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(foodId);
        dest.writeString(image);
        dest.writeString(Name);
        dest.writeString(price);
        dest.writeString(quantity);
        dest.writeString(description);
    }
}