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

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.LensEngine;
import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.face.face3d.ML3DFace;
import com.huawei.hms.mlsdk.face.face3d.ML3DFaceAnalyzer;
import com.huawei.hms.mlsdk.face.face3d.ML3DFaceAnalyzerSetting;
import com.huawei.mlkit.example.R;
import com.huawei.mlkit.example.camera.GraphicOverlay;
import com.huawei.mlkit.example.camera.LensEnginePreview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Detects face information in camera stream.
 *
 * @since  2020-12-16
 */
public class Live3DFaceAnalyseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = Live3DFaceAnalyseActivity.class.getSimpleName();

    private static final int CAMERA_PERMISSION_CODE = 0;

    private ML3DFaceAnalyzer analyzer;

    private LensEngine mLensEngine;

    private LensEnginePreview mPreview;

    private GraphicOverlay mOverlay;

    private int lensType = LensEngine.FRONT_LENS;

    private boolean isFront = true;

    private boolean isPermissionRequested;

    private static final String[] ALL_PERMISSION =
            new String[]{
                    Manifest.permission.CAMERA,
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_live_face_analyse);
        if (savedInstanceState != null) {
            this.lensType = savedInstanceState.getInt("lensType");
        }
        this.mPreview = this.findViewById(R.id.preview);
        this.mOverlay = this.findViewById(R.id.overlay);
        this.createFaceAnalyzer();
        this.findViewById(R.id.facingSwitch).setOnClickListener(this);
        Switch swFacePoints = this.findViewById(R.id.facePoints);
        Switch swFaceFeature = this.findViewById(R.id.faceFeature);
        swFacePoints.setVisibility(View.GONE);
        swFaceFeature.setVisibility(View.GONE);
        // Checking Camera Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            this.createLensEngine();
        } else {
            this.checkPermission();
        }
    }

    private void createFaceAnalyzer() {
        // Create a face analyzer. You can create an analyzer using the provided customized face detection parameter
        ML3DFaceAnalyzerSetting setting = new ML3DFaceAnalyzerSetting.Factory()
                // Fast detection of continuous video frames.
                // MLFaceAnalyzerSetting.TYPE_PRECISION: indicating the precision preference mode.
                // This mode will detect more faces and be more precise in detecting key points and contours, but will run slower.
                // MLFaceAnalyzerSetting.TYPE_SPEED: representing a preference for speed.
                // This will detect fewer faces and be less precise in detecting key points and contours, but will run faster.
                // .setPerformanceType(MLFaceAnalyzerSetting.TYPE_SPEED)
                .setPerformanceType(ML3DFaceAnalyzerSetting.TYPE_SPEED)
                .setTracingAllowed(true)
                .create();

        this.analyzer = MLAnalyzerFactory.getInstance().get3DFaceAnalyzer(setting);
        this.analyzer.setTransactor(new FaceAnalyzerTransactor(this.mOverlay));
    }

    public class FaceAnalyzerTransactor implements MLAnalyzer.MLTransactor<ML3DFace> {
        private GraphicOverlay mGraphicOverlay;

        FaceAnalyzerTransactor(GraphicOverlay ocrGraphicOverlay) {
            this.mGraphicOverlay = ocrGraphicOverlay;
        }

        /**
         * Process the results returned by the analyzer.
         */
        @Override
        public void transactResult(MLAnalyzer.Result<ML3DFace> result) {
            this.mGraphicOverlay.clear();
            SparseArray<ML3DFace> faceSparseArray = result.getAnalyseList();
            for (int i = 0; i < faceSparseArray.size(); i++) {
                ML3DFaceGraphic graphic = new ML3DFaceGraphic(this.mGraphicOverlay, faceSparseArray.valueAt(i), Live3DFaceAnalyseActivity.this);
                this.mGraphicOverlay.add(graphic);
            }
        }

        @Override
        public void destroy() {
            this.mGraphicOverlay.clear();
        }
    }

    private void createLensEngine() {
        Context context = this.getApplicationContext();
        // Create LensEngine. Recommended image size: large than 320*320, less than 1920*1920.
        this.mLensEngine = new LensEngine.Creator(context, this.analyzer)
                .setLensType(this.lensType)
                .applyDisplayDimension(640, 480)
                .applyFps(25.0f)
                .enableAutomaticFocus(true)
                .create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            this.createLensEngine();
            this.startLensEngine();
        } else {
            this.checkPermission();
        }
    }

    private void startLensEngine() {
        if (this.mLensEngine != null) {
            try {
                this.mPreview.start(this.mLensEngine, this.mOverlay);
            } catch (IOException e) {
                Log.e(Live3DFaceAnalyseActivity.TAG, "Failed to start lens engine.", e);
                this.mLensEngine.release();
                this.mLensEngine = null;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mPreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mLensEngine != null) {
            this.mLensEngine.release();
        }
        if (this.analyzer != null) {
            try {
                this.analyzer.stop();
            } catch (IOException e) {
                Log.e(Live3DFaceAnalyseActivity.TAG, "Stop failed: " + e.getMessage());
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("lensType", this.lensType);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        this.isFront = !this.isFront;
        if (this.isFront) {
            this.lensType = LensEngine.FRONT_LENS;
        } else {
            this.lensType = LensEngine.BACK_LENS;
        }
        if (this.mLensEngine != null) {
            this.mLensEngine.close();
        }
        this.createLensEngine();
        this.startLensEngine();
    }

    // Check the permissions required by the SDK.
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {
            isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            for (String perm : getAllPermission()) {
                if (PackageManager.PERMISSION_GRANTED != this.checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                }
            }

            if (!permissionsList.isEmpty()) {
                requestPermissions(permissionsList.toArray(new String[0]), 0);
            }
        }
    }

    // Permission application callback.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        boolean hasAllGranted = true;
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.createLensEngine();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                hasAllGranted = false;
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    showWaringDialog();
                } else {
                    Toast.makeText(this, R.string.toast, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static List<String> getAllPermission() {
        return Collections.unmodifiableList(Arrays.asList(ALL_PERMISSION));
    }

    private void showWaringDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.Information_permission)
                .setPositiveButton(R.string.go_authorization, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Guide the user to the setting page for manual authorization.
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Instruct the user to perform manual authorization. The permission request fails.
                        finish();
                    }
                }).setOnCancelListener(dialogInterface);
        dialog.setCancelable(false);
        dialog.show();
    }

  static DialogInterface.OnCancelListener dialogInterface =  new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            //Instruct the user to perform manual authorization. The permission request fails.
        }
    };
}
