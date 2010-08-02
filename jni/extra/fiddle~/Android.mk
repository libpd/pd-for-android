LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := fiddle~

LOCAL_CFLAGS := -DPD

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../../src

LOCAL_SRC_FILES := fiddle~.c

LOCAL_ALLOW_UNDEFINED_SYMBOLS := true

include $(BUILD_SHARED_LIBRARY)

