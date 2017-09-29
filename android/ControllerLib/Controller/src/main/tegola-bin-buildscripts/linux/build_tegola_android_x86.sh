#!/usr/bin/env bash
./clean_tegola_android_x86.sh
arch=x86
echo "Current arch is: $arch"
arch_ndk=i686-linux-android
apilevel=15
echo "Current apilevel is: $apilevel"
go_arch=386
echo "Current go_arch is: $go_arch"
export GOOS=android
#export GOARM=7
export GOARCH=$go_arch
ANDROID_NDK_CURRENT_TOOLCHAIN=$MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME/api-$apilevel/$arch
echo "Current NDK Toolchain is: $ANDROID_NDK_CURRENT_TOOLCHAIN"
export CC=$ANDROID_NDK_CURRENT_TOOLCHAIN/bin/$arch_ndk-gcc
echo "CC is: $CC"
export CXX=$ANDROID_NDK_CURRENT_TOOLCHAIN/bin/$arch_ndk-g++
echo "CXX is: $CXX"
export CGO_ENABLED=1
GO_PKG_DIR=$MY_GOLANG_WORKSPACE/pkg/gomobile/pkg_android_$arch
echo "GO_PKG_DIR is: $GO_PKG_DIR"
OUTPUT_DIR=$GOPATH/pkg/github.com/terranodo/tegola/android/api-$apilevel/$arch
mkdir -p $OUTPUT_DIR
OUTPUT_BIN=tegola_bin__android_$arch
OUTPUT_PATH=$OUTPUT_DIR/$OUTPUT_BIN
echo "OUTPUT_PATH is: $OUTPUT_PATH"
cd $GOPATH/src/github.com/terranodo/tegola/cmd/tegola/
go build -p=1 -pkgdir=$MY_GOLANG_WORKSPACE/pkg/gomobile/pkg_android_$arch -tags="" -ldflags="-extldflags=-pie" -o $OUTPUT_PATH -x -a -v . 2>&1 | tee $OUTPUT_DIR/build_$OUTPUT_BIN.out
if [ -e $OUTPUT_PATH ]
then
    OLD_PATH=$MY_ANDROID_STUDIO_WORKSPACE/src/github.com/terranodo/tegola-mobile/android/ControllerLib/Controller/src/main/res/raw/$OUTPUT_BIN
    rm $OLD_PATH
    mv $OUTPUT_PATH $OLD_PATH
fi