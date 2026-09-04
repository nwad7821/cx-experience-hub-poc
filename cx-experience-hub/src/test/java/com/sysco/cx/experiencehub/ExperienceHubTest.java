package com.sysco.cx.experiencehub;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExperienceHubTest {

    @Test
    void resolvesWillCallFromInMemoryDataSource() {
        RulesDataSource ds = ctx -> new FeatureConfig(Map.of(
                "isStorefront", false,
                "isWillCallEnabled", true));
        ExperienceHub hub = ExperienceHub.builder().dataSource(ds).build();
        var ctx = ExperienceContext.of("alice", "site-001", "acct-100", "exp-bhnp");
        assertTrue(hub.isWillCallEnabled(ctx));
        assertFalse(hub.isStorefront(ctx));
    }

    @Test
    void failuresReturnEmptyConfig() {
        RulesDataSource ds = ctx -> { throw new RuntimeException("boom"); };
        ExperienceHub hub = ExperienceHub.builder().dataSource(ds).build();
        var ctx = ExperienceContext.of("x", "y", "z", "exp-usbl");
        assertFalse(hub.isWillCallEnabled(ctx));
    }
}
