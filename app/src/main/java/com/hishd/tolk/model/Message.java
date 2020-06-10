package com.hishd.tolk.model;

import com.google.firebase.Timestamp;

import java.util.Map;

public class Message {
    public String sender;
    public String sender_email;
    public String message;
    public Timestamp timestamp;
    public String image_url;

    public Message() {
    }

    public Message(String sender, String sender_email, String message, Timestamp timestamp, String image_url) {
        this.sender = sender;
        this.sender_email = sender_email;
        this.message = message;
        this.timestamp = timestamp;
        this.image_url = image_url;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getSender_email() {
        return sender_email;
    }

    public void setSender_email(String sender_email) {
        this.sender_email = sender_email;
    }
}
