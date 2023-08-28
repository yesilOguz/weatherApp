package com.yesiloguz.weatherapp;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Map<String, String> recommends = new HashMap<String, String>();

    // You can get this key from
    // https://getpantry.cloud
    // signup and it will be give to you your api key
    final String key = "weatherapi-key";
    final String currentApiUrl = "https://api.weatherapi.com/v1/current.json?key=" + key + "&aqi=yes&q="; // paris // will add loc
    final String historyApiUrl = "https://api.weatherapi.com/v1/history.json?key=" + key + "&q="; // paris&dt=2023-08-22
    final String astronomyApiUrl = "https://api.weatherapi.com/v1/astronomy.json?key=" + key + "&q="; // &q=paris

    String loc = "Paris"; // if the user has not granted access to the gps, we will use this

    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;

    final String[] airQualityTexts = new String[]{"Good", "Moderate", "Unhealthy for sensitive group",
                                                "Unhealthy", "Very Unhealthy", "Hazardous"};

    TextView nameText, currentWeatherText, recommendText, currentCelciusText, currentFahrenheitText;
    TextView realCelciusText, realFahrenheitText, humidityText, cloudText, windMphText;
    TextView windKphText, windDegreeText, windDirectionText, precipitationMMText, precipitationInchText;
    TextView airQualityText, sunriseText, sunsetText, moonriseText, moonsetText;
    TextView weather_yesterday, weather_2day, weather_3day, maxC_yesterday, maxC_2day;
    TextView maxC_3day, maxF_yesterday, maxF_2day, maxF_3day, avgC_yesterday;
    TextView avgC_2day, avgC_3day, avgF_yesterday, avgF_2day, avgF_3day;
    TextView minC_yesterday, minC_2day, minC_3day, minF_yesterday, minF_2day;
    TextView minF_3day, avgHumidityText_yesterday, avgHumidityText_2day, avgHumidityText_3day;

    ImageView iconImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recommends.put("Sunny", "Wear sunglasses");
        recommends.put("Partly cloudy", "Wear a light jacket");
        recommends.put("Cloudy", "Wear a light jacket");
        recommends.put("Overcast", "Bring a raincoat");
        recommends.put("Mist", "Drive carefully");
        recommends.put("Patchy rain possible", "Bring an umbrella");
        recommends.put("Patchy snow possible", "Bring an umbrella");
        recommends.put("Patchy sleet possible", "Visibility may be reduced");
        recommends.put("Thundery outbreaks possible", "Unplug your electronic devices");
        recommends.put("Blowing snow", "Drive carefully");
        recommends.put("Blizzard", "Take shelter in a safe place near you");
        recommends.put("Fog", "Drive carefully");
        recommends.put("Freezing fog", "Drive carefully");
        recommends.put("Patchy light drizzle", "Wear a raincoat");
        recommends.put("Light drizzle", "Bring an umbrella");
        recommends.put("Freezing drizzle", "Turn on your fog lights");
        // And you can add more
        // You can look list
        // https://www.weatherapi.com/docs/weather_conditions.csv

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){}

        nameText = findViewById(R.id.nameText);

        iconImage = findViewById(R.id.icon);

        currentWeatherText = findViewById(R.id.weatherText);
        recommendText = findViewById(R.id.recommendText);

        currentCelciusText = findViewById(R.id.celsiusText);
        currentFahrenheitText = findViewById(R.id.fahrenheitText);

        realCelciusText = findViewById(R.id.realCelsiusText);
        realFahrenheitText = findViewById(R.id.realFahrenheitText);

        humidityText = findViewById(R.id.humidtyText);
        cloudText = findViewById(R.id.cloudText);

        windMphText = findViewById(R.id.mphText);
        windKphText = findViewById(R.id.kphText);

        windDegreeText = findViewById(R.id.degreeText);
        windDirectionText = findViewById(R.id.directionText);

        precipitationMMText = findViewById(R.id.precipitationMMText);
        precipitationInchText = findViewById(R.id.precipitationInchText);

        airQualityText = findViewById(R.id.airQualityText);

        sunriseText = findViewById(R.id.sunriseText);
        sunsetText = findViewById(R.id.sunsetText);

        moonriseText = findViewById(R.id.moonriseText);
        moonsetText = findViewById(R.id.moonsetText);

        weather_yesterday = findViewById(R.id.weather_yesterday);
        weather_2day = findViewById(R.id.weather_2day);
        weather_3day = findViewById(R.id.weather_3day);

        maxC_yesterday = findViewById(R.id.maxC_yesterday);
        maxC_2day = findViewById(R.id.maxC_2day);
        maxC_3day = findViewById(R.id.maxC_3day);

        maxF_yesterday = findViewById(R.id.maxF_yesterday);
        maxF_2day = findViewById(R.id.maxF_2day);
        maxF_3day = findViewById(R.id.maxF_3day);

        avgC_yesterday = findViewById(R.id.avgC_yesterday);
        avgC_2day = findViewById(R.id.avgC_2day);
        avgC_3day = findViewById(R.id.avgC_3day);

        avgF_yesterday = findViewById(R.id.avgF_yesterday);
        avgF_2day = findViewById(R.id.avgF_2day);
        avgF_3day = findViewById(R.id.avgF_3day);

        minC_yesterday = findViewById(R.id.minC_yesterday);
        minC_2day = findViewById(R.id.minC_2day);
        minC_3day = findViewById(R.id.minC_3day);

        minF_yesterday = findViewById(R.id.minF_yesterday);
        minF_2day = findViewById(R.id.minF_2day);
        minF_3day = findViewById(R.id.minF_3day);

        avgHumidityText_yesterday = findViewById(R.id.avgHumidity_yesterday);
        avgHumidityText_2day = findViewById(R.id.avgHumidity_2day);
        avgHumidityText_3day = findViewById(R.id.avgHumidity_3day);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // method to get the location
        getLastLocation();

    }

    void setTextes(){

        Calendar rightNow = Calendar.getInstance();

        int year = rightNow.get(Calendar.YEAR);
        int month = rightNow.get(Calendar.MONTH)+1;
        int day = rightNow.get(Calendar.DAY_OF_MONTH);

        int yesterday = day-1;
        int _2daysAgo = day-2;
        int _3daysAgo = day-3;

        String yesterdayDate = String.format("%d-%d-%d", year, month, yesterday);
        String _2daysDate = String.format("%d-%d-%d", year, month, _2daysAgo);
        String _3daysDate = String.format("%d-%d-%d", year, month, _3daysAgo);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL currentUrl = new URL(currentApiUrl + loc);

                    URL historyYesterdayUrl = new URL(historyApiUrl + loc + "&dt=" + yesterdayDate);
                    URL history2daysUrl = new URL(historyApiUrl + loc + "&dt=" + _2daysDate);
                    URL history3daysUrl = new URL(historyApiUrl + loc + "&dt=" + _3daysDate);

                    URL astronomyUrl = new URL(astronomyApiUrl + loc);

                    // connections
                    HttpURLConnection currentConn = (HttpURLConnection)currentUrl.openConnection();

                    HttpURLConnection yesterdayConn = (HttpURLConnection)historyYesterdayUrl.openConnection();
                    HttpURLConnection _2daysConn = (HttpURLConnection)history2daysUrl.openConnection();
                    HttpURLConnection _3daysConn = (HttpURLConnection)history3daysUrl.openConnection();

                    HttpURLConnection astronomyConn = (HttpURLConnection)astronomyUrl.openConnection();

                    // response codes
                    int responseCurrentCode = currentConn.getResponseCode();

                    int responseYesterdayCode = yesterdayConn.getResponseCode();
                    int response2daysCode = _2daysConn.getResponseCode();
                    int response3daysCode = _3daysConn.getResponseCode();

                    int responseAstronomyCode = astronomyConn.getResponseCode();

                    final int OK_CODE = HttpURLConnection.HTTP_OK;

                    if (responseCurrentCode ==  OK_CODE|| responseYesterdayCode == OK_CODE||
                        response2daysCode == OK_CODE || response3daysCode == OK_CODE ||
                        responseAstronomyCode == OK_CODE) {

                        HttpURLConnection[] connects = new HttpURLConnection[] {currentConn, yesterdayConn,
                                                                    _2daysConn, _3daysConn, astronomyConn};

                        // 0 : current
                        // 1 : yesterday
                        // 2 : 2 days ago
                        // 3 : 3 days ago
                        // 4 : astronomy
                        String[] connJsonStrings = new String[5];

                        int i = 0;
                        for (HttpURLConnection connection: connects) {

                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String inputLine;
                            StringBuilder response = new StringBuilder();

                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();

                            String responseBody = response.toString();

                            connJsonStrings[i] = responseBody;

                            i++;
                        }

                        currentConn.disconnect();
                        yesterdayConn.disconnect();
                        _2daysConn.disconnect();
                        _3daysConn.disconnect();
                        astronomyConn.disconnect();

                        JSONObject currentJson = new JSONObject(connJsonStrings[0]);

                        JSONObject yesterdayJson = new JSONObject(connJsonStrings[1]);
                        JSONObject _2daysJson = new JSONObject(connJsonStrings[2]);
                        JSONObject _3daysJson = new JSONObject(connJsonStrings[3]);

                        JSONObject astronomyJson = new JSONObject(connJsonStrings[4]);

                        // current datas

                        String city = currentJson.getJSONObject("location").getString("name");

                        String currentWeather = currentJson.getJSONObject("current").getJSONObject("condition").getString("text");
                        String iconUrl = currentJson.getJSONObject("current").getJSONObject("condition").getString("icon");

                        String tempC = currentJson.getJSONObject("current").getString("temp_c");
                        String tempF = currentJson.getJSONObject("current").getString("temp_f");

                        String realTempC = currentJson.getJSONObject("current").getString("feelslike_c");
                        String realTempF = currentJson.getJSONObject("current").getString("feelslike_f");

                        String humidty = currentJson.getJSONObject("current").getString("humidity");
                        String cloud = currentJson.getJSONObject("current").getString("cloud");

                        String windMph = currentJson.getJSONObject("current").getString("wind_mph");
                        String windKph = currentJson.getJSONObject("current").getString("wind_kph");
                        String windDegree = currentJson.getJSONObject("current").getString("wind_degree");
                        String windDir = currentJson.getJSONObject("current").getString("wind_dir");

                        String precipitationMM = currentJson.getJSONObject("current").getString("precip_mm");
                        String precipitationInch = currentJson.getJSONObject("current").getString("precip_in");

                        int airQualityIDX = currentJson.getJSONObject("current").getJSONObject("air_quality").getInt("us-epa-index");
                        String airQuality = airQualityTexts[airQualityIDX];

                        // astronomy datas

                        String sunrise = astronomyJson.getJSONObject("astronomy").getJSONObject("astro").getString("sunrise");
                        String sunset = astronomyJson.getJSONObject("astronomy").getJSONObject("astro").getString("sunset");

                        String moonrise = astronomyJson.getJSONObject("astronomy").getJSONObject("astro").getString("moonrise");
                        String moonset = astronomyJson.getJSONObject("astronomy").getJSONObject("astro").getString("moonset");

                        // yesterday datas
                        JSONObject yesterdayDayJson = yesterdayJson.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0).getJSONObject("day");

                        String yesterdayWeather = yesterdayDayJson.getJSONObject("condition").getString("text");

                        String maxTempC_yesterday = yesterdayDayJson.getString("maxtemp_c");
                        String maxTempF_yesterday = yesterdayDayJson.getString("maxtemp_f");

                        String avgTempC_yesterday = yesterdayDayJson.getString("avgtemp_c");
                        String avgTempF_yesterday = yesterdayDayJson.getString("avgtemp_f");

                        String minTempC_yesterday = yesterdayDayJson.getString("mintemp_c");
                        String minTempF_yesterday = yesterdayDayJson.getString("mintemp_f");

                        String avgHumidity_yesterday = yesterdayDayJson.getString("avghumidity");

                        // 2 days ago
                        JSONObject _2dayDayJson = _2daysJson.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0).getJSONObject("day");

                        String _2dayWeather = _2dayDayJson.getJSONObject("condition").getString("text");

                        String maxTempC_2day = _2dayDayJson.getString("maxtemp_c");
                        String maxTempF_2day = _2dayDayJson.getString("maxtemp_f");

                        String avgTempC_2day = _2dayDayJson.getString("avgtemp_c");
                        String avgTempF_2day = _2dayDayJson.getString("avgtemp_f");

                        String minTempC_2day = _2dayDayJson.getString("mintemp_c");
                        String minTempF_2day = _2dayDayJson.getString("mintemp_f");

                        String avgHumidity_2day = _2dayDayJson.getString("avghumidity");

                        // 3 days ago
                        JSONObject _3dayDayJson = _3daysJson.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0).getJSONObject("day");

                        String _3dayWeather = _3dayDayJson.getJSONObject("condition").getString("text");

                        String maxTempC_3day = _3dayDayJson.getString("maxtemp_c");
                        String maxTempF_3day = _3dayDayJson.getString("maxtemp_f");

                        String avgTempC_3day = _3dayDayJson.getString("avgtemp_c");
                        String avgTempF_3day = _3dayDayJson.getString("avgtemp_f");

                        String minTempC_3day = _3dayDayJson.getString("mintemp_c");
                        String minTempF_3day = _3dayDayJson.getString("mintemp_f");

                        String avgHumidity_3day = _3dayDayJson.getString("avghumidity");

                        URL iconURL = new URL("Https:" + iconUrl);
                        HttpURLConnection iconConn = (HttpURLConnection)iconURL.openConnection();

                        Bitmap icon = BitmapFactory.decodeStream(iconConn.getInputStream());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nameText.setText(city.toUpperCase());

                                iconImage.setImageBitmap(icon);

                                currentWeatherText.setText(currentWeather.toUpperCase());
                                recommendText.setText(recommends.get(currentWeather));

                                currentCelciusText.setText(tempC + " ");
                                currentFahrenheitText.setText(tempF + " ");

                                realCelciusText.setText(realTempC + " ");
                                realFahrenheitText.setText(realTempF + " ");

                                humidityText.setText(humidty + " ");
                                cloudText.setText(cloud + " ");

                                windMphText.setText(windMph + " ");
                                windKphText.setText(windKph + " ");

                                windDegreeText.setText(windDegree);
                                windDirectionText.setText(windDir);

                                precipitationMMText.setText(precipitationMM + " ");
                                precipitationInchText.setText(precipitationInch + " ");

                                airQualityText.setText(airQuality.toUpperCase());

                                sunriseText.setText(sunrise);
                                sunsetText.setText(sunset);

                                moonriseText.setText(moonrise);
                                moonsetText.setText(moonset);

                                weather_yesterday.setText(yesterdayWeather);
                                weather_2day.setText(_2dayWeather);
                                weather_3day.setText(_3dayWeather);

                                maxC_yesterday.setText(maxTempC_yesterday);
                                maxF_yesterday.setText(maxTempF_yesterday);

                                maxC_2day.setText(maxTempC_2day);
                                maxF_2day.setText(maxTempF_2day);

                                maxC_3day.setText(maxTempC_3day);
                                maxF_3day.setText(maxTempF_3day);

                                avgC_yesterday.setText(avgTempC_yesterday);
                                avgF_yesterday.setText(avgTempF_yesterday);

                                avgC_2day.setText(avgTempC_2day);
                                avgF_2day.setText(avgTempF_2day);

                                avgC_3day.setText(avgTempC_3day);
                                avgF_3day.setText(avgTempF_3day);

                                minC_yesterday.setText(minTempC_yesterday);
                                minF_yesterday.setText(minTempF_yesterday);

                                minC_2day.setText(minTempC_2day);
                                minF_2day.setText(minTempF_2day);

                                minC_3day.setText(minTempC_3day);
                                minF_3day.setText(minTempF_3day);

                                avgHumidityText_yesterday.setText(avgHumidity_yesterday);
                                avgHumidityText_2day.setText(avgHumidity_2day);
                                avgHumidityText_3day.setText(avgHumidity_3day);
                            }
                        });

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Weather information not available!!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
                catch (Exception e){ Log.d("HATA!", String.valueOf(e));}
            }
        }).start();

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if(checkPermissions()){
            if(isLocationEnabled()){

                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            loc = String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());

                            setTextes();
                        }
                    }
                });
            }
            else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        else {
            requestNewLocationData();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            loc = String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude());

            setTextes();
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }

}