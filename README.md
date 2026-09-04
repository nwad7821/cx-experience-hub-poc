# cx-experience-hub PoC

Proof-of-Concept for the **Centralized Experience-Based Feature Enablement** proposal.

This repository shows how a shared package (`cx-experience-hub`) can answer questions like
`isWillCallEnabled(userId, siteId, accountId, experience)` for any consumer (auth server,
frontends, order service) using a **single central data source** keyed by
`userId + siteId + accountId + experience`.

## Modules

| Module | Purpose | Port |
|---|---|---|
| `cx-experience-hub` | Shared Java library. Exposes `ExperienceHub.isFeatureEnabled(...)` and `isWillCallEnabled(...)`. Uses a pluggable `RulesDataSource` (HTTP + in-memory cache). | (library) |
| `cx-experience-hub-service` | Spring Boot service = the **central data source**. Stores rules keyed by `userId+siteId+accountId+experience` and returns config JSON like `{"isStorefront":false,"isWillCallEnabled":true}`. | 8081 |
| `cx-auth-server-poc` | Login stub. After login, uses `cx-experience-hub` to decide which app to redirect the user to (`mss-frontend` vs `cx-storefront`) based on rules. | 8082 |
| `mss-order-service-poc` | Order service. On order submit, uses `cx-experience-hub` to validate that Will Call is enabled for the user context. | 8083 |
| `mss-frontend-poc` | Minimal HTML page that logs in via auth server and queries `isWillCallEnabled` before showing the Will Call UI. | 8080 |

## Architecture

```
            ┌──────────────────────┐
            │ cx-experience-hub    │  (central data source, DB-backed)
            │ -service   :8081     │
            └──────────▲───────────┘
                       │ HTTP (rules lookup)
     ┌─────────────────┼─────────────────┐
     │                 │                 │
┌────┴───────┐  ┌──────┴───────┐  ┌──────┴────────┐
│ cx-auth    │  │ mss-frontend │  │ mss-order-svc │
│ -server    │  │  (browser)   │  │               │
│ (uses lib) │  │ (uses lib via│  │ (uses lib)    │
│            │  │  auth-server)│  │               │
└────────────┘  └──────────────┘  └───────────────┘
         All embed the `cx-experience-hub` Java library
```

## Key API (shared library)

```java
ExperienceContext ctx = ExperienceContext.of(userId, siteId, accountId, experience);

boolean willCall  = experienceHub.isWillCallEnabled(ctx);
boolean generic   = experienceHub.isFeatureEnabled(ctx, "isWillCallEnabled");
FeatureConfig all = experienceHub.getFeatures(ctx); // full JSON config
```

## End-to-end flow (Will Call example)

1. User logs in via `cx-auth-server-poc` from `mss-frontend-poc` (or `cx-storefront`).
2. `cx-auth-server-poc` calls `experienceHub.getFeatures(ctx)` to decide the target app:
   - `isStorefront: true` → redirect to `cx-storefront`
   - `isStorefront: false` → redirect to `mss-frontend`
3. After redirect, `mss-frontend-poc` calls `/api/features/willcall` (backed by the hub) to
   decide whether to render the Will Call UI.
4. When an order is submitted, `mss-order-service-poc` calls
   `experienceHub.isWillCallEnabled(ctx)` to authorize the Will Call selection.

## Seeded demo data

See `cx-experience-hub-service/src/main/resources/seed-rules.json`.

| userId | siteId | accountId | experience | isStorefront | isWillCallEnabled |
|---|---|---|---|---|---|
| `alice` | `site-001` | `acct-100` | `exp-bhnp` | false | true  |
| `bob`   | `site-001` | `acct-100` | `exp-bhnp` | false | false |
| `carol` | `site-002` | `acct-200` | `exp-usbl` | true  | true  |
| `*`     | `*`        | `*`       | `exp-usbl` | true  | false |

Wildcard `*` = fallback match. Most-specific match wins.

## Run locally

```bash
# Build everything
mvn -q -DskipTests package

# Or run via docker-compose
docker compose up --build
```

Then open http://localhost:8080 and log in as `alice` / `bob` / `carol`.

## Naming

All features live under the `cx-experience-hub:x.x.x` namespace per the proposal.
