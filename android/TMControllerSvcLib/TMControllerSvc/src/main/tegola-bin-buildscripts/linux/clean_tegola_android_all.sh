#!/usr/bin/env bash

@echo off
setlocal

# Usage: clean_tegola_android_all <accepts no arguments!>

REQUIRED_ARGS="-b_version_props_copy_path \"${MY_ANDROID_STUDIO_WORKSPACE}/src/github.com/terranodo/tegola-mobile/android/ControllerLib/Controller/src/main/assets\" -b_normalized_fn_bin_output_path \"${MY_ANDROID_STUDIO_WORKSPACE}/src/github.com/terranodo/tegola-mobile/android/ControllerLib/Controller/src/main/res/raw\""

t_platform=(android-arm android-arm64 android-x86 android-x86_64)

for index in ${!t_platform[*]} ; do
    echo clean_tegola_android_all.sh: Cleaning tegola android-platform build: ${t_platform[$index]}
    eval "./clean_tegola.sh -t_platform ${t_platform[$index]} $REQUIRED_ARGS"
    echo
done
