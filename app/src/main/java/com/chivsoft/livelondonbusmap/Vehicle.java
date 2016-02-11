package com.chivsoft.livelondonbusmap;

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
