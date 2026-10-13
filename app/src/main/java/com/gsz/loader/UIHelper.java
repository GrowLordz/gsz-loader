package com.gsz.loader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class UIHelper {

    private UIHelper() {}

    private static List<FieldDef> schema() {
        List<FieldDef> s = new ArrayList<>();

        s.add(FieldDef.header("COMBAT / VISUAL"));
        s.add(FieldDef.bool("FakeHWID", "Fake HWID"));
        s.add(FieldDef.bool("AutoHead", "Auto Head (hit-reg)"));
        s.add(FieldDef.bool("WallXuyenTuong", "Wall Xuyen Tuong V1"));
        s.add(FieldDef.bool("ColorBodyV2", "Chams V2 (Basic)"));
        s.add(FieldDef.bool("ColorBodyV3", "Chams V3 (Visibility)"));
        s.add(FieldDef.bool("ColorBodyNew", "Wall Mau New"));
        s.add(FieldDef.bool("WallVehicle", "Wall Vehicle"));
        s.add(FieldDef.bool("WhiteBody", "White Body"));
        s.add(FieldDef.bool("BlackSky", "Black Sky"));
        s.add(FieldDef.bool("WallClimb", "Wall Climb"));
        s.add(FieldDef.bool("FastCar", "Fast Car"));
        s.add(FieldDef.bool("WeaponGlow", "Weapon Glow"));

        s.add(FieldDef.header("ESP MAIN"));
        s.add(FieldDef.bool("EspVipPro", "ESP Type 3 (HP + Name)"));
        s.add(FieldDef.bool("EspRadar", "ESP Radar 360"));
        s.add(FieldDef.bool("EspLoai5", "ESP Type 5 (Box Frame)"));
        s.add(FieldDef.bool("EspLoai6", "ESP Type 6 (Skeleton)"));
        s.add(FieldDef.bool("EspLoai7", "ESP Type 7 (Detail Info)"));
        s.add(FieldDef.bool("Esp7_SoLuong", "  Show Enemy Counter"));
        s.add(FieldDef.bool("Esp7_VuKhi", "  Show Enemy Weapon"));
        s.add(FieldDef.bool("Esp7_TuThe", "  Show Posture"));
        s.add(FieldDef.bool("EspLoai8", "ESP Type 8 (Head HP)"));
        s.add(FieldDef.bool("EspLoai9", "ESP Type 9 (VIP 9 Full)"));
        s.add(FieldDef.bool("EspAntenna", "ESP Antenna"));
        s.add(FieldDef.bool("EspOutline", "ESP Outline (HDR)"));

        s.add(FieldDef.header("ESP 9 DETAIL"));
        s.add(FieldDef.bool("Esp9_Name", "  Player Name"));
        s.add(FieldDef.bool("Esp9_HP", "  Health Bar"));
        s.add(FieldDef.bool("Esp9_Team", "  Team Color Box"));
        s.add(FieldDef.bool("Esp9_Weapon", "  Weapon Icon"));
        s.add(FieldDef.bool("Esp9_Distance", "  Distance"));
        s.add(FieldDef.bool("Esp9_Line", "  Snap Line"));
        s.add(FieldDef.bool("Esp9_Skeleton", "  Skeleton"));
        s.add(FieldDef.bool("Esp9_Box", "  Corner Box"));
        s.add(FieldDef.bool("Esp9_Chevron", "  Chevron"));
        s.add(FieldDef.bool("Esp9_Status", "  Status Open/Cover"));
        s.add(FieldDef.bool("Esp9_VisCheck", "  Visibility Check"));
        s.add(FieldDef.bool("Esp9_Count", "  Player/Bot Counter"));

        s.add(FieldDef.header("ESP V3 BOX"));
        s.add(FieldDef.bool("BoxESPEnable", "Enable Box ESP V3"));
        s.add(FieldDef.bool("BoxESP_bShowBox", "  Box Corner"));
        s.add(FieldDef.bool("BoxESP_bShowHealth", "  Health Bar"));
        s.add(FieldDef.bool("BoxESP_bShowWeapon", "  Weapon Name"));
        s.add(FieldDef.bool("BoxESP_bShowName", "  Player Name"));
        s.add(FieldDef.bool("BoxESP_bShowFlag", "  Nation Flag"));
        s.add(FieldDef.bool("BoxESP_bShowDist", "  Distance"));
        s.add(FieldDef.bool("BoxESP_bShowState", "  State"));
        s.add(FieldDef.bool("BoxESP_bShowLines", "  Snap Lines"));
        s.add(FieldDef.bool("BoxESP_bShowCounter", "  Counter"));

        s.add(FieldDef.header("VEHICLE ESP"));
        s.add(FieldDef.bool("EspVehicle", "Enable Vehicle ESP"));
        s.add(FieldDef.bool("EspVeh_Dacia", "  Dacia"));
        s.add(FieldDef.bool("EspVeh_UAZ", "  UAZ"));
        s.add(FieldDef.bool("EspVeh_Buggy", "  Buggy"));
        s.add(FieldDef.bool("EspVeh_Coupe", "  Coupe RB"));
        s.add(FieldDef.bool("EspVeh_Mirado", "  Mirado"));
        s.add(FieldDef.bool("EspVeh_Motor", "  Motor / Scooter"));
        s.add(FieldDef.bool("EspVeh_Other", "  Others"));

        s.add(FieldDef.header("ITEM ESP"));
        s.add(FieldDef.bool("EspItem_Master", "Enable Item ESP"));
        s.add(FieldDef.bool("EspItem_AR", "  AR"));
        s.add(FieldDef.bool("EspItem_Sniper", "  Sniper"));
        s.add(FieldDef.bool("EspItem_SMG", "  SMG"));
        s.add(FieldDef.bool("EspItem_Shotgun", "  Shotgun"));
        s.add(FieldDef.bool("EspItem_LMG", "  LMG"));
        s.add(FieldDef.bool("EspItem_Pistol", "  Pistol"));
        s.add(FieldDef.bool("EspItem_Melee", "  Melee"));
        s.add(FieldDef.bool("EspItem_Special", "  Special"));
        s.add(FieldDef.bool("EspItem_Scope", "  Scope"));
        s.add(FieldDef.bool("EspItem_Grenade", "  Grenade"));
        s.add(FieldDef.bool("EspItem_Med", "  Medkit"));

        s.add(FieldDef.header("BOMB / AIM WARNING"));
        s.add(FieldDef.bool("EspBomMaster", "Enable Bomb Master"));
        s.add(FieldDef.bool("EspItemBom", "  Ground Grenade ESP"));
        s.add(FieldDef.bool("EspActiveBom", "  Active Bomb Warning"));
        s.add(FieldDef.bool("EspAimWarning", "Enemy Aim Warning"));
        s.add(FieldDef.bool("EspAimWarningVisCheck", "  Visibility Check"));

        s.add(FieldDef.header("AIMBOT V2"));
        s.add(FieldDef.bool("AimTouchEnable", "Enable Aimbot V2"));
        s.add(FieldDef.bool("AimTouchHipfire", "  Hipfire"));
        s.add(FieldDef.bool("AimTouchHipIgKnock", "    Ignore Knock"));
        s.add(FieldDef.bool("AimTouchHipIgBot", "    Ignore Bot"));
        s.add(FieldDef.bool("AimTouchHipVisCheck", "    VisCheck"));
        s.add(FieldDef.bool("AimTouchSG", "  Shotgun"));
        s.add(FieldDef.bool("AimTouchSGAutoFire", "    Auto Fire"));
        s.add(FieldDef.bool("AimTouchScopeAll", "  Scope (all weapons)"));
        s.add(FieldDef.bool("AimTouchScopeSniper", "  Sniper Scope"));
        s.add(FieldDef.bool("AimTouchMortar", "  Mortar"));

        s.add(FieldDef.header("SMART BULLET TRACK"));
        s.add(FieldDef.bool("SmartBT_Enable", "Enable Smart BT"));
        s.add(FieldDef.bool("SmartBT_VisCheck", "  VisCheck"));
        s.add(FieldDef.bool("SmartBT_IgKnock", "  Ignore Knock"));
        s.add(FieldDef.bool("SmartBT_IgBot", "  Ignore Bot"));
        s.add(FieldDef.slider("SmartBT_MaxDist", "  Max Dist (m)", 50, 500));
        s.add(FieldDef.slider("SmartBT_Bone", "  Bone (1-6)", 1, 6));
        s.add(FieldDef.slider("SmartBT_Radius", "  Radius (cm)", 0, 80));
        s.add(FieldDef.slider("SmartBT_HitChance", "  Hit Chance %", 0, 100));

        s.add(FieldDef.header("SKIN MOD"));
        s.add(FieldDef.bool("ModSkin", "Mod Skin System"));
        s.add(FieldDef.bool("ModEmote", "Mod Emote"));
        s.add(FieldDef.bool("SkinDeadBox", "Deadbox Skin"));
        s.add(FieldDef.bool("SkinAttachment", "Attachment Skin"));
        s.add(FieldDef.bool("KillMessage", "Kill Message"));
        s.add(FieldDef.bool("KillCountUI", "Kill Counter UI"));

        s.add(FieldDef.header("UTILITY"));
        s.add(FieldDef.bool("UnlockFPS", "Unlock 165 FPS"));
        s.add(FieldDef.bool("IpadView", "iPad View"));
        s.add(FieldDef.bool("LessShake", "Less Scope Shake"));
        s.add(FieldDef.bool("RemoveGrass", "Remove Grass"));
        s.add(FieldDef.bool("RemoveTrees", "Remove Trees"));
        s.add(FieldDef.bool("RemoveFog", "Remove Fog"));
        s.add(FieldDef.bool("Crosshair", "Small Crosshair"));
        s.add(FieldDef.bool("Accuracy", "100% Accuracy"));
        s.add(FieldDef.bool("GodMode", "Fast Shoot (Legacy)"));

        s.add(FieldDef.header("NUMERIC VALUES"));
        s.add(FieldDef.slider("OuterSpeed", "  Aimbot Outer Speed", 1, 100));
        s.add(FieldDef.slider("InnerSpeed", "  Aimbot Inner Speed", 1, 100));
        s.add(FieldDef.slider("OuterRecoil", "  Recoil Comp Outer", 0, 50));
        s.add(FieldDef.slider("IpadViewFOV", "  iPad FOV", 90, 160));
        s.add(FieldDef.slider("AimTouchHipSpeed", "  Hip Speed", 1, 100));
        s.add(FieldDef.slider("AimTouchHipFOV", "  Hip FOV", 1, 100));
        s.add(FieldDef.slider("AimTouchHipDist", "  Hip Max Dist (m)", 5, 500));
        s.add(FieldDef.slider("AimTouchScopeSpeed", "  Scope Speed", 1, 100));
        s.add(FieldDef.slider("AimTouchScopeFOV", "  Scope FOV", 1, 100));
        s.add(FieldDef.slider("AimTouchScopeDist", "  Scope Max Dist (m)", 5, 500));
        s.add(FieldDef.slider("AimTouchSniperSpeed", "  Sniper Speed", 1, 100));
        s.add(FieldDef.slider("AimTouchSniperFOV", "  Sniper FOV", 1, 100));
        s.add(FieldDef.slider("AimTouchSniperDist", "  Sniper Max Dist (m)", 5, 500));
        s.add(FieldDef.slider("FastCarSpeed", "  Fast Car Speed", 60, 6000));
        s.add(FieldDef.slider("OutlineColor", "  Outline Color (1-6)", 1, 6));
        s.add(FieldDef.slider("ColorV3Hidden", "  Chams V3 Hidden", 1, 6));
        s.add(FieldDef.slider("ColorV3Visible", "  Chams V3 Visible", 1, 6));
        s.add(FieldDef.slider("ColorV3Thickness", "  Chams V3 Thickness", 1, 20));

        return s;
    }

    public static void buildFromConfig(
            Context ctx,
            LinearLayout root,
            LexusConfig cfg,
            CustomTextData custom) {

        LayoutInflater inf = LayoutInflater.from(ctx);
        for (FieldDef def : schema()) {
            switch (def.type) {
                case HEADER: {
                    View v = inf.inflate(R.layout.row_section, root, false);
                    ((TextView) v).setText(def.title);
                    root.addView(v);
                    break;
                }
                case BOOL: {
                    View v = inf.inflate(R.layout.row_toggle, root, false);
                    TextView label = v.findViewById(R.id.label);
                    Switch sw = v.findViewById(R.id.switchToggle);
                    label.setText(def.label);
                    sw.setChecked(getBool(cfg, custom, def));
                    sw.setOnCheckedChangeListener((bv, isChecked) ->
                            setBool(cfg, custom, def, isChecked));
                    root.addView(v);
                    break;
                }
                case SLIDER: {
                    View v = inf.inflate(R.layout.row_slider, root, false);
                    TextView label = v.findViewById(R.id.label);
                    TextView value = v.findViewById(R.id.value);
                    SeekBar bar = v.findViewById(R.id.seekBar);
                    label.setText(def.label);
                    bar.setMax(def.max - def.min);
                    int cur = getInt(custom, def);
                    int clamped = Math.max(def.min, Math.min(def.max, cur));
                    bar.setProgress(clamped - def.min);
                    value.setText(String.valueOf(clamped));
                    bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override public void onProgressChanged(SeekBar sb, int prog, boolean fromUser) {
                            int real = def.min + prog;
                            value.setText(String.valueOf(real));
                            if (fromUser) setInt(custom, def, real);
                        }
                        @Override public void onStartTrackingTouch(SeekBar sb) {}
                        @Override public void onStopTrackingTouch(SeekBar sb) {}
                    });
                    root.addView(v);
                    break;
                }
            }
        }
    }

    private static boolean getBool(LexusConfig cfg, CustomTextData custom, FieldDef def) {
        Object target = def.fromCustomData ? custom : cfg;
        try {
            Field f = target.getClass().getField(def.key);
            return f.getBoolean(target);
        } catch (Throwable t) { return false; }
    }

    private static void setBool(LexusConfig cfg, CustomTextData custom, FieldDef def, boolean v) {
        Object target = def.fromCustomData ? custom : cfg;
        try {
            Field f = target.getClass().getField(def.key);
            f.setBoolean(target, v);
        } catch (Throwable ignored) {}
    }

    private static int getInt(CustomTextData custom, FieldDef def) {
        try {
            Field f = custom.getClass().getField(def.key);
            Class<?> t = f.getType();
            if (t == int.class)    return f.getInt(custom);
            if (t == long.class)   return (int) f.getLong(custom);
            if (t == double.class) return (int) Math.round(f.getDouble(custom));
            if (t == float.class)  return (int) f.getFloat(custom);
            return 0;
        } catch (Throwable t) { return 0; }
    }

    private static void setInt(CustomTextData custom, FieldDef def, int v) {
        try {
            Field f = custom.getClass().getField(def.key);
            Class<?> t = f.getType();
            if (t == int.class)    f.setInt(custom, v);
            else if (t == long.class)   f.setLong(custom, v);
            else if (t == double.class) f.setDouble(custom, v);
            else if (t == float.class)  f.setFloat(custom, v);
        } catch (Throwable ignored) {}
    }
}
