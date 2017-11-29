package com.luoyi.luoyipublisher.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.luoyi.luoyipublisher.R;
import com.luoyi.luoyipublisher.bean.User;
import com.luoyi.luoyipublisher.util.Constant;
import com.luoyi.luoyipublisher.util.StorageUtil;
import com.luoyi.luoyipublisher.view.CircleImageView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wwc on 2017/8/12.
 */

@ContentView(R.layout.register)
public class RegisterActivity extends AppCompatActivity {

    @ViewInject(R.id.edit_profile)
    private TextView editProfile;
    @ViewInject(R.id.register_phone)
    private TextView etPhone;
    @ViewInject(R.id.register_password)
    private TextView etPassword;


    @ViewInject(R.id.profile)
    private CircleImageView ivProfile;
    @ViewInject(R.id.btn_register)
    private Button register;

    private ListView listView;
    private PopupWindow popupWindow;
    private List<String> list;
    private View popupParentView;
    private Bitmap profile;//头像
    private File newProfile;
    public static final int RESULT_CODE = 1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        initEven();
        initPopupWindow();
    }

    /**
     * 初始化事件
     * @author 吴文超
     * Created on 2017/8/13 10:27
     */
    
    private void initEven(){
        editProfile.setClickable(true);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!popupWindow.isShowing()){
                    popupWindow.showAtLocation(popupParentView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,0,0);
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = etPhone.getText().toString();
                String password = etPassword.getText().toString();
                if(phone == null || phone.equals("")){
                    Toast.makeText(getApplicationContext(), Constant.MSG_PHONE_EMPTY, Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(password == null || password.equals("")){
                    Toast.makeText(getApplicationContext(),Constant.MSG_PASSWORD_EMPTY, Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    checkPhone(phone);
                }
            }
        });
    }

    /**
     * 初始化弹窗
     * @author 吴文超
     * Created on 2017/8/13 10:21
     */
    
    private void initPopupWindow(){
        popupWindow = new PopupWindow(RegisterActivity.this);
        popupWindow.setWidth(LinearLayoutCompat.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        //加载弹窗布局
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View popupView = layoutInflater.inflate(R.layout.popup_window,null);
        listView = (ListView) popupView.findViewById(R.id.lv_profile);
        getData();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.popup_window_item, R.id.item, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new MyItemOnclick());
        popupWindow.setContentView(popupView);
        //获得焦点，使得返回时不会退出Activity
        popupWindow.setFocusable(true);
        //点击外部区域使弹窗消失
        popupWindow.setOutsideTouchable(true);
        //设置背景为透明
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setAnimationStyle(R.style.PopupWindowsAnimation);
        popupParentView = layoutInflater.inflate(R.layout.register,null);

    }

    /**
     * 获得弹窗内容
     * @author 吴文超
     * Created on 2017/8/13 10:21
     */
    
    private List<String> getData(){
        if(list == null){
            list = new ArrayList<String>();
            list.add(this.getResources().getString(R.string.gallery));
            list.add(this.getResources().getString(R.string.take_picture));
            list.add(this.getResources().getString(R.string.cancel));
        }
        return list;
    }

    /**
     * 自定义的实现OnItemClickListener接口的类
     * @author 吴文超
     * Created on 2017/8/13 16:16
     */

    class MyItemOnclick implements AdapterView.OnItemClickListener {
        private AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        private final AlertDialog dialog = builder.create();
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String text = list.get(position);
            final String gallery = getApplicationContext().getResources().getString(R.string.gallery);
            final String take_picture = getApplicationContext().getResources().getString(R.string.take_picture);
            final String cancel = getApplicationContext().getResources().getString(R.string.cancel);
            if(text.equals(gallery)){
                selectFromGallery(dialog);
            }
            else if(text.equals(take_picture)){
                take_picture(dialog);
            }
            else if(text.equals(cancel)){
                cancel();
            }
        }
    }

    /**
     * 从图库选择图片
     * @author 吴文超
     * Created on 2017/8/13 16:18
     */

    private void selectFromGallery(AlertDialog dialog){
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 0);
        dialog.dismiss();
    }

    /**
     * 拍照
     * @author 吴文超
     * Created on 2017/8/13 16:19
     */

    private void take_picture(AlertDialog dialog){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        newProfile = new File(Constant.CACHE_PATH, getRandomFileName());
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(newProfile));
        startActivityForResult(intent, 1);// 采用ForResult打开
        dialog.dismiss();
    }

    /**
     * 取消
     * @author 吴文超
     * Created on 2017/8/13 16:19
     */

    private void cancel(){

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 0:{//完成从图库选取
                if(resultCode  == RESULT_OK){
                    cropPhoto(data.getData());
                }
            }
            break;
            case 1:{//完成拍照
                if (resultCode == RESULT_OK) {
                    cropPhoto(Uri.fromFile(newProfile));// 裁剪图片
                }
            }
            break;
            case 2:{//裁剪完成
                if(data  != null){
                    Bundle extras = data.getExtras();
                    profile = extras.getParcelable("data");
                    if (profile != null) {
                        /**
                         * 上传服务器代码
                         */
                        savePicToLocal(profile);// 保存在SD卡中
                        ivProfile.setImageBitmap(profile);// 用ImageView显示出来
                    }
                }
            }
            break;
            default:
                break;
        }
    }

    /**
     * 调用系统裁剪图片
     * @author 吴文超
     * Created on 2017/8/13 16:29
     */

    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 2);
    }

    /**
     * 保存图片到本地
     * @author 吴文超
     * Created on 2017/8/13 16:35
     */

    private void savePicToLocal(Bitmap pic){
        String sdDir = StorageUtil.getSDCardDir();
        if(Constant.CACHE_PATH == null){
            return;
        }
        String savePath = Constant.CACHE_PATH;
        String fileName = new Date().getTime()+".jpg";
        newProfile = new File(savePath);
        boolean result = false;
        if(!newProfile.exists()) {
            result = newProfile.mkdirs();
        }
        result = false;
        FileOutputStream fos = null;
        try {
            newProfile = new File(savePath, fileName);
            if(!newProfile.exists()){
                result = newProfile.createNewFile();
            }
            fos = new FileOutputStream(newProfile);
            profile.compress(Bitmap.CompressFormat.JPEG, 100, fos);// 把数据写入文件
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(fos != null){
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getRandomFileName(){
        return new Date().getTime()+".jpg";
    }

    /**
     * 校验手机号是否可用
     * @author 吴文超
     * Created on 2017/8/14 9:37
     */

    private void checkPhone(String phone){
        RequestParams requestParams = new RequestParams(Constant.CHECK_PHONE_URL);
        requestParams.addBodyParameter("phone",phone);
            x.http().post(requestParams, new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    if(result.equals(Constant.VALID_PHONE)){
                        register();
                    }
                    else if(result.equals(Constant.NOT_AVAILIABLE_PHONE)){
                        Toast.makeText(getApplicationContext(),Constant.MSG_PHONE_EXIST,Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onError(Throwable ex, boolean isOnCallback) {

                    Toast.makeText(getApplicationContext(),"连接失败",Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCancelled(CancelledException cex) {
                }
                @Override
                public void onFinished() {
                }
            });
    }

    /**
     * 注册
     * @author 吴文超
     * Created on 2017/8/14 10:01
     */

    private void register(){
        RequestParams requestParams = new RequestParams(Constant.REGISTER_URL);
        LoginActivity.user  = new User();
        LoginActivity.user.setUserId(etPhone.getText().toString());
        LoginActivity.user.setPhone(LoginActivity.user.getUserId());
        LoginActivity.user.setPassword(etPassword.getText().toString());
        Gson gson = new Gson();
        String jsonObj = gson.toJson(LoginActivity.user,User.class);
        requestParams.addBodyParameter("user",jsonObj);
        x.http().post(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if(result.equals(Constant.REGISTER_SUCCESS)){
                    Toast.makeText(getApplicationContext(),Constant.MSG_REGISTER_SUCCESS,Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CODE);
                    uploadProfile();
                    finish();
                }
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(getApplicationContext(),Constant.MSG_REGISTER_FAILURE,Toast.LENGTH_SHORT);
            }
            @Override
            public void onCancelled(CancelledException cex) {
            }
            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 上传头像
     * @author 吴文超
     * Created on 2017/8/15 21:05
     */

    private void uploadProfile(){
        String userId =  LoginActivity.user.getUserId();
        if( userId == null || "".equals(userId)){
            return;
        }
        RequestParams requestParams = new RequestParams(Constant.UPLOAD_PROFILE_URL);
        requestParams.addBodyParameter("userId",userId);
        requestParams.addBodyParameter("file", newProfile);
        requestParams.setMultipart(true);
        x.http().post(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if(result.equals(Constant.UPLOAD_SUCCESS)){
                    Log.i(Constant.TAG,"上传头像成功！");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e(Constant.TAG,"上传头像失败！");
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
