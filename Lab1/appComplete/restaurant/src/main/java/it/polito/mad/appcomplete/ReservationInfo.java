package it.polito.mad.appcomplete;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class ReservationInfo implements Parcelable {
    private String namePerson;
    private String timeReservation;
    private String addressPerson;
    private String phonePerson;
    private String email;

    public ReservationInfo(String namePerson, String timeReservation, String addressPerson, String phonePerson, String email) {
        this.namePerson = namePerson;
        this.timeReservation = timeReservation;
        this.addressPerson = addressPerson;
        this.phonePerson = phonePerson;
        this.email = email;
    }


    public ReservationInfo() {
        this.namePerson = " ";
        this.timeReservation = " ";
        this.addressPerson = " ";
        this.phonePerson = " ";
        this.email = " ";
    }

    protected ReservationInfo(Parcel in) {
        namePerson = in.readString();
        timeReservation = in.readString();
        addressPerson = in.readString();
        phonePerson = in.readString();
        email = in.readString();
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

    public void setTimeReservation(String timeReservation) {
        this.timeReservation = timeReservation;
    }

    public void setAddressPerson(String addressPerson) {
        this.addressPerson = addressPerson;
    }

    public void setPhonePerson(String phonePerson) {
        this.phonePerson = phonePerson;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNamePerson() {
        return namePerson;
    }

    public String getTimeReservation() {
        return timeReservation;
    }

    public String getAddressPerson() {
        return addressPerson;
    }

    public String getPhonePerson() {
        return phonePerson;
    }

    public String getEmail() {
        return email;
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
        dest.writeString(namePerson);
        dest.writeString(timeReservation);
        dest.writeString(addressPerson);
        dest.writeString(phonePerson);
        dest.writeString(email);
    }
}

