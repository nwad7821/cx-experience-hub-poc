package com.sysco.cx.experiencehub;

/**
 * Pluggable data source that returns the effective feature config for a context.
 * Implementations: HttpRulesDataSource (default), or any custom (DB, flag tool, etc.).
 */
public interface RulesDataSource {
    FeatureConfig fetch(ExperienceContext ctx);
}
