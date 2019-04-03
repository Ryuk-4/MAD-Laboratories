package it.polito.mad.appcomplete;

public class ReservationInfo {
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

}

