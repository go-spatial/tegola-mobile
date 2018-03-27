@echo off
setlocal EnableDelayedExpansion

REM Usage: build_tegola -t_platform android-arm|android-x86|android-arm64|android-x86_64|win-x86|win-x86_64 [-b_cgo_enabled_override_default 0|1] [-b_version <tegola version string>] [b_version_props_copy_path <path>] [b_normalized_fn_bin_output_path <path>]
REM
REM     -t_platform                         target build platform
REM                                           acceptable/supported target build platforms:
REM                                             android-arm: android 32-bit arm processor ABI
REM                                             android-x86: android 32-bit intel processor ABI
REM                                             android-arm64: android 64-bit arm processor ABI
REM                                             android-android-x86_64: android 64-bit intel processor ABI
REM                                             win-x86: windows 32-bit intel processor
REM                                             win-x86_64: windows 64-bit intel processor
REM     -b_cgo_enabled_override_default     overrides default value of CGO_ENABLED setting (in script below) for selected target build platform
REM                                           acceptable/supported CGO_ENABLED values
REM                                             0: CGO binding disabled
REM                                             1: CGO binding enabled
REM     -b_version                          version string inserted into tegola binary (also bin filename) - note that the value must not be malformed (no whitespace, escape literal, etc.)
REM     -b_version_props_copy_path    		destination path for copy of version.props file - note that this path should not be terminated with "\"
REM     -b_normalized_fn_bin_output_path    destination path for copy of binary w/ normalized fname - note that this path should not be terminated with "\"

::error codes
set ERR__NO_ARGS=-1
set ERR__INVALID_ARG=-2
set ERR__INVALID_TARGET_PLATFORM=-3
set ERR__INVALID_CGO_ENABLED_OVERRIDE=-4
set ERR__INVALID_GOOS=-5

:loop--do-while--parse-argument-name-value-pairs
  set argument_name=%1
  if not defined argument_name (
    echo build_tegola.bat: no more arguments to process
    goto :loop--do-while-end---parse-argument-name-value-pairs
  )
  REM echo %%1 is "%1"
  if "%argument_name%"=="" (
    echo build_tegola.bat: exiting loop--do-while-end---parse-argument-name-value-pairs
    goto :loop--do-while-end---parse-argument-name-value-pairs
  )
  REM echo argument name is "%argument_name%"
  set argument_value=%2
  if not defined argument_value (
    echo build_tegola.bat: argument value is undefined! exiting...
    exit /B %ERR__INVALID_ARG%
  )
  REM echo %%2 is "%2"
  if "%argument_value%"=="" (
    echo build_tegola.bat: argument value is undefined! exiting...
    exit /B %ERR__INVALID_ARG%
  ) 
  REM echo argument value is "%argument_value%" 

  echo build_tegola.bat: processing argments name-value pair: "%argument_name% %argument_value%"
  :switch-case--argument
    goto :case--argument-%argument_name% 2>nul
    if errorlevel 1 (
      REM if we are here then there is no matching case... in other words, no such named argument exists
      echo build_tegola.bat: ERROR: invalid argument "%argument_name%"
      goto :eof
    ) 

    :case--argument--t_platform
      :switch-case--t_platform
        goto :case--t_platform--%argument_value% 2>nul
        if errorlevel 1 (
          REM if we are here then there is no matching case... in other words, invalid target platform value
          echo build_tegola.bat: ERROR: invalid target platform "%argument_value%"
          goto :eof
        ) 

        :case--t_platform--android-arm
          set GOOS=android
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOOS="%GOOS%"
          set GOARCH=arm
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOARCH="%GOARCH%"
          set GOARM=7
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOARM="%GOARM%"
          set arch_friendly=arm
          set ndk_arch=arm-linux-androideabi
          set ndk_apilevel=15
          goto :switch-case-end--t_platform

        :case--t_platform--android-x86
          set GOOS=android
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOOS="%GOOS%"
          set GOARCH=386
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOARCH="%GOARCH%"
          set arch_friendly=x86
          set ndk_arch=i686-linux-android
          set ndk_apilevel=15
          goto :switch-case-end--t_platform

        :case--t_platform--android-arm64
          set GOOS=android
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOOS="%GOOS%"
          set GOARCH=arm64
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOARCH="%GOARCH%"
          set arch_friendly=arm64
          set ndk_arch=aarch64-linux-android
          set ndk_apilevel=21
          goto :switch-case-end--t_platform

        :case--t_platform--android-x86_64
          set GOOS=android
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOOS="%GOOS%"
          set GOARCH=amd64
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOARCH="%GOARCH%"
          set arch_friendly=x86_64
          set ndk_arch=x86_64-linux-android
          set ndk_apilevel=21
          goto :switch-case-end--t_platform

        :case--t_platform--win-x86
          set GOOS=windows
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOOS="%GOOS%"
          set GOARCH=386
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOARCH="%GOARCH%"
          set arch_friendly=x86
          goto :switch-case-end--t_platform

        :case--t_platform--win-x86_64
          set GOOS=windows
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOOS="%GOOS%"
          set GOARCH=amd64
          echo build_tegola.bat: go build command: pre-exec: go build env: var: GOARCH="%GOARCH%"
          set arch_friendly=x86_64
          goto :switch-case-end--t_platform
      :switch-case-end--t_platform
      goto :switch-case-end--argument

    :case--argument--b_cgo_enabled_override_default
      set CGO_ENABLED_OVERRIDE_DEFAULT=%argument_value%
      goto :switch-case-end--argument

    :case--argument--b_version
      set TEGOLA_VER_STRING=%argument_value%
      goto :switch-case-end--argument

  	:case--argument--b_version_props_copy_path
	  set VER_PROPS__DIR=%argument_value%
	  goto :switch-case-end--argument

    :case--argument--b_normalized_fn_bin_output_path
      set OUTPUT_BIN_NORMALIZED_FN__DIR=%argument_value%
      goto :switch-case-end--argument
  :switch-case-end--argument

  REM advance argument "pointer" to next
  REM echo shifting to next cmd line argument name-value pair
  shift
  shift

  goto :loop--do-while--parse-argument-name-value-pairs
:loop--do-while-end---parse-argument-name-value-pairs

set BASE_TEGOLA_SUBDIR=github.com\go-spatial\tegola
set TEGOLA_SRC_DIR=%GOPATH%\src\%BASE_TEGOLA_SUBDIR%
echo build_tegola.bat: go build command: pre-exec: meta: source dir: %TEGOLA_SRC_DIR%

if not defined TEGOLA_VER_STRING (
	REM build version string in format: "TAG-SHORT_COMMIT_HASH-BRANCH_NAME", e.g. "v0.6.1-436b82e-master"
	cd %TEGOLA_SRC_DIR%
	git describe --tags --always > TEGOLA_VER_STRING__TAG.txt
	set /p TEGOLA_VER_STRING__TAG=<TEGOLA_VER_STRING__TAG.txt
	rm TEGOLA_VER_STRING__TAG.txt > nul 2>&1
	git rev-parse --short HEAD > TEGOLA_VER_STRING__SHORT_HASH.txt
	set /p TEGOLA_VER_STRING__SHORT_HASH=<TEGOLA_VER_STRING__SHORT_HASH.txt
	rm TEGOLA_VER_STRING__SHORT_HASH.txt > nul 2>&1
	git rev-parse --abbrev-ref HEAD > TEGOLA_VER_STRING__BRANCH.txt
	set /p TEGOLA_VER_STRING__BRANCH=<TEGOLA_VER_STRING__BRANCH.txt
	rm TEGOLA_VER_STRING__BRANCH.txt > nul 2>&1
	set TEGOLA_VER_STRING=!TEGOLA_VER_STRING__TAG!_!TEGOLA_VER_STRING__SHORT_HASH!_!TEGOLA_VER_STRING__BRANCH!
)
echo build_tegola.bat: go build command: pre-exec: meta: cmd.Version: %TEGOLA_VER_STRING%

:switch-case--GOOS
  goto :case--GOOS--%GOOS% 2>nul 
  if errorlevel 1 (
    REM if we are here then there is no matching case... in other words, invalid GOOS value
    echo build_tegola.bat: ERROR: invalid GOOS "%GOOS%"
    goto :eof
  )

  :case--GOOS--android
    REM android go builds require cross-compiling via the Android NDK - set cross-compilation options (via env vars below)
	echo build_tegola.bat: go build command: pre-exec: meta: android x-compile: ndk: arch: %ndk_arch%
	echo build_tegola.bat: go build command: pre-exec: meta: android x-compile: ndk: apilevel: %ndk_apilevel%

    set ANDROID_NDK_CURRENT_TOOLCHAIN=%MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME%\api-%ndk_apilevel%\%arch_friendly%
    echo build_tegola.bat: go build command: pre-exec: meta: android x-compile: ndk: toolchain: %ANDROID_NDK_CURRENT_TOOLCHAIN%

    REM ndk x-compile requires "CC" env var
	set CC=%ANDROID_NDK_CURRENT_TOOLCHAIN%\bin\%ndk_arch%-gcc
	echo build_tegola.bat: go build command: pre-exec: go build env: var: CC="%CC%"

    REM ndk x-compile also requires "CXX" env var
	set CXX=%ANDROID_NDK_CURRENT_TOOLCHAIN%\bin\%ndk_arch%-g++
	echo build_tegola.bat: go build command: pre-exec: go build env: var: CXX="%CXX%"

    REM CGO must be enabled for go build to make proper use of Android NDK (since Android NDK is C/C++ source) - see https://github.com/golang/go/wiki/cgo
	set CGO_ENABLED=1
	echo build_tegola.bat: go build command: pre-exec: go build env: var: CGO_ENABLED="%CGO_ENABLED%"

	REM set go build cmd "pkgdir" arg val - android go builds require use of: gomobile package
	set GO_BLD_CMD_ARG_VAL__PKGDIR=%MY_GOLANG_WORKSPACE%\pkg\gomobile\pkg_android_%arch_friendly%
	echo build_tegola.bat: go build command: pre-exec: command string: build: arg: pkgdir: %GO_BLD_CMD_ARG_VAL__PKGDIR%

	REM set go build cmd "ldflags" arg val - set version string; also, android go builds require use of additional "extldflags" arg
	set GO_BLD_CMD_ARG_VAL__LDFLAGS=-w -X %TEGOLA_SRC_DIR%\cmd\tegola\cmd.Version=%TEGOLA_VER_STRING% -extldflags=-pie
	echo build_tegola.bat: go build command: pre-exec: command string: build: arg: ldflags: %GO_BLD_CMD_ARG_VAL__LDFLAGS%

	REM set go build cmd "o" arg val - this specifies output path of go build explicitly
	set OUTPUT_DIR=%MY_GOLANG_WORKSPACE%\pkg\%BASE_TEGOLA_SUBDIR%\android\api-%ndk_apilevel%\%arch_friendly%
	set OUTPUT_BIN=tegola__%TEGOLA_VER_STRING%__android_%arch_friendly%.bin
	set OUTPUT_BIN_NORMALIZED_FN=tegola_bin__android_%arch_friendly%
	set OUTPUT_PATH=%OUTPUT_DIR%\%OUTPUT_BIN%
	set GO_BLD_CMD_ARG_VAL__O=%OUTPUT_PATH%
	echo build_tegola.bat: go build command: pre-exec: command string: build: arg: o (explicit output path): %GO_BLD_CMD_ARG_VAL__O%

    goto :switch-case-end--GOOS

  :case--GOOS--windows
    REM by default, standard tegola windows build-target builds do not use CGO bindings, however some new tegola features may actually require it globally - e.g. geopkg provider, so, althgough we default to disable CGO, we allow it to be turned on via a cmdline arg to this script
	if defined CGO_ENABLED_OVERRIDE_DEFAULT (
		set CGO_ENABLED=%CGO_ENABLED_OVERRIDE_DEFAULT%
	) else (
		set CGO_ENABLED=0
	)
	echo build_tegola.bat: go build command: pre-exec: go build env: var: CGO_ENABLED="%GO_ENABLED%"

	REM set go build cmd "ldflags" arg val - set version string
	set GO_BLD_CMD_ARG_VAL__LDFLAGS=-w -X %TEGOLA_SRC_DIR%\cmd\tegola\cmd.Version=%TEGOLA_VER_STRING%
	echo build_tegola.bat: go build command: pre-exec: command string: build: arg: ldflags: %GO_BLD_CMD_ARG_VAL__LDFLAGS%

	REM set go build cmd "o" arg val - this specifies output path of go build explicitly
	set OUTPUT_DIR=%MY_GOLANG_WORKSPACE%\pkg\%BASE_TEGOLA_SUBDIR%\windows\%arch_friendly%
	set OUTPUT_BIN=tegola__%TEGOLA_VER_STRING%__windows_%arch_friendly%.bin
	set OUTPUT_BIN_NORMALIZED_FN=tegola_bin__windows_%arch_friendly%
	set OUTPUT_PATH=%OUTPUT_DIR%\%OUTPUT_BIN%
	set GO_BLD_CMD_ARG_VAL__O=%OUTPUT_PATH%
	echo build_tegola.bat: go build command: pre-exec: command string: build: arg: o (explicit output path): %GO_BLD_CMD_ARG_VAL__O%

    goto :switch-case-end--GOOS
:switch-case-end--GOOS

REM build go build cmd string
set go_build_cmd=go build -p=8
if defined GO_BLD_CMD_ARG_VAL__PKGDIR (
	set go_build_cmd=%go_build_cmd% -pkgdir="%GO_BLD_CMD_ARG_VAL__PKGDIR%"
)
set go_build_cmd=%go_build_cmd% -tags=""
if defined GO_BLD_CMD_ARG_VAL__LDFLAGS (
	set go_build_cmd=%go_build_cmd% -ldflags="%GO_BLD_CMD_ARG_VAL__LDFLAGS%"
)
if defined GO_BLD_CMD_ARG_VAL__O (
	set go_build_cmd=%go_build_cmd% -o %GO_BLD_CMD_ARG_VAL__O%
)
set go_build_cmd=%go_build_cmd% -x -a -v .
echo build_tegola.bat: go build command: pre-exec: command string: build: final: %go_build_cmd%

REM create OUTPUT_DIR
mkdir %OUTPUT_DIR% > nul 2>&1

rm %OUTPUT_DIR%\go_env.txt > nul 2>&1
go env > %OUTPUT_DIR%\go_env.txt
if exist %OUTPUT_DIR%\go_env.txt (
    echo build_tegola.bat: go build command: pre-exec: go build env vars: save: result: successfully saved to %OUTPUT_DIR%\go_env.txt
) else (
    echo build_tegola.bat: go build command: pre-exec: go build env vars: save: result: FAILED to save to %OUTPUT_DIR%\go_env.txt
)

REM track ver in tegola_version.properties in output dir
set ver_props_fn=tegola_version.properties
rm %OUTPUT_DIR%/$ver_props_fn% > /dev/null 2>&1
@echo TEGOLA_BIN_VER=%TEGOLA_VER_STRING%>%OUTPUT_DIR%\%ver_props_fn%
if exist %OUTPUT_DIR%\%ver_props_fn% (
    echo build_tegola.bat: go build command: pre-exec: ver_props_fn file: create: result: successfully created %OUTPUT_DIR%\%ver_props_fn%
) else (
    echo build_tegola.bat: go build command: pre-exec: ver_props_fn file: create: result: FAILED to create %OUTPUT_DIR%\%ver_props_fn%
)

cd %TEGOLA_SRC_DIR%\cmd\tegola\
set build_output=%OUTPUT_DIR%\build_%OUTPUT_BIN%.txt
echo build_tegola.bat: go build command: exec: running command in %cd%...
call %go_build_cmd% > "%build_output%" 2>&1
echo build_tegola.bat: go build command: exec: complete

if exist %OUTPUT_PATH% (
	echo build_tegola.bat: go build command: post-exec: successfully built tegola binary %OUTPUT_PATH%
	if defined OUTPUT_BIN_NORMALIZED_FN__DIR (
	    if not exist %OUTPUT_BIN_NORMALIZED_FN__DIR%\ (
	        mkdir %OUTPUT_BIN_NORMALIZED_FN__DIR%\
	        if exist %OUTPUT_BIN_NORMALIZED_FN__DIR%\ (
	            echo build_tegola.bat: go build command: post-exec: successfully created %OUTPUT_BIN_NORMALIZED_FN__DIR%\ directory
	        ) else (
	            echo build_tegola.bat: go build command: post-exec: failed to create %OUTPUT_BIN_NORMALIZED_FN__DIR%\ directory
	        )
	    )
		if exist %OUTPUT_BIN_NORMALIZED_FN__DIR%\ (
			rm %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN% > nul 2>&1
			cp %OUTPUT_PATH% %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN%
			if exist %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN% (
				if defined VER_PROPS__DIR (
					if not exist %VER_PROPS__DIR%\ (
					    mkdir %VER_PROPS__DIR%\
					)
					cp %OUTPUT_DIR%\%ver_props_fn% %VER_PROPS__DIR%\%ver_props_fn%
                )
				echo build_tegola.bat: go build command: post-exec: successfully copied %OUTPUT_PATH% to %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN%
			) else (
				echo build_tegola.bat: go build command: post-exec: failed to copy %OUTPUT_PATH% to %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN%
			)
		) else (
			echo build_tegola.bat: go build command: post-exec: failed to copy %OUTPUT_PATH% to %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN% since %OUTPUT_BIN_NORMALIZED_FN__DIR%\ does not exist!
		)
	)
) else (
	echo build_tegola.bat: go build command: post-exec: failed to build %OUTPUT_PATH% - see build output file %build_output% for details
)

echo build_tegola.bat: all done!