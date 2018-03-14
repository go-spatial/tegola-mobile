@echo off
setlocal

REM Usage: clean_tegola -t_platform android-arm|android-x86|android-arm64|android-x86_64|win-x86|win-x86_64
REM
REM     -t_platform                         target build platform
REM                                           acceptable/supported target build platforms:
REM                                             android-arm: android 32-bit arm processor ABI
REM                                             android-x86: android 32-bit intel processor ABI
REM                                             android-arm64: android 64-bit arm processor ABI
REM                                             android-x86_64: android 64-bit intel processor ABI
REM                                             win-x86: windows 32-bit intel processor
REM                                             win-x86_64: windows 64-bit intel processor
REM     -b_version_props_copy_path    		destination path for copy of version.props file - note that this path should not be terminated with "\"
REM     -b_normalized_fn_bin_output_path    destination path for copy of binary w/ normalized fname - note that this path should not be terminated with "\"

::error codes
set ERR__NO_ARGS=-1
set ERR__INVALID_ARG=-2
set ERR__INVALID_TARGET_PLATFORM=-3
set ERR__INVALID_GOOS=-4

:loop--do-while--parse-argument-name-value-pairs
  set argument_name=%1
  if not defined argument_name (
    echo clean_tegola.bat: no more arguments to process
    goto :loop--do-while-end---parse-argument-name-value-pairs
  )
  REM echo %%1 is "%1"
  if "%argument_name%"=="" (
    echo clean_tegola.bat: exiting loop--do-while-end---parse-argument-name-value-pairs
    goto :loop--do-while-end---parse-argument-name-value-pairs
  )
  REM echo argument name is "%argument_name%"
  set argument_value=%2
  if not defined argument_value (
    echo clean_tegola.bat: argument value is undefined! exiting...
    exit /B %ERR__INVALID_ARG%
  )
  REM echo %%2 is "%2"
  if "%argument_value%"=="" (
    echo clean_tegola.bat: argument value is undefined! exiting...
    exit /B %ERR__INVALID_ARG%
  ) 
  REM echo argument value is "%argument_value%" 

  echo clean_tegola.bat: processing argments name-value pair: "%argument_name% %argument_value%"
  :switch-case--argument
    goto :case--argument-%argument_name% 2>nul
    if errorlevel 1 (
      REM if we are here then there is no matching case... in other words, no such named argument exists
      echo clean_tegola.bat: ERROR: invalid argument "%argument_name%"
      goto :eof
    ) 

    :case--argument--t_platform
      :switch-case--t_platform
        goto :case--t_platform--%argument_value% 2>nul
        if errorlevel 1 (
          REM if we are here then there is no matching case... in other words, invalid target platform value
          echo clean_tegola.bat: ERROR: invalid target platform "%argument_value%"
          goto :eof
        ) 

        :case--t_platform--android-arm
          set GOOS=android
          set arch_friendly=arm
          set ndk_apilevel=16
          goto :switch-case-end--t_platform

        :case--t_platform--android-x86
          set GOOS=android
          set arch_friendly=x86
          set ndk_apilevel=16
          goto :switch-case-end--t_platform

        :case--t_platform--android-arm64
          set GOOS=android
          set arch_friendly=arm64
          set ndk_apilevel=21
          goto :switch-case-end--t_platform

        :case--t_platform--android-x86_64
          set GOOS=android
          set arch_friendly=x86_64
          set ndk_apilevel=21
          goto :switch-case-end--t_platform

        :case--t_platform--win-x86
          set GOOS=windows
          set arch_friendly=x86
          goto :switch-case-end--t_platform

        :case--t_platform--win-x86_64
          set GOOS=windows
          set arch_friendly=x86_64
          goto :switch-case-end--t_platform
      :switch-case-end--t_platform
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

:switch-case--GOOS
  goto :case--GOOS--%GOOS% 2>nul 
  if errorlevel 1 (
    REM if we are here then there is no matching case... in other words, invalid GOOS value
    echo clean_tegola.bat: ERROR: invalid GOOS "%GOOS%"
    goto :eof
  )

  :case--GOOS--android
    set OUTPUT_DIR=%GOPATH%\pkg\%BASE_TEGOLA_SUBDIR%\android\api-%ndk_apilevel%\%arch_friendly%
    set OUTPUT_BIN_NORMALIZED_FN=tegola_bin__android_%arch_friendly%
    goto :switch-case-end--GOOS

  :case--GOOS--windows
    set OUTPUT_DIR=%GOPATH%\pkg\%BASE_TEGOLA_SUBDIR%\windows\%arch_friendly%
    set OUTPUT_BIN_NORMALIZED_FN=tegola_bin__windows_%arch_friendly%
    goto :switch-case-end--GOOS
:switch-case-end--GOOS

set ver_props_fn=version.properties

REM remove version.properties file from VER_PROPS__DIR if it exists...
if defined VER_PROPS__DIR (
	if exist %VER_PROPS__DIR%\ (
		echo clean_tegola.bat: Cleaning %VER_PROPS__DIR%\%ver_props_fn%...
		rm %VER_PROPS__DIR%\%ver_props_fn% > nul 2>&1
	)
)

REM remove binaries with normalized fnames if they exist...
if defined OUTPUT_BIN_NORMALIZED_FN__DIR (
	if exist %OUTPUT_BIN_NORMALIZED_FN__DIR%\ (
		echo clean_tegola.bat: Cleaning %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN%...
		rm %OUTPUT_BIN_NORMALIZED_FN__DIR%\%OUTPUT_BIN_NORMALIZED_FN% > nul 2>&1
	)
)

echo clean_tegola.bat: Cleaning %OUTPUT_DIR%...
rm -rf %OUTPUT_DIR% > nul 2>&1

echo clean_tegola.bat: all done!