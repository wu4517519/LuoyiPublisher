package com.luoyi.luoyipublisher.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.eventhandle.SmartEventCallback;
import com.luoyi.luoyipublisher.R;
import com.luoyi.luoyipublisher.fragment.base.BaseFragment;
import com.luoyi.luoyipublisher.util.Constant;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.luoyi.luoyipublisher.fragment.CaptureFragment.baseURL;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.btnAGC;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.btnCaptureImage;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.btnHWencoder;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.btnMirror;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.btnNoiseSuppression;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.btnSpeex;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.imageSavePath;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.isPushing;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.isRecording;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.isStart;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.is_agc;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.is_hardware_encoder;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.is_mirror;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.is_noise_suppression;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.is_speex;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.libPublisher;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.printText;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.pushType;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.pushTypeSelector;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.swVideoEncoderSpeedSelector;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.sw_video_encoder_speed;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.txt;
import static com.luoyi.luoyipublisher.fragment.CaptureFragment.watemarkType;

/**
 * Created by wwc on 2017/9/30.
 */

public class CaptureSettingFragment extends BaseFragment {

    private TextView title;
    private ImageView titleBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(Constant.TAG, "CaptureFragment onCreateView..");
        View view = inflater.inflate(R.layout.fragment_capture_setting, container, false);
        initView(view);
        initEvent(view);
        return view;
    }

    private void initView(View view){

        title = (TextView) view.findViewById(R.id.title_text);
        title.setText("监控设置");
        titleBack = (ImageView) view.findViewById(R.id.title_back);

        //push type, audio/video/audio&video
        pushTypeSelector = (Spinner) view.findViewById(R.id.pushTypeSelctor);
        //水印
        CaptureFragment.watermarkSelctor = (Spinner) view.findViewById(R.id.watermarkSelctor);
        CaptureFragment.resolutionSelector = (Spinner) view.findViewById(R.id.resolutionSelctor);
        CaptureFragment.swVideoEncoderProfileSelector = (Spinner) view.findViewById(R.id.swVideoEncoderProfileSelector);
        //Recorder related settings
        CaptureFragment.recorderSelector = (Spinner) view.findViewById(R.id.recoder_selctor);
        //end

        btnNoiseSuppression = (Button) view.findViewById(R.id.button_noise_suppression);
        btnAGC = (Button) view.findViewById(R.id.button_agc);
        btnSpeex = (Button) view.findViewById(R.id.button_speex);

        btnMirror = (Button) view.findViewById(R.id.button_mirror);
        swVideoEncoderSpeedSelector = (Spinner) view.findViewById(R.id.sw_video_encoder_speed_selctor);
        btnHWencoder = (Button) view.findViewById(R.id.button_hwencoder);
        CaptureFragment.textCurURL = (TextView) view.findViewById(R.id.txtCurURL);
        CaptureFragment.textCurURL.setText(printText);
        CaptureFragment.btnInputPushUrl = (Button) view.findViewById(R.id.button_input_push_url);
        btnCaptureImage = (Button) view.findViewById(R.id.button_capture_image);


    }

    private void initEvent(View view) {
        final String[] types = new String[]{"音视频", "纯音频", "纯视频"};
        ArrayAdapter<String> adapterType = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, types);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        pushTypeSelector.setAdapter(adapterType);
        pushTypeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (isStart || isPushing || isRecording) {
                    Log.e(Constant.TAG, "Could not switch push type during publishing..");
                    return;
                }
                pushType = position;
                Log.e(Constant.TAG, "[推送类型]Currently choosing: " + types[position] + ", pushType: " + pushType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //水印
        final String[] watermarks = new String[]{"图片水印", "全部水印", "文字水印", "不加水印"};
        ArrayAdapter<String> adapterWatermark = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, watermarks);
        adapterWatermark.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CaptureFragment.watermarkSelctor.setAdapter(adapterWatermark);
        CaptureFragment.watermarkSelctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (isStart || isPushing || isRecording) {
                    Log.e(Constant.TAG, "Could not switch water type during publishing..");
                    return;
                }

                watemarkType = position;

                Log.e(Constant.TAG, "[水印类型]Currently choosing: " + watermarks[position] + ", watemarkType: " + watemarkType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //end

        final String[] profileSel = new String[]{"BaseLineProfile", "MainProfile", "HighProfile"};
        ArrayAdapter<String> adapterProfile = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, profileSel);
        adapterProfile.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CaptureFragment.swVideoEncoderProfileSelector.setAdapter(adapterProfile);
        CaptureFragment.swVideoEncoderProfileSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                if (isStart || isPushing || isRecording) {
                    Log.e(Constant.TAG, "Could not switch video profile during publishing..");
                    return;
                }

                Log.e(Constant.TAG, "[VideoProfile]Currently choosing: " + profileSel[position]);

                CaptureFragment.sw_video_encoder_profile = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final String[] recoderSel = new String[]{"本地不录像", "本地录像"};
        ArrayAdapter<String> adapterRecoder = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, recoderSel);
        adapterRecoder.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CaptureFragment.recorderSelector.setAdapter(adapterRecoder);
        CaptureFragment.recorderSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                Log.e(Constant.TAG, "Currently choosing: " + recoderSel[position]);

                if (1 == position) {
                    CaptureFragment.is_need_local_recorder = true;
                } else {
                    CaptureFragment.is_need_local_recorder = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CaptureFragment.btnAGC.setOnClickListener(new ButtonAGCListener());
        btnSpeex.setOnClickListener(new ButtonSpeexListener());
        btnMirror.setOnClickListener(new ButtonMirrorListener());

        final String[] video_encoder_speed_Sel = new String[]{"6", "5", "4", "3", "2", "1"};
        ArrayAdapter<String> adapterVideoEncoderSpeed = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, video_encoder_speed_Sel);
        adapterVideoEncoderSpeed.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CaptureFragment.swVideoEncoderSpeedSelector.setAdapter(adapterVideoEncoderSpeed);
        CaptureFragment.swVideoEncoderSpeedSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                Log.e(Constant.TAG, "Currently speed choosing: " + video_encoder_speed_Sel[position]);

                sw_video_encoder_speed = 6 - position;

                Log.e(Constant.TAG, "Choose speed=" + sw_video_encoder_speed);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnHWencoder.setOnClickListener(new ButtonHardwareEncoderListener());
        btnNoiseSuppression.setOnClickListener(new ButtonNoiseSuppressionListener());
        CaptureFragment.btnInputPushUrl.setOnClickListener(new ButtonInputPushUrlListener());

        CaptureFragment.btnCaptureImage.setOnClickListener(new ButtonCaptureImageListener());

        titleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getFragmentManager() ;
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.lyFrame, CaptureFragment.getInstance());
                ft.commit();
            }
        });
    }


    /**
     * desc 噪声抑制
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:38
     */
    class ButtonNoiseSuppressionListener implements View.OnClickListener {
        public void onClick(View v) {
            is_noise_suppression = !is_noise_suppression;

            if (is_noise_suppression)
                btnNoiseSuppression.setText("停用噪音抑制");
            else
                btnNoiseSuppression.setText("启用噪音抑制");
        }
    }

    /**
     * desc 启用ACG
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:38
     */
    class ButtonAGCListener implements View.OnClickListener {
        public void onClick(View v) {
            is_agc = !is_agc;

            if (is_agc)
                btnAGC.setText("停用AGC");
            else
                btnAGC.setText("启用AGC");
        }
    }

    /**
     * desc 启用SPEEX音频编码
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:39
     */
    class ButtonSpeexListener implements View.OnClickListener {
        public void onClick(View v) {
            is_speex = !is_speex;

            if (is_speex)
                btnSpeex.setText("不使用Speex");
            else
                btnSpeex.setText("使用Speex");
        }
    }

    /**
     * desc 启用镜像
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:40
     */
    class ButtonMirrorListener implements View.OnClickListener {
        public void onClick(View v) {
            is_mirror = !is_mirror;

            if (is_mirror)
                btnMirror.setText("关镜像");
            else
                btnMirror.setText("开镜像");

            if (libPublisher != null)
                libPublisher.SmartPublisherSetMirror(is_mirror ? 1 : 0);
        }
    }

    /**
     * desc 硬编码
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:40
     */
    class ButtonHardwareEncoderListener implements View.OnClickListener {
        public void onClick(View v) {
            is_hardware_encoder = !is_hardware_encoder;

            if (is_hardware_encoder)
                btnHWencoder.setText("当前硬解码");
            else
                btnHWencoder.setText("当前软解码");
        }
    }

    /**
     * desc 事件处理
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:40
     */
    class EventHande implements SmartEventCallback {
        @Override
        public void onCallback(int code, long param1, long param2, String param3, String param4, Object param5) {
            switch (code) {
                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_STARTED:
                    txt = "开始。。";
                    break;
                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_CONNECTING:
                    txt = "连接中。。";
                    break;
                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_CONNECTION_FAILED:
                    txt = "连接失败。。";
                    break;
                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_CONNECTED:
                    txt = "连接成功。。";
                    break;
                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_DISCONNECTED:
                    txt = "连接断开。。";
                    break;
                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_STOP:
                    txt = "关闭。。";
                    break;
                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_RECORDER_START_NEW_FILE:
                    Log.e(Constant.TAG, "开始一个新的录像文件 : " + param3);
                    txt = "开始一个新的录像文件。。";
                    break;
                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_ONE_RECORDER_FILE_FINISHED:
                    Log.e(Constant.TAG, "已生成一个录像文件 : " + param3);
                    txt = "已生成一个录像文件。。";
                    break;

                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_SEND_DELAY:
                    Log.e(Constant.TAG, "发送时延: " + param1 + " 帧数:" + param2);
                    txt = "收到发送时延..";
                    break;

                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_CAPTURE_IMAGE:
                    Log.e(Constant.TAG, "快照: " + param1 + " 路径：" + param3);

                    if (param1 == 0) {
                        txt = "截取快照成功。.";
                    } else {
                        txt = "截取快照失败。.";
                    }
                    break;
            }

            /*String str = "当前回调状态：" + txt;

            Log.e(Constant.TAG, str);*/

        }
    }

    class ButtonInputPushUrlListener implements View.OnClickListener {
        public void onClick(View v) {
            PopInputUrlDialog();
        }
    }

    /**
     * desc 快照
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:43
     */
    class ButtonCaptureImageListener implements View.OnClickListener {
        @SuppressLint("SimpleDateFormat")
        public void onClick(View v) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "dn_" + timeStamp;    //创建以时间命名的文件名称

            String imagePath = imageSavePath + "/" + imageFileName + ".png";

            Log.e(Constant.TAG, "imagePath:" + imagePath);

            libPublisher.SmartPublisherSaveCurImage(imagePath);
        }
    }

    /**
     * desc 输入推流URL对话框
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:41
     */
    private void PopInputUrlDialog() {
        final EditText inputUrlTxt = new EditText(getActivity());
        inputUrlTxt.setFocusable(true);
        //inputUrlTxt.setText(baseURL + String.valueOf((int)( System.currentTimeMillis() % 1000000)));
        inputUrlTxt.setText(baseURL);
        AlertDialog.Builder builderUrl = new AlertDialog.Builder(getActivity());
        builderUrl.setTitle("如 rtmp://player.daniulive.com:1935/hls/stream123456").setView(inputUrlTxt).setNegativeButton(
                "取消", null);

        builderUrl.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                String fullPushUrl = inputUrlTxt.getText().toString();
                SaveInputUrl(fullPushUrl);
            }
        });

        builderUrl.show();
    }

    private void SaveInputUrl(String url) {
        CaptureFragment.inputPushURL = "";

        if (url == null)
            return;

        // rtmp://
        if (url.length() < 8) {
            Log.e(Constant.TAG, "Input publish url error:" + url);
            return;
        }

        if (!url.startsWith("rtmp://")) {
            Log.e(Constant.TAG, "Input publish url error:" + url);
            return;
        }

        CaptureFragment.inputPushURL = url;

        Log.e(Constant.TAG, "Input publish url:" + url);
    }
}
