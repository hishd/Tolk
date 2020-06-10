package com.hishd.tolk.model;

import com.google.firebase.Timestamp;

public class Chat {
    public  String name;
    public  String image;
    public  String latest_message;
    public  Timestamp latest_timestamp;
    public String email;

    public Chat() {
    }

    public Chat(String name, String image, String latest_message, Timestamp latest_timestamp, String email) {
        this.name = name;
        this.image = image;
        this.latest_message = latest_message;
        this.latest_timestamp = latest_timestamp;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLatest_message() {
        return latest_message;
    }

    public void setLatest_message(String latest_message) {
        this.latest_message = latest_message;
    }

    public Timestamp getLatest_timestamp() {
        return latest_timestamp;
    }

    public void setLatest_timestamp(Timestamp latest_timestamp) {
        this.latest_timestamp = latest_timestamp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
