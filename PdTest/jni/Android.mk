LOCAL_PATH := $(call my-dir)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := helloworld
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../PdCore/jni/libpd/pure-data/src
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := helloworld.c
LOCAL_LDLIBS := -L$(LOCAL_PATH)/../../PdCore/libs/$(TARGET_ARCH_ABI) -lpd
include $(BUILD_SHARED_LIBRARY)
