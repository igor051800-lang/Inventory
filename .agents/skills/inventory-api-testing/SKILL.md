---
name: inventory-rest-runtime-testing
description: Run the Inventory REST API on real MySQL and verify authentication, linked entity CRUD, and legacy dump compatibility.
---

# Inventory REST runtime testing

## Devin Secrets Needed
None for isolated local testing. The committed local configuration expects MySQL root/password and database deva_inventory. Never reuse these defaults for a deployed environment.

## Environment
- Java 21 and Maven are required. Prefer the repository blueprint for installation and Maven mirror configuration.
- There is no frontend. Use shell HTTP requests for writes and login; browser GET JSON views are optional visual evidence.
- Start an isolated MySQL container (check ports/container names first):
  `docker run -d --name inventory-e2e-mysql -p 127.0.0.1:3306:3306 -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=deva_inventory mysql:8.4`
- Wait for final MySQL readiness, not the temporary initialization server. Confirm with `docker exec -e MYSQL_PWD=password inventory-e2e-mysql mysql -uroot -e 'SELECT VERSION()'`.
- Run `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B spring-boot:run '-Dspring-boot.run.jvmArguments=-Dspring.devtools.restart.enabled=false'`. Production config uses ddl-auto=update.
- For dump testing, use a separate database and a second app port; import `database/*.sql` before starting that app. Override `--server.port=8081 --spring.datasource.url=jdbc:mysql://localhost:3306/deva_inventory_legacy`.
- Inspect startup for `CommandAcceptanceException` even when the Started message appears. Hibernate can continue after failed foreign-key DDL.

## API flow
- All current endpoints are permitAll. POST `/api/roles`, `/api/users/{roleId}`, and `/api/categories` return **202**, while most other successful CRUD endpoints return **200**.
- Create your own user with a known fixture password; existing dump passwords are unknown. User creation BCrypt-encodes the password and creates its role link.
- Login uses form-encoded POST `/api/login`, fields **userName** and **password** (not username or JSON). Assert access_token and refresh_token in JSON match response headers; wrong password should be 401.
- Product prerequisite chain:
  1. POST `/api/suppliers`
  2. POST `/api/orders/{supplierId}`
  3. POST `/api/sale-orders/{orderId}`
  4. POST `/api/supplied-products/{saleOrderId}`
  5. Create category and brand.
  6. POST `/api/products/{supplierId}/{suppliedProductId}/{categoryId}/{brandId}`.
- Company/store/inventory chain: POST `/api/company`, `/api/stores/{companyId}`, `/api/inventories/{storeId}`. Company GET returns one object, not a list; use a fresh database for creating a second company scenario.
- Stock a product with PUT `/api/products/add-product-inventory/{productId}/{inventoryCode}`; stockStatus changes from `un-stocked` to `stocked`.
- Check JSON entity relationships for recursion and actual persisted values, not status codes alone.
- Services may swallow persistence exceptions. Verify create IDs and list/SQL readback; after DELETE returns `{"deleted":true}`, assert the row actually disappeared.
- Physical supplied-product table is `supplier_product`; its ID column is misspelled **suppliered_product_id**.
- The shared `hibernate_sequence` dump starts at 32. Verify inserts advance this table and preserve original rows, without creating entity-specific `_seq` tables.

## Evidence
Save full HTTP status, headers, bodies, SQL readbacks, and app logs. Record only actual browser interactions, not idle desktop while shell requests run. Chrome's JSON Pretty-print checkbox and zoom make live API responses readable. Clearly distinguish a failure observed on the branch from a regression proven against a baseline.
