#!/usr/bin/env bash

#general error codes
ERROR_CODE__SUCCEEDED=0
ERROR_CODE__FAILURE=-1          #eval to 255
AVAILABLE_ERROR_CODE=-2

# Usage: do_build -t_platform android-arm|android-x86|android-arm64|android-x86_64|win-x86|win-x86_64|darwin-x86|darwin-x86_64 [-b_cgo_enabled_override_default 0|1] [-b_version <tegola version string>]
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

#error codes
DOBUILD__ERROR_CODE__SUCCEEDED=$ERROR_CODE__SUCCEEDED
DOBUILD__ERROR_CODE__BUILD_FAILURE=$ERROR_CODE__FAILURE
DOBUILD__ERROR_CODE__NO_ARGS=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
DOBUILD__ERROR_CODE__INVALID_ARG=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
DOBUILD__ERROR_CODE__INVALID_TARGET_PLATFORM=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
DOBUILD__ERROR_CODE__INVALID_CGO_ENABLED_OVERRIDE=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
DOBUILD__ERROR_CODE__INVALID_GOOS=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
DOBUILD__ERROR_CODE__PKGDIR_DOES_NOT_EXIST=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
do_build() {
    RESULT=$DOBUILD__ERROR_CODE__BUILD_FAILURE

    while [[ $# -gt 0 ]] ; do
        argument_name="$1"
        argument_value="$2"

        case $argument_name in
            -t_platform)
                t_platform="$argument_value"
                case $t_platform in
                    android-arm)
                        export GOOS=android
                        echo do_build: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                        export GOARCH=arm
                        echo do_build: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                        export GOARM=7
                        echo do_build: go build command: pre-exec: go build env: var: GOARM="$(printenv GOARM)"
                        export TEGOLA_ARCH_FRIENDLY=arm
                        ndk_arch=arm-linux-androideabi
                        ndk_apilevel=16
                        ;;
                    android-x86)
                        export GOOS=android
                        echo do_build: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                        export GOARCH=386
                        echo do_build: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                        export TEGOLA_ARCH_FRIENDLY=x86
                        ndk_arch=i686-linux-android
                        ndk_apilevel=16
                        ;;
                    android-arm64)
                        export GOOS=android
                        echo do_build: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                        export GOARCH=arm64
                        echo do_build: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                        export TEGOLA_ARCH_FRIENDLY=arm64
                        ndk_arch=aarch64-linux-android
                        ndk_apilevel=21
                        ;;
                    android-x86_64)
                        export GOOS=android
                        echo do_build: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                        export GOARCH=amd64
                        echo do_build: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                        export TEGOLA_ARCH_FRIENDLY=x86_64
                        ndk_arch=x86_64-linux-android
                        ndk_apilevel=21
                        ;;
                    win-x86)
                        export GOOS=windows
                        echo do_build: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                        export GOARCH=386
                        echo do_build: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                        export TEGOLA_ARCH_FRIENDLY=x86
                        ;;
                    win-x86_64)
                        export GOOS=windows
                        echo do_build: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                        export GOARCH=amd64
                        echo do_build: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                        export TEGOLA_ARCH_FRIENDLY=x86_64
                        ;;
                    darwin-x86)
                        export GOOS=darwin
                        echo do_build: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                        export GOARCH=386
                        echo do_build: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                        export TEGOLA_ARCH_FRIENDLY=x86
                        ;;
                    darwin-x86_64)
                        export GOOS=darwin
                        echo do_build: go build command: pre-exec: go build env: var: GOOS="$(printenv GOOS)"
                        export GOARCH=amd64
                        echo do_build: go build command: pre-exec: go build env: var: GOARCH="$(printenv GOARCH)"
                        export TEGOLA_ARCH_FRIENDLY=x86_64
                        ;;
                    *)    # unknown platform
                        echo do_build: ERROR: invalid target platform "$argument_value"
                        ;;
                esac
                ;;
            -b_cgo_enabled_override_default)
                CGO_ENABLED_OVERRIDE_DEFAULT=$argument_value
                ;;
            -b_version)
                TEGOLA_VER_STRING=$argument_value
                ;;
            *)    # unknown argument
                echo do_build: ERROR: invalid argument "$argument_name"
                RESULT=$DOBUILD__ERROR_CODE__INVALID_ARG
                return $RESULT
                ;;
        esac

        shift
        shift
    done

    BASE_TEGOLA_SUBDIR=github.com/go-spatial/tegola
    TEGOLA_SRC_DIR=${GOPATH}/src/$BASE_TEGOLA_SUBDIR
    BUILD_WRK_DIR=${TEGOLA_SRC_DIR}/cmd/tegola
    echo do_build: go build command: pre-exec: meta: source dir: $TEGOLA_SRC_DIR

    if [[ -z "${TEGOLA_VER_STRING}" ]]; then
        echo do_build: go build command: pre-exec: meta: TEGOLA_VER_STRING: retrieving from git \(working dir ${TEGOLA_SRC_DIR}\)
        # build version string in format: "COMMIT_HASH.BRANCH_NAME.GOOS.TEGOLA_ARCH_FRIENDLY", e.g. "436b82e.master.android.arm"
        cd $TEGOLA_SRC_DIR
        TEGOLA_VER_STRING__SHORT_HASH="$(git rev-parse --short HEAD)"
        TEGOLA_VER_STRING__BRANCH="$(git rev-parse --abbrev-ref HEAD)"
        TEGOLA_VER_STRING=${TEGOLA_VER_STRING__SHORT_HASH}.${TEGOLA_VER_STRING__BRANCH}.${GOOS}.${TEGOLA_ARCH_FRIENDLY}
    else
        echo do_build: go build command: pre-exec: meta: TEGOLA_VER_STRING: using provided ver string "${TEGOLA_VER_STRING}"
    fi
    echo do_build: go build command: pre-exec: meta: TEGOLA_VER_STRING: $TEGOLA_VER_STRING

    case $GOOS in
        android)
            if [[ -z "${MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME}" ]]; then
                echo FATAL ERROR! Environment variable MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME not set! exiting...
                exit ${DOBUILD__ERROR_CODE__INVALID_ARG}
            fi

            # android go builds require cross-compiling via the Android NDK - set cross-compilation options (via env vars below)
            echo do_build: go build command: pre-exec: meta: android x-compile: ndk: arch: $ndk_arch
            echo do_build: go build command: pre-exec: meta: android x-compile: ndk: apilevel: $ndk_apilevel

            ANDROID_NDK_CURRENT_TOOLCHAIN=${MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME}/api-${ndk_apilevel}/$TEGOLA_ARCH_FRIENDLY
            echo do_build: go build command: pre-exec: meta: android x-compile: ndk: toolchain: $ANDROID_NDK_CURRENT_TOOLCHAIN

            # ndk x-compile requires "CC" env var
            export CC=${ANDROID_NDK_CURRENT_TOOLCHAIN}/bin/$ndk_arch-gcc
            echo do_build: go build command: pre-exec: go build env: var: CC="$(printenv CC)"

            # ndk x-compile also requires "CXX" env var
            export CXX=${ANDROID_NDK_CURRENT_TOOLCHAIN}/bin/$ndk_arch-g++
            echo do_build: go build command: pre-exec: go build env: var: CXX="$(printenv CXX)"

            # CGO must be enabled for go build to make proper use of Android NDK (since Android NDK is C/C++ source) - see https://github.com/golang/go/wiki/cgo
            export CGO_ENABLED=1
            echo do_build: go build command: pre-exec: go build env: var: CGO_ENABLED="$(printenv CGO_ENABLED)"

            # set go build cmd "pkgdir" arg val - android go builds require use of: gomobile package
            GO_BLD_CMD_ARG_VAL__PKGDIR=${GOPATH}/pkg/gomobile/pkg_android_${GOARCH}
            echo do_build: go build command: pre-exec: command string: build: arg: pkgdir: $GO_BLD_CMD_ARG_VAL__PKGDIR
            if [[ ! -e ${GO_BLD_CMD_ARG_VAL__PKGDIR}/ ]]; then
                echo do_build: go build command: pre-exec: command string: build: arg: pkgdir: FATAL ERROR!!! ${GO_BLD_CMD_ARG_VAL__PKGDIR}/ directory DOES NOT EXIST! BUILD WILL FAIL!!!
                RESULT=$DOBUILD__ERROR_CODE__PKGDIR_DOES_NOT_EXIST
                return $RESULT
            fi

            # set go build cmd "tags" arg val - disable rediscache on android
            GO_BLD_CMD_ARG_VAL__TAGS="noRedisCache"
            echo do_build: go build command: pre-exec: command string: build: arg: tags: $GO_BLD_CMD_ARG_VAL__TAGS

            # set go build cmd "ldflags" arg val - set version string; also, android go builds require use of additional "extldflags" arg
            GO_BLD_CMD_ARG_VAL__LDFLAGS="-w -X ${BASE_TEGOLA_SUBDIR}/cmd/tegola/cmd.Version=${TEGOLA_VER_STRING} -linkmode=external '-extldflags=-pie'"
            echo do_build: go build command: pre-exec: command string: build: arg: ldflags: $GO_BLD_CMD_ARG_VAL__LDFLAGS

            # set go build cmd "o" arg val - this specifies output path of go build explicitly
            TEGOLA_BUILD_OUTPUT_DIR=${GOPATH}/pkg/${BASE_TEGOLA_SUBDIR}/android/api-${ndk_apilevel}/$TEGOLA_ARCH_FRIENDLY
            echo do_build: go build command: pre-exec: go build: set output-dir: $TEGOLA_BUILD_OUTPUT_DIR
            TEGOLA_BUILD_BIN=tegola__${TEGOLA_VER_STRING}.bin
            TEGOLA_BUILD_BIN_PATH=${TEGOLA_BUILD_OUTPUT_DIR}/${TEGOLA_BUILD_BIN}
            GO_BLD_CMD_ARG_VAL__O=$TEGOLA_BUILD_BIN_PATH
            echo do_build: go build command: pre-exec: command string: build: arg: o \(explicit output path\): $GO_BLD_CMD_ARG_VAL__O
            ;;
        windows)
            # by default, standard tegola windows build-target builds do not use CGO bindings, however some new tegola features may actually require it globally - e.g. geopkg provider, so, althgough we default to disable CGO, we allow it to be turned on via a cmdline arg to this script
            if [[ -n "${CGO_ENABLED_OVERRIDE_DEFAULT}" ]]; then
                CGO_ENABLED=$CGO_ENABLED_OVERRIDE_DEFAULT
            else
                CGO_ENABLED=0
            fi
            echo do_build: go build command: pre-exec: go build env: var: CGO_ENABLED="$(printenv CGO_ENABLED)"

            # set go build cmd "ldflags" arg val - set version string
            GO_BLD_CMD_ARG_VAL__LDFLAGS="-w -X ${BASE_TEGOLA_SUBDIR}/cmd/tegola/cmd.Version=${TEGOLA_VER_STRING}"
            echo do_build: go build command: pre-exec: command string: build: arg: ldflags: $GO_BLD_CMD_ARG_VAL__LDFLAGS

            # set go build cmd "o" arg val - this specifies output path of go build explicitly
            TEGOLA_BUILD_OUTPUT_DIR=${GOPATH}/pkg/${BASE_TEGOLA_SUBDIR}/windows/$TEGOLA_ARCH_FRIENDLY
            echo do_build: go build command: pre-exec: go build output-dir: $TEGOLA_BUILD_OUTPUT_DIR
            TEGOLA_BUILD_BIN=tegola__${TEGOLA_VER_STRING}.bin
            TEGOLA_BUILD_BIN_PATH=${TEGOLA_BUILD_OUTPUT_DIR}/${TEGOLA_BUILD_BIN}
            GO_BLD_CMD_ARG_VAL__O=$TEGOLA_BUILD_BIN_PATH
            echo do_build: go build command: pre-exec: command string: build: arg: o \(explicit output path\): $GO_BLD_CMD_ARG_VAL__O
            ;;
        darwin)
            # by default, standard tegola darwin build-target builds do not use CGO bindings, however some new tegola features may actually require it globally - e.g. geopkg provider, so, althgough we default to disable CGO, we allow it to be turned on via a cmdline arg to this script
            if [[ -n "${CGO_ENABLED_OVERRIDE_DEFAULT}" ]]; then
                CGO_ENABLED=$CGO_ENABLED_OVERRIDE_DEFAULT
            else
                CGO_ENABLED=0
            fi
            echo do_build: go build command: pre-exec: go build env: var: CGO_ENABLED="$(printenv CGO_ENABLED)"

            # set go build cmd "ldflags" arg val - set version string - note that this path is relative to BUILD_WRK_DIR
            GO_BLD_CMD_ARG_VAL__LDFLAGS="-w -X ${BASE_TEGOLA_SUBDIR}/cmd/tegola/cmd.Version=${TEGOLA_VER_STRING}"
            echo do_build: go build command: pre-exec: command string: build: arg: ldflags: $GO_BLD_CMD_ARG_VAL__LDFLAGS

            # set go build cmd "o" arg val - this specifies output path of go build explicitly
            TEGOLA_BUILD_OUTPUT_DIR=${GOPATH}/pkg/${BASE_TEGOLA_SUBDIR}/darwin/$TEGOLA_ARCH_FRIENDLY
            echo do_build: go build command: pre-exec: go build output-dir: $TEGOLA_BUILD_OUTPUT_DIR
            TEGOLA_BUILD_BIN=tegola__${TEGOLA_VER_STRING}.bin
            TEGOLA_BUILD_BIN_PATH=${TEGOLA_BUILD_OUTPUT_DIR}/${TEGOLA_BUILD_BIN}
            GO_BLD_CMD_ARG_VAL__O=$TEGOLA_BUILD_BIN_PATH
            echo do_build: go build command: pre-exec: command string: build: arg: o \(explicit output path\): $GO_BLD_CMD_ARG_VAL__O
            ;;
    esac

    # build go build cmd string
    go_build_cmd="go build -p=8"
    if [[ -n "${GO_BLD_CMD_ARG_VAL__PKGDIR}" ]]; then
        go_build_cmd="${go_build_cmd} -pkgdir=\"${GO_BLD_CMD_ARG_VAL__PKGDIR}\""
    fi
    if [[ -n "${GO_BLD_CMD_ARG_VAL__TAGS}" ]]; then
        go_build_cmd="${go_build_cmd} -tags \"${GO_BLD_CMD_ARG_VAL__TAGS}\""
    fi
    if [[ -n "${GO_BLD_CMD_ARG_VAL__LDFLAGS}" ]]; then
        go_build_cmd="${go_build_cmd} -ldflags \"${GO_BLD_CMD_ARG_VAL__LDFLAGS}\""
    fi
    if [[ -n "${GO_BLD_CMD_ARG_VAL__O}" ]]; then
        go_build_cmd="${go_build_cmd} -o ${GO_BLD_CMD_ARG_VAL__O}"
    fi
    go_build_cmd="${go_build_cmd} -x -a -v ."
    echo do_build: go build command: pre-exec: command string: build: final: $go_build_cmd

    # create TEGOLA_BUILD_OUTPUT_DIR
    mkdir -p $TEGOLA_BUILD_OUTPUT_DIR > /dev/null 2>&1

    if [[ -e ${TEGOLA_BUILD_OUTPUT_DIR}/go_env.txt ]]; then
        echo do_build: go build command: pre-exec: go build env vars: existing go_env.txt file: delete: ${TEGOLA_BUILD_OUTPUT_DIR}/go_env.txt
        rm ${TEGOLA_BUILD_OUTPUT_DIR}/go_env.txt > /dev/null 2>&1
    fi
    {
        go env
    } > ${TEGOLA_BUILD_OUTPUT_DIR}/go_env.txt
    if [[ -e ${TEGOLA_BUILD_OUTPUT_DIR}/go_env.txt ]]; then
        echo do_build: go build command: pre-exec: go build env vars: new go_env.txt file: save: result: successfully saved to ${TEGOLA_BUILD_OUTPUT_DIR}/go_env.txt
    else
        echo do_build: go build command: pre-exec: go build env vars: new go_env.txt file: save: result: FAILED to save to ${TEGOLA_BUILD_OUTPUT_DIR}/go_env.txt
    fi

    # track ver in tegola_version.properties in output dir
    ver_props_fn=tegola_version.properties
    if [[ -e ${TEGOLA_BUILD_OUTPUT_DIR}/${ver_props_fn} ]]; then
        echo do_build: go build command: pre-exec: existing $ver_props_fn file: delete: ${TEGOLA_BUILD_OUTPUT_DIR}/${ver_props_fn}
        rm ${TEGOLA_BUILD_OUTPUT_DIR}/${ver_props_fn} > /dev/null 2>&1
    fi
    echo TEGOLA_BIN_VER=${TEGOLA_VER_STRING}>${TEGOLA_BUILD_OUTPUT_DIR}/${ver_props_fn}
    if [[ -e ${TEGOLA_BUILD_OUTPUT_DIR}/${ver_props_fn} ]]; then
        echo do_build: go build command: pre-exec: new $ver_props_fn file: save: result: successfully created ${TEGOLA_BUILD_OUTPUT_DIR}/${ver_props_fn}
    else
        echo do_build: go build command: pre-exec: new $ver_props_fn file: save: result: FAILED to create ${TEGOLA_BUILD_OUTPUT_DIR}/${ver_props_fn}
    fi

    if [[ -e $TEGOLA_BUILD_BIN_PATH ]]; then
        echo do_build: go build command: pre-exec: existing $TEGOLA_BUILD_BIN file: delete: $TEGOLA_BUILD_BIN_PATH
        rm $TEGOLA_BUILD_BIN_PATH > /dev/null 2>&1
    fi
    build_output=${TEGOLA_BUILD_OUTPUT_DIR}/build_${TEGOLA_BUILD_BIN}.txt
    if [[ -e ${TEGOLA_BUILD_OUTPUT_DIR}/build_${TEGOLA_BUILD_BIN}.txt ]]; then
        echo do_build: go build command: pre-exec: existing build_${TEGOLA_BUILD_BIN}.txt file: delete: ${TEGOLA_BUILD_OUTPUT_DIR}/build_${TEGOLA_BUILD_BIN}.txt
        rm ${TEGOLA_BUILD_OUTPUT_DIR}/build_${TEGOLA_BUILD_BIN}.txt > /dev/null 2>&1
    fi
    cd $BUILD_WRK_DIR
    echo do_build: go build command: exec: running command \in build working directory "$(pwd)"...
    (eval $go_build_cmd) > $build_output 2>&1
    wait
    echo do_build: go build command: exec: complete

    if [[ -e $TEGOLA_BUILD_BIN_PATH ]]; then
        echo do_build: go build command: post-exec: successfully built tegola binary $TEGOLA_BUILD_BIN_PATH
        chmod a+x ${TEGOLA_BUILD_BIN_PATH}
        export TEGOLA_BUILD__LAST=$(date +%Y%m%d_%H%M%S)
        echo do_build: go build command: post-exec: save TEGOLA_BUILD__LAST: $TEGOLA_BUILD__LAST
        RESULT=$DOBUILD__ERROR_CODE__SUCCEEDED
    else
        echo do_build: go build command: post-exec: failed to build $TEGOLA_BUILD_BIN_PATH - see build output file $build_output for details
        RESULT=$DOBUILD__ERROR_CODE__BUILD_FAILURE
    fi

    echo do_build: all done! result code: $RESULT
    return $RESULT
}
