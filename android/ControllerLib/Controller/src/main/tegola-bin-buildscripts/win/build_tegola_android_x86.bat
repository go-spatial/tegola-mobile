echo off
setlocal
call clean_tegola_android_x86.bat
set arch=x86
echo "Current arch is: %arch%"
set arch_ndk=i686-linux-android
set apilevel=15
echo "Current apilevel is: %apilevel%"
set go_arch=386
echo "Current go_arch is: %go_arch%"
set GOOS=android
set GOARM=7
set GOARCH=%go_arch%
set ANDROID_NDK_CURRENT_TOOLCHAIN=%MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME%\api-%apilevel%\%arch%
echo "Current NDK Toolchain is: %ANDROID_NDK_CURRENT_TOOLCHAIN%"
set CC=%ANDROID_NDK_CURRENT_TOOLCHAIN%\bin\%arch_ndk%-gcc
echo "CC is: %CC%"
set CXX=%ANDROID_NDK_CURRENT_TOOLCHAIN%\bin\%arch_ndk%-g++
echo "CXX is: %CXX%"
set CGO_ENABLED=1
set GO_PKG_DIR=%MY_GOLANG_WORKSPACE%\pkg\gomobile\pkg_android_%arch%
echo "GO_PKG_DIR is: %GO_PKG_DIR%"
set OUTPUT_DIR=%GOPATH%\pkg\github.com\terranodo\tegola\android\api-%apilevel%\%arch%
mkdir %OUTPUT_DIR%
set OUTPUT_BIN=tegola_bin__android_%arch%
set OUTPUT_PATH=%OUTPUT_DIR%\%OUTPUT_BIN%
echo "OUTPUT_PATH is: %OUTPUT_PATH%"
cd %GOPATH%\src\github.com\terranodo\tegola\cmd\tegola\
set go_buiild_cmd=go build -p=1 -pkgdir=%MY_GOLANG_WORKSPACE%\pkg\gomobile\pkg_android_%arch% -tags="" -ldflags="-extldflags=-pie" -o %OUTPUT_PATH% -x -a -v .
set build_output=%OUTPUT_DIR%\build_%OUTPUT_BIN%.out
echo "running go build command: %go_buiild_cmd%"
> "%build_output%" 2>&1 (call %go_buiild_cmd%)
echo "go build: complete - see build output file %build_output% for details"
set DEST=%MY_ANDROID_STUDIO_WORKSPACE%\src\github.com\terranodo\tegola-mobile\android\ControllerLib\Controller\src\main\res\raw\%OUTPUT_BIN%
if exist %OUTPUT_PATH% (
	echo "successfully built tegola android-platform binary %OUTPUT_PATH%"
    echo "final destination is: %DEST%"
    rm %DEST%
    echo "moving %OUTPUT_PATH% to %DEST%..."
    mv %OUTPUT_PATH% %DEST%
    if exist %DEST% (
    	echo "succeeded!"
    ) else (
    	echo "failed!"
    )
) else (
	echo "go build: failed to build %OUTPUT_PATH%"
)
echo "all done!"