@echo off
setlocal

REM Usage: rebuild_tegola_android_all [tegola version string (if excluded, build_tegola.bat implicitly retrieves version string as git describe value)]

set TEGOLA_VER_STRING=%1
set REQUIRED_ARGS=-b_version_props_copy_path %MY_ANDROID_STUDIO_WORKSPACE%\src\github.com\terranodo\tegola-mobile\android\ControllerLib\Controller\src\main\assets -b_normalized_fn_bin_output_path %MY_ANDROID_STUDIO_WORKSPACE%\src\github.com\terranodo\tegola-mobile\android\ControllerLib\Controller\src\main\res\raw
if defined TEGOLA_VER_STRING (
	set COMMON_ARGS=-b_version %TEGOLA_VER_STRING% %REQUIRED_ARGS%
) else (
	set COMMON_ARGS=%REQUIRED_ARGS%
)

set len=4
set t_platform[0]=android-arm
set t_platform[1]=android-arm64
set t_platform[2]=android-x86
set t_platform[3]=android-x86_64

set i=0
:build_loop
if %i% equ %len% goto :eof
for /f "usebackq delims== tokens=2" %%j in (`set t_platform[%i%]`) do (
	echo rebuild_tegola_android_all.bat: Running tegola android-platform build: %%j
	call clean_tegola.bat -t_platform %%j %REQUIRED_ARGS%
	call build_tegola.bat -t_platform %%j %COMMON_ARGS%
    echo.
)
set /a i=%i%+1
goto build_loop