/*
 * =========================================================================
 * Copyright (c) 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * =========================================================================
 * @file: DrawView.java
 */

package com.qualcomm.snapdragon.sdk.recognition.sample;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.qualcomm.snapdragon.sdk.face.FaceData;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceView;

public class DrawView extends SurfaceView {

    private Paint paintForTextBackground = new Paint(); // Draw the black background
    // behind the text
    private Paint paintForText = new Paint(); // Draw the text
    private FaceData[] mFaceArray;
    private boolean _inFrame; // Boolean to see if there is any faces in the frame
    private HashMap<String, String> hash;

    public DrawView(Context context, FaceData[] faceArray, boolean inFrame) {
        super(context);
        setWillNotDraw(false); // This call is necessary, or else the draw
        // method will not be called.
        mFaceArray = faceArray;
        _inFrame = inFrame;
        hash = FacialRecognitionActivity.instance.retrieveHash(getContext());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (_inFrame) // If the face detected is in frame.
        {
            Log.i("FacialRecognitionActivity", "face found," + mFaceArray.length);
            for (int i = 0; i < mFaceArray.length; i++) {
                String selectedPersonId = Integer.toString(mFaceArray[i]
                        .getPersonId());
                String personName = null;
                Iterator<HashMap.Entry<String, String>> iter = hash.entrySet()
                        .iterator();
                Log.i("FacialRecognitionActivity", "onDraw :" + String.valueOf(hash));
                while (iter.hasNext()) {
                    HashMap.Entry<String, String> entry = iter.next();
                    if (entry.getValue().equals(selectedPersonId)) {
                        personName = entry.getKey();
                    }
                }
                Log.i("FacialRecognitionActivity", "personName:" + personName);
                Rect rect = mFaceArray[i].rect;
                float pixelDensity = getResources().getDisplayMetrics().density;
                int textSize = (int) (rect.width() / 25 * pixelDensity);

                paintForText.setColor(Color.WHITE);
                paintForText.setTextSize(textSize);
                Typeface tp = Typeface.SERIF;
                Rect backgroundRect = new Rect(rect.left, rect.bottom,
                        rect.right, (rect.bottom + textSize));

                paintForTextBackground.setStyle(Paint.Style.FILL);
                paintForTextBackground.setColor(Color.BLACK);
                paintForText.setTypeface(tp);
                paintForTextBackground.setAlpha(80);
                if (personName != null) {
                    canvas.drawRect(backgroundRect, paintForTextBackground);
                    canvas.drawText(personName, rect.left, rect.bottom
                            + (textSize), paintForText);
                }
//                JSONObject checkinContent = new JSONObject();
//                checkinContent.put("timestamp", now.getTime());
//                checkins.put(personName, new Checkin("face", personName));
            }

        } else {
            canvas.drawColor(0, Mode.CLEAR);
        }
    }

}
