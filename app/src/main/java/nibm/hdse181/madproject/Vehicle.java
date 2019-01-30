package nibm.hdse181.madproject;

import com.google.firebase.firestore.GeoPoint;


public class Vehicle {
    String id;
    String title;
    boolean available;
    String imageURI;
    GeoPoint location;
    String owner;
    int passengers;
    String type;

    public Vehicle() {
    }

    public Vehicle(String id, String title, boolean available, String imageURI, GeoPoint location, String owner, int passengers, String type) {
        this.id = id;
        this.title = title;
        this.available = available;
        this.imageURI = imageURI;
        this.location = location;
        this.owner = owner;
        this.passengers = passengers;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getPassengers() {
        return passengers;
    }

    public void setPassengers(int passengers) {
        this.passengers = passengers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
