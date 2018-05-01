@echo off
setlocal

REM Usage: clean_tegola_android_all <accepts no arguments!>

set REQUIRED_ARGS=-b_version_props_copy_path %MY_ANDROID_STUDIO_WORKSPACE%\src\github.com\terranodo\tegola-mobile\android\ControllerLib\Controller\src\main\assets -b_normalized_fn_bin_output_path %MY_ANDROID_STUDIO_WORKSPACE%\src\github.com\terranodo\tegola-mobile\android\ControllerLib\Controller\src\main\res\raw

set len=4
set t_platform[0]=android-arm
set t_platform[1]=android-arm64
set t_platform[2]=android-x86
set t_platform[3]=android-x86_64

set i=0
:clean_loop
if %i% equ %len% goto :eof
for /f "usebackq delims== tokens=2" %%j in (`set t_platform[%i%]`) do (
	echo Cleaning tegola android-platform build: %%j
	call clean_tegola.bat -t_platform %%j %REQUIRED_ARGS%
	echo.
)
set /a i=%i%+1
goto clean_loop