LOCAL_PATH := $(call my-dir)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_accum
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../../pd-for-android/PdCore/jni/src
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_accum.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../../pd-for-android/PdCore/libs/armeabi -lpdnative
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_senergy~
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../../pd-for-android/PdCore/jni/src
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_senergy~.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../../pd-for-android/PdCore/libs/armeabi -lpdnative
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_barkflux_accum~
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../../pd-for-android/PdCore/jni/src
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_barkflux_accum~.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../../pd-for-android/PdCore/libs/armeabi -lpdnative
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_centroid~
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../../pd-for-android/PdCore/jni/src
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_centroid~.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../../pd-for-android/PdCore/libs/armeabi -lpdnative
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

LOCAL_MODULE := rj_zcr~
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../../pd-for-android/PdCore/jni/src
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_zcr~.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../../pd-for-android/PdCore/libs/armeabi -lpdnative
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

