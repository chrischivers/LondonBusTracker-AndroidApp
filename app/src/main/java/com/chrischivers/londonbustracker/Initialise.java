package com.chrischivers.londonbustracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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

public class Initialise extends AppCompatActivity {

    private ListView listView;
    private Button runButton;
    private CustomArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialise);
        setUpComponents();
        getRouteList();

    }

    public void setArrayAdapter (CustomArrayAdapter adapter) {
        this.adapter = adapter;
        listView.setAdapter(adapter);
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
        runButton = (Button) findViewById(R.id.runButton);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> selectedList = new ArrayList<String>();
                ArrayList<RouteFromToObj> routeList = adapter.routeList;

                for (int i = 0; i < routeList.size(); i++) {
                    if (routeList.get(i).isSelected()) {
                        selectedList.add((routeList.get(i).routeID));
                    }
                }
                System.out.println("Selected List: " + selectedList);
                String csvList = TextUtils.join(",",selectedList);
                Intent i = new Intent(getApplicationContext(), mapUI.class);
                i.putExtra("RouteSelection", csvList);
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
            setArrayAdapter(new CustomArrayAdapter(getApplicationContext(), R.layout.route_list_item, R.id.routeIDText, result));

        }
    }

}

