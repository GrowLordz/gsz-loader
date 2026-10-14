package com.gsz.loader;

import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_SHIZUKU = 4242;
    private static final int REQ_OVERLAY = 4243;

    private TextView statusText;
    private Button btnShizuku;
    private LinearLayout container;
    private TextView pathText;

    private LexusConfig config = new LexusConfig();
    private CustomTextData customData = new CustomTextData();

    private final Shizuku.OnRequestPermissionResultListener permListener =
            (requestCode, grantResult) -> {
                if (requestCode != REQ_SHIZUKU) return;
                boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
                updateShizukuUI(granted);
                if (!granted) toast("Shizuku permission denied");
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        btnShizuku = findViewById(R.id.btnShizuku);
        container = findViewById(R.id.container);
        pathText = findViewById(R.id.pathText);

        pathText.setText("Target: " + GamePaths.loaderConfigPath());

        try {
            Shizuku.addRequestPermissionResultListener(permListener);
        } catch (Throwable t) {
            toast("Shizuku not installed");
        }

        btnShizuku.setOnClickListener(v -> requestShizuku());

        findViewById(R.id.btnFloat).setOnClickListener(v -> startFloating());
        findViewById(R.id.btnPush).setOnClickListener(v -> pushConfig());
        findViewById(R.id.btnReload).setOnClickListener(v -> reloadFromDisk());
        findViewById(R.id.btnReset).setOnClickListener(v -> {
            config = new LexusConfig();
            customData = new CustomTextData();
            buildUI();
            toast("Reset to defaults");
        });

        buildUI();
        refreshShizukuState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshShizukuState();
    }

    @Override
    protected void onDestroy() {
        try {
            Shizuku.removeRequestPermissionResultListener(permListener);
        } catch (Throwable ignored) {}
        super.onDestroy();
    }


    private void startFloating() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            try {
                Intent permissionIntent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(permissionIntent, REQ_OVERLAY);
            } catch (Throwable t) {
                toast("Unable to open overlay permission settings");
            }
            return;
        }
        launchFloatingService();
    }

    private void launchFloatingService() {
        Intent serviceIntent = new Intent(this, FloatingService.class);
        serviceIntent.putExtra(FloatingService.EXTRA_CONFIG_PAYLOAD,
                ConfigWriter.buildLuaPayload(config, customData));
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, serviceIntent);
            } else {
                startService(serviceIntent);
            }
            toast("Floating menu started. Tap the GSZ bubble to open it.");
        } catch (Throwable t) {
            toast("Could not start floating menu: " + t.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_OVERLAY) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
                launchFloatingService();
            } else {
                toast("Overlay permission is required to show the floating menu");
            }
        }
    }

    private void refreshShizukuState() {
        boolean alive;
        try { alive = Shizuku.pingBinder(); } catch (Throwable t) { alive = false; }
        boolean granted = alive && ShizukuHelper.hasPermission();
        updateShizukuUI(granted);
    }

    private void updateShizukuUI(boolean granted) {
        if (granted) {
            statusText.setText(R.string.shizuku_active);
            statusText.setTextColor(getColor(R.color.accent_green));
            btnShizuku.setVisibility(View.GONE);
        } else {
            statusText.setText(R.string.shizuku_inactive);
            statusText.setTextColor(getColor(R.color.accent_red));
            btnShizuku.setVisibility(View.VISIBLE);
        }
    }

    private void requestShizuku() {
        try {
            if (!Shizuku.pingBinder()) {
                toast("Start Shizuku app first (via ADB or Wireless Debug)");
                return;
            }
            if (Shizuku.isPreV11() || Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                updateShizukuUI(true);
                return;
            }
            Shizuku.requestPermission(REQ_SHIZUKU);
        } catch (Throwable t) {
            toast("Shizuku error: " + t.getMessage());
        }
    }

    private void buildUI() {
        container.removeAllViews();
        UIHelper.buildFromConfig(this, container, config, customData);
    }

    private void pushConfig() {
        boolean granted;
        try { granted = ShizukuHelper.hasPermission(); } catch (Throwable t) { granted = false; }
        if (!granted) {
            toast("Grant Shizuku permission first");
            return;
        }

        final String payload = ConfigWriter.buildLuaPayload(config, customData);
        final String target = GamePaths.loaderConfigPath();

        btnPushSetEnabled(false);
        new Thread(() -> {
            boolean ok = ShizukuHelper.writeFileAsShell(target, payload);
            runOnUiThread(() -> {
                btnPushSetEnabled(true);
                if (ok) toast("Config pushed (" + payload.length() + " bytes)");
                else    toast("Push failed — check Shizuku + path");
            });
        }).start();
    }

    private void btnPushSetEnabled(boolean enabled) {
        View b = findViewById(R.id.btnPush);
        b.setEnabled(enabled);
        b.setAlpha(enabled ? 1f : 0.5f);
    }

    private void reloadFromDisk() {
        boolean granted;
        try { granted = ShizukuHelper.hasPermission(); } catch (Throwable t) { granted = false; }
        if (!granted) {
            toast("Grant Shizuku permission first");
            return;
        }

        final String target = GamePaths.loaderConfigPath();
        new Thread(() -> {
            String content = ShizukuHelper.readFileAsShell(target);
            runOnUiThread(() -> {
                if (content == null || content.isEmpty()) {
                    toast("No config file yet at target path");
                    return;
                }
                boolean ok = ConfigWriter.applyLuaToConfig(content, config, customData);
                if (ok) {
                    buildUI();
                    toast("Config loaded from disk");
                } else {
                    toast("Parse failed");
                }
            });
        }).start();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}
