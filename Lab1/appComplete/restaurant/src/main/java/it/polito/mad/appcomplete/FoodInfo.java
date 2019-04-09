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

    protected FoodInfo(Parcel in) {
        image = in.readParcelable(Bitmap.class.getClassLoader());
        Name = in.readString();
        price = in.readInt();
        quantity = in.readInt();
        description = in.readString();
    }

    public String getName() {
        return Name;
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
        dest.writeParcelable(image, flags);
        dest.writeString(Name);
        dest.writeInt(price);
        dest.writeInt(quantity);
        dest.writeString(description);
    }
}