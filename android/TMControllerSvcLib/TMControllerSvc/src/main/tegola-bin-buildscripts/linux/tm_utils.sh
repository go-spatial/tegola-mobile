#!/usr/bin/env bash

#general error codes
ERROR_CODE__SUCCEEDED=0
ERROR_CODE__FAILURE=-1          #eval to 255
AVAILABLE_ERROR_CODE=-2

# Usage: tm_init_bin_to_abi_mapping <no args!>

#error codes
TMIBTAM__ERROR_CODE__TM_CTRLR_SRVC_ROOT_DIR_DIR_DOES_NOT_EXIST=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
TMIBTAM__ERROR_CODE__TM_CREATE_ABI_MAPPING_FAILURE=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
tm_init_bin_to_abi_mapping() {
    echo tm_copy_bin_to_libs: TM_CTRLR_SRVC_SRC_DIR is: TM_CTRLR_SRVC_ROOT_DIR
    if [[ ! -e ${TM_CTRLR_SRVC_ROOT_DIR}/ ]]; then
        echo tm_copy_bin_to_libs: TM_CTRLR_SRVC_ROOT_DIR $TM_CTRLR_SRVC_ROOT_DIR does not exist! exiting tm_copy_bin_to_libs\(\)...
        RESULT=$TMIBTAM__ERROR_CODE__TM_CTRLR_SRVC_ROOT_DIR_DIR_DOES_NOT_EXIST
        return $RESULT
    fi

    export TM_ABI_TEGOLA_VERSION_PROPS_PATH=${TM_CTRLR_SRVC_ROOT_DIR}/tm_abi_tegola_version.properties
    if [[ -e ${TM_ABI_TEGOLA_VERSION_PROPS_PATH} ]]; then
        echo tm_init_bin_to_abi_mapping: existing ${TM_ABI_TEGOLA_VERSION_PROPS_PATH} file: delete: ${TM_ABI_TEGOLA_VERSION_PROPS_PATH}
        rm ${TM_ABI_TEGOLA_VERSION_PROPS_PATH} > /dev/null 2>&1
    fi

    echo tm_init_bin_to_abi_mapping: new ${TM_ABI_TEGOLA_VERSION_PROPS_PATH} file: create: ${TM_ABI_TEGOLA_VERSION_PROPS_PATH}
    touch ${TM_ABI_TEGOLA_VERSION_PROPS_PATH} > /dev/null 2>&1
    if [[ ! -e ${TM_ABI_TEGOLA_VERSION_PROPS_PATH} ]]; then
        echo tm_init_bin_to_abi_mapping: new ${TM_ABI_TEGOLA_VERSION_PROPS_PATH} file: create: result: failed to create empty ${TM_ABI_TEGOLA_VERSION_PROPS_PATH}
        RESULT=$TMIBTAM__ERROR_CODE__TM_CREATE_ABI_MAPPING_FAILURE
        return $RESULT
    else
        echo tm_init_bin_to_abi_mapping: new ${TM_ABI_TEGOLA_VERSION_PROPS_PATH} file: create: result: successfully created empty ${TM_ABI_TEGOLA_VERSION_PROPS_PATH}
    fi
}


#error codes
TMCL__ERROR_CODE__SUCCEEDED=$ERROR_CODE__SUCCEEDED
TMCL__ERROR_CODE__TM_CTRLR_SRVC_LIBS_DIR_DOES_NOT_EXIST=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
tm_clean_libs() {
    TM_CTRLR_SRVC_LIBS_DIR=${TM_CTRLR_SRVC_SRC_MAIN_DIR}/libs
#    echo tm_clean_libs: TM_CTRLR_SRVC_LIBS_DIR is: $TM_CTRLR_SRVC_LIBS_DIR
    if [[ ! -e ${TM_CTRLR_SRVC_LIBS_DIR}/ ]]; then
        echo tm_clean_libs: TM_CTRLR_SRVC_LIBS_DIR $TM_CTRLR_SRVC_LIBS_DIR does not exist! exiting tm_clean_libs\(\)...
        RESULT=$TMCL__ERROR_CODE__TM_CTRLR_SRVC_LIBS_DIR_DOES_NOT_EXIST
        return $RESULT
    fi

    for f1 in ${TM_CTRLR_SRVC_LIBS_DIR}/*; do
        if [[ -d ${f1} ]]; then
            for f2 in $f1/lib_tegola_bin*; do
                if [[ ! -d ${f2} ]] && [[ -e ${f2} ]] ; then
                    echo tm_clean_libs: existing file: delete: ${f2}
                    rm ${f2} > /dev/null 2>&1 > /dev/null 2>&1
                fi
            done
        fi
    done
}


# Usage: tm_copy_bin_to_libs <no args!>

#error codes
TMCBTL__ERROR_CODE__SUCCEEDED=$ERROR_CODE__SUCCEEDED
TMCBTL__ERROR_CODE__GOOS_NOT_ANDROID=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
TMCBTL__ERROR_CODE__TEGOLA_BIN_DOES_NOT_EXIST=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
TMCBTL__ERROR_CODE__TM_CTRLR_SRVC_LIBS_DIR_DOES_NOT_EXIST=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
TMCBTL__ERROR_CODE__TM_CTRLR_SRVC_LIB_ABI_DIR_DOES_NOT_EXIST=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
TMCBTL__ERROR_CODE__UNSUPPORTED_ARCH=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
TMCBTL__ERROR_CODE__COPY_FAILURE=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
TMCBTL__ERROR_CODE__TM_ABI_MAPPING_DOES_NOT_EXIST=$((AVAILABLE_ERROR_CODE=AVAILABLE_ERROR_CODE-1))
tm_copy_bin_to_libs() {
    RESULT=$TMCBTL__ERROR_CODE__COPY_FAILURE

    echo tm_copy_bin_to_libs: GOOS is: $GOOS
    if [[ "${GOOS}" != "android" ]] ; then
        echo tm_copy_bin_to_libs: GOOS is not android! exiting tm_copy_bin_to_libs\(\)...
        RESULT=$TMCBTL__ERROR_CODE__GOOS_NOT_ANDROID
        return $RESULT
    fi

    #reconstruct TEGOLA_BUILD_BIN_PATH from TEGOLA_BUILD_OUTPUT_DIR and TEGOLA_BUILD_BIN (even though we should already have TEGOLA_BUILD_BIN_PATH)
    #   since we want to make use of TEGOLA_BUILD_BIN
    echo tm_copy_bin_to_libs: TEGOLA_BUILD_BIN_PATH is: ${TEGOLA_BUILD_OUTPUT_DIR}/${TEGOLA_BUILD_BIN}
    if [[ ! -e ${TEGOLA_BUILD_OUTPUT_DIR}/${TEGOLA_BUILD_BIN} ]]; then
        echo tm_copy_bin_to_libs: tegola bin ${TEGOLA_BUILD_OUTPUT_DIR}/${TEGOLA_BUILD_BIN} does not exist! exiting tm_copy_bin_to_libs\(\)...
        RESULT=$TMCBTL__ERROR_CODE__TEGOLA_BIN_DOES_NOT_EXIST
        return $RESULT
    fi

    TM_CTRLR_SRVC_LIBS_DIR=${TM_CTRLR_SRVC_SRC_MAIN_DIR}/libs
    echo tm_copy_bin_to_libs: TM_CTRLR_SRVC_LIBS_DIR is: $TM_CTRLR_SRVC_LIBS_DIR
    if [[ ! -e ${TM_CTRLR_SRVC_LIBS_DIR}/ ]]; then
        echo tm_copy_bin_to_libs: TM_CTRLR_SRVC_LIBS_DIR $TM_CTRLR_SRVC_LIBS_DIR does not exist! exiting tm_copy_bin_to_libs\(\)...
        RESULT=$TMCBTL__ERROR_CODE__TM_CTRLR_SRVC_LIBS_DIR_DOES_NOT_EXIST
        return $RESULT
    fi

    echo tm_copy_bin_to_libs: TEGOLA_ARCH_FRIENDLY is: $TEGOLA_ARCH_FRIENDLY
    unset TM_CTRLR_SRVC_ABIS
    case $TEGOLA_ARCH_FRIENDLY in
        arm)
            TM_CTRLR_SRVC_ABIS[0]=armeabi
            TM_CTRLR_SRVC_ABIS[1]=armeabi-v7a
            ;;
        arm64)
            TM_CTRLR_SRVC_ABIS[0]=arm64-v8a
            ;;
        x86)
            TM_CTRLR_SRVC_ABIS[0]=x86
            TM_CTRLR_SRVC_ABIS[1]=x86_64
            ;;
        *)
            echo tm_copy_bin_to_libs: unsupported arch: ${TEGOLA_ARCH_FRIENDLY}. exiting tm_copy_bin_to_libs\(\)...
            RESULT=$TMCBTL__ERROR_CODE__UNSUPPORTED_ARCH
            return $RESULT
            ;;
    esac
    #note the added "lib_" prefix and the ".so" suffix REQUIRED in order to be packaged in apk properly
    TM_TEGOLA_BIN_NORMALIZED_FN=lib_tegola_bin__${GOOS}_${TEGOLA_ARCH_FRIENDLY}.so

    #validate existence of all dirs - we do not want a partial copy!
    for index in ${!TM_CTRLR_SRVC_ABIS[*]} ; do
        echo tm_copy_bin_to_libs: ABI TM_CTRLR_SRVC_ABIS\[$index\] is: ${TM_CTRLR_SRVC_ABIS[$index]}
        LIB_ABI_DIR=${TM_CTRLR_SRVC_LIBS_DIR}/${TM_CTRLR_SRVC_ABIS[$index]}
        if [[ ! -e ${LIB_ABI_DIR}/ ]]; then
            echo tm_copy_bin_to_libs: LIB_ABI_DIR ${LIB_ABI_DIR}/ does not exist! exiting tm_copy_bin_to_libs\(\)...
            RESULT=$TMCBTL__ERROR_CODE__TM_CTRLR_SRVC_LIB_ABI_DIR_DOES_NOT_EXIST
            return $RESULT
        fi
    done

    #now delete existing and copy new
    for index in ${!TM_CTRLR_SRVC_ABIS[*]} ; do
        LIB_ABI_DIR=${TM_CTRLR_SRVC_LIBS_DIR}/${TM_CTRLR_SRVC_ABIS[$index]}
        if [[ -e ${LIB_ABI_DIR}/${TM_TEGOLA_BIN_NORMALIZED_FN} ]]; then
            echo tm_copy_bin_to_libs: existing ${TM_TEGOLA_BIN_NORMALIZED_FN} file: delete: ${LIB_ABI_DIR}/${TM_TEGOLA_BIN_NORMALIZED_FN}
            rm ${LIB_ABI_DIR}/${TM_TEGOLA_BIN_NORMALIZED_FN} > /dev/null 2>&1
        fi
        echo tm_copy_bin_to_libs: ${TEGOLA_BUILD_OUTPUT_DIR}/${TEGOLA_BUILD_BIN}: copy to: ${LIB_ABI_DIR}/${TM_TEGOLA_BIN_NORMALIZED_FN}
        cp ${TEGOLA_BUILD_OUTPUT_DIR}/${TEGOLA_BUILD_BIN} ${LIB_ABI_DIR}/${TM_TEGOLA_BIN_NORMALIZED_FN}
        if [[ -e ${LIB_ABI_DIR}/${TM_TEGOLA_BIN_NORMALIZED_FN} ]]; then
            echo tm_copy_bin_to_libs: ${TEGOLA_BUILD_OUTPUT_DIR}/${TEGOLA_BUILD_BIN}: successfully copied to ${LIB_ABI_DIR}/${TM_TEGOLA_BIN_NORMALIZED_FN}
        else
            echo tm_copy_bin_to_libs: ${TEGOLA_BUILD_OUTPUT_DIR}/${TEGOLA_BUILD_BIN}: failed to copy to ${LIB_ABI_DIR}/${TM_TEGOLA_BIN_NORMALIZED_FN}
            RESULT=$TMCBTL__ERROR_CODE__COPY_FAILURE
            return $RESULT
        fi
    done

    #now update ver props file with mapping for ABIs
    if [[ ! -e ${TM_ABI_TEGOLA_VERSION_PROPS_PATH} ]]; then
        echo tm_copy_bin_to_libs: ${TM_ABI_TEGOLA_VERSION_PROPS_PATH} file: append: ${TM_ABI_TEGOLA_VERSION_PROPS_PATH} does not exist!
        RESULT=$TMCBTL__ERROR_CODE__TM_ABI_MAPPING_DOES_NOT_EXIST
        return $RESULT
    fi
    n_abis=0
    unset TM_ABI_TEGOLA_VERSION_PROPS_LINE
    for index in ${!TM_CTRLR_SRVC_ABIS[*]} ; do
        ABI=${TM_CTRLR_SRVC_ABIS[$index]}
        if [[ n_abis -eq 0 ]]; then
            TM_ABI_TEGOLA_VERSION_PROPS_LINE=${TM_TEGOLA_BIN_NORMALIZED_FN}=
        else
            TM_ABI_TEGOLA_VERSION_PROPS_LINE=${TM_ABI_TEGOLA_VERSION_PROPS_LINE},
        fi
        TM_ABI_TEGOLA_VERSION_PROPS_LINE=${TM_ABI_TEGOLA_VERSION_PROPS_LINE}${ABI}
        n_abis=$((n_abis+1))
    done
    if [[ n_abis -gt 0 ]]; then
        TM_ABI_TEGOLA_VERSION_PROPS_LINE=${TM_ABI_TEGOLA_VERSION_PROPS_LINE}$'\n'
    fi
    echo tm_copy_bin_to_libs: ${TM_ABI_TEGOLA_VERSION_PROPS_PATH} file: append: line: "${TM_ABI_TEGOLA_VERSION_PROPS_LINE}"
    echo ${TM_ABI_TEGOLA_VERSION_PROPS_LINE}>>${TM_ABI_TEGOLA_VERSION_PROPS_PATH}
}