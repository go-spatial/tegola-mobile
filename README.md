# Tegola Mobile
Tegola Mobile is a port of tegola (https://github.com/terranodo/tegola) - *shell-based* high performance vector tile server delivering Mapbox Vector Tiles leveraging PostGIS as the data provider - to non-rooted Android devices.

## Background
Since tegola was designed to be a self-contained shell-based executable and *non-rooted* Android devices do not provide a direct means to execute terminal/shell commands or binaries, the tegola shell binary must be executed or launched by the context of an Android app (or service).  Additionally, tegola follows the daemon pattern, meaning it is meant to run continuously on its host until its process is stopped.  Finally, since we wish to avoid straying from tegola's design philosophy of code-once-deploy-arbitrarily, we require that tegola must be launched out-of-process on Android - see below for explanation.

### Tegola for Android - Manual Out-of-Process Shell-Binary Go Build Cross-Compilation with Android Standalone NDK Toolchain vs. Linked/Bound In-Process Library with Gomobile Bind
Quite a heading, eh?  Let's explain...  Normally, functionality meant to be harnessed from a module written in golang, on Android, is meant to be interfaced via gomobile (see https://github.com/golang/mobile) bind.  Gomobile is in a very early stage of development and is not that well documented.  The gist is that the intent of gombile-bind is to provide a means to link libraries written in golang and harness them in-process via compile-time linkage.  

However, to take this approach and utilize gomobile as a build tool, we would have to modify/refactor tegola specifically for Android to include at least a "start" API entry point, not to mention adapt other facets of tegola that currently were coded with the expectation that it would be executed as a self-contained process.  Hence, to avoid the complication of forking and refactoring tegola just for Android, we simply require that tegola must be harnessed and launched out-of-process by the context of an Android foreground service (see https://developer.android.com/guide/components/services.html#Foreground).  

To be able to do this for Android targets, this means that we must *manually* cross-compile tegola shell-executable binaries with custom built Android standalone NDK toolchains (for each target Android API level).  Finally, since tegola is built with golang and we are running on Android platforms, Tegola Mobile compatibility is restricted to those Android platforms on which building golang apps is supported.  

### Golang Supported Android Build platforms
At the time of this writing, the latest version of golang (https://golang.org/dl) is version 1.9 and supports cross-compiling tegola for the following Android platforms

- CPU architecture: arm
	- golang "GOOS": "android"
 	- golang "GOARCH": "arm"
 	- supported Android arm CPU_ABIs: armeabi, armeabi-v7a
 	- supported Android APIs: 15 through 26
- CPU architecture: arm64
 	- golang "GOOS": "android"
 	- golang "GOARCH": "arm64"
 	- supported Android arm CPU_ABIs: arm64-v8a
 	- supported Android APIs: 15 through 26
- CPU architecture: x86
 	- "GOOS": "android"
 	- "GOARCH": "386"
 	- supported Android arm CPU_ABIs: x86
 	- supported Android APIs: 15 through 26
- CPU architecture: x86_64
 	- "GOOS": "android"
 	- "GOARCH": "amd64"
 	- supported Android arm CPU_ABIs: x86_64
 	- supported Android APIs: 15 through 26


## Building Tegola Mobile from Source
##### Summary
Building Tegola Mobile is a two-step process.  First, the Tegola Mobile Android Studio project houses Android-platform-specific binaries of a specific version of the tegola server within raw resources that must be built/precompiled from source.  We intend to build one APK per tegola feature/sourcecode version.

 - Note that when cross-compiling the tegola binary for Android Golang build tools in conjunction with the NDK standlone toolchains, we target the minimum API-level that a given architecture supports for the purpose of backward compatibility.  This is because targeting a higher API target renders the binary incompatible on older platforms.  For example, if we target API-26 (the latest version of Android at the time of this writing) when building the arm version of the tegola binary, the binary will run on arm/API-26 devices but is not guaranteed to do so on devices with arm/API-level<26 devices.  Whereas the inverse is true - i.e. a tegola binary version targeting arm/API-15 will run on the arm/API-15 platform as well as the arm/API-26 platform.

After precompiling tegola Android-platform binaries, all four - for arm/API-15, arm64/API-21, x86/API-15, and x86_64/API-21 - are placed within the raw resources location of the Android Studio project.  Note that we have written buildscripts to take the headache out of this part of the process.  The APK can then be built from Tegola Mobile sources with the Android Studio project.

#### Build-Host Configuration
 1. Download and install latest **JDK** (http://www.oracle.com/technetwork/java/javase/downloads/index.html) for your host environment
 2. Add path to Java `/bin` subdirectory to your PATH environment variable
 3. Download and install the latest version of **Golang** (https://golang.org/dl/)
 4. Set environment variable GOROOT to the root of your Golang installation directory
 5. Add `"$GOROOT/bin"` to your PATH environment variable
 6. Create Golang workspace directory if you have not already done so
 7. Set environment variable MY_GOLANG_WORKSPACE to the root of your Golang workspace directory
 8. Set environment variable GOPATH to `"$MY_GOLANG_WORKSPACE"`
 9. Download and install the latest version of **Android Studio** (https://developer.android.com/studio/index.html#downloads) for your host environment
 10. Create Android Studio workspace directory if you have not already done so
 11. Set environment variable MY_ANDROID_STUDIO_WORKSPACE to the root of your Android Studio workspace directory
 12. Download and install/update **Android SDKs**, **Build Tools**, and **NDK** *via Android Studio*
	  1. Launch Android Studio - note that on Linux environments, if this is the first time launching Android Studio, the launcher will run through first-time environment setup, which takes several minutes to complete while it downloads necessary supporting files
	  2. From Android Studio launcher, select `Configure|SDK Manager`; this will launch the Android Studio SDK Manager in a new Window but will be entitled "Default Settings"
	  3. In the right pane, select the `SDK Tools` tab
	  4. Make sure the following are selected (note - select an item if is not selected or its "Status" column reads "Update Available"): 
		 - `Android SDK Build-Tools`
		 - `LLDB`
		 - `Android SDK Platform-Tools`
		 - `Android SDK Tools`
		 - `NDK`
	  5. Click `Apply`
	  6. If any components need to be installed or updated, Android Studio SDK Manager will notify you module-by-module; click `OK` each time and the respective module will be downloaded/installed - note that installing the NDK usually takes a very long time so don't be alarmed when it does
 13. Set environment variable MY_ANDROID_HOME to the root of your Android Studio installation directory
 14. Set environment variable MY_ANDROID_SDK_HOME to `"$MY_ANDROID_HOME/sdk"` - note: make sure case is correct since on Windows this directory name is in lower case, while on Linux it is "Sdk"
 15. Set environment variable MY_ANDROID_NDK_HOME to `"$MY_ANDROID_HOME/ndk-bundle"`
 16. Add `"$MY_ANDROID_SDK_HOME/tools"` to your PATH environment variable
 17. Add `"$MY_ANDROID_NDK_HOME"` to your PATH environment variable
 18. Install Gomobile binary and cross-compilation dependency files
	 1. Execute shell command: `go get golang.org/x/mobile/cmd/gomobile`
		 - note that this will create gomobile `bin`, `pkg` and `src` subdirectories filestructure and download initial gomobile binaries/dependencies *within the directory that `$MY_GOLANG_WORKSPACE` points to*
	 2. Add path to gomobile binary, `"$MY_GOLANG_WORKSPACE/bin"`, to your PATH environment variable
	 3. Strangely, gomobile expects environment variable ANDROID_HOME to be set to the same directory that `$MY_ANDROID_SDK_HOME` points to, so set environment ANDROID_HOME to `"$MY_ANDROID_SDK_HOME"`
	 3. Execute shell command: `gomobile init`
		 - note that this will create gomobile dependency files necessary for cross-compilation within
			 - `$MY_GOLANG_WORKSPACE/pkg/gomobile/pkg_android_386`
			 - `$MY_GOLANG_WORKSPACE/pkg/gomobile/pkg_android_386`
			 - `$MY_GOLANG_WORKSPACE/pkg/gomobile/pkg_android_386`
			 - and `$MY_GOLANG_WORKSPACE/pkg/gomobile/pkg_android_386`   
 19. Download and install Python
 20. Add path to Python `/bin` subdirectory to your PATH environment variable
 21. Set environment variable MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME to `"$MY_ANDROID_HOME/ndk-standalone-toolchain"`
 22. Build Android NDK Standalone Toolchains
	 - For Android-platform: *arm/API 15* (minimum 32-bit arm)
		 - Execute shell command: `mkdir $MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME/api-15/arm`
		 - Execute shell command:  `python \$MY_ANDROID_NDK_HOME/build/tools/make_standalone_toolchain.py --arch arm --api 15 --deprecated-headers --install-dir $MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME/api-15/arm --force`
	 - For Android-platform: *x86/API 15* (minimum 32-bit x86)
		 - Execute shell command: `mkdir $MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME/api-15/x86`
		 - Execute shell command:  `python \$MY_ANDROID_NDK_HOME/build/tools/make_standalone_toolchain.py --arch x86 --api 15 --deprecated-headers --install-dir $MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME/api-15/x86 --force`
	 - For Android-platform: *arm64/API 21* (minimum 64-bit arm)
		 - Execute shell command: `mkdir $MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME/api-21/arm64`
		 - Execute shell command:  `python \$MY_ANDROID_NDK_HOME/build/tools/make_standalone_toolchain.py --arch arm64 --api 21 --deprecated-headers --install-dir $MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME/api-21/arm64 --force`
	 - For Android-platform: *x86_64/API 21* (minimum 64-bit x86)
		 - Execute shell command: `mkdir $MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME/api-21/x86_64`
		 - Execute shell command:  `python \$MY_ANDROID_NDK_HOME/build/tools/make_standalone_toolchain.py --arch x86_64 --api 21 --deprecated-headers --install-dir $MY_ANDROID_NDK_STANDALONE_TOOLCHAIN_HOME/api-21/x86_64 --force`


#### Build Tegola Android-Platform Target Binaries
Since tegola is written in Golang, we must use Golang build tools in combination with the NDK Standalone Toolchains (see above) to cross-compile the four supported Android-platform tegola binaries.  Note that these steps must be completed each time you want to target a new revision or different feature version of tegola (for Android).

 1. (Only once) Fork the github **tegola-mobile** repo (https://github.com/sacontreras/tegola-mobile, *but you know that already if you are reading this*) for your github account if you have not already done so
 2. (Only once) Create directory `$MY_ANDROID_STUDIO_WORKSPACE/src/github.com/terranodo/tegola-mobile/`
 3. Clone/pull *your* forked **tegola-mobile** repo to `$MY_ANDROID_STUDIO_WORKSPACE/src/github.com/terranodo/tegola-mobile/` to update sources
 4. (Only once) Fork the github **tegola** repo (https://github.com/terranodo/tegola) for your github account if you have not already done so
 5. Switch to the version github branch of **tegola** (to be housed within the APK)
 6. (Only once) Create directory `$GOPATH/src/github.com/terranodo/tegola/`
 7. Clone/pull *your* forked **tegola** version branch repo to `$GOPATH/src/github.com/terranodo/tegola/` to update sources
 8. Cross-compile tegola Android-platform binaries with Golang build tools
	 - Execute the "build all" schell script:
		 - On **Linux** build-hosts: `$MY_ANDROID_STUDIO_WORKSPACE/src/github.com/terranodo/tegola-mobile/android/ControllerLib/Controller/src/main/tegola-bin-buildscripts/linux/buildbuild_tegola_android_all.sh`  
		 - On **Windows** build-hosts: `$MY_ANDROID_STUDIO_WORKSPACE/src/github.com/terranodo/tegola-mobile/android/ControllerLib/Controller/src/main/tegola-bin-buildscripts/win/buildbuild_tegola_android_all.bat`
	 - Provided go-build cross-compilation succeeds, tegola android-platform binaries will be placed within the raw resource directory of the Android Studio project
		 - file location:  `$MY_ANDROID_STUDIO_WORKSPACE/src/github.com/terranodo/tegola-mobile/android/ControllerLib/Controller/src/main/res/raw/`
		 - filenames:
			 - `tegola_bin__android_arm` (for CPU_ABIs: armeabi and armeabi-v7a)
			 - `tegola_bin__android_arm64` (for CPU_ABI: arm64_v8a)
			 - `tegola_bin__android_x86` (for CPU_ABI: x86)
			 - `tegola_bin__android_x86_64` (for CPU_ABI: x86_64)

#### Build Tegola Mobile APK with Android Studio
From Android Studio, select `Build|Build APK` or `Build|Generate Signed APK`
