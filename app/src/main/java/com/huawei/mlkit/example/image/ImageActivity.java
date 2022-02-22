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

package com.huawei.mlkit.example.image;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.mlkit.example.R;
import com.huawei.mlkit.example.image.classification.ImageClassificationAnalyseActivity;
import com.huawei.mlkit.example.image.documentSkewCorrection.DocumentSkewCorrectionActivity;
import com.huawei.mlkit.example.image.imagesuperresolution.ImageSuperResolutionActivity;
import com.huawei.mlkit.example.image.imgseg.HairSegmentationStillAnalyseActivity;
import com.huawei.mlkit.example.image.imgseg.ImageSegmentationLiveAnalyseActivity;
import com.huawei.mlkit.example.image.imgseg.ImageSegmentationStillAnalyseActivity;
import com.huawei.mlkit.example.image.landmark.ImageLandmarkAnalyseActivity;
import com.huawei.mlkit.example.image.object.LiveObjectAnalyseActivity;
import com.huawei.mlkit.example.image.productvisionsearch.ProductVisionSearchAnalyseActivity;
import com.huawei.mlkit.example.image.scenedetection.SenceDetectionLiveAnalyseActivity;
import com.huawei.mlkit.example.image.scenedetection.SenceDetectionStillAnalyseActivity;
import com.huawei.mlkit.example.image.textimagesuperresolution.TextImageSuperResolutionActivity;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main_image);
        this.findViewById(R.id.btn_imgseg_live).setOnClickListener(this);
        this.findViewById(R.id.btn_imgseg_image).setOnClickListener(this);
        this.findViewById(R.id.btn_live_scene_detection).setOnClickListener(this);
        this.findViewById(R.id.btn_image_scene_detection).setOnClickListener(this);
        this.findViewById(R.id.btn_object).setOnClickListener(this);
        this.findViewById(R.id.btn_landmark).setOnClickListener(this);
        this.findViewById(R.id.btn_classification).setOnClickListener(this);
        this.findViewById(R.id.btn_imageSuper_resolution).setOnClickListener(this);
        this.findViewById(R.id.btn_text_super_resolution).setOnClickListener(this);
        this.findViewById(R.id.btn_productvisionsearch).setOnClickListener(this);
        this.findViewById(R.id.btn_documentSkewCorrection).setOnClickListener(this);
        this.findViewById(R.id.btn_imgseg_hair).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_imgseg_live:
                this.startActivity(new Intent(ImageActivity.this, ImageSegmentationLiveAnalyseActivity.class));
                break;
            case R.id.btn_imgseg_image:
                this.startActivity(new Intent(ImageActivity.this, ImageSegmentationStillAnalyseActivity.class));
                break;
            case R.id.btn_live_scene_detection:
                this.startActivity(new Intent(ImageActivity.this, SenceDetectionLiveAnalyseActivity.class));
                break;
            case R.id.btn_image_scene_detection:
                this.startActivity(new Intent(ImageActivity.this, SenceDetectionStillAnalyseActivity.class));
                break;
            case R.id.btn_object:
                this.startActivity(new Intent(ImageActivity.this, LiveObjectAnalyseActivity.class));
                break;
            case R.id.btn_landmark:
                this.startActivity(new Intent(ImageActivity.this, ImageLandmarkAnalyseActivity.class));
                break;
            case R.id.btn_classification:
                this.startActivity(new Intent(ImageActivity.this, ImageClassificationAnalyseActivity.class));
                break;
            case R.id.btn_imageSuper_resolution:
                this.startActivity(new Intent(ImageActivity.this, ImageSuperResolutionActivity.class));
                break;
            case R.id.btn_text_super_resolution:
                this.startActivity(new Intent(ImageActivity.this, TextImageSuperResolutionActivity.class));
                break;
            case R.id.btn_productvisionsearch:
                this.startActivity(new Intent(ImageActivity.this, ProductVisionSearchAnalyseActivity.class));
                break;
            case R.id.btn_documentSkewCorrection:
                this.startActivity(new Intent(ImageActivity.this, DocumentSkewCorrectionActivity.class));
                break;
            case R.id.btn_imgseg_hair:
                this.startActivity(new Intent(ImageActivity.this, HairSegmentationStillAnalyseActivity.class));
                break;
            default:
                break;
        }
    }
}
