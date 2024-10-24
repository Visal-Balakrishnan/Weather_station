package com.example.weather.model;

import java.util.List;

public class ThingSpeakResponse {
    private Channel channel;
    private List<Feed> feeds;

    public Channel getChannel() {
        return channel;
    }

    public List<Feed> getFeeds() {
        return feeds;
    }

    public static class Channel {
        private String name;

        public String getName() {
            return name;
        }
    }

    public static class Feed {
        private String field1; // Temperature
        private String field2; // Humidity
        private String field3; // Rain value

        public String getField1() {
            return field1;
        }

        public String getField2() {
            return field2;
        }

        public String getField3() {
            return field3;
        }
    }
}
