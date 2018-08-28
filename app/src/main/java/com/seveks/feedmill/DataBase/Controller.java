package com.seveks.feedmill.DataBase;

public class Controller{
    private String ip, name;
    private int controllerId;

    public Controller(int controllerId, String ip, String name){
        this.controllerId = controllerId;
        this.ip = ip;
        this.name = name;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setControllerId(int controllerId) {
        this.controllerId = controllerId;
    }

    public int getId() {
        return controllerId;
    }

    public String getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

}