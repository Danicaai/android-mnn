package cn.sample.mnn.detect;

/**
 * Created by GDD on 2019/4/18 .
 */

public class Facetest{
    //人脸检测模型导入
    public native boolean FaceDetectionModelInit(String faceDetectionModelPath,int minFaceSize);

    //人脸检测
    public native String FaceDetect(byte[] imageDate, int imageWidth , int imageHeight, int imageChannel);
    //人脸检测模型反初始化
    public native boolean FaceDetectionModelUnInit();

    static {
        System.loadLibrary("MNN");
        System.loadLibrary("facedetect");
    }
}
