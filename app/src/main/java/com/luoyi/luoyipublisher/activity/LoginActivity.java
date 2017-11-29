package com.luoyi.luoyipublisher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.luoyi.luoyipublisher.R;
import com.luoyi.luoyipublisher.bean.User;
import com.luoyi.luoyipublisher.util.Constant;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by wwc on 2017/8/6.
 */
@ContentView(R.layout.login)
public class LoginActivity  extends AppCompatActivity{

    @ViewInject(R.id.username)
    private EditText etUsername;
    @ViewInject(R.id.password)
    private EditText etPassword;
    @ViewInject(R.id.btn_login)
    private Button btnLogin;
    @ViewInject(R.id.register)
    private TextView register;
    private String userId;
    private String password;
    public static User user;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        x.view().inject(this);
        initEven();
    }

    private void initEven(){

        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                userId = etUsername.getText().toString();
                password = etPassword.getText().toString();
                if(null == userId || "".equals(userId) || null == password || "".equals(password)){
                    Toast.makeText(getApplicationContext(),"用户名或密码不能为空！",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(user == null){
                        user = new User();
                        user.setUserId(userId);
                        user.setPassword(password);
                    }
                    login();
                    /*networkAsyncTask request = new networkAsyncTask();
                    HashMap<String,String> map = new HashMap<String,String>();
                    map.put("userId",userId);
                    map.put("pwd",pwd);*/
                    /*request.execute(map);*/
                }
            }
        });
        register.setClickable(true);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                LoginActivity.this.startActivityForResult(intent,999);
            }
        });
    }

    private void login(){
        RequestParams requestParams = new RequestParams(Constant.LOGIN_URL);
        requestParams.addBodyParameter("userId",etUsername.getText().toString());
        requestParams.addBodyParameter("pwd",etPassword.getText().toString());
        x.http().post(requestParams,loginCallback);
    }

    /*
    自定义登录回调接口
     */
    Callback.CommonCallback<String> loginCallback = new Callback.CommonCallback<String>() {
        @Override
        public void onSuccess(String result) {
                if(result.equals(Constant.LOGIN_SUCCESS)){
                    Toast.makeText(getApplicationContext(),"登录成功",Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    bundle.putString("userId",userId);
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.putExtras(bundle);
                    LoginActivity.this.startActivity(intent);
                    finish();
                }
                else if(result.equals(Constant.USRE_NOT_EXIST)){
                    Toast.makeText(getApplicationContext(),Constant.MSG_USRE_NOT_EXIST,Toast.LENGTH_SHORT).show();
                }
                else if(result.equals(Constant.NOT_MATCH)){
                    Toast.makeText(getApplicationContext(),Constant.MSG_NOT_MATCH,Toast.LENGTH_SHORT).show();
                }
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            Toast.makeText(getApplicationContext(),Constant.MSG_CONN_FAILURE,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancelled(CancelledException cex) {

        }

        @Override
        public void onFinished() {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RegisterActivity.RESULT_CODE) {
                etUsername.setText(user.getUserId());
                etPassword.setText(user.getPassword());
                login();
        }
    }

    /*@Event(R.id.btn_login)
    private void test(View v){
        switch (v.getId()){
            case R.id.btn_login :{
                String userId = username.getText().toString();
                String pwd = password.getText().toString();
                if(null == userId || "".equals(userId) || null == pwd || "".equals(pwd)){
                    Toast.makeText(getApplicationContext(),"用户名或密码不能为空！",Toast.LENGTH_SHORT).show();
                }
                else{
                    networkAsyncTask request = new networkAsyncTask();
                    HashMap<String,String> map = new HashMap<String,String>();
                    map.put("userId",userId);
                    map.put("pwd",pwd);
                    request.execute(map);
                }
            }
        }
    }
    class networkAsyncTask extends AsyncTask<HashMap<String,String>, Integer, String>{
        @Override
        protected String doInBackground(HashMap<String, String>... params) {
            HttpUtil httpUtil = new HttpUtil();
            String response = httpUtil.getInstance().httpURLConnectionPost(params[0],Constant.LOGIN_URL);
            return response;

        }

        @Override
        protected void onPostExecute(String s) {
            //未知原因，登录失败
            try {
                if(s == null){
                    Toast.makeText(getApplicationContext(),Constant.UNKNOW_ERO,Toast.LENGTH_SHORT).show();
                }
                else if(s.equals(Constant.LOGIN_SUCCESS)){
                    Toast.makeText(getApplicationContext(),"登录成功",Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    bundle.putString("userId",userId);
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.putExtras(bundle);
                    LoginActivity.this.startActivity(intent);
                    finish();
                }
                else if(s.equals(Constant.NOT_EXIST)){
                    Toast.makeText(getApplicationContext(),Constant.NOT_EXIST,Toast.LENGTH_SHORT).show();
                }
                else if(s.equals(Constant.NOT_MATCH)){
                    Toast.makeText(getApplicationContext(),Constant.NOT_MATCH,Toast.LENGTH_SHORT).show();
                }
                else if(s.equals(Constant.CONN_FAILURE)){
                    Toast.makeText(getApplicationContext(),Constant.CONN_FAILURE,Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if(null != e.getCause()){
                    Log.e(Constant.TAG,e.getCause().toString());
                }
                Log.e(Constant.TAG,e.getMessage());
            }
        }
    }*/
}
