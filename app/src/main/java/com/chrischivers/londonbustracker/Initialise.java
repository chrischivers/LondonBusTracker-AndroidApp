package com.chrischivers.londonbustracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialise);
        setUpComponents();
        getRouteList();
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
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        runButton = (Button) findViewById(R.id.runButton);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                List<String> selectedList = new ArrayList<String>();

                for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                    if (checked.get(i)) {
                        selectedList.add((String) listView.getItemAtPosition(i));
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
        String urlString = "http://vmi49109.contabo.host/route_list_request.asp";
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
            List<String> routeList = new ArrayList<String>();

            try {
                connection = (HttpURLConnection) urls[0].openConnection();
                connection.setRequestMethod("GET");

// Get Response
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                responseStr = reader.readLine();
                JSONObject object = new JSONObject(responseStr);
                JSONArray jArray = object.getJSONArray("routeList");

                for (int i = 0; i < jArray.length(); i++) {
                    routeList.add(jArray.getString(i));
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
            listView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.list_black_test, result));
        }
    }

}

