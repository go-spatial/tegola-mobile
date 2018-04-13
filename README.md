# Tegola Mobile
Tegola Mobile is a port of tegola (https://github.com/terranodo/tegola) - a *shell-based* high performance vector tile server delivering Mapbox Vector Tiles leveraging PostGIS as the data provider - to non-rooted Android devices.

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
 	- supported Android APIs: 16 through 26
- CPU architecture: arm64
 	- golang "GOOS": "android"
 	- golang "GOARCH": "arm64"
 	- supported Android arm CPU_ABIs: arm64-v8a
 	- supported Android APIs: 16 through 26
- CPU architecture: x86
 	- "GOOS": "android"
 	- "GOARCH": "386"
 	- supported Android arm CPU_ABIs: x86
 	- supported Android APIs: 16 through 26
- CPU architecture: x86_64
 	- "GOOS": "android"
 	- "GOARCH": "amd64"
 	- supported Android arm CPU_ABIs: x86_64
 	- supported Android APIs: 16 through 26


## Building Tegola Mobile from Source
##### Summary
Building Tegola Mobile is a two-step process.  First, the Tegola Mobile Android Studio project houses Android-platform-specific binaries of a specific version of the tegola server within raw resources that must be built/precompiled from source.  We intend to build one APK per tegola feature/sourcecode version.

 - Note that when cross-compiling the tegola binary for Android Golang build tools in conjunction with the NDK standlone toolchains, we target the minimum API-level that a given architecture supports for the purpose of backward compatibility.  This is because targeting a higher API target renders the binary incompatible on older platforms.  For example, if we target API-26 (the latest version of Android at the time of this writing) when building the arm version of the tegola binary, the binary will run on arm/API-26 devices but is not guaranteed to do so on devices with arm/API-level<26 devices.  Whereas the inverse is true - i.e. a tegola binary version targeting arm/API-16 will run on the arm/API-16 platform as well as the arm/API-26 platform.

After compiling tegola Android-platform binaries, all four - for arm/API-16, arm64/API-21, x86/API-16, and x86_64/API-21 - are placed within the raw resources location of the Android Studio project.  Note that we have written buildscripts to take the headache out of this part of the process.  The APK can then be built from Tegola Mobile sources with the Android Studio project.

#### Build-Host Configuration
 1. Download and install **JDK 1.8 (aka SE 8) or compatible** (http://www.oracle.com/technetwork/java/javase/downloads/index.html) for your host environment
 2. Add path to Java `/bin` subdirectory to your PATH environment variable
 3. Create Golang workspace directory if you have not already done so
 4. Set environment variable MY_GOLANG_WORKSPACE to the root of your Golang workspace directory
 5. Download and install the latest version of **Android Studio** (https://developer.android.com/studio/index.html#downloads) for your host environment
 6. Create Android Studio workspace directory if you have not already done so
 7. Set environment variable MY_ANDROID_STUDIO_WORKSPACE to the root of your Android Studio workspace directory
 8. Download and install/update **Android SDK Tools**, **Android SDK Build Tools**, and **Android SDK Platform-Tools** via **Android Studio SDK Manager**
	  1. Launch Android Studio - note that on Linux environments, if this is the first time launching Android Studio, the launcher will run through first-time environment setup, which takes several minutes to complete while it downloads necessary supporting files
	  2. From Android Studio launcher, select `Configure|SDK Manager`; this will launch the Android Studio SDK Manager in a new Window but will be entitled "Default Settings"
	  3. In the right pane, select the `SDK Tools` tab
	  4. Make sure the following are selected (note - select an item if is not selected or its "Status" column reads "Update Available"):
		 - `Android SDK Build-Tools`
		 - `Android SDK Platform-Tools`
		 - `Android SDK Tools`
		5. Click `Apply`
	  6. If any components need to be installed or updated, Android Studio SDK Manager will notify you module-by-module; click `OK` each time and the respective module will be downloaded/installed - note that installing the NDK usually takes a very long time so don't be alarmed when it does
 9. Download and install/update **Docker** (https://www.docker.com/get-docker) - note: you will need to create a Docker account if you have not already done so


#### Build Tegola Android-Platform Target Binaries
Since tegola is written in Golang, we must use Golang and Gomobile build tools in combination with custom built NDK Standalone Toolchains to cross-compile the four supported Android-platform tegola binaries.  Note that these steps must be completed each time you want to target a new revision or different feature version of tegola (for Android).

 1. (Only once) Fork the github **tegola-mobile** repo (https://github.com/go-spatial/tegola-mobile, *the repo this readme is in*) for your github account if you have not already done so
 2. (Only once) Create directory `$MY_ANDROID_STUDIO_WORKSPACE/src/github.com/terranodo/tegola-mobile/`
 3. Clone/pull *your* forked **tegola-mobile** repo to `$MY_ANDROID_STUDIO_WORKSPACE/src/github.com/terranodo/tegola-mobile/` to update sources
 4. (Only once) Fork the github **tegola** repo (https://github.com/go-spatial/tegola) for your github account if you have not already done so
 5. (Only once) Create directories:
	 1. `$MY_GOLANG_WORKSPACE/src/github.com/go-spatial/tegola/`
	 2. `$MY_GOLANG_WORKSPACE/pkg/github.com/go-spatial/tegola/`
 5. Clone/pull *your* forked **tegola** version branch repo to `$MY_GOLANG_WORKSPACE/src/github.com/go-spatial/tegola/` to update sources
 6. Check out the branch/version of tegola you want in the APK: `cd ${MY_GOLANG_WORKSPACE/src/github.com/go-spatial/tegola/`, then `git checkout <version>`
 7. Cross-compile tegola Android-platform binaries using the **tegola-android-xcompiler** Docker image
	 1. Start the Docker daemon if you have not already done so
	 2. (Only once) Add directory `$MY_GOLANG_WORKSPACE` to Docker's Shared Volumes (see https://docs.docker.com/storage/volumes/)
	 2. Execute command-line: `docker pull trippiest1/tegola-android-xcompiler` - note that the image is approx 7GB, give or take, so the first time you pull this image from Docker Hub it will take some time... on the other hand, if you have already done so the effect of this command will either update the image if there have been any changes (or do nothing if there have been no changes to the image)
	 3. Execute command-line:
		 - `docker run --name="TM-tegola-bin-compiler" --privileged=true -v  $MY_GOLANG_WORKSPACE/src/github.com/go-spatial/tegola:/usr/golang-workspace/src/github.com/go-spatial/tegola -v $MY_GOLANG_WORKSPACE/pkg/github.com/go-spatial/tegola:/usr/golang-workspace/pkg/github.com/go-spatial/tegola -v $MY_ANDROID_STUDIO_WORKSPACE/src/github.com/terranodo/tegola-mobile/android:/usr/android-studio-workspace/src/github.com/terranodo/tegola-mobile/android -e MY_ANDROID_STUDIO_WORKSPACE=$MY_ANDROID_STUDIO_WORKSPACE -it tegola-android-xcompiler $MY_ANDROID_STUDIO_WORKSPACE/src/github.com/terranodo/tegola-mobile/android/ControllerLib/Controller/src/main/tegola-bin-buildscripts/linux/rebuild_tegola_android_all.sh`
	 4. Provided android cross-compilation succeeds, tegola android-platform binaries will be placed within the raw resource directory of the Android Studio project
		 - file location:  `$MY_ANDROID_STUDIO_WORKSPACE/src/github.com/terranodo/tegola-mobile/android/ControllerLib/Controller/src/main/res/raw/`
		 - filenames:
			 - `tegola_bin__android_arm` (for CPU_ABIs: armeabi and armeabi-v7a)
			 - `tegola_bin__android_arm64` (for CPU_ABI: arm64_v8a)
			 - `tegola_bin__android_x86` (for CPU_ABI: x86)
			 - `tegola_bin__android_x86_64` (for CPU_ABI: x86_64)


#### Build Tegola Mobile APK with Android Studio
You are now ready to build the APK.

1. Start Android Studio
2. Open the **Tegola Mobile** Android Studio project from the Android Studio launcher by clicking `Open an Existing Android Studio project`
3. Navigate to the Tegola Mobile Android Studio project directory, located at `$MY_ANDROID_STUDIO_WORKSPACE/src/github.com/terranodo/tegola-mobile/android/TegolaMobile` and click `OK`
4. From Android Studio, select `Build|Build APK` or `Build|Generate Signed APK` - note that to build a signed APK you will need to generate a keystore and signing certificate if you have not already done so

* Note: if you need to build an updated version, `cd $MY_GOLANG_WORKSPACE/src/github.com/terranodo/tegola-mobile/`, `git pull`, and finally repeat step 8 from section "Build Tegola Android-Platform Target Binaries" above

## Running Tegola Mobile APK
1. First you need to install **Tegola Mobile** on your target device - note that this can be an emulator or a physical Android device
2. If you want to run **Tegola Mobile** within the Android Studio debugger
	1. you will need:
	 	- physical device with USB Debugging enabled, attached via USB
	 	- _OR_, set up and run an instance of an Android Device Emulator - see https://developer.android.com/studio/run/managing-avds.html
	2. From Android Studio, select `Run|Debug Bootstrapper` - note that Android Studio will
		1. build a debug version of the APK
		2. automatically install it, provided you have either an instance AVD running or a USB-debuggable Android device attached to your build-host
		3. automatically launch the main activity - note that currently the Bootstrapper will automatically start TCS (Tegola Controller Service), which will in turn automatically spawn a new child process of tegola mvt server/daemon
3. _OR_, if you do not want to attach the Android Studio debugger, you will need to install the debug version or the signed version of the Tegola Mobile APK via ADB
	1. see https://developer.android.com/studio/command-line/adb.html
	2. you will need to manually launch Tegola Mobile from Android's app drawer by "clicking" the Tegola Mobile launcher icon
