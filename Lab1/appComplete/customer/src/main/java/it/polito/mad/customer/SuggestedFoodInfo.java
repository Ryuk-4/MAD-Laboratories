package it.polito.mad.customer;

import android.os.Parcel;
import android.os.Parcelable;

public class SuggestedFoodInfo implements Parcelable {

    private String name;
    private String description;
    private String imageUrl;
    private String price;
    private String key;

    public SuggestedFoodInfo(Parcel in)
    {
        this.name = in.readString();
        this.description = in.readString();
        this.imageUrl = in.readString();
        this.price = in.readString();
        this.key = in.readString();
    }

    public SuggestedFoodInfo(String name, String description, String imageUrl, String price, String key) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.key = key;
    }

    public SuggestedFoodInfo() {
    }

    public static final Creator<SuggestedFoodInfo> CREATOR = new Creator<SuggestedFoodInfo>() {
        @Override
        public SuggestedFoodInfo createFromParcel(Parcel in) {
            return new SuggestedFoodInfo(in);
        }

        @Override
        public SuggestedFoodInfo[] newArray(int size) {
            return new SuggestedFoodInfo[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeString(price);
        dest.writeString(key);
    }
}
