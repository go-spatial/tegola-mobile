#!/usr/bin/env bash

@echo off
setlocal

# Usage: clean_tegola_android_all <accepts no arguments!>

SRC_MAIN_DIR=${MY_ANDROID_STUDIO_WORKSPACE}/src/github.com/terranodo/tegola-mobile/android/TMControllerSvcLib/TMControllerSvc/src/main
REQUIRED_ARGS="-b_version_props_copy_path \"${SRC_MAIN_DIR}/assets\" -b_normalized_fn_bin_output_path \"${SRC_MAIN_DIR}/res/raw\""

t_platform=(android-arm android-arm64 android-x86 android-x86_64)

for index in ${!t_platform[*]} ; do
    echo clean_tegola_android_all.sh: Cleaning tegola android-platform build: ${t_platform[$index]}
    eval "./clean_tegola.sh -t_platform ${t_platform[$index]} $REQUIRED_ARGS"
    echo
done
