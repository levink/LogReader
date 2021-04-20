#include <jni.h>
#include "Master.h"
#include "CLogReader.h"
#include "Util.h"

#define VOID extern "C" JNIEXPORT void JNICALL
#define BOOL extern "C" JNIEXPORT jboolean JNICALL


CLogReader* reader = nullptr;


BOOL Java_com_logger_classes_LogReader_setFilter(JNIEnv *env, jobject, jstring filter_) {
    delete reader;
    reader = new CLogReader();

    const char *filter = env->GetStringUTFChars(filter_, nullptr);
    bool result = reader->setFilter(filter);
    env->ReleaseStringUTFChars(filter_, filter);
    return (jboolean)result;
}

BOOL Java_com_logger_classes_LogReader_addBlock(JNIEnv *env, jobject instance, jbyteArray jBlock, jint count) {
    if (count <= 0) {
        return JNI_FALSE;
    }

    Master master(env, instance);
    char* block = master.convert(jBlock);
    bool added = reader->addSourceBlock(block, count);
    reader->parse(false, [&master](const String& item) {
        master.saveItem(item.getValue());
    });
    master.release(jBlock, block);

    return static_cast<jboolean>(added);
}


VOID Java_com_logger_classes_LogReader_parseLast(JNIEnv *env, jobject instance) {
    Master master(env, instance);
    reader->parse(true, [&master](const String& item) {
        master.saveItem(item.getValue());
    });
    delete reader;
    reader = nullptr;
}


