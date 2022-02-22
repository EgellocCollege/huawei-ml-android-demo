/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.mlkit.example.body.faceverification;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.common.MLException;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.faceverify.MLFaceTemplateResult;
import com.huawei.hms.mlsdk.faceverify.MLFaceVerificationAnalyzer;
import com.huawei.hms.mlsdk.faceverify.MLFaceVerificationAnalyzerFactory;
import com.huawei.hms.mlsdk.faceverify.MLFaceVerificationAnalyzerSetting;
import com.huawei.hms.mlsdk.faceverify.MLFaceVerificationResult;
import com.huawei.mlkit.example.R;
import com.huawei.mlkit.example.body.face.LiveFaceAnalyseActivity;

import java.util.List;

public class FaceVerificationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = FaceVerificationActivity.class.getSimpleName();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    ImageView template;

    ImageView compare;

    RelativeLayout templateRe;

    RelativeLayout compareRe;

    TextView templateTextView;

    TextView compareTextView;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BitmapUtils.recycleBitmap(templateBitmap);
        BitmapUtils.recycleBitmap(templateBitmapCopy);
        BitmapUtils.recycleBitmap(compareBitmap);
        BitmapUtils.recycleBitmap(compareBitmapCopy);
    }

    TextView resultTextView;

    ImageView templatePreview;

    ImageView comparePreview;

    Button compareBtn;

    MLFaceVerificationAnalyzer analyzer;

    private int REQUEST_CHOOSE_TEMPLATEPIC = 2001;

    private int REQUEST_CHOOSE_COMPAEPIC = 2002;

    private Uri templateImageUri;

    private Uri compareImageUri;

    private Bitmap templateBitmap;
    private Bitmap compareBitmap;
    private Bitmap templateBitmapCopy;
    private Bitmap compareBitmapCopy;

    private boolean isLandScape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_verification);
        verifyStoragePermissions(this);
        template = this.findViewById(R.id.img_template);
        template.setOnClickListener(this);
        compare = this.findViewById(R.id.img_verify);
        compare.setOnClickListener(this);
        compareBtn = this.findViewById(R.id.btn_verify);
        compareBtn.setOnClickListener(this);
        templateRe = this.findViewById(R.id.template);
        templateRe.setOnClickListener(this);
        compareRe = this.findViewById(R.id.verify);
        compareRe.setOnClickListener(this);
        templateTextView = this.findViewById(R.id.txt_template);
        compareTextView = this.findViewById(R.id.txt_verify);

        templatePreview = this.findViewById(R.id.tempPreview);
        comparePreview = this.findViewById(R.id.compPreview);
        resultTextView = this.findViewById(R.id.edit_text);
        isLandScape = (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        initAnalyzer();
    }

    private void initAnalyzer() {
        MLFaceVerificationAnalyzerSetting.Factory factory = new MLFaceVerificationAnalyzerSetting.Factory().setMaxFaceDetected(3);
        MLFaceVerificationAnalyzerSetting setting = factory.create();
        analyzer = MLFaceVerificationAnalyzerFactory
                .getInstance()
                .getFaceVerificationAnalyzer(setting);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.template:
            case R.id.img_template:
                BitmapUtils.recycleBitmap(templateBitmap);
                BitmapUtils.recycleBitmap(templateBitmapCopy);
                selectLocalImage(REQUEST_CHOOSE_TEMPLATEPIC);
                break;
            case R.id.verify:
            case R.id.img_verify:
                BitmapUtils.recycleBitmap(compareBitmap);
                BitmapUtils.recycleBitmap(compareBitmapCopy);
                selectLocalImage(REQUEST_CHOOSE_COMPAEPIC);
                break;
            case R.id.btn_verify:
                compare();
                // test();
                break;
            default:
                break;
        }
    }
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //check permission
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // If you do not have the write permission, a dialog box is displayed when you apply for the write permission.
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            Log.e(TAG,"No permission.");
        }
    }

    private void loadTemplatePic() {
        if (templateBitmap == null) {
            return;
        }
        long startTime = System.currentTimeMillis();
        List<MLFaceTemplateResult> results = analyzer.setTemplateFace(MLFrame.fromBitmap(templateBitmap));
        long endTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append("##setTemplateFace|COST[");
        sb.append(endTime - startTime);
        sb.append("]");
        if (results.isEmpty()) {
            sb.append("Failure!");
        } else {
            sb.append("Success!");
        }
        for (MLFaceTemplateResult template : results) {
            int id = template.getTemplateId();
            Rect location = template.getFaceInfo().getFaceRect();
            Canvas canvas = new Canvas(templateBitmapCopy);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);// Not Filled
            paint.setStrokeWidth((location.right - location.left) / 50f);  // Line width
            canvas.drawRect(location, paint);// framed
            templatePreview.setImageBitmap(templateBitmapCopy);
            sb.append("|Face[");
            sb.append(location);
            sb.append("]ID[");
            sb.append(id);
            sb.append("]");
        }
        sb.append("\n");
        resultTextView.setText(sb.toString());
    }

    private Bitmap loadPic(Uri picUri, ImageView view) {
        Bitmap pic = null;
        pic = BitmapUtils.loadFromPath(this, picUri, ((View) view.getParent()).getWidth(),
                ((View) view.getParent()).getHeight()).copy(Bitmap.Config.ARGB_8888, true);
        if (pic == null) {
            Toast.makeText(this.getApplicationContext(), R.string.please_select_picture, Toast.LENGTH_SHORT).show();
        }
        view.setImageBitmap(pic);
        return pic;
    }

    private void compare() {

        if (compareBitmap == null) {
            return;
        }
        final long startTime = System.currentTimeMillis();
        try {
            Task<List<MLFaceVerificationResult>> task = analyzer.asyncAnalyseFrame(MLFrame.fromBitmap(compareBitmap));
            final StringBuilder sb = new StringBuilder();
            sb.append("##getFaceSimilarity|");
            task.addOnSuccessListener(new OnSuccessListener<List<MLFaceVerificationResult>>() {
                @Override
                public void onSuccess(List<MLFaceVerificationResult> mlCompareList) {
                    long endTime = System.currentTimeMillis();
                    sb.append("COST[");
                    sb.append(endTime - startTime);
                    sb.append("]|Success!");
                    for (MLFaceVerificationResult template : mlCompareList) {
                        Rect location = template.getFaceInfo().getFaceRect();
                        Canvas canvas = new Canvas(compareBitmapCopy);
                        Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Paint.Style.STROKE);// Not Filled
                        paint.setStrokeWidth((location.right - location.left) / 50f);  // Line width
                        canvas.drawRect(location, paint);// framed
                        int id = template.getTemplateId();
                        float similarity = template.getSimilarity();
                        comparePreview.setImageBitmap(compareBitmapCopy);
                        sb.append("|Face[");
                        sb.append(location);
                        sb.append("]Id[");
                        sb.append(id);
                        sb.append("]Similarity[");
                        sb.append(similarity);
                        sb.append("]");
                    }
                    sb.append("\n");
                    resultTextView.append(sb.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    long endTime = System.currentTimeMillis();
                    sb.append("COST[");
                    sb.append(endTime - startTime);
                    sb.append("]|Failure!");
                    if (e instanceof MLException) {
                        MLException mlException = (MLException) e;
                        // Obtain error codes. Developers can process the error codes and display differentiated messages based on the error codes.
                        int errorCode = mlException.getErrCode();
                        // Obtain error information. Developers can quickly locate faults based on the error code.
                        String errorMessage = mlException.getMessage();
                        sb.append("|ErrorCode[");
                        sb.append(errorCode);
                        sb.append("]Msg[");
                        sb.append(errorMessage);
                        sb.append("]");
                    } else {
                        sb.append("|Error[");
                        sb.append(e.getMessage());
                        sb.append("]");
                    }
                    sb.append("\n");
                    resultTextView.append(sb.toString());
                }
            });
        }catch (RuntimeException e){
            Log.e(TAG,"Set the image containing the face for comparison.");
        }
    }

    private void selectLocalImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        this.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == this.REQUEST_CHOOSE_TEMPLATEPIC) && (resultCode == Activity.RESULT_OK)) {
            // In this case, imageUri is returned by the chooser, save it.
            if (data == null) {
                Toast.makeText(this.getApplicationContext(), R.string.please_select_picture, Toast.LENGTH_SHORT).show();
                return;
            }
            this.templateImageUri = data.getData();
            templateBitmap = loadPic(templateImageUri, templatePreview);
            templateBitmapCopy = templateBitmap.copy(Bitmap.Config.ARGB_8888, true);
            template.setVisibility(View.INVISIBLE);
            template.setFocusableInTouchMode(false);
            templateTextView.setVisibility(View.INVISIBLE);
            this.loadTemplatePic();
        } else if ((requestCode == this.REQUEST_CHOOSE_COMPAEPIC)) {
            if (data == null) {
                Toast.makeText(this.getApplicationContext(), R.string.please_select_picture, Toast.LENGTH_SHORT).show();
                return;
            }
            this.compareImageUri = data.getData();
            compareBitmap = loadPic(compareImageUri, comparePreview);
            compareBitmapCopy = compareBitmap.copy(Bitmap.Config.ARGB_8888, true);
            compare.setVisibility(View.INVISIBLE);
            compare.setFocusableInTouchMode(false);
            compareTextView.setVisibility(View.INVISIBLE);
        }
    }
}
