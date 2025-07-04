LOCAL_PATH := $(call my-dir)

#---------------------------------------------------------------

#include $(CLEAR_VARS)
#LOCAL_MODULE := pd
#LOCAL_EXPORT_C_INCLUDES := ${LOCAL_PATH}/../../PdCore/src/main/jni/libpd/pure-data/src
#LOCAL_SRC_FILES := ${LOCAL_PATH}/../../PdCore/build/intermediates/library_jni/$(APP_OPTIM)/jni/$(TARGET_ARCH_ABI)/libpd.so
#ifneq ($(MAKECMDGOALS),clean)
#    include $(PREBUILT_SHARED_LIBRARY)
#endif

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := helloworld
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := helloworld.c
LOCAL_SHARED_LIBRARIES = pd
include $(BUILD_SHARED_LIBRARY)

# If you don't need your project to build with NDKs older than r21, you can omit
# this block.
ifneq ($(call ndk-major-at-least,21),true)
    $(call import-add-path,$(NDK_GRADLE_INJECTED_IMPORT_PATH))
endif
$(call import-module,prefab/PdCore)

#---------------------------------------------------------------
