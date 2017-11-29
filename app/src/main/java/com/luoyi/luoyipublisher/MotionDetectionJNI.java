package com.luoyi.luoyipublisher;

import android.graphics.Bitmap;

/**
 * Created by wwc on 2017/9/17.
 */

public class MotionDetectionJNI {


    /**
     * desc 计算差分图像
     * @param preFrame 前一帧图像数据
     * @param aftFrame 前一帧图像数据
     * @param buff 返回的二值化图像
     * @param width 图像的宽度
     * @param height 图像的高度
     * @return  二值化后的灰度图像
     * @author wwc
     * Created on 2017/9/17 12:31
     */
    public native void diffCalcu(int[] preFrame, int[] aftFrame,  int[] buff, int width, int height);

    /**
     * desc 求两帧图像的交集
     * @param frame0 第一帧
     * @param frame1 第二帧
     * @param buff 返回的交集
     * @param width 图像的宽度
     * @param height 图像的高度
     * @return
     * @author wwc
      * Created on 2017/9/20 19:55
      */
    public native void seekIntersection(int[] frame0, int[] frame1, int[] buff, int width, int height);

    /**
     * desc  绘制最小外接矩形
     * @param srcBitmap
     * @param buff 返回绘制后的颜色值
     * @return
     * @author wwc
      * Created on 2017/9/24 10:10
      */
    public native void drawMinRect(Bitmap srcBitmap, int[] buff);

    public native void threeFrameDiff(int[] firstFrame, int[] secondFrame,  int[] thirdFrame, int[] buff, int width, int height);

    public native  void drawMonitorArea(int[] pixels, int width, int height, int left, int top, int right, int bottom);

}
