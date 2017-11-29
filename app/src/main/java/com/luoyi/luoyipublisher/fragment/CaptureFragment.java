package com.luoyi.luoyipublisher.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.daniulive.smartpublisher.SmartPublisherJni;
import com.daniulive.smartpublisher.SmartPublisherJni.WATERMARK;
import com.eventhandle.SmartEventCallback;
import com.luoyi.luoyipublisher.FrameProcess;
import com.luoyi.luoyipublisher.R;
import com.luoyi.luoyipublisher.activity.RecorderManager;
import com.luoyi.luoyipublisher.common.AlarmLogManager;
import com.luoyi.luoyipublisher.common.DeviceCallback;
import com.luoyi.luoyipublisher.common.DeviceManager;
import com.luoyi.luoyipublisher.common.MonitorCallback;
import com.luoyi.luoyipublisher.fragment.base.BaseFragment;
import com.luoyi.luoyipublisher.util.Constant;
import com.luoyi.luoyipublisher.util.ScreenUtil;
import com.luoyi.luoyipublisher.view.MarkSizeView;
import com.voiceengine.NTAudioRecord;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.luoyi.luoyipublisher.FrameProcess.mBitmaps;
import static com.luoyi.luoyipublisher.R.id.button_start_recorder;

/**
 * Created by wwc on 2017/9/28.
 */

public class CaptureFragment extends BaseFragment implements
        SurfaceHolder.Callback, Camera.PreviewCallback, MonitorCallback,DeviceCallback {

    private View mView;
    private View captureView;
    private RelativeLayout rlCapture;
    private LinearLayout ly1;
    private LinearLayout ly2;
    private MarkSizeView markSizeView;
    private MarkSizeView.GraphicPath mGraphicPath;
    private Rect mMarkedArea;
    private TextView captureTips;
    private TextView captureAll;
    private Rect defaultMonitorArea;
    private Rect two;
    private int screenWidth;
    private int screenHeight;

    //for recorder path
    public static String recDir = "/sdcard/daniulive/rec";
    public static String printText = "URL:";
    NTAudioRecord audioRecord_ = null;    //for audio capture
    public static TextView textCurURL = null;
    public static SmartPublisherJni libPublisher = null;
    /* 推送类型选择
     * 0: 音视频
     * 1: 纯音频
     * 2: 纯视频
     * */
    public static Spinner pushTypeSelector;
    public static int pushType = 0;

    /* 水印类型选择
     * 0: 图片水印
     * 1: 全部水印
     * 2: 文字水印
     * 3: 不加水印
     * */
    public static Spinner watermarkSelctor;
    public static  int watemarkType = 0;

    /* 推流分辨率选择
     * 0: 640*480
     * 1: 320*240
     * 2: 176*144
     * 3: 1280*720
     * */
    public static Spinner resolutionSelector;

    /* video软编码profile设置
     * 1: baseline profile
     * 2: main profile
     * 3: high profile
     * */
    public static Spinner swVideoEncoderProfileSelector;
    public static int sw_video_encoder_profile = 1;    //default with baseline profile
    public static Spinner recorderSelector;
    private ImageView btnRecoderMgr;
    private ImageView btnMute;
    private ImageView imgSwitchCamera;
    private ImageView btnStartPush;
    private ImageView btnStartRecorder;
    private ImageView captureSetting;
    private ImageView drawArea;
    public static Button btnNoiseSuppression;
    public static Button btnAGC;
    public static Button btnSpeex;
    public static Button btnMirror;
    public static Spinner swVideoEncoderSpeedSelector;
    public static Button btnHWencoder;
    public static Button btnInputPushUrl;
    public static Button btnStartStop;
    public static Button btnCaptureImage;

    private SurfaceView mSurfaceView = null;
    private SurfaceHolder mSurfaceHolder = null;
    public static Camera mCamera = null;
    private Camera.AutoFocusCallback myAutoFocusCallback = null;
    public static boolean mPreviewRunning = false;
    public static boolean isStart = false;
    public static boolean isPushing = false;
    public static boolean isRecording = false;
    public static boolean isWritelogoFileSuccess = false;
    final private String logoPath = "/sdcard/daniulivelogo.png";
    private String publishURL;
    public static final  String baseURL = "rtmp://192.168.80.130/LuoyiLive/1";
    public static String inputPushURL = "";
    public static String txt = "当前状态";

    private static final int FRONT = 1;        //前置摄像头标记
    private static final int BACK = 2;        //后置摄像头标记
    private int currentCameraType = BACK;    //当前打开的摄像头标记
    private static final int PORTRAIT = 1;    //竖屏
    private static final int LANDSCAPE = 2;    //横屏
    private int currentOrigentation = PORTRAIT;
    private int curCameraIndex = -1;

    private int videoWidth = 640;
    private int videoHeight = 480;
    private int frameCount = 0;

    public static boolean is_need_local_recorder = false;        // do not enable recorder in default
    public static boolean is_noise_suppression = true;
    public static boolean is_agc = false;
    public static boolean is_speex = false;
    public static  boolean is_mute = false;
    public static boolean is_mirror = false;
    public static int sw_video_encoder_speed = 6;
    public static boolean is_hardware_encoder = false;
    private Context myContext;
    public static String imageSavePath;
    private FrameProcess mFrameProcess;
    private Thread frameProcessThread;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

    private static CaptureFragment captureFragment;
    private final MyHandler myHandler = new MyHandler();
    private MineFragment mineFragment;
    private BottomNavigationBar bottomNavigationBar;

    private long lastTime = 0;
    private long timeDiff = 0;
    private final int SAVE_COVER_PERIOD = 1000*3;//上传封面时间间隔
    private CoverHandler coverHandler;
    private Timer timer;
    private TimerTask timerTask;
    private boolean isSetMonitorArea = false;
    /**
     * desc 加载so库文件
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:49
     */
    static {
        try {
            System.loadLibrary("SmartPublisher");
            System.loadLibrary("opencv_java3");
            System.loadLibrary("native-lib");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CaptureFragment() {
        // Required empty public constructor
    }

    public static CaptureFragment getInstance() {
        if(captureFragment == null){
            captureFragment = new CaptureFragment();
        }
        return captureFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenWidth = ScreenUtil.getScreenWidth(getActivity());
        screenHeight = ScreenUtil.getScreenHeight(getActivity());
        defaultMonitorArea = new Rect();
        two = new Rect();
        mFrameProcess = new FrameProcess(videoWidth, videoHeight);
        mFrameProcess.setMonitorCallback(this);
        coverHandler = new CoverHandler();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constant.TAG, "CaptureFragment onCreateView..");
        View view = inflater.inflate(R.layout.fragment_capture, container, false);
        mView = view;
        initView(view);
        initEvent(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(Constant.TAG, "CaptureFragment onStart call");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(Constant.TAG, "CaptureFragment onResume call");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(Constant.TAG, "CaptureFragment onDestroyView call");
    }

    @Override
    public void onDestroy() {
        Log.d(Constant.TAG, "activity destory!");
        super.onDestroy();
        if (isStart) {
            isStart = false;
            StopPublish();
            Log.d(Constant.TAG, "onDestroy StopPublish");
        }
        if (isPushing || isRecording) {
            if (audioRecord_ != null) {
                Log.d(Constant.TAG, "surfaceDestroyed, call StopRecording..");
                audioRecord_.StopRecording();
                audioRecord_ = null;
            }
            stopPush();
            stopRecorder();
            isPushing = false;
            isRecording = false;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(Constant.TAG, "CaptureFragment onDetach call");
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(Constant.TAG, "surfaceCreated..");
        try {

            int CammeraIndex = findBackCamera();
            Log.d(Constant.TAG, "BackCamera: " + CammeraIndex);

            if (CammeraIndex == -1) {
                CammeraIndex = findFrontCamera();
                currentCameraType = FRONT;
                imgSwitchCamera.setEnabled(false);
                if (CammeraIndex == -1) {
                    Log.d(Constant.TAG, "NO camera!!");
                    return;
                }
            } else {
                currentCameraType = BACK;
            }

            if (mCamera == null) {
                mCamera = openCamera(currentCameraType);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(Constant.TAG, "surfaceChanged..");
        initCamera(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.d(Constant.TAG, "Surface Destroyed");
    }

    /**
     * desc 预览帧处理
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:47
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        timeDiff = System.currentTimeMillis() - lastTime;
        frameCount++;
        if (frameCount % 3000 == 0) {
            Log.d("OnPre", "gc+");
            System.gc();
            Log.d("OnPre", "gc-");
        }

        if (data == null) {
            Camera.Parameters params = camera.getParameters();
            Camera.Size size = params.getPreviewSize();
            int bufferSize = (((size.width | 0x1f) + 1) * size.height * ImageFormat.getBitsPerPixel(params.getPreviewFormat())) / 8;
            camera.addCallbackBuffer(new byte[bufferSize]);
        } else {
            if (isStart || isPushing || isRecording) {
                //传递实时采集的video数据
                libPublisher.SmartPublisherOnCaptureVideoData(data, data.length, currentCameraType, currentOrigentation);
                if(isPushing){
                    if(timeDiff >= mFrameProcess.TIME_INTERVAL){
                        mFrameProcess.setmVideoWidth(videoWidth);
                        mFrameProcess.setmVideoHeight(videoHeight);
                        long curTime = System.currentTimeMillis();
                        mFrameProcess.setMonitorAreaRect(mMarkedArea);
                        mFrameProcess.dataBuffer.add(data);
                        mFrameProcess.timeQueue.add(df.format(new Date()));
                        mFrameProcess.lastRecFrameTime = curTime;
                        lastTime = curTime;
                    }
                }
            }
            camera.addCallbackBuffer(data);
        }
    }

    @Override
    public void onStartMonitor() {
        DeviceManager.getInstance().updateDeviceOnlineStatus(true);
    }

    @Override
    public void onEndMonitor() {
        DeviceManager.getInstance().updateDeviceOnlineStatus(false);
    }

    public void onInvasion(String st, String et, String filePath) {
        Message msg = new Message();
        Map<String,String> map = new HashMap<String,String>();
        map.put("st",st);
        map.put("et",et);
        map.put("saveFilePath",filePath);
        map.put("msg",Constant.INVASION);
        msg.obj = map;
        msg.what = 0;
        myHandler.sendMessage(msg);
    }

    /**
     * desc 设置FPS
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 9:04
     */
    private void SetCameraFPS(Camera.Parameters parameters) {
        if (parameters == null)
            return;

        int[] findRange = null;

        int defFPS = 20 * 1000;

        List<int[]> fpsList = parameters.getSupportedPreviewFpsRange();
        if (fpsList != null && fpsList.size() > 0) {
            for (int i = 0; i < fpsList.size(); ++i) {
                int[] range = fpsList.get(i);
                if (range != null
                        && Camera.Parameters.PREVIEW_FPS_MIN_INDEX < range.length
                        && Camera.Parameters.PREVIEW_FPS_MAX_INDEX < range.length) {
                    Log.d(Constant.TAG, "Camera index:" + i + " support min fps:" + range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]);

                    Log.d(Constant.TAG, "Camera index:" + i + " support max fps:" + range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);

                    if (findRange == null) {
                        if (defFPS <= range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]) {
                            findRange = range;

                            Log.d(Constant.TAG, "Camera found appropriate fps, min fps:" + range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]
                                    + " ,max fps:" + range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
                        }
                    }
                }
            }
        }

        if (findRange != null) {
            parameters.setPreviewFpsRange(findRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], findRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        }
    }

    /**
     * desc 初始化摄像头，在SufaceChanged中被调用
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:45
     */
    /*it will call when surfaceChanged*/
    private void initCamera(SurfaceHolder holder) {
        Log.d(Constant.TAG, "initCamera..");

        if (mPreviewRunning)
            mCamera.stopPreview();

        Camera.Parameters parameters;
        try {
            parameters = mCamera.getParameters();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        parameters.setPreviewSize(videoWidth, videoHeight);
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);

        SetCameraFPS(parameters);

        setCameraDisplayOrientation(getActivity(), curCameraIndex, mCamera);

        mCamera.setParameters(parameters);

        int bufferSize = (((videoWidth | 0xf) + 1) * videoHeight * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())) / 8;

        mCamera.addCallbackBuffer(new byte[bufferSize]);

        mCamera.setPreviewCallbackWithBuffer(this);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
            ex.printStackTrace();
        }
        mCamera.startPreview();
        mCamera.autoFocus(myAutoFocusCallback);
        mPreviewRunning = true;
    }

    @SuppressLint("NewApi")
    private Camera openCamera(int type) {
        int frontIndex = -1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras();
        Log.d(Constant.TAG, "cameraCount: " + cameraCount);

        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
            Camera.getCameraInfo(cameraIndex, info);

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontIndex = cameraIndex;
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backIndex = cameraIndex;
            }
        }

        currentCameraType = type;
        if (type == FRONT && frontIndex != -1) {
            curCameraIndex = frontIndex;
            return Camera.open(frontIndex);
        } else if (type == BACK && backIndex != -1) {
            curCameraIndex = backIndex;
            return Camera.open(backIndex);
        }
        return null;
    }

    private void switchCamera() throws IOException {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        if (currentCameraType == FRONT) {
            mCamera = openCamera(BACK);
            imgSwitchCamera.setImageDrawable(getResources().getDrawable(R.drawable.back_camera));
        } else if (currentCameraType == BACK) {
            mCamera = openCamera(FRONT);
            imgSwitchCamera.setImageDrawable(getResources().getDrawable(R.drawable.front_camera));
        }

        initCamera(mSurfaceHolder);
    }
    /**
     * desc 切换分辨率
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:37
     */
    //Check if it has front camera
    private int findFrontCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return camIdx;
            }
        }
        return -1;
    }

    //Check if it has back camera
    private int findBackCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return camIdx;
            }
        }
        return -1;
    }

    private void setCameraDisplayOrientation(Activity activity, int cameraId,
                                             android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        Log.d(Constant.TAG, "curDegree: " + result);

        camera.setDisplayOrientation(result);
    }

    void SwitchResolution(int position) {
        Log.d(Constant.TAG, "Current Resolution position: " + position);

        switch (position) {
            case 0:
                videoWidth = 640;
                videoHeight = 480;
                break;
            case 1:
                videoWidth = 320;
                videoHeight = 240;
                break;
            case 2:
                videoWidth = 176;
                videoHeight = 144;
                break;
            case 3:
                videoWidth = 1280;
                videoHeight = 720;
                break;
            default:
                videoWidth = 640;
                videoHeight = 480;
        }

        mCamera.stopPreview();
        initCamera(mSurfaceHolder);
    }

    void CheckInitAudioRecorder() {
        if (audioRecord_ == null) {
            audioRecord_ = new NTAudioRecord(getActivity(), 1);
        }
        if (audioRecord_ != null) {
            Log.d(Constant.TAG, "onCreate, call executeAudioRecordMethod..");
            // auido_ret: 0 ok, other failed
            int auido_ret = audioRecord_.executeAudioRecordMethod();
            Log.d(Constant.TAG, "onCreate, call executeAudioRecordMethod.. auido_ret=" + auido_ret);
        }
    }

    //Configure recorder related function.
    void ConfigRecorderFunction(boolean isNeedLocalRecorder) {
        if (libPublisher != null) {
            if (isNeedLocalRecorder) {
                if (recDir != null && !recDir.isEmpty()) {
                    int ret = libPublisher.SmartPublisherCreateFileDirectory(recDir);
                    if (0 == ret) {
                        if (0 != libPublisher.SmartPublisherSetRecorderDirectory(recDir)) {
                            Log.d(Constant.TAG, "Set recoder dir failed , path:" + recDir);
                            return;
                        }

                        if (0 != libPublisher.SmartPublisherSetRecorder(1)) {
                            Log.d(Constant.TAG, "SmartPublisherSetRecoder failed.");
                            return;
                        }

                        if (0 != libPublisher.SmartPublisherSetRecorderFileMaxSize(200)) {
                            Log.d(Constant.TAG, "SmartPublisherSetRecoderFileMaxSize failed.");
                            return;
                        }

                    } else {
                        Log.d(Constant.TAG, "Create recoder dir failed, path:" + recDir);
                    }
                }
            } else {
                if (0 != libPublisher.SmartPublisherSetRecorder(0)) {
                    Log.d(Constant.TAG, "SmartPublisherSetRecoder failed.");
                    return;
                }
            }
        }
    }

    private void ConfigControlEnable(boolean isEnable) {
        btnRecoderMgr.setEnabled(isEnable);
        if(btnHWencoder != null)
            btnHWencoder.setEnabled(isEnable);
        if(btnNoiseSuppression != null)
            btnNoiseSuppression.setEnabled(isEnable);
        if(btnAGC != null)
            btnAGC.setEnabled(isEnable);
        if(btnSpeex != null)
            btnSpeex.setEnabled(isEnable);
    }

    private void InitAndSetConfig() {

        SwitchResolution(0);
        Log.d(Constant.TAG, "videoWidth: " + videoWidth + " videoHeight: " + videoHeight
                + " pushType:" + pushType);

        int audio_opt = 1;
        int video_opt = 1;

        if (pushType == 1) {
            video_opt = 0;
        } else if (pushType == 2) {
            audio_opt = 0;
        }

        libPublisher.SmartPublisherInit(myContext, audio_opt, video_opt,
                videoWidth, videoHeight);

        if (is_hardware_encoder) {
            int hwHWKbps = setHardwareEncoderKbps(videoWidth, videoHeight);

            Log.d(Constant.TAG, "hwHWKbps: " + hwHWKbps);

            int isSupportHWEncoder = libPublisher
                    .SetSmartPublisherVideoHWEncoder(hwHWKbps);

            if (isSupportHWEncoder == 0) {
                Log.d(Constant.TAG, "Great, it supports hardware encoder!");
            }
        }

        libPublisher.SetSmartPublisherEventCallback(new EventHande());

        // 如果想和时间显示在同一行，请去掉'\n'
        String watermarkText = "大牛直播(daniulive)\n\n";

        String path = logoPath;

        if (watemarkType == 0) {
            if (isWritelogoFileSuccess)
                libPublisher.SmartPublisherSetPictureWatermark(path,
                        WATERMARK.WATERMARK_POSITION_TOPRIGHT, 160,
                        160, 10, 10);

        } else if (watemarkType == 1) {
            if (isWritelogoFileSuccess)
                libPublisher.SmartPublisherSetPictureWatermark(path,
                        WATERMARK.WATERMARK_POSITION_TOPRIGHT, 160,
                        160, 10, 10);

            libPublisher.SmartPublisherSetTextWatermark(watermarkText, 1,
                    WATERMARK.WATERMARK_FONTSIZE_BIG,
                    WATERMARK.WATERMARK_POSITION_BOTTOMRIGHT, 10, 10);

            // libPublisher.SmartPublisherSetTextWatermarkFontFileName("/system/fonts/DroidSansFallback.ttf");

            // libPublisher.SmartPublisherSetTextWatermarkFontFileName("/sdcard/DroidSansFallback.ttf");
        } else if (watemarkType == 2) {
            libPublisher.SmartPublisherSetTextWatermark(watermarkText, 1,
                    WATERMARK.WATERMARK_FONTSIZE_BIG,
                    WATERMARK.WATERMARK_POSITION_BOTTOMRIGHT, 10, 10);

            // libPublisher.SmartPublisherSetTextWatermarkFontFileName("/system/fonts/DroidSansFallback.ttf");
        } else {
            Log.d(Constant.TAG, "no watermark settings..");
        }
        // end

        if (!is_speex) {
            // set AAC encoder
            libPublisher.SmartPublisherSetAudioCodecType(1);
        } else {
            // set Speex encoder
            libPublisher.SmartPublisherSetAudioCodecType(2);
            libPublisher.SmartPublisherSetSpeexEncoderQuality(8);
        }

        libPublisher.SmartPublisherSetNoiseSuppression(is_noise_suppression ? 1
                : 0);

        libPublisher.SmartPublisherSetAGC(is_agc ? 1 : 0);

        // libPublisher.SmartPublisherSetClippingMode(0);

        libPublisher.SmartPublisherSetSWVideoEncoderProfile(sw_video_encoder_profile);

        libPublisher.SmartPublisherSetSWVideoEncoderSpeed(sw_video_encoder_speed);

        // libPublisher.SetRtmpPublishingType(0);

        // libPublisher.SmartPublisherSetGopInterval(40);

        // libPublisher.SmartPublisherSetFPS(15);

        // libPublisher.SmartPublisherSetSWVideoBitRate(600, 1200);

        libPublisher.SmartPublisherSaveImageFlag(1);
    }

    private void stop() {
        Log.d(Constant.TAG, "onClick stop..");
        StopPublish();
        isStart = false;
        //btnStartStop.setText(" 开始推流 ");
        if (!mFrameProcess.addFrameThread.isInterrupted()) {
            mFrameProcess.addFrameThread.interrupt();
        }
        if (!frameProcessThread.isInterrupted()) {
            frameProcessThread.interrupt();
        }

    }

    /**
     * desc 停止推流
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:43
     */
    private void stopPush() {
        Toast.makeText(getActivity(),"停止监控",Toast.LENGTH_SHORT).show();
        if (!isRecording) {
            if (audioRecord_ != null) {
                Log.d(Constant.TAG, "stopPush, call audioRecord_.StopRecording..");
                audioRecord_.StopRecording();
                audioRecord_ = null;
            }
        }

        if (libPublisher != null) {
            libPublisher.SmartPublisherStopPublisher();
        }
        onEndMonitor();
        timer.cancel();
        timer = null;
    }

    private void stopRecorder() {
        if (!isPushing) {
            if (audioRecord_ != null) {
                Log.d(Constant.TAG, "stopRecorder, call audioRecord_.StopRecording..");
                audioRecord_.StopRecording();
                audioRecord_ = null;
            }
        }

        if (libPublisher != null) {
            libPublisher.SmartPublisherStopRecorder();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        try {
            super.onConfigurationChanged(newConfig);
            Log.d(Constant.TAG, "onConfigurationChanged, start:" + isStart);
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!isStart && !isPushing && !isRecording) {
                    currentOrigentation = LANDSCAPE;
                }
            } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (!isStart && !isPushing && !isRecording) {
                    currentOrigentation = PORTRAIT;
                }
            }
        } catch (Exception ex) {
        }
    }

    private void StopPublish() {
        if (audioRecord_ != null) {
            Log.d(Constant.TAG, "surfaceDestroyed, call StopRecording..");
            audioRecord_.StopRecording();
            audioRecord_ = null;
        }

        if (libPublisher != null) {
            libPublisher.SmartPublisherStop();
        }
    }

    private int setHardwareEncoderKbps(int width, int height) {
        int hwEncoderKpbs = 0;

        switch (width) {
            case 176:
                hwEncoderKpbs = 300;
                break;
            case 320:
                hwEncoderKpbs = 500;
                break;
            case 640:
                hwEncoderKpbs = 1000;
                break;
            case 1280:
                hwEncoderKpbs = 1700;
                break;
            default:
                hwEncoderKpbs = 1000;
        }

        return hwEncoderKpbs;
    }

    /**
     * 根据目录创建文件夹
     *
     * @param context
     * @param cacheDir
     * @return
     */
    public static File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        //判断sd卡正常挂载并且拥有权限的时候创建文件
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
            Log.d(Constant.TAG, "appCacheDir: " + appCacheDir);
        }
        if (appCacheDir == null || !appCacheDir.exists() && !appCacheDir.mkdirs()) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    /**
     * 检查是否有权限
     *
     * @param context
     * @return
     */
    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        return perm == 0;
    }

    private byte[] ReadAssetFileDataToByte(InputStream in) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int c = 0;
        if(in != null){
            while ((c = in.read()) != -1) {
                bytestream.write(c);
            }
        }
        byte bytedata[] = bytestream.toByteArray();
        bytestream.close();
        return bytedata;
    }

    private void initView(View view) {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);    //屏幕常亮
        myContext = this.getActivity().getApplicationContext();
        //设置快照路径(具体路径可自行设置)
        File storageDir = getOwnCacheDirectory(myContext, "LuoyiPublisher");//创建保存的路径
        imageSavePath = storageDir.getPath();
        Log.d(Constant.TAG, "快照存储路径: " + imageSavePath);

        markSizeView = new MarkSizeView(getActivity());
        captureView = view.findViewById(R.id.one);
        ly1 = (LinearLayout) view.findViewById(R.id.top_ly);
        ly2 = (LinearLayout) view.findViewById(R.id.bottom_ly);
        drawArea = (ImageView) view.findViewById(R.id.drawArea);
        captureTips = new TextView(getActivity());
        captureAll = new TextView(getActivity());
        btnRecoderMgr = (ImageView) view.findViewById(R.id.button_recoder_manage);
        btnRecoderMgr.setOnClickListener(new ButtonRecorderMangerListener());
        //end

        btnMute = (ImageView) view.findViewById(R.id.button_mute);
        btnStartRecorder = (ImageView) view.findViewById(button_start_recorder);
        btnStartRecorder.setOnClickListener(new ButtonStartRecorderListener());
        btnStartPush = (ImageView) view.findViewById(R.id.button_start_push);
        imgSwitchCamera = (ImageView) view.findViewById(R.id.button_switchCamera);
        mSurfaceView = (SurfaceView) mView.findViewById(R.id.surface);
        captureSetting = (ImageView) view.findViewById(R.id.capture_setting);
    }

    private void initEvent(View view) {

        btnMute.setOnClickListener(new ButtonMuteListener());
        btnStartPush.setOnClickListener(new ButtonStartPushListener());
        btnStartRecorder.setOnClickListener(new ButtonStartRecorderListener());
        imgSwitchCamera.setOnClickListener(new SwitchCameraListener());

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //自动聚焦变量回调
        myAutoFocusCallback = new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                //success表示对焦成功
                /*if (success) {
                    Log.d(Constant.TAG, "onAutoFocus succeed...");
                } else {
                    Log.d(Constant.TAG, "onAutoFocus failed...");
                }*/
            }
        };
        libPublisher = new SmartPublisherJni();

        /**
         * 监控设置
         */
        captureSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getFragmentManager() ;
                FragmentTransaction ft = fm.beginTransaction();
                CaptureSettingFragment csf = new CaptureSettingFragment();
                ft.replace(R.id.lyFrame, csf);
                ft.commit();
            }
        });

        /**
         * 绘制警戒区域
         */
        drawArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                captureView.getGlobalVisibleRect(defaultMonitorArea);
                ly1.getGlobalVisibleRect(two);
                drawArea.setImageDrawable(getResources().getDrawable(R.drawable.draw_area));
                rlCapture = (RelativeLayout) mView.findViewById(R.id.rl_capture_pw);
                if(!isAddMarkSizeView(rlCapture)){
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    markSizeView.setLayoutParams(params);
                    rlCapture.addView(markSizeView);


                    RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    rlParams.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
                    captureTips.setTextColor(R.color.white);
                    captureTips.setLayoutParams(rlParams);
                    captureTips.setTextSize(20);
                    captureTips.setGravity(Gravity.CENTER);
                    captureTips.setText("请绘制监控区域");
                    rlCapture.addView(captureTips);

                    rlParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    rlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
                    rlParams.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
                    rlParams.setMargins(0,0,0,10);
                    captureAll.setTextColor(R.color.white);
                    captureAll.setTextSize(18);
                    captureAll.setLayoutParams(rlParams);
                    captureAll.setGravity(Gravity.CENTER);
                    captureAll.setText("全屏监控");
                    rlCapture.addView(captureAll);
                }
                markSizeView.setUnmarkedColor(markSizeView.DEFAULT_UNMARKED_COLOR);
                markSizeView.setEnabled(true);
                captureTips.setVisibility(View.VISIBLE);
                markSizeView.callOnClick();
            }
        });

        /**
         * 绘制区域监听
         */
        markSizeView.setmOnClickListener(new MarkSizeView.onClickListener() {
            @Override
            public void onConfirm(Rect markedArea, MotionEvent event) {
                setMarkAreaCoordinate(markedArea);
                markSizeView.reset();
                markSizeView.setUnmarkedColor(getResources().getColor(R.color.transparent));
                markSizeView.setEnabled(false);
                drawArea.setImageDrawable(getResources().getDrawable(R.drawable.area));
                Toast.makeText(getActivity(), "监控区域已更新",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConfirm(MarkSizeView.GraphicPath path) {
                mGraphicPath = path;
                markSizeView.reset();
                markSizeView.setUnmarkedColor(getResources().getColor(R.color.transparent));
                markSizeView.setEnabled(false);
                drawArea.setImageDrawable(getResources().getDrawable(R.drawable.area));
            }

            @Override
            public void onCancel() {
                captureTips.setVisibility(View.VISIBLE);
                captureAll.setVisibility(View.VISIBLE);
                drawArea.setImageDrawable(getResources().getDrawable(R.drawable.draw_area));
            }

            @Override
            public void onTouch() {
                captureTips.setVisibility(View.GONE);
                captureAll.setVisibility(View.GONE);
            }
        });

        /**
         * 选择全屏监控
         */
        captureAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markSizeView.setUnmarkedColor(getResources().getColor(R.color.transparent));
                captureTips.setVisibility(View.GONE);
                captureAll.setVisibility(View.GONE);

                Rect fullScreen =  new Rect(1,1,1,1);
                setMarkAreaCoordinate(fullScreen);
                markSizeView.reset();
                markSizeView.setEnabled(false);
                drawArea.setImageDrawable(getResources().getDrawable(R.drawable.area));
                Toast.makeText(getActivity(), "已设置全屏监控",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 判断是否是MarkSizeView
     * @param layout
     * @return
     */
    private boolean isAddMarkSizeView(RelativeLayout layout) {
        View view = null;
        for (int index = layout.getChildCount(); index > 0; index--) {
            view = layout.getChildAt(index);
            if (view != null && view instanceof MarkSizeView) {
                return true;
            }
        }
        return false;
    }


    /**
     * desc 设置监控区域坐标
     * @param
     * @return
     * @author wwc
     * Created on 2017/10/2 22:51
     */
    private void setMarkAreaCoordinate(Rect markedArea){

        mMarkedArea = new Rect();
        mMarkedArea.top = getRightY(getStretchY(markedArea.top));
        mMarkedArea.bottom = getRightY(getStretchY(markedArea.bottom));
        mMarkedArea.left = getRightX(markedArea.left);
        mMarkedArea.right = getRightX(markedArea.right);

        /*mMarkedArea.left = getRightX(rlCapture.getHeight() - markedArea.bottom);
        //mMarkedArea.top = defaultMonitorArea.top+markedArea.top;
        mMarkedArea.top = getRightY(markedArea.left);
        mMarkedArea.right = getRightX(rlCapture.getHeight() - markedArea.top);
        mMarkedArea.bottom = getRightY(markedArea.right);*/
    }

    /**
     * desc 获得拉伸后的y坐标
     * @param
     * @return
     * @author wwc
      * Created on 2017/10/12 9:28
      */
    private int getStretchY(int orginalY){
        int newY = Math.round(screenHeight * orginalY / (float) rlCapture.getHeight());
        return newY;
    }

    /**
     * desc 根据屏幕分辨率及预览分辨率调整y坐标
     * @param
     * @return
     * @author wwc
      * Created on 2017/10/12 9:29
      */
    private int getRightY(int y){
        float newY = (y * (float)videoWidth) / (float)screenHeight;
        return Math.round(newY);
    }

    /**
     * 根据屏幕分辨率及预览分辨率调整x坐标
     * @param x
     * @return
     */
    private int getRightX(int x){
        float newX = (x * (float) videoHeight) / (float)screenWidth;
        return  Math.round(newX);
    }

    /**
     * desc 切换前后摄像头
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/30 22:49
     */
    class SwitchCameraListener implements View.OnClickListener {
        public void onClick(View v) {
            Log.d(Constant.TAG, "Switch camera..");
            try {
                switchCamera();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * desc 录像管理
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:37
     */
    class ButtonRecorderMangerListener implements View.OnClickListener {
        public void onClick(View v) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }

            Intent intent = new Intent();
            intent.setClass(getActivity(), RecorderManager.class);
            intent.putExtra("RecoderDir", recDir);
            startActivity(intent);
        }
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
     * desc 启用静音
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:39
     */
    class ButtonMuteListener implements View.OnClickListener {
        public void onClick(View v) {
            is_mute = !is_mute;

            if (is_mute){
                //btnMute.setText("取消静音");
                Log.d(Constant.TAG, "取消静音");
                Toast.makeText(getActivity(), "取消静音", Toast.LENGTH_SHORT).show();
                btnMute.setImageDrawable(getResources().getDrawable(R.drawable.enable_mute));
            }
            else{
                //btnMute.setText("静音");
                Toast.makeText(getActivity(), "开启静音", Toast.LENGTH_SHORT).show();
                Log.d(Constant.TAG, "静音");
                btnMute.setImageDrawable(getResources().getDrawable(R.drawable.mute));
            }
            if (libPublisher != null)
                libPublisher.SmartPublisherSetMute(is_mute ? 1 : 0);
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
                    Log.d(Constant.TAG, "开始一个新的录像文件 : " + param3);
                    txt = "开始一个新的录像文件。。";
                    break;
                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_ONE_RECORDER_FILE_FINISHED:
                    Log.d(Constant.TAG, "已生成一个录像文件 : " + param3);
                    txt = "已生成一个录像文件。。";
                    break;

                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_SEND_DELAY:
                    Log.d(Constant.TAG, "发送时延: " + param1 + " 帧数:" + param2);
                    txt = "收到发送时延..";
                    break;

                case EVENTID.EVENT_DANIULIVE_ERC_PUBLISHER_CAPTURE_IMAGE:
                    Log.d(Constant.TAG, "快照: " + param1 + " 路径：" + param3);

                    if (param1 == 0) {
                        txt = "截取快照成功。.";
                    } else {
                        txt = "截取快照失败。.";
                    }
                    break;
            }

            String str = "当前回调状态：" + txt;

            //Log.d(Constant.TAG, str);

        }
    }



    /**
     * desc 开始推流
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 8:41
     */
    class ButtonStartPushListener implements View.OnClickListener {
        public void onClick(View v) {
            if (isStart) {
                return;
            }

            if (isPushing) {
                stopPush();

                if (!isRecording) {
                    ConfigControlEnable(true);
                }
                //btnStartPush.setText(" 推送");
                isPushing = false;
                btnStartPush.setImageDrawable(getResources().getDrawable(R.drawable.monitor));
                return;
            }
            Log.d(Constant.TAG, "onClick start push..");

            if (libPublisher == null)
                return;
            isPushing = true;
            btnStartPush.setImageDrawable(getResources().getDrawable(R.drawable.enable_monitor));

            if (!isRecording) {
                InitAndSetConfig();
            }

            if (inputPushURL != null && inputPushURL.length() > 1) {
                publishURL = inputPushURL;
                Log.d(Constant.TAG, "start, input publish url:" + publishURL);
            } else {
                //publishURL = baseURL + String.valueOf((int)( System.currentTimeMillis() % 1000000));
                publishURL = DeviceManager.getInstance().getDevice().getPushUrl();
                Log.d(Constant.TAG, "开始推流，推流URL:" + publishURL);
                //publishURL = baseURL;
            }
            printText = "URL:" + publishURL;
            Log.d(Constant.TAG, printText);

            if (libPublisher.SmartPublisherSetURL(publishURL) != 0) {
                Log.d(Constant.TAG, "Failed to set publish stream URL..");
            }

            int startRet = libPublisher.SmartPublisherStartPublisher();
            if (startRet != 0) {
                isPushing = false;
                btnStartPush.setImageDrawable(getResources().getDrawable(R.drawable.monitor));
                Log.d(Constant.TAG, "Failed to start push stream..");
                return;
            }

            if (!isRecording) {
                if (pushType == 0 || pushType == 1) {
                    CheckInitAudioRecorder();    //enable pure video publisher..
                }
            }

            if (!isRecording) {
                ConfigControlEnable(false);
            }

            /*textCurURL = (TextView) mView.findViewById(R.id.txtCurURL);
            textCurURL.setText(printText);*/

            //btnStartPush.setText(" 停止推送 ");
            Toast.makeText(getActivity(),"开始监控",Toast.LENGTH_SHORT).show();
            mFrameProcess.setmVideoWidth(videoWidth);
            mFrameProcess.setmVideoHeight(videoHeight);
            mFrameProcess.firstFrame = new int[videoWidth * videoHeight];
            mFrameProcess.secondFrame = new int[videoWidth * videoHeight];
            mFrameProcess.thirdFrame = new int[videoWidth * videoHeight];
            mFrameProcess.buff = new int[videoWidth * videoHeight];
            frameProcessThread = new Thread(mFrameProcess);
            frameProcessThread.start();
            onStartMonitor();
            timer = new Timer();
            initTimeTask();
            timer.schedule(timerTask, 0, SAVE_COVER_PERIOD);
        }

    }

    class ButtonStartRecorderListener implements View.OnClickListener {
        public void onClick(View v) {
            if (isStart) {
                return;
            }

            if (isRecording) {
                stopRecorder();

                if (!isPushing) {
                    ConfigControlEnable(true);
                }

                //btnStartRecorder.setText(" 录像");
                Toast.makeText(getActivity(),"开始录像",Toast.LENGTH_SHORT).show();
                Log.d(Constant.TAG,"录像");
                isRecording = false;
                btnStartRecorder.setImageDrawable(getResources().getDrawable(R.drawable.recording));
                return;
            }


            Log.d(Constant.TAG, "onClick start recorder..");

            if (libPublisher == null)
                return;

            isRecording = true;
            btnStartRecorder.setImageDrawable(getResources().getDrawable(R.drawable.enable_recording));
            if (!isPushing) {
                InitAndSetConfig();
            }

            ConfigRecorderFunction(true);

            int startRet = libPublisher.SmartPublisherStartRecorder();
            if (startRet != 0) {
                isRecording = false;

                Log.d(Constant.TAG, "Failed to start recorder.");
                Toast.makeText(getActivity(),"启动录像失败",Toast.LENGTH_SHORT).show();
                btnStartRecorder.setImageDrawable(getResources().getDrawable(R.drawable.recording));
                return;
            }

            if (!isPushing) {
                if (pushType == 0 || pushType == 1) {
                    CheckInitAudioRecorder();    //enable pure video publisher..
                }
            }

            if (!isPushing) {
                ConfigControlEnable(false);
            }

            //btnStartRecorder.setText("停止录像");
            Toast.makeText(getActivity(),"停止录像",Toast.LENGTH_SHORT).show();
            Log.d(Constant.TAG,"停止录像");

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

            Log.d(Constant.TAG, "imagePath:" + imagePath);

            libPublisher.SmartPublisherSaveCurImage(imagePath);
        }
    }
    class ButtonStartListener implements View.OnClickListener {
        public void onClick(View v) {
            if (isPushing || isRecording) {
                return;
            }

            if (isStart) {
                stop();
                btnRecoderMgr.setEnabled(true);
                btnHWencoder.setEnabled(true);

                btnNoiseSuppression.setEnabled(true);
                btnAGC.setEnabled(true);
                btnSpeex.setEnabled(true);

                return;
            }

            isStart = true;
            btnStartStop.setText(" 停止推流 ");
            Log.d(Constant.TAG, "onClick start..");

            if (libPublisher != null) {
                publishURL = DeviceManager.getInstance().getDevice().getPushUrl();
                Log.d(Constant.TAG, "开始推流，推流URL:" + publishURL);
                /*if (inputPushURL != null && inputPushURL.length() > 1) {
                    publishURL = inputPushURL;
                    Log.d(Constant.TAG, "start, input publish url:" + publishURL);
                } else {
                    //publishURL = baseURL + String.valueOf((int) (System.currentTimeMillis() % 1000000));

                    Log.d(Constant.TAG, "start, generate random url:" + publishURL);

                }*/

                printText = "URL:" + publishURL;

                Log.d(Constant.TAG, printText);

                textCurURL = (TextView) mView.findViewById(R.id.txtCurURL);
                textCurURL.setText(printText);

                ConfigRecorderFunction(is_need_local_recorder);

                Log.d(Constant.TAG, "videoWidth: " + videoWidth + " videoHeight: " + videoHeight + " pushType:" + pushType);

                int audio_opt = 1;
                int video_opt = 1;

                if (pushType == 1) {
                    video_opt = 0;
                } else if (pushType == 2) {
                    audio_opt = 0;
                }

                libPublisher.SmartPublisherInit(myContext, audio_opt, video_opt, videoWidth, videoHeight);

                if (is_hardware_encoder) {
                    int hwHWKbps = setHardwareEncoderKbps(videoWidth, videoHeight);

                    Log.d(Constant.TAG, "hwHWKbps: " + hwHWKbps);

                    int isSupportHWEncoder = libPublisher.SetSmartPublisherVideoHWEncoder(hwHWKbps);

                    if (isSupportHWEncoder == 0) {
                        Log.d(Constant.TAG, "Great, it supports hardware encoder!");
                    }
                }

                libPublisher.SetSmartPublisherEventCallback(new EventHande());

                //如果想和时间显示在同一行，请去掉'\n'
                String watermarkText = "大牛直播(daniulive)\n\n";

                String path = logoPath;

                if (watemarkType == 0) {
                    if (isWritelogoFileSuccess)
                        libPublisher.SmartPublisherSetPictureWatermark(path, WATERMARK.WATERMARK_POSITION_TOPRIGHT, 160, 160, 10, 10);
                } else if (watemarkType == 1) {
                    if (isWritelogoFileSuccess)
                        libPublisher.SmartPublisherSetPictureWatermark(path, WATERMARK.WATERMARK_POSITION_TOPRIGHT, 160, 160, 10, 10);

                    libPublisher.SmartPublisherSetTextWatermark(watermarkText, 1, WATERMARK.WATERMARK_FONTSIZE_BIG, WATERMARK.WATERMARK_POSITION_BOTTOMRIGHT, 10, 10);

                    //libPublisher.SmartPublisherSetTextWatermarkFontFileName("/system/fonts/DroidSansFallback.ttf");

                    //libPublisher.SmartPublisherSetTextWatermarkFontFileName("/sdcard/DroidSansFallback.ttf");
                } else if (watemarkType == 2) {
                    libPublisher.SmartPublisherSetTextWatermark(watermarkText, 1, WATERMARK.WATERMARK_FONTSIZE_BIG, WATERMARK.WATERMARK_POSITION_BOTTOMRIGHT, 10, 10);

                    //libPublisher.SmartPublisherSetTextWatermarkFontFileName("/system/fonts/DroidSansFallback.ttf");
                } else {
                    Log.d(Constant.TAG, "no watermark settings..");
                }
                //end


                if (!is_speex) {
                    // set AAC encoder
                    libPublisher.SmartPublisherSetAudioCodecType(1);
                } else {
                    // set Speex encoder
                    libPublisher.SmartPublisherSetAudioCodecType(2);
                    libPublisher.SmartPublisherSetSpeexEncoderQuality(8);
                }

                libPublisher.SmartPublisherSetNoiseSuppression(is_noise_suppression ? 1 : 0);

                libPublisher.SmartPublisherSetAGC(is_agc ? 1 : 0);

                //libPublisher.SmartPublisherSetClippingMode(0);

                libPublisher.SmartPublisherSetSWVideoEncoderProfile(sw_video_encoder_profile);

                libPublisher.SmartPublisherSetSWVideoEncoderSpeed(sw_video_encoder_speed);

                libPublisher.SmartPublisherSaveImageFlag(1);

                //libPublisher.SetRtmpPublishingType(0);


                //libPublisher.SmartPublisherSetGopInterval(40);

                //libPublisher.SmartPublisherSetFPS(15);

                //libPublisher.SmartPublisherSetSWVideoBitRate(600, 1200);
                // IF not set url or url is empty, it will not publish stream
                // if ( libPublisher.SmartPublisherSetURL("") != 0 )
                if (libPublisher.SmartPublisherSetURL(publishURL) != 0) {
                    Log.d(Constant.TAG, "Failed to set publish stream URL..");
                }

                int isStarted = libPublisher.SmartPublisherStart();
                if (isStarted != 0) {
                    Log.d(Constant.TAG, "Failed to publish stream..");
                } else {
                    btnRecoderMgr.setEnabled(false);
                    btnHWencoder.setEnabled(false);

                    btnNoiseSuppression.setEnabled(false);
                    btnAGC.setEnabled(false);
                    btnSpeex.setEnabled(false);
                }
            }

            if (pushType == 0 || pushType == 1) {
                CheckInitAudioRecorder();    //enable pure video publisher..
            }
        }
    }
    class ButtonStopListener implements View.OnClickListener {
        public void onClick(View v) {
            //onDestroy();
        }
    }
    /**
     * 自定义handler
     */
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
                Map<String, String> map = (Map<String, String>) msg.obj;
                if (map.get("msg").equals(Constant.INVASION)) {
                    String saveFilePath = map.get("saveFilePath");
                    Toast.makeText(getActivity(), "有物体入侵，st=" + map.get("st") + ",et=" + map.get("et"), Toast.LENGTH_SHORT).show();
                    AlarmLogManager.getInstance().addAlarmLog(
                            getActivity(), DeviceManager.getInstance().getDevice(), saveFilePath);
                }
        }

    }

    class CoverHandler extends android.os.Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mBitmaps != null && mBitmaps.size() > 0){
                DeviceManager.getInstance().updateMointorCover(mBitmaps.get(0));
            }
        }
    }

    private void initTimeTask(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                coverHandler.sendMessage(message);
            }
        };
    }
}
