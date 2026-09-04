package com.sysco.cx.experiencehub;

import java.util.Objects;

/**
 * The canonical context every logged-in request already provides.
 * Matches the proposal: userId + siteId + accountId + experience.
 */
public final class ExperienceContext {
    private final String userId;
    private final String siteId;
    private final String accountId;
    private final String experience;

    private ExperienceContext(String userId, String siteId, String accountId, String experience) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.siteId = Objects.requireNonNull(siteId, "siteId");
        this.accountId = Objects.requireNonNull(accountId, "accountId");
        this.experience = Objects.requireNonNull(experience, "experience");
    }

    public static ExperienceContext of(String userId, String siteId, String accountId, String experience) {
        return new ExperienceContext(userId, siteId, accountId, experience);
    }

    public String userId()     { return userId; }
    public String siteId()     { return siteId; }
    public String accountId()  { return accountId; }
    public String experience() { return experience; }

    /** Cache key. */
    public String key() {
        return userId + "|" + siteId + "|" + accountId + "|" + experience;
    }

    @Override public String toString() {
        return "ExperienceContext{" + key() + "}";
    }
}
