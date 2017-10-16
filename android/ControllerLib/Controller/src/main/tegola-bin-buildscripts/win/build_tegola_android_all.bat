@echo off
echo "Running tegola android-platform build for: android/arm"
call build_tegola_android_arm.bat
echo "Running tegola android-platform build for: android/arm64"
call build_tegola_android_arm64.bat
echo "Running tegola android-platform build for: android/x86"
call build_tegola_android_x86.bat
echo "Running tegola android-platform build for: android/x86_64"
call build_tegola_android_x86_64.bat