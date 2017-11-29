package com.luoyi.luoyipublisher.util.umeng;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.luoyi.luoyipublisher.util.umeng.notification.AndroidNotification;
import com.luoyi.luoyipublisher.util.umeng.notification.AndroidUnicast;
import com.luoyi.luoyipublisher.util.umeng.notification.UmengNotification;
import com.umeng.message.MessageSharedPrefs;
import com.umeng.message.util.HttpRequest;

import org.json.JSONObject;

import static org.xutils.common.util.MD5.md5;

/**
 * Created by wwc on 2017/10/6.
 */

public class CustomNotification {

    public static void transmission(final Context mContext, final Handler handler) {
        try {
            final AndroidUnicast unicast = new AndroidUnicast("59d6c8d8b27b0a713f00002e", "c6soazigulltkjwmulqvt7zqwlfthnne");
            unicast.setDeviceToken(MessageSharedPrefs.getInstance(mContext).getDeviceToken());
            unicast.setTicker("入侵警报！！！");
            unicast.setTitle("入侵检测");
            unicast.setText("有运动目标入侵境界区域，请立即查看！");
            unicast.setPlaySound(true);
            unicast.goAppAfterOpen();
            unicast.setDisplayType(AndroidNotification.DisplayType.NOTIFICATION);
            unicast.setProductionMode();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        send(unicast, mContext, handler);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {

        }
    }

    /**
     * desc 推送消息
     * @param 
     * @return
     * @author wwc
      * Created on 2017/10/6 9:22
      */
    public static void send(UmengNotification msg, final Context mContext, Handler handler) throws Exception {
        String timestamp = Integer.toString((int) (System.currentTimeMillis() / 1000));
        msg.setPredefinedKeyValue("timestamp", timestamp);

        String url = "http://msg.umeng.com/api/send";
        String postBody = msg.getPostBody();

        String p_sign = "POST" + url + postBody + msg.getAppMasterSecret();
        String sign = md5(p_sign);
        url = url + "?sign=" + sign;

        String response = HttpRequest.post(url).acceptJson()
                .send(postBody).body("UTF-8");
        JSONObject responseJson = new JSONObject(response);
        String ret = responseJson.getString("ret");

        if (!ret.equalsIgnoreCase("SUCCESS")) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "透传发送失败", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "透传发送成功", Toast.LENGTH_LONG).show();
                }
            });
        }

    }
}
