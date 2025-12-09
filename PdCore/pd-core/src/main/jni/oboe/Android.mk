# Build Oboe JNI binary

include $(CLEAR_VARS)

LOCAL_MODULE := pdnativeoboe
LOCAL_C_INCLUDES := $(PD_C_INCLUDES) $(LOCAL_PATH)/jni
LOCAL_SRC_FILES := ../oboe/z_jni_oboe_shared.c ../oboe/z_jni_oboe.cpp ../oboe/OboeEngine.cpp
LOCAL_SHARED_LIBRARIES := pd oboe
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)

$(call import-module,prefab/oboe)

