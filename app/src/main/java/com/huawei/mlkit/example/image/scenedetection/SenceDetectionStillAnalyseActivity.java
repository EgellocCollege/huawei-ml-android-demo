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

package com.huawei.mlkit.example.image.scenedetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.scd.MLSceneDetection;
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzer;
import com.huawei.hms.mlsdk.scd.MLSceneDetectionAnalyzerFactory;
import com.huawei.mlkit.example.R;

import java.util.List;

public class SenceDetectionStillAnalyseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SenceDetectionStillAnalyseActivity.class.getSimpleName();

    private MLSceneDetectionAnalyzer analyzer;

    private Bitmap bitmap;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_image_scene_analyse);
        this.findViewById(R.id.scene_detect).setOnClickListener(this);
        textView = findViewById(R.id.result_scene);
    }

    @Override
    public void onClick(View v) {
        this.analyzer();
    }

    private void analyzer() {
        this.analyzer = MLSceneDetectionAnalyzerFactory.getInstance().getSceneDetectionAnalyzer();
        // Create an MLFrame by using android.graphics.Bitmap. Recommended image size: large than 224*224.
        Bitmap originBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.scenedetection_image);
        MLFrame frame = new MLFrame.Creator()
                .setBitmap(originBitmap)
                .create();
        Task<List<MLSceneDetection>> task = this.analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new OnSuccessListener<List<MLSceneDetection>>() {
            @Override
            public void onSuccess(List<MLSceneDetection> sceneInfos) {
                if (sceneInfos != null && !sceneInfos.isEmpty()) {
                    SenceDetectionStillAnalyseActivity.this.displaySuccess(sceneInfos);
                } else {
                    SenceDetectionStillAnalyseActivity.this.displayFailure();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                SenceDetectionStillAnalyseActivity.this.displayFailure();
            }
        });
    }

    private void displaySuccess(List<MLSceneDetection> sceneInfos) {
        String str = "SceneCount：" + sceneInfos.size() + "\n";
        for (int i = 0; i < sceneInfos.size(); i++) {
            MLSceneDetection sceneInfo = sceneInfos.get(i);
            str += "Scene：" + sceneInfo.getResult() + "\n" + "Confidence：" + sceneInfo.getConfidence() + "\n";
        }
        textView.setText(str);
    }

    private void displayFailure() {
        Toast.makeText(this.getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
    }

    private Bitmap getBitmap(Context context, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        TypedValue value = new TypedValue();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        context.getResources().openRawResource(resId, value);
        options.inTargetDensity = value.density;
        options.inScaled = false;
        options.inSampleSize = 1;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.analyzer != null) {
            this.analyzer.stop();
        }
    }
}
