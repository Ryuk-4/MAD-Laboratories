package it.polito.mad.customer;


import android.os.Parcel;
import android.os.Parcelable;

public class OrderRecap implements Parcelable {
    private String price;
    private String quantity;
    private String name;
    private String key;

    public OrderRecap(String price, String quantity, String name, String key) {
        this.price = price;
        this.quantity = quantity;
        this.name = name;
        this.key = key;
    }

    public OrderRecap(Parcel in)
    {
        this.price = in.readString();
        this.quantity = in.readString();
        this.name = in.readString();
        this.key = in.readString();
    }

    public static final Creator<OrderRecap> CREATOR = new Creator<OrderRecap>() {
        @Override
        public OrderRecap createFromParcel(Parcel in) {
            return new OrderRecap(in);
        }

        @Override
        public OrderRecap[] newArray(int size) {
            return new OrderRecap[size];
        }
    };

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "OrderRecap{" +
                "price='" + price + '\'' +
                ", quantity='" + quantity + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(price);
        dest.writeString(quantity);
        dest.writeString(name);
        dest.writeString(key);
    }
}
