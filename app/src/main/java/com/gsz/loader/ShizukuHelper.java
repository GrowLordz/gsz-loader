package com.gsz.loader;

import android.content.pm.PackageManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import rikka.shizuku.Shizuku;

public final class ShizukuHelper {

    private static final String TAG = "ShizukuHelper";

    private static final Method NEW_PROCESS_METHOD;

    static {
        Method method = null;
        try {
            method = Shizuku.class.getDeclaredMethod(
                    "newProcess",
                    String[].class,
                    String[].class,
                    String.class
            );
            method.setAccessible(true);
        } catch (Throwable t) {
            Log.e(TAG, "Shizuku.newProcess is unavailable on this version", t);
        }
        NEW_PROCESS_METHOD = method;
    }

    private ShizukuHelper() {
    }

    private static Process shizukuNewProcess(String[] cmd) throws Exception {
        if (NEW_PROCESS_METHOD == null) {
            throw new IllegalStateException("Shizuku.newProcess is unavailable");
        }
        return (Process) NEW_PROCESS_METHOD.invoke(null, cmd, null, null);
    }

    public static boolean isBinderAlive() {
        try {
            return Shizuku.pingBinder();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasPermission() {
        try {
            return isBinderAlive()
                    && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean writeFileAsShell(String absPath, String content) {
        if (!isBinderAlive() || !hasPermission()) {
            Log.e(TAG, "writeFileAsShell: no shizuku permission");
            return false;
        }
        try {
            String parent = new File(absPath).getParent();
            if (parent != null) {
                Process mk = shizukuNewProcess(
                        new String[]{"sh", "-c", "mkdir -p '" + parent + "'"}
                );
                mk.waitFor();
                mk.destroy();
            }

            Process p = shizukuNewProcess(
                    new String[]{
                            "sh", "-c",
                            "cat > '" + absPath + "' && chmod 644 '" + absPath + "'"
                    }
            );

            OutputStream os = p.getOutputStream();
            os.write(content.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int code = p.waitFor();
            p.destroy();
            return code == 0;
        } catch (Throwable t) {
            Log.e(TAG, "writeFileAsShell failed", t);
            return false;
        }
    }

    public static String readFileAsShell(String absPath) {
        if (!isBinderAlive() || !hasPermission()) return null;
        try {
            Process p = shizukuNewProcess(
                    new String[]{"sh", "-c", "cat '" + absPath + "' 2>/dev/null"}
            );
            InputStream is = p.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
            is.close();
            p.waitFor();
            p.destroy();
            String out = bos.toString(StandardCharsets.UTF_8);
            return out.isEmpty() ? null : out;
        } catch (Throwable t) {
            Log.e(TAG, "readFileAsShell failed", t);
            return null;
        }
    }

    public static boolean isGameRunning() {
        if (!isBinderAlive() || !hasPermission()) return false;
        try {
            for (String pkg : GamePaths.getPackages()) {
                Process p = shizukuNewProcess(
                        new String[]{"sh", "-c", "pidof " + pkg}
                );
                InputStream is = p.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[256];
                int n;
                while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
                is.close();
                p.waitFor();
                p.destroy();
                if (bos.size() > 0) return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }
}
