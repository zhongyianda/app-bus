package com.qualcomm.snapdragon.sdk.recognition.sample;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.ObjectArraySerializer;
import com.qualcomm.snapdragon.sdk.face.FaceData;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by fengxiaoping on 5/30/16.
 */
public class Checkin {
    public static final String TAG = "Checkin";
    private final String type;
    private Date timestamp;
    private HashMap<String, FaceData> content;
    private String installationId;

    // For face checkin
    public Checkin(String installationId, Date timestamp, String type, HashMap<String, FaceData> content) {
        this.installationId = installationId;
        this.timestamp = timestamp;
        this.type = type;
        this.content = content;
    }

    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("installationId", installationId);
        params.put("type", type);
        HashMap<String, Object> c = new HashMap<>();
        c.put("timestamp", timestamp.getTime());
        params.put("content", c);
        if ("face".equals(type)) {
            HashMap<String, Object> people = new HashMap<>();
            for (String key : content.keySet()) {
                HashMap<String, Object> faceData = new HashMap<>();
                faceData.put("confidence", content.get(key).getRecognitionConfidence() / (double) 100);
                people.put(key, faceData);
            }
            c.put("people", people);
        }
        return params;
    }

    public void print() {
        Log.i(TAG, timestamp + "," + type + "," + content);
    }
}
