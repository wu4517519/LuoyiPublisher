package com.luoyi.luoyipublisher.common;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luoyi.luoyipublisher.activity.MainActivity;
import com.luoyi.luoyipublisher.bean.AlarmLog;
import com.luoyi.luoyipublisher.bean.Device;
import com.luoyi.luoyipublisher.util.Constant;
import com.luoyi.luoyipublisher.util.ImageUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by wwc on 2017/10/6.
 */

public class AlarmLogManager {
    private static AlarmLogManager alarmLogManager;

    private AlarmLog alarmLog;

    public AlarmLog getAlarmLog(){
        return alarmLog;
    }

    public static AlarmLogManager getInstance(){
        if(alarmLogManager == null){
            alarmLogManager = new AlarmLogManager();
        }
        return alarmLogManager;
    }

    public void addAlarmLog(final Context context, Device device, final String imgSavePath){
        AlarmLog alarmLog = new AlarmLog();
        alarmLog.setAndroidId(device.getAndroidId());
        alarmLog.setType(Constant.ALARM_TYPE_INVASION);
        alarmLog.setTime(new Timestamp(System.currentTimeMillis()));
        RequestParams requestParams = new RequestParams(Constant.ADD_ALARM_LOG);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        String alarmJson = gson.toJson(alarmLog);
        requestParams.addBodyParameter("alarmJson", alarmJson);
        x.http().post(requestParams, new Callback.CommonCallback<String>(){
            @Override
            public void onSuccess(String result) {
                if(result != null || !result.equals("") || !result.equals("null")){
                    Log.d(Constant.TAG,"添加警报日志成功");
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                    AlarmLog alarm = gson.fromJson(result, AlarmLog.class);
                    uploadAlarmImage(alarm, imgSavePath);
                    String message = "检测到目标入侵警戒区域，请立即查看！【落意监控】";
                    sendSMS(context, MainActivity.getUser().getPhone(), message);
                }
                else if(result.equals(Constant.FAILURE)){
                    Log.d(Constant.TAG,"添加警报日志失败");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.d(Constant.TAG,"addAlarmLog error");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    public static void uploadAlarmImage(AlarmLog alarmLog, String imgSavePath){

        File file = new File(imgSavePath);
        if(file.exists()){
            RequestParams requestParams = new RequestParams(Constant.UPLOAD_ALARM_IMG);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            requestParams.addBodyParameter("alarmJson", gson.toJson(alarmLog, AlarmLog.class));
            requestParams.addBodyParameter("alarmImage",file);
            x.http().post(requestParams, new Callback.CommonCallback<String>(){
                @Override
                public void onSuccess(String result) {
                    if(result != null || !result.equals("") || !result.equals("null") && result.equals(Constant.SUCCESS)){
                        Log.d(Constant.TAG,"上传监控图片成功");
                    }else if(result.equals("failure")){
                        Log.d(Constant.TAG,"上传监控图片失败");
                    }
                /*boolean delRet = ImageUtil.deleteBitmap(ImageUtil.lastSaveFilePath);
                if(delRet){
                    Log.d(Constant.TAG,"删除监控图片成功！");
                }*/
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    ex.printStackTrace();
                    Log.d(Constant.TAG,"onError 上传监控图片失败");
                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onFinished() {

                }
            });
        }

    }

    private void sendSMS(Context context, String phone, String message){
        SmsManager smsManager = SmsManager.getDefault();
        //自动拆分短信
        ArrayList<String> texts = smsManager.divideMessage(message);
        //迭代发送
        for (String text : texts) {
            smsManager.sendTextMessage(
                    phone,//destinationAddress：目的电话号码
                    null,//scAddress：短信中心电话号码为null时使用系统默认
                    text, //text：短信内容
                    null,//sentIntent：发送状态
                    null//deliveryIntent：对方接收状态
            );
        }
        handleSMSSendStatus(context);
        handleSMSRecStatus(context);
    }

    private void handleSMSSendStatus(Context context){
    //处理返回的发送状态
        String SENT_SMS_ACTION = "SENT_SMS_ACTION";
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent,
                0);
    // register the Broadcast Receivers
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.d(Constant.TAG,"发送短信成功");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                    default:
                        Log.d(Constant.TAG,"发送短信失败");
                }
            }
        }, new IntentFilter(SENT_SMS_ACTION));
    }

    private void handleSMSRecStatus(Context context){
    //处理返回的接收状态
        String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
    // create the deilverIntent parameter
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent deliverPI = PendingIntent.getBroadcast(context, 0,
                deliverIntent, 0);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                Log.d(Constant.TAG,"收信人已经成功接收");
            }
        }, new IntentFilter(DELIVERED_SMS_ACTION));
    }
}
