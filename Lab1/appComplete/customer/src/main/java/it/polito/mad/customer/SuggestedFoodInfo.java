package it.polito.mad.customer;

import android.os.Parcel;
import android.os.Parcelable;

public class SuggestedFoodInfo implements Parcelable {

    private String name;
    private String description;
    private String imageUrl;
    private String price;

    public SuggestedFoodInfo(Parcel in)
    {
        this.name = in.readString();
        this.description = in.readString();
        this.imageUrl = in.readString();
        this.price = in.readString();
    }

    public SuggestedFoodInfo(String name, String description, String imageUrl, String price) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public SuggestedFoodInfo() {
    }

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
    }
}
