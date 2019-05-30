package it.polito.mad.appcomplete;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ReservationInfo {
    private String orderID;
    private String namePerson;
    private String idPerson;
    private String cLatitude;
    private String cLongitude;
    private String timeReservation;
    private Map<String, FoodInfo> OrderList = new HashMap<>();
    private String restaurantId;
    private String rLatitude;
    private String rLongitude;
    private String status_order;
    private String note;
    private String date;

    public ReservationInfo(String namePerson, String cLatitude, String cLongitude, String restaurantId,
                           String rLatitude, String rLongitude) {
        this.namePerson = namePerson;
        this.cLatitude = cLatitude;
        this.cLongitude = cLongitude;
        this.restaurantId = restaurantId;
        this.rLatitude = rLatitude;
        this.rLongitude = rLongitude;
    }

    public ReservationInfo() {

    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void setNamePerson(String namePerson) {
        this.namePerson = namePerson;
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

    public void setEmail(String email) {
        this.note = email;
    }

    public String getNamePerson() {
        return namePerson;
    }

    public String getTimeReservation() {
        return timeReservation;
    }

    public String getcLatitude() {
        return cLatitude;
    }

    public void setcLatitude(String cLatitude) {
        this.cLatitude = cLatitude;
    }

    public String getcLongitude() {
        return cLongitude;
    }

    public void setcLongitude(String cLongitude) {
        this.cLongitude = cLongitude;
    }

    public String getrLatitude() {
        return rLatitude;
    }

    public void setrLatitude(String rLatitude) {
        this.rLatitude = rLatitude;
    }

    public String getrLongitude() {
        return rLongitude;
    }

    public void setrLongitude(String rLongitude) {
        this.rLongitude = rLongitude;
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

    public String getStatus_order() {
        return status_order;
    }

    public void setStatus_order(String status_order) {
        this.status_order = status_order;
    }

    public Map<String, FoodInfo> getOrderList() {
        return OrderList;
    }

    public void setOrderList(Map<String, FoodInfo> orders) {
        this.OrderList = orders;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public static final Comparator<ReservationInfo> BY_TIME_ASCENDING = new Comparator<ReservationInfo>() {
        @Override
        public int compare(ReservationInfo o1, ReservationInfo o2) {
            return o1.getTimeReservation().compareTo(o2.getTimeReservation());
        }
    };

}

