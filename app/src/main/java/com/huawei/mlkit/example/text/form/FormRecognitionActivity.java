package com.huawei.mlkit.example.text.form;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzer;
import com.huawei.hms.mlsdk.fr.MLFormRecognitionAnalyzerFactory;
import com.huawei.hms.mlsdk.fr.MLFormRecognitionConstant;
import com.huawei.hms.mlsdk.fr.MLFormRecognitionTablesAttribute;
import com.huawei.mlkit.example.R;

import java.util.ArrayList;

public class FormRecognitionActivity extends AppCompatActivity {
    private static final String TAG = FormRecognitionActivity.class.getSimpleName();
    TextView form_result;
    Button form_detect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_recogintion);
        form_result = findViewById(R.id.form_result);
        form_detect = findViewById(R.id.form_detect);
        form_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analyzer();
            }
        });
    }

    private void analyzer() {
        // Get bitmap.
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.form_recognition);
        // Convert bitmap to MLFrame.
        MLFrame frame = MLFrame.fromBitmap(bitmap);
        // Create analyzer.
        MLFormRecognitionAnalyzer analyzer = MLFormRecognitionAnalyzerFactory.getInstance().getFormRecognitionAnalyzer();
        Task<JsonObject> task = analyzer.asyncAnalyseFrame(frame);

        task.addOnSuccessListener(new OnSuccessListener<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                if (jsonObject != null && jsonObject.get("retCode").getAsInt() == MLFormRecognitionConstant.SUCCESS) {
                    String str = jsonObject.toString();
                    form_result.setText(str);
                    try {
                        Gson gson = new Gson();
                        MLFormRecognitionTablesAttribute attribute = gson.fromJson(str, MLFormRecognitionTablesAttribute.class);
                        Log.d(TAG, "RetCode: " + attribute.getRetCode());
                        MLFormRecognitionTablesAttribute.TablesContent tablesContent = attribute.getTablesContent();
                        Log.d(TAG, "tableCount: " + tablesContent.getTableCount());
                        ArrayList<MLFormRecognitionTablesAttribute.TablesContent.TableAttribute> tableAttributeArrayList = tablesContent.getTableAttributes();
                        Log.d(TAG, "tableID: " + tableAttributeArrayList.get(0).getId());
                        ArrayList<MLFormRecognitionTablesAttribute.TablesContent.TableAttribute.TableCellAttribute> tableCellAttributes = tableAttributeArrayList.get(0).getTableCellAttributes();
                        for (int i = 0; i < tableCellAttributes.size(); i++) {
                            Log.d(TAG, "startRow: " + tableCellAttributes.get(i).getStartRow());
                            Log.d(TAG, "endRow: " + tableCellAttributes.get(i).getEndRow());
                            Log.d(TAG, "startCol: " + tableCellAttributes.get(i).getStartCol());
                            Log.d(TAG, "endCol: " + tableCellAttributes.get(i).getEndCol());
                            Log.d(TAG, "textInfo: " + tableCellAttributes.get(i).getTextInfo());
                            Log.d(TAG, "cellCoordinate: ");
                            MLFormRecognitionTablesAttribute.TablesContent.TableAttribute.TableCellAttribute.TableCellCoordinateAttribute coordinateAttribute = tableCellAttributes.get(i).getTableCellCoordinateAttribute();
                            Log.d(TAG, "topLeft_x: " + coordinateAttribute.getTopLeftX());
                            Log.d(TAG, "topLeft_y: " + coordinateAttribute.getTopLeftY());
                            Log.d(TAG, "topRight_x: " + coordinateAttribute.getTopRightX());
                            Log.d(TAG, "topRight_y: " + coordinateAttribute.getTopRightY());
                            Log.d(TAG, "bottomLeft_x: " + coordinateAttribute.getBottomLeftX());
                            Log.d(TAG, "bottomLeft_y: " + coordinateAttribute.getBottomLeftY());
                            Log.d(TAG, "bottomRight_x: " + coordinateAttribute.getBottomRightX());
                            Log.d(TAG, "bottomRight_y: " + coordinateAttribute.getBottomRightY());
                        }
                    } catch (RuntimeException e) {
                        Log.e(TAG, e.getMessage());
                    }
                } else if (jsonObject != null && jsonObject.get("retCode").getAsInt() == MLFormRecognitionConstant.FAILED) {
                    form_result.setText(getString(R.string.teble_error));
                }
            }
        }).addOnFailureListener(onFailureListener);
    }

    static OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(Exception e) {
            Log.e(TAG, e.getMessage());
        }
    };
}
