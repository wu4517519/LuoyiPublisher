package com.luoyi.luoyipublisher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Message;
import android.util.Log;

import com.luoyi.luoyipublisher.bean.Device;
import com.luoyi.luoyipublisher.common.DeviceCallback;
import com.luoyi.luoyipublisher.common.DeviceManager;
import com.luoyi.luoyipublisher.common.MonitorCallback;
import com.luoyi.luoyipublisher.util.Constant;
import com.luoyi.luoyipublisher.util.ImageUtil;
import com.luoyi.luoyipublisher.util.StorageUtil;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.blur;

/**
 * Created by wwc on 2017/9/16.
 */

public class FrameProcess implements Runnable {

    public Thread addFrameThread;
    public final int TIME_INTERVAL = 250;//抓取帧的时间间隔0.3s
    public LinkedList<byte[]> dataBuffer = new LinkedList<byte[]>();//帧数据缓存队列
    public LinkedList<String> timeQueue = new LinkedList<String>();//帧数据缓存队列
    public long lastRecFrameTime = 0;
    public long lastTakeFrameTime = 0;
    public long lastFinishTime = 0;
    public final int MAX_PROCESS_TIME = 2000;//最大处理时间

    private Rect monitorAreaRect;//监控区域
    private byte[] mData;
    private int mVideoWidth;
    private int mVideoHeight;
    public int[] firstFrame;
    public int[] secondFrame ;
    public int[] thirdFrame ;
    public int[] buff ;
    public static  List<Bitmap> mBitmaps;
    private final int sizeThreshold = 150;//运动目标面积阈值
    MotionDetectionJNI mdJNI;

    private int count = 0;
    private double totalTime = 0f;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

    public MonitorCallback monitorCallback;

    public void setMonitorCallback(MonitorCallback monitorCallback) {
        this.monitorCallback = monitorCallback;
    }


    public FrameProcess() {}

    public FrameProcess(int videoWidth, int videoHeight) {
        this.mVideoWidth = videoWidth;
        this.mVideoHeight = videoHeight;
    }

    class AddBitmapBuffThread implements Runnable{
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                addFrameToBitmapBuff();
            }
        }
    }

    private void addFrameToBitmapBuff() {
        int i = 0;
        synchronized (dataBuffer){
            while(i < dataBuffer.size()){
                Bitmap bitmap = frameToBitmap(dataBuffer.poll(), mVideoWidth, mVideoHeight);
                if(bitmap != null){
                    mBitmaps.add(bitmap);
                }
                i++;
            }
        }
    }

    @Override
    public void run() {

        if(mdJNI == null){
            mdJNI = new MotionDetectionJNI();
        }
        if (mBitmaps == null) {
            mBitmaps = new ArrayList<>();
        }
        addFrameThread = new Thread(new AddBitmapBuffThread());
        addFrameThread.start();

        while (!Thread.interrupted()) {
                if (mBitmaps != null && mBitmaps.size() >= 3) {
                    lastTakeFrameTime = System.currentTimeMillis();
                    diffCalcu(mBitmaps);
                    clearBuff(mBitmaps.size());
                    lastFinishTime = System.currentTimeMillis();
                    if (lastFinishTime - lastTakeFrameTime > MAX_PROCESS_TIME) {
                        adjustHandleFrame(lastFinishTime - lastTakeFrameTime);
                    }
                }
        }
    }



    /**
     * 差分运算
     *
     * @author wwc
     * Created on 2017/9/14 20:34
     */
    public void diffCalcu(List<Bitmap> bitmaps) {
        double st = System.currentTimeMillis();
        int width = bitmaps.get(1).getWidth();
        int height = bitmaps.get(1).getHeight();
        bitmaps.get(0).getPixels(firstFrame, 0, width, 0, 0, width, height);
        bitmaps.get(1).getPixels(secondFrame, 0, width, 0, 0, width, height);
        bitmaps.get(2).getPixels(thirdFrame, 0, width, 0, 0, width, height);
        double et0 = (System.currentTimeMillis() - st) / 1000.0f;
            try {
               //求第1和第2帧的二值化图像
                /*mdJNI.diffCalcu(firstFrame, secondFrame, buff, width, height);
                diffBitmap0 = Bitmap.createBitmap(buff, width, height, Bitmap.Config.ARGB_8888);
                //movingBitmap = Bitmap.createBitmap(buff, width, height, Bitmap.Config.ARGB_8888);
                blurBitmap(diffBitmap0);
                double et1 = (System.currentTimeMillis() - st) / 1000.0f;
                //求第2和第3帧的二值化图像
                mdJNI.diffCalcu(secondFrame, thirdFrame, buff, width, height);
                diffBitmap1 = Bitmap.createBitmap(buff, width, height, Bitmap.Config.ARGB_8888);
                blurBitmap(diffBitmap1);
                double et2 = (System.currentTimeMillis() - st) / 1000.0f;
                diffBitmap0.getPixels(firstFrame, 0, width, 0, 0, width, height);
                diffBitmap1.getPixels(secondFrame, 0, width, 0, 0, width, height);
                mdJNI.seekIntersection(firstFrame, secondFrame, buff, width, height);
                double et3 = (System.currentTimeMillis() - st) / 1000.0f;*/
                //将diffBitmap0，diffBitmap1进行交集计算得到的图像进行保存
                //binaryBitmap = Bitmap.createBitmap(buff, width, height, Bitmap.Config.ARGB_8888);
                mdJNI.threeFrameDiff(firstFrame, secondFrame, thirdFrame, buff, width, height);
                double et4 = (System.currentTimeMillis() - st) / 1000.0f;
                Bitmap diffBitmap = Bitmap.createBitmap(buff, width, height, Bitmap.Config.ARGB_8888);
                double et44 = (System.currentTimeMillis() - st) / 1000.0f;
                boolean isInvasion = isInvasion(diffBitmap);
                double et5 = (System.currentTimeMillis() - st) / 1000.0f;
                double et8 = 0;
                if (isInvasion) {
                    Date date = new Date();
                    double et6 = (System.currentTimeMillis() - st) / 1000.0f;
                    Log.d(Constant.TAG, "入侵时间:" + timeQueue.peek() + ",检测完成时间" + df.format(date) + "s");
                    diffBitmap = drawMonitorArea(diffBitmap,width, height);
                    double et7 = (System.currentTimeMillis() - st) / 1000.0f;
                    ImageUtil.saveBitmap(diffBitmap, "draw-minRect");
                    String saveFilePath = ImageUtil.saveBitmap(bitmaps.get(2), "alarmLog");
                    this.monitorCallback.onInvasion(timeQueue.peek(), df.format(date), saveFilePath);
                    et8 = (System.currentTimeMillis() - st) / 1000.0f;
                    double etet8 = (System.currentTimeMillis() - st) / 1000.0f;
                }
                if(et8 == 0)
                    totalTime += et5;
                else
                    totalTime += et8;
                count++;

            } catch (Exception e) {
                //Log.d("wwc",e.getCause().toString());
                e.printStackTrace();
            }
    }

    private void blurBitmap(Bitmap bitmap){
        Mat srcMat = new Mat();
        //bitmap转换为矩阵
        Utils.bitmapToMat(bitmap, srcMat);
        Mat dstMat = new Mat();
        blur(srcMat ,dstMat , new Size(8,8));
    }

    /**
     * desc 判断是否入侵
     * @param rawBitmap 标记运动目标的图像
     * @return
     * @author wwc
     * Created on 2017/9/24 21:13
     */
    private boolean isInvasion(Bitmap rawBitmap) {
        boolean isInvasion = false;
        Mat srcMat = new Mat();
        //bitmap转换为矩阵
        Utils.bitmapToMat(rawBitmap, srcMat);
        Mat dstMat = new Mat();
        blur(srcMat ,dstMat , new Size(8,8));
        Utils.matToBitmap(dstMat, rawBitmap);
        Imgproc.cvtColor(dstMat, dstMat, Imgproc.COLOR_BGRA2GRAY);
        MatOfInt4 matOfInt4 = new MatOfInt4();
        List<MatOfPoint> matOfPoints = new ArrayList<>();
        //查找轮廓
        Imgproc.findContours(dstMat, matOfPoints, matOfInt4, RETR_EXTERNAL, CHAIN_APPROX_NONE, new Point());
        Mat imageContours = Mat.zeros(dstMat.size(), CV_8UC1);
        double total = 0;
        double max = 0;
        double min = 999999;
        for (int i = 0; i < matOfPoints.size(); i++) {
            //绘制轮廓的最小外接矩形
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(matOfPoints.get(i).toArray());
            RotatedRect rect = Imgproc.minAreaRect(matOfPoint2f);
            double area = rect.size.area();
            if (area < sizeThreshold)
                continue;
            if (area > max)
                max = area;
            else if (min > area && area != 0)
                min = area;
            total += area;
            //绘制轮廓
            Imgproc.drawContours(imageContours, matOfPoints, i, new Scalar(255), 1, 8, matOfInt4, Imgproc.INTER_MAX, new Point());
            Point[] P = new Point[4];
            rect.points(P);

            if (is2RectIntersect(monitorAreaRect, P)) {
                isInvasion = true;
                for (int j = 0; j <= 3; j++) {
                    Imgproc.line(imageContours, P[j], P[(j + 1) % 4], new Scalar(255), 2);
                }
                //break;
            }
        }
        double aver = total / matOfPoints.size();
        Utils.matToBitmap(imageContours, rawBitmap);
        return isInvasion;
    }

    /**
     * desc 绘画监控区域
     * @param 
     * @return  
     * @author wwc
      * Created on 2017/10/11 15:25
      */
    private Bitmap drawMonitorArea(Bitmap bitmap,int width, int height) {
        bitmap.getPixels(buff, 0, width, 0, 0, width, height);
        mdJNI.drawMonitorArea(buff, width, height, monitorAreaRect.left, monitorAreaRect.top, monitorAreaRect.right, monitorAreaRect.bottom);
        Bitmap newBitmap = Bitmap.createBitmap(buff, width, height, Bitmap.Config.ARGB_8888);
        return newBitmap;
    }

    /**
     * desc 对图像进行开操作
     *
     * @param rawBitmap 源图像
     * @return
     * @author wwc
     * Created on 2017/9/25 16:33
     */
    private void opening(Bitmap rawBitmap) {
        Mat src = new Mat();
        //bitmap转换为矩阵
        Utils.bitmapToMat(rawBitmap, src);
        Mat dst = new Mat();
        Imgproc.morphologyEx(src, dst, MORPH_OPEN, new Mat(3, 3, CV_8UC1), new Point(-1, -1), 1);
        Utils.matToBitmap(src, rawBitmap);
    }

    /**
     * 判断矩形是否相交
     *
     * @param moitorArea 监控区域
     * @param movingRect 运动目标坐标数组
     * @return
     */
    private boolean is2RectIntersect(Rect moitorArea, Point[] movingRect) {
        Rect moRect = new Rect((int)movingRect[1].x, (int)movingRect[1].y, (int)movingRect[2].x, (int)movingRect[0].y);
        return moitorArea.contains(moRect);
    }

    /**
     * 将帧转换为Bitmap
     *
     * @author wwc
     * Created on 2017/9/13 21:08
     */
    public Bitmap frameToBitmap(byte[] data, int width, int height) {
        try{
            YuvImage image = new YuvImage(data, ImageFormat.NV21, width, height, null);
            if(image!=null){
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, width, height), 80, stream);
                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                bmp = rotateBitmap(bmp);
                stream.close();
                return bmp;
            }
        }catch(Exception ex){
            Log.e(Constant.TAG,"Error:"+ex.getMessage());
        }
        return null;
    }

    /**
     * 图片去色,返回灰度图片
     *
     * @author wwc
     * Created on 2017/9/14 9:24
     */
    public Bitmap toGrayScale(Bitmap bmpOriginal, int width, int height) {
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        bmpGrayscale = rotateBitmap(bmpGrayscale);
        return bmpGrayscale;
    }

    /**
     * desc 旋转图片
     * @param
     * @return
     * @author wwc
      * Created on 2017/10/2 23:34
      */
    public Bitmap rotateBitmap(Bitmap bmp){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap bitmap  = Bitmap.createBitmap(bmp, 0,0, bmp.getWidth(),  bmp.getHeight(), matrix, true);
        return bitmap;
    };


    /**
     * desc 调整处理帧
     *
     * @param
     * @return
     * @author wwc
     * Created on 2017/9/28 9:51
     */
    private void adjustHandleFrame(long d) {
        int total = (int) d / TIME_INTERVAL;
        int i = 0;
        while (i < total) {
            if (timeQueue.size() <= 0)
                break;
            timeQueue.poll();
            if(mBitmaps.size() > 0)
                mBitmaps.remove(0);
            i++;
        }
    }

    private synchronized void clearBuff(int size) {
        int i = 0;
        while (i < size) {
            timeQueue.poll();
            mBitmaps.remove(0);
            i++;
        }
    }


    public int getmVideoWidth() {
        return mVideoWidth;
    }

    public void setmVideoWidth(int mVideoWidth) {
        this.mVideoWidth = mVideoWidth;
    }

    public int getmVideoHeight() {
        return mVideoHeight;
    }

    public void setmVideoHeight(int mVideoHeight) {
        this.mVideoHeight = mVideoHeight;
    }

    public Rect getMonitorAreaRect() {
        return monitorAreaRect;
    }

    public void setMonitorAreaRect(Rect monitorAreaRect) {
        this.monitorAreaRect = monitorAreaRect;
    }



}