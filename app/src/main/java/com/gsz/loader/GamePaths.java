package com.gsz.loader;

import android.os.Environment;
import java.io.File;

public final class GamePaths {
    private static final String[] PACKAGES = {
            "com.tencent.ig",
            "com.tencent.igfit",
            "com.vng.pubgmobile",
            "com.pubg.krmobile",
            "com.rekoo.pubgm",
            "com.pubg.imobile",
    };
    private GamePaths() {}
    public static String[] getPackages() { return PACKAGES.clone(); }

    public static String paksDir() {
        String base = Environment.getExternalStorageDirectory().getAbsolutePath();
        for (String p : PACKAGES) {
            String path = base + "/Android/data/" + p
                    + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Paks";
            if (new File(path).exists()) return path;
        }
        return base + "/Android/data/" + PACKAGES[0]
                + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Paks";
    }

    public static String loaderConfigPath() {
        return paksDir() + "/grow_loader.txt";
    }

    public static String luaFilePath() {
        return paksDir() + "/growzsmd.lua";
    }
}
