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

package com.huawei.mlkit.example.text.generalCard;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.huawei.hms.mlsdk.text.MLText;

import java.util.ArrayList;
import java.util.List;

/**
 * Hong Kong Permanent Identity Card post-processing plug-in.
 *
 * @since 2020-12-16
 */
public class HKIdCardProcess {
    private static final String TAG = HKIdCardProcess.class.getSimpleName();

    private final MLText text;

    public HKIdCardProcess(MLText text) {
        this.text = text;
    }

    public UniversalCardResult getResult() {
        List<MLText.Block> blocks = this.text.getBlocks();
        if (blocks.isEmpty()) {
            Log.i(HKIdCardProcess.TAG, "HKIdCardProcess::getResult blocks is empty");
            return null;
        }

        ArrayList<BlockItem> originItems = this.getOriginItems(blocks);

        String valid = "";
        String number = "";
        boolean numberFlag = false;
        boolean validFlag = false;

        int location = 1;
        for (BlockItem item : originItems) {
            String tempStr = item.text;

            if (!validFlag && (originItems.size() - location) < 3) {
                String result = this.tryGetValidDate(tempStr);
                if (!result.isEmpty()) {
                    valid = result;
                    validFlag = true;
                }
            }

            if (!numberFlag) {
                String result = this.tryGetCardNumber(tempStr);
                if (!result.isEmpty()) {
                    number = result;
                    numberFlag = true;
                }
            }
            location++;
        }

        Log.i(HKIdCardProcess.TAG, "valid: " + valid);
        Log.i(HKIdCardProcess.TAG, "number: " + number);

        return new UniversalCardResult(valid, number);
    }

    private String tryGetValidDate(String originStr) {
        int[] formatter = {2, 2, 2};
        return Utils.getCorrectDate(originStr, "\\-", formatter);
    }

    private String tryGetCardNumber(String originStr) {
        return Utils.getHKIdCardNum(originStr);
    }

    private ArrayList<BlockItem> getOriginItems(List<MLText.Block> blocks) {
        ArrayList<BlockItem> originItems = new ArrayList<>();
        for (MLText.Block block : blocks) {
            List<MLText.TextLine> lines = block.getContents();
            for (MLText.TextLine line : lines) {
                String text = line.getStringValue();
                text = Utils.filterString(text, "[^a-zA-Z0-9\\.\\-,<\\(\\)\\s]");
                Log.d(HKIdCardProcess.TAG, "HKIdCardProcess text: " + text);
                Point[] points = line.getVertexes();
                Rect rect = new Rect(points[0].x, points[0].y, points[2].x, points[2].y);
                BlockItem item = new BlockItem(text, rect);
                originItems.add(item);
            }
        }
        return originItems;
    }
}
