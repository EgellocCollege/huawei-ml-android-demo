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

package com.huawei.mlkit.example.body.livenessdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.hms.mlsdk.livenessdetection.MLLivenessCaptureConfig;
import com.huawei.hms.mlsdk.livenessdetection.ui.LivenessDetectActivity;
import com.huawei.mlkit.example.R;
import com.huawei.hms.mlsdk.livenessdetection.MLLivenessCapture;
import com.huawei.hms.mlsdk.livenessdetection.MLLivenessCaptureResult;

import static com.huawei.hms.mlsdk.livenessdetection.MLLivenessDetectView.DETECT_MASK;

public class LiveLivenessDetectionActivity extends AppCompatActivity {
    private static final String TAG = LiveLivenessDetectionActivity.class.getSimpleName();

    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    private static final int RC_CAMERA_AND_EXTERNAL_STORAGE_DEFAULT = 0x01 << 8;
    private static final int RC_CAMERA_AND_EXTERNAL_STORAGE_CUSTOM = 0x01 << 9;
    private static final int START_CUSTOM_ACTIVITY = 1001;

    private Button defaultBtn;
    private Button customBtn;
    @SuppressLint("StaticFieldLeak")
    private static TextView mTextResult;
    @SuppressLint("StaticFieldLeak")
    private static ImageView mImageResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_liveness_detection);

        defaultBtn = findViewById(R.id.capture_btn);
        customBtn = findViewById(R.id.custom_btn);
        mTextResult = findViewById(R.id.text_detect_result);
        mImageResult = findViewById(R.id.img_detect_result);

        defaultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(LiveLivenessDetectionActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    startCaptureActivity();
                    return;
                }
                ActivityCompat.requestPermissions(LiveLivenessDetectionActivity.this, PERMISSIONS, RC_CAMERA_AND_EXTERNAL_STORAGE_DEFAULT);
            }
        });
        customBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(LiveLivenessDetectionActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    startCustomActivity();
                    return;
                }
                ActivityCompat.requestPermissions(LiveLivenessDetectionActivity.this, PERMISSIONS, RC_CAMERA_AND_EXTERNAL_STORAGE_CUSTOM);
            }
        });
    }

    //Callback for receiving the liveness detection result.
    private static MLLivenessCapture.Callback callback = new MLLivenessCapture.Callback() {
        /**
         * Liveness detection success callback.
         * @param result result
         */
        @Override
        public void onSuccess(MLLivenessCaptureResult result) {
            mTextResult.setText(result.toString());
            mTextResult.setBackgroundResource(result.isLive() ? R.drawable.bg_blue : R.drawable.bg_red);
            mImageResult.setImageBitmap(result.getBitmap());
        }

        @Override
        public void onFailure(int errorCode) {
            mTextResult.setText("errorCode:" + errorCode);
        }
    };

    public static final MLLivenessCapture.Callback customCallback = new MLLivenessCapture.Callback() {
        /**
         * Liveness detection success callback.
         * @param result result
         */
        @Override
        public void onSuccess(MLLivenessCaptureResult result) {
            mTextResult.setText(result.toString());
            mTextResult.setBackgroundResource(result.isLive() ? R.drawable.bg_blue : R.drawable.bg_red);
            mImageResult.setImageBitmap(result.getBitmap());
        }

        @Override
        public void onFailure(int errorCode) {
            mTextResult.setText("errorCode:" + errorCode);
        }
    };

    private void startCaptureActivity() {
        //Obtain liveness detection config and set detect mask and sunglasses
        MLLivenessCaptureConfig captureConfig = new MLLivenessCaptureConfig.Builder().setOptions(DETECT_MASK).build();
        // Obtains the liveness detection plug-in instance.
        MLLivenessCapture capture = MLLivenessCapture.getInstance();
        //set liveness detection config
        capture.setConfig(captureConfig);

        // Enable liveness detection.
        capture.startDetect(this, this.callback);
    }


    private void startCustomActivity() {
        Intent intent = new Intent(this, LivenessCustomDetectionActivity.class);
        this.startActivity(intent);
    }

    // Permission application callback.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult ");
        if (requestCode == RC_CAMERA_AND_EXTERNAL_STORAGE_DEFAULT && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCaptureActivity();
        }
        if (requestCode == RC_CAMERA_AND_EXTERNAL_STORAGE_CUSTOM && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCustomActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i(TAG, "onActivityResult requestCode " + requestCode + ", resultCode " + resultCode);
    }
}
