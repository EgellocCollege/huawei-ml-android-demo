/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.mlkit.example.body.handgesture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.gesture.MLGesture;
import com.huawei.hms.mlsdk.gesture.MLGestureAnalyzer;
import com.huawei.hms.mlsdk.gesture.MLGestureAnalyzerFactory;
import com.huawei.hms.mlsdk.gesture.MLGestureAnalyzerSetting;

import com.huawei.mlkit.example.R;
import com.huawei.mlkit.example.camera.GraphicOverlay;

import java.util.ArrayList;
import java.util.List;

public class StillHandGestureAnalyseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = StillHandGestureAnalyseActivity.class.getSimpleName();

    private GraphicOverlay mGraphicOverlay;

    private ImageView mPreviewView;

    private Button mDetectSync;

    private Button mDetectAsync;

    private MLFrame mlFrame;

    private MLGestureAnalyzer mAnalyzer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_hand_analyse);
        initView();
    }

    private void initView() {
        mPreviewView = findViewById(R.id.handstill_previewPane);
        mGraphicOverlay = findViewById(R.id.handstill_previewOverlay);
        mDetectSync = findViewById(R.id.handstill_detect_sync);
        mDetectSync.setOnClickListener(this);
        mDetectAsync = findViewById(R.id.handstill_detect_async);
        mDetectAsync.setOnClickListener(this);

        mPreviewView.setImageResource(R.drawable.gesture);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.handstill_detect_async:
                mGraphicOverlay.clear();
                createAnalyzer();
                // Asynchronous analyse.
                analyzerAsync();
                break;
            case R.id.handstill_detect_sync:
                mGraphicOverlay.clear();
                createAnalyzer();
                // Synchronous analyse.
                analyzerSync();
                break;
            default:
                break;
        }
    }

    private void createAnalyzer() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 16;
        // Create an MLFrame by using the bitmap.
        Bitmap originBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.gesture, options);

        // Gets the targeted width / height, only portrait.
        int maxHeight = ((View) mPreviewView.getParent()).getHeight();
        int targetWidth = ((View) mPreviewView.getParent()).getWidth();
        // Determine how much to scale down the image
        float scaleFactor = Math.max(
                (float) originBitmap.getWidth() / (float) targetWidth,
                (float) originBitmap.getHeight() / (float) maxHeight);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                originBitmap,
                (int) (originBitmap.getWidth() / scaleFactor),
                (int) (originBitmap.getHeight() / scaleFactor),
                true);

        mlFrame = new MLFrame.Creator().setBitmap(resizedBitmap).create();

        MLGestureAnalyzerSetting setting =
                new MLGestureAnalyzerSetting.Factory()
                        .create();

        this.mAnalyzer = MLGestureAnalyzerFactory.getInstance().getGestureAnalyzer(setting);
    }

    /**
     * Synchronous analyse.
     */
    private void analyzerSync() {
        List<MLGesture> mlGesturesList = new ArrayList<>();
        SparseArray<MLGesture> mlGestureSparseArray = mAnalyzer.analyseFrame(mlFrame);
        for (int i = 0; i < mlGestureSparseArray.size(); i++) {
            mlGesturesList.add(mlGestureSparseArray.get(i));
        }
            processSuccess(mlGesturesList);
    }

    /**
     * Asynchronous analyse.
     */
    private void analyzerAsync() {
        Task<List<MLGesture>> task = mAnalyzer.asyncAnalyseFrame(mlFrame);
        task.addOnSuccessListener(new OnSuccessListener<List<MLGesture>>() {
            @Override
            public void onSuccess(List<MLGesture> results) {
                // Detection success.
                if (results != null && !results.isEmpty()) {
                    processSuccess(results);
                } else {
                    processFailure("async analyzer result is null.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // Detection failure.
                processFailure(e.getMessage());
            }
        });
    }

    private void processFailure(String str) {
        Log.e(TAG, str);
    }

    private void processSuccess(List<MLGesture> results) {
        mGraphicOverlay.clear();
        HandGestureGraphic handGraphic = new HandGestureGraphic(mGraphicOverlay, results);
        mGraphicOverlay.add(handGraphic);
    }
}
