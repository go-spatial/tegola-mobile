package go_spatial.com.github.tegola.mobile.android.controller;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Patterns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Dispatcher;
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
    public static String getProperty(final File f_props, final String s_prop_name) throws IOException {
        InputStream f_inputstream_props = null;
        try {
            Properties properties = new Properties();
            f_inputstream_props = new FileInputStream(f_props);
            properties.load(f_inputstream_props);
            return properties.getProperty(s_prop_name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (f_inputstream_props != null) {
                try {
                    f_inputstream_props.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    public static String getAssetProperty(final Context context, final String s_props_filename, final String s_prop_name) {
        InputStream f_inputstream_props = null;
        try {
            Properties properties = new Properties();
            f_inputstream_props = context.getAssets().open(s_props_filename);
            properties.load(f_inputstream_props);
            return properties.getProperty(s_prop_name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (f_inputstream_props != null) {
                try {
                    f_inputstream_props.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static class Files {
        private final static String TAG = Utils.Files.class.getCanonicalName();

        public static class F_PUBLIC_ROOT_DIR extends File {
            private static F_PUBLIC_ROOT_DIR m_this = null;
            private F_PUBLIC_ROOT_DIR(@NonNull final Context context) {
                super(context.getExternalCacheDir().getPath());
            }
            public static F_PUBLIC_ROOT_DIR getInstance(@NonNull final Context context) throws PackageManager.NameNotFoundException {
                if (m_this == null)
                    m_this = new F_PUBLIC_ROOT_DIR(context);
                return m_this;
            }
        }

        public static class F_LIBS_DIR extends File {
            private static F_LIBS_DIR m_this = null;
            private F_LIBS_DIR(@NonNull final Context context) throws PackageManager.NameNotFoundException, IOException {
                super(
                    android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
                        ? context.getApplicationInfo().nativeLibraryDir
                        : context.getApplicationInfo().dataDir + "/lib"
                );
            }
            public static F_LIBS_DIR getInstance(@NonNull final Context context) throws PackageManager.NameNotFoundException, IOException {
                if (m_this == null)
                    m_this = new F_LIBS_DIR(context);
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

        //in bytes
        public static long folderSize(File directory) {
            long length = 0;
            for (File file : directory.listFiles()) {
                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
            }
            return length;
        }

        private static void delete(File f, int n_level) throws IOException {
            if (f == null)
                throw new NullPointerException("File object is null!");
            if (!f.exists())
                throw new FileNotFoundException(f.getPath() + " does not exist!");
            StringBuilder sb_lvl = new StringBuilder();
            for (int i = 0; i < n_level; i++)
                sb_lvl.append("\t");
            if (!f.isFile()) {
                File[] f_children = f.listFiles();
                Log.d(TAG, "delete: " + sb_lvl + " directory \"" + f.getPath() + "\" contains " + f_children.length + " children");
                for (File f_child : f_children)
                    delete(f_child, n_level + 1);

            }
            boolean b_is_file = f.isFile();
            boolean b_deleted = f.delete();
            Log.d(TAG, "delete: " + sb_lvl + " "
                    + (b_deleted ? "successfully deleted" : "FAILED TO DELETE")
                    + " " + (b_is_file ? "file" : "directory")
                    + " \"" + f.getPath() + "\"");
        }
        public static void delete(File f) throws IOException {
            delete(f, 0);
        }
    }

    public static class HTTP {
        private final static String TAG = Utils.HTTP.class.getCanonicalName();

        public static boolean isValidUrl(String url) {
            Pattern p = Patterns.WEB_URL;
            Matcher m = p.matcher(url.toLowerCase());
            return m.matches();
        }

        public static class Get {
            private final static String TAG = Utils.HTTP.Get.class.getCanonicalName();

            public interface ContentHandler {
                void onStartRead(long n_size);
                void onChunkRead(int n_bytes_read, byte[] bytes_1kb_chunk);
                void onReadError(long n_remaining, Exception e);
                void onReadComplete(long n_read, long n_remaining);
            }

            private static Dispatcher getDispatcher() {
                Dispatcher dispatcher = new Dispatcher();
                dispatcher.setMaxRequestsPerHost(20);
                return dispatcher;
            }

            public static void exec(final String s_url, final ContentHandler content_handler) {
                if (s_url == null)
                    throw new NullPointerException("s_url cannot be null");
                final OkHttpClient httpClient = new OkHttpClient.Builder().dispatcher(getDispatcher())
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build();
                final Request http_get_request = new Request.Builder()
                        .url(s_url)
                        .get()
                        .build();
                Log.d(TAG, "exec: new Request created: " + http_get_request.toString());
                Headers request_headers = http_get_request.headers();
                if (request_headers != null && request_headers.size() > 0) {
                    Log.d(TAG, "\tHeaders:");
                    for (int i = 0; i < request_headers.size(); i++) {
                        String s_hdr_name = request_headers.name(i);
                        String s_hdr_val = request_headers.get(s_hdr_name);
                        Log.d(TAG, "\t\t" + s_hdr_name + ": " + s_hdr_val);
                    }
                }
                final Call httpClient_call__get_request = httpClient.newCall(http_get_request);
                Log.d(TAG, "exec: new OkHttpClient Call (for " + http_get_request.toString() + ") created: " + httpClient_call__get_request.toString() + "; executing...");
                Response htt_get_response = null;
                long n_content_length = 0, n_bytes_remaining = 0;
                try {
                    htt_get_response = httpClient_call__get_request.execute();
                    Log.d(TAG, "exec: executed OkHttpClient Call (" + httpClient_call__get_request.toString() + "); handling response...");
                    if (!htt_get_response.isSuccessful())
                        throw new IOException("Unexpected Response (to " + http_get_request.toString() + "): " + htt_get_response.toString());
                    else {
                        Log.d(TAG, "exec: Response (to " + http_get_request.toString() + "): " + htt_get_response.toString());
                        Headers response_headers = htt_get_response.headers();
                        if (response_headers != null && response_headers.size() > 0) {
                            //DEBUG - remove for release (unnecessary iteration for release builds)
                            Log.d(TAG, "\tHeaders:");
                            for (int i = 0; i < response_headers.size(); i++) {
                                String s_hdr_name = response_headers.name(i);
                                String s_hdr_val = response_headers.get(s_hdr_name);
                                Log.d(TAG, "\t\t" + s_hdr_name + ": " + s_hdr_val);
                            }
                        }

                        n_content_length = n_bytes_remaining = htt_get_response.body().contentLength();
                        content_handler.onStartRead(n_bytes_remaining);
                        if (n_content_length < 0) {
                            Log.w(TAG, "exec: response headers do not appear to contain Content-Length!");
                            n_bytes_remaining = 0;
                        } else {
                            Log.d(TAG, "exec: reading ResponseBody (" + n_content_length + " bytes)...");
                        }

                        byte[] bytes = new byte[8192];
                        int n_bytes_read = 0;
                        Log.d(TAG, "exec: entering response.body().source().read() loop...");
                        while ((n_bytes_read = htt_get_response.body().source().read(bytes)) != -1) {
                            content_handler.onChunkRead(n_bytes_read, bytes);
                            n_bytes_remaining -= n_bytes_read;
                        }

                        content_handler.onReadComplete(
                                n_content_length < 0
                                        ? -n_bytes_remaining
                                        : n_content_length - n_bytes_remaining
                                , n_content_length < 0
                                        ? 0
                                        : n_bytes_remaining);
                    }
                } catch (Exception e) {
                    content_handler.onReadError(
                            n_content_length < 0
                                ? 0
                                : n_bytes_remaining
                            , e);
                } finally {
                    if (httpClient_call__get_request != null)
                        httpClient_call__get_request.cancel();
                    if (htt_get_response != null) {
                        Log.d(TAG, "exec: closing response (to " + http_get_request.toString() + ")");
                        htt_get_response.body().close();
                        htt_get_response.close();
                    } else {
                        Log.d(TAG, "exec: cannot close response (to " + http_get_request.toString() + ") as response is null");
                    }
                    if (httpClient != null) {
                        Log.d(TAG, "exec: httpClient.dispatcher().cancelAll()");
                        httpClient.dispatcher().cancelAll();
                    }
                }
            }
        }

        public static class AsyncGet {
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

            public static class RemoteFileInvalidParameterException extends Exception {
                public RemoteFileInvalidParameterException(String message) {
                    super(message);
                }
            }

            public static class RemoteFile_SizeException extends Exception {
                private final HttpUrl m_httpUrl;
                public final HttpUrl get_httpurl() {
                    return m_httpUrl;
                }
                public RemoteFile_SizeException(final HttpUrl httpUrl) {
                    m_httpUrl = httpUrl;
                }
                public RemoteFile_SizeException(final HttpUrl httpUrl, final String msg) {
                    super(msg);
                    m_httpUrl = httpUrl;
                }
            }

            public static class StageHandlerOnChunkRead_LocalFileAlreadyExistsException extends IOException {
                private final HttpUrl_To_Local_File m_httpUrl_to_local_file;
                public final HttpUrl_To_Local_File get_httpUrl_to_local_file() {
                    return m_httpUrl_to_local_file;
                }
                public StageHandlerOnChunkRead_LocalFileAlreadyExistsException(final HttpUrl_To_Local_File httpUrl_to_local_file) {
                    m_httpUrl_to_local_file = httpUrl_to_local_file;
                }
                public StageHandlerOnChunkRead_LocalFileAlreadyExistsException(final HttpUrl_To_Local_File httpUrl_to_local_file, final String msg) {
                    super(msg);
                    m_httpUrl_to_local_file = httpUrl_to_local_file;
                }
            }

            public static class StageHandlerOnChunkRead_LocalFileCreateException extends IOException {
                private final HttpUrl_To_Local_File m_httpUrl_to_local_file;
                public final HttpUrl_To_Local_File get_httpUrl_to_local_file() {
                    return m_httpUrl_to_local_file;
                }
                public StageHandlerOnChunkRead_LocalFileCreateException(final HttpUrl_To_Local_File httpUrl_to_local_file) {
                    m_httpUrl_to_local_file = httpUrl_to_local_file;
                }
                public StageHandlerOnChunkRead_LocalFileCreateException(final HttpUrl_To_Local_File httpUrl_to_local_file, final String msg) {
                    super(msg);
                    m_httpUrl_to_local_file = httpUrl_to_local_file;
                }
            }

            public static class StageHandlerOnChunkRead_GeneralIOException extends IOException {
                private final HttpUrl_To_Local_File m_httpUrl_to_local_file;
                public final HttpUrl_To_Local_File get_httpUrl_to_local_file() {
                    return m_httpUrl_to_local_file;
                }
                public StageHandlerOnChunkRead_GeneralIOException(final HttpUrl_To_Local_File httpUrl_to_local_file) {
                    m_httpUrl_to_local_file = httpUrl_to_local_file;
                }
                public StageHandlerOnChunkRead_GeneralIOException(final HttpUrl_To_Local_File httpUrl_to_local_file, final String msg) {
                    super(msg);
                    m_httpUrl_to_local_file = httpUrl_to_local_file;
                }
            }

            public static abstract class TaskStageHandler {
                private final static String TAG = "Utils.HTTP.AsyncGet" + TaskStageHandler.class.getSimpleName();

                private HttpUrl_To_Local_File m_httpUrl_to_local_file = null;
                private Task m_asyncgetfiletask = null;
                private final Object m_asyncgetfiletask_sync_target = new Object();

                public void set_asyncgetfiletask(final Task asyncgetfiletask) {
                    synchronized (m_asyncgetfiletask_sync_target) {
                        m_asyncgetfiletask =  asyncgetfiletask;
                    }
                }
                public Task get_asyncgetfiletask() {
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

            public static class Task extends AsyncTask <HttpUrl_To_Local_File, Void, Exception> {
                private final static String TAG = "Utils.HTTP.AsyncGet" + Task.class.getSimpleName();

                private final TaskStageHandler m_asyncgetfiletask_stage_handler;

                private Call call__http_get = null;
                public Call get_http_get_call() {return call__http_get;}

                public Task(@NonNull final TaskStageHandler asyncgetfiletask_stage_handler) {
                    m_asyncgetfiletask_stage_handler = asyncgetfiletask_stage_handler;
                    asyncgetfiletask_stage_handler.set_asyncgetfiletask(this);
                }

                private static class ChunkedResponseBody extends ResponseBody {
                    private final static String TAG = "Utils.HTTP.AsyncGet" + ChunkedResponseBody.class.getSimpleName();

                    private final ResponseBody m_responseBody;
                    private final TaskStageHandler m_asyncgetfiletask_stage_handler;
                    private BufferedSource m_bufferedSource;

                    ChunkedResponseBody(@NonNull final ResponseBody responseBody, @NonNull final HttpUrl_To_Local_File httpUrl_to_local_file, @NonNull final TaskStageHandler asyncgetfiletask_stage_handler) {
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
                                long bytesRead = super.read(sink, byteCount);   // read() returns the number of bytes read, or -1 if this source is exhausted.
                                //Log.d(TAG, "ForwardingSource::read() - read " + bytesRead + " bytes into sink; assert sink.size()==" + bytesRead + " --> " + (sink.size() == bytesRead) + "; calling m_asyncgetfiletask_stage_handler.onChunkRead()...");
                                m_asyncgetfiletask_stage_handler.onChunkRead(sink, bytesRead, m_responseBody.contentLength(), bytesRead == -1);
                                sink.flush();
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

                private long request_file_size(@NonNull final OkHttpClient httpClient, @NonNull final HttpUrl http_url) throws IOException, RemoteFile_SizeException {
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
                    call__http_get = httpClient.newCall(http_request_file_size);
                    Log.d(TAG, "request_file_size: new OkHttpClient Call (for " + http_request_file_size.toString() + ") created: " + http_request_file_size.toString() + "; executing...");
                    Response response = null;
                    try {
                        response = call__http_get.execute();
                        Log.d(TAG, "request_file_size: executed OkHttpClient Call (" + call__http_get.toString() + "); handling response...");
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
                                    throw new RemoteFile_SizeException(http_url, "no Content-Length header");
                                }
                                try {
                                    s_content_length = s_content_length.trim();
                                    Long l_content_length = Long.parseLong(s_content_length);
                                    if (l_content_length == null)
                                        throw new RemoteFile_SizeException(http_url, "invalid \"Content-Length value\": \"" + s_content_length + "\"");
                                    l_file_size = l_content_length.longValue();
                                } catch (NumberFormatException e) {
                                    throw new RemoteFile_SizeException(http_url, "invalid \"Content-Length value\": \"" + s_content_length + "\"");
                                }
                            }
                        }
                    } finally {
                        if (call__http_get != null)
                            call__http_get.cancel();
                        if (response != null) {
                            response.body().source().buffer().close();
                            response.body().close();
                            response.close();
                        }
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
                                if (n_bytes_remaining > 0) {
                                    Log.e(TAG, "download_file: response.body().source().read() loop: n_bytes_read==-1 --> failed to read, n_bytes_remaining==" + n_bytes_remaining);
                                } else {
                                    Log.d(TAG, "download_file: response.body().source().read() loop: n_bytes_read==-1 --> breaking out of loop; n_bytes_remaining==0");
                                }
                                break;
                            }
                        }
                    } else {
                        Log.e(TAG, "download_file: response body contains no content (zero bytes)!");
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
                    call__http_get = httpClient.newCall(http_request_file_download);
                    Log.d(TAG, "request_file_download: new OkHttpClient Call (for " + http_request_file_download.toString() + ") created: " + http_request_file_download.toString() + "; executing...");
                    Response response = null;
                    try {
                        response = call__http_get.execute();
                        Log.d(TAG, "request_file_download: executed OkHttpClient Call (" + call__http_get.toString() + "); handling response...");
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
                        if (call__http_get != null)
                            call__http_get.cancel();
                        if (response != null) {
                            Log.d(TAG, "request_file_download: closing response (to " + http_request_file_download.toString() + ")");
                            response.body().source().buffer().close();
                            response.body().close();
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
                    call__http_get = httpClient.newCall(http_request_file_download);
                    Log.d(TAG, "request_file_download: new OkHttpClient Call (for " + http_request_file_download.toString() + ") created: " + http_request_file_download.toString() + "; executing...");
                    Response response = null;
                    try {
                        response = call__http_get.execute();
                        Log.d(TAG, "request_file_download: executed OkHttpClient Call (" + call__http_get.toString() + "); handling response...");
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
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw e;
                    }
                    finally {
                        if (call__http_get != null)
                            call__http_get.cancel();
                        if (response != null) {
                            Log.d(TAG, "request_file_download: closing response (to " + http_request_file_download.toString() + ")");
                            response.body().source().buffer().close();
                            response.body().close();
                            response.close();
                        } else {
                            Log.d(TAG, "request_file_download: cannot close response (to " + http_request_file_download.toString() + ") as response is null");
                        }
                    }
                }

                private static Dispatcher getDispatcher() {
                    Dispatcher dispatcher = new Dispatcher();
                    dispatcher.setMaxRequestsPerHost(20);
                    return dispatcher;
                }

                @Override
                protected Exception doInBackground(final HttpUrl_To_Local_File[] httpUrl_to_local_file) {
                    Exception exception = null;

                    OkHttpClient httpClient = null;
                    try {
                        if (httpUrl_to_local_file == null || httpUrl_to_local_file[0] == null)
                            throw new RemoteFileInvalidParameterException("HttpUrl_To_Local_File is null");
                        if (httpUrl_to_local_file[0].get_url() == null)
                            throw new RemoteFileInvalidParameterException("HttpUrl_To_Local_File.url is null");
                        if (httpUrl_to_local_file[0].get_file() == null)
                            throw new RemoteFileInvalidParameterException("HttpUrl_To_Local_File.file is null");
                        httpClient = new OkHttpClient.Builder().dispatcher(getDispatcher())
                                .readTimeout(10, TimeUnit.SECONDS)
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
                    } catch (RemoteFileInvalidParameterException e) {
                        exception = e;
                    } finally {
                        if (httpClient != null)
                            httpClient.dispatcher().cancelAll();
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

            public static class TaskExecuteQueueItem {
                private final TaskExecuteQueueItemExecutor m_executor;
                final public TaskExecuteQueueItemExecutor get_executor() {
                    return m_executor;
                }
                private final HttpUrl_To_Local_File m_httpurl_to_local_file;
                final public HttpUrl_To_Local_File get_httpUrl_to_local_file() {
                    return m_httpurl_to_local_file;
                }

                public TaskExecuteQueueItem(@NonNull final TaskExecuteQueueItemExecutor executor, @NonNull final HttpUrl_To_Local_File httpurl_to_local_file) {
                    m_executor = executor;
                    m_httpurl_to_local_file = httpurl_to_local_file;
                }
            }

            public static class TaskExecuteQueueException extends Exception {
                public TaskExecuteQueueException(String message) {
                    super(message);
                }
            }

            public static abstract class TaskExecuteQueueListener {
                private final LinkedHashMap<TaskExecuteQueueItemExecutor, Exception> item_excutor_exception_map = new LinkedHashMap<TaskExecuteQueueItemExecutor, Exception>();

                public abstract void onPreExecute();
                    public abstract void onItemExecutor_PreExecute(final TaskExecuteQueueItemExecutor executor);
                    public abstract void onItemExecutor_PostExecute(final TaskExecuteQueueItemExecutor executor);
                    public abstract void onItemExecutor_Cancelled(final TaskExecuteQueueItemExecutor executor);
                public abstract void onCancelled();
                public abstract void onPostExecute(final LinkedHashMap<TaskExecuteQueueItemExecutor, Exception> item_excutor_exception_map);
            }

            public static class TaskExecuteQueue extends LinkedBlockingQueue<TaskExecuteQueueItem> {
                private final TaskExecuteQueueListener m_listener;
                private int m_n_pending = 0;

                public TaskExecuteQueue() {
                    m_listener = null;
                    m_n_pending = 0;
                }
                public TaskExecuteQueue(final TaskExecuteQueueListener listener) {
                    m_listener = listener;
                    m_n_pending = 0;
                }

                @Override
                public boolean add(TaskExecuteQueueItem asyncGetFileTaskExecuteQueueItem) {
                    final boolean b_added = super.add(asyncGetFileTaskExecuteQueueItem);
                    if (b_added)
                        ++m_n_pending;
                    return b_added;
                }

                public void execute() throws TaskExecuteQueueException {
                    if (isEmpty())
                        throw new TaskExecuteQueueException("queue is empty");

                    if (m_listener != null)
                        m_listener.onPreExecute();

                    Iterator<TaskExecuteQueueItem> iterator_exec_queue_items = iterator();
                    while (iterator_exec_queue_items.hasNext()) {
                        TaskExecuteQueueItem exec_queue_item = iterator_exec_queue_items.next();
                        exec_queue_item.get_executor().execute(exec_queue_item.get_httpUrl_to_local_file());
                    }
                }
            }

            public static class TaskExecuteQueueItemExecutor extends Task {
                private final static String TAG = "Utils.HTTP.AsyncGet" + TaskExecuteQueueItemExecutor.class.getSimpleName();

                private final TaskExecuteQueue m_queue;

                public TaskExecuteQueueItemExecutor(@NonNull TaskStageHandler stage_handler, @NonNull TaskExecuteQueue queue) {
                    super(stage_handler);
                    m_queue = queue;
                }

                @Override
                protected void onPreExecute() {
                }

                @Override
                protected void onCancelled(Exception exception) {
                    Log.d(TAG, "onCancelled: exception is: " + (exception != null ? exception.getClass().getSimpleName() : "<no exception>") +"; calling super.onCancelled(exception)...");
                    super.onCancelled(exception);
                    --m_queue.m_n_pending;
                    if (m_queue.m_listener != null) {
                        if (exception != null) {
                            Log.d(TAG, "onCancelled: adding exception " + exception.getClass().getSimpleName() + " for this executor to m_queue.m_listener.item_excutor_exception_map...");
                            m_queue.m_listener.item_excutor_exception_map.put(this, exception);
                        }
                        Log.d(TAG, "onCancelled: canceling http get request (" + get_http_get_call().request().toString() + ")");
                        get_http_get_call().cancel();
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
                    Log.d(TAG, "onPostExecute: exception: " + (exception != null ? exception.getClass().getSimpleName() : "<no exception>") +"; calling super.onPostExecute(exception)...");
                    super.onPostExecute(exception);
                    --m_queue.m_n_pending;
                    if (m_queue.m_listener != null) {
                        if (exception != null) {
                            Log.d(TAG, "onPostExecute: adding exception " + exception.getClass().getSimpleName() + " for this executor to m_queue.m_listener.item_excutor_exception_map...");
                            m_queue.m_listener.item_excutor_exception_map.put(this, exception);
                        }
                        Log.d(TAG, "onPostExecute: canceling http get request (" + get_http_get_call().request().toString() + ")");
                        get_http_get_call().cancel();
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

    public final static class TEGOLA_BIN {
        private final String TAG = TEGOLA_BIN.class.getCanonicalName();

        private static TEGOLA_BIN m_this = null;
        private final File f_tegola_bin;
        private Constants.Enums.CPU_ABI[] supp_abis;

        private TEGOLA_BIN(@NonNull final Context context) throws PackageManager.NameNotFoundException, IOException, Exceptions.TegolaBinaryNotExecutableException, Exceptions.UnsupportedCPUABIException {
            File f_libs = Utils.Files.F_LIBS_DIR.getInstance(context);
            if (BuildConfig.tegola_bin_abi_map.size() > 0) {
                ArrayList<Constants.Enums.CPU_ABI> al_supported_abis = null;
                String s_tegola_bin_for_abi = null;
                for (String s_tegola_bin_name : BuildConfig.tegola_bin_abi_map.keySet()) {
                    Log.d(TAG, "TEGOLA_BIN ctor: BuildConfig.tegola_bin_abi_map - found key (tegola bin name): " + s_tegola_bin_name);
                    String[] abi_list = BuildConfig.tegola_bin_abi_map.get(s_tegola_bin_name);
                    if (abi_list != null && abi_list.length > 0) {
                        ArrayList<Constants.Enums.CPU_ABI> al_abis = new ArrayList<>();
                        Log.d(TAG, "TEGOLA_BIN ctor: \tabi list:");
                        for (int i = 0; i < abi_list.length; i++) {
                            String abi = abi_list[i];
                            al_abis.add(Constants.Enums.CPU_ABI.fromString(abi));
                            Log.d(TAG, "TEGOLA_BIN ctor: \t\t" + abi);
                            if (abi.equals(Build.CPU_ABI)) {
                                Log.d(TAG, "TEGOLA_BIN ctor: \t\t\tfound bin name (" + s_tegola_bin_name + ") for matching ABI (" + Build.CPU_ABI + ")!");
                                s_tegola_bin_for_abi = s_tegola_bin_name;
                                al_supported_abis = al_abis;
                            }
                        }
                    }
                }
                if (s_tegola_bin_for_abi != null) {
                    f_tegola_bin = new File(f_libs, s_tegola_bin_for_abi);
                    f_tegola_bin.setReadOnly();
                    f_tegola_bin.setExecutable(true);
                    if (f_tegola_bin.setExecutable(true))
                        throw new Exceptions.TegolaBinaryNotExecutableException(f_tegola_bin.getName());
                    Log.d(TAG, "TEGOLA_BIN ctor: teggola bin " + f_tegola_bin.getCanonicalPath() + " " + (f_tegola_bin.exists() ? "exists" : "does NOT exist"));
                    supp_abis = al_supported_abis.toArray(new Constants.Enums.CPU_ABI[al_supported_abis.size()]);
                } else {
                    throw new Exceptions.UnsupportedCPUABIException("no tegola binary exists for ABI " + Build.CPU_ABI);
                }
            } else
                f_tegola_bin = null;
        }
        private TEGOLA_BIN() {
            f_tegola_bin = null;
        }

        public static TEGOLA_BIN getInstance(@NonNull final Context context) throws PackageManager.NameNotFoundException, IOException, Exceptions.TegolaBinaryNotExecutableException, Exceptions.UnsupportedCPUABIException {
            if (m_this == null)
                m_this = new TEGOLA_BIN(context);
            return m_this;
        }

        public final File get() {
            return f_tegola_bin;
        }

        private String m_s_ver = "UNKNOWN";
        public void set_version_string(@NonNull final String s_ver) {m_s_ver = s_ver;}
        public String get_version_string() {return m_s_ver;}

        public final Constants.Enums.CPU_ABI[] supported_ABIs() {return supp_abis;}
    }

    public static class GPKG {
        public static class Local {
            public static class F_GPKG_DIR extends File {
                private static F_GPKG_DIR m_this = null;
                private F_GPKG_DIR(@NonNull final Context context) throws PackageManager.NameNotFoundException, IOException {
                    super(
                            Boolean.valueOf(
                                    getAssetProperty(
                                            context
                                            , "gpkg.properties"
                                            , "PUBLIC"
                                    )
                            )
                                ? Files.F_PUBLIC_ROOT_DIR.getInstance(context)
                                : context.getFilesDir()
                            , Constants.Strings.GPKG_BUNDLE.SUBDIR
                    );
                }
                public static F_GPKG_DIR getInstance(@NonNull final Context context) throws PackageManager.NameNotFoundException, IOException {
                    if (m_this == null)
                        m_this = new F_GPKG_DIR(context);
                    return m_this;
                }
            }
        }

        public static class Remote {
            public static String build_root_url_string(final String s_http_proto_prefix, final String s_gpkg_bundle__root_url, final String s_gpkg_bundle__name) {
                return new StringBuilder()
                        .append(s_http_proto_prefix)
                        .append(s_gpkg_bundle__root_url)
                        .append("/")
                        .append(s_gpkg_bundle__name)
                        .toString();
            }
        }
    }

    public static class Shell {
        private final static String TAG = Utils.Shell.class.getCanonicalName();

        //caution: do not use for commands that produce "a lot" of output
        public static String[] run(final String cmd, final String grep_str) {
            ArrayList<String> list_output_lines = new ArrayList<>();
            try {
                Process proc = Runtime.getRuntime().exec(cmd);
                InputStream inputstream_proc = proc != null ? proc.getInputStream() : null;
                if (inputstream_proc != null) {
                    BufferedReader reader_logcat_proc_inputstream = new BufferedReader(new InputStreamReader(inputstream_proc));
                    String s_line = "";
                    boolean badd = true;
                    while ((s_line = reader_logcat_proc_inputstream.readLine()) != null) {
                        Log.d(TAG, "run: cmd '" + cmd + "' output-line: '" + s_line + "'");
                        if (grep_str != null && grep_str.length() > 0) {
                            if (!s_line.contains(grep_str)) {
                                Log.d(TAG, "run:\t\tDID NOT FIND occurrence of '" + grep_str + "' in output-line");
                                badd = false;
                            } else {
                                Log.d(TAG, "run:\t\tFOUND occurrence of '" + grep_str + "' in output-line");
                                badd = true;
                            }
                        }
                        if (badd) {
                            Log.d(TAG, "run:\t\tADDING output-line to output-aggregate");
                            list_output_lines.add(s_line);
                        }
                    }
                    reader_logcat_proc_inputstream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            String[] s_ary_output_aggregate = list_output_lines.toArray(new String[list_output_lines.size()]);
            Log.d(TAG, "run: output-aggregate contains " + s_ary_output_aggregate.length + " lines");
            return s_ary_output_aggregate;
        }
        public static String[] run(final String cmd) {
            return run(cmd, null);
        }
    }
}
