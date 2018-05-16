#!/usr/bin/env bash

# Usage: rebuild_tegola_android_all [tegola version string (if excluded, build_tegola.bat implicitly retrieves version string as git describe value)]

if [[ -z "${MY_ANDROID_STUDIO_WORKSPACE}" ]]; then
    echo FATAL ERROR! Environment variable MY_ANDROID_STUDIO_WORKSPACE not set! exiting...
    exit -1
fi

TEGOLA_VER_STRING=$1
REQUIRED_ARGS="-b_version_props_copy_path \"${MY_ANDROID_STUDIO_WORKSPACE}/src/github.com/terranodo/tegola-mobile/android/ControllerLib/Controller/src/main/assets\" -b_normalized_fn_bin_output_path \"${MY_ANDROID_STUDIO_WORKSPACE}/src/github.com/terranodo/tegola-mobile/android/ControllerLib/Controller/src/main/res/raw\""
if [[ -n "${TEGOLA_VER_STRING}" ]]; then
	COMMON_ARGS="-b_version $TEGOLA_VER_STRING $REQUIRED_ARGS"
else
	COMMON_ARGS="$REQUIRED_ARGS"
fi

t_platform=(android-arm android-arm64 android-x86 android-x86_64)

for index in ${!t_platform[*]} ; do
    echo rebuild_tegola_android_all.sh: Running tegola android-platform build: ${t_platform[$index]}
    eval "${MY_ANDROID_STUDIO_WORKSPACE}/src/github.com/terranodo/tegola-mobile/android/TMControllerSvcLib/TMControllerSvc/src/main/tegola-bin-buildscripts/linux/clean_tegola.sh -t_platform ${t_platform[$index]} $REQUIRED_ARGS"
    eval "${MY_ANDROID_STUDIO_WORKSPACE}/src/github.com/terranodo/tegola-mobile/android/TMControllerSvcLib/TMControllerSvc/src/main/tegola-bin-buildscripts/linux/build_tegola.sh -t_platform ${t_platform[$index]} $COMMON_ARGS"
    echo
done
