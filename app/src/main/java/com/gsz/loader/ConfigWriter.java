package com.gsz.loader;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigWriter {

    private ConfigWriter() {}

    public static String buildLuaPayload(LexusConfig cfg, CustomTextData txt) {
        StringBuilder sb = new StringBuilder(8 * 1024);
        sb.append("-- GSZ Loader external config (auto-generated)\n");
        sb.append("return {\n");
        sb.append("LexusConfig = {\n");
        appendObject(sb, cfg, 2);
        sb.append("},\n");
        sb.append("CustomTextData = {\n");
        appendObject(sb, txt, 2);
        sb.append("}\n}\n");
        return sb.toString();
    }

    private static void appendObject(StringBuilder sb, Object obj, int indent) {
        String pad = repeat(" ", indent);
        for (Field f : obj.getClass().getFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            try {
                Object v = f.get(obj);
                String sval;
                if (v instanceof Boolean) sval = v.toString();
                else if (v instanceof Integer) sval = v.toString();
                else if (v instanceof Long)    sval = v.toString();
                else if (v instanceof Double)  sval = v.toString();
                else if (v instanceof Float)   sval = v.toString();
                else sval = "nil";
                sb.append(pad).append('[').append('"').append(f.getName())
                        .append('"').append(']').append(" = ")
                        .append(sval).append(",\n");
            } catch (IllegalAccessException ignored) {}
        }
    }

    public static boolean applyLuaToConfig(String content,
                                           LexusConfig cfg,
                                           CustomTextData txt) {
        if (content == null || content.isEmpty()) return false;
        int lexusStart = content.indexOf("LexusConfig");
        int customStart = content.indexOf("CustomTextData");
        if (lexusStart < 0 || customStart < 0) return false;
        String lexusBlock = content.substring(lexusStart, customStart);
        String customBlock = content.substring(customStart);
        int n1 = applyBlock(lexusBlock, cfg);
        int n2 = applyBlock(customBlock, txt);
        return n1 + n2 > 0;
    }

    private static final Pattern ENTRY = Pattern.compile(
            "\\[\"([A-Za-z_][A-Za-z0-9_]*)\"\\]\\s*=\\s*([^,}\\n]+)");

    private static int applyBlock(String block, Object obj) {
        Matcher m = ENTRY.matcher(block);
        int applied = 0;
        while (m.find()) {
            String key = m.group(1);
            String raw = m.group(2).trim();
            try {
                Field f = obj.getClass().getField(key);
                Class<?> t = f.getType();
                if (t == boolean.class) {
                    f.setBoolean(obj, "true".equalsIgnoreCase(raw));
                } else if (t == int.class) {
                    f.setInt(obj, (int) Double.parseDouble(raw));
                } else if (t == long.class) {
                    f.setLong(obj, (long) Double.parseDouble(raw));
                } else if (t == double.class) {
                    f.setDouble(obj, Double.parseDouble(raw));
                } else if (t == float.class) {
                    f.setFloat(obj, Float.parseFloat(raw));
                } else {
                    continue;
                }
                applied++;
            } catch (Throwable ignored) {}
        }
        return applied;
    }

    private static String repeat(String s, int n) {
        StringBuilder b = new StringBuilder(s.length() * n);
        for (int i = 0; i < n; i++) b.append(s);
        return b.toString();
    }
}
