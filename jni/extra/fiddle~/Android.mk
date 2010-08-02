LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := fiddle~

LOCAL_ALLOW_UNDEFINED_SYMBOLS := true

LOCAL_CFLAGS := -DPD -I$(LOCAL_PATH)/../..

LOCAL_SRC_FILES := fiddle~.c

LOCAL_LDLIBS := -lc

include $(BUILD_SHARED_LIBRARY)
