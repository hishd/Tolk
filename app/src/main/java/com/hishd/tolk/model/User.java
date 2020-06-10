package com.hishd.tolk.model;

public class User {

    public String name;
    public String email;
    public String password;
    public String image_url;

    public User() {
    }

    public User(String name, String email, String password, String image_url) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.image_url = image_url;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
