package com.chrischivers.londonbustracker;

import com.google.android.gms.maps.model.LatLng;

public class MoveInstruction {

    Vehicle vehicle;
    String route;
    String towards;
    String nextStopName;
    String reg;
    LatLng[] latLngArray;
    int[] rotationArray;
    double[] proportionalDurationArray;
    long nextArrivalTime;



    public MoveInstruction(Vehicle vehicle, String route, String towards, String nextStopName, String reg, LatLng[] latLngArray, int[] rotationArray, double[] proportionalDurationArray, long nextArrivalTime) {
        this.vehicle = vehicle;
        this.route  = route;
        this.towards = towards;
        this.nextStopName = nextStopName;
        this.reg = reg;
        this.latLngArray = latLngArray;
        this.rotationArray = rotationArray;
        this.proportionalDurationArray = proportionalDurationArray;
        this.nextArrivalTime = nextArrivalTime;

    }
}


//Vehicle vehicle, MoveInstruction mI, String route, String towards, String nextStopName, String reg