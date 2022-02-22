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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.mlsdk.model.download.MLLocalModelManager;
import com.huawei.hms.mlsdk.model.download.MLModelDownloadListener;
import com.huawei.hms.mlsdk.model.download.MLModelDownloadStrategy;
import com.huawei.hms.mlsdk.tts.MLTtsConfig;
import com.huawei.hms.mlsdk.tts.MLTtsConstants;
import com.huawei.hms.mlsdk.tts.MLTtsEngine;
import com.huawei.hms.mlsdk.tts.MLTtsLocalModel;
import com.huawei.mlkit.example.R;

import java.util.Locale;

public class TtsOfflineModeActivity extends BaseActivity {
    private static final long M = 1024 * 1024;

    private EditText mEditText;
    private TextView mTextView;
    private TextView tv_download_progress;

    MLLocalModelManager manager;
    MLTtsEngine mlTtsEngine;
    MLTtsConfig mlTtsConfigs;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case HANDLE_CODE:
                    String text = message.getData().getString(HANDLE_KEY);
                    mTextView.setText(text + "\n");
                    Log.e(TAG, text);
                    break;
                default:
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts_offline_mode);
        findViewById(R.id.btn_speak).setOnClickListener(this);
        findViewById(R.id.btn_stop_speak).setOnClickListener(this);
        findViewById(R.id.btn_download_model).setOnClickListener(this);
        findViewById(R.id.close).setOnClickListener(this);
        mEditText = findViewById(R.id.edit_input);
        mTextView = findViewById(R.id.textView);
        tv_download_progress = findViewById(R.id.tv_download_progress);
        // Use customized parameter settings to create a offline TTS engine.
        mlTtsConfigs = new MLTtsConfig()
                // Setting the language for synthesis.
                .setLanguage(MLTtsConstants.TTS_EN_US)
                // Set the timbre.
                .setPerson(MLTtsConstants.TTS_SPEAKER_OFFLINE_EN_US_MALE_EAGLE)
                // Set the speech speed. Range: 0.2–2.0 1.0 indicates 1x speed.
                .setSpeed(1.0f)
                // Set the volume. Range: 0.2–2.0 1.0 indicates 1x volume.
                .setVolume(1.0f)
                // set the synthesis mode.
                .setSynthesizeMode(MLTtsConstants.TTS_OFFLINE_MODE);
        mlTtsEngine = new MLTtsEngine(mlTtsConfigs);
        //Sets the volume of the built-in player.
        mlTtsEngine.setPlayerVolume(20);
        // Pass the TTS callback to the TTS engine.
        mlTtsEngine.setTtsCallback(callback);
        manager = MLLocalModelManager.getInstance();
        setOnResultCallback(new DisplayResultCallback() {
            @Override
            public void displayResult(String str) {
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString(HANDLE_KEY, str);
                msg.setData(data);
                msg.what = HANDLE_CODE;
                handler.sendMessage(msg);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
                mEditText.setText("");
                break;
            case R.id.btn_speak:
                final String text = mEditText.getText().toString();
                //Check whether the offline model corresponding to the language has been downloaded.
                MLTtsLocalModel model = new MLTtsLocalModel.Factory(MLTtsConstants.TTS_SPEAKER_OFFLINE_EN_US_MALE_EAGLE).create();
                manager.isModelExist(model).addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if (aBoolean) {
                            speak(text);
                        } else {
                            Log.e(TAG, "isModelDownload== " + aBoolean);
                            showToast("The offline model has not been downloaded!");
                            downloadModel(MLTtsConstants.TTS_SPEAKER_OFFLINE_EN_US_MALE_EAGLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "downloadModel failed: " + e.getMessage());
                        showToast(e.getMessage());
                    }
                });
                break;
            case R.id.btn_download_model:
                downloadModel(MLTtsConstants.TTS_SPEAKER_OFFLINE_EN_US_MALE_EAGLE);
                break;
            case R.id.btn_stop_speak:
                mlTtsEngine.stop();
                break;
            default:
                break;
        }
    }

    private void speak(String text) {
        /**
         *First parameter sourceText: text information to be synthesized. The value can contain a maximum of 500 characters.
         *Second parameter indicating the synthesis mode: The format is configA | configB | configC.
         *configA：
         *    MLTtsEngine.QUEUE_APPEND：After an audio synthesis task is generated, the audio synthesis task is processed as follows: If playback is going on, the task is added to the queue for execution in sequence; if playback pauses, the playback is resumed and the task is added to the queue for execution in sequence; if there is no playback, the audio synthesis task is executed immediately.
         *    MLTtsEngine.QUEUE_FLUSH：The ongoing audio synthesis task and playback are stopped immediately, all audio synthesis tasks in the queue are cleared, and the current audio synthesis task is executed immediately and played.
         *configB：
         *    MLTtsEngine.OPEN_STREAM：The synthesized audio data is output through onAudioAvailable.
         *configC：
         *    MLTtsEngine.EXTERNAL_PLAYBACK：external playback mode. The player provided by the SDK is shielded. You need to process the audio output by the onAudioAvailable callback API. In this case, the playback-related APIs in the callback APIs become invalid, and only the callback APIs related to audio synthesis can be listened.
         */
        // Use the built-in player of the SDK to play speech in queuing mode.
        String id = mlTtsEngine.speak(text, MLTtsEngine.QUEUE_APPEND);
        // In queuing mode, the synthesized audio stream is output through onAudioAvailable, and the built-in player of the SDK is used to play the speech.
        // String id = mlTtsEngine.speak(text, MLTtsEngine.QUEUE_APPEND | MLTtsEngine.OPEN_STREAM);
        // In queuing mode, the synthesized audio stream is output through onAudioAvailable, and the audio stream is not played, but controlled by you.
        // String id = mlTtsEngine.speak(text, MLTtsEngine.QUEUE_APPEND | MLTtsEngine.OPEN_STREAM | MLTtsEngine.EXTERNAL_PLAYBACK);
    }

    /**
     * Downloading TTS Offline Models.
     *
     * @param person Speaker of the downloaded language model.
     */
    private void downloadModel(String person) {
        final MLTtsLocalModel model = new MLTtsLocalModel.Factory(person).create();
        MLModelDownloadStrategy request = new MLModelDownloadStrategy.Factory().create();

        MLModelDownloadListener modelDownloadListener = new MLModelDownloadListener() {
            @Override
            public void onProcess(long alreadyDownLength, long totalLength) {
                showProcess(alreadyDownLength, "Model download is complete", totalLength);
            }
        };
        manager.downloadModel(model, request, modelDownloadListener)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mlTtsEngine.updateConfig(mlTtsConfigs);
                        Log.i(TAG, "downloadModel: " + model.getModelName() + " success");
                        showToast("downloadModel Success");
                        speak(mEditText.getText().toString().trim());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "downloadModel failed: " + e.getMessage());
                        showToast(e.getMessage());
                    }
                });
    }

    private void showProcess(long alreadyDownLength, String buttonText, long totalLength) {
        double downDone = alreadyDownLength * 1.0 / M;
        double downTotal = totalLength * 1.0 / M;
        String downD = String.format(Locale.ROOT,"%.2f", downDone);
        String downT = String.format(Locale.ROOT,"%.2f", downTotal);

        String text = downD + "M" + "/" + downT + "M";
        updateButton(text, false);
        if (downD.equals(downT)) {
            showToast(buttonText);
            updateButton(buttonText, true);
        }
    }


    private void updateButton(final String text, final boolean downloadSuccess) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_download_progress.setText(text);
                if (!downloadSuccess) {
                    tv_download_progress.setVisibility(View.VISIBLE);
                } else {
                    tv_download_progress.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TtsOfflineModeActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mlTtsEngine != null) {
            this.mlTtsEngine.shutdown();
        }
    }
}
