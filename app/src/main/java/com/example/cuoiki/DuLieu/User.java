package com.example.cuoiki.DuLieu;

public class User {
    private String username;
    private String password;
    private String gmail;
    private String phone_number;
    private String full_name;
    private String role;

    public User(String username, String password, String gmail, String phone_number, String full_name, String role) {
        this.username = username;
        this.password = password;
        this.gmail = gmail;
        this.phone_number = phone_number;
        this.full_name = full_name;
        this.role = role;
    }

    //  Getter & Setter cho tất cả các trường

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
