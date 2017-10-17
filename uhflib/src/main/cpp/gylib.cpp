//
// Created by RuanJian-GuoYong on 2017/8/7.
//
#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_mylib_GyFunction_getString(JNIEnv *env, jobject instance) {
    std::string hell = "";
    return env->NewStringUTF("HELLO C++");
}
