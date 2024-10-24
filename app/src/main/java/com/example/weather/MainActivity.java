package com.example.weather;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.example.weather.model.ThingSpeakResponse;

public class MainActivity extends AppCompatActivity {

    private TextView locationTextView;
    private TextView temperatureTextView;
    private TextView humidityTextView;
    private TextView weatherConditionTextView;
    private ScrollView mainScrollView; // Declare the ScrollView
    private static final String TAG = "WeatherApp"; // Added TAG for logging
    private static final int FETCH_INTERVAL_MS = 30000; // 30 seconds
    private Handler handler;
    private Runnable fetchRunnable;
    private static final String CHANNEL_ID = "weather_alerts"; // Notification channel ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TextViews
        locationTextView = findViewById(R.id.location);
        temperatureTextView = findViewById(R.id.temperature);
        humidityTextView = findViewById(R.id.humidity); // Ensure this TextView exists in your XML layout
        weatherConditionTextView = findViewById(R.id.weather_condition);
        mainScrollView = findViewById(R.id.main); // Initialize the ScrollView

        // Create notification channel
        createNotificationChannel();

        // Initialize Handler and Runnable for fetching data
        handler = new Handler();
        fetchRunnable = new Runnable() {
            @Override
            public void run() {
                fetchWeatherData();
                handler.postDelayed(this, FETCH_INTERVAL_MS); // Re-run this runnable after the specified interval
            }
        };

        // Start fetching weather data
        handler.post(fetchRunnable);
    }

    private void fetchWeatherData() {
        ThingSpeakApiClient apiClient = new ThingSpeakApiClient(this);
        apiClient.fetchWeatherData(new ThingSpeakApiClient.WeatherDataCallback() {
            @Override
            public void onSuccess(ThingSpeakResponse response) {
                Log.d(TAG, "Response JSON: " + response.toString()); // Log the full response

                if (response.getFeeds() == null || response.getFeeds().isEmpty()) {
                    Log.e(TAG, "No feed data available");
                    return;
                }

                // Assuming feeds contain the latest data, update UI accordingly
                ThingSpeakResponse.Feed latestFeed = response.getFeeds().get(0);
                Log.d(TAG, "Fetched Temperature: " + latestFeed.getField1());
                Log.d(TAG, "Fetched Humidity: " + latestFeed.getField2());
                Log.d(TAG, "Fetched Rain Value: " + latestFeed.getField3());

                // Update TextViews
                temperatureTextView.setText(latestFeed.getField1() + "°C");
                humidityTextView.setText(latestFeed.getField2() + "%"); // Assuming field2 is humidity

                // Check for critical weather conditions
                checkCriticalConditions(latestFeed);
                updateBackground(latestFeed); // Update background based on weather condition
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error fetching weather data: " + error.getMessage());
            }
        });
    }

    private void checkCriticalConditions(ThingSpeakResponse.Feed latestFeed) {
        // Example threshold values
        float temperatureThreshold = 40; // Example threshold for temperature
        float humidityThreshold = 80; // Example threshold for humidity

        try {
            float temperature = Float.parseFloat(latestFeed.getField1());
            float humidity = Float.parseFloat(latestFeed.getField2());

            // Alert for high temperature
            if (temperature > temperatureThreshold) {
                sendNotification("High Temperature Alert", "Alert: High Temperature! (" + temperature + "°)");
                temperatureTextView.setTextColor(Color.RED); // Change color to indicate alert
            }

            // Alert for high humidity
            if (humidity > humidityThreshold) {
                sendNotification("High Humidity Alert", "Alert: High Humidity! (" + humidity + "%)");
                humidityTextView.setTextColor(Color.RED); // Change color to indicate alert
            }

        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException: " + e.getMessage());
        }
    }

    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.noti) // Replace with your notification icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Intent to open the app when notification is clicked
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Updated PendingIntent with FLAG_IMMUTABLE
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        builder.setContentIntent(pendingIntent);

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Weather Alerts";
            String description = "Channel for weather alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void updateBackground(ThingSpeakResponse.Feed latestFeed) {
        float rainValue;

        try {
            rainValue = Float.parseFloat(latestFeed.getField3());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing rain value: " + e.getMessage());
            rainValue = 0; // Default to 0 if parsing fails
        }

        // Update background based on rain value
        if (rainValue > 1040) {
            mainScrollView.setBackgroundResource(R.drawable.rainy_background); // Set a rainy background
        } else {
            float temperature;

            try {
                temperature = Float.parseFloat(latestFeed.getField1());
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing temperature: " + e.getMessage());
                temperature = 0; // Default to 0 if parsing fails
            }

            // Example temperature ranges to decide background
            if (temperature < 20) {
                mainScrollView.setBackgroundResource(R.drawable.cold_background); // Set a cold background
            } else if (temperature < 30) {
                mainScrollView.setBackgroundResource(R.drawable.hot_background); // Set a warm background
            } else {
                mainScrollView.setBackgroundResource(R.drawable.warm_background); // Set a hot background
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetchRunnable); // Stop fetching data when activity is destroyed
    }
}
