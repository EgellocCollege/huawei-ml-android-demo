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

package com.huawei.mlkit.example.body.face3d;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.common.MLPosition;
import com.huawei.hms.mlsdk.face.MLFace;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer;
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting;
import com.huawei.hms.mlsdk.face.face3d.ML3DFace;
import com.huawei.hms.mlsdk.face.face3d.ML3DFaceAnalyzer;
import com.huawei.hms.mlsdk.face.face3d.ML3DFaceAnalyzerSetting;
import com.huawei.mlkit.example.R;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Detects face information in image.
 *
 * @since  2020-12-16
 */
public class Still3DFaceAnalyseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = Still3DFaceAnalyseActivity.class.getSimpleName();

    private TextView mTextView;

    private ML3DFaceAnalyzer analyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_image_face_analyse);
        this.mTextView = this.findViewById(R.id.result);
        this.findViewById(R.id.face_detect).setOnClickListener(this);
    }

    private void analyzer() {
        // Create a face analyzer. You can create an analyzer using the provided customized face detection parameter
        ML3DFaceAnalyzerSetting setting = new ML3DFaceAnalyzerSetting.Factory()
                // Fast detection of continuous video frames.
                // MLFaceAnalyzerSetting.TYPE_PRECISION: indicating the precision preference mode.
                // This mode will detect more faces and be more precise in detecting key points and contours, but will run slower.
                // MLFaceAnalyzerSetting.TYPE_SPEED: representing a preference for speed.
                // This will detect fewer faces and be less precise in detecting key points and contours, but will run faster.
                .setPerformanceType(MLFaceAnalyzerSetting.TYPE_PRECISION)
                .setTracingAllowed(false)
                .create();
        this.analyzer = MLAnalyzerFactory.getInstance().get3DFaceAnalyzer(setting);
        // Create an MLFrame by using the bitmap. Recommended image size: large than 320*320, less than 1920*1920.
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.face_image);
        MLFrame frame = MLFrame.fromBitmap(bitmap);
        // Call the asyncAnalyseFrame method to perform face detection
        Task<List<ML3DFace>> task = this.analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new OnSuccessListener<List<ML3DFace>>() {
            @Override
            public void onSuccess(List<ML3DFace> faces) {
                // Detection success.
                if (faces.size() > 0) {
                    Still3DFaceAnalyseActivity.this.displaySuccess(faces.get(0));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // Detection failure.
                Still3DFaceAnalyseActivity.this.displayFailure();
            }
        });
    }

    private void displayFailure() {
        this.mTextView.setText("Failure");
    }

    private void displaySuccess(ML3DFace mLFace) {
        float[] projectionMatrix = new float[ 4 * 4];
        float[] viewMatrix = new float[ 4 * 4];
        mLFace.get3DProjectionMatrix(projectionMatrix, 1, 10);
        mLFace.get3DViewMatrix(viewMatrix);
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String result =  "3DFaceEulerX: " + decimalFormat.format(mLFace.get3DFaceEulerX());
        result += "\n3DFaceEulerY: " + decimalFormat.format(mLFace.get3DFaceEulerY());
        result += "\n3DFaceEulerZ: " + decimalFormat.format(mLFace.get3DFaceEulerZ());
        result += "\n3DProjectionMatrix:";
        for (int i = 0; i < 16; i++) {
            result += " " + decimalFormat.format(projectionMatrix[i]);
        }
        result += "\nViewMatrix:";
        for (int i = 0; i < 16; i++) {
            result += " " + decimalFormat.format(viewMatrix[i]);
        }

        this.mTextView.setText(result);
    }

    @Override
    public void onClick(View v) {
        this.analyzer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.analyzer == null) {
            return;
        }
        try {
            this.analyzer.stop();
        } catch (IOException e) {
            Log.e(Still3DFaceAnalyseActivity.TAG, "Stop failed: " + e.getMessage());
        }
    }
}
