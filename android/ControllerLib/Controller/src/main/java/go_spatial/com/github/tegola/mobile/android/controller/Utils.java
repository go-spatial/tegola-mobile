package go_spatial.com.github.tegola.mobile.android.controller;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class Utils {
    public static String getProperty(final InputStream f_inputstream_props, final String s_prop_name) throws IOException {
        Properties properties = new Properties();
        properties.load(f_inputstream_props);
        return properties.getProperty(s_prop_name);
    }

    public static class Files {
        private final static String TAG = Utils.Files.class.getName();

        public static class F_GPKG_DIR extends File {
            private static F_GPKG_DIR m_this = null;

            private F_GPKG_DIR(@NonNull final Context context) throws PackageManager.NameNotFoundException {
                super(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir + File.separator + Constants.Strings.GPKG_BUNDLE_SUBDIR);
            }

            public static F_GPKG_DIR getInstance(@NonNull final Context context) throws PackageManager.NameNotFoundException {
                if (m_this == null)
                    m_this = new F_GPKG_DIR(context);
                return m_this;
            }
        }

        public static class CopyResult {
            public int n_size = 0;
            public int n_bytes_read = 0;
            public int n_bytes_wrote = 0;

            public CopyResult() {}

            public CopyResult(final int n_size, final int n_bytes_read, final int n_bytes_wrote) {
                this.n_size = n_size;
                this.n_bytes_read = n_bytes_read;
                this.n_bytes_wrote = n_bytes_wrote;
            }
        }


        public static CopyResult copy(final InputStream inputstream, final OutputStream outputstream) throws IOException {
            final CopyResult copyResult = new CopyResult();
            copyResult.n_size = inputstream != null ? inputstream.available() : 0;
            if (copyResult.n_size > 0) {
                byte[] buf_src_bytes = new byte[1024];
                int n_bytes_read = 0;
                while ((n_bytes_read = inputstream.read(buf_src_bytes)) > 0) {
                    copyResult.n_bytes_read += n_bytes_read;
                    outputstream.write(buf_src_bytes, 0, n_bytes_read);
                    copyResult.n_bytes_wrote += n_bytes_read;
                }
            }
            return copyResult;
        }

        public static CopyResult copy(final File f_src, final File f_dest) throws IOException {
            FileInputStream fis_src = null;
            FileOutputStream fos_dest = null;
            CopyResult copyResult = new CopyResult();
            try {
                fis_src = new FileInputStream(f_src);
                fos_dest = new FileOutputStream(f_dest);
                copyResult = copy(fis_src, fos_dest);
                Log.d(TAG, "copy: wrote " + copyResult.n_bytes_wrote + "/" + copyResult.n_size + " bytes from " + f_src.getCanonicalPath() + " to " + f_dest.getCanonicalPath());
            } catch (IOException e) {
                throw e;
            } finally {
                try {
                    if (fos_dest != null)
                        fos_dest.close();
                    if (fis_src != null)
                        fis_src.close();
                } catch (IOException e) {}
            }
            return copyResult;
        }

        enum TM_APP_FILE_TYPE {
            PRIVATE
            , DATA
            ;
        }
        public static CopyResult copy_raw_res_to_app_file(final Context context, final int raw_res_id, final String s_dest_fname, final TM_APP_FILE_TYPE file_type) throws IOException, PackageManager.NameNotFoundException {
            InputStream inputstream_raw_res = null;
            File f_out = null;
            FileOutputStream fos_app_file = null;
            CopyResult copyResult = new CopyResult();
            try {
                inputstream_raw_res = context.getResources().openRawResource(raw_res_id);
                switch (file_type) {
                    case PRIVATE: {
                        f_out = new File(context.getFilesDir().getPath(), s_dest_fname);
                        fos_app_file = context.openFileOutput(s_dest_fname, Context.MODE_PRIVATE);
                        break;
                    }
                    case DATA: {
                        PackageInfo pkginfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                        f_out = new File(pkginfo.applicationInfo.dataDir, s_dest_fname);
                        fos_app_file = new FileOutputStream(f_out);
                        break;
                    }
                }
                copyResult = copy(inputstream_raw_res, fos_app_file);
                Log.d(TAG, "copy_raw_res_to_app_file: wrote " + copyResult.n_bytes_wrote + "/" + copyResult.n_size + " bytes from raw res (id) " + raw_res_id + " to app " + file_type.name() + " file " + f_out.getCanonicalPath());
            } catch (IOException e) {
                throw e;
            } catch (PackageManager.NameNotFoundException e) {
                throw e;
            } finally {
                try {
                    if (fos_app_file != null)
                        fos_app_file.close();
                    if (inputstream_raw_res != null)
                        inputstream_raw_res.close();
                } catch (IOException e) {}
            }
            return copyResult;
        }
    }

    public static class HTTP {
        private final static String TAG = Utils.HTTP.class.getName();

        public static class HttpUrl_To_Local_File {
            private final HttpUrl url;
            public final HttpUrl get_url() {return url;}

            private final File file;
            public File get_file() {return file;}

            public HttpUrl_To_Local_File(@NonNull final HttpUrl url, @NonNull final File file) {
                this.url = url;
                this.file = file;
            }
        }

        public static class AsyncGetFileTask_RemoteFileInvalidParameterException extends Exception {
            public AsyncGetFileTask_RemoteFileInvalidParameterException(String message) {
                super(message);
            }
        }
        public static class AsyncGetFileTask_RemoteFile_SizeException extends Exception {
            private final HttpUrl m_httpUrl;
            public final HttpUrl get_httpurl() {
                return m_httpUrl;
            }
            public AsyncGetFileTask_RemoteFile_SizeException(final HttpUrl httpUrl) {
                m_httpUrl = httpUrl;
            }
            public AsyncGetFileTask_RemoteFile_SizeException(final HttpUrl httpUrl, final String msg) {
                super(msg);
                m_httpUrl = httpUrl;
            }
        }
        public static class AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileAlreadyExistsException extends IOException {
            private final HttpUrl_To_Local_File m_httpUrl_to_local_file;
            public final HttpUrl_To_Local_File get_httpUrl_to_local_file() {
                return m_httpUrl_to_local_file;
            }
            public AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileAlreadyExistsException(final HttpUrl_To_Local_File httpUrl_to_local_file) {
                m_httpUrl_to_local_file = httpUrl_to_local_file;
            }
            public AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileAlreadyExistsException(final HttpUrl_To_Local_File httpUrl_to_local_file, final String msg) {
                super(msg);
                m_httpUrl_to_local_file = httpUrl_to_local_file;
            }
        }
        public static class AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileCreateException extends IOException {
            private final HttpUrl_To_Local_File m_httpUrl_to_local_file;
            public final HttpUrl_To_Local_File get_httpUrl_to_local_file() {
                return m_httpUrl_to_local_file;
            }
            public AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileCreateException(final HttpUrl_To_Local_File httpUrl_to_local_file) {
                m_httpUrl_to_local_file = httpUrl_to_local_file;
            }
            public AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileCreateException(final HttpUrl_To_Local_File httpUrl_to_local_file, final String msg) {
                super(msg);
                m_httpUrl_to_local_file = httpUrl_to_local_file;
            }
        }
        public static class AsyncGetFileTask_StageHandlerOnChunkRead_GeneralIOException extends IOException {
            private final HttpUrl_To_Local_File m_httpUrl_to_local_file;
            public final HttpUrl_To_Local_File get_httpUrl_to_local_file() {
                return m_httpUrl_to_local_file;
            }
            public AsyncGetFileTask_StageHandlerOnChunkRead_GeneralIOException(final HttpUrl_To_Local_File httpUrl_to_local_file) {
                m_httpUrl_to_local_file = httpUrl_to_local_file;
            }
            public AsyncGetFileTask_StageHandlerOnChunkRead_GeneralIOException(final HttpUrl_To_Local_File httpUrl_to_local_file, final String msg) {
                super(msg);
                m_httpUrl_to_local_file = httpUrl_to_local_file;
            }
        }

        public static abstract class AsyncGetFileTaskStageHandler {
            private final static String TAG = AsyncGetFileTaskStageHandler.class.getName();
            private HttpUrl_To_Local_File m_httpUrl_to_local_file = null;
            private AsyncGetFileTask m_asyncgetfiletask = null;
            private final Object m_asyncgetfiletask_sync_target = new Object();

            public void set_asyncgetfiletask(final AsyncGetFileTask asyncgetfiletask) {
                synchronized (m_asyncgetfiletask_sync_target) {
                    m_asyncgetfiletask =  asyncgetfiletask;
                }
            }
            public AsyncGetFileTask get_asyncgetfiletask() {
                synchronized (m_asyncgetfiletask_sync_target) {
                    return m_asyncgetfiletask;
                }
            }

            private void set_httpUrl_to_local_file(@NonNull final HttpUrl_To_Local_File httpUrl_to_local_file) {
                m_httpUrl_to_local_file = httpUrl_to_local_file;
            }
            public final HttpUrl_To_Local_File get_httpUrl_to_local_file() {
                return m_httpUrl_to_local_file;
            }

            public abstract void onPreExecute();
            public abstract void onChunkRead(Buffer sink, long bytesRead, long contentLength, boolean done) throws IOException;
            public abstract void onCancelled(Exception exception);
            public abstract void onPostExecute(Exception exception);
        }

        public static class AsyncGetFileTask extends AsyncTask <HttpUrl_To_Local_File, Void, Exception> {
            private final static String TAG = AsyncGetFileTask.class.getName();

            private final AsyncGetFileTaskStageHandler m_asyncgetfiletask_stage_handler;

            public AsyncGetFileTask(@NonNull final AsyncGetFileTaskStageHandler asyncgetfiletask_stage_handler) {
                m_asyncgetfiletask_stage_handler = asyncgetfiletask_stage_handler;
                asyncgetfiletask_stage_handler.set_asyncgetfiletask(this);
            }

            private static class ChunkedResponseBody extends ResponseBody {
                private final static String TAG = AsyncGetFileTask.ChunkedResponseBody.class.getSimpleName();
                private final ResponseBody m_responseBody;
                private final AsyncGetFileTaskStageHandler m_asyncgetfiletask_stage_handler;
                private BufferedSource m_bufferedSource;

                ChunkedResponseBody(@NonNull final ResponseBody responseBody, @NonNull final HttpUrl_To_Local_File httpUrl_to_local_file, @NonNull final AsyncGetFileTaskStageHandler asyncgetfiletask_stage_handler) {
                    m_responseBody = responseBody;
                    asyncgetfiletask_stage_handler.set_httpUrl_to_local_file(httpUrl_to_local_file);
                    m_asyncgetfiletask_stage_handler = asyncgetfiletask_stage_handler;
                }

                @Override
                public MediaType contentType() {
                    return m_responseBody.contentType();
                }

                @Override
                public long contentLength() {
                    return m_responseBody.contentLength();
                }

                @Override
                public BufferedSource source() {
                    if (m_bufferedSource == null) {
                        Log.d(TAG, "source: Okio buffering ResponseBody source (contentLength " + contentLength() + ")");
                        m_bufferedSource = Okio.buffer(source(m_responseBody.source()));
                    } else {
//                        Log.d(TAG, "NOT using Okio to buffer source(m_responseBody.source())..");
                    }
                    return m_bufferedSource;
                }

                private Source source(Source source) {
                    final Source source_ret = new ForwardingSource(source) {
                        @Override
                        public long read(Buffer sink, long byteCount) throws IOException {
                            long bytesRead = super.read(sink, byteCount);
                            //Log.d(TAG, "ForwardingSource::read() - read " + bytesRead + " bytes into sink; assert sink.size()==" + bytesRead + " --> " + (sink.size() == bytesRead) + "; calling m_asyncgetfiletask_stage_handler.onChunkRead()...");
                            // read() returns the number of bytes read, or -1 if this source is exhausted.
                            m_asyncgetfiletask_stage_handler.onChunkRead(sink, bytesRead, m_responseBody.contentLength(), bytesRead == -1);
                            return bytesRead;
                        }
                    };
                    Log.d(TAG, "source: returning new ForwardingSource w/ our own read() override");
                    return source_ret;
                }
            }


            @Override
            protected void onPreExecute() {
                m_asyncgetfiletask_stage_handler.onPreExecute();
            }

            private long request_file_size(@NonNull final OkHttpClient httpClient, @NonNull final HttpUrl http_url) throws IOException, AsyncGetFileTask_RemoteFile_SizeException {
                long l_file_size = 0;
                final Request http_request_file_size = new Request.Builder()
                        .url(http_url)
                        .get()
                        .addHeader("Range", "bytes=0-")
                        .build();
                Log.d(TAG, "request_file_size: new Request created: " + http_request_file_size.toString());
                Headers request_headers = http_request_file_size.headers();
                if (request_headers != null && request_headers.size() > 0) {
                    Log.d(TAG, "\tHeaders:");
                    for (int i = 0; i < request_headers.size(); i++) {
                        String s_hdr_name = request_headers.name(i);
                        String s_hdr_val = request_headers.get(s_hdr_name);
                        Log.d(TAG, "\t\t" + s_hdr_name + ": " + s_hdr_val);
                    }
                }
                final Call httpClient_call__request_file_size = httpClient.newCall(http_request_file_size);
                Log.d(TAG, "request_file_size: new OkHttpClient Call (for " + http_request_file_size.toString() + ") created: " + http_request_file_size.toString() + "; executing...");
                Response response = null;
                try {
                    response = httpClient_call__request_file_size.execute();
                    Log.d(TAG, "request_file_size: executed OkHttpClient Call (" + httpClient_call__request_file_size.toString() + "); handling response...");
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected Response (to " + http_request_file_size.toString() + "): " + response.toString());
                    else {
                        Log.d(TAG, "Response (to " + http_request_file_size.toString() + "): " + response.toString());
                        Headers response_headers = response.headers();
                        if (response_headers != null && response_headers.size() > 0) {
                            //DEBUG - remove for release (unnecessary iteration for release builds)
                            Log.d(TAG, "\tHeaders:");
                            for (int i = 0; i < response_headers.size(); i++) {
                                String s_hdr_name = response_headers.name(i);
                                String s_hdr_val = response_headers.get(s_hdr_name);
                                Log.d(TAG, "\t\t" + s_hdr_name + ": " + s_hdr_val);
                            }
                            String s_content_length = response_headers.get("Content-Length");
                            if (s_content_length == null) {
                                throw new AsyncGetFileTask_RemoteFile_SizeException(http_url, "no Content-Length header");
                            }
                            try {
                                s_content_length = s_content_length.trim();
                                Long l_content_length = Long.parseLong(s_content_length);
                                if (l_content_length == null)
                                    throw new AsyncGetFileTask_RemoteFile_SizeException(http_url, "invalid \"Content-Length value\": \"" + s_content_length + "\"");
                                l_file_size = l_content_length.longValue();
                            } catch (NumberFormatException e) {
                                throw new AsyncGetFileTask_RemoteFile_SizeException(http_url, "invalid \"Content-Length value\": \"" + s_content_length + "\"");
                            }
                        }
                    }
                } finally {
                    if (response != null)
                        response.close();
                }
                return l_file_size;
            }

            private void download_file(@NonNull final Response response) throws IOException {
                if (response.body().contentLength() > 0) {
                    Log.d(TAG, "download_file: downloading ChunkedResponseBody (" + response.body().contentLength() + " bytes)...");
                    byte[] bytes = new byte[1024];
                    int n_bytes_read = 0;
                    long n_bytes_remaining = response.body().contentLength();
                    Log.d(TAG, "download_file: entering response.body().source().read() loop...");
                    while (true) {
                        n_bytes_read = response.body().source().read(bytes);
                        if (n_bytes_read != -1) {
                            //Log.d(TAG, "download_file: response.body().source().read() loop: read next " + n_bytes_read + " byte-chunk of " + n_bytes_remaining + " bytes remaining");
                            n_bytes_remaining -= n_bytes_read;
                        } else {
                            //Log.d(TAG, "download_file: response.body().source().read() loop: n_bytes_read==-1 --> breaking out of loop");
                            break;
                        }
                    }
                } else {

                }
            }

            private void request_file_download(@NonNull final OkHttpClient httpClient, @NonNull final HttpUrl http_url, final long l_byte_offset, final long l_byte_length) throws IOException {
                final Request http_request_file_download = new Request.Builder()
                        .url(http_url)
                        .get()
                        .addHeader("Range", "bytes=" + l_byte_offset + "-" + (l_byte_offset + l_byte_length - 1))
                        .build();
                Log.d(TAG, "request_file_download: new Request created: " + http_request_file_download.toString());
                Headers request_headers = http_request_file_download.headers();
                if (request_headers != null && request_headers.size() > 0) {
                    Log.d(TAG, "\tHeaders:");
                    for (int i = 0; i < request_headers.size(); i++) {
                        String s_hdr_name = request_headers.name(i);
                        String s_hdr_val = request_headers.get(s_hdr_name);
                        Log.d(TAG, "\t\t" + s_hdr_name + ": " + s_hdr_val);
                    }
                }
                final Call httpClient_call__request_file_download = httpClient.newCall(http_request_file_download);
                Log.d(TAG, "request_file_download: new OkHttpClient Call (for " + http_request_file_download.toString() + ") created: " + http_request_file_download.toString() + "; executing...");
                Response response = null;
                try {
                    response = httpClient_call__request_file_download.execute();
                    Log.d(TAG, "request_file_download: executed OkHttpClient Call (" + httpClient_call__request_file_download.toString() + "); handling response...");
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected Response (to " + http_request_file_download.toString() + "): " + response.toString());
                    else {
                        Log.d(TAG, "request_file_download: Response (to " + http_request_file_download.toString() + "): " + response.toString());
                        Headers response_headers = response.headers();
                        if (response_headers != null && response_headers.size() > 0) {
                            //DEBUG - remove for release (unnecessary iteration for release builds)
                            Log.d(TAG, "\tHeaders:");
                            for (int i = 0; i < response_headers.size(); i++) {
                                String s_hdr_name = response_headers.name(i);
                                String s_hdr_val = response_headers.get(s_hdr_name);
                                Log.d(TAG, "\t\t" + s_hdr_name + ": " + s_hdr_val);
                            }
                        }
                        Log.d(TAG, "request_file_download: Response (to " + http_request_file_download.toString() + ") body has " + response.body().byteStream().available() + " bytes available");
                    }
                } finally {
                    if (response != null) {
                        Log.d(TAG, "request_file_download: closing response (to " + http_request_file_download.toString() + ")");
                        response.close();
                    } else {
                        Log.d(TAG, "request_file_download: cannot close response (to " + http_request_file_download.toString() + ") as response is null");
                    }
                }
            }
            private void request_file_download(@NonNull final OkHttpClient httpClient, @NonNull final HttpUrl http_url) throws IOException {
                final Request http_request_file_download = new Request.Builder()
                        .url(http_url)
                        .get()
                        .build();
                Log.d(TAG, "request_file_download: new Request created: " + http_request_file_download.toString());
                Headers request_headers = http_request_file_download.headers();
                if (request_headers != null && request_headers.size() > 0) {
                    Log.d(TAG, "\tHeaders:");
                    for (int i = 0; i < request_headers.size(); i++) {
                        String s_hdr_name = request_headers.name(i);
                        String s_hdr_val = request_headers.get(s_hdr_name);
                        Log.d(TAG, "\t\t" + s_hdr_name + ": " + s_hdr_val);
                    }
                }
                final Call httpClient_call__request_file_download = httpClient.newCall(http_request_file_download);
                Log.d(TAG, "request_file_download: new OkHttpClient Call (for " + http_request_file_download.toString() + ") created: " + http_request_file_download.toString() + "; executing...");
                Response response = null;
                try {
                    response = httpClient_call__request_file_download.execute();
                    Log.d(TAG, "request_file_download: executed OkHttpClient Call (" + httpClient_call__request_file_download.toString() + "); handling response...");
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected Response (to " + http_request_file_download.toString() + "): " + response.toString());
                    else {
                        Log.d(TAG, "request_file_download: Response (to " + http_request_file_download.toString() + "): " + response.toString());
                        Headers response_headers = response.headers();
                        if (response_headers != null && response_headers.size() > 0) {
                            //DEBUG - remove for release (unnecessary iteration for release builds)
                            Log.d(TAG, "\tHeaders:");
                            for (int i = 0; i < response_headers.size(); i++) {
                                String s_hdr_name = response_headers.name(i);
                                String s_hdr_val = response_headers.get(s_hdr_name);
                                Log.d(TAG, "\t\t" + s_hdr_name + ": " + s_hdr_val);
                            }
                        }
                        download_file(response);
                    }
                } finally {
                    if (response != null) {
                        Log.d(TAG, "request_file_download: closing response (to " + http_request_file_download.toString() + ")");
                        response.close();
                    } else {
                        Log.d(TAG, "request_file_download: cannot close response (to " + http_request_file_download.toString() + ") as response is null");
                    }
                }
            }

            @Override
            protected Exception doInBackground(final HttpUrl_To_Local_File[] httpUrl_to_local_file) {
                Exception exception = null;

                try {
                    if (httpUrl_to_local_file == null || httpUrl_to_local_file[0] == null)
                        throw new AsyncGetFileTask_RemoteFileInvalidParameterException("HttpUrl_To_Local_File is null");
                    if (httpUrl_to_local_file[0].get_url() == null)
                        throw new AsyncGetFileTask_RemoteFileInvalidParameterException("HttpUrl_To_Local_File.url is null");
                    if (httpUrl_to_local_file[0].get_file() == null)
                        throw new AsyncGetFileTask_RemoteFileInvalidParameterException("HttpUrl_To_Local_File.file is null");
                    final OkHttpClient httpClient = new OkHttpClient.Builder()
                            //network interceptor not currently needed - only application interceptor
//                            .addNetworkInterceptor(new Interceptor() {
//                                @Override public Response intercept(Chain chain) throws IOException {
//                                    Response originalResponse = chain.proceed(chain.request());
//                                    return originalResponse.newBuilder()
//                                            .body(new ChunkedResponseBody(originalResponse.body(), m_httpurl_to_local_file[0], m_asyncgetfiletask_stage_handler))
//                                            .build();
//                                }
//                            })
                            .addInterceptor(new Interceptor() {
                                @Override
                                public Response intercept(Chain chain) throws IOException {
                                    Response originalResponse = chain.proceed(chain.request());
                                    return originalResponse.newBuilder()
                                            .body(new ChunkedResponseBody(originalResponse.body(), httpUrl_to_local_file[0], m_asyncgetfiletask_stage_handler))
                                            .build();
                                }
                            })
                            .build();
                    //Log.d(TAG, "doInBackground: new OkHttpClient created");
                    HttpUrl http_url = httpUrl_to_local_file[0].get_url();
                    request_file_download(httpClient, http_url);
                } catch (IOException e) {
                    exception = e;
                } catch (AsyncGetFileTask_RemoteFileInvalidParameterException e) {
                    exception = e;
                }
                return exception;
            }

            @Override
            protected void onCancelled(Exception exception) {
                m_asyncgetfiletask_stage_handler.onCancelled(exception);
            }

            @Override
            protected void onPostExecute(Exception exception) {
                m_asyncgetfiletask_stage_handler.onPostExecute(exception);
            }
        }

        public static class AsyncGetFileTaskExecuteQueueItem {
            private final AsyncGetFileTaskExecuteQueueItemExecutor m_executor;
            final public AsyncGetFileTaskExecuteQueueItemExecutor get_executor() {
                return m_executor;
            }
            private final HttpUrl_To_Local_File m_httpurl_to_local_file;
            final public HttpUrl_To_Local_File get_httpUrl_to_local_file() {
                return m_httpurl_to_local_file;
            }

            public AsyncGetFileTaskExecuteQueueItem(@NonNull final AsyncGetFileTaskExecuteQueueItemExecutor executor, @NonNull final HttpUrl_To_Local_File httpurl_to_local_file) {
                m_executor = executor;
                m_httpurl_to_local_file = httpurl_to_local_file;
            }
        }
        public static class AsyncGetFileTaskExecuteQueueException extends Exception {
            public AsyncGetFileTaskExecuteQueueException(String message) {
                super(message);
            }
        }
        public static abstract class AsyncGetFileTaskExecuteQueueListener {
            private final LinkedHashMap<AsyncGetFileTaskExecuteQueueItemExecutor, Exception> item_excutor_exception_map = new LinkedHashMap<AsyncGetFileTaskExecuteQueueItemExecutor, Exception>();

            public abstract void onItemExecutor_PostExecute(final AsyncGetFileTaskExecuteQueueItemExecutor executor);
            public abstract void onItemExecutor_Cancelled(final AsyncGetFileTaskExecuteQueueItemExecutor executor);
            public abstract void onCancelled();
            public abstract void onPostExecute(final LinkedHashMap<AsyncGetFileTaskExecuteQueueItemExecutor, Exception> item_excutor_exception_map);
        }
        public static class AsyncGetFileTaskExecuteQueue extends LinkedBlockingQueue<AsyncGetFileTaskExecuteQueueItem> {
            private final AsyncGetFileTaskExecuteQueueListener m_listener;
            private int m_n_pending = 0;

            public AsyncGetFileTaskExecuteQueue() {
                m_listener = null;
                m_n_pending = 0;
            }
            public AsyncGetFileTaskExecuteQueue(final AsyncGetFileTaskExecuteQueueListener listener) {
                m_listener = listener;
                m_n_pending = 0;
            }

            @Override
            public boolean add(AsyncGetFileTaskExecuteQueueItem asyncGetFileTaskExecuteQueueItem) {
                final boolean b_added = super.add(asyncGetFileTaskExecuteQueueItem);
                if (b_added)
                    ++m_n_pending;
                return b_added;
            }

            public void execute() throws AsyncGetFileTaskExecuteQueueException {
                if (isEmpty())
                    throw new AsyncGetFileTaskExecuteQueueException("queue is empty");

                Iterator<AsyncGetFileTaskExecuteQueueItem> iterator_exec_queue_items = iterator();
                while (iterator_exec_queue_items.hasNext()) {
                    AsyncGetFileTaskExecuteQueueItem exec_queue_item = iterator_exec_queue_items.next();
                    exec_queue_item.get_executor().execute(exec_queue_item.get_httpUrl_to_local_file());
                }
            }
        }

        public static class AsyncGetFileTaskExecuteQueueItemExecutor extends AsyncGetFileTask {
            private final static String TAG = AsyncGetFileTaskExecuteQueueItemExecutor.class.getSimpleName();

            private final AsyncGetFileTaskExecuteQueue m_queue;

            public AsyncGetFileTaskExecuteQueueItemExecutor(@NonNull AsyncGetFileTaskStageHandler stage_handler, @NonNull AsyncGetFileTaskExecuteQueue queue) {
                super(stage_handler);
                m_queue = queue;
            }

            @Override
            protected void onCancelled(Exception exception) {
                Log.d(TAG, "onCancelled: " + (exception != null ? exception.getClass().getName() : " no exception") +"; calling super.onCancelled(exception)...");
                super.onCancelled(exception);
                --m_queue.m_n_pending;
                if (m_queue.m_listener != null) {
                    if (exception != null) {
                        Log.d(TAG, "onCancelled: adding " + exception.getClass().getName() + " for this executor to m_queue.m_listener.item_excutor_exception_map...");
                        m_queue.m_listener.item_excutor_exception_map.put(this, exception);
                    }
                    m_queue.m_listener.onItemExecutor_Cancelled(this);
                }
                if (m_queue.m_n_pending == 0) {
                    if (m_queue.m_listener != null) {
                        Log.d(TAG, "onCancelled: calling m_queue.m_listener.onPostExecute() w/ " + (m_queue.m_listener.item_excutor_exception_map.size() > 0 ? "non-" : "") + "empty m_queue.m_listener.item_excutor_exception_map");
                        m_queue.m_listener.onPostExecute(m_queue.m_listener.item_excutor_exception_map.size() > 0 ? m_queue.m_listener.item_excutor_exception_map : null);
                        m_queue.m_listener.item_excutor_exception_map.clear();
                    }
                    m_queue.clear();
                }
            }

            @Override
            protected void onPostExecute(Exception exception) {
                Log.d(TAG, "onPostExecute: " + (exception != null ? exception.getClass().getName() : " no exception") +"; calling super.onPostExecute(exception)...");
                super.onPostExecute(exception);
                --m_queue.m_n_pending;
                if (m_queue.m_listener != null) {
                    if (exception != null) {
                        Log.d(TAG, "onPostExecute: adding " + exception.getClass().getName() + " for this executor to m_queue.m_listener.item_excutor_exception_map...");
                        m_queue.m_listener.item_excutor_exception_map.put(this, exception);
                    }
                    m_queue.m_listener.onItemExecutor_PostExecute(this);
                }
                if (m_queue.m_n_pending == 0) {
                    if (m_queue.m_listener != null) {
                        Log.d(TAG, "onPostExecute: calling m_queue.m_listener.onPostExecute() w/ " + (m_queue.m_listener.item_excutor_exception_map.size() > 0 ? "non-" : "") + "empty m_queue.m_listener.item_excutor_exception_map");
                        m_queue.m_listener.onPostExecute(m_queue.m_listener.item_excutor_exception_map.size() > 0 ? m_queue.m_listener.item_excutor_exception_map : null);
                        m_queue.m_listener.item_excutor_exception_map.clear();
                    }
                    m_queue.clear();
                }
            }
        }
    }
}
