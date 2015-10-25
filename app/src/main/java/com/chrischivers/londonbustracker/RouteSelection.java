package com.chrischivers.londonbustracker;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RouteSelection extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private final static int MAX_NUMBER_ROUTES = 10;

    private ListView listView;
    private Button useSelectedButton;
    private Button useCurrentLocationButton;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private ArrayAdapter<RouteFromToObj> routeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialise);
        setUpComponents();
        getRouteList();

        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_initialise, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpComponents() {
        listView = (ListView) findViewById(R.id.routeListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RouteFromToObj routeFromToObj = routeAdapter.getItem(position);
                routeFromToObj.toggleChecked();
                RouteViewHolder viewHolder= (RouteViewHolder) view.getTag();
                viewHolder.getCheckBox().setChecked(routeFromToObj.isChecked());
            }
        });
        useSelectedButton = (Button) findViewById(R.id.useSelectedButton);
        useSelectedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> selectedList = new ArrayList<String>();

                for (int i = 0; i < routeAdapter.getCount(); i++) {
                    if (routeAdapter.getItem(i).isChecked()) {
                        selectedList.add((routeAdapter.getItem(i).getRouteID()));
                    }
                }
                if (selectedList.size() == 0) {
                    Toast.makeText(getApplicationContext(),"No routes selected", Toast.LENGTH_SHORT).show();
                }
                else if (selectedList.size() > MAX_NUMBER_ROUTES) {
                    Toast.makeText(getApplicationContext(),"Too many routes selected. Maximum allowed is " + MAX_NUMBER_ROUTES +".", Toast.LENGTH_SHORT).show();
                } else {
                    String csvList = TextUtils.join(",", selectedList);
                    Intent i = new Intent(getApplicationContext(), mapUI.class);

                    Bundle args = new Bundle();
                    args.putParcelable("FetchedLocation", mLastLocation);
                    i.putExtra("LocationBundle", args);
                    i.putExtra("MODE", "LIST_SELECTION");
                    i.putExtra("RouteSelection", csvList);
                    startActivity(i);
                }
            }
        });
        useCurrentLocationButton = (Button) findViewById(R.id.useCurrentLocationButton);
        useCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), mapUI.class);

                Bundle args = new Bundle();
                args.putParcelable("FetchedLocation", mLastLocation);
                i.putExtra("LocationBundle", args);
                i.putExtra("MODE","RADIUS");
                startActivity(i);
            }
        });

    }

    private void getRouteList() {
        String urlString = "http://23.92.71.114/route_list_with_first_last_stops_request.asp";
        try {
            URL url = new URL(urlString);
            new getRouteList().execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private class getRouteList extends AsyncTask<URL, Void, List> {
        protected List doInBackground(URL... urls) {
            HttpURLConnection connection = null;
            String responseStr = "";
            List<RouteFromToObj> routeList = new ArrayList<RouteFromToObj>();

            try {
                connection = (HttpURLConnection) urls[0].openConnection();
                connection.setRequestMethod("GET");

                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                responseStr = reader.readLine();
                JSONObject object = new JSONObject(responseStr);
                JSONArray jArray = object.getJSONArray("routeList");

                for (int i = 0; i < jArray.length(); i++) {
                    String str = jArray.getString(i);
                    String splitStr[] = str.split(";");
                    routeList.add(new RouteFromToObj(splitStr[0], splitStr[1], splitStr[2]));
                }

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return routeList;

        }

        protected void onPostExecute(List result) {
            //setArrayAdapter(new CustomArrayAdapter(getApplicationContext(), R.layout.route_list_item, R.id.routeIDText, result));
            ArrayList<RouteFromToObj> routeFromToObjList = new ArrayList<RouteFromToObj>();
            routeFromToObjList.addAll(result);
            routeAdapter = new RouteArrayAdapter(getApplicationContext(), routeFromToObjList);
            listView.setAdapter(routeAdapter);
        }
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(this, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    private static class RouteViewHolder {
        private CheckBox checkBox ;
        private TextView fromToTextView ;
        private TextView routeTextView;

        public RouteViewHolder() {}
        public RouteViewHolder( TextView routeTextView, TextView fromToTextView, CheckBox checkBox ) {
            this.checkBox = checkBox ;
            this.fromToTextView = fromToTextView ;
            this.routeTextView= routeTextView;
        }
        public CheckBox getCheckBox() {
            return checkBox;
        }
        public void setCheckBox(CheckBox checkBox) {
            this.checkBox = checkBox;
        }
        public TextView getRouteTextView() {
            return routeTextView;
        }

        public TextView getFromToTextView() {
            return fromToTextView;
        }
        public void setRouteTextView(TextView textView) {
            this.routeTextView = textView;
        }

        public void setFromToTextView(TextView textView) {
            this.fromToTextView = textView;
        }
    }

    private static class RouteArrayAdapter extends ArrayAdapter<RouteFromToObj> {

        private LayoutInflater inflater;

        public RouteArrayAdapter( Context context, List<RouteFromToObj> routeList) {
            super( context, R.layout.route_list_item, R.id.routeIDText, routeList );
            // Cache the LayoutInflate to avoid asking for a new one each time.
            inflater = LayoutInflater.from(context) ;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Planet to display
            RouteFromToObj route = this.getItem( position );

            // The child views in each row.
            CheckBox checkBox ;
            TextView routeIDTextView ;
            TextView routeToFromTextView;

            // Create a new row view
            if ( convertView == null ) {
                convertView = inflater.inflate(R.layout.route_list_item, null);

                // Find the child views.
                routeIDTextView = (TextView) convertView.findViewById( R.id.routeIDText );
                routeToFromTextView = (TextView) convertView.findViewById( R.id.routeFromToText);
                checkBox = (CheckBox) convertView.findViewById( R.id.routeSelectedCheckbox );

                // Optimization: Tag the row with it's child views, so we don't have to
                // call findViewById() later when we reuse the row.
                convertView.setTag( new RouteViewHolder(routeIDTextView,routeToFromTextView,checkBox) );

                // If CheckBox is toggled, update the planet it is tagged with.
                checkBox.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        RouteFromToObj route = (RouteFromToObj) cb.getTag();
                        route.setChecked( cb.isChecked() );
                    }
                });
            }
            // Reuse existing row view
            else {
                // Because we use a ViewHolder, we avoid having to call findViewById().
                RouteViewHolder viewHolder = (RouteViewHolder) convertView.getTag();
                checkBox = viewHolder.getCheckBox() ;
                routeIDTextView = viewHolder.getRouteTextView() ;
                routeToFromTextView = viewHolder.getFromToTextView();
            }

            // Tag the CheckBox with the Planet it is displaying, so that we can
            // access the planet in onClick() when the CheckBox is toggled.
            checkBox.setTag( route );

            // Display planet data
            checkBox.setChecked( route.isChecked() );
            routeIDTextView.setText(route.getRouteID() );
            routeToFromTextView.setText(route.getFromTo());

            return convertView;
        }

    }

    private static class RouteFromToObj {
        private String routeID;
        private String to;
        private String from;
        private boolean checked = false;



        public RouteFromToObj(String routeID, String to, String from) {
            this.routeID = routeID;
            this.to = to;
            this.from = from;
        }

        public void toggleChecked() {
            checked = !checked;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public String getRouteID() {
            return routeID;
        }

        public String getFromTo() {
            return from + " <-> " + to;
        }

        @Override
        public String toString() {
            return ("route: " + routeID + ". to: " + to + ". from: " + from);
        }
    }


}

