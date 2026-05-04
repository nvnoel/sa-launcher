package com.rockstargames.gtasa;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;

public class GTASA extends Activity {

    private static final String TAG = "GTASA_Launcher";
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final int REQUEST_MANAGE_EXTERNAL_STORAGE = 101;

    // Load necessary native libraries
    private void loadNativeLibraries() {
        try {
            // Load mods explicitly
            System.loadLibrary("AML");
            System.loadLibrary("plugin_fastman92limitAdjuster");
            System.loadLibrary("threadfix");

            // Core game engine dependencies
            System.loadLibrary("TouchSenseSDK");
            System.loadLibrary("ImmEmulatorJ");
            System.loadLibrary("SCAnd");

            // Core game engine
            System.loadLibrary("GTASA");

            Log.i(TAG, "All native libraries loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load a native library: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Exception while loading native library: " + e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupImmersiveMode();

        if (checkStoragePermissions()) {
            startGameEngine();
        } else {
            requestStoragePermissions();
        }
    }

    private void setupImmersiveMode() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            window.getInsetsController().hide(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
            window.getInsetsController().setSystemBarsBehavior(android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int read = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int write = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
            }
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGameEngine();
            } else {
                Log.e(TAG, "Storage permission denied.");
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MANAGE_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    startGameEngine();
                } else {
                    Log.e(TAG, "Manage External Storage permission denied.");
                    finish();
                }
            }
        }
    }

    private void startGameEngine() {
        loadNativeLibraries();
    }

    // Override path getters to force Fastman92 unprotected directory logic in Java
    @Override
    public File getExternalFilesDir(String type) {
        File customDir = new File(Environment.getExternalStorageDirectory(), "Android_unprotected/com.rockstargames.gtasa/files");
        if (!customDir.exists()) {
            customDir.mkdirs();
        }
        return customDir;
    }

    @Override
    public File getObbDir() {
        File customDir = new File(Environment.getExternalStorageDirectory(), "Android_unprotected/com.rockstargames.gtasa/obb");
        if (!customDir.exists()) {
            customDir.mkdirs();
        }
        return customDir;
    }

    @Override
    public File getFilesDir() {
        File customDir = new File(Environment.getExternalStorageDirectory(), "Android_unprotected/com.rockstargames.gtasa/files");
        if (!customDir.exists()) {
            customDir.mkdirs();
        }
        return customDir;
    }
}
