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

package com.huawei.mlkit.example.text;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.mlkit.example.R;
import com.huawei.mlkit.example.text.IDCard.IcrAnalyseActivity;
import com.huawei.mlkit.example.text.IDCard.IcrVnAnalyseActivity;
import com.huawei.mlkit.example.text.bankCard.BcrAnalyseActivity;
import com.huawei.mlkit.example.text.document.ImageDocumentAnalyseActivity;
import com.huawei.mlkit.example.text.form.FormRecognitionActivity;
import com.huawei.mlkit.example.text.generalCard.GcrAnalyseActivity;
import com.huawei.mlkit.example.text.text.ImageTextAnalyseActivity;

public class TextActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main_text);
        this.findViewById(R.id.btn_formRecoginition).setOnClickListener(this);
        this.findViewById(R.id.btn_text).setOnClickListener(this);
        this.findViewById(R.id.btn_document).setOnClickListener(this);
        this.findViewById(R.id.btn_icr).setOnClickListener(this);
        this.findViewById(R.id.btn_gcr).setOnClickListener(this);
        this.findViewById(R.id.btn_bcr).setOnClickListener(this);
        this.findViewById(R.id.btn_icr_vn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_formRecoginition:
                this.startActivity(new Intent(TextActivity.this, FormRecognitionActivity.class));
                break;
            case R.id.btn_text:
                this.startActivity(new Intent(TextActivity.this, ImageTextAnalyseActivity.class));
                break;
            case R.id.btn_document:
                this.startActivity(new Intent(TextActivity.this, ImageDocumentAnalyseActivity.class));
                break;
            case R.id.btn_icr:
                this.startActivity(new Intent(TextActivity.this, IcrAnalyseActivity.class));
                break;
            case R.id.btn_gcr:
                this.startActivity(new Intent(TextActivity.this, GcrAnalyseActivity.class));
                break;
            case R.id.btn_bcr:
                this.startActivity(new Intent(TextActivity.this, BcrAnalyseActivity.class));
                break;
            case R.id.btn_icr_vn:
                this.startActivity(new Intent(TextActivity.this, IcrVnAnalyseActivity.class));
                break;
            default:
                break;
        }
    }
}
