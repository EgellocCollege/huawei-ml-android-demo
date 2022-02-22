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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.SparseArray;

import com.huawei.hms.mlsdk.scd.MLSceneDetection;
import com.huawei.mlkit.example.camera.GraphicOverlay;

public class MLSenceDetectionGraphic extends GraphicOverlay.Graphic {
    private final GraphicOverlay overlay;

    private final SparseArray<MLSceneDetection> results;

    public MLSenceDetectionGraphic(GraphicOverlay overlay, SparseArray<MLSceneDetection> results) {
        super(overlay);
        this.overlay = overlay;
        this.results = results;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(48);

        canvas.drawText("SceneCount：" + results.size(), overlay.getWidth() / 5f, 50, paint);
        for (int i = 0; i < results.size(); i++) {
            canvas.drawText("Scene：" + results.get(i).getResult(), overlay.getWidth() / 5f, 100 * (i + 1), paint);
            canvas.drawText("Confidence：" + results.get(i).getConfidence(), overlay.getWidth() / 5f, (100 * (i + 1)) + 50, paint);
        }
    }
}
