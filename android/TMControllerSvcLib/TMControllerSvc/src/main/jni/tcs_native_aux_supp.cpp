#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <strings.h>
#include <android/log.h>
#include <signal.h>

extern "C" {
	static JavaVM* g_pVM;
	static JNIEnv* g_pEnv;
    static jclass g_jclass_NativeSignalException;

    #define XSTRINGIZE(s) STRINGIZE(s)
    #define STRINGIZE(s) #s

    #define J_PKG__TEGOLA_MOBILE_CONTROLLER "go_spatial/com/github/tegola/mobile/android/controller"
    #define J_CLASS__NATIVE_SIGNAL_EXCEPTION "Exceptions$NativeSignalException"
    #define STR_LITERAL__SEP "/"

    #define J_PKG_CLASS__NATIVE_SIGNAL_EXCEPTION J_PKG__TEGOLA_MOBILE_CONTROLLER STR_LITERAL__SEP J_CLASS__NATIVE_SIGNAL_EXCEPTION

	#define S_SIGABRT "SIGABRT"
    #define S_SIGFPE "SIGFPE"
    #define S_SIGILL "SIGILL"
    #define S_SIGINT "SIGINT"
    #define S_SIGSEGV "SIGSEGV"
    #define S_SIGTERM "SIGTERM"
    #define S_SIG_UNK(s) "UKNOWN_SIGNAL (" XSTRINGIZE(s) ")"

    //forward decl
    static jboolean nativeInit(JNIEnv*, jclass);

    static JNINativeMethod g_native_to_java_method_map[] = {
    	{
    		"nativeInit",
    		"()Z",
    		(void*)nativeInit
    	}
    };

    //java-->native init
    static jboolean nativeInit(JNIEnv* penv, jclass clazz) {
    	//placeholder, empty for now...
    	return JNI_TRUE;
    }


    /*
    	custom signal trap/handiling (see https://en.wikipedia.org/wiki/C_signal_handling) - BEGIN
    */
    static struct sigaction sigaction_old[NSIG];

	//cheesy, but quick way to map sigs to english names for now...
    const char* get_sig_name(const int signal) {
    	switch (signal) {
    		case SIGABRT: return S_SIGABRT;
    		case SIGFPE: return S_SIGFPE;
    		case SIGILL: return S_SIGILL;
    		case SIGINT: return S_SIGINT;
    		case SIGSEGV: return S_SIGSEGV;
    		case SIGTERM: return S_SIGTERM;
    		default: return S_SIG_UNK(signal);
    	}
    }

    void sa_sigaction__throwJavaNativeSignalException(int signal, siginfo_t *info, void *reserved) {
    	const char* sig_name = get_sig_name(signal);
    	__android_log_print(ANDROID_LOG_DEBUG, "tcs_native_aux_supp::sa_sigaction__throwJavaNativeSignalException", "trapped %s(%i)! throwing java NativeSignalException", sig_name, signal);
		g_pEnv->ThrowNew(g_jclass_NativeSignalException, sig_name);
		__android_log_print(ANDROID_LOG_DEBUG, "tcs_native_aux_supp::sa_sigaction__throwJavaNativeSignalException", "turning over signal %s instance to sigaction_old[%i].sa_handler(%i)", sig_name, signal, signal);
        sigaction_old[signal].sa_handler(signal);
    }

    //define which signals we are interested in and then replace default sig handler with our own
    void init_sig_traps() {
    	struct sigaction sigaction__throwJavaNativeSignalException;
		memset(&sigaction__throwJavaNativeSignalException, 0, sizeof(struct sigaction));
		sigaction__throwJavaNativeSignalException.sa_sigaction = sa_sigaction__throwJavaNativeSignalException;
		sigaction__throwJavaNativeSignalException.sa_flags = SA_RESETHAND;

		//now specify which signals in particular we want to trap
		sigaction(SIGFPE, &sigaction__throwJavaNativeSignalException, &sigaction_old[SIGFPE]);
    	__android_log_print(ANDROID_LOG_DEBUG, "tcs_native_aux_supp::init_sig_traps", "added sig trap for SIGFPE(%i)", SIGFPE);
    	sigaction(SIGILL, &sigaction__throwJavaNativeSignalException, &sigaction_old[SIGILL]);
    	__android_log_print(ANDROID_LOG_DEBUG, "tcs_native_aux_supp::init_sig_traps", "added sig trap for SIGILL(%i)", SIGILL);
    	sigaction(SIGSEGV, &sigaction__throwJavaNativeSignalException, &sigaction_old[SIGSEGV]);
    	__android_log_print(ANDROID_LOG_DEBUG, "tcs_native_aux_supp::init_sig_traps", "added sig trap for SIGSEGV(%i)", SIGSEGV);
    	//SIGABRT, SIGINT, and SIGTERM handled as per usual...
    }
    /*
    	custom signal trap/handiling - END
    */


    jint JNI_OnLoad(JavaVM* pVM, void *reserved) {
    	if (pVM == NULL) {
    		__android_log_print(ANDROID_LOG_ERROR, "tcs_native_aux_supp::JNI_OnLoad", "JavaVM is NULL!");
    		return -1;
    	}
    	g_pVM = pVM;

    	if (g_pVM->GetEnv((void**)&g_pEnv, JNI_VERSION_1_6) != JNI_OK || g_pEnv == NULL) {
    		__android_log_print(ANDROID_LOG_ERROR, "tcs_native_aux_supp::JNI_OnLoad", "Could not retrieve JNIEnv");
    		return -2;
    	}
    	__android_log_print(ANDROID_LOG_DEBUG, "tcs_native_aux_supp::JNI_OnLoad", "retrieved JNIEnv");

    	//map g_jclass_NativeSignalException to static java class Exceptions.NativeSignalException
    	jclass jclass_NativeSignalException = g_pEnv->FindClass(J_PKG_CLASS__NATIVE_SIGNAL_EXCEPTION);
    	if (jclass_NativeSignalException == NULL) {
    		__android_log_print(ANDROID_LOG_ERROR, "tcs_native_aux_supp::JNI_OnLoad", "Could not find java class " J_PKG_CLASS__NATIVE_SIGNAL_EXCEPTION);
    		return -3;
    	}
    	g_jclass_NativeSignalException = reinterpret_cast<jclass>(g_pEnv->NewGlobalRef(jclass_NativeSignalException));
    	__android_log_print(ANDROID_LOG_DEBUG, "tcs_native_aux_supp::JNI_OnLoad", "native mapped java class " J_PKG_CLASS__NATIVE_SIGNAL_EXCEPTION);

    	init_sig_traps();
    	__android_log_print(ANDROID_LOG_DEBUG, "tcs_native_aux_supp::JNI_OnLoad", "sig traps initialized");

    	return JNI_VERSION_1_6;
    }
}