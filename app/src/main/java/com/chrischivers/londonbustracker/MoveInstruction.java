package com.chrischivers.londonbustracker;

import com.google.android.gms.maps.model.LatLng;

public class MoveInstruction {

    LatLng[] latLngArray;
    int[] rotationArray;
    double[] proportionalDurationArray;
    long nextArrivalTime;



    public MoveInstruction(LatLng[] latLngArray, int[] rotationArray, double[] proportionalDurationArray, long nextArrivalTime) {
        this.latLngArray = latLngArray;
        this.rotationArray = rotationArray;
        this.proportionalDurationArray = proportionalDurationArray;
        this.nextArrivalTime = nextArrivalTime;

    }
}
