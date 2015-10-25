package com.chrischivers.londonbustracker;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class mapUI extends FragmentActivity  {


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private TextView mapTextBelow;

    private final String wsuri = "ws://23.92.71.114/";
    private  Map<String, Vehicle> vehicleMap = new HashMap<String, Vehicle>();
    private String mapTextBelowHoldingReg = "";

    private String routeSelection;
    private String MODE;
    private String MODE_LIST_SELECTION = "LIST_SELECTION";
    private String MODE_RADIUS = "RADIUS";
    private int RADIUS_DEFAULT_LENGTH = 1500;
    private LatLng currentPosition = null;
    private LatLng DEFAULT_POSITION = new LatLng(51.508155,-0.128317);

    private final WebSocketConnection mConnection = new WebSocketConnection();
    Handler handler = new Handler();
    IconGenerator iconFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // First we need to check availability of play services

        setContentView(R.layout.activity_maps);
        mapTextBelow = (TextView) findViewById(R.id.mapTextBelow);
        setUpIconFactory();

        Bundle bundle = getIntent().getParcelableExtra("LocationBundle");
        assert(bundle != null);
        Location location = bundle.getParcelable("FetchedLocation");
        if (location != null) {
            currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            System.out.println("Current Position: " + currentPosition);
        }

        MODE = getIntent().getStringExtra("MODE");
        if (MODE.equals(MODE_LIST_SELECTION)) {
            routeSelection = getIntent().getStringExtra("RouteSelection");

            setUpMapIfNeeded();
            setUpWebSocket();
        } else if (MODE.equals(MODE_RADIUS)) {
            setUpMapIfNeeded();
            setUpWebSocket();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetMap();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
        setUpMap();
        mMap.setInfoWindowAdapter(new MapWindowAdapter(this));
    }

    private void resetMap() {
        mMap.clear();
        vehicleMap.clear();
    }


    private void setUpMap() {
        if (currentPosition != null ) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPosition, 14);
            mMap.animateCamera(cameraUpdate);
        } else {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_POSITION, 14);
            mMap.animateCamera(cameraUpdate);
        }
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    currentPosition = cameraPosition.target;
                    if (MODE.equals(MODE_RADIUS)) {
                        mConnection.sendTextMessage("RADIUS," + RADIUS_DEFAULT_LENGTH + ",(" + currentPosition.latitude + ", " + currentPosition.longitude + ")");

                        //Map<String, Vehicle> vehicleMapCopy = vehicleMap;
                            Set<String> keySet = vehicleMap.keySet();
                            Set<Vehicle> toRemove = new HashSet<Vehicle>();
                            for (String key : keySet) {
                                Vehicle v = vehicleMap.get(key);
                                LatLng markerPosition = v.markerPair.textMarker.getPosition();
                                float[] distance = new float[1];
                                Location.distanceBetween(currentPosition.latitude, currentPosition.longitude, markerPosition.latitude, markerPosition.longitude, distance);
                                if (distance[0] > RADIUS_DEFAULT_LENGTH) {
                                    toRemove.add(v);
                                }
                            }
                            for (Vehicle v: toRemove) {
                                killMarker(v);
                            }
                    }
                }
            });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mapTextBelow.setText(marker.getSnippet());
                mapTextBelowHoldingReg = marker.getTitle();
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapTextBelow.setText("");
                mapTextBelowHoldingReg = "";
            }
        });

    }

    private void setUpWebSocket() {

        try {
            mConnection.connect(wsuri, new WebSocketHandler() {

                @Override
                public void onOpen() {
                    Toast.makeText(getApplicationContext(), "Connected to server. Please wait 1-2 minutes for vehicles to sync.", Toast.LENGTH_SHORT).show();
                    if (MODE.equals(MODE_LIST_SELECTION)) {
                        mConnection.sendTextMessage("ROUTELIST," + routeSelection);
                    } else if (MODE.equals(MODE_RADIUS)) {
                        if (currentPosition != null) {
                            mConnection.sendTextMessage("RADIUS," + RADIUS_DEFAULT_LENGTH + ",(" + currentPosition.latitude + ", " + currentPosition.longitude + ")");
                        } else {
                            mConnection.sendTextMessage("RADIUS," + RADIUS_DEFAULT_LENGTH + ",(" + DEFAULT_POSITION.latitude + ", " + DEFAULT_POSITION.longitude + ")");
                        }
                    }
                }

                @Override
                public void onTextMessage(String payload) {
                    processMessage(payload);
                }

                @Override
                public void onClose(int code, String reason) {
                    Toast.makeText(getApplicationContext(), "Connection dropped. Please reload.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (WebSocketException e) {

            System.out.println(e.toString());
        }
    }

    private void setUpIconFactory() {
        iconFactory = new IconGenerator(this);
        iconFactory.setBackground(new ColorDrawable(Color.TRANSPARENT));
        iconFactory.setTextAppearance(R.style.markerText);
    }

    private void processMessage(String message) {
        try {
            JSONObject jObject = new JSONObject(message);
            System.out.println(jObject);
            if (jObject.getString("nextArr").equals("kill")) {
                Vehicle v = vehicleMap.get(jObject.getString("reg"));
                killMarker(v);
            } else {
                setNewLocation(jObject.getString("reg"), jObject.getLong("nextArr"), jObject.getString("movementData"), jObject.getString("routeID"), jObject.getInt("directionID"), jObject.getString("towards"), jObject.getString("nextStopID"), jObject.getString("nextStopName"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void killMarker(Vehicle v) {
        if (v != null) {
            v.markerPair.textMarker.remove();
            v.markerPair.imageMarker.remove();
            vehicleMap.remove(v.reg);
        }
    }

    private void setNewLocation(final String reg, final long nextArr, String movementDataArray, final String routeID, final int directionID, final String towards, final String nextStopID, final String nextStopName) {
        String splitArray[] = movementDataArray.substring(2,movementDataArray.length() -2).split("\",\"");
        final LatLng latLngArray[] = new LatLng[splitArray.length];
        final int rotationArray[] = new int[splitArray.length];
        final double proportionalDurationArray[] = new double[splitArray.length];

        for (int i = 0; i < splitArray.length; i++) {
            String entry[] = splitArray[i].split(",");
            latLngArray[i] = new LatLng(Double.parseDouble(entry[0]),Double.parseDouble(entry[1]));
            rotationArray[i] = Integer.parseInt(entry[2]);
            proportionalDurationArray[i] = Double.parseDouble(entry[3]);
        }

        if(vehicleMap.containsKey(reg)) {
            Vehicle v = vehicleMap.get(reg);
            v.setStateParameters(routeID,directionID,towards,nextStopID,nextStopName);
            moveVehicle(new MoveInstruction(v, routeID, towards, nextStopName, reg, latLngArray, rotationArray, proportionalDurationArray, nextArr));

        } else {
            MarkerPair mp = addMarkerPair(routeID, latLngArray[0], rotationArray[0], reg, directionID, towards, nextStopID, nextStopName);
            Vehicle newVehicle = new Vehicle(reg,mp,routeID,directionID,towards,nextStopID,nextStopName);
            vehicleMap.put(reg, newVehicle);
            moveVehicle(new MoveInstruction(newVehicle, routeID, towards, nextStopName, reg, latLngArray, rotationArray, proportionalDurationArray, nextArr));
        }
    }

    private void moveVehicle(MoveInstruction mI) {
        Thread moveInstruction = new Thread(new MoveMarker(mI));
        long waitTime = mI.vehicle.nextArrivalTime - System.currentTimeMillis();
        System.out.println("Wait Time: " + waitTime);
        if (waitTime <= 0) {
            moveInstruction.run();
        } else {
            handler.postDelayed(moveInstruction, waitTime);
        }
    }

    private MarkerPair addMarkerPair(String routeID, LatLng initialPosition, int initialRotation, String reg, int directionID, String towards, String nextStopID, String nextStopName) {

        MarkerOptions imageMarkerOptions = new MarkerOptions()
                .position(new LatLng(initialPosition.latitude + 0.000005, initialPosition.longitude + 0.000005)) // Offset necessary to avoid flickering markers in UI
                .rotation(initialRotation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.busicon))
                .title(reg)
                .snippet("Route: " + routeID + " towards " + towards + "\n" + "Next Stop: " + nextStopName)
                .anchor(0.5f, 0.5f)
                .visible(false);


        MarkerOptions textMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(routeID)))
                .position(initialPosition)
                .rotation(initialRotation)
                .title(reg)
                .snippet("Route: " + routeID +" towards " + towards + "\n" + "Next Stop: " + nextStopName)
                .anchor(0.5f, 0.5f)
                .visible(false);


        Marker imageMarker = mMap.addMarker(imageMarkerOptions);
        Marker textMarker = mMap.addMarker(textMarkerOptions);
        return new MarkerPair(imageMarker, textMarker);
    }





  class MoveMarker implements Runnable {
      //TODO update routeID etc
      private MoveInstruction mI;
      private Handler handler = new Handler();
      private LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Linear();

      public MoveMarker(MoveInstruction mI) {
          this.mI = mI;
      }

      @Override
      public void run() {
          mI.vehicle.markerPair.imageMarker.setVisible(true);
          mI.vehicle.markerPair.textMarker.setVisible(true);

          mI.vehicle.markerPair.imageMarker.setSnippet("Route: " + mI.route + " towards " + mI.towards + "\n" + "Next Stop: " + mI.nextStopName);
          mI.vehicle.markerPair.textMarker.setSnippet("Route: " + mI.route + " towards " + mI.towards + "\n" + "Next Stop: " + mI.nextStopName);

          //Sets text view if marker currently being shown
          if (mapTextBelowHoldingReg.equals(mI.reg)) {
              mapTextBelow.setText(mI.vehicle.markerPair.imageMarker.getSnippet());
          }

          mI.vehicle.nextArrivalTime = mI.nextArrivalTime;
          long dur = (mI.nextArrivalTime - System.currentTimeMillis());
          if (dur < 0) {
              dur = 0;
          }
        //  System.out.println("Dur" + dur);
          long startTimeOffsetAccumulator = 0;
          for (int i = 0; i < mI.latLngArray.length; i++) {

              final long actualDuration = Math.round(dur * mI.proportionalDurationArray[i]);
            //  System.out.println("Proportional duration: " + mI.proportionalDurationArray[i]);
            //  System.out.println("Actual duration: " + actualDuration);
            //  System.out.println("Accumulator: " + startTimeOffsetAccumulator);
           //   System.out.println("LatLng: " + mI.latLngArray[i]);
              final LatLng latLng  = mI.latLngArray[i];
              final int rotation = mI.rotationArray[i];
              handler.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                      mI.vehicle.markerPair.imageMarker.setRotation(rotation);
                      MarkerAnimation.animateMarkerToHC(mI.vehicle.markerPair, latLng, latLngInterpolator, actualDuration);
                  }
              }, (startTimeOffsetAccumulator));
              startTimeOffsetAccumulator += actualDuration;

          }
      }
  }
}