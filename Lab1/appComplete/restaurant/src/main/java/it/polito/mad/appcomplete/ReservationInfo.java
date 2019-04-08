package it.polito.mad.appcomplete;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class ReservationInfo implements Parcelable {
    private String namePerson;
    private String timeReservation;
    private String personOrder;
    private String note;

    public ReservationInfo(String namePerson, String timeReservation, String personOrder, String note) {
        this.namePerson = namePerson;
        this.timeReservation = timeReservation;
        this.personOrder = personOrder;
        this.note = note;
    }

    public ReservationInfo(String namePerson, String timeReservation, String personOrder) {
        this.namePerson = namePerson;
        this.timeReservation = timeReservation;
        this.personOrder = personOrder;
    }

    public ReservationInfo() {
        this.namePerson = " ";
        this.timeReservation = " ";
        this.personOrder = " ";
        this.note = " ";
    }

    protected ReservationInfo(Parcel in) {
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
        dest.writeString(personOrder);
        dest.writeString(note);
    }
}

