
package com.frasker.radarview;

public class RadarData {

    private String title;
    private double percent;

    public RadarData(String title, double percent /* 0 - 1*/) {
        this.title = title;
        this.percent = percent;
    }

    public String getTitle() {
        return title;
    }

    public double getPercent() {
        return percent;
    }
}