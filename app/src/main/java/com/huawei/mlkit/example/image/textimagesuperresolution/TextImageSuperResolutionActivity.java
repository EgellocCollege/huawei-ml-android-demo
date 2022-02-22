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

package com.huawei.mlkit.example.image.textimagesuperresolution;

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
import com.huawei.hms.mlsdk.common.MLException;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.textimagesuperresolution.MLTextImageSuperResolution;
import com.huawei.hms.mlsdk.textimagesuperresolution.MLTextImageSuperResolutionAnalyzer;
import com.huawei.hms.mlsdk.textimagesuperresolution.MLTextImageSuperResolutionAnalyzerFactory;
import com.huawei.mlkit.example.R;

import androidx.appcompat.app.AppCompatActivity;

public class TextImageSuperResolutionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = TextImageSuperResolutionActivity.class.getSimpleName();
    private MLTextImageSuperResolutionAnalyzer analyzer;
    private static final int INDEX_3X = 1;
    private static final int INDEX_ORIGINAL = 2;
    private ImageView imageView;
    private Bitmap srcBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_super_resolution);
        imageView = findViewById(R.id.image);
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tisr_image);
        findViewById(R.id.button_3x).setOnClickListener(this);
        findViewById(R.id.button_original).setOnClickListener(this);
        createAnalyzer();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_3x) {
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

        if (analyzer == null) {
            return;
        }

        // Create an MLFrame by using the bitmap.
        MLFrame frame = new MLFrame.Creator().setBitmap(srcBitmap).create();
        Task<MLTextImageSuperResolution> task = analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new OnSuccessListener<MLTextImageSuperResolution>() {
            public void onSuccess(MLTextImageSuperResolution result) {
                // success.
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                setImage(result.getBitmap());
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                // failure.
                if (e instanceof MLException) {
                    MLException mlException = (MLException) e;
                    // Get the error code, developers can give different page prompts according to the error code.
                    int errorCode = mlException.getErrCode();
                    // Get the error message, developers can combine the error code to quickly locate the problem.
                    String errorMessage = mlException.getMessage();
                    Toast.makeText(getApplicationContext(), "Error：" + errorCode + " Message:" + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error：" + errorCode + " Message:" + errorMessage);
                } else {
                    // Other exception。
                    Toast.makeText(getApplicationContext(), "Failed：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void setImage(final Bitmap bitmap) {
        TextImageSuperResolutionActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    private void createAnalyzer() {
        analyzer = MLTextImageSuperResolutionAnalyzerFactory.getInstance().getTextImageSuperResolutionAnalyzer();
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