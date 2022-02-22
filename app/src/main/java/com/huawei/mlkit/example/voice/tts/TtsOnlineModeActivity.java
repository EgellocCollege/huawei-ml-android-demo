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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hms.mlsdk.tts.MLTtsConfig;
import com.huawei.hms.mlsdk.tts.MLTtsConstants;
import com.huawei.hms.mlsdk.tts.MLTtsEngine;
import com.huawei.hms.mlsdk.tts.MLTtsSpeaker;
import com.huawei.mlkit.example.R;

import java.util.List;

public class TtsOnlineModeActivity extends BaseActivity {
    private EditText mEditText;
    private TextView mTextView;

    private List<MLTtsSpeaker> list;

    MLTtsEngine mlTtsEngine;
    MLTtsConfig mlConfigs;

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
        setContentView(R.layout.activity_tts_online_mode);
        findViewById(R.id.btn_speak).setOnClickListener(this);
        findViewById(R.id.btn_stop_speak).setOnClickListener(this);
        findViewById(R.id.close).setOnClickListener(this);
        mEditText = findViewById(R.id.edit_input);
        mTextView = findViewById(R.id.textView);

        // Method 1: Use the default parameter settings to create a TTS engine.
        // In the default settings, the source language is Chinese, the Chinese female voice is used,
        // the voice speed is 1.0 (1x), and the volume is 1.0 (1x).
        // MLTtsConfig mlConfigs = new MLTtsConfig();
        // Method 2: Use customized parameter settings to create a TTS engine.
        mlConfigs = new MLTtsConfig()
                // Setting the language for synthesis.
                .setLanguage(MLTtsConstants.TTS_EN_US)
                // Set the timbre.
                .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN)
                // Set the speech speed. Range: 0.2–4.0 1.0 indicates 1x speed.
                .setSpeed(1.0f)
                // Set the volume. Range: 0.2–4.0 1.0 indicates 1x volume.
                .setVolume(1.0f)
                // set the synthesis mode.
                .setSynthesizeMode(MLTtsConstants.TTS_ONLINE_MODE);

        mlTtsEngine = new MLTtsEngine(mlConfigs);
        //Sets the volume of the built-in player.
        mlTtsEngine.setPlayerVolume(20);
        // Pass the TTS callback to the TTS engine.
        mlTtsEngine.setTtsCallback(callback);

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
                String text = mEditText.getText().toString();
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
                break;
            case R.id.btn_stop_speak:
                mlTtsEngine.stop();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mlTtsEngine != null) {
            this.mlTtsEngine.shutdown();
        }
    }
}
