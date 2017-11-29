package com.luoyi.luoyipublisher.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;

import com.luoyi.luoyipublisher.MotionDetectionJNI;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;

/**
 * Created by wwc on 2017/9/13.
 */

public class FrameHandleUtil {
    private static FrameHandleUtil frameHandleUtil;
    private final int mThreshold = 25;
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;
    MotionDetectionJNI mdJNI;

    public FrameHandleUtil(){

    }

    public FrameHandleUtil(RenderScript rs, ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic){
        getInstance();
        frameHandleUtil.rs = rs;
        frameHandleUtil.yuvToRgbIntrinsic = yuvToRgbIntrinsic;
    }

    public static FrameHandleUtil getInstance(){
        if(frameHandleUtil == null){
            frameHandleUtil = new FrameHandleUtil();
        }
        return frameHandleUtil;
    }

    /**
     * 将帧转换为Bitmap
     * @author wwc
      * Created on 2017/9/13 21:08
      */
    public Bitmap frameToBitmap(byte[] data, int prevSizeW, int prevSizeH){
        try {
            if (yuvType == null)
            {
                yuvType = new Type.Builder(rs, Element.U8(rs)).setX(data.length);
                in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

                rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(prevSizeW).setY(prevSizeH);
                out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
            }
            in.copyFrom(data);

            yuvToRgbIntrinsic.setInput(in);
            yuvToRgbIntrinsic.forEach(out);

            Bitmap bmpout = Bitmap.createBitmap(prevSizeW, prevSizeH, Bitmap.Config.ARGB_8888);
            out.copyTo(bmpout);
            return bmpout;
        } catch (Exception e) {
            Log.e("wwc",e.getCause().toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 图片去色,返回灰度图片
     * @author wwc
      * Created on 2017/9/14 9:24
      */
    public Bitmap toGrayScale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public void saveBitmap(Bitmap bitmap, String name) {
        FileOutputStream out = null;
        Log.e("wwc", "保存图片");
        File f = new File(StorageUtil.getInstance().getExternalSDCardDir()+
                File.separator+"AboutLuoyiTest", name+"-"+
                String.valueOf((int)( System.currentTimeMillis() % 1000000))+".jpg");
        if (!f.getParentFile().exists()) {
            f.mkdirs();
        }
        try {
            if(!f.exists()){
                boolean rt = f.createNewFile();
                int a = 0;
            }
            out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
            if(out != null){
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 差分运算
     * @author wwc
      * Created on 2017/9/14 20:34
      */
    public void diffCalcu(List<Bitmap> bitmaps){
        mdJNI = new MotionDetectionJNI();
        Bitmap diffBitmap0 = null;
        Bitmap diffBitmap1 = null;
        Bitmap binaryBitmap = null;
        Bitmap movingBitmap = null;
        Bitmap intersectionBitmap = null;
        int min = 999999999;
        int max = 0;
        int count = 0;
        if(bitmaps.size() == 3){
            try {
                int width = bitmaps.get(1).getWidth();
                int height = bitmaps.get(1).getHeight();
                double st = System.currentTimeMillis();

                int[] buff = new int[width * height];

                int[] firstFrame = new int[width * height];
                bitmaps.get(0).getPixels(firstFrame, 0, width, 0, 0, width, height);
                int[] secondFrame = new int[width * height];
                bitmaps.get(1).getPixels(secondFrame, 0, width, 0, 0, width, height);
                int[] secondFrame_copy = new int[width * height];
                bitmaps.get(1).getPixels(secondFrame_copy, 0, width, 0, 0, width, height);
                int[] thirdFrame = new int[width * height];
                bitmaps.get(2).getPixels(thirdFrame, 0, width, 0, 0, width, height);


                //求第0和第1帧的二值化图像
                mdJNI.diffCalcu(firstFrame, secondFrame, buff, width, height);
                diffBitmap0 = Bitmap.createBitmap(buff, width, height, Bitmap.Config.ARGB_8888);
                //movingBitmap = Bitmap.createBitmap(buff, width, height, Bitmap.Config.ARGB_8888);
                double et1 = (System.currentTimeMillis() - st) / 1000.0f;
                //求第1和第2帧的二值化图像
                mdJNI.diffCalcu(secondFrame, thirdFrame, buff, width, height);
                diffBitmap1 = Bitmap.createBitmap(buff, width, height, Bitmap.Config.ARGB_8888);
                double et2 = (System.currentTimeMillis() - st) / 1000.0f;
                diffBitmap0.getPixels(firstFrame, 0, width, 0, 0, width, height);
                diffBitmap1.getPixels(secondFrame, 0, width, 0, 0, width, height);
                mdJNI.seekIntersection(firstFrame, secondFrame, buff, width, height);
                double et3 = (System.currentTimeMillis() - st) / 1000.0f;
                //将diffBitmap0，diffBitmap1进行交集计算得到的图像进行保存
                binaryBitmap = Bitmap.createBitmap(buff, width, height, Bitmap.Config.ARGB_8888);
                //int aver = (max + min) / 2;
                //saveBitmap(diffBitmap, "diff");
                //binaryBitmap = opening(binaryBitmap);
                //saveBitmap(binaryBitmap, "oping");
                Bitmap minRectBitmap = Bitmap.createBitmap(buff, width, height, Bitmap.Config.ARGB_8888);
                boolean isInvasion = isInvasion(binaryBitmap, minRectBitmap);
                double et4 = 0;
                double et5 = 0;
                if(isInvasion){
                    saveBitmap(bitmaps.get(0),"gray");
                    saveBitmap(diffBitmap0, "diff0");
                    saveBitmap(diffBitmap1, "diff1");
                    saveBitmap(binaryBitmap, "binary");
                    et4 = (System.currentTimeMillis() - st) / 1000.0f;
                    saveBitmap(minRectBitmap, "minRect");
                    et5 = (System.currentTimeMillis() - st) / 1000.0f;
                }
                double et6 = (System.currentTimeMillis() - st) / 1000.0f;
                if(isInvasion){
                    System.out.println("sfgsdfsdf");
                }

            } catch (Exception e) {
                Log.e("wwc",e.getCause().toString());
                e.printStackTrace();
            }
        }
    }

    /*for(int i = 0; i < height; i++){
                    for(int j = 0; j < width; j++){
                      int beforePixel = bitmaps.get(0).getPixel(j, i);
                        int afterPixel = diffBitmap.getPixel(j, i);
                        int beforeGray = (77 * Color.red(beforePixel) + 59 * Color.green(beforePixel) + 28 * Color.blue(beforePixel)) >> 8;
                        int afterGray =  (77 * Color.red(afterPixel) + 59 * Color.green(afterPixel) + 28 * Color.blue(afterPixel)) >> 8;
                        int grayScaleDiff = Math.abs(afterGray - beforeGray);
                        movingBitmap.setPixel(j, i, binary(grayScaleDiff));
                        *//*if(grayScaleDiff > max){
                            max = grayScaleDiff;
                        }
                        if(grayScaleDiff < min){
                            min = grayScaleDiff;
                        }*//*
                       *//* diffBitmap.setPixel(j, i, diff);
                        movingBitmap.setPixel(j, i, diff);
                        pixel = movingBitmap.getPixel(j, i);
                        movingBitmap.setPixel(j, i, isMovingPixel(diff));
                        last = movingBitmap.getPixel(j, i);*//*
                    }
                }*/

    /**
     * desc 判断是否入侵
     * @param rawBitmap 需要鉴别的图像
     * @param newBitmap 标记运动目标的图像
     * @return
     * @author wwc
      * Created on 2017/9/24 21:13
      */
    private boolean isInvasion(Bitmap rawBitmap, Bitmap newBitmap){
        Point[] border = {new Point(0,0), new Point(640,0),  new Point(0,480), new Point(640,480)};
        Point borderCenter = new Point(Math.abs(border[0].x - border[1]. x) / 2, Math.abs(border[0].y - border[3]. y) / 2);
        boolean isInvasion = false;
        int sizeThreshold = 70;
        Mat mat = new Mat();
        //bitmap转换为矩阵
        Utils.bitmapToMat(rawBitmap,mat);
        Mat mat2 = new Mat();

        Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_BGRA2GRAY);
        mat = mat2;
        MatOfInt4 matOfInt4 = new MatOfInt4();
        List<MatOfPoint> matOfPoints = new ArrayList<>();
        //查找轮廓
        Imgproc.findContours(mat, matOfPoints, matOfInt4, RETR_EXTERNAL, CHAIN_APPROX_NONE, new Point());
        Mat imageContours = Mat.zeros(mat.size(), CV_8UC1);
        double total = 0;
        double max = 0;
        double min = 999999;
        int count = 0;
        for(int i = 0; i < matOfPoints.size(); i++){
            //绘制轮廓
            Imgproc.drawContours(imageContours, matOfPoints, i, new Scalar(255),1 , 8 , matOfInt4, Imgproc.INTER_MAX, new Point());
            //绘制轮廓的最小外接矩形
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(matOfPoints.get(i).toArray());
            RotatedRect rect = Imgproc.minAreaRect(matOfPoint2f);
            double area = rect.size.area();
            if(area>max)
                max = area;
            if(min > area && area != 0)
                min = area;
            total += area;
            count++;
            if(rect.size.area() < sizeThreshold)
                continue;

            Point []P = new Point[4];
            rect.points(P);
            if(is2RectIntersect(borderCenter, rect.center, (border[1].x - border[0].x)/2, (border[3].y - border[1].y)/2)){
                isInvasion =  true;
                for(int j = 0; j <= 3; j++){
                    Imgproc.line(imageContours, P[j], P[(j+1)%4], new Scalar(255), 2);
                }
                break;
            }
        }
        //double aver = total / count;
        Utils.matToBitmap(imageContours, newBitmap);
        return isInvasion;
    }

    /**
     * desc 对图像进行开操作
     * @param rawBitmap 源图像
     * @return
     * @author wwc
      * Created on 2017/9/25 16:33
      */
    private Bitmap opening(Bitmap rawBitmap){
        Mat src = new Mat();
        //bitmap转换为矩阵
        Utils.bitmapToMat(rawBitmap, src);
        Mat dst = new Mat();
        Imgproc.morphologyEx(src, dst, MORPH_OPEN, new Mat(3,3,CV_8UC1), new Point(-1, -1), 1);
        Bitmap newBitmap = Bitmap.createBitmap(rawBitmap.getWidth(), rawBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, newBitmap);
        return newBitmap;
    }

    /**
     * 判断矩形是否相交
     * @param ca 警戒线中心点
     * @param cb 运动物体最小外接矩形中心点
     * @param caHalfWidth 警戒区域宽度一半
     * @param caHalfHeight 警戒区域高度一半
     * @return
     */
    private boolean is2RectIntersect(Point ca, Point cb, double caHalfWidth, double caHalfHeight){
        boolean result = Math.abs(cb.x - ca.x) <= caHalfWidth;
        boolean result2 = Math.abs(cb.y - ca.y) <= caHalfHeight;
        if(result && result2){
            System.out.print(result);
        }
        return result && result2;
    }

    /**
     * 根据灰度值对图像进行二值化
     * @author wwc
     * Created on 2017/9/15 18:56
     */
    private int binary(int grayscale){
        if(grayscale > mThreshold){
            return Color.WHITE;
        }
        return Color.BLACK;
    }

    /**
     * 获得差分图像
     * @author wwc
     * Created on 2017/9/15 18:51
     */
    public Bitmap getDiffPic(Bitmap bitmap){
        int threshold = 10;//阈值
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                if(bitmap.getPixel(j, i) > threshold){
                    bitmap.setPixel(j, i, 1);
                }
                else{
                    bitmap.setPixel(j, i, 0);
                }
            }
        }
        return null;
    }
}
