package nibm.hdse181.madproject;

import com.google.firebase.firestore.GeoPoint;

public class Owner {
    String id;
    String name;
    String email;
    String mobile;
    String photoURI;
    GeoPoint location;

    public Owner() {

    }

    public Owner(String id, String name, String email, String mobile, String photoURI, GeoPoint location) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.photoURI = photoURI;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhotoURI() {
        return photoURI;
    }

    public void setPhotoURI(String photoURI) {
        this.photoURI = photoURI;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
