package com.luoyi.luoyipublisher.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.google.gson.Gson;
import com.luoyi.luoyipublisher.R;
import com.luoyi.luoyipublisher.bean.User;
import com.luoyi.luoyipublisher.common.DeviceManager;
import com.luoyi.luoyipublisher.fragment.CaptureFragment;
import com.luoyi.luoyipublisher.fragment.MineFragment;
import com.luoyi.luoyipublisher.util.Constant;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import static com.luoyi.luoyipublisher.application.MyApp.mPushAgent;

/**
 * Created by wwc on 2017/8/9.
 */
@ContentView(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener{

    private Context mContext;
    private CaptureFragment captureFragment;
    private MineFragment mineFragment;
    @ViewInject(R.id.bottom_navigation_bar)
    private BottomNavigationBar bottomNavigationBar;
    private TextView setting;
    private static User user;
    private int lastSelectedPosition = 0;
    private long exitTime = 0;
    private String userId;
    private String deviceToken;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        mContext = this;
        Bundle bundle = this.getIntent().getExtras();
        userId = bundle.getString("userId");
        initView();
        getUserInfo();
        checkDevice();
    }

    private void initView() {
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        /*bottomNavigationBar.setActiveColor("#FFFFFF");
        bottomNavigationBar.setInActiveColor("#CCCCCC");*/
        bottomNavigationBar.setBarBackgroundColor("#87CEFA");
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.capture, "监控").setActiveColorResource(R.color.lavenderblush).setInActiveColorResource(R.color.grey))
                .addItem(new BottomNavigationItem(R.drawable.mine,  "我的").setActiveColorResource(R.color.lavenderblush).setInActiveColorResource(R.color.grey))
                .setFirstSelectedPosition(lastSelectedPosition )
                .initialise();

        bottomNavigationBar.setTabSelectedListener(this);
        setDefaultFragment();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setDefaultFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        captureFragment = CaptureFragment.getInstance();
        transaction.replace(R.id.lyFrame, captureFragment);
        mineFragment = MineFragment.getInstance();
        transaction.commit();
    }

    @Override
    public void onTabSelected(int position) {
        FragmentManager fm = getFragmentManager() ;
        FragmentTransaction ft = fm.beginTransaction();
        try {
            switch(position){
                case 0:{
                    if(null == captureFragment){
                        captureFragment = CaptureFragment.getInstance();
                    }
                    ft.replace(R.id.lyFrame,captureFragment);
                }
                break;
                case 1:{
                    if(null == mineFragment){
                        mineFragment = MineFragment.getInstance();
                    }
                    ft.replace(R.id.lyFrame,mineFragment);
                }
                break;
                default:break;
            }
            ft.commit();
        } catch (Exception e) {
            Log.e(Constant.TAG,e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }

    public static User getUser(){
        return user;
    }

    /**
     * 获取用户信息
     * @author 吴文超
     * Created on 2017/8/11 21:20
     */

    private void getUserInfo(){
        RequestParams requestParams = new RequestParams(Constant.QUERY_USERINFO_URL);
        requestParams.addBodyParameter("userId",userId);
        x.http().post(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Gson gson = new Gson();
                user = gson.fromJson(result,User.class);
                if(user != null){
                    mineFragment.setUser(user);
                }
                if(mineFragment.isResumed()){
                    if(user.getNickName() == null){
                        mineFragment.setNickName(user.getUserId());
                    }
                    else{
                        mineFragment.setNickName(user.getNickName());
                    }
                    if(!"".equals(user.getProfile())){
                        mineFragment.loadProfile(Constant.BASE_PATH+"/"+user.getProfile());
                    }
                }

            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e(Constant.TAG,"getUserInfo Eror");
            }
            @Override
            public void onCancelled(CancelledException cex) {
            }
            @Override
            public void onFinished() {
            }
        });
    }

    private void checkDevice(){
        final String ANDROID_ID = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
        DeviceManager.getInstance().isDeviceExist(userId, ANDROID_ID, mPushAgent.getRegistrationId());
    }

}
