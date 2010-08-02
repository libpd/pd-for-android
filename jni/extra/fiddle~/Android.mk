include $(CLEAR_VARS)

LOCAL_MODULE := fiddle~

LOCAL_CFLAGS := -DPD

LOCAL_C_INCLUDES := $(LOCAL_PATH)/extra/fiddle~

LOCAL_SRC_FILES := $(LOCAL_PATH)/extra/fiddle~/fiddle~.c

LOCAL_ALLOW_UNDEFINED_SYMBOLS := true

# LOCAL_PRELINK_MODULE := false
# LOCAL_LDLIBS := -lc

include $(BUILD_SHARED_LIBRARY)

