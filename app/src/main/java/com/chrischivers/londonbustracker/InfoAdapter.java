package com.chrischivers.londonbustracker;


        import android.view.LayoutInflater;
        import android.view.View;
        import android.widget.TextView;

        import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
        import com.google.android.gms.maps.model.Marker;

public class InfoAdapter implements InfoWindowAdapter {
    LayoutInflater inflater = null;
    private mapUI mapUI;
    private TextView route;
    private TextView towards;
    private TextView nextStop;
    private TextView reg;


    public InfoAdapter(LayoutInflater inflater, mapUI mapUI) {
        this.inflater = inflater;
        this.mapUI = mapUI;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View v = inflater.inflate(R.layout.info_window, null);
        if (marker != null) {
            route = (TextView) v.findViewById(R.id.infoWindowRoute);
            towards = (TextView) v.findViewById(R.id.infoWindowTowards);
            nextStop = (TextView) v.findViewById(R.id.infoWindowNextStop);
            reg = (TextView) v.findViewById(R.id.infoWindowVehicleReg);
            if (marker.getSnippet() != null) {
               String snippetSplit[] = marker.getSnippet().split(",");
                route.setText("Route: " + snippetSplit[0]);
                towards.setText("Towards: " + snippetSplit[1]);
                nextStop.setText("Next Stop: " + snippetSplit[2]);
                reg.setText("Vehicle reg: " + snippetSplit[3]);
            }
        }
        return (v);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return (null);
    }
}
