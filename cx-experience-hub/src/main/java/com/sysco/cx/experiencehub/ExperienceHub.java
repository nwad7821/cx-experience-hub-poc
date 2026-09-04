package com.sysco.cx.experiencehub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Public entry point of the shared library. Consumers call this.
 *
 * Example:
 * <pre>
 *   ExperienceHub hub = ExperienceHub.builder()
 *       .dataSource(new HttpRulesDataSource("http://hub:8081"))
 *       .build();
 *   boolean willCall = hub.isWillCallEnabled(ctx);
 * </pre>
 */
public final class ExperienceHub {

    private static final Logger log = LoggerFactory.getLogger(ExperienceHub.class);

    /** Standard feature keys under the cx-experience-hub namespace. */
    public static final class Features {
        public static final String IS_STOREFRONT       = "isStorefront";
        public static final String IS_WILL_CALL_ENABLED = "isWillCallEnabled";
        private Features() {}
    }

    private final RulesDataSource dataSource;
    private final boolean defaultOnFailure;
    private final ConcurrentMap<String, FeatureConfig> cache = new ConcurrentHashMap<>();

    private ExperienceHub(Builder b) {
        this.dataSource = b.dataSource;
        this.defaultOnFailure = b.defaultOnFailure;
    }

    public static Builder builder() { return new Builder(); }

    public FeatureConfig getFeatures(ExperienceContext ctx) {
        return cache.computeIfAbsent(ctx.key(), k -> safeFetch(ctx));
    }

    public boolean isFeatureEnabled(ExperienceContext ctx, String feature) {
        return getFeatures(ctx).isEnabled(feature);
    }

    public boolean isWillCallEnabled(ExperienceContext ctx) {
        return isFeatureEnabled(ctx, Features.IS_WILL_CALL_ENABLED);
    }

    public boolean isStorefront(ExperienceContext ctx) {
        return isFeatureEnabled(ctx, Features.IS_STOREFRONT);
    }

    /** Test / admin hook. */
    public void invalidate() { cache.clear(); }

    private FeatureConfig safeFetch(ExperienceContext ctx) {
        try {
            FeatureConfig cfg = dataSource.fetch(ctx);
            return cfg == null ? FeatureConfig.EMPTY : cfg;
        } catch (Exception e) {
            log.warn("cx-experience-hub lookup failed for {} - defaulting to {}", ctx, defaultOnFailure, e);
            return FeatureConfig.EMPTY;
        }
    }

    public static final class Builder {
        private RulesDataSource dataSource;
        private boolean defaultOnFailure = false;
        public Builder dataSource(RulesDataSource ds) { this.dataSource = ds; return this; }
        public Builder defaultOnFailure(boolean v) { this.defaultOnFailure = v; return this; }
        public ExperienceHub build() {
            if (dataSource == null) throw new IllegalStateException("RulesDataSource required");
            return new ExperienceHub(this);
        }
    }
}
