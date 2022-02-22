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

package com.huawei.mlkit.example.voice.asr;

import java.util.List;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.hms.mlplugin.asr.MLAsrCaptureActivity;
import com.huawei.hms.mlplugin.asr.MLAsrCaptureConstants;
import com.huawei.hms.mlsdk.asr.MLAsrConstants;
import com.huawei.hms.mlsdk.asr.MLAsrListener;
import com.huawei.hms.mlsdk.asr.MLAsrRecognizer;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.mlkit.example.ChooseActivity;
import com.huawei.mlkit.example.R;
import com.huawei.mlkit.example.voice.aft.AftAnalyseActivity;

public class AsrOnlineAnalyseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AsrOnlineAnalyseActivity.class.getSimpleName();
    private static final int HANDLE_CODE = 0;
    private static final String HANDLE_KEY = "text";
    private static final int AUDIO_PERMISSION_CODE = 1;
    private static final int ML_ASR_CAPTURE_CODE = 2;
    private TextView mTextView;
    private MLAsrRecognizer mSpeechRecognizer;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_voice_asr_online_mode);
        this.mTextView = this.findViewById(R.id.textView);
        findViewById(R.id.voice_input).setOnClickListener(this);
        findViewById(R.id.no_voice_input).setOnClickListener(this);
        findViewById(R.id.btn_query_languages).setOnClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            this.requestCameraPermission();
        }
        // Set ApiKey.
        MLApplication.getInstance().setApiKey(ChooseActivity.apiKey);
        // Set access token.
        // MLApplication.getInstance().setAccessToken(MainActivity.accessToken);
    }

    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(this, permissions, AsrOnlineAnalyseActivity.AUDIO_PERMISSION_CODE);
            return;
        }
    }

    // Permission application callback.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        if (requestCode != AsrOnlineAnalyseActivity.AUDIO_PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
    }

    private void displayResult(String str) {
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString(HANDLE_KEY, str);
        msg.setData(data);
        msg.what = HANDLE_CODE;
        handler.sendMessage(msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // If you want to use ASR, you need to apply for an agconnect-services.json file in the developer
            // alliance(https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/ml-add-agc),
            // replacing the agconnect-services.json in the project.
            case R.id.voice_input:
                // Use Intent for recognition settings.
                Intent intentPlugin = new Intent(this, MLAsrCaptureActivity.class)
                        // Set the language that can be recognized to English. If this parameter is not set,
                        // English is recognized by default. Example: "zh-CN": Chinese；"en-US": English；"fr-FR": French；"es-ES": Spanish；"de-DE": German；"it-IT": Italian。
                        .putExtra(MLAsrCaptureConstants.LANGUAGE, "en-US")
                        // Set whether to display text on the speech pickup UI. MLAsrCaptureConstants.FEATURE_ALLINONE: no;
                        // MLAsrCaptureConstants.FEATURE_WORDFLUX: yes.
                        .putExtra(MLAsrCaptureConstants.FEATURE, MLAsrCaptureConstants.FEATURE_WORDFLUX);
                        // Set the usage scenario to shopping,Currently, only Chinese scenarios are supported.
                    // .putExtra(MLAsrConstants.SCENES_KEY, MLAsrConstants.SCENES_SHOPPING);
                // ML_ASR_CAPTURE_CODE: request code between the current activity and speech pickup UI activity.
                // You can use this code to obtain the processing result of the speech pickup UI.
                startActivityForResult(intentPlugin, ML_ASR_CAPTURE_CODE);
                break;
            case R.id.no_voice_input:
                // Call an API to create a speech recognizer.
                mSpeechRecognizer = MLAsrRecognizer.createAsrRecognizer(this);
                // Set the ASR result listener callback. You can obtain the ASR result or result code from the listener.
                mSpeechRecognizer.setAsrListener(new SpeechRecognitionListener());
                // Set parameters and start the audio device.
                Intent intentSdk = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        // Set the language that can be recognized to English. If this parameter is not set,
                        // English is recognized by default. Example: "zh-CN": Chinese；"en-US": English；"fr-FR": French；"es-ES": Spanish；"de-DE": German；"it-IT": Italian。
                        .putExtra(MLAsrCaptureConstants.LANGUAGE, "en-US")
                        // Set to return the recognition result along with the speech. If you ignore the setting, this mode is used by default. Options are as follows:
                        // MLAsrConstants.FEATURE_WORDFLUX: Recognizes and returns texts through onRecognizingResults.
                        // MLAsrConstants.FEATURE_ALLINONE: After the recognition is complete, texts are returned through onResults.
                        .putExtra(MLAsrConstants.FEATURE, MLAsrConstants.FEATURE_ALLINONE);
                        // Set the usage scenario to shopping,Currently, only Chinese scenarios are supported.
                    // .putExtra(MLAsrConstants.SCENES_KEY, MLAsrConstants.SCENES_SHOPPING);
                // Start speech recognition.
                mSpeechRecognizer.startRecognizing(intentSdk);
                mTextView.setText("Ready to speak.");
                break;
            case R.id.btn_query_languages:
                MLAsrRecognizer.createAsrRecognizer(this).getLanguages(new MLAsrRecognizer.LanguageCallback() {
                    @Override
                    public void onResult(List<String> result) {
                        Toast.makeText(AsrOnlineAnalyseActivity.this,result.toString(),Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String text = "";
        if (null == data) {
            displayResult("Intent data is null.");
        }
        // ML_ASR_CAPTURE_CODE: request code between the current activity and speech pickup UI activity.
        if (requestCode == ML_ASR_CAPTURE_CODE) {
            if (data == null) {
                return;
            }
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }

            switch (resultCode) {
                // MLAsrCaptureConstants.ASR_SUCCESS: Recognition is successful.
                case MLAsrCaptureConstants.ASR_SUCCESS:
                    // Obtain the text information recognized from speech.
                    if (bundle.containsKey(MLAsrCaptureConstants.ASR_RESULT)) {
                        text = bundle.getString(MLAsrCaptureConstants.ASR_RESULT);
                    }
                    if (text == null || "".equals(text)) {
                        text = "Result is null.";
                    }
                    // Process the recognized text information.
                    displayResult(text);
                    break;
                // MLAsrCaptureConstants.ASR_FAILURE: Recognition fails.
                case MLAsrCaptureConstants.ASR_FAILURE:
                    // Check whether a result code is contained.
                    if (bundle.containsKey(MLAsrCaptureConstants.ASR_ERROR_CODE)) {
                        text = text + bundle.getInt(MLAsrCaptureConstants.ASR_ERROR_CODE);
                        // Perform troubleshooting based on the result code.
                    }
                    // Check whether error information is contained.
                    if (bundle.containsKey(MLAsrCaptureConstants.ASR_ERROR_MESSAGE)) {
                        String errorMsg = bundle.getString(MLAsrCaptureConstants.ASR_ERROR_MESSAGE);
                        // Perform troubleshooting based on the error information.
                        if (errorMsg != null && !"".equals(errorMsg)) {
                            text = "[" + text + "]" + errorMsg;
                        }
                    }
                    // Check whether a sub-result code is contained.
                    if (bundle.containsKey(MLAsrCaptureConstants.ASR_SUB_ERROR_CODE)) {
                        int subErrorCode = bundle.getInt(MLAsrCaptureConstants.ASR_SUB_ERROR_CODE);
                        // Process the sub-result code.
                        text = "[" + text + "]" + subErrorCode;
                    }

                    displayResult(text);
                    break;
                default:
                    displayResult("Failure.");
                    break;
            }
        }

    }

    // Use the callback to implement the MLAsrListener API and methods in the API.
    class SpeechRecognitionListener implements MLAsrListener {
        @Override
        public void onStartingOfSpeech() {
            mTextView.setText("Speaking...");
            // The user starts to speak, that is, the speech recognizer detects that the user starts to speak.
        }

        @Override
        public void onVoiceDataReceived(byte[] data, float energy, Bundle bundle) {
            // Return the original PCM stream and audio power to the user.
        }

        @Override
        public void onState(int i, Bundle bundle) {
            // Notify the app status change.
        }

        @Override
        public void onRecognizingResults(Bundle partialResults) {
            // Receive the recognized text from MLAsrRecognizer.
            mTextView.setText(partialResults.getString(MLAsrRecognizer.RESULTS_RECOGNIZING));
        }

        @Override
        public void onResults(Bundle results) {
            // Text data of ASR.
            mTextView.setText(results.getString(MLAsrRecognizer.RESULTS_RECOGNIZED));
        }

        @Override
        public void onError(int error, String errorMessage) {
            // Called when an error occurs in recognition.
            mTextView.setText(error + errorMessage);
        }

        @Override
        public void onStartListening() {
            // The recorder starts to receive speech.
            mTextView.setText("The recorder starts to receive speech.");
        }
    }
}



