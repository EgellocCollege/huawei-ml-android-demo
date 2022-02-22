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

package com.huawei.mlkit.example.voice.aft;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.hms.mlsdk.aft.MLAftConstants;
import com.huawei.hms.mlsdk.aft.MLAftEvents;
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftEngine;
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftListener;
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftResult;
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftSetting;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.mlkit.example.ChooseActivity;
import com.huawei.mlkit.example.R;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AftAnalyseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AftAnalyseActivity.class.getSimpleName();
    private static final int RECORD_REQUEST_CODE = 3;
    private static final int THRESHOLD_TIME = 60000; // 60s
    private TextView tvFileName;
    private TextView tvText;
    private ImageView imgVoice;
    private Button btnQueryShort;
    private Button btnQueryLong;
    private Button btnLong;
    private ImageView imgPlay;
    private String taskId;
    private Uri uri;
    private MLRemoteAftEngine engine;
    private MLRemoteAftSetting setting;
    private static Timer mTimer;
    private TimerTask mTimerTask;
    private String mLongTaskId;
    private Intent aftLongData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_voice_aft);
        this.tvFileName = this.findViewById(R.id.file_name);
        this.tvText = this.findViewById(R.id.text_output);
        this.imgVoice = this.findViewById(R.id.voice_input);
        this.imgPlay = this.findViewById(R.id.voice_play);
        this.imgVoice.setOnClickListener(this);
        this.btnQueryShort =this.findViewById(R.id.btn_query_short_languages);
        this.btnQueryShort.setOnClickListener(this);
        this.btnQueryLong = this.findViewById(R.id.btn_query_long_languages);
        this.btnQueryLong.setOnClickListener(this);
        this.btnLong = this.findViewById(R.id.btn_long_aft);
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
            this.requestPermission();
        }

        this.setting = new MLRemoteAftSetting.Factory()
                // Set the transcription language code, complying with the BCP 47 standard. Currently, zh (Chinese) and en-US (English) are supported.
                .setLanguageCode("en-US")
                .enablePunctuation(true)
                // This method is supported only for short voice calls.
                .enableWordTimeOffset(true)
                .enableSentenceTimeOffset(true)
                .create();
        // Set ApiKey.
        MLApplication.getInstance().setApiKey(ChooseActivity.apiKey);
        // Set access token.
        // MLApplication.getInstance().setAccessToken(MainActivity.accessToken);
        this.engine = MLRemoteAftEngine.getInstance();
        this.engine.init(this);
        // Pass the listener callback to the audio file transcription engine.
        this.engine.setAftListener(this.aftListener);
    }

    private void requestPermission() {
        final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, this.RECORD_REQUEST_CODE);
            return;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.voice_input:
                this.tvFileName.setText("add_voice");
                AftAnalyseActivity.this.imgPlay.setImageResource(R.drawable.icon_voice_new);
                this.tvText.setText("");
                this.startRecord();
                break;
            case R.id.btn_query_short_languages:
                MLRemoteAftEngine.getInstance().getShortAftLanguages(new MLRemoteAftEngine.LanguageCallback() {
                    @Override
                    public void onResult(final List<String> result) {
                        Toast.makeText(AftAnalyseActivity.this,result.toString(),Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "support languages==" + result.toString());
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        Log.e(TAG, "errorCode:" + errorCode + "errorMsg:" + errorMsg);
                    }
                });
                break;
            case R.id.btn_query_long_languages:
                MLRemoteAftEngine.getInstance().getLongAftLanguages(new MLRemoteAftEngine.LanguageCallback() {
                    @Override
                    public void onResult(final List<String> result) {
                        Toast.makeText(AftAnalyseActivity.this,result.toString(),Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "support languages==" + result.toString());
                    }

                    @Override
                    public void onError(int errorCode, String errorMsg) {
                        Log.e(TAG, "errorCode:" + errorCode + "errorMsg:" + errorMsg);
                    }
                });
                break;
            default:
                break;
        }
    }

    private void btnLong(Intent aftLongData){
        btnLong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dealAsrLongUri(aftLongData);
                Toast.makeText(AftAnalyseActivity.this, "Start transcription", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startRecord() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, this.uri);
        this.startActivityForResult(intent, AftAnalyseActivity.RECORD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == AftAnalyseActivity.RECORD_REQUEST_CODE) {
            this.uri = data.getData();
            String filePath = this.getAudioFilePathFromUri(this.uri);
            File file = new File(filePath);
            this.setFileName(file.getName());
            Long audioTime = getAudioFileTimeFromUri(uri);
            if (audioTime < THRESHOLD_TIME) {
                // Transfer the audio less than one minute to the audio file transcription engine.
                this.taskId = this.engine.shortRecognize(this.uri, this.setting);
                Log.i(TAG, "Short audio transcription.");
            } else {
                // Transfer the audio more than one minute to the audio file transcription engine.
              //  this.taskId = this.engine.longRecognize(this.uri, this.setting);
                btnLong(data);
                Log.i(TAG, "Long audio transcription.");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setFileName(final String path) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AftAnalyseActivity.this.tvFileName.setText(path);
                AftAnalyseActivity.this.imgPlay.setImageResource(R.drawable.icon_voice_accent_new);
            }
        });
    }

    private String getAudioFilePathFromUri(Uri uri) {
        Cursor cursor = this.getContentResolver()
                .query(uri, null, null, null, null);
        int index = 0;
        String path;
        if (cursor != null) {
            cursor.moveToFirst();
            index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
            path = cursor.getString(index);
        } else {
            path = uri.getPath();
        }
        return path;
    }


    private Long getAudioFileTimeFromUri(Uri uri) {
        Long time = null;
        Cursor cursor = this.getContentResolver()
                .query(uri, null, null, null, null);
        if (cursor != null) {

            cursor.moveToFirst();
            time = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
        } else {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(String.valueOf(uri));
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e(TAG, "Failed to read the file time.");
            }
            time = Long.valueOf(mediaPlayer.getDuration());
        }
        return time;
    }


    private void displayFailure(int errorCode, String errorMsg) {
        this.tvText.setText("Failure, errorCode is:" + errorCode + ", errorMessage is:" + errorMsg);
    }

    /**
     * Audio file transcription callback function. If you want to use AFT,
     * you need to apply for an agconnect-services.json file in the developer
     * alliance(https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/ml-add-agc),
     * replacing the sample-agconnect-services.json in the project.
     */
    private MLRemoteAftListener aftListener = new MLRemoteAftListener() {
        @Override
        public void onInitComplete(String taskId, Object ext) {
            // Reserved.
            engine.startTask(mLongTaskId);
        }

        @Override
        public void onUploadProgress(String taskId, double progress, Object ext) {
            // Reserved.
        }

        @Override
        public void onEvent(String taskId, int eventId, Object ext) {
            Log.e(TAG, "MLAsrCallBack onEvent" + eventId);
            if (MLAftEvents.UPLOADED_EVENT == eventId) {
                Toast.makeText(AftAnalyseActivity.this, "file upload success, loading...", Toast.LENGTH_SHORT).show();
                startQueryResult();
            }
        }

        // Get notification of transcription results, where developers can process the transcription results.
        @Override
        public void onResult(String taskId, MLRemoteAftResult result, Object ext) {
            Object remoteid = engine.getTaskMetadata(taskId, MLAftConstants.TaskMetadata.REMOTE_TASK_ID);
            Object status = engine.getTaskMetadata(taskId, MLAftConstants.TaskMetadata.TASK_STATUS);
            Log.i(AftAnalyseActivity.TAG, "Remote transcription TaskId is  "+ remoteid);
            Log.i(AftAnalyseActivity.TAG, "Remote transcription status is  "+ status);

            Log.i(AftAnalyseActivity.TAG, taskId + " ");
            if (result.isComplete()) {
                Log.i(AftAnalyseActivity.TAG, "result" + result.getText());
                cancelTimer();
                AftAnalyseActivity.this.tvText.setText(result.getText());
                List<MLRemoteAftResult.Segment> words = result.getWords();
                if (words != null && words.size() != 0) {
                    for (MLRemoteAftResult.Segment word : words) {
                        Log.e(TAG, "MLAsrCallBack word  text is : " + word.getText() + ", startTime is : " + word.getStartTime() + ". endTime is : " + word.getEndTime());
                    }
                }

                List<MLRemoteAftResult.Segment> sentences = result.getSentences();
                if (sentences != null && sentences.size() != 0) {
                    for (MLRemoteAftResult.Segment sentence : sentences) {
                        Log.e(TAG, "MLAsrCallBack sentence  text is : " + sentence.getText() + ", startTime is : " + sentence.getStartTime() + ". endTime is : " + sentence.getEndTime());
                    }
                }
            } else {
                tvText.setText("Loading...");
                return;
            }
        }

        // Transliteration error callback function.
        @Override
        public void onError(String taskId, int errorCode, String message) {
            AftAnalyseActivity.this.displayFailure(errorCode, message);
            Log.e(AftAnalyseActivity.TAG, "onError." + errorCode + " task:" + taskId + " errorMessage：" + message);
        }
    };

    private void startQueryResult() {
        Timer mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                getResult();
            }
        };
        mTimer.schedule(mTimerTask, 5000, 10000);
    }

    private void getResult() {
        engine.setAftListener(aftListener);
        engine.getLongAftResult(mLongTaskId);
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (null != mTimerTask) {
            mTimerTask.cancel();
        }
    }

    private void dealAsrLongUri(Intent data) {
        Uri selectedImage = data.getData();
        engine.setAftListener(aftListener);
        MLRemoteAftSetting setting = new MLRemoteAftSetting.Factory()
                .setLanguageCode("en-US")
                .enablePunctuation(true)
                .enableWordTimeOffset(true)
                .enableSentenceTimeOffset(true)
                .create();
        mLongTaskId = engine.longRecognize(selectedImage, setting);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mTimerTask) {
            mTimerTask.cancel();
        }
    }
}
