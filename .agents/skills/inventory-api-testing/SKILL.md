---
name: inventory-api-testing
description: Run local Inventory REST migration checks against a seeded MySQL database.
---

# Inventory API runtime testing

## Devin Secrets Needed
None for the disposable local environment. Use the local database settings documented in the environment blueprint; never reuse test credentials in deployed environments.

## Startup
Read the environment blueprint for Java/Maven setup and MySQL seeding.
Check whether `inv-mysql` and port 8080 are already in use before starting services.
Run `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn spring-boot:run`.
Capture startup output and confirm the actual JVM and Boot version.
Existing sale_order foreign-key mismatch warnings may occur with the supplied dump; distinguish those from fatal startup failures.
Do not commit regenerated `target/` artifacts.

## Authentication fixtures
The current API permits unauthenticated fixture creation.
POST JSON to `/api/roles` with `roleName` and `roleDescription` (202).
POST JSON to `/api/users/{roleId}` with `userName`, plaintext test `password`, `firstName`, `lastName`, and `email` (202). The service BCrypt-encodes the password.
POST form parameters `userName` and `password` to `/api/login`, not JSON.
Assert access_token and refresh_token in JSON and matching headers; wrong password must return 401.
POST `/api/checktoken/{access_token}` verifies token user/roles.
Read controllers before cleanup: roles may have no delete route, and user deletion may be unsupported. Report remaining fixture ids.

## Persistence checks
Read `hibernate_sequence.next_val` before writes; assert consecutive ids across entity types and no new per-entity `_SEQ` tables.
Categories support create/list/get-by-id/get-by-name/update/delete.
Brands and stores expose list rather than individual GET.
Store creation is `/api/stores/{companyId}`; updates/deletes use storeId.
`/api/company` returns a singleton and nested stores, useful for relationship serialization.
Seeded products/supplied-products may be empty; empty array reads do not prove product-object serialization.
Always read after writes/deletes: a 2xx response alone can conceal a swallowed persistence error.
Check updated entity JSON because updates use Hibernate references.

## Evidence
This is a REST-only backend. Prefer shell HTTP response logs; use browser direct GETs only for supplementary visible JSON evidence.
Record only actual browser interactions. Browser JSON pretty-print and zoom can make individual records readable.
Use OPTIONS with Origin, Access-Control-Request-Method, and Access-Control-Request-Headers to verify CORS.
