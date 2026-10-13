package com.gsz.loader;

public class FieldDef {
    public enum Type { BOOL, SLIDER, HEADER }

    public final Type type;
    public final String key;
    public final String label;
    public final int min, max;
    public final boolean fromCustomData;
    public final String title;

    private FieldDef(Type t, String key, String label, int min, int max,
                     boolean fromCustom, String title) {
        this.type = t;
        this.key = key;
        this.label = label;
        this.min = min;
        this.max = max;
        this.fromCustomData = fromCustom;
        this.title = title;
    }

    public static FieldDef header(String title) {
        return new FieldDef(Type.HEADER, null, null, 0, 0, false, title);
    }
    public static FieldDef bool(String key, String label) {
        return new FieldDef(Type.BOOL, key, label, 0, 0, false, null);
    }
    public static FieldDef boolCustom(String key, String label) {
        return new FieldDef(Type.BOOL, key, label, 0, 0, true, null);
    }
    public static FieldDef slider(String key, String label, int min, int max) {
        return new FieldDef(Type.SLIDER, key, label, min, max, true, null);
    }
}
