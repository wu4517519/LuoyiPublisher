package com.luoyi.luoyipublisher.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.luoyi.luoyipublisher.R;
import com.luoyi.luoyipublisher.bean.User;
import com.luoyi.luoyipublisher.util.Constant;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by wwc on 2017/8/30.
 */
@ContentView(R.layout.change_pwd)
public class ChangePwdActivity extends AppCompatActivity {

    @ViewInject(R.id.title_back)
    private ImageView back;
    @ViewInject(R.id.title_text)
    private TextView tieleText;
    @ViewInject(R.id.oldPwd)
    private EditText oldPwd;
    @ViewInject(R.id.newPwd)
    private EditText newPwd;
    @ViewInject(R.id.change)
    private Button change;

    private User user;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        tieleText.setText("修改密码");
        user = MainActivity.getUser();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(oldPwd.getText()) && !TextUtils.isEmpty(newPwd.getText())){
                    if(user.getPassword().equals(oldPwd.getText().toString())){
                        user.setPassword(newPwd.getText().toString());
                        updateUserInfo();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), Constant.MSG_ERR_PWD, Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), Constant.MSG_INPUT_INCOMPLETE, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 更新用户信息
     * @author 吴文超
     * Created on 2017/8/30 9:46
     */

    private void updateUserInfo(){
        RequestParams requestParams = new RequestParams(Constant.UPDATE_USERINFO_URL);
        Gson gson = new Gson();
        String userJson = gson.toJson(user,User.class);
        requestParams.addBodyParameter("userJson",userJson);
        x.http().post(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if(result.equals(Constant.UPDATE_USERINFO_SUCCESS)){
                    Toast.makeText(getApplicationContext(), Constant.MSG_UPDATE_USERINFO_SUCCESS, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

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
