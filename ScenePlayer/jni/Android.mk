LOCAL_PATH := $(call my-dir)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := pd
LOCAL_EXPORT_C_INCLUDES := ../../PdCore/jni/libpd/pure-data/src
LOCAL_SRC_FILES := ../../PdCore/libs/$(TARGET_ARCH_ABI)/libpd.so
ifneq ($(MAKECMDGOALS),clean)
    include $(PREBUILT_SHARED_LIBRARY)
endif

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_accum
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_accum.c
LOCAL_SHARED_LIBRARIES = pd
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_senergy_tilde
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_senergy~.c
LOCAL_SHARED_LIBRARIES = pd
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_barkflux_accum_tilde
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_barkflux_accum~.c
LOCAL_SHARED_LIBRARIES = pd
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_centroid_tilde
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_centroid~.c
LOCAL_SHARED_LIBRARIES = pd
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := rj_zcr_tilde
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := rj_zcr~.c
LOCAL_SHARED_LIBRARIES = pd
include $(BUILD_SHARED_LIBRARY)

#---------------------------------------------------------------
