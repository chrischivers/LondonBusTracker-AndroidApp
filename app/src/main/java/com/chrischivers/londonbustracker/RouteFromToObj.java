package com.chrischivers.londonbustracker;

/**
 * Created by chrischivers on 21/10/15.
 */
public class RouteFromToObj {
    String routeID;
    String to;
   String from;
    private boolean selected = false;



    public RouteFromToObj(String routeID, String to, String from) {
        this.routeID = routeID;
        this.to = to;
        this.from = from;
    }

    public void setSelected(boolean b) {
        selected = b;
    }

    public boolean isSelected() {
        return selected;
    }
}
