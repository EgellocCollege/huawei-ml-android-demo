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

package com.huawei.mlkit.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.huawei.mlkit.example.body.BodyActivity;
import com.huawei.mlkit.example.image.ImageActivity;
import com.huawei.mlkit.example.language.LanguageActivity;
import com.huawei.mlkit.example.text.TextActivity;
import com.huawei.mlkit.example.voice.VoiceActivity;
import com.huawei.mlkit.example.custommodel.CustomModelActivity;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseActivity extends AppCompatActivity implements View.OnClickListener {
    // Your ApiKey, please see：https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/sdk-data-security-0000001050040129.
    public static final String apiKey = "your ApiKey";
    // Your access token, please see: https://developer.huawei.com/consumer/en/doc/HMSCore-Guides/open-platform-oauth-0000001050123437-V5.
    public static final String accessToken = "your access token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_choose);
        this.findViewById(R.id.btn_body).setOnClickListener(this);
        this.findViewById(R.id.btn_language).setOnClickListener(this);
        this.findViewById(R.id.btn_text).setOnClickListener(this);
        this.findViewById(R.id.btn_voice).setOnClickListener(this);
        this.findViewById(R.id.btn_custom_model).setOnClickListener(this);
        this.findViewById(R.id.btn_image).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_body:
                this.startActivity(new Intent(ChooseActivity.this, BodyActivity.class));
                break;
            case R.id.btn_language:
                this.startActivity(new Intent(ChooseActivity.this, LanguageActivity.class));
                break;
            case R.id.btn_text:
                this.startActivity(new Intent(ChooseActivity.this, TextActivity.class));
                break;
            case R.id.btn_voice:
                this.startActivity(new Intent(ChooseActivity.this, VoiceActivity.class));
                break;
            case R.id.btn_custom_model:
                this.startActivity(new Intent(ChooseActivity.this, CustomModelActivity.class));
                break;
            case R.id.btn_image:
                this.startActivity(new Intent(ChooseActivity.this, ImageActivity.class));
                break;
            default:
                break;
        }
    }
}
