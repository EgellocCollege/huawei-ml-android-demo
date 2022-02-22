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

package com.huawei.mlkit.example.image.imagesuperresolution;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.imagesuperresolution.MLImageSuperResolutionAnalyzer;
import com.huawei.hms.mlsdk.imagesuperresolution.MLImageSuperResolutionAnalyzerFactory;
import com.huawei.hms.mlsdk.imagesuperresolution.MLImageSuperResolutionAnalyzerSetting;
import com.huawei.hms.mlsdk.imagesuperresolution.MLImageSuperResolutionResult;
import com.huawei.mlkit.example.R;

import androidx.appcompat.app.AppCompatActivity;

public class ImageSuperResolutionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ImageSuperResolutionActivity.class.getSimpleName();
    private MLImageSuperResolutionAnalyzer analyzer;
    private static final int INDEX_1X = 0;
    private static final int INDEX_3X = 1;
    private static final int INDEX_ORIGINAL = 2;
    private ImageView imageView;
    private Bitmap srcBitmap;
    private int selectItem = INDEX_1X;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_super_resolution);
        imageView = findViewById(R.id.image);
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.superresolution_image);
        findViewById(R.id.button_1x).setOnClickListener(this);
        findViewById(R.id.button_3x).setOnClickListener(this);
        findViewById(R.id.button_original).setOnClickListener(this);
        createAnalyzer(selectItem);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_1x) {
            detectImage(INDEX_1X);
        } else if (view.getId() == R.id.button_3x) {
            detectImage(INDEX_3X);
        } else if (view.getId() == R.id.button_original) {
            detectImage(INDEX_ORIGINAL);
        }
    }

    private void release() {
        if (analyzer == null) {
            return;
        }
        analyzer.stop();
    }

    private void detectImage(int type) {
        if (type == INDEX_ORIGINAL) {
            setImage(srcBitmap);
            return;
        }

        // The analyzer only supports a single instance.
        // If you want to switch to a different scale, you need to release the model and recreate it.
        if (type != selectItem) {
            release();
            createAnalyzer(type);
        }

        if (analyzer == null) {
            return;
        }
        selectItem = type;

        // Create an MLFrame by using the bitmap.
        MLFrame frame = MLFrame.fromBitmap(srcBitmap);
        Task<MLImageSuperResolutionResult> task = analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new OnSuccessListener<MLImageSuperResolutionResult>() {
            public void onSuccess(MLImageSuperResolutionResult result) {
                // Recognition success.
                setImage(result.getBitmap());
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                // Recognition failure.
                Log.e(TAG, e.getMessage());
                Toast.makeText(getApplicationContext(), "Failed：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setImage(final Bitmap bitmap) {
        ImageSuperResolutionActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    private void createAnalyzer(int type) {
        if (type == INDEX_1X) {
            // Method 1: use the default setting, that is, 1x image super resolution.
            analyzer = MLImageSuperResolutionAnalyzerFactory.getInstance().getImageSuperResolutionAnalyzer();
        } else {
            // Method 2: using the custom setting.
            MLImageSuperResolutionAnalyzerSetting setting = new MLImageSuperResolutionAnalyzerSetting.Factory()
                    // set the scale of image super resolution to 3x.
                    .setScale(MLImageSuperResolutionAnalyzerSetting.ISR_SCALE_3X)
                    .create();
            analyzer = MLImageSuperResolutionAnalyzerFactory.getInstance().getImageSuperResolutionAnalyzer(setting);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (srcBitmap != null) {
            srcBitmap.recycle();
        }
        release();
    }
}