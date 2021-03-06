/*
 * =========================================================================
 * Copyright (c) 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * =========================================================================
 * @file FacialRecognitionActivity.java
 */

package com.qualcomm.snapdragon.sdk.recognition.sample;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.SaveCallback;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FEATURE_LIST;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FP_MODES;

import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;

public class FacialRecognitionActivity extends Activity {

    private GridView gridView;
    public static FacialProcessing faceObj;
    public final String TAG = "FacialRecognitionActivity";
    public final int confidence_value = 58;
    public static boolean activityStartedOnce = false;
    private HashMap<String, String> hash = new HashMap<>();

    public static List<People> users = new ArrayList<People>();

    public static FacialRecognitionActivity instance = null;

    List<AVObject> peopleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facial_recognition);

        instance = this;

        AVOSCloud.initialize(this, "p6974wyAF311qTN0tvxrw8oT-gzGzoHsz", "VEi5qSwbn4Hh4Q8pauISVFyD");
//        AVOSCloud.setDebugLogEnabled(true);
        AVObject.registerSubclass(People.class);

        if (!activityStartedOnce) // Check to make sure FacialProcessing object
        // is not created multiple times.
        {
            AVQuery<AVObject> query = new AVQuery<>("People");
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> list, AVException e) {
                    peopleList = list;
                }
            });
            hash = retrieveHash(getApplicationContext()); // Retrieve the previously

            activityStartedOnce = true;
            // Check if Facial Recognition feature is supported in the device
            boolean isSupported = FacialProcessing
                    .isFeatureSupported(FEATURE_LIST.FEATURE_FACIAL_RECOGNITION);
            if (isSupported) {
                Log.d(TAG, "Feature Facial Recognition is supported");
                faceObj = FacialProcessing.getInstance();
//                loadAlbum(); // De-serialize a previously stored album.
                syncFromCloud();
                if (faceObj != null) {
                    faceObj.setRecognitionConfidence(confidence_value);
                    faceObj.setProcessingMode(FP_MODES.FP_MODE_STILL);
                }
            } else // If Facial recognition feature is not supported then
            // display an alert box.
            {
                Log.i(TAG, "Feature Facial Recognition is NOT supported");
                new AlertDialog.Builder(this)
                        .setMessage(
                                "Your device does NOT support Qualcomm's Facial Recognition feature. ")
                        .setCancelable(false)
                        .setNegativeButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        FacialRecognitionActivity.this.finish();
                                    }
                                }).show();
            }

            (AVObject.getQuery(People.class)).findInBackground(new FindCallback<People>() {
                @Override
                public void done(List<People> list, AVException e) {
                    if (e == null) {
                        users = list;
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "用户获取失败,请重启重试",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        // Vibrator for button press
        final Vibrator vibrate = (Vibrator) FacialRecognitionActivity.this
                .getSystemService(Context.VIBRATOR_SERVICE);

        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapter(this));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
//                vibrate.vibrate(85);
                switch (position) {

                    case 0: // Adding a person
                        addNewPerson();
                        break;

//                    case 1: // Updating an existing person
//                        updateExistingPerson();
//                        break;
//
//                    case 2: // Identifying a person.
//                        identifyPerson();
//                        break;

                    case 1: // Live Recognition
                        liveRecognition();
                        break;

                    case 2: // Reseting an album
                        resetAlbum();
                        break;

//                    case 5: // Delete Existing Person
//                        deletePerson();
//                        break;

                    case 3: // Delete Existing Person
                        syncFromCloud();
                        break;

//                    case 0: // Adding a person
//                        addNewPerson();
//                        break;
//
//                    case 1: // Updating an existing person
//                        updateExistingPerson();
//                        break;
//
//                    case 2: // Identifying a person.
//                        identifyPerson();
//                        break;
//
//                    case 3: // Live Recognition
//                        liveRecognition();
//                        break;
//
//                    case 4: // Reseting an album
//                        resetAlbum();
//                        break;
//
//                    case 5: // Delete Existing Person
//                        deletePerson();
//                        break;
//
//                    case 6: // Delete Existing Person
//                        syncFromCloud();
//                        break;
                }
            }
        });
    }

    public void syncFromCloud() {
        AVQuery<AVObject> avQuery = new AVQuery<>("FaceRecog");
        avQuery.getFirstInBackground(new GetCallback<AVObject>() {
            @Override
            public void done(final AVObject avObject, AVException e) {
                AVFile dataFile = avObject.getAVFile("data");
                if (dataFile == null) {
                    return;
                }
                dataFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] albumArray, AVException e) {
                        try {
                            hash = (HashMap) JsonHelper.toMap(avObject.getJSONObject("map"));
                            saveHash(hash, instance);
                            faceObj.deserializeRecognitionAlbum(albumArray);
                            Toast.makeText(getApplicationContext(),
                                    "Album Synchronize Success.",
                                    Toast.LENGTH_LONG).show();
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /*
     * Method to handle adding a new person to the recognition album
     */
    private void addNewPerson() {
        Intent intent = new Intent(this, AddPhoto.class);
        intent.putExtra("Username", "null");
        intent.putExtra("PersonId", -1);
        intent.putExtra("UpdatePerson", false);
        intent.putExtra("IdentifyPerson", false);
        startActivity(intent);
    }

    /*
     * Method to handle updating of an existing person from the recognition
     * album
     */
    private void updateExistingPerson() {
        Intent intent = new Intent(this, ChooseUser.class);
        intent.putExtra("DeleteUser", false);
        intent.putExtra("UpdateUser", true);
        startActivity(intent);
    }

    /*
     * Method to handle identification of an existing person from the
     * recognition album
     */
    private void identifyPerson() {
        Intent intent = new Intent(this, AddPhoto.class);
        intent.putExtra("Username", "Not Identified");
        intent.putExtra("PersonId", -1);
        intent.putExtra("UpdatePerson", false);
        intent.putExtra("IdentifyPerson", true);
        startActivity(intent);
    }

    /*
     * Method to handle deletion of an existing person from the recognition
     * album
     */
    private void deletePerson() {
        Intent intent = new Intent(this, ChooseUser.class);
        intent.putExtra("DeleteUser", true);
        intent.putExtra("UpdateUser", false);
        startActivity(intent);
    }

    /*
     * Method to handle live identification of the people
     */
    private void liveRecognition() {
        Intent intent = new Intent(this, LiveRecognition.class);
        startActivity(intent);
    }

    /*
     * Method to handle reseting of the recognition album
     */
    private void resetAlbum() {
        // Alert box to confirm before reseting the album
        new AlertDialog.Builder(this)
                .setMessage(
                        "Are you sure you want to RESET the album? All the photos saved will be LOST")
                .setCancelable(true)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                boolean result = faceObj.resetAlbum();
                                if (result) {
//                                    HashMap<String, String> hashMap = retrieveHash(getApplicationContext());
                                    HashMap<String, String> hashMap = hash;
                                    hashMap.clear();
                                    saveHash(hashMap, getApplicationContext());
                                    saveAlbum();
                                    Toast.makeText(getApplicationContext(),
                                            "Album Reset Successful.",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Internal Error. Reset album failed",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.facial_recognition, menu);
        return true;
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroyed");
        if (faceObj != null) // If FacialProcessing object is not released, then
        // release it and set it to null
        {
            faceObj.release();
            faceObj = null;
            Log.d(TAG, "Face Recog Obj released");
        } else {
            Log.d(TAG, "In Destroy - Face Recog Obj = NULL");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onResume() {
        Log.i("Hash", String.valueOf(retrieveHash(this)));
        super.onResume();
    }

    @Override
    public void onBackPressed() { // Destroy the activity to avoid stacking of
        // android activities
        super.onBackPressed();
        FacialRecognitionActivity.this.finishAffinity();
        activityStartedOnce = false;
    }

    /*
     * Function to retrieve a HashMap from the Shared preferences.
     * @return
     */
    protected HashMap<String, String> retrieveHash(Context context) {
        return hash;
    }

    /*
     * Function to store a HashMap to shared preferences.
     * @param hash
     */
    protected void saveHash(HashMap<String, String> hashMap, Context context) {
        hash = hashMap;
    }

    /*
     * Method to save the recognition album to a permanent device memory
     */
    public void saveAlbum() {
        final byte[] albumBuffer = faceObj.serializeRecogntionAlbum();

        Log.i(TAG, "save album," + hash);
        AVQuery<AVObject> avQuery = new AVQuery<>("FaceRecog");
        avQuery.getFirstInBackground(new GetCallback<AVObject>() {
            @Override
            public void done(AVObject testObject, AVException e) {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    AVFile file = new AVFile("facereg.data", albumBuffer);
                    testObject.put("data", file);
                    testObject.put("map", (hash != null) ? hash : new HashMap<String, String>());
                    Log.i(TAG, "MAP:" + testObject.get("map"));
                    testObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e != null) {
                                e.printStackTrace();
                            } else {
                                Log.i(TAG, "DONE ALL");
                            }
                        }
                    });
                }
            }
        });

    }

}
