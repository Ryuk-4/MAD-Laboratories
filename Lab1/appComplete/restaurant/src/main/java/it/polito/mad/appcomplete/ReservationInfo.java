package it.polito.mad.appcomplete;

import java.util.Comparator;

public class ReservationInfo {
    private String orderID;
    private String namePerson;
    private String idPerson;
    private String personAddress;
    private String timeReservation;
    private String personOrder;
    private String restaurantId;
    private String restaurantAddress;
    private String note;

    public ReservationInfo(String orderID, String idPerson, String namePerson, String timeReservation,
                           String personOrder, String personAddress) {
        this.orderID = orderID;
        this.idPerson = idPerson;
        this.namePerson = namePerson;
        this.timeReservation = timeReservation;
        this.personAddress = personAddress;
        this.personOrder = personOrder;
    }

    public ReservationInfo(String namePerson, String personAddress, String restaurantId,String restaurantAddress){
        this.namePerson = namePerson;
        this.personAddress = personAddress;
        this.restaurantId = restaurantId;
        this.restaurantAddress = restaurantAddress;
    }

    public ReservationInfo() {
        this.idPerson = "";
        this.orderID = "";
        this.namePerson = "";
        this.timeReservation = "";
        this.personOrder = "";
        this.personAddress = "";
        this.note = "";
    }

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

    public String getPersonAddress() {
        return personAddress;
    }

    public void setPersonAddress(String personAddress) {
        this.personAddress = personAddress;
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

}

