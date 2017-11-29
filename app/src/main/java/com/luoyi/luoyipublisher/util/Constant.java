package com.luoyi.luoyipublisher.util;

import java.io.File;

/**
 * Created by wwc on 2017/8/8.
 */

public class Constant {
    /*public static final String LUOYI_SERVER_ADDR = "http://192.168.16.100";
    public static final String PORT = "8080";*/
    //落意服务器ip地址
    public static final String LUOYI_SERVER_ADDR = "http://120.77.218.173";
    //端口号
    public static final String PORT = "80";
    //项目名
    public static final String PROJECT_NAME = "LuoyiServer";
    //基本访问路径
    public static final String BASE_PATH = LUOYI_SERVER_ADDR+":"+PORT+"/"+PROJECT_NAME;


    public static final String  SUCCESS = "success";
    public static final String  FAILURE = "failure";

    /*用户模块 start */

    //校验手机是否可用
    public static final String CHECK_PHONE_URL = BASE_PATH+"/user/checkPhone";
    //注册
    public static final String REGISTER_URL = BASE_PATH+"/user/register";
    //登录
    public static final String LOGIN_URL = BASE_PATH+"/user/login";
    //查询用户信息
    public static final String QUERY_USERINFO_URL = BASE_PATH+"/user/getUserInfo";
    //查询用户信息
    public static final String UPLOAD_PROFILE_URL = BASE_PATH+"/user/uploadProfile";
    //更新用户信息
    public static final String UPDATE_USERINFO_URL = BASE_PATH+"/user/updateUserInfo";

    /*用户模块 end */

    /*设备模块*/
    //添加设备
    public static final String ADD_DEVICE = BASE_PATH+"/device/addDevice";
    //根据AndroidId查找设备
    public static final String FIND_DEVICE_BY_ANDROID_ID = BASE_PATH+"/device/findDeviceByAndroidId";
    //查找用户监控设备列表
    public static final String FIND_DEVICE_LIST = BASE_PATH+"/device/findDeviceList";
    //更新设备在线状态
    public static final String UPDATE_DEVICE_ONLINE_STATUS  = BASE_PATH+"/device/updateDeviceOnlineStatus";
    /*设备模块 end */

    /*监控模块*/

    //流媒体服务器IP地址
    public static final String STREAM_MEDIA_SERVER_ADDRESS = "120.77.153.127";
    //推流前缀
    public static final String PUSH_URL_PREFIX = "rtmp://"+STREAM_MEDIA_SERVER_ADDRESS+"/LuoyiLive/";
    public static final String INVASION = "目标闯入";
    public static final String UPLOAD_COVER_IMG =  BASE_PATH+"/device/uploadCoverImg";
    /*监控模块 end */


    /*警报日志模块*/
    public static final int ALARM_TYPE_INVASION = 0;
    public static final String ADD_ALARM_LOG =  BASE_PATH+"/alarmLog/addAlarmLog";
    public static final String UPLOAD_ALARM_IMG =  BASE_PATH+"/alarmLog/uploadAlarmImg";
    public static final String FIND_ALARM_LOG = BASE_PATH+"/alarmLog/findAlarmImg";


    //本地路径
    public static final String LOCAL_PATH = StorageUtil.getSDCardDir() == null ? "":StorageUtil.getSDCardDir()+ File.separator+"LuoyiVC";
    //软件缓存路径
    public static final String  CACHE_PATH = LOCAL_PATH == null ? "":LOCAL_PATH+ File.separator+"Cache";



    public static final String  TAG = "wwc";
    public static final String  LOGIN_SUCCESS = "loginSuccess";
    public static final String  USRE_NOT_EXIST = "userNotExist";
    public static final String  NOT_MATCH = "notMatch";
    public static final String  VALID_PHONE = "validPhone";
    public static final String  NOT_AVAILIABLE_PHONE = "notAvailablePhone";
    public static final String  REGISTER_SUCCESS = "registerSuccess";
    public static final String  REGISTER_FAILURE = "registerFailure";
    public static final String  UPLOAD_SUCCESS = "uploadSuccess";
    public static final String  UPLOAD_FAILAURE = "uploadFailure";
    public static final String  UPDATE_USERINFO_SUCCESS = "updateSuccess";

    public static final String  MSG_LOGIN_SUCCESS = "欢迎回来";
    public static final String  MSG_USRE_NOT_EXIST = "用户不存在";
    public static final String  MSG_NOT_MATCH = "用户名或密码错误";
    public static final String  MSG_CONN_FAILURE = "连接服务器失败";
    public static final String  MSG_UNKNOW_ERO = "未知错误，登录失败";
    public static final String  MSG_PHONE_EXIST = "手机号已注册";
    public static final String  MSG_PHONE_EMPTY = "请输入手机号";
    public static final String  MSG_PASSWORD_EMPTY = "请输入密码";
    public static final String  MSG_REGISTER_SUCCESS = "注册成功";
    public static final String  MSG_REGISTER_FAILURE = "注册失败";
    public static final String  MSG_UPDATE_PROFILE_SUCCESS = "修改头像成功";
    public static final String  MSG_CLEAR_SUCCESS = "清除缓存成功，已清除";
    public static final String  MSG_UPDATE_USERINFO_SUCCESS = "个人信息已更新";
    public static final String  MSG_ERR_PWD = "密码不正确";
    public static final String  MSG_INPUT_INCOMPLETE ="请输入完整信息";




}
