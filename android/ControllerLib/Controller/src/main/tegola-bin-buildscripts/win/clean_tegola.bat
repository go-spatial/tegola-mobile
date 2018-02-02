@echo off

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
          set arch_friendly=arm
          set ndk_apilevel=15
          goto :switch-case-end--t_platform

        :case--t_platform--android-x86
          set GOOS=android
          set arch_friendly=x86
          set ndk_apilevel=15
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

    :case--argument--b_cgo_enabled_override_default
      set CGO_ENABLED_OVERRIDE_DEFAULT=%argument_value%
      goto :switch-case-end--argument
  :switch-case-end--argument

  REM advance argument "pointer" to next
  REM echo shifting to next cmd line argument name-value pair
  shift
  shift

  goto :loop--do-while--parse-argument-name-value-pairs
:loop--do-while-end---parse-argument-name-value-pairs

:switch-case--GOOS
  goto :case--GOOS--%GOOS% 2>nul 
  if errorlevel 1 (
    REM if we are here then there is no matching case... in other words, invalid GOOS value
    echo ERROR: invalid GOOS "%GOOS%"
    goto :eof
  )

  :case--GOOS--android
    set OUTPUT_DIR=%GOPATH%\pkg\github.com\terranodo\tegola\android\api-%ndk_apilevel%\%arch_friendly%
    goto :switch-case-end--GOOS

  :case--GOOS--windows
    set OUTPUT_DIR=%GOPATH%\pkg\github.com\terranodo\tegola\windows\%arch_friendly%
    goto :switch-case-end--GOOS
:switch-case-end--GOOS

echo Cleaning OUTPUT_DIR: %OUTPUT_DIR%...
rm -rf %OUTPUT_DIR%
if not exist "%OUTPUT_PATH%" (
	echo %OUTPUT_DIR% cleaned
) else (
	echo Could not clean %OUTPUT_DIR%
)