package com.jinsun.todayeat.model;

import com.jinsun.todayeat.Constants;

public class UserRequestData {
    private double mLat;
    private double mLon;
    private String mAddress;
    private boolean[] mFoods;
    private int mRadius; //km 단위

    public UserRequestData() {
        mLat = -200;
        mLon = -200;
        mAddress = "";
        mFoods = new boolean[5];
        mRadius = 1;
    }

    public String getFoodsToString(){
        StringBuilder searchFoods = new StringBuilder();
        boolean[] foods = getFoods();
        for (int i = 0; i < 5; i++) {
            if (foods[i]) {
                searchFoods.append(Constants.STR_FOODS[i]);
                searchFoods.append(";");
            }
        }
        return searchFoods.toString();
    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double lat) {
        mLat = lat;
    }

    public double getLon() {
        return mLon;
    }

    public void setLon(double lon) {
        mLon = lon;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public boolean[] getFoods() {
        return mFoods;
    }

    public void setKorean(boolean b) {
        mFoods[Constants.FOODS.KOREAN.ordinal()] = b;
    }

    public void setChinese(boolean b) {
        mFoods[Constants.FOODS.CHINESE.ordinal()] = b;
    }

    public void setJapanese(boolean b) {
        mFoods[Constants.FOODS.JAPANESE.ordinal()] = b;
    }

    public void setWestern(boolean b) {
        mFoods[Constants.FOODS.WESTERN.ordinal()] = b;
    }

    public void setFlour(boolean b) {
        mFoods[Constants.FOODS.FLOUR.ordinal()] = b;
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int radius) {
        mRadius = radius;
    }

    public boolean isReadyData() {
        boolean isReadyFoods = false;
        for (boolean i : mFoods) {
            if (i) {
                isReadyFoods = true;
                break;
            }
        }

        return (mLat != -200) && (mLon != -200) && (!mAddress.equals("")) && isReadyFoods;
    }

}
