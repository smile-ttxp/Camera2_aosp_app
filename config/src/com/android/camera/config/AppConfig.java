package com.android.camera.config;

import com.android.camera2.BuildConfig;

public class AppConfig {
    public static boolean isCaptureModuleSupported() {
        return BuildConfig.USE_API2;
    }

    public static boolean isLandscapeScreenSupported() {
        return !BuildConfig.FORCE_PORTRAIT_SCREEN;
    }

    public static boolean isFilmstripSupported() {
        return BuildConfig.USE_FILMSTRIP;
    }
}
