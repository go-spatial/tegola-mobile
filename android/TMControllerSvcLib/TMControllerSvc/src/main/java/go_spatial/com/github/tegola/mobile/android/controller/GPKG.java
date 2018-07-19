package go_spatial.com.github.tegola.mobile.android.controller;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import go_spatial.com.github.tegola.mobile.android.controller.utils.Files;

public class GPKG {
    public static class Local {
        public static class F_GPKG_DIR extends File {
            private static Local.F_GPKG_DIR m_this = null;
            private F_GPKG_DIR(@NonNull final Context context) throws PackageManager.NameNotFoundException, IOException {
                super(
                    Boolean.valueOf(
                        Files.getAssetPropsFileProperty(
                            context
                            , "gpkg.properties"
                            , "PUBLIC"
                        )
                    ) ? Files.F_PUBLIC_ROOT_DIR.getInstance(context) : context.getFilesDir()
                    , Constants.Strings.GPKG_BUNDLE.SUBDIR
                );
            }
            public static Local.F_GPKG_DIR getInstance(@NonNull final Context context) throws PackageManager.NameNotFoundException, IOException {
                if (m_this == null)
                    m_this = new Local.F_GPKG_DIR(context);
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
