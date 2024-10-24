package com.example.weather;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weather.model.ThingSpeakResponse;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class ThingSpeakApiClient {
    private static final String BASE_URL = "https://api.thingspeak.com/channels/2669221/feeds.json?api_key=C1YLAI0JEEXVOLQO&results=2"; // Replace with your channel ID and API key
    private final RequestQueue requestQueue;

    public ThingSpeakApiClient(MainActivity mainActivity) {
        requestQueue = Volley.newRequestQueue(mainActivity);
    }

    public void fetchWeatherData(final WeatherDataCallback callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("ThingSpeakApiClient", "Response JSON: " + response.toString()); // Log the full response
                        try {
                            ThingSpeakResponse thingSpeakResponse = new Gson().fromJson(response.toString(), ThingSpeakResponse.class);
                            callback.onSuccess(thingSpeakResponse);
                        } catch (Exception e) {
                            Log.e("ThingSpeakApiClient", "Parsing error: " + e.getMessage());
                            callback.onError(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ThingSpeakApiClient", "Error fetching data: " + error.getMessage());
                        callback.onError(error);
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    public interface WeatherDataCallback {
        void onSuccess(ThingSpeakResponse response);
        void onError(Exception error);
    }
}
