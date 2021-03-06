package it.polito.mad.customer;

import android.support.annotation.NonNull;

public class MyCustomLocation{
    private double latitude;
    private double longitude;

    public MyCustomLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public String toString() {
        return "MyCustomLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
