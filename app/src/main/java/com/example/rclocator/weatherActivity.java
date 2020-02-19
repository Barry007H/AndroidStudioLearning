package com.example.rclocator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class weatherActivity extends AppCompatActivity {

    private static final String TAG = "weatherActivity";
    private TextView textViewResult;
    private double lat;
    private double lon;
    private String Api_key;
    private GetWeatherTask getWeatherTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        textViewResult = findViewById(R.id.text_view);
        Api_key = "098d47b375ae8d40e3b8e7b2b0fad6f8";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lat = extras.getDouble("Lat");
            lon = extras.getDouble("Lon");
        }
        String units = "metric";
        String url = String.format(
                "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + Api_key,
                lat, lon, units, Api_key);
        getWeatherTask = new GetWeatherTask();
        getWeatherTask.execute(url);
    }
    private class WeatherDetails {
        String temperature = "";
    }
    private class GetWeatherTask extends AsyncTask<String, Void, WeatherDetails> {
        @Override
        protected WeatherDetails doInBackground(String... strings) {
            WeatherDetails wd = new WeatherDetails();
            try {
                URL url = new URL(strings[0]);
                URLConnection urlConnection = url.openConnection();
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                Log.d(TAG, "doInBackground: HTTP connection" + httpURLConnection);
                InputStream stream = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String inputString;
                while ((inputString = bufferedReader.readLine()) != null) {
                    builder.append(inputString);
                }

                JSONObject topLevel = new JSONObject(builder.toString());
                JSONObject main = topLevel.getJSONObject("main");
                wd.temperature = String.valueOf(main.getDouble("temp"));

                httpURLConnection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return wd;
        }
        @Override
        protected void onPostExecute(WeatherDetails wd) {
            textViewResult.setText("Current Temperature :" + wd.temperature);
        }
    }
}
