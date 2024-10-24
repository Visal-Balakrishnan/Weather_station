// Feed.java
package com.example.weather.model; // Update the package name accordingly

public class Feed {
    private String created_at; // The date and time the entry was created
    private String field1; // Field 1 data (can be temperature, humidity, etc.)
    private String field2; // Field 2 data (can be another measurement)
    private String field3; // Field 3 data (if needed)

    // Getters and Setters
    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }
}
