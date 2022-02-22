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
package com.huawei.mlkit.example.language.textembedding;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.ml.common.utils.SmartLog;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.textembedding.MLTextEmbeddingAnalyzer;
import com.huawei.hms.mlsdk.textembedding.MLTextEmbeddingAnalyzerFactory;
import com.huawei.hms.mlsdk.textembedding.MLTextEmbeddingSetting;
import com.huawei.hms.mlsdk.textembedding.MLTextEmbeddingException;
import com.huawei.hms.mlsdk.textembedding.MLVocabularyVersion;
import com.huawei.mlkit.example.ChooseActivity;
import com.huawei.mlkit.example.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Integer.parseInt;

public class TextEmbeddingAnalyseActivity extends AppCompatActivity {
    private static final String PATH = "/sdcard/DCIM/TextEmbedding";

    private static final String TAG = TextEmbeddingAnalyseActivity.class.getSimpleName();

    private MLTextEmbeddingAnalyzer analyzer;

    private static final int PERMISSION_CODE = 0x01 << 8;

    private static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};
    private boolean mkdirs;
    private boolean newFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_embedding_analyse);

        final EditText et_Word2v = findViewById(R.id.et_word2v);
        final EditText et_Sentence2v = findViewById(R.id.et_sentence2v);

        final EditText et1_Word2s = findViewById(R.id.et1_word2s);
        final EditText et2_Word2s = findViewById(R.id.et2_word2s);
        final TextView tv_Word2s = findViewById(R.id.tv_word2s);

        final EditText et1_Sentence2s = findViewById(R.id.et1_sentence2s);
        final EditText et2_Sentence2s = findViewById(R.id.et2_sentence2s);
        final TextView tv_Sentence2s = findViewById(R.id.tv_sentence2s);

        final EditText et_Word_For_Simil = findViewById(R.id.et_word_for_simil);
        final EditText et_Num_For_Simil = findViewById(R.id.et_num_for_simil);
        final TextView tv_Simil_Words = findViewById(R.id.tv_simil_words);

        final TextView tv_Dic_Info = findViewById(R.id.tv_dic_info);

        // Set ApiKey.
        MLApplication.getInstance().setApiKey(ChooseActivity.apiKey);
        // Set access token.
        // MLApplication.getInstance().setAccessToken(MainActivity.accessToken);

        ActivityCompat.requestPermissions(TextEmbeddingAnalyseActivity.this, PERMISSIONS, PERMISSION_CODE);

        this.createTextEmbeddingAnalyzer();

        //Query dictionary version information
        findViewById(R.id.bt_dic_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Task<MLVocabularyVersion> vocabularyVersionTask = analyzer.getVocabularyVersion();
                vocabularyVersionTask.addOnSuccessListener(new OnSuccessListener<MLVocabularyVersion>() {
                    @Override
                    public void onSuccess(MLVocabularyVersion dictionaryVersionVo) {
                        tv_Dic_Info.setText(new Gson().toJson(dictionaryVersionVo));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e instanceof MLTextEmbeddingException) {
                            MLTextEmbeddingException embeddingException = (MLTextEmbeddingException) e;
                            embeddingException.getErrCode();
                            embeddingException.getMessage();
                            Toast.makeText(TextEmbeddingAnalyseActivity.this, embeddingException.getMessage() + ":" + embeddingException.getErrCode(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }
        });

        // Query vector
        findViewById(R.id.bt_word2v).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(TextEmbeddingAnalyseActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(TextEmbeddingAnalyseActivity.this, "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
                    return;

                }
                Task<Float[]> wordVectorTask = analyzer.analyseWordVector(et_Word2v.getText().toString());
                Log.d(TAG, et_Word2v.getText().toString());
                wordVectorTask.addOnSuccessListener(new OnSuccessListener<Float[]>() {
                    @Override
                    public void onSuccess(Float[] wordVector) {
                        Toast.makeText(TextEmbeddingAnalyseActivity.this, "analyseWordVector successed", Toast.LENGTH_SHORT).show();
                        try {
                            JSONArray jsonObject = new JSONArray(wordVector);
                            String result = "word： " + et_Word2v.getText().toString() + "\n" + "\n" + "Vector value： " + jsonObject.toString() + "\n\n";
                            writeTxtToFile(result, true);
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e instanceof MLTextEmbeddingException) {
                            MLTextEmbeddingException embeddingException = (MLTextEmbeddingException) e;
                            embeddingException.getErrCode();
                            embeddingException.getMessage();
                            Toast.makeText(TextEmbeddingAnalyseActivity.this, embeddingException.getMessage() + ":" + embeddingException.getErrCode(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });

                return;
            }
        });

        final EditText et_Words2v = findViewById(R.id.et_words2v);
        findViewById(R.id.bt_words2v).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String words = et_Words2v.getText().toString();
                final Set<String> stringSet = new HashSet<String>();
                if (!words.isEmpty()) {
                    List<String> stringList = Arrays.asList(words.split(","));
                    stringSet.addAll(stringList);
                }

                // Querying Word Vectors in Batches.
                Task<Map<String, Float[]>> wordVectorBatchTask = analyzer.analyseWordVectorBatch(stringSet);
                SmartLog.d(TAG, et_Words2v.getText().toString());
                wordVectorBatchTask.addOnSuccessListener(new OnSuccessListener<Map<String, Float[]>>() {
                    @Override
                    public void onSuccess(Map<String, Float[]> wordsVector) {
                        // Identification success processing (Map is HashMap, key is the search word, and value is the vector result).
                        for (String word : stringSet) {
                            // vector is the word vector corresponding to word.
                            Float[] vector = wordsVector.get(word);
                            if (vector.length == 0) {
                                // The word vector corresponding to word is not found.
                                continue;
                            }
                            // Processing vector results.
                        }

                        Toast.makeText(TextEmbeddingAnalyseActivity.this, "analyseWordsVector successed", Toast.LENGTH_SHORT).show();
                        JSONObject jsonObject = new JSONObject(wordsVector);
                        String result = "Words： " + et_Words2v.getText().toString() + "\n" + "Vector value：" + jsonObject.toString() + "\n\n";
                        writeTxtToFile(result, true);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e instanceof MLTextEmbeddingException) {
                            MLTextEmbeddingException embeddingException = (MLTextEmbeddingException) e;
                            embeddingException.getErrCode();
                            embeddingException.getMessage();
                            Toast.makeText(TextEmbeddingAnalyseActivity.this, embeddingException.getMessage() + ":" + embeddingException.getErrCode(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });

                return;
            }
        });


        // Query vector
        findViewById(R.id.bt_sentence2v).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(TextEmbeddingAnalyseActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(TextEmbeddingAnalyseActivity.this, "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
                    return;

                }
                Task<Float[]> sentenceVectorTask = analyzer.analyseSentenceVector(et_Sentence2v.getText().toString());
                sentenceVectorTask.addOnSuccessListener(new OnSuccessListener<Float[]>() {
                    @Override
                    public void onSuccess(Float[] sentenceVector) {
                        Toast.makeText(TextEmbeddingAnalyseActivity.this, "analyseSentenceVector successed", Toast.LENGTH_SHORT).show();
                        try {
                            JSONArray jsonObject = new JSONArray(sentenceVector);
                            String result = "sentence： " + et_Sentence2v.getText().toString() + "\n" + "Vector value： " + jsonObject.toString() + "\n\n";

                            writeTxtToFile(result, true);
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e instanceof MLTextEmbeddingException) {
                            MLTextEmbeddingException embeddingException = (MLTextEmbeddingException) e;
                            embeddingException.getErrCode();
                            embeddingException.getMessage();
                            Toast.makeText(TextEmbeddingAnalyseActivity.this, embeddingException.getMessage() + ":" + embeddingException.getErrCode(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });

            }
        });

        // Query the similarity of two words
        findViewById(R.id.bt_word2s).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Task<Float> wordsSimilarityTask = analyzer.analyseWordsSimilarity(et1_Word2s.getText().toString(), et2_Word2s.getText().toString());
                wordsSimilarityTask.addOnSuccessListener(new OnSuccessListener<Float>() {
                    @Override
                    public void onSuccess(Float wordsSimilarity) {
                        Toast.makeText(TextEmbeddingAnalyseActivity.this, "analyseWordsSimilarity successed", Toast.LENGTH_SHORT).show();
                        tv_Word2s.setText(wordsSimilarity + "");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e instanceof MLTextEmbeddingException) {
                            MLTextEmbeddingException embeddingException = (MLTextEmbeddingException) e;
                            embeddingException.getErrCode();
                            embeddingException.getMessage();
                            Toast.makeText(TextEmbeddingAnalyseActivity.this, embeddingException.getMessage() + ":" + embeddingException.getErrCode(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }
        });

        // Query the similarity of two sentences
        findViewById(R.id.bt_sentence2s).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Task<Float> sentencesSimilarityTask = analyzer.analyseSentencesSimilarity(et1_Sentence2s.getText().toString(), et2_Sentence2s.getText().toString());
                sentencesSimilarityTask.addOnSuccessListener(new OnSuccessListener<Float>() {
                    @Override
                    public void onSuccess(Float sentencesSimilarity) {
                        Toast.makeText(TextEmbeddingAnalyseActivity.this, "analyseSentencesSimilarity successed", Toast.LENGTH_SHORT).show();
                        tv_Sentence2s.setText(sentencesSimilarity + "");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e instanceof MLTextEmbeddingException) {
                            MLTextEmbeddingException embeddingException = (MLTextEmbeddingException) e;
                            embeddingException.getErrCode();
                            embeddingException.getMessage();
                            Toast.makeText(TextEmbeddingAnalyseActivity.this, embeddingException.getMessage() + ":" + embeddingException.getErrCode(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }
        });

        // Query multiple similar words
        findViewById(R.id.bt_word_for_simil).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Task<List<String>> multipleSimilarityWordsTask = analyzer.analyseSimilarWords(et_Word_For_Simil.getText().toString(), parseInt(et_Num_For_Simil.getText().toString()));
                multipleSimilarityWordsTask.addOnSuccessListener(new OnSuccessListener<List<String>>() {
                    @Override
                    public void onSuccess(List<String> words) {
                        Toast.makeText(TextEmbeddingAnalyseActivity.this, "analyseSimilarWords successed", Toast.LENGTH_SHORT).show();
                        JSONArray jsonObject = new JSONArray(words);
                        tv_Simil_Words.setText(jsonObject.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        if (e instanceof MLTextEmbeddingException) {
                            MLTextEmbeddingException embeddingException = (MLTextEmbeddingException) e;
                            embeddingException.getErrCode();
                            embeddingException.getMessage();
                            Toast.makeText(TextEmbeddingAnalyseActivity.this, embeddingException.getMessage() + ":" + embeddingException.getErrCode(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
            }
        });

    }

    private void createTextEmbeddingAnalyzer() {
        MLTextEmbeddingSetting setting = new MLTextEmbeddingSetting.Factory()
                .setLanguage(MLTextEmbeddingSetting.LANGUAGE_EN)
                .create();
        this.analyzer = MLTextEmbeddingAnalyzerFactory.getInstance().getMLTextEmbeddingAnalyzer(setting);
    }

    public void writeTxtToFile(String buffer, boolean append) {
        Log.i(TAG, "writeTxtToFile strFilePath =" + PATH);
        RandomAccessFile raf = null;
        FileOutputStream out = null;
        try {

            File dir = new File(PATH);
            if (!dir.exists()) {
                mkdirs = dir.mkdirs();
            }
            File file = new File(PATH + "/" + "Result.txt");
            if (!file.exists()) {
                newFile = file.createNewFile();
            }

            if (append) {
                raf = new RandomAccessFile(file, "rw");
                raf.seek(file.length());
                raf.write(buffer.getBytes());
            } else {
                try {
                    out = new FileOutputStream(file);
                    out.write(buffer.getBytes());
                    out.flush();
                }catch (IOException e){
                    Log.e(TAG, e.getMessage());
                }

            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult ");
        if (requestCode == PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            finish();
        }
    }
}
