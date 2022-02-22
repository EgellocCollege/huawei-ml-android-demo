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

package com.huawei.mlkit.example.voice.sounddect;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.mlsdk.sounddect.MLSoundDectConstants;
import com.huawei.hms.mlsdk.sounddect.MLSoundDectListener;
import com.huawei.hms.mlsdk.sounddect.MLSoundDector;
import com.huawei.mlkit.example.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class SoundDectActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SoundDectActivity.class.getSimpleName();
    private static final int RC_RECORD_CODE = 0x123;

    private String[] perms = {Manifest.permission.RECORD_AUDIO};
    private Vector<String> logList;
    private SimpleDateFormat dateFormat;
    private TextView textView;
    private MLSoundDector soundDector;

    private MLSoundDectListener listener = new MLSoundDectListener() {
        @Override
        public void onSoundSuccessResult(Bundle result) {
            String nowTime = dateFormat.format(new Date());
            int soundType = result.getInt(MLSoundDector.RESULTS_RECOGNIZED);
            switch (soundType) {
                case MLSoundDectConstants.SOUND_EVENT_TYPE_LAUGHTER:
                    logList.add(nowTime + "\tsoundType:laughter");
                    break;
                case MLSoundDectConstants.SOUND_EVENT_TYPE_BABY_CRY:
                    logList.add(nowTime + "\tsoundType:baby cry");
                    break;
                case MLSoundDectConstants.SOUND_EVENT_TYPE_SNORING:
                    logList.add(nowTime + "\tsoundType:snoring");
                    break;
                case MLSoundDectConstants.SOUND_EVENT_TYPE_SNEEZE:
                    logList.add(nowTime + "\tsoundType:sneeze");
                    break;
                case MLSoundDectConstants.SOUND_EVENT_TYPE_SCREAMING:
                    logList.add(nowTime + "\tsoundType:screaming");
                    break;
                case MLSoundDectConstants.SOUND_EVENT_TYPE_MEOW:
                    logList.add(nowTime + "\tsoundType:meow");
                    break;
                case MLSoundDectConstants.SOUND_EVENT_TYPE_BARK:
                    logList.add(nowTime + "\tsoundType:bark");
                    break;
                case MLSoundDectConstants.SOUND_EVENT_TYPE_WATER:
                    logList.add(nowTime + "\tsoundType:water");
                    break;
                case MLSoundDectConstants.SOUND_EVENT_TYPE_CAR_ALARM:
                    logList.add(nowTime + "\tsoundType:car alarm");
                    break;
                case MLSoundDectConstants.SOUND_EVENT_TYPE_DOOR_BELL:
                    logList.add(nowTime + "\tsoundType:doorbell");
                    break;
                case MLSoundDectConstants.SOUND_EVENT_TYPE_KNOCK:
                    logList.add(nowTime + "\tsoundType:knock");
                    break;
                case MLSoundDectConstants.SOUND_EVENT_TYPE_ALARM:
                    logList.add(nowTime + "\tsoundType:alarm");
                    break;
                case MLSoundDectConstants.SOUND_EVENT_TYPE_STEAM_WHISTLE:
                    logList.add(nowTime + "\tsoundType:steam whistle");
                    break;
                default:
                    logList.add(nowTime + "\tsoundType:unknown type");
                    break;

            }

            StringBuffer buf = new StringBuffer();
            for (String log : logList) {
                buf.append(log+ "\n");
            }
            if (logList.size() > 10) {
                logList.remove(0);
            }
            textView.setText(buf);
        }

        @Override
        public void onSoundFailResult(int errCode) {
            String errCodeDesc = "";
            switch (errCode) {
                case MLSoundDectConstants.SOUND_DECT_ERROR_NO_MEM:
                    errCodeDesc = "no memory error";
                    break;
                case MLSoundDectConstants.SOUND_DECT_ERROR_FATAL_ERROR:
                    errCodeDesc = "fatal error";
                    break;
                case MLSoundDectConstants.SOUND_DECT_ERROR_AUDIO:
                    errCodeDesc = "microphone error";
                    break;
                case MLSoundDectConstants.SOUND_DECT_ERROR_INTERNAL:
                    errCodeDesc = "internal error";
                    break;
                default:
                    break;
            }
            Log.e(TAG, "FailResult errCode: " + errCode + "errCodeDesc:" + errCodeDesc);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_dect);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        textView = findViewById(R.id.textView);
        findViewById(R.id.start_btn).setOnClickListener(this);
        findViewById(R.id.stop_btn).setOnClickListener(this);
        logList = new Vector<>();
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        initModel();
    }

    private void initModel() {
        // Initialize the voice recognizer
        soundDector = MLSoundDector.createSoundDector();
        // Setting Recognition Result Listening
        soundDector.setSoundDectListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundDector.destroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_btn:
                if (ActivityCompat.checkSelfPermission(SoundDectActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    boolean startSuccess = soundDector.start(SoundDectActivity.this);
                    if (startSuccess) {
                        Toast.makeText(SoundDectActivity.this, R.string.sound_dect_start, Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                ActivityCompat.requestPermissions(SoundDectActivity.this, perms, RC_RECORD_CODE);

                break;
            case R.id.stop_btn:
                soundDector.stop();
                Toast.makeText(SoundDectActivity.this, R.string.sound_dect_stop, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    // Permission application callback.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult ");
        if (requestCode == RC_RECORD_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            boolean startSuccess = soundDector.start(SoundDectActivity.this);
            if (startSuccess) {
                Toast.makeText(SoundDectActivity.this, R.string.sound_dect_start, Toast.LENGTH_LONG).show();
            }
        }
    }
}