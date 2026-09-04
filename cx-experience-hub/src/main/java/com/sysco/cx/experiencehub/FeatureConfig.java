package com.sysco.cx.experiencehub;

import java.util.Collections;
import java.util.Map;

/**
 * Config JSON returned by the central data source, e.g.
 * { "isStorefront": false, "isWillCallEnabled": true }
 */
public final class FeatureConfig {
    public static final FeatureConfig EMPTY = new FeatureConfig(Collections.emptyMap());

    private final Map<String, Object> values;

    public FeatureConfig(Map<String, Object> values) {
        this.values = values == null ? Collections.emptyMap() : Collections.unmodifiableMap(values);
    }

    public Map<String, Object> asMap() { return values; }

    public boolean isEnabled(String feature) {
        Object v = values.get(feature);
        if (v instanceof Boolean b) return b;
        if (v instanceof String s)  return Boolean.parseBoolean(s);
        return false;
    }

    public String getString(String feature) {
        Object v = values.get(feature);
        return v == null ? null : String.valueOf(v);
    }
}
