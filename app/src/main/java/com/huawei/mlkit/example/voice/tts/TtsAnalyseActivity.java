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

package com.huawei.mlkit.example.voice.tts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.mlkit.example.ChooseActivity;
import com.huawei.mlkit.example.R;

public class TtsAnalyseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int WRITE_PERMISSION_CODE = 0;

    // Permission
    private static final String[] ALL_PERMISSION = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // tts Online mode
    private Button btn_online_mode;

    // tts Offline mode
    private Button btn_offline_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_tts);
        // Set ApiKey.
        MLApplication.getInstance().setApiKey(ChooseActivity.apiKey);
        // Set access token.
        // MLApplication.getInstance().setAccessToken(MainActivity.accessToken);
        initView();
        initListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }
    }

    private void initView() {
        btn_online_mode = findViewById(R.id.btn_online_mode);
        btn_offline_mode = findViewById(R.id.btn_offline_mode);
    }

    private void initListener() {
        btn_online_mode.setOnClickListener(this);
        btn_offline_mode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_online_mode:
                startActivity(new Intent(this, TtsOnlineModeActivity.class));
                break;
            case R.id.btn_offline_mode:
                startActivity(new Intent(this, TtsOfflineModeActivity.class));
                break;
            default:
                break;
        }
    }

    // Check the permissions required by the SDK.
    private void checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            ArrayList<String> permissionsList = new ArrayList<>();
            for (String perm : getAllPermission()) {
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, perm)) {
                    permissionsList.add(perm);
                }
            }
            if (!permissionsList.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[0]), WRITE_PERMISSION_CODE);
            }
        }
    }

    public static List<String> getAllPermission() {
        return Collections.unmodifiableList(Arrays.asList(ALL_PERMISSION));
    }

    // Permission application callback.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        if (requestCode == WRITE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
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

    private void showWaringDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.permission_warn)
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
                }).setOnCancelListener(dialogFace);
        dialog.setCancelable(false);
        dialog.show();
    }

    static DialogInterface.OnCancelListener dialogFace =  new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            //Instruct the user to perform manual authorization. The permission request fails.
        }
    };
}
