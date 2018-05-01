LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS 	:= -llog

LOCAL_MODULE    := tcsnativeauxsupp
LOCAL_SRC_FILES := tcs_native_aux_supp.cpp

include $(BUILD_SHARED_LIBRARY)