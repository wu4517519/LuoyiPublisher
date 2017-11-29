package com.luoyi.luoyipublisher.util;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wwc on 2017/9/12.
 */

public class ImageUtil {


    public int[] getGrayScaleImage(byte[] raw, int width, int height){
        return null;
    }

    public static String saveBitmap(Bitmap bitmap, String name) {

        FileOutputStream out = null;
        String randomStr = StringUtil.getRandomString();
        File f = new File(StorageUtil.getInstance().getSDCardDir() +
                File.separator + "AboutLuoyiTest", name + "-" +randomStr + ".jpg");
        if (!f.getParentFile().exists()) {
            f.mkdirs();
        }
        try {
            if (!f.exists()) {
                boolean rt = f.createNewFile();
            }
            out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            if (out != null) {
                out.close();
            }
            String savePath = f.getAbsolutePath();
            Log.d(Constant.TAG, "保存图片"+f.getName()+"，在"+savePath);
            return savePath;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteBitmap(String filePath){
        if(filePath == null || filePath.equals(""))
            return false;
        boolean result = false;
        File file = new File(filePath);
        if(file.exists()){
            result = file.delete();
        }
        return result;
    }
}
