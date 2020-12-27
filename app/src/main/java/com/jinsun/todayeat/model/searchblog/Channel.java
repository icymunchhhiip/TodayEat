package com.jinsun.todayeat.model.searchblog;

public class Channel {
    private String lastBuildDate;
    private String postdate;
    private int total;
    private int start;
    private int display;
    private Items items;

    public String getLastBuildDate() {
        return lastBuildDate;
    }

    public String getPostdate() {
        return postdate;
    }

    public int getTotal() {
        return total;
    }

    public int getStart() {
        return start;
    }

    public int getDisplay() {
        return display;
    }

    public Items getItems() {
        return items;
    }
}
