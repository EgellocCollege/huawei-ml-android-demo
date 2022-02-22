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

package com.huawei.mlkit.example.body.face;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import com.huawei.hms.mlsdk.common.MLPosition;
import com.huawei.hms.mlsdk.face.MLFace;
import com.huawei.hms.mlsdk.face.MLFaceKeyPoint;
import com.huawei.hms.mlsdk.face.MLFaceShape;
import com.huawei.mlkit.example.camera.GraphicOverlay;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MLFaceGraphic extends GraphicOverlay.Graphic {
    private static final String TAG = MLFaceGraphic.class.getSimpleName();

    private final GraphicOverlay overlay;

    private final Paint facePositionPaint;

    private final Paint keypointPaint;

    private final Paint boxPaint;

    private final Paint facePaint;

    private final Paint eyePaint;

    private final Paint eyebrowPaint;

    private final Paint lipPaint;

    private final Paint nosePaint;

    private final Paint noseBasePaint;

    private final Paint textPaint;

    private final Paint faceFeaturePaintText;

    private volatile MLFace mFace;

    private Context mContext;

    private  float LINE_WIDTH;

    public static float dp2px(Context context, float dipValue) {
        return dipValue * context.getResources().getDisplayMetrics().density + 0.5f;
    }

    private boolean isLandScape() {
        Configuration configuration = this.mContext.getResources().getConfiguration(); // Get the configuration information.
        int ori = configuration.orientation; // Get screen orientation.
        return ori == Configuration.ORIENTATION_LANDSCAPE;
    }

    public MLFaceGraphic(GraphicOverlay overlay, MLFace face, Context context) {
        super(overlay);

        this.mContext = context;
        this.mFace = face;
        this.overlay = overlay;
        final int selectedColor = Color.WHITE;
        LINE_WIDTH = dp2px(this.mContext, 1);
        this.facePositionPaint = new Paint();
        this.facePositionPaint.setColor(selectedColor);

        this.textPaint = new Paint();
        this.textPaint.setColor(Color.WHITE);
        this.textPaint.setTextSize(dp2px(context, 6));
        this.textPaint.setTypeface(Typeface.DEFAULT);

        this.faceFeaturePaintText = new Paint();
        this.faceFeaturePaintText.setColor(Color.WHITE);
        this.faceFeaturePaintText.setTextSize(dp2px(context, 11));
        this.faceFeaturePaintText.setTypeface(Typeface.DEFAULT);

        this.keypointPaint = new Paint();
        this.keypointPaint.setColor(Color.RED);
        this.keypointPaint.setStyle(Paint.Style.FILL);
        this.keypointPaint.setTextSize(dp2px(context, 2));

        this.boxPaint = new Paint();
        this.boxPaint.setColor(Color.parseColor("#ffcc66"));
        this.boxPaint.setStyle(Paint.Style.STROKE);
        this.boxPaint.setStrokeWidth(LINE_WIDTH);

        this.facePaint = new Paint();
        this.facePaint.setColor(Color.parseColor("#ffcc66"));
        this.facePaint.setStyle(Paint.Style.STROKE);
        this.facePaint.setStrokeWidth(LINE_WIDTH);

        this.eyePaint = new Paint();
        this.eyePaint.setColor(Color.parseColor("#00ccff"));
        this.eyePaint.setStyle(Paint.Style.STROKE);
        this.eyePaint.setStrokeWidth(LINE_WIDTH);

        this.eyebrowPaint = new Paint();
        this.eyebrowPaint.setColor(Color.parseColor("#006666"));
        this.eyebrowPaint.setStyle(Paint.Style.STROKE);
        this.eyebrowPaint.setStrokeWidth(LINE_WIDTH);

        this.nosePaint = new Paint();
        this.nosePaint.setColor(Color.parseColor("#ffff00"));
        this.nosePaint.setStyle(Paint.Style.STROKE);
        this.nosePaint.setStrokeWidth(LINE_WIDTH);

        this.noseBasePaint = new Paint();
        this.noseBasePaint.setColor(Color.parseColor("#ff6699"));
        this.noseBasePaint.setStyle(Paint.Style.STROKE);
        this.noseBasePaint.setStrokeWidth(LINE_WIDTH);

        this.lipPaint = new Paint();
        this.lipPaint.setColor(Color.parseColor("#990000"));
        this.lipPaint.setStyle(Paint.Style.STROKE);
        this.lipPaint.setStrokeWidth(LINE_WIDTH);
    }

    public List<String> sortHashMap(HashMap<String, Float> map) {
        Set<Map.Entry<String, Float>> entey = map.entrySet();
        List<Map.Entry<String, Float>> list = new ArrayList<Map.Entry<String, Float>>(entey);
        Collections.sort(list, comparator);
        List<String> emotions = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            emotions.add(list.get(i).getKey());
        }
        return emotions;
    }

  static Comparator comparator =  new Comparator<Map.Entry<String, Float>>() {
        @Override
        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
            if (o2.getValue() - o1.getValue() >= 0) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    @Override
    public void draw(Canvas canvas) {
        if (this.mFace == null) {
            return;
        }
        // Draw rect of face.
        canvas.drawRect(translateRect(mFace.getBorder()), boxPaint);
        // Draw points of face.
        paintKeyPoint(canvas);
        // Draw features of face.
        paintFeatures(canvas);
    }

    // Draw the face features.
    private void paintFeatures(Canvas canvas) {
        float start = this.overlay.getWidth() / 4.0f;
        float x = start;
        float width = this.overlay.getWidth() / 3f;
        float y;
        if (this.isLandScape()) {
            y = (dp2px(this.mContext, this.overlay.getHeight() / 8.0f)) < 130 ? 130 : (dp2px(this.mContext, this.overlay.getHeight() / 8.0f));
        } else {
            y = (dp2px(this.mContext, this.overlay.getHeight() / 16.0f)) < 350.0 ? 350 : (dp2px(this.mContext, this.overlay.getHeight() / 16.0f));
            if (this.overlay.getHeight() > 2500) {
                y = dp2px(this.mContext, this.overlay.getHeight() / 10.0f);
            }
        }
        float space = dp2px(this.mContext, 12);
        Log.i(TAG, x + "," + y + "; height" + this.overlay.getHeight() + ",width" + this.overlay.getWidth());

        HashMap<String, Float> emotions = new HashMap<>();
        emotions.put("Smiling", this.mFace.getEmotions().getSmilingProbability());
        emotions.put("Neutral", this.mFace.getEmotions().getNeutralProbability());
        emotions.put("Angry", this.mFace.getEmotions().getAngryProbability());
        emotions.put("Fear", this.mFace.getEmotions().getFearProbability());
        emotions.put("Sad", this.mFace.getEmotions().getSadProbability());
        emotions.put("Disgust", this.mFace.getEmotions().getDisgustProbability());
        emotions.put("Surprise", this.mFace.getEmotions().getSurpriseProbability());
        List<String> result = this.sortHashMap(emotions);

        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        canvas.drawText("Glass Probability: " + decimalFormat.format(mFace.getFeatures().getSunGlassProbability()), x, y, this.faceFeaturePaintText);
        x = x + width;
        String sex = (mFace.getFeatures().getSexProbability() > 0.5f) ? "Female" : "Male";
        canvas.drawText("Gender: " + sex, x, y, this.faceFeaturePaintText);
        y = y - space;
        x = start;
        canvas.drawText("EulerAngleY: " + decimalFormat.format(mFace.getRotationAngleY()), x, y, this.faceFeaturePaintText);
        x = x + width;
        canvas.drawText("EulerAngleX: " + decimalFormat.format(mFace.getRotationAngleX()), x, y, this.faceFeaturePaintText);
        y = y - space;
        x = start;
        canvas.drawText("EulerAngleZ: " + decimalFormat.format(mFace.getRotationAngleZ()), x, y, this.faceFeaturePaintText);
        x = x + width;
        canvas.drawText("Emotion: " + result.get(0), x, y, this.faceFeaturePaintText);
        x = start;
        y = y - space;
        canvas.drawText("Hat Probability: " + decimalFormat.format(mFace.getFeatures().getHatProbability()), x, y, this.faceFeaturePaintText);
        x = x + width;
        canvas.drawText("Age: " + mFace.getFeatures().getAge(), x, y, this.faceFeaturePaintText);
        y = y - space;
        x = start;
        canvas.drawText("Moustache Probability: " + decimalFormat.format(mFace.getFeatures().getMoustacheProbability()), x, y, this.faceFeaturePaintText);
        y = y - space;
        canvas.drawText("Right eye open Probability: " + decimalFormat.format(mFace.opennessOfRightEye()), x, y, this.faceFeaturePaintText);
        y = y - space;
        canvas.drawText("Left eye open Probability: " + decimalFormat.format(mFace.opennessOfLeftEye()), x, y, this.faceFeaturePaintText);
    }

    // Draw the face points.
    private void paintKeyPoint(Canvas canvas) {
        if (this.mFace.getFaceShapeList() != null) {
            for (MLFaceShape faceShape : this.mFace.getFaceShapeList()) {
                if (faceShape == null) {
                    continue;
                }
                List<MLPosition> points = faceShape.getPoints();
                for (int i = 0; i < points.size(); i++) {
                    MLPosition point = points.get(i);
                    canvas.drawPoint(this.translateX(point.getX().floatValue()), this.translateY(point.getY().floatValue()),
                            this.boxPaint);
                    if (i != (points.size() - 1)) {
                        MLPosition next = points.get(i + 1);
                            if (i % 3 == 0) {
                                canvas.drawText(i + 1 + "", this.translateX(point.getX().floatValue()),
                                        this.translateY(point.getY().floatValue()), this.textPaint);
                            }
                            canvas.drawLines(new float[]{this.translateX(point.getX().floatValue()),
                                    this.translateY(point.getY().floatValue()), this.translateX(next.getX().floatValue()),
                                    this.translateY(next.getY().floatValue())}, this.getPaint(faceShape));
                    }
                }
            }
        }
        for (MLFaceKeyPoint keyPoint : this.mFace.getFaceKeyPoints()) {
            if (keyPoint != null) {
                MLPosition point = keyPoint.getPoint();
                canvas.drawCircle(this.translateX(point.getX()), this.translateY(point.getY()), dp2px(this.mContext, 3), this.keypointPaint);
            }
        }
    }

    public Rect translateRect(Rect rect) {
        float left = translateX(rect.left);
        float right = translateX(rect.right);
        float bottom = translateY(rect.bottom);
        float top = translateY(rect.top);
        if (left > right) {
            float size = left;
            left = right;
            right = size;
        }
        if (bottom < top) {
            float size = bottom;
            bottom = top;
            top = size;
        }
        return new Rect((int) left, (int) top, (int) right, (int) bottom);
    }

    private Paint getPaint(MLFaceShape faceShape) {
        switch (faceShape.getFaceShapeType()) {
            case MLFaceShape.TYPE_LEFT_EYE:
            case MLFaceShape.TYPE_RIGHT_EYE:
                return this.eyePaint;
            case MLFaceShape.TYPE_BOTTOM_OF_LEFT_EYEBROW:

            case MLFaceShape.TYPE_BOTTOM_OF_RIGHT_EYEBROW:
            case MLFaceShape.TYPE_TOP_OF_LEFT_EYEBROW:
            case MLFaceShape.TYPE_TOP_OF_RIGHT_EYEBROW:
                return this.eyebrowPaint;
            case MLFaceShape.TYPE_BOTTOM_OF_LOWER_LIP:
            case MLFaceShape.TYPE_TOP_OF_LOWER_LIP:
            case MLFaceShape.TYPE_BOTTOM_OF_UPPER_LIP:
            case MLFaceShape.TYPE_TOP_OF_UPPER_LIP:
                return this.lipPaint;
            case MLFaceShape.TYPE_BOTTOM_OF_NOSE:
                return this.noseBasePaint;
            case MLFaceShape.TYPE_BRIDGE_OF_NOSE:
                return this.nosePaint;
            default:
                return this.facePaint;
        }
    }
}