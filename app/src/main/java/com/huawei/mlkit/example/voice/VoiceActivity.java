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

package com.huawei.mlkit.example.voice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.mlkit.example.R;
import com.huawei.mlkit.example.voice.aft.AftAnalyseActivity;
import com.huawei.mlkit.example.voice.asr.AsrAnalyseActivity;
import com.huawei.mlkit.example.voice.sounddect.SoundDectActivity;
import com.huawei.mlkit.example.voice.speechrtt.RealTimeTranscriptionActivity;
import com.huawei.mlkit.example.voice.translate.TranslatorActivity;
import com.huawei.mlkit.example.voice.tts.TtsAnalyseActivity;

public class VoiceActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main_voice);
        this.findViewById(R.id.btn_translate).setOnClickListener(this);
        this.findViewById(R.id.btn_tts).setOnClickListener(this);
        this.findViewById(R.id.btn_asr).setOnClickListener(this);
        this.findViewById(R.id.btn_aft).setOnClickListener(this);
        this.findViewById(R.id.btn_soundDect).setOnClickListener(this);
        this.findViewById(R.id.btn_speechrtt).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_translate:
                this.startActivity(new Intent(VoiceActivity.this, TranslatorActivity.class));
                break;
            case R.id.btn_tts:
                this.startActivity(new Intent(VoiceActivity.this, TtsAnalyseActivity.class));
                break;
            case R.id.btn_asr:
                this.startActivity(new Intent(VoiceActivity.this, AsrAnalyseActivity.class));
                break;
            case R.id.btn_aft:
                this.startActivity(new Intent(VoiceActivity.this, AftAnalyseActivity.class));
                break;
            case R.id.btn_soundDect:
                this.startActivity(new Intent(VoiceActivity.this, SoundDectActivity.class));
                break;
            case R.id.btn_speechrtt:
                this.startActivity(new Intent(VoiceActivity.this, RealTimeTranscriptionActivity.class));
                break;
            default:
                break;
        }
    }
}
