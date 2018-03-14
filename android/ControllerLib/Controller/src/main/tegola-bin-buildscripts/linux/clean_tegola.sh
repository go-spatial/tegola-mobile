#!/usr/bin/env bash

# Usage: clean_tegola -t_platform android-arm|android-x86|android-arm64|android-x86_64|win-x86|win-x86_64
#
#     -t_platform                         target build platform
#                                           acceptable/supported target build platforms:
#                                             android-arm: android 32-bit arm processor ABI
#                                             android-x86: android 32-bit intel processor ABI
#                                             android-arm64: android 64-bit arm processor ABI
#                                             android-x86_64: android 64-bit intel processor ABI
#                                             win-x86: windows 32-bit intel processor
#                                             win-x86_64: windows 64-bit intel processor
#     -b_version_props_copy_path    		destination path for copy of version.props file - note that this path should not be terminated with "\"
#     -b_normalized_fn_bin_output_path    destination path for copy of binary w/ normalized fname - note that this path should not be terminated with "\"

# error codes
ERR__NO_ARGS=-1
ERR__INVALID_ARG=-2
ERR__INVALID_TARGET_PLATFORM=-3
ERR__INVALID_GOOS=-4

while [[ $# -gt 0 ]] ; do
    argument_name="$1"
    argument_value="$2"

    case $argument_name in
        -t_platform)
            t_platform="$argument_value"
            case $t_platform in
                android-arm)
                    GOOS=android
                    arch_friendly=arm
                    ndk_apilevel=16
                    ;;
                android-x86)
                    GOOS=android
                    arch_friendly=x86
                    ndk_apilevel=16
                    ;;
                android-arm64)
                    GOOS=android
                    arch_friendly=arm64
                    ndk_apilevel=21
                    ;;
                android-x86_64)
                    GOOS=android
                    arch_friendly=x86_64
                    ndk_apilevel=21
                    ;;
                win-x86)
                    GOOS=windows
                    arch_friendly=x86
                    ;;
                win-x86_64)
                     GOOS=windows
                     arch_friendly=x86_64
                    ;;
                *)    # unknown platform
                    echo clean_tegola.sh: ERROR: invalid target platform "$argument_value"
                    ;;
            esac
            ;;
        -b_version_props_copy_path)
            VER_PROPS__DIR=$argument_value
            ;;
        -b_normalized_fn_bin_output_path)
            OUTPUT_BIN_NORMALIZED_FN__DIR=$argument_value
            ;;
        *)    # unknown argument
            echo clean_tegola.sh: ERROR: invalid argument "$argument_name"
            exit $ERR__INVALID_ARG
            ;;
    esac

    shift
    shift
done

BASE_TEGOLA_SUBDIR=github.com/go-spatial/tegola

case $GOOS in
    android)
        OUTPUT_DIR=${MY_GOLANG_WORKSPACE}/pkg/${BASE_TEGOLA_SUBDIR}/android/api-${ndk_apilevel}/$arch_friendly
        OUTPUT_BIN_NORMALIZED_FN=tegola_bin__android_$arch_friendly
        ;;
    windows)
        OUTPUT_DIR=${MY_GOLANG_WORKSPACE}/pkg/${BASE_TEGOLA_SUBDIR}/windows/$arch_friendly
        OUTPUT_BIN_NORMALIZED_FN=tegola_bin__windows_$arch_friendly
        ;;
esac

# remove version.properties file from VER_PROPS__DIR if it exists...
ver_props_fn=version.properties
if [[ -n "${VER_PROPS__DIR}" ]]; then
	if [[ -e ${VER_PROPS__DIR}/ ]]; then
		echo clean_tegola.sh: Cleaning ${VER_PROPS__DIR}/$ver_props_fn
		rm ${VER_PROPS__DIR}/$ver_props_fn > /dev/null 2>&1
	fi
fi

# remove binary with normalized fname if it exists
if [[ -n "${OUTPUT_BIN_NORMALIZED_FN__DIR}" ]]; then
	if [[ -e ${OUTPUT_BIN_NORMALIZED_FN__DIR}/ ]]; then
		echo clean_tegola.sh: Cleaning ${OUTPUT_BIN_NORMALIZED_FN__DIR}/$OUTPUT_BIN_NORMALIZED_FN
		rm ${OUTPUT_BIN_NORMALIZED_FN__DIR}/$OUTPUT_BIN_NORMALIZED_FN > /dev/null 2>&1
	fi
fi

echo clean_tegola.sh: Cleaning ${OUTPUT_DIR}/ directory
rm -rf $OUTPUT_DIR > /dev/null 2>&1
