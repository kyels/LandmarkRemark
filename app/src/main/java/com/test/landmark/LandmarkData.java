package com.test.landmark;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 *  Data structure for Remarks
 */
@IgnoreExtraProperties
public class LandmarkData {

    public double latitude;
    public double longitude;
    public String landmarkName;
    public String lastRemark;

    public LandmarkData() {
        // Default constructor required for calls to DataSnapshot.getValue(LandmarkData.class)
    }

    public LandmarkData(double latitude, double longitude, String landmarkName, String lastRemark) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.landmarkName = landmarkName;
        this.lastRemark = lastRemark;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("lat", latitude);
        result.put("lng", longitude);
        result.put("landmark_name", landmarkName);
        result.put("last_remark", lastRemark);

        return result;
    }

    @Exclude
    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }
}
