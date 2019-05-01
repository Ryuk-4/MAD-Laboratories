package it.polito.mad.deliverman;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class ReservationInfo implements Parcelable {
    private String orderID;
    private String namePerson;
    private String idPerson;
    private String timeReservation;
    private String personOrder;
    private String note;

    public ReservationInfo(String orderID, String idPerson, String namePerson, String timeReservation, String personOrder, String note) {
        this.orderID = orderID;
        this.idPerson = idPerson;
        this.namePerson = namePerson;
        this.timeReservation = timeReservation;
        this.personOrder = personOrder;
        this.note = note;
    }

    public ReservationInfo(String orderID, String idPerson, String namePerson, String timeReservation, String personOrder) {
        this.orderID = orderID;
        this.idPerson = idPerson;
        this.namePerson = namePerson;
        this.timeReservation = timeReservation;
        this.personOrder = personOrder;
    }

    public ReservationInfo() {
        this.idPerson = " ";
        this.orderID = " ";
        this.namePerson = " ";
        this.timeReservation = " ";
        this.personOrder = " ";
        this.note = " ";
    }

    protected ReservationInfo(Parcel in) {
        orderID = in.readString();
        idPerson = in.readString();
        namePerson = in.readString();
        timeReservation = in.readString();
        personOrder = in.readString();
        note = in.readString();
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

    public void setPersonOrder(String personOrder) {
        this.personOrder = personOrder;
    }

    public void setNote(String note) {
        this.note = note;
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

    public void setPhonePerson(String phonePerson) {
        this.personOrder = phonePerson;
    }

    public void setEmail(String email) {
        this.note = email;
    }

    public String getNamePerson() {
        return namePerson;
    }

    public String getTimeReservation() {
        return timeReservation;
    }

    public String getPersonOrder() {
        return personOrder;
    }

    public String getNote() {
        return note;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getIdPerson() {
        return idPerson;
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
        dest.writeString(personOrder);
        dest.writeString(note);
    }
}

