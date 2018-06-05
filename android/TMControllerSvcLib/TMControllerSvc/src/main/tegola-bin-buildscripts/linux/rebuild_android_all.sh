#!/usr/bin/env bash

THIS_SCRIPT="rebuild_android_all.sh"

if [[ -z "${MY_ANDROID_STUDIO_WORKSPACE}" ]]; then
    echo FATAL ERROR! Environment variable MY_ANDROID_STUDIO_WORKSPACE not set! exiting...
    exit -1
fi

export TM_CTRLR_SRVC_ROOT_DIR=${MY_ANDROID_STUDIO_WORKSPACE}/src/github.com/terranodo/tegola-mobile/android/TMControllerSvcLib/TMControllerSvc
export TM_CTRLR_SRVC_SRC_MAIN_DIR=${TM_CTRLR_SRVC_ROOT_DIR}/src/main
echo ${THIS_SCRIPT}: set TM_CTRLR_SRVC_SRC_MAIN_DIR to: $TM_CTRLR_SRVC_SRC_MAIN_DIR

#t_platform=(android-arm android-arm64 android-x86 android-x86_64)
# build for android-x86_64 is disabled since we don't use it
t_platform=(android-arm android-arm64 android-x86)

SRC_SCRIPT="${TM_CTRLR_SRVC_SRC_MAIN_DIR}/tegola-bin-buildscripts/linux/tm_utils.sh"
#echo ${THIS_SCRIPT}: sourcing script ${SRC_SCRIPT}...
source ${SRC_SCRIPT}
FUNC_CALL="tm_init_bin_to_abi_mapping"
#echo ${THIS_SCRIPT}: calling func ${FUNC_CALL}...
eval "${FUNC_CALL}"
RETURN_CODE=$?
#echo ${THIS_SCRIPT}: return code from ${SRC_SCRIPT} func call "$FUNC_CALL": $RETURN_CODE

FUNC_CALL="tm_clean_libs"
#echo ${THIS_SCRIPT}: calling func ${FUNC_CALL}...
eval "${FUNC_CALL}"
RETURN_CODE=$?
#echo ${THIS_SCRIPT}: return code from ${SRC_SCRIPT} func call "$FUNC_CALL": $RETURN_CODE

SRC_SCRIPT="${TM_CTRLR_SRVC_SRC_MAIN_DIR}/tegola-bin-buildscripts/linux/build_bin.sh"
#echo ${THIS_SCRIPT}: sourcing script ${SRC_SCRIPT}...
source ${SRC_SCRIPT}
for index in ${!t_platform[*]} ; do
    unset TEGOLA_ARCH_FRIENDLY
    unset GOOS
    unset TEGOLA_VER_STRING
    unset TEGOLA_BUILD__LAST

    echo ${THIS_SCRIPT}: Running tegola android-platform build: ${t_platform[$index]}
    FUNC_CALL="do_build -t_platform ${t_platform[$index]}"
#    echo ${THIS_SCRIPT}: calling func ${FUNC_CALL}...
    eval "${FUNC_CALL}"
    RETURN_CODE=$?
#    echo ${THIS_SCRIPT}: return code from ${SRC_SCRIPT} func call "$FUNC_CALL": $RETURN_CODE
    if [[ $RETURN_CODE -ne 0 ]] ; then
        echo ${THIS_SCRIPT}: build failed, exiting scrtipt...
        exit $RETURN_CODE
    fi

    FUNC_CALL="tm_copy_bin_to_libs"
#    echo ${THIS_SCRIPT}: calling func ${FUNC_CALL}...
    eval "${FUNC_CALL}"
    RETURN_CODE=$?
#    echo ${THIS_SCRIPT}: return code from ${SRC_SCRIPT} func call "$FUNC_CALL": $RETURN_CODE
    if [[ $RETURN_CODE -ne 0 ]] ; then
        echo ${THIS_SCRIPT}: build failed, exiting scrtipt...
        exit $RETURN_CODE
    fi
done
unset TEGOLA_ARCH_FRIENDLY
unset GOOS
unset TEGOLA_VER_STRING
unset TEGOLA_BUILD__LAST

unset TM_CTRLR_SRVC_SRC_MAIN_DIR
unset TM_CTRLR_SRVC_ROOT_DIR
