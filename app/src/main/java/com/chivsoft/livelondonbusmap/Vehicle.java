package com.chivsoft.livelondonbusmap;

public class Vehicle {
    String reg;
    MarkerPair markerPair;
    long nextArrivalTime = 0;
    String routeID;
    String direction;
    String towards;
    String nextStopID;
    String nextStopName;


    public Vehicle(String reg, MarkerPair markerPair, String routeID, String direction, String towards, String nextStopID, String nextStopName) {
        this.reg = reg;
        this.markerPair = markerPair;
        this.routeID = routeID;
        this.direction = direction;
        this.towards = towards;
        this.nextStopID = nextStopID;
        this.nextStopName = nextStopName;
    }

    public void setStateParameters(String routeID, String direction, String towards, String nextStopID, String nextStopName) {
        this.routeID = routeID;
        this.direction = direction;
        this.towards = towards;
        this.nextStopID = nextStopID;
        this.nextStopName = nextStopName;

    }

}
