package com.example.roomgan;

import android.app.Application;

import android.util.Base64;

public class GlobalState extends Application {

    private String username;
    private String password;

    public void setUsername(String username){
        this.username = username;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public String getToken(){
        return "Basic " + Base64.encodeToString((this.username+":"+this.password).getBytes(), Base64.NO_WRAP);
    }
}
