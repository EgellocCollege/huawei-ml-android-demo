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

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hms.mlplugin.productvisionsearch.MLProductVisionSearchCapture;
import com.huawei.hms.mlsdk.productvisionsearch.MLProductVisionSearch;
import com.huawei.hms.mlsdk.productvisionsearch.MLVisionSearchProduct;
import com.huawei.mlkit.example.R;


public class ProductFragment extends MLProductVisionSearchCapture.AbstractProductFragment<RealProductBean> {
    private View root;

    private List<RealProductBean> mlProducts = new ArrayList<>();

    private GridView gridView;

    private BottomSheetAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        root = View.inflate(getContext(), R.layout.fragment_product, null);
        initView();
        return root;
    }

    private void initView() {
        gridView = root.findViewById(R.id.gv);
        gridView.setNumColumns(2);
        adapter = new BottomSheetAdapter(mlProducts, getContext());
        root.findViewById(R.id.img_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        gridView.setAdapter(adapter);
    }

    @Override
    public List<RealProductBean> getProductList(List<MLProductVisionSearch> list) throws Exception {
        return mLProductVisionSearchToTestBean(list);
    }

    @Override
    public void onResult(List<RealProductBean> productList) {

        if (null == productList) {
            return;
        }
        mlProducts.clear();
        mlProducts.addAll(productList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onError(Exception e) {
        return false;
    }

    private List<RealProductBean> mLProductVisionSearchToTestBean(List<MLProductVisionSearch> list) {
        List<RealProductBean> productBeans = new ArrayList<>();
        for (MLProductVisionSearch mlProductVisionSearch : list) {
            for (MLVisionSearchProduct product : mlProductVisionSearch.getProductList()) {
                RealProductBean productBean = new RealProductBean();
                productBean.setImgUrl(product.getImageList().get(0).getImageId());
                productBean.setId(product.getProductId());
                productBean.setName(product.getProductId());
                productBeans.add(productBean);
            }
        }
        return productBeans;
    }
}
