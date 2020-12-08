#include <jni.h>
#include <android/log.h>
#include <sys/types.h>
#include <stdio.h>
#include <unistd.h>
#include <pthread.h>


static int pfd[2];
static pthread_t thr;
static const char *LOGTAG = "And.Std.Log";

static void *thread_func(void *dummy)
{
    ssize_t rdsz = 0;
    char buf[128];
    while ((rdsz = read(pfd[0], buf, sizeof(buf) - 1)) > 0)
    {
        if (buf[rdsz - 1] == '\n')
        {
            --rdsz;
        }
        buf[rdsz] = 0;
        __android_log_write(ANDROID_LOG_DEBUG, LOGTAG, buf);
    }
    return 0;
}

jint
Java_com_zoffcc_applications_loggingstdout_LoggingStdout_start_1logging(JNIEnv *env, jobject clazz)
{
    // setvbuf(stdout, 0, _IONBF, 0);
    // setvbuf(stderr, 0, _IONBF, 0);
    //
    setvbuf(stdout, 0, _IOLBF, 0);
    setvbuf(stderr, 0, _IOLBF, 0);

    /* create the pipe and redirect stdout and stderr */
    pipe(pfd);
    dup2(pfd[1], 1);
    dup2(pfd[1], 2);

    /* spawn the logging thread */
    if (pthread_create(&thr, 0, thread_func, 0) != 0)
    {
        return -1;
    }

    pthread_detach(thr);

    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "LOGGING TEST -----------------------------");
    fprintf(stderr, "%s:LOGGING TEST:stderr\n", LOGTAG);
    fprintf(stdout, "%s:LOGGING TEST:stdout\n", LOGTAG);
    __android_log_print(ANDROID_LOG_INFO, LOGTAG, "LOGGING TEST -----------------------------");

    return 0;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    return JNI_VERSION_1_6;
}
