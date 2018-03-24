#!/usr/bin/env bash

# Usage: build_tegola -t_platform android-arm|android-x86|android-arm64|android-x86_64|win-x86|win-x86_64 [-b_cgo_enabled_override_default 0|1] [-b_version <tegola version string>] [b_version_props_copy_path <path>] [b_normalized_fn_bin_output_path <path>]
#
#     -t_platform                         target build platform
#                                           acceptable/supported target build platforms:
#                                             android-arm: android 32-bit arm processor ABI
#                                             android-x86: android 32-bit intel processor ABI
#                                             android-arm64: android 64-bit arm processor ABI
#                                             android-android-x86_64: android 64-bit intel processor ABI
#                                             win-x86: windows 32-bit intel processor
#                                             win-x86_64: windows 64-bit intel processor
#     -b_cgo_enabled_override_default     overrides default value of CGO_ENABLED setting (in script below) for selected target build platform
#                                           acceptable/supported CGO_ENABLED values
#                                             0: CGO binding disabled
#                                             1: CGO binding enabled
#     -b_version                          version string inserted into tegola binary (also bin filename) - note that the value must not be malformed (no whitespace, escape literal, etc.)
#     -b_version_props_copy_path    		destination path for copy of version.props file - note that this path should not be terminated with "\"
#     -b_normalized_fn_bin_output_path    destination path for copy of binary w/ normalized fname - note that this path should not be terminated with "\"

#error codes
ERR__NO_ARGS=-1
ERR__INVALID_ARG=-2
ERR__INVALID_TARGET_PLATFORM=-3
ERR__INVALID_CGO_ENABLED_OVERRIDE=-4
ERR__INVALID_GOOS=-5

while [[ $# -gt 0 ]] ; do
    argument_name="$1"
    argument_value="$2"

    case $argument_name in
        -t_platform)
            t_platform="$argument_value"
            case $t_platform in
                android-arm)
                    export GOOS=android
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                    export GOARCH=arm
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                    export GOARM=7
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOARM="$(printenv GOARM)"
                    arch_friendly=arm
                    ndk_arch=arm-linux-androideabi
                    ndk_apilevel=16
                    ;;
                android-x86)
                    export GOOS=android
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                    export GOARCH=386
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                    arch_friendly=x86
                    ndk_arch=i686-linux-android
                    ndk_apilevel=16
                    ;;
                android-arm64)
                    export GOOS=android
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                    export GOARCH=arm64
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                    arch_friendly=arm64
                    ndk_arch=aarch64-linux-android
                    ndk_apilevel=21
                    ;;
                android-x86_64)
                    export GOOS=android
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                    export GOARCH=amd64
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                    arch_friendly=x86_64
                    ndk_arch=x86_64-linux-android
                    ndk_apilevel=21
                    ;;
                win-x86)
                    export GOOS=windows
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                    export GOARCH=386
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                    arch_friendly=x86
                    ;;
                win-x86_64)
                    export GOOS=windows
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                    export GOARCH=amd64
                    echo build_tegola.sh: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                    arch_friendly=x86_64
                    ;;
                *)    # unknown platform
                    echo build_tegola.sh: ERROR: invalid target platform "$argument_value"
                    ;;
            esac
            ;;
        -b_cgo_enabled_override_default)
            CGO_ENABLED_OVERRIDE_DEFAULT=$argument_value
            ;;
        -b_version)
            TEGOLA_VER_STRING=$argument_value
            ;;
        -b_version_props_copy_path)
            VER_PROPS__DIR=$argument_value
            ;;
        -b_normalized_fn_bin_output_path)
            OUTPUT_BIN_NORMALIZED_FN__DIR=$argument_value
            ;;
        *)    # unknown argument
            echo build_tegola.sh: ERROR: invalid argument "$argument_name"
            exit $ERR__INVALID_ARG
            ;;
    esac

    shift
    shift
done

BASE_TEGOLA_SUBDIR=github.com/go-spatial/tegola
TEGOLA_SRC_DIR=$GOPATH/src/$BASE_TEGOLA_SUBDIR
echo build_tegola.sh: go build command: pre-exec: meta: source dir: $TEGOLA_SRC_DIR

if [[ -z "${TEGOLA_VER_STRING}" ]]; then
	# build version string in format: "TAG-SHORT_COMMIT_HASH-BRANCH_NAME", e.g. "v0.6.1-436b82e-master"
	cd $TEGOLA_SRC_DIR
	TEGOLA_VER_STRING__TAG="$(git describe --tags --always)"
	TEGOLA_VER_STRING__SHORT_HASH="$(git rev-parse --short HEAD)"
	TEGOLA_VER_STRING__BRANCH="$(git rev-parse --abbrev-ref HEAD)"
	TEGOLA_VER_STRING=${TEGOLA_VER_STRING__TAG}_${TEGOLA_VER_STRING__SHORT_HASH}_${TEGOLA_VER_STRING__BRANCH}
fi
echo build_tegola.sh: go build command: pre-exec: meta: cmd.Version: $TEGOLA_VER_STRING

case $GOOS in
    android)
        # android go builds require cross-compiling via the Android NDK - set cross-compilation options (via env vars below)
        echo build_tegola.sh: go build command: pre-exec: meta: android x-compile: ndk: arch: $ndk_arch
        echo build_tegola.sh: go build command: pre-exec: meta: android x-compile: ndk: apilevel: $ndk_apilevel

        ANDROID_NDK_CURRENT_TOOLCHAIN=${MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME}/api-${ndk_apilevel}/$arch_friendly
        echo build_tegola.sh: go build command: pre-exec: meta: android x-compile: ndk: toolchain: $ANDROID_NDK_CURRENT_TOOLCHAIN

        # ndk x-compile requires "CC" env var
        export CC=${ANDROID_NDK_CURRENT_TOOLCHAIN}/bin/$ndk_arch-gcc
        echo build_tegola.sh: go build command: pre-exec: go build env: var: CC="$(printenv CC)"

        # ndk x-compile also requires "CXX" env var
        export CXX=${ANDROID_NDK_CURRENT_TOOLCHAIN}/bin/$ndk_arch-g++
        echo build_tegola.sh: go build command: pre-exec: go build env: var: CXX="$(printenv CXX)"

        # CGO must be enabled for go build to make proper use of Android NDK (since Android NDK is C/C++ source) - see https://github.com/golang/go/wiki/cgo
        export CGO_ENABLED=1
        echo build_tegola.sh: go build command: pre-exec: go build env: var: CGO_ENABLED="$(printenv CGO_ENABLED)"

        # set go build cmd "pkgdir" arg val - android go builds require use of: gomobile package
        GO_BLD_CMD_ARG_VAL__PKGDIR=${MY_GOLANG_WORKSPACE}/pkg/gomobile/pkg_android_$arch_friendly
        echo build_tegola.sh: go build command: pre-exec: command string: build: arg: pkgdir: $GO_BLD_CMD_ARG_VAL__PKGDIR

        # set go build cmd "ldflags" arg val - set version string; also, android go builds require use of additional "extldflags" arg
        GO_BLD_CMD_ARG_VAL__LDFLAGS="-w -X ${TEGOLA_SRC_DIR}/cmd/tegola/cmd.Version=${TEGOLA_VER_STRING} -extldflags=-pie"
        echo build_tegola.sh: go build command: pre-exec: command string: build: arg: ldflags: $GO_BLD_CMD_ARG_VAL__LDFLAGS

        # set go build cmd "o" arg val - this specifies output path of go build explicitly
        OUTPUT_DIR=${MY_GOLANG_WORKSPACE}/pkg/${BASE_TEGOLA_SUBDIR}/android/api-${ndk_apilevel}/$arch_friendly
        OUTPUT_BIN=tegola__${TEGOLA_VER_STRING}__android_${arch_friendly}.bin
        OUTPUT_BIN_NORMALIZED_FN=tegola_bin__android_$arch_friendly
        OUTPUT_PATH=${OUTPUT_DIR}/${OUTPUT_BIN}
        GO_BLD_CMD_ARG_VAL__O=$OUTPUT_PATH
        echo build_tegola.sh: go build command: pre-exec: command string: build: arg: o \(explicit output path\): $GO_BLD_CMD_ARG_VAL__O
        ;;
    windows)
        # by default, standard tegola windows build-target builds do not use CGO bindings, however some new tegola features may actually require it globally - e.g. geopkg provider, so, althgough we default to disable CGO, we allow it to be turned on via a cmdline arg to this script
        if [[ -n "${CGO_ENABLED_OVERRIDE_DEFAULT}" ]]; then
            CGO_ENABLED=$CGO_ENABLED_OVERRIDE_DEFAULT
        else
            CGO_ENABLED=0
        fi
        echo build_tegola.sh: go build command: pre-exec: go build env: var: CGO_ENABLED="$(printenv CGO_ENABLED)"

        # set go build cmd "ldflags" arg val - set version string
        tegola/cmd/tegola/cmd.Version=
        GO_BLD_CMD_ARG_VAL__LDFLAGS="-w -X ${TEGOLA_SRC_DIR}/cmd/tegola/cmd.Version=${TEGOLA_VER_STRING}"
        echo build_tegola.sh: go build command: pre-exec: command string: build: arg: ldflags: $GO_BLD_CMD_ARG_VAL__LDFLAGS

        # set go build cmd "o" arg val - this specifies output path of go build explicitly
        OUTPUT_DIR=${MY_GOLANG_WORKSPACE}/pkg/${BASE_TEGOLA_SUBDIR}/windows/$arch_friendly
        OUTPUT_BIN=tegola__${TEGOLA_VER_STRING}__windows_${arch_friendly}.bin
        OUTPUT_BIN_NORMALIZED_FN=tegola_bin__windows_$arch_friendly
        OUTPUT_PATH=${OUTPUT_DIR}/${OUTPUT_BIN}
        GO_BLD_CMD_ARG_VAL__O=$OUTPUT_PATH
        echo build_tegola.sh: go build command: pre-exec: command string: build: arg: o \(explicit output path\): $GO_BLD_CMD_ARG_VAL__O
        ;;
esac

# build go build cmd string
go_build_cmd="go build -p=8"
if [[ -n "${GO_BLD_CMD_ARG_VAL__PKGDIR}" ]]; then
	go_build_cmd="${go_build_cmd} -pkgdir=\"${GO_BLD_CMD_ARG_VAL__PKGDIR}\""
fi
go_build_cmd="${go_build_cmd} -tags=\"\""
if [[ -n "${GO_BLD_CMD_ARG_VAL__LDFLAGS}" ]]; then
	go_build_cmd="${go_build_cmd} -ldflags=\"${GO_BLD_CMD_ARG_VAL__LDFLAGS}\""
fi
if [[ -n "${GO_BLD_CMD_ARG_VAL__O}" ]]; then
	go_build_cmd="${go_build_cmd} -o ${GO_BLD_CMD_ARG_VAL__O}"
fi
go_build_cmd="${go_build_cmd} -x -a -v ."
echo build_tegola.sh: go build command: pre-exec: command string: build: final: $go_build_cmd

# create OUTPUT_DIR
mkdir -p $OUTPUT_DIR > /dev/null 2>&1

rm ${OUTPUT_DIR}/go_env.txt > /dev/null 2>&1
{
    go env
} > ${OUTPUT_DIR}/go_env.txt
if [[ -e ${OUTPUT_DIR}/go_env.txt ]]; then
    echo build_tegola.sh: go build command: pre-exec: go build env vars: save: result: successfully saved to ${OUTPUT_DIR}/go_env.txt
else
    echo build_tegola.sh: go build command: pre-exec: go build env vars: save: result: FAILED to save to ${OUTPUT_DIR}/go_env.txt
fi


# track ver in version.properties in output dir
ver_props_fn=version.properties
rm ${OUTPUT_DIR}/$ver_props_fn > /dev/null 2>&1
echo TEGOLA_BIN_VER=${TEGOLA_VER_STRING}>${OUTPUT_DIR}/${ver_props_fn}
if [[ -e ${OUTPUT_DIR}/${ver_props_fn} ]]; then
    echo build_tegola.sh: go build command: pre-exec: $ver_props_fn file: create: result: successfully created ${OUTPUT_DIR}/${ver_props_fn}
else
    echo build_tegola.sh: go build command: pre-exec: $ver_props_fn file: create: result: FAILED to create ${OUTPUT_DIR}/${ver_props_fn}
fi

cd ${TEGOLA_SRC_DIR}/cmd/tegola/
build_output=${OUTPUT_DIR}/build_${OUTPUT_BIN}.txt
echo build_tegola.sh: go build command: exec: running command in "$(pwd)"...
(eval $go_build_cmd) > $build_output 2>&1
wait
echo build_tegola.sh: go build command: exec: complete

if [[ -e $OUTPUT_PATH ]]; then
	echo build_tegola.sh: go build command: post-exec: successfully built tegola binary $OUTPUT_PATH
	chmod a+x ${OUTPUT_PATH}
	if [[ -n "${OUTPUT_BIN_NORMALIZED_FN__DIR}" ]]; then
	    if [[ ! -e ${OUTPUT_BIN_NORMALIZED_FN__DIR}/ ]]; then
	        mkdir -p ${OUTPUT_BIN_NORMALIZED_FN__DIR}/
	        if [[ -e ${OUTPUT_BIN_NORMALIZED_FN__DIR}/ ]]; then
	            echo build_tegola.sh: go build command: post-exec: successfully created ${OUTPUT_BIN_NORMALIZED_FN__DIR}/ directory
	        else
	            echo build_tegola.sh: go build command: post-exec: failed to create ${OUTPUT_BIN_NORMALIZED_FN__DIR}/ directory
	        fi
	    fi
		if [[ -e ${OUTPUT_BIN_NORMALIZED_FN__DIR}/ ]]; then
			rm ${OUTPUT_BIN_NORMALIZED_FN__DIR}/${OUTPUT_BIN_NORMALIZED_FN} > /dev/null 2>&1
			cp $OUTPUT_PATH ${OUTPUT_BIN_NORMALIZED_FN__DIR}/${OUTPUT_BIN_NORMALIZED_FN}
			if [[ -e ${OUTPUT_BIN_NORMALIZED_FN__DIR}/${OUTPUT_BIN_NORMALIZED_FN} ]]; then
				if [[ -n "${VER_PROPS__DIR}" ]]; then
					if [[ ! -e ${VER_PROPS__DIR}/ ]]; then
					    mkdir -p ${VER_PROPS__DIR}/
					fi
					cp ${OUTPUT_DIR}/$ver_props_fn ${VER_PROPS__DIR}/$ver_props_fn
                fi
				echo build_tegola.sh: go build command: post-exec: successfully copied $OUTPUT_PATH to ${OUTPUT_BIN_NORMALIZED_FN__DIR}/${OUTPUT_BIN_NORMALIZED_FN}
			else
				echo build_tegola.sh: go build command: post-exec: failed to copy $OUTPUT_PATH to ${OUTPUT_BIN_NORMALIZED_FN__DIR}/${OUTPUT_BIN_NORMALIZED_FN}
			fi
		else
			echo build_tegola.sh: go build command: post-exec: failed to copy $OUTPUT_PATH to ${OUTPUT_BIN_NORMALIZED_FN__DIR}/$OUTPUT_BIN_NORMALIZED_FN since ${OUTPUT_BIN_NORMALIZED_FN__DIR}/ does not exist!
		fi
	fi
else
	echo build_tegola.sh: go build command: post-exec: failed to build $OUTPUT_PATH - see build output file $build_output for details
fi

echo build_tegola.sh: all done!
