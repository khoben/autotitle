LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := api-key
LOCAL_SRC_FILES := api-key.c

include $(BUILD_SHARED_LIBRARY)