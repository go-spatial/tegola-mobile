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
import java.util.Properties;

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
import okio.BufferedSink;
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

        public static class AsyncGetFileInvalidParameterException extends Exception {
            public AsyncGetFileInvalidParameterException(String message) {
                super(message);
            }
        }
        public static class AsyncGetFileAlreadyExistsException extends Exception {
            public AsyncGetFileAlreadyExistsException(String message) {
                super(message);
            }
        }
        public static class AsyncGetFileSizeException extends Exception {
            public AsyncGetFileSizeException(String message) {
                super(message);
            }
        }

        public static class AsyncGetFile extends AsyncTask <HttpUrl_To_Local_File, Void, Exception> {
            private final static String TAG = Utils.HTTP.AsyncGetFile.class.getName();

            public static abstract class Handler {
                private final static String TAG = Utils.HTTP.AsyncGetFile.Handler.class.getName();
                private HttpUrl_To_Local_File m_httpUrl_to_local_file = null;

                private void set_httpUrl_to_local_file(@NonNull final HttpUrl_To_Local_File httpUrl_to_local_file) {
                    m_httpUrl_to_local_file = httpUrl_to_local_file;
                }
                public final HttpUrl_To_Local_File get_httpUrl_to_local_file() {
                    return m_httpUrl_to_local_file;
                }

                public abstract void onPreExecute();
                public abstract void onChunkRead(Buffer sink, long bytesRead, long contentLength, boolean done);
                public abstract void onFileAlreadyExists();
                public abstract void onCancelled(Exception exception);
                public abstract void onPostExecute(Exception exception);
            }
            private final Handler m_Handler;

            public AsyncGetFile(@NonNull final Handler handler) {
                m_Handler = handler;
            }

            private static class ChunkedResponseBody extends ResponseBody {
                private final static String TAG = Utils.HTTP.AsyncGetFile.ChunkedResponseBody.class.getSimpleName();
                private final ResponseBody m_responseBody;
                private final Handler m_handler;
                private BufferedSource m_bufferedSource;

                ChunkedResponseBody(@NonNull final ResponseBody responseBody, @NonNull final HttpUrl_To_Local_File httpUrl_to_local_file, @NonNull final Handler handler) {
                    m_responseBody = responseBody;
                    handler.set_httpUrl_to_local_file(httpUrl_to_local_file);
                    m_handler = handler;
                }

                @Override public MediaType contentType() {
                    return m_responseBody.contentType();
                }

                @Override public long contentLength() {
                    return m_responseBody.contentLength();
                }

                //cannot use BufferedSource for large files so why don't we disable this method altogether?
                @Override public BufferedSource source() {
                    if (m_bufferedSource == null) {
                        Log.d(TAG, "Okio buffering ResponseBody source (contentLength " + contentLength() + ")");
                        m_bufferedSource = Okio.buffer(source(m_responseBody.source()));
                    } else {
//                        Log.d(TAG, "NOT using Okio to buffer source(m_responseBody.source())..");
                    }
                    return m_bufferedSource;
                }

                private Source source(Source source) {
                    final Source source_ret = new ForwardingSource(source) {
                        @Override public long read(Buffer sink, long byteCount) throws IOException {
                            long bytesRead = super.read(sink, byteCount);
                            //Log.d(TAG, "ForwardingSource::read() - read " + bytesRead + " bytes into sink; assert sink.size()==" + bytesRead + " --> " + (sink.size() == bytesRead) + "; calling m_handler.onChunkRead()...");
                            // read() returns the number of bytes read, or -1 if this source is exhausted.
                            m_handler.onChunkRead(sink, bytesRead, m_responseBody.contentLength(), bytesRead == -1);
                            return bytesRead;
                        }
                    };
                    Log.d(TAG, "source: returning new ForwardingSource w/ our own read() override");
                    return source_ret;
                }
            }


            @Override
            protected void onPreExecute() {
                m_Handler.onPreExecute();
            }

            private long request_file_size(@NonNull final OkHttpClient httpClient, @NonNull final HttpUrl http_url) throws IOException, AsyncGetFileSizeException {
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
                                throw new AsyncGetFileSizeException(http_url.url().toString() + " - no Content-Length header");
                            }
                            try {
                                s_content_length = s_content_length.trim();
                                Long l_content_length = Long.parseLong(s_content_length);
                                if (l_content_length == null)
                                    throw new AsyncGetFileSizeException(http_url.url().toString() + " - invalid \"Content-Length value\": \"" + s_content_length + "\"");
                                l_file_size = l_content_length.longValue();
                            } catch (NumberFormatException e) {
                                throw new AsyncGetFileSizeException(http_url.url().toString() + " - invalid \"Content-Length value\": \"" + s_content_length + "\"");
                            }
                        }
                    }
                } finally {
                    if (response != null)
                        response.close();
                }
                return l_file_size;
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
                    if (response != null)
                        response.close();
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

                        if (response.body().contentLength() > 0) {
                            Log.d(TAG, "request_file_download: downloading ChunkedResponseBody (" + response.body().contentLength() + " bytes)...");
                            byte[] bytes = new byte[1024];
                            int n_bytes_read = 0;
                            while ((n_bytes_read += response.body().source().read(bytes)) < response.body().contentLength()) {}
                            Log.d(TAG, "request_file_download: done downloading ChunkedResponseBody (" + response.body().contentLength() + " bytes)!");
                        }
                    }
                } finally {
                    if (response != null)
                        response.close();
                }
            }

            @Override
            protected Exception doInBackground(final HttpUrl_To_Local_File[] httpUrl_to_local_file) {
                Exception exception = null;

                try {
                    if (httpUrl_to_local_file == null || httpUrl_to_local_file[0] == null) {
                        cancel(false);
                        throw new AsyncGetFileInvalidParameterException("HttpUrl_To_Local_File is null");
                    }
                    if (httpUrl_to_local_file[0].get_url() == null) {
                        cancel(false);
                        throw new AsyncGetFileInvalidParameterException("HttpUrl_To_Local_File.url is null");
                    }
                    if (httpUrl_to_local_file[0].get_file() == null) {
                        cancel(false);
                        throw new AsyncGetFileInvalidParameterException("HttpUrl_To_Local_File.file is null");
                    }
                    final OkHttpClient httpClient = new OkHttpClient.Builder()
//                            .addNetworkInterceptor(new Interceptor() {
//                                @Override public Response intercept(Chain chain) throws IOException {
//                                    Response originalResponse = chain.proceed(chain.request());
//                                    return originalResponse.newBuilder()
//                                            .body(new ChunkedResponseBody(originalResponse.body(), httpUrl_to_local_file[0], m_Handler))
//                                            .build();
//                                }
//                            })
                            .addInterceptor(new Interceptor() {
                                @Override public Response intercept(Chain chain) throws IOException {
                                    Response originalResponse = chain.proceed(chain.request());
                                    return originalResponse.newBuilder()
                                            .body(new ChunkedResponseBody(originalResponse.body(), httpUrl_to_local_file[0], m_Handler))
                                            .build();
                                }
                            })
                            .build();
                    //Log.d(TAG, "doInBackground: new OkHttpClient created");
                    HttpUrl http_url = httpUrl_to_local_file[0].get_url();
                    request_file_download(httpClient, http_url);
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(false);
                    exception = e;
                } catch (AsyncGetFileInvalidParameterException e) {
                    e.printStackTrace();
                    cancel(false);
                    exception = e;
                }
                return exception;
            }

            @Override
            protected void onCancelled(Exception exception) {
                m_Handler.onCancelled(exception);
            }

            @Override
            protected void onPostExecute(Exception exception) {
                m_Handler.onPostExecute(exception);
            }
        }
    }
}
