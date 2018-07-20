package go_spatial.com.github.tegola.mobile.android.controller;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HTTPAsyncGetExecutorService extends ThreadPoolExecutor {
    private final String TAG = HTTPAsyncGetExecutorService.class.getSimpleName();

    public HTTPAsyncGetExecutorService(final RejectedExecutionHandler handler) {
        super(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), handler);
    }

    @NonNull
    @Override
    public <T> Future<T> submit(@NonNull Callable<T> callable) {
        return super.submit(callable);
    }

    @NonNull
    @Override
    public <T> Future<T> submit(@NonNull Runnable runnable, T t) {
        return super.submit(runnable, t);
    }

    @NonNull
    @Override
    public Future<?> submit(@NonNull Runnable runnable) {
        return super.submit(runnable);
    }

    @Override
    public void execute(@NonNull Runnable runnable) {
        super.execute(runnable);
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

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        Log.d(TAG, String.format("beforeExecute"));
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Log.d(TAG, String.format("afterExecute"));
        super.afterExecute(r, t);
    }

    @Override
    protected void terminated() {
        Log.d(TAG, String.format("terminated"));
        super.terminated();
    }
}
