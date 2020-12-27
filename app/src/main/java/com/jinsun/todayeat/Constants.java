package com.jinsun.todayeat;

public final class Constants {
    //activity splash
    public static final int SPLASH_DELAY_MILLIS = 1000;

    //pager
    public static final int PAGER_SEARCH = 0;
    public static final int PAGER_EXCLUSION_LIST = 1;

    //activity result
    public static final int GPS_ENABLE_REQUEST_CODE     = 2001;
    public static final int PERMISSIONS_REQUEST_CODE    = 100;
    public static final int ADDRESS_REQUEST_CODE        = 2002;
    public static final String EXTRA_ADDRESS            = "EXTRA_ADDRESS";
    public static final String EXTRA_LAT                = "EXTRA_LAT";
    public static final String EXTRA_LON                = "EXTRA_LON";

    //foods
    public enum FOODS {KOREAN, CHINESE, JAPANESE, WESTERN, FLOUR}
    public static final String[] STR_FOODS = {"한식", "중식", "일식", "양식", "분식"};

    //POI
    public static final int POI_MAX_PAGE     = 50;
    public static final int POI_MAX_COUNT    = 200;
    public static final int TMAP_VERSION     = 1;

}
