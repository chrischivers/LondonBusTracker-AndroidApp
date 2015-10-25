package com.chrischivers.londonbustracker;

import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;

public class MarkerPair {
    Marker imageMarker;
    Marker textMarker;

    public MarkerPair(Marker imageMarker, Marker textMarker) {
        this.imageMarker = imageMarker;
        this.textMarker = textMarker;
    }
}
