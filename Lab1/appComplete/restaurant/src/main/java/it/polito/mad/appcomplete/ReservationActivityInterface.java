package it.polito.mad.appcomplete;

public interface ReservationActivityInterface {

    // It's used to move one reservation from a fragment to another
    void processReservation(String fragmentTag, ReservationInfo reservation);

    // It's used to undo the move from a fragment to another
    void undoOperation(String fragmentTag);
}
