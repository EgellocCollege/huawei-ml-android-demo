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

package com.huawei.mlkit.example.image.imgseg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation;
import com.huawei.mlkit.example.camera.GraphicOverlay;
import com.huawei.mlkit.example.camera.LensEnginePreview;

/**
 * Draw the detected object information and overlay it on the preview frame.
 *
 * @since  2020-12-16
 */
public class MLSegmentGraphic extends GraphicOverlay.Graphic {
    private static final String TAG = MLSegmentGraphic.class.getSimpleName();
    private Rect mDestRect;
    private final Paint resultPaint;
    private Bitmap bitmapForeground;
    private Bitmap bitmapBackground;
    private Boolean isFront;

    MLSegmentGraphic(LensEnginePreview preview, GraphicOverlay overlay, MLImageSegmentation segmentation, Boolean isFront) {
        super(overlay);
        this.bitmapForeground = segmentation.getForeground();
        this.isFront = isFront;

        int width = bitmapForeground.getWidth();
        int height = bitmapForeground.getHeight();
        int div = overlay.getWidth() - preview.getWidth();
        int left = overlay.getWidth() - width + div / 2;
        // Set the image display area.
        // Partial display.
        mDestRect = new Rect(left, 0, overlay.getWidth() - div / 2, height / 2);
        // All display.
        // mDestRect = new Rect(0, 0, overlay.getWidth(), overlay.getHeight());
        this.resultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.resultPaint.setFilterBitmap(true);
        this.resultPaint.setDither(true);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(isFront ? convert(bitmapForeground) : bitmapForeground, null, mDestRect, resultPaint);
    }

    private Bitmap convert(Bitmap bitmap) {
        Matrix m = new Matrix();
        m.setScale(-1, 1);
        Bitmap reverseBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        return reverseBitmap;
    }
}
