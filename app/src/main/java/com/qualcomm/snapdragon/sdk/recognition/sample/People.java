package com.qualcomm.snapdragon.sdk.recognition.sample;


import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;

@AVClassName("People")
public class People extends AVObject {

    @Override
    public String toString() {
//        return super.toString();
        return getString("name");
    }


    public String getId() {
        return getString("objectId");
    }
}

