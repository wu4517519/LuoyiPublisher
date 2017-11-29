package com.luoyi.luoyipublisher.common;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luoyi.luoyipublisher.bean.AlarmLog;
import com.luoyi.luoyipublisher.bean.Device;
import com.luoyi.luoyipublisher.util.Constant;
import com.luoyi.luoyipublisher.util.ImageUtil;
import com.luoyi.luoyipublisher.util.StringUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

/**
 * Created by wwc on 2017/10/6.
 */

public class DeviceManager {

    private static DeviceManager deviceManager;
    private Device device;
    private boolean flag;

    public Device getDevice(){
        return device;
    }

    public static DeviceManager getInstance(){
        if(deviceManager == null){
            deviceManager = new DeviceManager();
        }
        return deviceManager;
    }

    public Device addDevice(String userId, String androidId, String deviceToken){
        Device newDevice = new Device();
        newDevice.setUserid(userId);
        newDevice.setAndroidId(androidId);
        newDevice.setDeviceToken(deviceToken);
        newDevice.setDeviceType(0);
        newDevice.setIsOnline(0);
        newDevice.setPushUrl(Constant.PUSH_URL_PREFIX+userId+StringUtil.getRandomString());
        Log.d(Constant.TAG,"推流URL"+newDevice.getPushUrl());
        RequestParams requestParams = new RequestParams(Constant.ADD_DEVICE);
        String deviceJson = new Gson().toJson(newDevice, Device.class);
        requestParams.addBodyParameter("deviceJson",deviceJson);
        x.http().post(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if(result != null || !result.equals("") || !result.equals("null")){
                    device = new Gson().fromJson(result, Device.class);
                    Log.d(Constant.TAG,"添加设备成功");
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                device = null;
                Log.d(Constant.TAG,"添加设备失败");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
        return device;
    }

    public void isDeviceExist(final String userId, final String ANDROID_ID,final String deviceToken){
        RequestParams requestParams = new RequestParams(Constant.FIND_DEVICE_BY_ANDROID_ID);
        requestParams.addBodyParameter("androidId",ANDROID_ID);
        x.http().post(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if(result.equals("null")){
                    Log.d(Constant.TAG, "查找设备不存在");
                    DeviceManager.getInstance().addDevice(userId, ANDROID_ID, deviceToken);
                }
                else if(result.equals("")){
                    Log.d(Constant.TAG, "查找设备失败");
                }
                else{
                    Log.d(Constant.TAG, "该设备已存在");
                    device = new Gson().fromJson(result, Device.class);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                device = null;
                Log.d(Constant.TAG, "onError 查找设备失败");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    public void updateDeviceOnlineStatus(final boolean isOnline){
        if(device != null){
            final RequestParams requestParams = new RequestParams(Constant.UPDATE_DEVICE_ONLINE_STATUS);
            requestParams.addBodyParameter("id",device.getId()+"");
            requestParams.addBodyParameter("isOnline",isOnline == true ? "1" : "0");
            x.http().post(requestParams, new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    if(result != null || !result.equals("") || !result.equals("null")){
                        String s = isOnline == true ? "online":"offline";
                        if(result.equals(Constant.SUCCESS)){
                            Log.d(Constant.TAG,"设备"+device.getAndroidId()+"已更新状态为"+s);
                        }
                    }

                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    Log.d(Constant.TAG,"设备联网状态更新失败");
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

    public void updateMointorCover(Bitmap coverBitmap){
        if(coverBitmap != null){
            final File tempFile = new File(ImageUtil.saveBitmap(coverBitmap, "coverBitmap"));
            RequestParams requestParams = new RequestParams(Constant.UPLOAD_COVER_IMG);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            requestParams.addBodyParameter("id", DeviceManager.getInstance().device.getId()+"");
            requestParams.addBodyParameter("coverImg",tempFile);
            x.http().post(requestParams, new Callback.CommonCallback<String>(){
                @Override
                public void onSuccess(String result) {
                    if(result != null || !result.equals("") || !result.equals("null") && result.equals(Constant.SUCCESS)){
                        Log.d(Constant.TAG,"上传封面图片成功");
                    }else if(result.equals("failure")){
                        Log.d(Constant.TAG,"上传封面图片失败");
                    }
                boolean delRet = ImageUtil.deleteBitmap(tempFile.getAbsolutePath());
                if(delRet){
                    Log.d(Constant.TAG,"删除封面图片成功！");
                }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    ex.printStackTrace();
                    Log.d(Constant.TAG,"onError 上传封面图片时出现错误");
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

}
