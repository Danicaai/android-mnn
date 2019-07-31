#include <android/bitmap.h>
#include <android/log.h>
#include <jni.h>
#include <string>
#include <vector>
#include <iostream>
#include <mutex>
#include <thread>
#include "face_detect.h"
#include "cJSON.h"
using namespace std;

static TIEVD::FaceDetect *m_face_detect_mnn;

static string model_path;
static int minFaceSize;
static int framecount;

//sdk是否初始化成功
bool detection_sdk_init_ok = false;
typedef unsigned char BYTE;

std::string set_search_result (std::vector<TIEVD::FaceInfo> &res_obj)
{
    int face_num = res_obj.size();
    cJSON *root = cJSON_CreateObject();
    cJSON *patch_region = cJSON_CreateArray();
    if(face_num>0)
    {
         cJSON_AddItemToObject(root, "detectstatus", cJSON_CreateString("1"));
    }
    else
         cJSON_AddItemToObject(root, "detectstatus", cJSON_CreateString("-1"));
    for(int i = 0; i < face_num; ++i)
    {
        cJSON *patch_region_item = cJSON_CreateObject();
        cJSON_AddNumberToObject(patch_region_item,"x",res_obj[i].bbox.xmin);
        cJSON_AddNumberToObject(patch_region_item,"y",res_obj[i].bbox.ymin);
        cJSON_AddNumberToObject(patch_region_item,"w",res_obj[i].bbox.xmax - res_obj[i].bbox.xmin);
        cJSON_AddNumberToObject(patch_region_item,"h",res_obj[i].bbox.ymax - res_obj[i].bbox.ymin);
        cJSON_AddItemToArray(patch_region, patch_region_item);
    }
    cJSON_AddItemToObject(root,"results",patch_region);


    char *buf = cJSON_PrintUnformatted(root);
    string result = buf;
    free(buf);
    buf = nullptr;
    cJSON_Delete(root);
    return result;
}
extern "C" {


JNIEXPORT jboolean JNICALL
Java_cn_sample_mnn_detect_Facetest_FaceDetectionModelInit(JNIEnv *env, jobject instance,
                                                      jstring faceDetectionModelPath_,jint tminFaceSize) {

    //modle init
    if (detection_sdk_init_ok) {
        //  LOGD("人脸检测模型已经导入");
        return true;
    }
    jboolean tRet = false;
    if (NULL == faceDetectionModelPath_) {
        //   LOGD("导入的人脸检测的目录为空");
        return tRet;
    }

    //获取模型的绝对路径的目录（不是/aaa/bbb.bin这样的路径，是/aaa/)
    const char *faceDetectionModelPath = env->GetStringUTFChars(faceDetectionModelPath_, 0);
    if (NULL == faceDetectionModelPath) {
        return tRet;
    }


    string tFaceModelDir = faceDetectionModelPath;
    string tLastChar = tFaceModelDir.substr(tFaceModelDir.length() - 1, 1);
    //LOGD("init, tFaceModelDir last =%s", tLastChar.c_str());
    //目录补齐/
    if ("\\" == tLastChar) {
        tFaceModelDir = tFaceModelDir.substr(0, tFaceModelDir.length() - 1) + "/";
    } else if (tLastChar != "/") {
        tFaceModelDir += "/";
    }
    model_path = tFaceModelDir;
    minFaceSize = tminFaceSize;
    // init face detect
    m_face_detect_mnn = new TIEVD::FaceDetect(model_path, 0.7f, 0.8f, 0.9f);

    env->ReleaseStringUTFChars(faceDetectionModelPath_, faceDetectionModelPath);
    detection_sdk_init_ok = true;
    tRet = true;
    return tRet;
}

JNIEXPORT jstring JNICALL
Java_cn_sample_mnn_detect_Facetest_FaceDetect(JNIEnv *env, jobject instance, jbyteArray imageDate_,
                                          jint imageWidth, jint imageHeight, jint imageChannel){

    if(!detection_sdk_init_ok){
        //LOGD("人脸检测模型SDK未初始化，直接返回空");
        return NULL;
    }

    int tImageDateLen = env->GetArrayLength(imageDate_);
    if(imageChannel == tImageDateLen / imageWidth / imageHeight){
        //LOGD("数据宽=%d,高=%d,通道=%d",imageWidth,imageHeight,imageChannel);
    }
    else{
        //LOGD("数据长宽高通道不匹配，直接返回空");
        return NULL;
    }

    jbyte *imageDate = env->GetByteArrayElements(imageDate_, NULL);
    if (NULL == imageDate){
        // LOGD("导入数据为空，直接返回空");
        return NULL;
    }

    if(imageWidth<20||imageHeight<20){
        // LOGD("导入数据的宽和高小于20，直接返回空");
        return NULL;
    }

    //TODO 通道需测试
    if(3 == imageChannel || 4 == imageChannel){
        //图像通道数只能是3或4；
    }else{
        // LOGD("图像通道数只能是3或4，直接返回空");
        return NULL;
    }

    unsigned char *faceImageCharDate = (unsigned char*)imageDate; // do not need free
    cv::Mat srcmatimg;
    if(imageChannel==3) {

        cv::Mat temp_mat(imageHeight, imageWidth, CV_8UC3, faceImageCharDate);
        cvtColor(temp_mat, srcmatimg, CV_RGB2BGR);
    }else{

        cv::Mat temp_mat(imageHeight, imageWidth, CV_8UC4, faceImageCharDate);
        cvtColor(temp_mat, srcmatimg, CV_RGBA2BGR);
    }


    string result;
    std::vector<TIEVD::FaceInfo> face_info = m_face_detect_mnn->Detect(srcmatimg, minFaceSize, 3);
    result = set_search_result (face_info);
    char *p = const_cast<char*>(result.c_str());
    jstring jres = env->NewStringUTF(p);
    env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
    return jres;//*/

}

JNIEXPORT jboolean JNICALL
Java_cn_sample_mnn_detect_Facetest_FaceDetectionModelUnInit(JNIEnv *env, jobject instance) {
    if(!detection_sdk_init_ok){
       // LOGD("人脸检测模型已经释放过或者未初始化");
        return true;
    }
    jboolean tDetectionUnInit = false;
    delete m_face_detect_mnn;
    detection_sdk_init_ok = false;
    tDetectionUnInit = true;
    return tDetectionUnInit;

}
}