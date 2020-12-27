package com.jinsun.todayeat.model;

public class ExceptedPlaceModel {
    private int id;
    private String name;
    private String addr;
    private String dong;

    public ExceptedPlaceModel(int id, String name, String addr, String dong) {
        this.id = id;
        this.name = name;
        this.addr = addr;
        this.dong = dong;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddr() {
        return addr;
    }

    public String getDong() {
        return dong;
    }

}
