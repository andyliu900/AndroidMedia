#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_ideacode_android_1audio_ui_JniUsageTestActivity_stringFromJNI(
        JNIEnv* env,
        jobject) {
    std::string hello = "Hello from C++";
    LOGD("From C++: %s", hello.c_str());
    return env->NewStringUTF(hello.c_str());
}