//
// Created by Konstantin on 24-Jan-19.
//

#include <jni.h>

#ifndef ANDROID_MASTER_H
#define ANDROID_MASTER_H

class Master {
    JNIEnv* env;
    jobject instance;
    jclass objectClass;
    jmethodID saveItemMethodID;
public:
    Master(JNIEnv *env, jobject instance);
    ~Master();
    void saveItem(const char* str);
    char* convert(jbyteArray javaBlock);
    void release(jbyteArray javaBlock, char* convertedBlock);
};


#endif //ANDROID_MASTER_H
