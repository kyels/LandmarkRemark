package com.test.landmark;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 *  Data structure for Remarks
 */
@IgnoreExtraProperties
public class RemarkData {

    public String userId;
    public String username;
    public String landmarkId;
    public String landmarkName;
    public String remark;

    public RemarkData() {
        // Default constructor required for calls to DataSnapshot.getValue(Remark.class)
    }

    public RemarkData(String userId, String username, String landmarkId, String landmarkName, String remark) {
        this.userId = userId;
        this.username = username;
        this.landmarkId = landmarkId;
        this.landmarkName = landmarkName;
        this.remark = remark;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("user_id", userId);
        result.put("username", username);
        result.put("landmark_id", landmarkId);
        result.put("landmark_name", landmarkName);
        result.put("remark", remark);

        return result;
    }

}
