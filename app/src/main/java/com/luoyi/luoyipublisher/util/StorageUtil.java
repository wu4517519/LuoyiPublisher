package com.luoyi.luoyipublisher.util;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wwc on 2017/7/25.
 */

public class StorageUtil {
    /**
     * 获得SD卡路径
     * @author 吴文超
     * Created on 2017/7/22 17:54
     */

    private static StorageUtil storageUtil;
    private static String SDCardDir;
    private static String ExternalSDCardDir;

    private StorageUtil(){

    }
    public static StorageUtil getInstance(){
        if(storageUtil == null){
            storageUtil =  new StorageUtil();
        }
        return storageUtil;
    }

    public static String getSDCardDir(){
        if(SDCardDir == null){
            //如果存在SD卡
            if(Environment.getExternalStorageState()
                    .equals(Environment.MEDIA_MOUNTED)){
                SDCardDir = Environment.getExternalStorageDirectory().toString();//返回根目录
            }
            else{
                return null;
            }
        }
        return SDCardDir;
    }

    /**
     * desc 获得外部存储路径
     * @param
     * @return  
     * @author wwc
      * Created on 2017/9/25 15:59
      */
    public String getExternalSDCardDir(){
        if(SDCardDir == null){
            List<String> paths = new ArrayList<String>();
            String extFileStatus = Environment.getExternalStorageState();
            File extFile = Environment.getExternalStorageDirectory();
            //首先判断一下外置SD卡的状态，处于挂载状态才能获取的到
            if (extFileStatus.equals(Environment.MEDIA_MOUNTED)
                    && extFile.exists() && extFile.isDirectory()
                    && extFile.canWrite()) {
                //外置SD卡的路径
                paths.add(extFile.getAbsolutePath());
            }
            try {
                // obtain executed result of command line code of 'mount', to judge
                // whether tfCard exists by the result
                Runtime runtime = Runtime.getRuntime();
                Process process = runtime.exec("mount");
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                int mountPathIndex = 1;
                while ((line = br.readLine()) != null) {
                    // format of sdcard file system: vfat/fuse
                    if ((!line.contains("fat") && !line.contains("fuse") && !line
                            .contains("storage"))
                            || line.contains("secure")
                            || line.contains("asec")
                            || line.contains("firmware")
                            || line.contains("shell")
                            || line.contains("obb")
                            || line.contains("legacy") || line.contains("data")) {
                        continue;
                    }
                    String[] parts = line.split(" ");
                    int length = parts.length;
                    if (mountPathIndex >= length) {
                        continue;
                    }
                    String mountPath = parts[mountPathIndex];
                    if (!mountPath.contains("/") || mountPath.contains("data")
                            || mountPath.contains("Data")) {
                        continue;
                    }
                    File mountRoot = new File(mountPath);
                    if (!mountRoot.exists() || !mountRoot.isDirectory()
                            || !mountRoot.canWrite()) {
                        continue;
                    }
                    boolean equalsToPrimarySD = mountPath.equals(extFile
                            .getAbsolutePath());
                    if (equalsToPrimarySD) {
                        continue;
                    }
                    //扩展存储卡即TF卡或者SD卡路径
                    paths.add(mountPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            ExternalSDCardDir = paths.get(1);
        }
        return ExternalSDCardDir;
    }


    /**
     * 获得缓存大小
     * @author 吴文超
     * Created on 2017/8/22 18:06
     */

    public static long getCacheSize(File cacheFile){
        long cacheSize = 0;
        if(cacheFile.exists()){
            for(File file: cacheFile.listFiles()){
                if(!file.isDirectory()){
                    cacheSize += file.length();
                }
                else{
                    cacheSize += getCacheSize(file);
                }
            }
        }
        return cacheSize;
    }

    /**
     * 格式化单位
     * @author 吴文超
     * Created on 2017/8/22 18:06
     */

    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "Byte";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }

    /**
     * 清除缓存文件
     * @author 吴文超
     * Created on 2017/8/22 18:35
     */

    public static boolean  clearCache(File cacheFile){
        if(cacheFile.exists()){
            try {
                for(File file:cacheFile.listFiles()){
                    if(!file.isDirectory() && file.exists()){
                        file.delete();
                    }
                    else{
                        clearCache(file);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
