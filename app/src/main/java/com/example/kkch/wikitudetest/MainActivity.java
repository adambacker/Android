package com.example.kkch.wikitudetest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.StartupConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainActivity extends Activity {

    final String TAG = "tag";

    protected static final String WIKITUDE_SDK_KEY = "tWBu8esDIxgGLoGZYNp998jpgxicORd1tl8XXDeZvmTFsUI3tkaLShNsuXMibK68PHNrj3y3cUYI3BxMv288sgERSC0QmcxxzVjYoHFc/zswwJDtVsopmxZ/uEU67pKlHIlaRf59hfXUQ86wGjNYs2ytGmyERS3JWLSjXlJTxBZTYWx0ZWRfX28Js7CqqstaoPeJCjYDvD0FXi22jE4vnnnoc/0TVpP4i4nH/sofo453KRJ1KUG1nlILiyPAUSJSpFPSi9PE+6H/msY3ou+N9iYJIUsY+omLbHDtvifjzZYI5yROQIhDLZGnhsUHic1S7NI/iKhlPVNX6BbIGWIZh4dSVBHexXkDPZ/XvnWe+kohd/KterMIzZIW5C6kPPwh3wGPLJIEjsKWNFaBAlhBifviHyXS92W3UbEZ4yX323B/tKjuXXzLMR2ILOjerZbHETZ0RPorWH1d2CbHQsnW4maBTHeRUSq7ftRKExt607VZASfSNTxVULhtbob9gRQS1B3VeiJ+dAy6JvlragL+drJ2VR9RZcUejiKC5MMfcIIdMQDr1nwT7EQTYtSSGZ98O2Vvo505ZJL3KjsTHyGxEzXOaR8tT3PaOBjHIW98aKzevFsaRIDXoeCT9SnrDLbI7ThRhmDUFP5lupCOSZqX/82wFQ7KhSxYx12tJz8zVaY=";
    protected static final String MAIN_ACTIVITY_TITLE = "Wikitude Android Sample";
    protected static final String MAIN_ARCHITECT_WORLD_URL = "Sibai/index.html";
    //protected static final int CULLING_DISTANCE_METERS = 50 * 1000;
    protected static final float UNKNOWN_ALTITUDE = -32768f;
    protected ArchitectView architectView;
    protected ArchitectView.SensorAccuracyChangeListener sensorAccuracyListener;
    protected LocationListener locationListener;
    protected ArchitectView.ArchitectUrlListener urlListener;

    protected ILocationProvider locationProvider;

    protected Location lastKnownLocaton;
    protected JSONArray poiData;
    protected boolean isLoading = false;

    private long lastCalibrationToastShownTimeMillis = System.currentTimeMillis();

    protected Bitmap screenCapture = null;
    private static final int WIKITUDE_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 3;

    private GoogleApiClient client;


    LocationManager locationManager;
    double latitude=0, longitude=0;
    long minTime = 1000;
    float minDistance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RequestPermission();
        if (this.isFinishing()) return;

        setContentView(R.layout.activity_main);

        this.setTitle(MAIN_ACTIVITY_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        this.architectView = (ArchitectView)this.findViewById( R.id.architectView );

        final StartupConfiguration config = new StartupConfiguration(
                WIKITUDE_SDK_KEY,
                StartupConfiguration.Features.Geo | StartupConfiguration.Features.Tracking2D,
                StartupConfiguration.CameraPosition.DEFAULT);

        try {
            this.architectView.onCreate(config);
        } catch (RuntimeException rex) {
            this.architectView = null;
            Toast.makeText(getApplicationContext(), "can't create Architect View", Toast.LENGTH_SHORT).show();
            Log.e(this.getClass().getName(), "Exception in ArchitectView.onCreate()", rex);
        }

        this.sensorAccuracyListener = this.getSensorAccuracyListener();

        if (this.urlListener != null && this.architectView != null) {
            this.architectView.registerUrlListener(this.getUrlListener());
        }

        this.locationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onLocationChanged(final Location location) {
                if (location != null) {
//                    latitude = location.getLatitude();
//                    longitude = location.getLongitude();
                    Random random = new Random();
                    double rand = random.nextDouble()/1000;
//                    Toast.makeText(getApplicationContext(), ""+rand,Toast.LENGTH_SHORT).show();

                    latitude = 24.683434+rand;
                    longitude = 46.69787-rand;
//                    Toast.makeText(getApplicationContext(), "location updated.",Toast.LENGTH_LONG).show();
                    MainActivity.this.lastKnownLocaton = location;
                    if (MainActivity.this.architectView != null) {

                        MainActivity.this.architectView.setLocation(
                                latitude,
                                longitude,
                                0.0,
                                (location.hasAccuracy() ? location.getAccuracy() : 20)
                        );
                    }
                }
            }
        };

        this.locationProvider = getLocationProvider(this.locationListener);

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


        this.architectView.registerUrlListener(this.getUrlListener());


    }

    public void showMap(View view) {
        Intent intent = new Intent(getApplication(), MapsActivity.class);
        MyLocation myLocation = new MyLocation(latitude, longitude);
        intent.putExtra("myLocation", myLocation);
        startActivity(intent);
        //architectView.setFlashEnabled(true);
    }
    public void onSetting(View view) {
        Toast.makeText(getApplicationContext(), "Setting button Clicked!", Toast.LENGTH_LONG).show();
    }
    public void onList(View view) {
        Toast.makeText(getApplicationContext(), "List button Clicked!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
/*
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "Wikitude Sample Main Page",
                // TODO: If you have a Web page content that matches the app activity content of, please check whether the Web page URL that has been automatically generated is correct. If you do not have, please specify the "null" in the URL.
                Uri.parse("http://host/path"),
                // TODO: Please check whether the application URL that is automatically generated is correct.
                Uri.parse("android-app://isshiki.mywikitudeappforandroid/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
        */
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (this.architectView != null) {

            this.architectView.onPostCreate();

            try {
                this.architectView.load(MAIN_ARCHITECT_WORLD_URL);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (this.architectView != null) {
            this.architectView.onResume();

            if (this.sensorAccuracyListener != null) {
                this.architectView.registerSensorAccuracyChangeListener(this.sensorAccuracyListener);
            }
        }
        if (this.locationProvider != null) {
            this.locationProvider.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.architectView != null) {
            this.architectView.onDestroy();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.architectView != null) {
            this.architectView.onPause();
            if (this.sensorAccuracyListener != null) {
                this.architectView.unregisterSensorAccuracyChangeListener(this.sensorAccuracyListener);
            }
        }
        if (this.locationProvider != null) {
            this.locationProvider.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
/*
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "Main Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://isshiki.mywikitudeappforandroid/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
        */

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if ( this.architectView != null ) {
            this.architectView.onLowMemory();
        }
    }



    private void RequestPermission() {

        List<String> permissionList = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
            Toast.makeText(this, "You can not start with the camera can not be used.", Toast.LENGTH_LONG).show();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            Toast.makeText(this, "Location information (GPS) can not be started and can not be used.", Toast.LENGTH_LONG).show();
        }
        if (permissionList.size() > 0) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            int REQUEST_CODE_NONE = 0;
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_NONE);

            for (int i = 0; i < 300; i++)
            {
                if (isFinishing()) return;
                try {
                    Thread.sleep(100);
                    Thread.yield();
                } catch (InterruptedException e) {
                    break;
                }

                if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                        (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    return;
                }
            }


            Toast.makeText(this, "After the permission settings, please re-start the app again.", Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    protected ArchitectView.SensorAccuracyChangeListener getSensorAccuracyListener() {
        return new ArchitectView.SensorAccuracyChangeListener() {
            @Override
            public void onCompassAccuracyChanged(int accuracy) {
                // UNRELIABLE = 0, LOW = 1, MEDIUM = 2, HIGH = 3
                if (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM &&
                        MainActivity.this != null && !MainActivity.this.isFinishing() &&
                        System.currentTimeMillis() - MainActivity.this.lastCalibrationToastShownTimeMillis > 5 * 1000) {
                    Toast.makeText(MainActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG).show();
                    MainActivity.this.lastCalibrationToastShownTimeMillis = System.currentTimeMillis();
                }
            }
        };
    }

    public ArchitectView.ArchitectUrlListener getUrlListener() {
        return new ArchitectView.ArchitectUrlListener() {

            @Override
            public boolean urlWasInvoked(String uriString) {
                Uri invokedUri = Uri.parse(uriString);

                if ("button".equalsIgnoreCase(invokedUri.getHost())) {
                    MainActivity.this.architectView.captureScreen(ArchitectView.CaptureScreenCallback.CAPTURE_MODE_CAM_AND_WEBVIEW, new ArchitectView.CaptureScreenCallback() {

                        @Override
                        public void onScreenCaptured(final Bitmap screenCapture) {
                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                MainActivity.this.screenCapture = screenCapture;
                                Toast.makeText(MainActivity.this, "Storage is not available and can not save the screen capture.", Toast.LENGTH_LONG).show();
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WIKITUDE_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                            } else {
                                MainActivity.this.saveScreenCaptureToExternalStorage(screenCapture);
                            }
                        }
                    });
                }
                return true;
            }
        };
    }
    protected void saveScreenCaptureToExternalStorage(Bitmap screenCapture) {
        if (screenCapture != null) {
            final File screenCaptureFile = new File(Environment.getExternalStorageDirectory().toString(), "screenCapture_" + System.currentTimeMillis() + ".jpg");
            try {

                final FileOutputStream out = new FileOutputStream(screenCaptureFile);
                screenCapture.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();

                final Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpg");
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(screenCaptureFile));

                final String chooserTitle = "Share a snapshot";
                MainActivity.this.startActivity(Intent.createChooser(share, chooserTitle));

            } catch (final Exception e) {
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Unexpected error, " + e, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    public ILocationProvider getLocationProvider(final LocationListener locationListener) {
        return new LocationProvider(this, locationListener);
    }

    protected void injectData() {
        if (!isLoading) {
            final Thread t = new Thread(new Runnable() {

                @Override
                public void run() {

                    isLoading = true;

                    final int WAIT_FOR_LOCATION_STEP_MS = 2000;
                    while (lastKnownLocaton == null && !isFinishing()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.location_fetching, Toast.LENGTH_SHORT).show();
                            }
                        });
                        try {
                            Thread.sleep(WAIT_FOR_LOCATION_STEP_MS);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    if (lastKnownLocaton != null && !isFinishing()) {
                        poiData = getPoiInformation(lastKnownLocaton, 20);
                        callJavaScript("World.loadPoisFromJsonData", new String[]{poiData.toString()});
                    }

                    isLoading = false;
                }
            });
            t.start();
        }
    }

    public static JSONArray getPoiInformation(final Location userLocation, final int numberOfPlaces) {

        if (userLocation == null) {
            return null;
        }

        final JSONArray pois = new JSONArray();

        final String ATTR_ID = "id";
        final String ATTR_NAME = "name";
        final String ATTR_LATITUDE = "latitude";
        final String ATTR_LONGITUDE = "longitude";
        final String ATTR_ALTITUDE = "altitude";
        final String ATTR_DISTANCE = "distance";

        for (int i = 1; i <= numberOfPlaces; i++) {

            double[] poiLocationLatLon = getRandomLatLonNearby(userLocation.getLatitude(), userLocation.getLongitude());
            double distance = getDistance(poiLocationLatLon[0], userLocation.getLatitude(), poiLocationLatLon[1], userLocation.getLongitude());
            BigDecimal decimalKm = new BigDecimal(distance / 1000);
            String distanceString = (distance > 999) ? (decimalKm.setScale(2, BigDecimal.ROUND_HALF_UP)  + " km") : (Math.round(distance) + " m");

            JSONObject singlePoiInfo = new JSONObject();
            try {
                singlePoiInfo.accumulate(ATTR_ID, String.valueOf(i));
                singlePoiInfo.accumulate(ATTR_NAME, "POI#" + i);
                singlePoiInfo.accumulate(ATTR_LATITUDE, poiLocationLatLon[0]);
                singlePoiInfo.accumulate(ATTR_LONGITUDE, poiLocationLatLon[1]);
                singlePoiInfo.accumulate(ATTR_ALTITUDE, UNKNOWN_ALTITUDE);
                singlePoiInfo.accumulate(ATTR_DISTANCE, distanceString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pois.put(singlePoiInfo);
        }

        return pois;
    }

    private static double[] getRandomLatLonNearby(final double lat, final double lon) {
        return new double[]{lat + Math.random() /5 - 0.1, lon + Math.random() / 5 - 0.1};
    }

    private void callJavaScript(final String methodName, final String[] arguments) {
        final StringBuilder argumentsString = new StringBuilder("");
        for (int i = 0; i < arguments.length; i++) {
            argumentsString.append(arguments[i]);
            if (i < arguments.length - 1) {
                argumentsString.append(", ");
            }
        }

        if (this.architectView != null) {
            final String js = (methodName + "( " + argumentsString.toString() + " );");
            this.architectView.callJavascript(js);
        }
    }

    public static final boolean isVideoDrawablesSupported() {
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        return extensions != null && extensions.contains("GL_OES_EGL_image_external");
    }

    public static double getDistance(double targetLatitude, double centerPointLatitude, double targetLongtitude, double centerPointLongitude) {
        double Δφ = (centerPointLatitude - targetLatitude) * Math.PI / 180;
        double Δλ = (centerPointLongitude - targetLongtitude) * Math.PI / 180;
        double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) + Math.cos(targetLatitude * Math.PI / 180) * Math.cos(centerPointLatitude * Math.PI / 180) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371e3 * c;
    }
}
