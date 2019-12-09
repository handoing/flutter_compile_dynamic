package com.example.flutter_compile_dynamic;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.flutter.embedding.engine.FlutterJNI;
import io.flutter.util.PathUtils;
import io.flutter.view.FlutterMain;
import io.flutter.view.VsyncWaiter;

public class MyFlutterMain {

    private static final String TAG = "FlutterMain";
    private static final String AOT_SHARED_LIBRARY_NAME = "aot-shared-library-name";
    private static final String SNAPSHOT_ASSET_PATH_KEY = "snapshot-asset-path";
    private static final String VM_SNAPSHOT_DATA_KEY = "vm-snapshot-data";
    private static final String ISOLATE_SNAPSHOT_DATA_KEY = "isolate-snapshot-data";
    private static final String FLUTTER_ASSETS_DIR_KEY = "flutter-assets-dir";
    public static final String PUBLIC_AOT_SHARED_LIBRARY_NAME = MyFlutterMain.class.getName() + '.' + "aot-shared-library-name";
    public static final String PUBLIC_VM_SNAPSHOT_DATA_KEY = MyFlutterMain.class.getName() + '.' + "vm-snapshot-data";
    public static final String PUBLIC_ISOLATE_SNAPSHOT_DATA_KEY = MyFlutterMain.class.getName() + '.' + "isolate-snapshot-data";
    public static final String PUBLIC_FLUTTER_ASSETS_DIR_KEY = MyFlutterMain.class.getName() + '.' + "flutter-assets-dir";
    private static final String DEFAULT_AOT_SHARED_LIBRARY_NAME = "libapp.so";
    private static final String DEFAULT_VM_SNAPSHOT_DATA = "vm_snapshot_data";
    private static final String DEFAULT_ISOLATE_SNAPSHOT_DATA = "isolate_snapshot_data";
    private static final String DEFAULT_LIBRARY = "libflutter.so";
    private static final String DEFAULT_KERNEL_BLOB = "kernel_blob.bin";
    private static final String DEFAULT_FLUTTER_ASSETS_DIR = "flutter_assets";
    private static String sAotSharedLibraryName = "libapp.so";
    private static String sVmSnapshotData = "vm_snapshot_data";
    private static String sIsolateSnapshotData = "isolate_snapshot_data";
    private static String sFlutterAssetsDir = "flutter_assets";
    private static boolean sInitialized = false;
    @Nullable
    private static ResourceExtractor sResourceExtractor;
    @Nullable
    private static FlutterMain.Settings sSettings;

    public MyFlutterMain() {
    }

    @NonNull
    private static String fromFlutterAssets(@NonNull String filePath) {
        return sFlutterAssetsDir + File.separator + filePath;
    }

    public static void startInitialization(@NonNull Context applicationContext) {
        startInitialization(applicationContext, new FlutterMain.Settings());
    }

    public static void startInitialization(@NonNull Context applicationContext, @NonNull FlutterMain.Settings settings) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("startInitialization must be called on the main thread");
        } else if (sSettings == null) {
            sSettings = settings;
            long initStartTimestampMillis = SystemClock.uptimeMillis();
            initConfig(applicationContext);
            initResources(applicationContext);
            System.loadLibrary("flutter");
            VsyncWaiter.getInstance((WindowManager)applicationContext.getSystemService(Context.WINDOW_SERVICE)).init();
            long initTimeMillis = SystemClock.uptimeMillis() - initStartTimestampMillis;
            FlutterJNI.nativeRecordStartTimestamp(initTimeMillis);
        }
    }

    public static void ensureInitializationComplete(@NonNull Context applicationContext, @Nullable String[] args) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ensureInitializationComplete must be called on the main thread");
        } else if (sSettings == null) {
            throw new IllegalStateException("ensureInitializationComplete must be called after startInitialization");
        } else if (!sInitialized) {
            try {
                if (sResourceExtractor != null) {
                    sResourceExtractor.waitForCompletion();
                }

                List<String> shellArgs = new ArrayList();
                shellArgs.add("--icu-symbol-prefix=_binary_icudtl_dat");
                ApplicationInfo applicationInfo = getApplicationInfo(applicationContext);
                shellArgs.add("--icu-native-lib-path=" + applicationInfo.nativeLibraryDir + File.separator + "libflutter.so");
                if (args != null) {
                    Collections.addAll(shellArgs, args);
                }

                File dir = applicationContext.getDir("libs", Activity.MODE_PRIVATE);
                String libPath =  dir.getAbsolutePath() + File.separator + "libapp.so";

                shellArgs.add("--aot-shared-library-name=" + libPath);

                shellArgs.add("--snapshot-asset-path=" + PathUtils.getDataDirectory(applicationContext) + "/" + sFlutterAssetsDir);
                shellArgs.add("--vm-snapshot-data=" + sVmSnapshotData);
                shellArgs.add("--isolate-snapshot-data=" + sIsolateSnapshotData);
                shellArgs.add("--cache-dir-path=" + PathUtils.getCacheDirectory(applicationContext));
                if (sSettings.getLogTag() != null) {
                    shellArgs.add("--log-tag=" + sSettings.getLogTag());
                }

                String appBundlePath = findAppBundlePath(applicationContext);
                String appStoragePath = PathUtils.getFilesDir(applicationContext);
                String engineCachesPath = PathUtils.getCacheDirectory(applicationContext);
                FlutterJNI.nativeInit(applicationContext, (String[])shellArgs.toArray(new String[0]), appBundlePath, appStoragePath, engineCachesPath);
                sInitialized = true;
            } catch (Exception var7) {
                Log.e("FlutterMain", "Flutter initialization failed.", var7);
                throw new RuntimeException(var7);
            }
        }
    }

    public static void ensureInitializationCompleteAsync(@NonNull final Context applicationContext, @Nullable final String[] args, @NonNull final Handler callbackHandler, @NonNull final Runnable callback) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ensureInitializationComplete must be called on the main thread");
        } else if (sSettings == null) {
            throw new IllegalStateException("ensureInitializationComplete must be called after startInitialization");
        } else if (!sInitialized) {
            (new Thread(new Runnable() {
                public void run() {
                    if (MyFlutterMain.sResourceExtractor != null) {
                        MyFlutterMain.sResourceExtractor.waitForCompletion();
                    }

                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            MyFlutterMain.ensureInitializationComplete(applicationContext.getApplicationContext(), args);
                            callbackHandler.post(callback);
                        }
                    });
                }
            })).start();
        }
    }

    @NonNull
    private static ApplicationInfo getApplicationInfo(@NonNull Context applicationContext) {
        try {
                return applicationContext.getPackageManager().getApplicationInfo(applicationContext.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException var2) {
            throw new RuntimeException(var2);
        }
    }

    private static void initConfig(@NonNull Context applicationContext) {
        Bundle metadata = getApplicationInfo(applicationContext).metaData;
        if (metadata != null) {
            sAotSharedLibraryName = metadata.getString(PUBLIC_AOT_SHARED_LIBRARY_NAME, "libapp.so");
            sFlutterAssetsDir = metadata.getString(PUBLIC_FLUTTER_ASSETS_DIR_KEY, "flutter_assets");
            sVmSnapshotData = metadata.getString(PUBLIC_VM_SNAPSHOT_DATA_KEY, "vm_snapshot_data");
            sIsolateSnapshotData = metadata.getString(PUBLIC_ISOLATE_SNAPSHOT_DATA_KEY, "isolate_snapshot_data");
        }
    }

    private static void initResources(@NonNull Context applicationContext) {
        (new ResourceCleaner(applicationContext)).start();
        String dataDirPath = PathUtils.getDataDirectory(applicationContext);
        String packageName = applicationContext.getPackageName();
        PackageManager packageManager = applicationContext.getPackageManager();
        AssetManager assetManager = applicationContext.getResources().getAssets();
        sResourceExtractor = new ResourceExtractor(dataDirPath, packageName, packageManager, assetManager);
        sResourceExtractor.addResource(fromFlutterAssets(sVmSnapshotData)).addResource(fromFlutterAssets(sIsolateSnapshotData)).addResource(fromFlutterAssets("kernel_blob.bin"));
        sResourceExtractor.start();
    }

    @Nullable
    public static String findAppBundlePath(@NonNull Context applicationContext) {
        String dataDirectory = PathUtils.getDataDirectory(applicationContext);
        File appBundle = new File(dataDirectory, sFlutterAssetsDir);
        return appBundle.exists() ? appBundle.getPath() : null;
    }

    @NonNull
    public static String getLookupKeyForAsset(@NonNull String asset) {
        return fromFlutterAssets(asset);
    }

    @NonNull
    public static String getLookupKeyForAsset(@NonNull String asset, @NonNull String packageName) {
        return getLookupKeyForAsset("packages" + File.separator + packageName + File.separator + asset);
    }

    public static class Settings {
        private String logTag;

        public Settings() {
        }

        @Nullable
        public String getLogTag() {
            return this.logTag;
        }

        public void setLogTag(String tag) {
            this.logTag = tag;
        }
    }
}
