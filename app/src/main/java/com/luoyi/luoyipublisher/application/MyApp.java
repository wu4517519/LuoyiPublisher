package com.luoyi.luoyipublisher.application;

import android.app.Application;
import android.util.Log;

import com.luoyi.luoyipublisher.application.onestep.AppManager;
import com.luoyi.luoyipublisher.util.Constant;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import org.xutils.x;

/**
 * Created by wwc on 2017/8/11.
 */

public class MyApp extends Application {

    private static MyApp instance;
    public static PushAgent mPushAgent;//友盟推送代理

    public static MyApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(false); //输出debug日志，开启会影响性能
        instance = this;
        com.shang.commonjar.contentProvider.Global.init(this);
        mPushAgent = PushAgent.getInstance(this);
        initRegisterUmengPush();
        /*LeakCanary.install(this);
//        CrashHandler.getInstance().init(this);
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {

                KeepAliveWatcher.keepAlive(MyApp.this);
                startService(new Intent(MyApp.this, ListenClipboardService.class));
                startService(new Intent(MyApp.this, BigBangMonitorService.class));
                return false;
            }
        });*/
        AppManager.getInstance(this);
    }

    /**
     * desc 友盟注册代码
     * @param
     * @return
     * @author wwc
     * Created on 2017/10/5 17:37
     */
    private void initRegisterUmengPush() {

        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
                Log.d(Constant.TAG, "友盟注册成功，设备token为：" + deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.d(Constant.TAG, "友盟注册失败，错误码：" + s + ",错误信息：" + s1);
            }
        });
    }
}
