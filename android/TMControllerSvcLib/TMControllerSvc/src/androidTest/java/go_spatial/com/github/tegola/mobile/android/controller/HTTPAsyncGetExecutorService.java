package go_spatial.com.github.tegola.mobile.android.controller;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import go_spatial.com.github.tegola.mobile.android.controller.utils.HTTP;

public class HTTPAsyncGetExecutorService extends ThreadPoolExecutor {
    private final String TAG = HTTPAsyncGetExecutorService.class.getSimpleName();

    public HTTPAsyncGetExecutorService(final RejectedExecutionHandler handler) {
        super(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), handler);
    }

    @Override
    public boolean prestartCoreThread() {
        Log.d(TAG, String.format("prestartCoreThread"));
        return super.prestartCoreThread();
    }

    @Override
    public int prestartAllCoreThreads() {
        Log.d(TAG, String.format("prestartAllCoreThreads"));
        return super.prestartAllCoreThreads();
    }

    @NonNull
    @Override
    public <T> Future<T> submit(@NonNull Callable<T> callable) {
        Log.d(TAG, String.format("submit - %s", callable.getClass().getCanonicalName()));
        if (!(callable instanceof HTTP.AsyncGet.CallableTask))
            throw new ClassCastException(
                String.format(
                    "%s is incompatible with %s - arg to submit() must be type %s",
                    HTTPAsyncGetExecutorService.class.getCanonicalName(),
                    callable.getClass().getCanonicalName(),
                    HTTP.AsyncGet.CallableTask.class.getCanonicalName()
                )
            );
        return super.submit(callable);
    }

    @NonNull
    @Override
    public <T> Future<T> submit(@NonNull Runnable runnable, T t) {
        Log.d(TAG, String.format("submit - %s", runnable.getClass().getCanonicalName()));
        if (!(runnable instanceof HTTP.AsyncGet.RunnableTask))
            throw new ClassCastException(
                String.format(
                    "%s is incompatible with %s - arg to submit() must be type %s",
                    HTTPAsyncGetExecutorService.class.getCanonicalName(),
                    runnable.getClass().getCanonicalName(),
                    HTTP.AsyncGet.RunnableTask.class.getCanonicalName()
                )
            );
        return super.submit(runnable, t);
    }

    @NonNull
    @Override
    public Future<?> submit(@NonNull Runnable runnable) {
        Log.d(TAG, String.format("submit - %s", runnable.getClass().getCanonicalName()));
        if (!(runnable instanceof HTTP.AsyncGet.RunnableTask))
            throw new ClassCastException(
                String.format(
                    "%s is incompatible with %s - arg to submit() must be type %s",
                    HTTPAsyncGetExecutorService.class.getCanonicalName(),
                    runnable.getClass().getCanonicalName(),
                    HTTP.AsyncGet.RunnableTask.class.getCanonicalName()
                )
            );
        return super.submit(runnable);
    }

    @Override
    public void execute(@NonNull Runnable runnable) {
        Log.d(TAG, String.format("execute - %s", runnable.getClass().getCanonicalName()));
        if (!(runnable instanceof FutureTask) && !(runnable instanceof HTTP.AsyncGet.RunnableTask)) {
            throw new ClassCastException(
                String.format(
                    "%s is incompatible with %s - arg to submit() must be type %s",
                    HTTPAsyncGetExecutorService.class.getCanonicalName(),
                    runnable.getClass().getCanonicalName(),
                    HTTP.AsyncGet.RunnableTask.class.getCanonicalName()
                )
            );
        }
        super.execute(runnable);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        Log.d(TAG, String.format("beforeExecute - %s", r.getClass().getCanonicalName()));
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Log.d(TAG, String.format("afterExecute - %s", r.getClass().getCanonicalName()));
        super.afterExecute(r, t);
    }

    @Override
    protected void terminated() {
        Log.d(TAG, String.format("terminated"));
        super.terminated();
    }
}
