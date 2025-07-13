LOCAL_PATH := $(call my-dir)

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
$(call import-module,prefab/pd-core)

#---------------------------------------------------------------
