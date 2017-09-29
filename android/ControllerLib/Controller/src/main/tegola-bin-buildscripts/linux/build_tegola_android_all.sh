#!/usr/bin/env bash
echo "Running tegola android-platform build for: android/arm"
./build_tegola_android_arm.sh
echo "Running tegola android-platform build for: android/arm64"
./build_tegola_android_arm64.sh
echo "Running tegola android-platform build for: android/x86"
./build_tegola_android_x86.sh
echo "Running tegola android-platform build for: android/x86_64"
./build_tegola_android_x86_64.sh