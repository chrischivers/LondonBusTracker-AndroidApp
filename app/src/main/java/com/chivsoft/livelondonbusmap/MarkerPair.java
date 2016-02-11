package com.chivsoft.livelondonbusmap;

import com.google.android.gms.maps.model.Marker;

public class MarkerPair {
    Marker imageMarker;
    Marker textMarker;

    public MarkerPair(Marker imageMarker, Marker textMarker) {
        this.imageMarker = imageMarker;
        this.textMarker = textMarker;
    }
}
