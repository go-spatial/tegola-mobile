package go_spatial.com.github.tegola.mobile.android.controller.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Files {
    private final static String TAG = Files.class.getCanonicalName();

    public static String getPropsFileProperty(final File f_props, final String s_prop_name) throws IOException {
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

    public static String getAssetPropsFileProperty(final Context context, final String s_props_filename, final String s_prop_name) {
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
