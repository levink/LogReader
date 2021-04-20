//
// Created by Konstantin on 24-Jan-19.
//

#include "Master.h"

Master::Master(JNIEnv *env, jobject instance) {
    this->env = env;
    this->instance = instance;
    this->objectClass = env->GetObjectClass(instance);
    this->saveItemMethodID = env->GetMethodID(objectClass,
            "saveItem",
            "(Ljava/lang/String;)V");
}
Master::~Master() {
    env->DeleteLocalRef(objectClass);
}

void Master::saveItem(const char *str) {
    jstring javaMethodParam = env->NewStringUTF(str);
    env->CallVoidMethod(instance, saveItemMethodID, javaMethodParam);
    env->DeleteLocalRef(javaMethodParam);
}

char *Master::convert(jbyteArray javaBlock) {
    return (char*)env->GetByteArrayElements(javaBlock, nullptr);
}

void Master::release(jbyteArray javaBlock, char *convertedBlock) {
    env->ReleaseByteArrayElements(javaBlock, (jbyte*)convertedBlock, 0);
}