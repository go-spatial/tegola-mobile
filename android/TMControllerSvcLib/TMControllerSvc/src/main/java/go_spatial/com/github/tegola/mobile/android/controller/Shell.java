package go_spatial.com.github.tegola.mobile.android.controller;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Shell {
    private final static String TAG = Shell.class.getCanonicalName();

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
