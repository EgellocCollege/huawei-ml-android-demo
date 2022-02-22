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

package com.huawei.mlkit.example.image.productvisionsearch;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huawei.mlkit.example.R;


public class BottomSheetAdapter extends BaseAdapter {

    private List<RealProductBean> mlProducts;
    private Context context;

    public BottomSheetAdapter(List<RealProductBean> mlProducts, Context context) {
        this.mlProducts = mlProducts;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mlProducts == null ? 0 : mlProducts.size();
    }

    @Override
    public Object getItem(int position) {
        return mlProducts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.adapter_item_product, null);
        }
        Glide.with(context).load(mlProducts.get(position).getImgUrl()).into((ImageView) convertView.findViewById(R.id.img));
        ((TextView) convertView.findViewById(R.id.tv)).setText(mlProducts.get(position).getName());
        return convertView;
    }
}