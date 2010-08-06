LOCAL_PATH := $(call my-dir)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_accum
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_accum.c
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_senergy~
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_senergy~.c
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_barkflux_accum~
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_barkflux_accum~.c
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_centroid~
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_centroid~.c
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

LOCAL_MODULE := rj_zcr~
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_zcr~.c
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

