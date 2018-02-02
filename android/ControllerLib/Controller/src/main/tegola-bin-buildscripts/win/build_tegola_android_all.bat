@echo off

REM Usage: build_tegola_android_all <tegola version string>

set TEGOLA_VER_STRING=%1

echo "Running tegola android-platform build for: android/arm"
call clean_tegola.bat -t_platform android-arm
call build_tegola.bat -t_platform android-arm -b_version %TEGOLA_VER_STRING% -b_normalized_fn_bin_output_path %MY_ANDROID_STUDIO_WORKSPACE%\src\github.com\terranodo\tegola-mobile\android\ControllerLib\Controller\src\main\res\raw

echo "Running tegola android-platform build for: android/arm64"
call clean_tegola.bat -t_platform android-arm64
call build_tegola.bat -t_platform android-arm64 -b_version %TEGOLA_VER_STRING% -b_normalized_fn_bin_output_path %MY_ANDROID_STUDIO_WORKSPACE%\src\github.com\terranodo\tegola-mobile\android\ControllerLib\Controller\src\main\res\raw

echo "Running tegola android-platform build for: android/x86"
call clean_tegola.bat -t_platform android-x86
call build_tegola.bat -t_platform android-x86 -b_version %TEGOLA_VER_STRING% -b_normalized_fn_bin_output_path %MY_ANDROID_STUDIO_WORKSPACE%\src\github.com\terranodo\tegola-mobile\android\ControllerLib\Controller\src\main\res\raw

echo "Running tegola android-platform build for: android/x86_64"
call clean_tegola.bat -t_platform android-x86_64
call build_tegola.bat -t_platform android-x86_64 -b_version %TEGOLA_VER_STRING% -b_normalized_fn_bin_output_path %MY_ANDROID_STUDIO_WORKSPACE%\src\github.com\terranodo\tegola-mobile\android\ControllerLib\Controller\src\main\res\raw