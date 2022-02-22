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
import android.util.Pair;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.mlsdk.tts.MLTtsAudioFragment;
import com.huawei.hms.mlsdk.tts.MLTtsCallback;
import com.huawei.hms.mlsdk.tts.MLTtsConstants;
import com.huawei.hms.mlsdk.tts.MLTtsError;
import com.huawei.hms.mlsdk.tts.MLTtsWarn;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    protected static final String TAG = BaseActivity.class.getSimpleName();
    // use English language and female speaker,
    // see https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/ml-tts-0000001050068169 to get more language and speaker

    protected static final int HANDLE_CODE = 1;
    protected static final String HANDLE_KEY = "text";

    /**
     * TTS callback function.. If you want to use TTS,
     * you need to apply for an agconnect-services.json file in the developer
     * alliance(https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/ml-add-agc),
     * replacing the sample-agconnect-services.json in the project.
     */
    MLTtsCallback callback = new MLTtsCallback() {
        @Override
        public void onError(String taskId, MLTtsError err) {
            // Processing logic for TTS failure.
            String str = "TaskID: " + taskId + ", error:" + err;
            if (resultCallback != null) {
                resultCallback.displayResult(str);
            }
        }

        @Override
        public void onWarn(String taskId, MLTtsWarn warn) {
            // Alarm handling without affecting service logic.
            String str = "TaskID: " + taskId + ", warn:" + warn;
            if (resultCallback != null) {
                resultCallback.displayResult(str);
            }
        }

        @Override
        public void onRangeStart(String taskId, int start, int end) {
            // Process the mapping between the currently played segment and text.
            String str = "TaskID: " + taskId + ", onRangeStart [" + start + "，" + end + "]";
            if (resultCallback != null) {
                resultCallback.displayResult(str);
            }
        }

        @Override
        public void onAudioAvailable(String s, MLTtsAudioFragment mlTtsAudioFragment, int i, Pair<Integer, Integer> pair, Bundle bundle) {
            //  Audio stream callback API, which is used to return the synthesized audio data to the app.
            //  taskId: ID of an audio synthesis task corresponding to the audio.
            // audioFragment: audio data.
            // offset: offset of the audio segment to be transmitted in the queue. One audio synthesis task corresponds to an audio synthesis queue.
            //  range: text area where the audio segment to be transmitted is located; range.first (included): start position; range.second (excluded): end position.
        }

        @Override
        // Callback method of a TTS event. eventName: event name. The events are as follows:
        // MLTtsConstants.EVENT_PLAY_RESUME: playback resumption.
        // MLTtsConstants.EVENT_PLAY_PAUSE: playback pause.
        // MLTtsConstants.EVENT_PLAY_STOP: playback stop.
        public void onEvent(String taskId, int eventId, Bundle bundle) {
            String str = "TaskID: " + taskId + ", eventName:" + eventId;
            // Callback method of an audio synthesis event. eventId: event name.
            switch (eventId) {
                case MLTtsConstants.EVENT_PLAY_START:
                    //  Called when playback starts.
                    break;
                case MLTtsConstants.EVENT_PLAY_STOP:
                    // Called when playback stops.
                    boolean isInterrupted = bundle.getBoolean(MLTtsConstants.EVENT_PLAY_STOP_INTERRUPTED);
                    str += " " + isInterrupted;
                    break;
                case MLTtsConstants.EVENT_PLAY_RESUME:
                    //  Called when playback resumes.
                    break;
                case MLTtsConstants.EVENT_PLAY_PAUSE:
                    // Called when playback pauses.
                    break;

                /*//Pay attention to the following callback events when you focus on only synthesized audio data but do not use the internal player for playback:
                case MLTtsConstants.EVENT_SYNTHESIS_START:
                    //  Called when TTS starts.
                    break;
                case MLTtsConstants.EVENT_SYNTHESIS_END:
                    // Called when TTS ends.
                    break;
                case MLTtsConstants.EVENT_SYNTHESIS_COMPLETE:
                    // TTS is complete. All synthesized audio streams are passed to the app.
                    boolean isInterrupted = bundle.getBoolean(MLTtsConstants.EVENT_SYNTHESIS_INTERRUPTED);
                    break;*/
                default:
                    break;
            }
            if (resultCallback != null) {
                resultCallback.displayResult(str);
            }
        }
    };

    protected DisplayResultCallback resultCallback = null;

    public void setOnResultCallback(DisplayResultCallback resultCallback) {
        this.resultCallback = resultCallback;
    }

    public interface DisplayResultCallback {
        /** tts Result
         * 
         * @param str Result
         */
        void displayResult(String str);
    }
}
