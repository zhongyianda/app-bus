/*
 * =========================================================================
 * Copyright (c) 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * =========================================================================
 * @file LiveRecognition.java
 */

package com.qualcomm.snapdragon.sdk.recognition.sample;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.FunctionCallback;
import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FP_MODES;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.PREVIEW_ROTATION_ANGLE;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class LiveRecognition extends Activity implements Camera.PreviewCallback {

    Camera cameraObj; // Accessing the Android native Camera.
    FrameLayout preview;
    CameraSurfacePreview mPreview;
    private int FRONT_CAMERA_INDEX = 1;
    private int BACK_CAMERA_INDEX = 0;
    private OrientationEventListener orientationListener;
    private FacialProcessing faceObj;
    private int frameWidth;
    private int frameHeight;
    private boolean cameraFacingFront = true;
    private static PREVIEW_ROTATION_ANGLE rotationAngle = PREVIEW_ROTATION_ANGLE.ROT_90;
    private DrawView drawView;
    private FaceData[] faceArray; // Array in which all the face data values will be returned for each face detected.
    private ImageView switchCameraButton;
    private Vibrator vibrate;
    HashMap<String, String> hash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_recognition);

        faceObj = FacialRecognitionActivity.faceObj;
        switchCameraButton = (ImageView) findViewById(R.id.camera_facing);
        vibrate = (Vibrator) LiveRecognition.this
                .getSystemService(Context.VIBRATOR_SERVICE);

        hash = FacialRecognitionActivity.instance.retrieveHash(this);

        orientationListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {

            }
        };

        switchCameraButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                vibrate.vibrate(80);
                if (cameraFacingFront) {
                    switchCameraButton
                            .setImageResource(R.drawable.camera_facing_back);
                    cameraFacingFront = false;
                } else {
                    switchCameraButton
                            .setImageResource(R.drawable.camera_facing_front);
                    cameraFacingFront = true;
                }
                stopCamera();
                startCamera();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.live_recognition, menu);
        return true;
    }

    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onResume() {
        super.onResume();
        if (cameraObj != null) {
            stopCamera();
        }
        startCamera();
    }

    /*
     * Stops the camera preview. Releases the camera. Make the objects null.
     */
    private void stopCamera() {

        if (cameraObj != null) {
            cameraObj.stopPreview();
            cameraObj.setPreviewCallback(null);
            preview.removeView(mPreview);
            cameraObj.release();
        }
        cameraObj = null;
    }

    /*
     * Method that handles initialization and starting of camera.
     */
    private void startCamera() {
        if (cameraFacingFront) {
            cameraObj = Camera.open(FRONT_CAMERA_INDEX); // Open the Front camera
        } else {
            cameraObj = Camera.open(BACK_CAMERA_INDEX); // Open the back camera
        }
        mPreview = new CameraSurfacePreview(LiveRecognition.this, cameraObj,
                orientationListener); // Create a new surface on which Camera will be displayed.
        preview = (FrameLayout) findViewById(R.id.cameraPreview2);
        preview.addView(mPreview);
        cameraObj.setPreviewCallback(LiveRecognition.this);
        frameWidth = cameraObj.getParameters().getPreviewSize().width;
        frameHeight = cameraObj.getParameters().getPreviewSize().height;
    }

    public static Date lastCheckedInAt = new Date();
    //    public static HashMap<String, Checkin> checkins = new HashMap<String, Checkin>();
    public static Checkin checkin = null;
    public static HashMap<String, FaceData> people = new HashMap<>();
    public static int CHECKIN_INTERVAL = 3000;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Date now = new Date();

        boolean result = false;
        faceObj.setProcessingMode(FP_MODES.FP_MODE_VIDEO);
        if (cameraFacingFront) {
            result = faceObj.setFrame(data, frameWidth, frameHeight, true,
                    rotationAngle);
        } else {
            result = faceObj.setFrame(data, frameWidth, frameHeight, false,
                    rotationAngle);
        }
        if (result) {
            int numFaces = faceObj.getNumFaces();
            if (numFaces == 0) {
                Log.d("TAG", "No Face Detected");
                if (drawView != null) {
                    preview.removeView(drawView);
                    drawView = new DrawView(this, null, false);
                    preview.addView(drawView);
                }
            } else {
                faceArray = faceObj.getFaceData();
                Log.d("TAG", "Face Detected");
                if (faceArray == null) {
                    Log.e("TAG", "Face array is null");
                } else {
                    int surfaceWidth = mPreview.getWidth();
                    int surfaceHeight = mPreview.getHeight();
                    faceObj.normalizeCoordinates(surfaceWidth, surfaceHeight);
                    preview.removeView(drawView); // Remove the previously created view to avoid unnecessary stacking of
                    // Views.
                    for (FaceData face : faceArray) {
                        for (String key : hash.keySet()) {
                            if (hash.get(key).equals(Integer.toString(face.getPersonId()))) {
//                                people.put(key, face.getRecognitionConfidence() / (double) 100);
                                people.put(key, face);
                            }
                        }
                    }
                    drawView = new DrawView(this, faceArray, true);
                    preview.addView(drawView);
                }
            }
        }
        if (now.getTime() - lastCheckedInAt.getTime() > CHECKIN_INTERVAL) {
            // Do checkin
            if (people.size() > 0) {
                checkin = new Checkin(
                        AVInstallation.getCurrentInstallation().getInstallationId(),
                        now,
                        "face",
                        people);
                people = new HashMap<>();
                checkin.print();
                lastCheckedInAt = now;
                AVCloud.callFunctionInBackground("checkin", checkin.toHashMap(), new FunctionCallback<Object>() {
                    @Override
                    public void done(Object o, AVException e) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

}
