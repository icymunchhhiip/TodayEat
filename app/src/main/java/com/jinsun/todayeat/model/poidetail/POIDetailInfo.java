package com.jinsun.todayeat.model.poidetail;

public class POIDetailInfo {
    private String id;
    private String name;
    private String tel;
    private Double frontLat;
    private Double frontLon;
    private Double lat;
    private Double lon;
    private String point;
    private String additionalInfo;
    private String desc;
//    private String twFlag;
//    private String parkFlag;
//    private String yaFlag;
//    private String facility;
//    private String routeInfo;
//    private String homepageURL;
//    private String viewId;
//    private String bizCatName;
//    private String address;
//    private String firstNo;
//    private String secondNo;
//    private String zipCode;
//    private String mlClass;
//    private String upperLegalCode;
//    private String middleLegalCode;
//    private String lowerLegalCode;
//    private String detailLegalCode;
//    private String upperAdminCode;
//    private String middleAdminCode;
//    private String lowerAdminCode;
//    private String merchantFlag;
//    private String merchantDispType;
    //메뉴1~5
    //전기차 관련 정보


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTel() {
        return tel;
    }

    public Double getFrontLat() {
        return frontLat;
    }

    public Double getFrontLon() {
        return frontLon;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public String getPoint() {
        return point;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public String getDesc() {
        return desc;
    }
}
