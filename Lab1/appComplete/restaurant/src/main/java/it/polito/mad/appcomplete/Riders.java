package it.polito.mad.appcomplete;

import android.location.Location;

public class Riders {
    String id;
    String pic;
    String name;
    Location location;

    public Riders(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude(){
        return location.getLatitude();
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getLongitude(){
        return location.getLongitude();
    }


}
