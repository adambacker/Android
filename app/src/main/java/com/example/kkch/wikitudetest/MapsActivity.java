package com.example.kkch.wikitudetest;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.games.internal.api.StatsImpl;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    static final String TAG = "tag";
    private GoogleMap mMap;
    int iZoom = 16;
    double myLatitude = 0;
    double myLongitude = 0;
    double[] latitude;
    double[] longitude;
    String [] placeName;
    int placeNum = 0;
    String site_url = "http://5.189.171.31:7033/api/wikitudes/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        new JsonSearchTask().execute();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        initMap_GPS();
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        MyLocationListener myLocationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, myLocationListener);

        Intent intent = getIntent();
        if (intent != null) {
            MyLocation myLocation = (MyLocation) intent.getSerializableExtra("myLocation");
            myLatitude = myLocation.getLatitude();
            myLongitude = myLocation.getLongitude();
            LatLng myLatLng = new LatLng(myLatitude, myLongitude);
            mMap.addMarker(new MarkerOptions()
                    .position(myLatLng)
                    .title("My Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, iZoom));
            Log.d(TAG, "onMapReady: !!!!!!!!!!"+placeNum);

        }
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void initMap_GPS() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private class JsonSearchTask extends AsyncTask<Void, Void, Void> {
        String resultString = "";

        @Override
        protected Void doInBackground(Void... params) {
            try {
                resultString = ParseStringResult(sendQuery(site_url));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {


            super.onPostExecute(aVoid);
        }
    }

    private String sendQuery(String query) throws IOException{
        String result = "";

        URL searchURL = new URL(query);

        HttpURLConnection httpURLConnection = (HttpURLConnection) searchURL.openConnection();

        if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

            InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader, 8192);

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }

            bufferedReader.close();
        }
        Log.d(TAG, "sendQuery!!!!!!!!!:"+query);
        Log.d(TAG, "Result!!!!!!!!!:"+result);
        return result;
    }

    private String ParseStringResult(String json) throws JSONException{
        String parsedResult = "";
        Log.d(TAG, "Json!!!!!!!!!:"+json);
        JSONArray jsonArray = new JSONArray(json);
        placeNum = jsonArray.length();
        Log.d(TAG, "ParseStringResult: !!!!!!!!!!"+placeNum);
        latitude = new double[placeNum];
        longitude = new double[placeNum];
        placeName = new String[placeNum];
        for (int i=0;i<placeNum;i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            latitude[i] = jsonObject.getDouble("latitude");
            longitude[i] = jsonObject.getDouble("longitude");
            placeName[i] = jsonObject.getString("name");
        }

        //JSONObject jsonObject_responseData = jsonObject.getJSONObject("queries");
        //Log.d(TAG, "parsedResult!!!!!!!!!:"+parsedResult);
        //String ssss = jsonObject_responseData.getString("id");
        //Log.d(TAG, "parsedResult!!!!!!!!!:"+ssss);
        //JSONArray jsonArray_results = jsonObject_responseData.getJSONArray("request");
        //Log.d(TAG, jsonArray_results.getJSONObject(0).getString("title"));
        //parsedResult += "Google Search APIs (JSON) for : <b>" + search_item + "</b><br/>";
        //parsedResult += "Number of results returned = <b>" + jsonArray_results.length() + "</b><br/><br/>";

//        for(int i = 0; i < jsonArray_results.length(); i++){
//
//            JSONObject jsonObject_i = jsonArray_results.getJSONObject(i);
//
//            String iTitle = jsonObject_i.getString("title");
//            String iContent = jsonObject_i.getString("content");
//            String iUrl = jsonObject_i.getString("url");
//
//            parsedResult += "<a href='" + iUrl + "'>" + iTitle + "</a><br/>";
//            parsedResult += iContent + "<br/><br/>";
//        }


        return parsedResult;
    }


    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            String str = String.format("%.7f", location.getLatitude());
//            tvLatitudeValue.setText(str);
//            str = String.format("%.7f", location.getLongitude());
//            tvLongitudeValue.setText(str);
            mMap.clear();

            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("My Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location)));
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, iZoom));
            for (int i=0;i<placeNum;i++) {

                latLng = new LatLng(latitude[i], longitude[i]);
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(placeName[i])
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location)));
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, iZoom));
            }
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }
        @Override
        public void onProviderEnabled(String s) {

        }
        @Override
        public void onProviderDisabled(String s) {

        }
    }
}
