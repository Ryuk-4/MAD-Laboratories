package it.polito.mad.appcomplete;

public interface ReservationActivityInterface {
    void processReservation(String fragmentTag, ReservationInfo reservation);
    void undoOperation(String fragmentTag);
}
