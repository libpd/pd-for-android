LOCAL_PATH := $(call my-dir)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_accum
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../PdCore/jni/libpd/pure-data/src
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_accum.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../PdCore/libs/$(TARGET_ARCH_ABI) -lpd
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_senergy_tilde
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../PdCore/jni/libpd/pure-data/src
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_senergy~.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../PdCore/libs/$(TARGET_ARCH_ABI) -lpd
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_barkflux_accum_tilde
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../PdCore/jni/libpd/pure-data/src
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_barkflux_accum~.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../PdCore/libs/$(TARGET_ARCH_ABI) -lpd
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_centroid_tilde
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../PdCore/jni/libpd/pure-data/src
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_centroid~.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../PdCore/libs/$(TARGET_ARCH_ABI) -lpd
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_zcr_tilde
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../PdCore/jni/libpd/pure-data/src
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_zcr~.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../PdCore/libs/$(TARGET_ARCH_ABI) -lpd
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------
