package com.luoyi.luoyipublisher.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.luoyi.luoyipublisher.R;
import com.luoyi.luoyipublisher.activity.LoginActivity;
import com.luoyi.luoyipublisher.activity.MainActivity;
import com.luoyi.luoyipublisher.activity.SettingActivity;
import com.luoyi.luoyipublisher.fragment.base.BaseFragment;
import com.luoyi.luoyipublisher.bean.User;
import com.luoyi.luoyipublisher.util.Constant;
import com.luoyi.luoyipublisher.util.StorageUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wwc on 2017/8/9.
 */

public class MineFragment extends BaseFragment {

    private static MineFragment mineFragment;

    private TextView nickName;
    private ImageView ivProfile;
    private ImageView ivSetting;
    private TextView mCacheSize;
    private ListView listView;
    private PopupWindow popupWindow;
    private List<String> list;
    private View popupParentView;
    private Bitmap profile;//头像
    private File newProfile;
    private User user;
    private long lastClearTime = 0;


    public MineFragment() {
        // Required empty public constructor
    }

    public static MineFragment getInstance() {
        if(mineFragment == null){
            mineFragment = new MineFragment();
        }
        return mineFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.e("wwc","MineFragment onCreate call");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("wwc","MineFragment onResume call");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(Constant.TAG, "MineFragment onCreateView call");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        nickName = (TextView) view.findViewById(R.id.fm_nickname);
        ivProfile = (ImageView) view.findViewById(R.id.circleImage);
        mCacheSize = (TextView) view.findViewById(R.id.cache_size);
        ivSetting = (ImageView) view.findViewById(R.id.setting);
        initPopupWindow();
        initEven();
        return view;
    }


    /**
     * 初始化头像
     * @author 吴文超
     * Created on 2017/8/11 18:05
     */

    public void loadProfile(String picUrl){
        Log.e(Constant.TAG, "MineFragment loadProfile called");
        ImageOptions imageOptions =  new ImageOptions.Builder()
                .setFadeIn(true)
                .setLoadingDrawableId(R.drawable.loading)
                .setFailureDrawableId(R.drawable.load_failure)
                //设置使用缓存
                .setUseMemCache(true)
                //设置显示圆形图片
                .setCircular(true)
                .setIgnoreGif(false)
                .build();
        x.image().bind(ivProfile, picUrl, imageOptions);
    }

    public User getUser(){
        return user;
    }

    public void setUser(User user){
        this.user = user;
    }
    public void setNickName(String nickName){
        this.nickName.setText(nickName);
    }

    private void initEven(){
        Log.e(Constant.TAG, "MineFragment initEven called");
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!popupWindow.isShowing()){
                    popupWindow.showAtLocation(popupParentView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,0,0);
                }
            }
        });

        mCacheSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentTime = new Date().getTime();
                File cacheFile = new File(Constant.CACHE_PATH);
                if(currentTime - lastClearTime >= 2000 && StorageUtil.getCacheSize(cacheFile) > 0){
                    boolean result = StorageUtil.clearCache(cacheFile);
                    if(result){
                        lastClearTime = new Date().getTime();
                        Toast.makeText(getActivity().getApplicationContext(),
                                Constant.MSG_CLEAR_SUCCESS+mCacheSize.getText().toString()+"缓存文件",Toast.LENGTH_SHORT).show();
                        mCacheSize.setText("0Kb");
                    }
                }
            }
        });

        ivSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 初始化弹窗
     * @author 吴文超
     * Created on 2017/8/13 10:21
     */

    private void initPopupWindow(){
        Log.e(Constant.TAG, "MineFragment initPopupWindow called");
        popupWindow = new PopupWindow(getActivity());
        popupWindow.setWidth(LinearLayoutCompat.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        //加载弹窗布局
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View popupView = layoutInflater.inflate(R.layout.popup_window,null);
        listView = (ListView) popupView.findViewById(R.id.lv_profile);
        getData();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.popup_window_item, R.id.item, list);
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
        private AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        private final AlertDialog dialog = builder.create();
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String text = list.get(position);
            final String gallery = getActivity().getResources().getString(R.string.gallery);
            final String take_picture = getActivity().getResources().getString(R.string.take_picture);
            final String cancel = getActivity().getResources().getString(R.string.cancel);
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

    private String getRandomFileName(){
        return new Date().getTime()+".jpg";
    }

    /**
     * 取消
     * @author 吴文超
     * Created on 2017/8/13 16:19
     */

    private void cancel(){

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 0:{//完成从图库选取
                if(resultCode  == getActivity().RESULT_OK){
                    cropPhoto(data.getData());
                }
            }
            break;
            case 1:{//完成拍照
                if (resultCode == getActivity().RESULT_OK) {
                    cropPhoto(Uri.fromFile(newProfile));// 裁剪图片
                }
            }
            break;
            case 2:{//裁剪完成
                if(data  != null){
                    Bundle extras = data.getExtras();
                    profile = extras.getParcelable("data");
                    if (profile != null) {
                        savePicToLocal(profile);// 保存在SD卡中
                        uploadProfile();
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
        if(user == null)
            user = MainActivity.getUser();
        requestParams.addBodyParameter("userId",userId);
        requestParams.addBodyParameter("file", newProfile);
        requestParams.setMultipart(true);
        x.http().post(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if(result.equals(Constant.UPLOAD_SUCCESS)){
                    Toast.makeText(getActivity(),Constant.MSG_UPDATE_PROFILE_SUCCESS, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onStart() {
        super.onStart();
        setCacheSize();
    }


    private void setCacheSize(){
        long cacheSize = StorageUtil.getCacheSize(new File(Constant.CACHE_PATH));
        String strSize = StorageUtil.getFormatSize(cacheSize);
        mCacheSize.setText(strSize);
    }
}
