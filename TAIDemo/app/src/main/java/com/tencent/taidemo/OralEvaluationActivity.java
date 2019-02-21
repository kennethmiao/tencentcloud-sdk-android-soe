package com.tencent.taidemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tencent.taisdk.TAIErrCode;
import com.tencent.taisdk.TAIError;
import com.tencent.taisdk.TAIOralEvaluation;
import com.tencent.taisdk.TAIOralEvaluationCallback;
import com.tencent.taisdk.TAIOralEvaluationData;
import com.tencent.taisdk.TAIOralEvaluationEvalMode;
import com.tencent.taisdk.TAIOralEvaluationFileType;
import com.tencent.taisdk.TAIOralEvaluationListener;
import com.tencent.taisdk.TAIOralEvaluationParam;
import com.tencent.taisdk.TAIOralEvaluationRet;
import com.tencent.taisdk.TAIOralEvaluationServerType;
import com.tencent.taisdk.TAIOralEvaluationStorageMode;
import com.tencent.taisdk.TAIOralEvaluationWorkMode;

import java.io.InputStream;


public class OralEvaluationActivity extends AppCompatActivity {
    private TAIOralEvaluation oral;
    private EditText refText;
    private TextView logText;
    private Button recordBtn;
    private Button localRecordBtn;
    private RadioButton workOnceBtn;
    private RadioButton workStreamBtn;
    private RadioButton evalWordBtn;
    private RadioButton evalSentenceBtn;
    private RadioButton evalParagraphBtn;
    private RadioButton evalFreeBtn;
    private RadioButton storageDisableBtn;
    private RadioButton storageEnableBtn;
    private RadioButton typeEnglishBtn;
    private RadioButton typeChineseBtn;
    private EditText scoreCoeff;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oralevaluation);
        this.refText = this.findViewById(R.id.refText);
        this.refText.setText("how are you");
        this.logText = this.findViewById(R.id.logText);
        this.recordBtn = this.findViewById(R.id.recordBtn);
        this.localRecordBtn = this.findViewById(R.id.localRecordBtn);
        this.workOnceBtn = this.findViewById(R.id.workOnceBtn);
        this.workStreamBtn = this.findViewById(R.id.workStreamBtn);
        this.workStreamBtn.setChecked(true);
        this.evalWordBtn = this.findViewById(R.id.evalWordBtn);
        this.evalSentenceBtn = this.findViewById(R.id.evalSentenceBtn);
        this.evalSentenceBtn.setChecked(true);
        this.evalParagraphBtn = this.findViewById(R.id.evalParagraphBtn);
        this.evalFreeBtn = this.findViewById(R.id.evalFreeBtn);
        this.storageDisableBtn = this.findViewById(R.id.storageDisable);
        this.storageEnableBtn = this.findViewById(R.id.storageEnable);
        this.storageDisableBtn.setChecked(true);
        this.typeEnglishBtn = this.findViewById(R.id.typeEnglish);
        this.typeChineseBtn = this.findViewById(R.id.typeChinese);
        this.typeEnglishBtn.setChecked(true);
        this.scoreCoeff = this.findViewById(R.id.scoreCoeff);
        this.scoreCoeff.setText("1.0");
        this.requestPermission();
    }


    public void onRecord(View view) {
        if(this.oral == null){
            this.oral = new TAIOralEvaluation();
            this.oral.setListener(new TAIOralEvaluationListener() {
                @Override
                public void onEvaluationData(final TAIOralEvaluationData data, final TAIOralEvaluationRet result, final TAIError error) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(error.code != TAIErrCode.SUCC){
                                OralEvaluationActivity.this.recordBtn.setText(R.string.start_record);
                            }
                            Gson gson = new Gson();
                            String errString = gson.toJson(error);
                            String retString = gson.toJson(result);
                            OralEvaluationActivity.this.setResponse(String.format("oralEvaluation:seq:%d, end:%d, error:%s, ret:%s", data.seqId, data.bEnd ? 1 : 0, errString, retString));
                        }
                    });
                }
            });
        }
        if(oral.isRecording()){
            this.oral.stopRecordAndEvaluation(new TAIOralEvaluationCallback() {
                @Override
                public void onResult(final TAIError error) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            String string = gson.toJson(error);
                            OralEvaluationActivity.this.setResponse(String.format("stopRecordAndEvaluation:%s", string));
                            OralEvaluationActivity.this.recordBtn.setText(R.string.start_record);
                        }
                    });
                }
            });
        }
        else{
            if(this.scoreCoeff.getText().toString().equals("")){
                this.setResponse("startRecordAndEvaluation:scoreCoeff invalid");
                return;
            }
            this.logText.setText("");
            TAIOralEvaluationParam param = new TAIOralEvaluationParam();
            param.context = this;
            param.sessionId = String.format("%d", System.currentTimeMillis() / 1000);
            param.appId = "1253488539";
            param.secretId = "AKIDq9MQI1PuGTUJvOQpmW8kFYihT2PZ9QJ6";
            param.secretKey = "jGeqqfHm7GDxYxBGD6tXzEHBtRn041mL";
            int evalMode = TAIOralEvaluationEvalMode.SENTENCE;
            if(this.evalWordBtn.isChecked()){
                evalMode = TAIOralEvaluationEvalMode.WORD;
            }
            else if(this.evalSentenceBtn.isChecked()){
                evalMode = TAIOralEvaluationEvalMode.SENTENCE;
            }
            else if(this.evalParagraphBtn.isChecked()){
                evalMode = TAIOralEvaluationEvalMode.PARAGRAPH;
            }
            else if(this.evalFreeBtn.isChecked()){
                evalMode = TAIOralEvaluationEvalMode.FREE;
            }
            param.workMode = this.workOnceBtn.isChecked() ? TAIOralEvaluationWorkMode.ONCE : TAIOralEvaluationWorkMode.STREAM;
            param.evalMode = evalMode;
            param.storageMode = this.storageDisableBtn.isChecked() ? TAIOralEvaluationStorageMode.DISABLE : TAIOralEvaluationStorageMode.ENABLE;
            param.fileType = TAIOralEvaluationFileType.MP3;
            param.serverType = this.typeChineseBtn.isChecked() ? TAIOralEvaluationServerType.CHINESE : TAIOralEvaluationServerType.ENGLISH;
            param.scoreCoeff = Double.parseDouble(this.scoreCoeff.getText().toString());
            param.refText = this.refText.getText().toString();
            this.oral.startRecordAndEvaluation(param, new TAIOralEvaluationCallback() {
                @Override
                public void onResult(final TAIError error) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(error.code == TAIErrCode.SUCC){
                                OralEvaluationActivity.this.recordBtn.setText(R.string.stop_record);
                            }
                            Gson gson = new Gson();
                            String string = gson.toJson(error);
                            OralEvaluationActivity.this.setResponse(String.format("startRecordAndEvaluation:%s", string));
                        }
                    });
                }
            });
        }
    }

    public void onLocalRecord(View view)
    {
        if(this.oral == null){
            this.oral = new TAIOralEvaluation();
            this.oral.setListener(new TAIOralEvaluationListener() {
                @Override
                public void onEvaluationData(final TAIOralEvaluationData data, final TAIOralEvaluationRet result, final TAIError error) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(error.code != TAIErrCode.SUCC){
                                OralEvaluationActivity.this.recordBtn.setText(R.string.start_record);
                            }
                            Gson gson = new Gson();
                            String errString = gson.toJson(error);
                            String retString = gson.toJson(result);
                            OralEvaluationActivity.this.setResponse(String.format("oralEvaluation:seq:%d, end:%d, error:%s, ret:%s", data.seqId, data.bEnd ? 1 : 0, errString, retString));
                        }
                    });
                }
            });
        }
        this.logText.setText("");
        TAIOralEvaluationParam param = new TAIOralEvaluationParam();
        param.context = this;
        param.sessionId = String.format("%d", System.currentTimeMillis() / 1000);
        param.appId = "1253488539";
        param.secretId = "AKIDq9MQI1PuGTUJvOQpmW8kFYihT2PZ9QJ6";
        param.secretKey = "jGeqqfHm7GDxYxBGD6tXzEHBtRn041mL";
        int evalMode = TAIOralEvaluationEvalMode.SENTENCE;
        if(this.evalWordBtn.isChecked()){
            evalMode = TAIOralEvaluationEvalMode.WORD;
        }
        else if(this.evalSentenceBtn.isChecked()){
            evalMode = TAIOralEvaluationEvalMode.SENTENCE;
        }
        else if(this.evalParagraphBtn.isChecked()){
            evalMode = TAIOralEvaluationEvalMode.PARAGRAPH;
        }
        else if(this.evalFreeBtn.isChecked()){
            evalMode = TAIOralEvaluationEvalMode.FREE;
        }
        param.workMode = this.workOnceBtn.isChecked() ? TAIOralEvaluationWorkMode.ONCE : TAIOralEvaluationWorkMode.STREAM;
        param.evalMode = evalMode;
        param.storageMode = this.storageDisableBtn.isChecked() ? TAIOralEvaluationStorageMode.DISABLE : TAIOralEvaluationStorageMode.ENABLE;
        param.fileType = TAIOralEvaluationFileType.MP3;
        param.serverType = this.typeChineseBtn.isChecked() ? TAIOralEvaluationServerType.CHINESE : TAIOralEvaluationServerType.ENGLISH;
        param.scoreCoeff = Double.parseDouble(this.scoreCoeff.getText().toString());
        param.refText = "hello guagua";


        try{
            InputStream is = getAssets().open("hello_guagua.mp3");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            TAIOralEvaluationData data = new TAIOralEvaluationData();
            data.seqId = 1;
            data.bEnd = true;
            data.audio = buffer;
            this.oral.oralEvaluation(param, data, new TAIOralEvaluationCallback() {
                @Override
                public void onResult(final TAIError error) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            String string = gson.toJson(error);
                            OralEvaluationActivity.this.setResponse(String.format("oralEvaluation:%s", string));
                        }
                    });
                }
            });
        }
        catch (Exception e){

        }


    }

    private void requestPermission()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1234);
        }
    }

    private void setResponse(String rsp)
    {
        String old = this.logText.getText().toString();
        this.logText.setText(String.format("%s\n%s", old, rsp));
    }
}
