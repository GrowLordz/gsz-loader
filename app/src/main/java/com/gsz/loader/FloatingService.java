package com.gsz.loader;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FloatingService extends Service {

    public static final String EXTRA_CONFIG_PAYLOAD = "com.gsz.loader.EXTRA_CONFIG_PAYLOAD";
    private static final String CHANNEL_ID = "gsz_floating";
    private static final int NOTIF_ID = 1001;

    private WindowManager windowManager;
    private View fabView;
    private View panelView;
    private WindowManager.LayoutParams fabParams;
    private WindowManager.LayoutParams panelParams;

    private boolean panelExpanded = false;
    private String currentTab = "ESP";

    private LexusConfig config = new LexusConfig();
    private CustomTextData customData = new CustomTextData();

    private TextView tabEsp, tabAim, tabMisc;
    private LinearLayout contentEsp, contentAim, contentMisc;
    private TextView masterLabel;
    private Switch masterSwitch;

    private boolean contentBuiltEsp = false;
    private boolean contentBuiltAim = false;
    private boolean contentBuiltMisc = false;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        startForegroundInternal();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Allow display over other apps, then start the menu again", Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }
        try {
            showFab();
        } catch (Throwable t) {
            Toast.makeText(this, "Unable to create floating button: " + t.getMessage(), Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    private void startForegroundInternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "GSZ Floating", NotificationManager.IMPORTANCE_MIN);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }

        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GSZ Loader")
                .setContentText("Floating panel active")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();

        startForeground(NOTIF_ID, notif);
    }

    // ================================================================
    // FAB
    // ================================================================
    private void showFab() {
        if (fabView != null) return;
        fabView = LayoutInflater.from(this).inflate(R.layout.overlay_fab, null);

        int type = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        fabParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        fabParams.gravity = Gravity.TOP | Gravity.START;
        fabParams.x = 0;
        fabParams.y = 300;

        attachFabDrag(fabView);
        windowManager.addView(fabView, fabParams);
    }

    private void attachFabDrag(View v) {
        final int[] startPos = new int[2];
        final int[] startTouch = new int[2];
        final boolean[] dragged = {false};

        v.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startPos[0] = fabParams.x;
                    startPos[1] = fabParams.y;
                    startTouch[0] = (int) event.getRawX();
                    startTouch[1] = (int) event.getRawY();
                    dragged[0] = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - startTouch[0];
                    int dy = (int) event.getRawY() - startTouch[1];
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) dragged[0] = true;
                    fabParams.x = startPos[0] + dx;
                    fabParams.y = startPos[1] + dy;
                    try { windowManager.updateViewLayout(fabView, fabParams); } catch (Throwable ignored) {}
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!dragged[0]) {
                        togglePanel();
                    } else {
                        int screenW = getResources().getDisplayMetrics().widthPixels;
                        if (fabParams.x + v.getWidth() / 2 < screenW / 2) fabParams.x = 0;
                        else fabParams.x = screenW - v.getWidth();
                        try { windowManager.updateViewLayout(fabView, fabParams); } catch (Throwable ignored) {}
                    }
                    return true;
            }
            return false;
        });
    }

    // ================================================================
    // PANEL
    // ================================================================
    private void togglePanel() {
        if (panelExpanded) hidePanel(); else showPanel();
    }

    private void showPanel() {
        if (panelView != null) {
            try { windowManager.removeView(panelView); } catch (Throwable ignored) {}
            panelView = null;
        }

        panelView = LayoutInflater.from(this).inflate(R.layout.overlay_panel, null);

        // Cache view refs
        tabEsp = panelView.findViewById(R.id.tabEsp);
        tabAim = panelView.findViewById(R.id.tabAim);
        tabMisc = panelView.findViewById(R.id.tabMisc);
        contentEsp = panelView.findViewById(R.id.contentEsp);
        contentAim = panelView.findViewById(R.id.contentAim);
        contentMisc = panelView.findViewById(R.id.contentMisc);
        masterLabel = panelView.findViewById(R.id.masterLabel);
        masterSwitch = panelView.findViewById(R.id.masterSwitch);

        // Tag untuk UIHelper deteksi overlay mode
        contentEsp.setTag("overlay");
        contentAim.setTag("overlay");
        contentMisc.setTag("overlay");

        // Build konten tab (lazy)
        ensureTabContent(currentTab);

        // Tab listeners
        tabEsp.setOnClickListener(v -> switchTab("ESP"));
        tabAim.setOnClickListener(v -> switchTab("AIMBOT"));
        tabMisc.setOnClickListener(v -> switchTab("MISC"));

        // Master switch
        updateMasterLabel();
        masterSwitch.setOnCheckedChangeListener((bv, checked) -> onMasterToggle(checked));

        // Drag header
        attachPanelDrag(panelView.findViewById(R.id.header));

        // Buttons
        panelView.findViewById(R.id.btnMinimize).setOnClickListener(v -> hidePanel());
        panelView.findViewById(R.id.btnClose).setOnClickListener(v -> { hidePanel(); stopSelf(); });

        Button reload = panelView.findViewById(R.id.btnReloadOverlay);
        reload.setOnClickListener(v -> reloadConfig());

        Button push = panelView.findViewById(R.id.btnPushOverlay);
        push.setOnClickListener(v -> pushConfig());

        updateStatusDot();

        // Window layout
        int type = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        panelParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        panelParams.gravity = Gravity.TOP | Gravity.START;
        panelParams.x = 40;
        panelParams.y = 180;

        windowManager.addView(panelView, panelParams);
        panelExpanded = true;
        fabView.setVisibility(View.GONE);
    }

    private void switchTab(String tab) {
        currentTab = tab;
        ensureTabContent(tab);

        contentEsp.setVisibility("ESP".equals(tab) ? View.VISIBLE : View.GONE);
        contentAim.setVisibility("AIMBOT".equals(tab) ? View.VISIBLE : View.GONE);
        contentMisc.setVisibility("MISC".equals(tab) ? View.VISIBLE : View.GONE);

        updateTabStyles();
        updateMasterLabel();
        updateMasterState();
    }

    private void ensureTabContent(String tab) {
        switch (tab) {
            case "ESP":
                if (!contentBuiltEsp) {
                    UIHelper.buildCategory(this, contentEsp, config, customData, "ESP");
                    contentBuiltEsp = true;
                }
                break;
            case "AIMBOT":
                if (!contentBuiltAim) {
                    UIHelper.buildCategory(this, contentAim, config, customData, "AIMBOT");
                    contentBuiltAim = true;
                }
                break;
            case "MISC":
                if (!contentBuiltMisc) {
                    UIHelper.buildCategory(this, contentMisc, config, customData, "MISC");
                    contentBuiltMisc = true;
                }
                break;
        }
    }

    private void updateTabStyles() {
        int activeBg = R.drawable.bg_tab_active;
        int inactiveBg = R.drawable.bg_tab_inactive;
        int activeColor = 0xFFFFFFFF;
        int inactiveColor = 0xFF8890A0;

        tabEsp.setBackgroundResource("ESP".equals(currentTab) ? activeBg : inactiveBg);
        tabEsp.setTextColor("ESP".equals(currentTab) ? activeColor : inactiveColor);

        tabAim.setBackgroundResource("AIMBOT".equals(currentTab) ? activeBg : inactiveBg);
        tabAim.setTextColor("AIMBOT".equals(currentTab) ? activeColor : inactiveColor);

        tabMisc.setBackgroundResource("MISC".equals(currentTab) ? activeBg : inactiveBg);
        tabMisc.setTextColor("MISC".equals(currentTab) ? activeColor : inactiveColor);
    }

    private void updateMasterLabel() {
        switch (currentTab) {
            case "ESP":    masterLabel.setText("ENABLE ESP"); break;
            case "AIMBOT": masterLabel.setText("ENABLE AIMBOT"); break;
            case "MISC":   masterLabel.setText("ENABLE MISC"); break;
        }
    }

    private void updateMasterState() {
        boolean on = false;
        switch (currentTab) {
            case "ESP":    on = config.EspVipPro || config.EspLoai9 || config.EspLoai5 || config.EspLoai6; break;
            case "AIMBOT": on = config.AimTouchEnable || config.SmartBT_Enable; break;
            case "MISC":   on = config.ModSkin || config.UnlockFPS; break;
        }
        masterSwitch.setOnCheckedChangeListener(null);
        masterSwitch.setChecked(on);
        masterSwitch.setOnCheckedChangeListener((bv, checked) -> onMasterToggle(checked));
    }

    private void onMasterToggle(boolean on) {
        switch (currentTab) {
            case "ESP":
                config.EspVipPro = on;
                config.EspLoai9 = on;
                break;
            case "AIMBOT":
                config.AimTouchEnable = on;
                config.SmartBT_Enable = on;
                break;
            case "MISC":
                config.ModSkin = on;
                config.UnlockFPS = on;
                break;
        }
    }

    private void attachPanelDrag(View v) {
        final int[] startPos = new int[2];
        final int[] startTouch = new int[2];

        v.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startPos[0] = panelParams.x;
                    startPos[1] = panelParams.y;
                    startTouch[0] = (int) event.getRawX();
                    startTouch[1] = (int) event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    panelParams.x = startPos[0] + (int) event.getRawX() - startTouch[0];
                    panelParams.y = startPos[1] + (int) event.getRawY() - startTouch[1];
                    try { windowManager.updateViewLayout(panelView, panelParams); } catch (Throwable ignored) {}
                    return true;
            }
            return false;
        });
    }

    private void hidePanel() {
        if (panelView != null) {
            try { windowManager.removeView(panelView); } catch (Throwable ignored) {}
            panelView = null;
        }
        panelExpanded = false;
        if (fabView != null) fabView.setVisibility(View.VISIBLE);
    }

    private void updateStatusDot() {
        if (panelView == null) return;
        View dot = panelView.findViewById(R.id.statusDot);
        if (dot == null) return;
        boolean ok;
        try { ok = ShizukuHelper.hasPermission(); } catch (Throwable t) { ok = false; }
        dot.setBackgroundResource(ok ? R.drawable.bg_status_dot_green : R.drawable.bg_status_dot_red);
    }

    private void pushConfig() {
        boolean granted;
        try { granted = ShizukuHelper.hasPermission(); } catch (Throwable t) { granted = false; }
        if (!granted) { toast("Grant Shizuku first"); return; }

        final String payload = ConfigWriter.buildLuaPayload(config, customData);
        final String target = GamePaths.loaderConfigPath();

        new Thread(() -> {
            boolean ok = ShizukuHelper.writeFileAsShell(target, payload);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (ok) toast("✓ Pushed " + payload.length() + " bytes");
                else    toast("✗ Push failed");
            });
        }).start();
    }

    private void reloadConfig() {
        boolean granted;
        try { granted = ShizukuHelper.hasPermission(); } catch (Throwable t) { granted = false; }
        if (!granted) { toast("Grant Shizuku first"); return; }

        final String target = GamePaths.loaderConfigPath();
        new Thread(() -> {
            String content = ShizukuHelper.readFileAsShell(target);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (content == null || content.isEmpty()) { toast("No config file"); return; }
                boolean ok = ConfigWriter.applyLuaToConfig(content, config, customData);
                if (ok) {
                    rebuildAllTabs();
                    toast("✓ Config loaded");
                } else {
                    toast("✗ Parse failed");
                }
            });
        }).start();
    }

    private void rebuildAllTabs() {
        if (panelView == null) return;
        contentEsp.removeAllViews();
        contentAim.removeAllViews();
        contentMisc.removeAllViews();
        contentBuiltEsp = false;
        contentBuiltAim = false;
        contentBuiltMisc = false;
        ensureTabContent(currentTab);
        updateMasterState();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (intent != null) {
            String payload = intent.getStringExtra(EXTRA_CONFIG_PAYLOAD);
            if (payload != null && !payload.isEmpty()) {
                ConfigWriter.applyLuaToConfig(payload, config, customData);
                rebuildAllTabs();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        super.onDestroy();
        if (fabView != null) {
            try { windowManager.removeView(fabView); } catch (Throwable ignored) {}
            fabView = null;
        }
        if (panelView != null) {
            try { windowManager.removeView(panelView); } catch (Throwable ignored) {}
            panelView = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}