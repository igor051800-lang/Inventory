---
name: inventory-rest-testing
description: Run Inventory's REST API locally with Java 21 and an isolated H2 database, and verify relationship persistence and JWT login.
---

# Inventory REST runtime testing

## Runtime setup

Use a Java 21 JDK and Maven. Check their installed locations rather than assuming the system Java is appropriate; this environment has used `/home/ubuntu/jdk21` and `/home/ubuntu/maven/bin`.

H2 is a test-scoped dependency, so local API startup needs the test classpath. Disable DevTools restart for predictable startup:

```sh
export JAVA_HOME=/home/ubuntu/jdk21
export PATH="$JAVA_HOME/bin:/home/ubuntu/maven/bin:$PATH"
mvn -q spring-boot:run \
  -Dspring-boot.run.useTestClasspath=true \
  '-Dspring-boot.run.jvmArguments=-Dspring.devtools.restart.enabled=false' \
  '-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:h2:mem:e2e;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.username=sa --spring.datasource.password= --spring.jpa.hibernate.ddl-auto=create'
```

This creates disposable data. Never apply the `create` setting to a persistent database. MySQL-dialect foreign-key drop warnings can occur against fresh H2; distinguish these from request-time exceptions and verify the application actually starts.

If Maven Central is rate-limited, the Google mirror is `https://maven-central.storage-download.googleapis.com/maven2/`.

## HTTP workflow

- The application is REST-only on port 8080. Read controller mappings before constructing requests.
- Create a role and user through the API for local tests; current security intentionally permits all endpoints.
- Login is form-encoded `POST /api/login`, fields `userName` and `password`, not JSON. Verify JWT roles as well as successful authentication.
- Build a connected company/store/inventory and supplier/order/sale-order/supplied-product/product graph. Record response IDs rather than assuming per-entity sequences.
- Product reception changes supplied status to `recieved`; stocking uses `PUT /api/products/add-product-inventory/{productId}/{inventoryCode}`.
- `GET /api/sale-orders/{id}` expects an order ID, not a sale-order ID.
- Verify mutations and deletes with fresh reads. A 200 or `{"deleted":true}` alone does not prove persistence.
- Empty sale-order list/update responses and unsupported user mutations may be existing application behavior. Compare with the base branch before attributing failures to an upgrade.
- Fresh H2 IDs do not establish compatibility with an existing MySQL schema or sequence table.

## Devin Secrets Needed

None for disposable local H2 API tests. External database testing requires separately authorized connection settings; do not assume production access.
