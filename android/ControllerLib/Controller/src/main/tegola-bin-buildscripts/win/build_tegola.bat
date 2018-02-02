@echo off

REM Usage: build_tegola -t_platform android-arm|android-x86|android-arm64|android-x86_64|win-x86|win-x86_64 [-b_cgo_enabled_override_default 0|1] [-b_version <tegola version string>]
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
REM     -b_normalized_fn_bin_output_path    copy bin output to path - note that this path should not be terminated with "\"

setlocal

::error codes
set ERR__NO_ARGS=-1
set ERR__INVALID_ARG=-2
set ERR__INVALID_TARGET_PLATFORM=-3
set ERR__INVALID_GOOS=-4

:loop--do-while--parse-argument-name-value-pairs
  set argument_name=%1
  if not defined argument_name (
    echo no more arguments to process
    goto :loop--do-while-end---parse-argument-name-value-pairs
  )
  REM echo %%1 is "%1"
  if "%argument_name%"=="" (
    echo exiting loop--do-while-end---parse-argument-name-value-pairs
    goto :loop--do-while-end---parse-argument-name-value-pairs
  )
  REM echo argument name is "%argument_name%"
  set argument_value=%2
  if not defined argument_value (
    echo argument value is undefined! exiting...
    exit /B %ERR__INVALID_ARG%
  )
  REM echo %%2 is "%2"
  if "%argument_value%"=="" (
    echo argument value is undefined! exiting...
    exit /B %ERR__INVALID_ARG%
  ) 
  REM echo argument value is "%argument_value%" 

  echo processing argments name-value pair: "%argument_name% %argument_value%"
  :switch-case--argument
    goto :case--argument-%argument_name% 2>nul
    if errorlevel 1 (
      REM if we are here then there is no matching case... in other words, no such named argument exists
      echo ERROR: invalid argument "%argument_name%"
      goto :eof
    ) 

    :case--argument--t_platform
      :switch-case--t_platform
        goto :case--t_platform--%argument_value% 2>nul
        if errorlevel 1 (
          REM if we are here then there is no matching case... in other words, invalid target platform value
          echo ERROR: invalid target platform "%argument_value%"
          goto :eof
        ) 

        :case--t_platform--android-arm
          set GOOS=android
          set GOARCH=arm
          set GOARM=7
          set arch_friendly=arm
          set ndk_arch=arm-linux-androideabi
          set ndk_apilevel=15
          goto :switch-case-end--t_platform

        :case--t_platform--android-x86
          set GOOS=android
          set GOARCH=386
          set arch_friendly=x86
          set ndk_arch=i686-linux-android
          set ndk_apilevel=15
          goto :switch-case-end--t_platform

        :case--t_platform--android-arm64
          set GOOS=android
          set GOARCH=arm64
          set arch_friendly=arm64
          set ndk_arch=aarch64-linux-android
          set ndk_apilevel=21
          goto :switch-case-end--t_platform

        :case--t_platform--android-x86_64
          set GOOS=android
          set GOARCH=amd64
          set arch_friendly=x86_64
          set ndk_arch=x86_64-linux-android
          set ndk_apilevel=21
          goto :switch-case-end--t_platform

        :case--t_platform--win-x86
          set GOOS=windows
          set GOARCH=386
          set arch_friendly=x86
          goto :switch-case-end--t_platform

        :case--t_platform--win-x86_64
          set GOOS=windows
          set GOARCH=amd64
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

echo target GOOS is: "%GOOS%"
echo target GOARCH is: "%GOARCH%"

:switch-case--GOOS
  goto :case--GOOS--%GOOS% 2>nul 
  if errorlevel 1 (
    REM if we are here then there is no matching case... in other words, invalid GOOS value
    echo ERROR: invalid GOOS "%GOOS%"
    goto :eof
  )

  :case--GOOS--android
    if "%GOARCH%"=="arm" (
      REM validate acceptable GOARM value for arm GOARCH
      echo target GOARM is: "%GOARM%"
    )
    REM android builds require use of gomobile and the android NDK, which means CGO must be enabled - see https://github.com/golang/go/wiki/cgo
    set CGO_ENABLED=1
    echo target android x-compile ndk arch is: "%ndk_arch%"
    echo target android x-compile ndk apilevel is: "%ndk_apilevel%"
    set ANDROID_NDK_CURRENT_TOOLCHAIN=%MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME%\api-%ndk_apilevel%\%arch_friendly%
    echo source android x-compile ndk toolchain is: "%ANDROID_NDK_CURRENT_TOOLCHAIN%"
    set ndk_CC=%ANDROID_NDK_CURRENT_TOOLCHAIN%\bin\%ndk_arch%-gcc
    echo android ndk x-compiler (CC) is: "%ndk_CC%"
    REM ndk x-compile requires "CC" env var
    set CC=%ndk_CC%
    set ndk_CXX=%ANDROID_NDK_CURRENT_TOOLCHAIN%\bin\%ndk_arch%-g++
    echo android ndk x-compiler CXX is: "%ndk_CXX%"
    REM ndk x-compile also requires "CXX" env var
    set CXX=%ndk_CXX%
    set GO_PKG_DIR=%MY_GOLANG_WORKSPACE%\pkg\gomobile\pkg_android_%arch_friendly%
    echo GO_PKG_DIR is: "%GO_PKG_DIR%"
    set OUTPUT_DIR=%GOPATH%\pkg\github.com\terranodo\tegola\android\api-%ndk_apilevel%\%arch_friendly%
    mkdir %OUTPUT_DIR%
    set OUTPUT_BIN=tegola--%TEGOLA_VER_STRING%--android-%arch_friendly%.bin
    set OUTPUT_BIN_NORMALIZED_FN=tegola_bin__android_%arch_friendly%
    set OUTPUT_PATH=%OUTPUT_DIR%\%OUTPUT_BIN%
    echo OUTPUT_PATH is: "%OUTPUT_PATH%"
    set go_build_cmd=go build -p=1 -pkgdir=%MY_GOLANG_WORKSPACE%\pkg\gomobile\pkg_android_%arch_friendly% -tags="" -ldflags="-w -X %GOPATH%\src\github.com\terranodo\tegola\cmd\tegola\cmd.Version=%TEGOLA_VER_STRING% -extldflags=-pie" -o %OUTPUT_PATH% -x -a -v .
    goto :switch-case-end--GOOS

  :case--GOOS--windows
    REM by default, standard tegola windows build-target builds do not use CGO bindings, however some new tegola features may actually require it globally - e.g. geopkg provider, so, althgough we default to disable CGO, we allow it to be turned on via a cmdline arg to this script
    if defined CGO_ENABLED_OVERRIDE_DEFAULT (
      set CGO_ENABLED=%CGO_ENABLED_OVERRIDE_DEFAULT%
    ) else (
      set CGO_ENABLED=0
    )
    set OUTPUT_DIR=%GOPATH%\pkg\github.com\terranodo\tegola\windows\%arch_friendly%
    mkdir %OUTPUT_DIR%
    set OUTPUT_BIN=tegola--%TEGOLA_VER_STRING%--windows-%arch_friendly%.bin
    set OUTPUT_BIN_NORMALIZED_FN=tegola_bin__windows_%arch_friendly%
    set OUTPUT_PATH=%OUTPUT_DIR%\%OUTPUT_BIN%
    echo OUTPUT_PATH is: "%OUTPUT_PATH%"
    set go_build_cmd=go build -p=1 -ldflags="-w -X %GOPATH%\src\github.com\terranodo\tegola\cmd\tegola\cmd.Version=%TEGOLA_VER_STRING%" -o %OUTPUT_PATH% -x -a -v .
    goto :switch-case-end--GOOS
:switch-case-end--GOOS

set build_output=%OUTPUT_DIR%\build_%OUTPUT_BIN%.out
rm %build_output% > nul 2>&1
echo go_build_cmd is: '%go_build_cmd%'
echo CGO_ENABLED: %CGO_ENABLED%
echo running go build command...
cd %GOPATH%\src\github.com\terranodo\tegola\cmd\tegola\
call %go_build_cmd% > "%build_output%" 2>&1
echo go build: complete - see build output file %build_output% for details

if exist %OUTPUT_PATH% (
	echo successfully built tegola binary %OUTPUT_PATH%
	if defined OUTPUT_BIN_NORMALIZED_FN__DIR (
		if exist %OUTPUT_BIN_NORMALIZED_FN__DIR%\ (
			rm %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN% > nul 2>&1
			echo copying %OUTPUT_PATH% to %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN%...
			cp %OUTPUT_PATH% %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN%
			if exist %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN% (
				echo succeeded!
			) else (
				echo failed!
			)
		) else (
			echo cannot copy %OUTPUT_PATH% to %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN% since %OUTPUT_BIN_NORMALIZED_FN__DIR%\ does not exist!
		)
	)
) else (
	echo go build: failed to build %OUTPUT_PATH%
)

echo all done!