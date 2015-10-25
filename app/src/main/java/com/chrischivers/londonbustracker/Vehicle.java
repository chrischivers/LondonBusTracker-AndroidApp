package com.chrischivers.londonbustracker;

import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;

public class Vehicle {
    String reg;
    MarkerPair markerPair;
    long nextArrivalTime = 0;
    String routeID;
    int directionID;
    String towards;
    String nextStopID;
    String nextStopName;


    public Vehicle(String reg, MarkerPair markerPair, String routeID, int directionID, String towards, String nextStopID, String nextStopName) {
        this.reg = reg;
        this.markerPair = markerPair;
        this.routeID = routeID;
        this.directionID = directionID;
        this.towards = towards;
        this.nextStopID = nextStopID;
        this.nextStopName = nextStopName;
    }

    public void setStateParameters(String routeID, int directionID, String towards, String nextStopID, String nextStopName) {
        this.routeID = routeID;
        this.directionID = directionID;
        this.towards = towards;
        this.nextStopID = nextStopID;
        this.nextStopName = nextStopName;

    }

}
