package com.luoyi.luoyipublisher.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.luoyi.luoyipublisher.R;
import com.luoyi.luoyipublisher.bean.User;
import com.luoyi.luoyipublisher.util.Constant;
import com.luoyi.luoyipublisher.util.RegexValidateUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by wwc on 2017/8/29.
 */
@ContentView(R.layout.setting)
public class SettingActivity extends AppCompatActivity implements View.OnClickListener{


    private User user;


    @ViewInject(R.id.title_back)
    private ImageView back;
    @ViewInject(R.id.title_text)
    private TextView tieleText;
    @ViewInject(R.id.ly_logout)
    private View ly_logout;

    @ViewInject(R.id.tv_username)
    private TextView tvUsername;
    @ViewInject(R.id.tv_editPwd)
    private TextView tvPwd;
    @ViewInject(R.id.arrow1)
    private ImageView editPwd;

    @ViewInject(R.id.tv_nickName)
    private TextView tvNickName;
    @ViewInject(R.id.arrow2)
    private ImageView editNick;

    @ViewInject(R.id.tv_phone)
    private TextView tvPhone;
    @ViewInject(R.id.arrow3)
    private ImageView editPhone;

    @ViewInject(R.id.tv_email)
    private TextView tvEmail;
    @ViewInject(R.id.arrow4)
    private ImageView editEmail;

    private AlertDialog logoutDialog = null;
    private AlertDialog nickDialog = null;
    private AlertDialog phoneDialog = null;
    private AlertDialog emailDialog = null;

    private View dialogView = null;
    private EditText inputText = null;
    private ImageView empty;

    private String lastNickname;
    private String lastPhone;
    private String lastEmail;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        tieleText.setText("设置");
        initEvent();
        initDialogView();
        initDialogListener();
        this.user = MainActivity.getUser();
        initUserInfo();
        ly_logout.setOnClickListener(this);
        editPwd.setOnClickListener(this);
        editNick.setOnClickListener(this);
        editEmail.setOnClickListener(this);
        editPhone.setOnClickListener(this);

    }

    private void initEvent(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void initDialogView(){
        dialogView = getLayoutInflater().inflate(R.layout.edit_dialog, null);
        inputText = (EditText) dialogView.findViewById(R.id.input);
        empty = (ImageView) dialogView.findViewById(R.id.empty);
    }
    private void initDialogListener(){
        //清空按钮
        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputText.setText("");
            }
        });
        logoutDialog = new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("退出登录后将无法继续监控，是否退出？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                        startActivity(intent);
                        finish();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        nickDialog = new AlertDialog.Builder(this)
                .setTitle("修改昵称")//设置对话框的标题
                .setView(dialogView)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!TextUtils.isEmpty(inputText.getText())){
                            if(!inputText.getText().toString().equals(lastNickname) ){
                                tvNickName.setText(inputText.getText());
                                user.setNickName(inputText.getText().toString());
                                lastNickname = user.getNickName();
                                updateUserInfo();
                            }
                        }
                        dialog.dismiss();
                    }
                }).create();

        phoneDialog = new AlertDialog.Builder(this)
                .setTitle("修改手机号")//设置对话框的标题
                .setView(dialogView)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!TextUtils.isEmpty(inputText.getText())) {
                            if (!inputText.getText().toString().equals(lastPhone)) {
                                if (RegexValidateUtil.checkMobileNumber(inputText.getText().toString())) {
                                    tvPhone.setText(inputText.getText());
                                    user.setPhone(inputText.getText().toString());
                                    lastPhone = user.getPhone();
                                    updateUserInfo();
                                } else {
                                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.illegalPhone).toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        dialog.dismiss();
                    }
                }).create();

        emailDialog = new AlertDialog.Builder(this)
                .setTitle("修改邮箱")//设置对话框的标题
                .setView(dialogView)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!TextUtils.isEmpty(inputText.getText())) {
                            if (!inputText.getText().toString().equals(lastEmail)) {
                                if (RegexValidateUtil.checkEmail(inputText.getText().toString())) {
                                    tvEmail.setText(inputText.getText());
                                    user.setEmail(inputText.getText().toString());
                                    lastEmail = user.getEmail();
                                    updateUserInfo();
                                } else {
                                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.illegalEmail).toString(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                        dialog.dismiss();
                    }
                }).create();
    }

    private void initUserInfo(){
        if(user != null){
            if(!TextUtils.isEmpty(user.getUserId())){
                tvUsername.setText(user.getUserId());
            }
            if(!TextUtils.isEmpty(user.getPhone()+"")){
                tvPhone.setText(user.getPhone()+"");
                lastPhone = user.getPhone();
            }
            if(!TextUtils.isEmpty(user.getNickName())){
                tvNickName.setText(user.getNickName());
                lastNickname = user.getNickName();
            }
            if(!TextUtils.isEmpty(user.getEmail())){
                tvEmail.setText(user.getEmail());
                lastEmail = user.getEmail();
            }
        }
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

    @Override
    public void onClick(View v) {
            initDialogView();
            initDialogListener();
            switch (v.getId()){
                case R.id.ly_logout:{
                    logoutDialog.show();
                }break;
                case R.id.arrow1:{
                    Intent intent = new Intent(getApplicationContext(), ChangePwdActivity.class);
                    startActivity(intent);
                }break;
                case R.id.arrow2:{
                    inputText.setText(tvNickName.getText());
                    nickDialog.show();
                }break;
                case R.id.arrow3:{
                    inputText.setText(tvPhone.getText());
                    phoneDialog.show();
                }break;
                case R.id.arrow4:{
                    inputText.setText(tvEmail.getText());
                    emailDialog.show();
                }break;
                default:{
                    break;
                }
            }
    }
}
