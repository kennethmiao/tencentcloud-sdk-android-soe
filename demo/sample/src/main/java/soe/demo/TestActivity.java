package soe.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.cloud.soe.TencentSOE;
import com.tencent.cloud.soe.model.InitOralProcessResponse;
import com.tencent.cloud.soe.model.SOECallback;
import com.tencent.cloud.soe.model.SOEError;
import com.tencent.cloud.soe.model.TransmitOralProcessResponse;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tencent.cloud.soe.TencentSOE.WORK_MODE_STREAM;

public class TestActivity extends Activity {
    /**
     * SecretId 可在腾讯云官网查询
     */
    private final String TAG = TestActivity.class.getSimpleName();
    private static final String SECRET_ID = "";
    /**
     * SecretKey 可在腾讯云官网查询
     */
    private static final String SECRET_KEY = "";

    private static final int MSG_INIT_OK = 0x101;// 初始化成功
    private static final int MSG_INIT_ERROR = 0x102;// 初始化失败

    private static final int MSG_TRANSMIT_OK = 0x201;// 传输成功
    private static final int MSG_TRANSMIT_ERROR = 0x202;// 传输失败

    private static final int MSG_ERROR = 0x300;// 其他错误

    private Handler mMyHandler = new Handler(new HandlerCallback());

    class HandlerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT_OK:
                    tvLog.setText(msg.obj.toString());
                    toast("初始化成功");
                    break;
                case MSG_INIT_ERROR:
                    toast(msg.obj.toString());
                    btFrameRecorder.setText("开始分片录音");
                    break;
                case MSG_TRANSMIT_OK:
                    tvLog.setText(msg.obj.toString());
                    int index = msg.arg1;
                    int isEnd = msg.arg2;
                    toast(index + (isEnd == 1 ? " - 已完成" : " - 未完成"));
                    break;
                case MSG_TRANSMIT_ERROR:
                    toast(msg.obj.toString());
                    break;
                case MSG_ERROR:
                    toast(msg.obj.toString());
                    break;
            }
            return true;
        }
    }

    @BindView(R.id.path)
    TextView tvPath;
    @BindView(R.id.edit)
    EditText etRefText;
    @BindView(R.id.recorder)
    Button btRecorder;
    @BindView(R.id.radio1)
    RadioGroup rgEvalMode;
    @BindView(R.id.radio2)
    RadioGroup rgFileType;
    @BindView(R.id.log)
    TextView tvLog;
    @BindView(R.id.frame_recorder)
    Button btFrameRecorder;

    private Context mContext;
    /**
     * 测试文件
     */
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SOE/soe.mp3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
        mContext = this;
        rgEvalMode.setOnCheckedChangeListener(radioListener);
        rgFileType.setOnCheckedChangeListener(radioListener);
        tvPath.setText(filePath);
        etRefText.setText("How are you,Nice to meet you!");
        getPersimmions();
    }

    public void getPersimmions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    requestPermissions(new String[]{
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    }, 10);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doExecuteStream() {
        try {
            String RefText = etRefText.getText().toString();
            if (filePath.endsWith(".mp3")) {// 只有MP3需要校验
                try {
                    TencentSOE.checkMP3Format(filePath);
                } catch (Exception e) {
                    sendMessage(MSG_ERROR, e.getMessage());
                    return;
                }
            }
            ArrayList<String> base64StringArray = TencentSOE.encodeAudioFile(filePath, 512 * 1024);
            TencentSOE.newInstance(SECRET_ID, SECRET_KEY)
                    .setRootUrl("soe.tencentcloudapi.com")// 非必要
                    .setRegion("")// 非必要
                    .setSoeAppId("default")// 非必要
                    .setRefText(RefText)
                    .setEvalMode(evalMode)
                    .setScoreCoeff(1.0f)
                    .setIsLongLifeSession(TencentSOE.SESSION_LIFE_LONG)
                    .setStorageMode(0)
                    .setVoiceFileType(fileType)
                    .setUserVoiceData(base64StringArray)// 或直接setUserVoiceData(filePath, 9 * 1024)
                    .execute(callback);
        } catch (Exception e) {
            sendMessage(MSG_ERROR, e.getMessage());
        }
    }

    private void doExecuteOnce() {
        try {
            String RefText = etRefText.getText().toString();
            if (filePath.endsWith(".mp3")) {// 只有MP3需要校验
                try {
                    TencentSOE.checkMP3Format(filePath);
                } catch (Exception e) {
                    sendMessage(MSG_ERROR, e.getMessage());
                    return;
                }
            }
            String base64String = TencentSOE.encodeAudioFile(filePath);
            TencentSOE.newInstance(SECRET_ID, SECRET_KEY)
                    .setRootUrl("soe.tencentcloudapi.com")// 非必要
                    .setRegion("")// 非必要
                    .setSoeAppId("default")// 非必要
                    .setRefText(RefText)
                    .setEvalMode(evalMode)
                    .setScoreCoeff(1.0f)
                    .setIsLongLifeSession(TencentSOE.SESSION_LIFE_LONG)
                    .setStorageMode(0)
                    .setVoiceFileType(fileType)
                    .setUserVoiceData(base64String)
                    .execute(callback);
        } catch (Exception e) {
            sendMessage(MSG_ERROR, e.getMessage());
        }
    }

    private SOECallback callback = new SOECallback() {
        public void onInitSuccess(InitOralProcessResponse response) {
            sendMessage(MSG_INIT_OK, response.toString());
        }

        public void onTransmitSuccess(int index, int isEnd, TransmitOralProcessResponse response) {
            Message msg = new Message();
            msg.what = MSG_TRANSMIT_OK;
            msg.arg1 = index;
            msg.arg2 = isEnd;
            msg.obj = response.toString();
            mMyHandler.sendMessage(msg);
        }

        public void onError(SOEError e) {
            sendMessage(MSG_INIT_ERROR, e.getMessage());
        }
    };

    private void sendMessage(int what, Object obj) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        mMyHandler.sendMessage(msg);
    }

    private void toast(String text) {
        final Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 3000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, 1000);
    }

    private int evalMode = TencentSOE.EVAL_MODE_SENTENCE;
    private int fileType = TencentSOE.AUDIO_TYPE_MP3;

    private OnCheckedChangeListener radioListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (group.getCheckedRadioButtonId()) {
                case R.id.evalmode0:
                    evalMode = TencentSOE.EVAL_MODE_WORD;
                    break;
                case R.id.evalmode1:
                    evalMode = TencentSOE.EVAL_MODE_SENTENCE;
                    break;
                case R.id.filetype1:
                    fileType = TencentSOE.AUDIO_TYPE_RAW;
                    break;
                case R.id.filetype2:
                    fileType = TencentSOE.AUDIO_TYPE_WAV;
                    break;
                case R.id.filetype3:
                    fileType = TencentSOE.AUDIO_TYPE_MP3;
                    break;
                default:
                    break;
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                if (uri != null) {
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        filePath = UriToPath.getPathByUri4kitkat(mContext, uri);
                    } else {
                        filePath = uri.getPath();
                   }
                    tvPath.setText(filePath);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick({R.id.execute_once, R.id.execute_stream, R.id.recorder, R.id.select,R.id.frame_recorder})
    void doClick(View v) {
        switch (v.getId()) {
            case R.id.execute_stream:
                doExecuteStream();
                break;
            case R.id.execute_once:
                doExecuteOnce();
                break;
            case R.id.recorder:
                if (btRecorder.getText().equals("开始录音")) {
                    TencentSOE.startRecordMp3(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SOE/", "soe");
                    btRecorder.setText("停止录音");
                } else {
                    TencentSOE.stopRecord();
                    btRecorder.setText("开始录音");
                    tvPath.setText(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SOE/soe.mp3");
                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SOE/soe.mp3";
                }
                break;
            case R.id.select:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;
            case R.id.frame_recorder:
                if (btFrameRecorder.getText().equals("开始分片录音")) {
                    try {
                        String RefText = etRefText.getText().toString();
                        TencentSOE.newInstance(SECRET_ID, SECRET_KEY)
                                .setRootUrl("soe.ap-guangzhou.tencentcloudapi.com/")// 非必要
                                .setRegion("ap-guangzhou")// 非必要
                                .setSoeAppId("default")// 非必要
                                .setRefText(RefText)
                                .setEvalMode(evalMode)
                                .setWorkMode(TencentSOE.WORK_MODE_STREAM)
                                .setScoreCoeff(1.0f)
                                .setVoiceFileType(TencentSOE.AUDIO_TYPE_MP3)
                                .setIsLongLifeSession(TencentSOE.SESSION_LIFE_SHORT)
                                .setStorageMode(0)
                                .setCallBack(callback)
                                .startFrameRecord();

                        btFrameRecorder.setText("结束分片录音");
                    }
                    catch (Exception e) {
                        sendMessage(MSG_ERROR, e.getMessage());
                    }
                } else {
                    TencentSOE.stopFrameRecord();
                    btFrameRecorder.setText("开始分片录音");
                }
                break;
        }
    }
}
