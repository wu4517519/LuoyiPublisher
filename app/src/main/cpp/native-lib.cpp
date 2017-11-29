/* DO NOT EDIT THIS FILE - it is machine generated */
#include <string.h>
#include "com_luoyi_luoyipublisher_MotionDetectionJNI.h"
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
using namespace cv;

#include <android/bitmap.h>

#include <iostream>
using namespace std;


//运动阈值
const int MOVE_THRESHOLD = 35;
const int WHITE = -1;
const int BLACK = -16777216;
int thresh = 100;
int max_thresh = 255;

int Abs(int subtract, int beSubtract);
int getColor(int color);
int getGrayValue(int color);
int isMovingPixel(int prePixel, int curPixel);
int * bitmap_to_mat(JNIEnv *env, jobject &srcBitmap, Mat &srcMat);



JNIEXPORT void JNICALL Java_com_luoyi_luoyipublisher_MotionDetectionJNI_diffCalcu
        (JNIEnv *env, jobject job, jintArray preFrame, jintArray curFrame, jintArray buff, jint width, jint height)
{
    int length = 0;
    length = env->GetArrayLength(preFrame);
    jint *preArray, * curArray, *pBuff;
    preArray = env->GetIntArrayElements(preFrame, NULL);
    curArray = env->GetIntArrayElements(curFrame, NULL);
    pBuff = env->GetIntArrayElements(buff, NULL);
    int i,j;
    for(int i = 0; i < length; i++)
    {
        pBuff[i] = isMovingPixel(preArray[i], curArray[i]);
    }
    env->SetIntArrayRegion(buff,0,length,pBuff);
    env->ReleaseIntArrayElements(preFrame,preArray,JNI_ABORT);
    env->ReleaseIntArrayElements(curFrame,curArray,JNI_ABORT);
    env->ReleaseIntArrayElements(buff,pBuff,JNI_ABORT);
}

JNIEXPORT void JNICALL Java_com_luoyi_luoyipublisher_MotionDetectionJNI_seekIntersection
        (JNIEnv *env, jobject job, jintArray frame0, jintArray frame1, jintArray buff, jint width, jint height)
{
    jint *pFrame0, * pFrame1, * pBuff;
    pFrame0 =env->GetIntArrayElements(frame0, NULL);
    pFrame1 =env->GetIntArrayElements(frame1, NULL);
    pBuff =env->GetIntArrayElements(buff, NULL);
    int length =env->GetArrayLength(frame0);

    for(int i = 0; i < length; i++)
    {
        pBuff[i] = ((pFrame0[i] == WHITE) && (pFrame1[i] == WHITE)) ? WHITE : BLACK;
    }
   env->SetIntArrayRegion(buff,0,length,pBuff);
   env->ReleaseIntArrayElements(frame0,pFrame0,JNI_ABORT);
   env->ReleaseIntArrayElements(frame1,pFrame1,JNI_ABORT);
    env->ReleaseIntArrayElements(buff,pBuff,JNI_ABORT);
}

JNIEXPORT void JNICALL Java_com_luoyi_luoyipublisher_MotionDetectionJNI_threeFrameDiff
        (JNIEnv *env, jobject job, jintArray firstFame, jintArray secondFrame, jintArray thirdFrame, jintArray buff, jint width, jint height)
{
    int length = 0;
    length = env->GetArrayLength(firstFame);
    jint *pFirstFrame, * pSecondFrame,*pThirdFrame, *pBuff;
    pFirstFrame = env->GetIntArrayElements(firstFame, NULL);
    pSecondFrame = env->GetIntArrayElements(secondFrame, NULL);
    pThirdFrame = env->GetIntArrayElements(thirdFrame, NULL);
    pBuff = env->GetIntArrayElements(buff, NULL);
    int i;
    int movCount= 0;
    for(int i = 0; i < length; i++)
    {
        /*int diff = Abs(getGrayValue(pFirstFrame[i]), getGrayValue(pSecondFrame[i]));
        movCount += diff;*/
        /*diff = Abs(getGrayValue(pSecondFrame[i]), getGrayValue(pThirdFrame[i]));
        movCount += diff;*/
        int r1 = isMovingPixel(pFirstFrame[i], pSecondFrame[i]);
        int r2 = isMovingPixel(pSecondFrame[i], pThirdFrame[i]);
        pBuff[i] = ((r1 == WHITE) && (r2 == WHITE)) ? WHITE : BLACK;
    }
    //double aver = movCount / length;
    env->SetIntArrayRegion(buff,0,length,pBuff);
    env->ReleaseIntArrayElements(firstFame,pFirstFrame,JNI_ABORT);
    env->ReleaseIntArrayElements(secondFrame,pSecondFrame,JNI_ABORT);
    env->ReleaseIntArrayElements(thirdFrame,pThirdFrame,JNI_ABORT);
    env->ReleaseIntArrayElements(buff,pBuff,JNI_ABORT);
}
JNIEXPORT void JNICALL Java_com_luoyi_luoyipublisher_MotionDetectionJNI_drawMonitorArea
        (JNIEnv *env, jobject job, jintArray pixels, jint width, jint height, jint left, jint top, jint right, jint bottom)
{

    jint *p;
    p = env->GetIntArrayElements(pixels, NULL);
    int length =env->GetArrayLength(pixels);
    int areaWidth = right - left;
    int lt = top == 0 ? left : (top - 1) * width + left;
    int rt = top == 0 ? right : lt + areaWidth;
    int lb = (bottom - 1) * width + left;
    int rb = lb + areaWidth;

    int i = 0;
   for(i = lt; i <= rt; i++)
   {
       p[i] = WHITE;
   }
    int b = 0;
    for(b = lt + width; ;)
    {
        p[b]  = WHITE;
        p[b + areaWidth] = WHITE;
        b += width;
        if(b >= lb)
            break;
    }
    int c = 0;
    for(c = lb; c <= rb; c++)
    {
        p[c] = WHITE;
    }

    env->SetIntArrayRegion(pixels,0,length,p);
    env->ReleaseIntArrayElements(pixels, p ,JNI_ABORT);
}

int Abs(int subtract, int beSubtract)
{
    int result = beSubtract - subtract;
    return result >= 0 ? result : (0 - result);
}

/*获得新的颜色值*/
int getColor(int color)
{
    /*int alpha = color & 0xFF000000;
    int red = (color & 0x00FF0000) >> 16;
    int green = (color & 0x0000FF00) >> 8;
    int blue = (color & 0x000000FF);
    int newColor = alpha | (red << 16) | (green << 8) | blue;*/

    int alpha = ((color & 0xFF000000) >> 24);
    int red = ((color & 0x00FF0000) >> 16);
    int green = ((color & 0x0000FF00) >> 8);
    int blue = (color & 0x000000FF);
    int newColor = alpha << 24 | red << 16 | green << 8 | blue;

    return newColor;
}

/*获得灰度值*/
int getGrayValue(int color)
{
    int red = (color & 0x00FF0000) >> 16;
    int green = (color & 0x0000FF00) >> 8;
    int blue = (color & 0x000000FF);
    int grayValue = (77*red + 150*green + 29*blue + 128) >> 8;
    return grayValue;
}

/*判断像素点是否为运动点*/
int isMovingPixel(int prePixel, int curPixel)
{
    int preGray = getGrayValue(prePixel);
    int curGray = getGrayValue(curPixel);
    int diff = Abs(preGray,curGray);
    //return diff;
    //return getColor(diff);
    //return diff >= MOVE_THRESHOLD ? getColor(255) : 0;
    return diff >= MOVE_THRESHOLD ? WHITE : BLACK;
}
