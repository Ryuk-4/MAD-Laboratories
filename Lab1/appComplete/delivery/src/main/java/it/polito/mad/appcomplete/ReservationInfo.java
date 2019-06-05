package it.polito.mad.appcomplete;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class ReservationInfo implements Parcelable {
    private String orderID;
    private String namePerson;
    private String idPerson;
    private String timeReservation;
    private String restaurantAddress;
    private String addressOrder;
    private String restaurantId;

    private String cLatitude;

    public String getcLatitude() {
        return cLatitude;
    }

    public String getcLongitude() {
        return cLongitude;
    }

    public String getrLongitude() {
        return rLongitude;
    }

    private String cLongitude;
    private String rLatitude;
    private String rLongitude;

    public void setcLatitude(String cLatitude) {
        this.cLatitude = cLatitude;
    }

    public void setcLongitude(String cLongitude) {
        this.cLongitude = cLongitude;
    }

    public void setrLongitude(String rLongitude) {
        this.rLongitude = rLongitude;
    }




    public ReservationInfo() {

    }

    protected ReservationInfo(Parcel in) {
        orderID = in.readString();
        idPerson = in.readString();
        namePerson = in.readString();
        timeReservation = in.readString();
        restaurantAddress = in.readString();
        addressOrder =  in.readString();
        restaurantId =  in.readString();
        cLatitude =  in.readString();
        cLongitude =  in.readString();
        rLatitude =  in.readString();
        rLongitude =  in.readString();
    }

    public static final Creator<ReservationInfo> CREATOR = new Creator<ReservationInfo>() {
        @Override
        public ReservationInfo createFromParcel(Parcel in) {
            return new ReservationInfo(in);
        }

        @Override
        public ReservationInfo[] newArray(int size) {
            return new ReservationInfo[size];
        }
    };

    public void setNamePerson(String namePerson) {
        this.namePerson = namePerson;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public void setIdPerson(String idPerson) {
        this.idPerson = idPerson;
    }

    public void setTimeReservation(String timeReservation) {
        this.timeReservation = timeReservation;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public void setAddressOrder(String addressOrder) {
        this.addressOrder = addressOrder;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }


    public String getNamePerson() {
        return namePerson;
    }

    public String getTimeReservation() {
        return timeReservation;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getIdPerson() {
        return idPerson;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }
    public String getAddressOrder() {
        return addressOrder;
    }
    public String getRestaurantId() {
        return restaurantId;
    }

    public static final Comparator<ReservationInfo> BY_TIME_ASCENDING = new Comparator<ReservationInfo>() {
        @Override
        public int compare(ReservationInfo o1, ReservationInfo o2) {
            return o1.getTimeReservation().compareTo(o2.getTimeReservation());
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderID);
        dest.writeString(idPerson);
        dest.writeString(namePerson);
        dest.writeString(timeReservation);
        dest.writeString(cLatitude);
        dest.writeString(cLongitude);
        dest.writeString(rLongitude);
        dest.writeString(rLatitude);

    }

    public String getrLatitude() {
        return rLatitude;
    }

    public void setrLatitude(String rLatitude) {
        this.rLatitude = rLatitude;
    }
}

