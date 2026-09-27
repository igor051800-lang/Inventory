# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.1-SNAPSHOT] - 2026-09-27

Migration from Java 8 + Spring Boot 2.6.4 to Java 21 + Spring Boot 3.3.13
(Spring Framework 6, Spring Security 6, Hibernate 6, Jakarta EE 10).

### Changed

- **Build (`pom.xml`)**
  - `spring-boot-starter-parent` `2.6.4` -> `3.3.13`.
  - `java.version` `1.8` -> `21`.
  - MySQL JDBC driver `mysql:mysql-connector-java` -> `com.mysql:mysql-connector-j`
    (version managed by the Boot parent).
  - `com.auth0:java-jwt` `3.19.1` -> `4.4.0`.
- **Jakarta namespace** – all `javax.persistence.*` and `javax.servlet.*` imports
  replaced with `jakarta.persistence.*` / `jakarta.servlet.*` in every entity under
  `model/` (`Address`, `Brand`, `Category`, `Company`, `Customer`, `Inventory`,
  `Order`, `OrderProduct`, `Product`, `Role`, `SaleOrder`, `Store`,
  `SuppliedProduct`, `Supplier`, `User`) and in `filter/CustomAuthenticationFilter`.
- **Security configuration (`config/WebSecurity`)** – rewritten for Spring Security 6:
  - No longer extends the removed `WebSecurityConfigurerAdapter`; security is now
    declared as a `SecurityFilterChain` `@Bean` using the lambda DSL.
  - `AuthenticationManager` is exposed as a `@Bean` obtained from
    `AuthenticationConfiguration`.
  - Authentication is provided by a `DaoAuthenticationProvider` `@Bean` wired with
    the existing `UserDetailsService` and a `BCryptPasswordEncoder`.
  - Behaviour is preserved: stateless sessions, CSRF disabled, the same CORS
    configuration, `CustomAuthenticationFilter` processing `POST /api/login`, and
    every other endpoint `permitAll`.
- **`src/main/resources/application.properties`**
  - Removed the explicit `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect`
    (deprecated in Hibernate 6; the dialect is auto-detected from the JDBC connection).
  - Added `spring.jpa.properties.hibernate.id.db_structure_naming_strategy=single`
    so Hibernate 6 keeps using the shared `hibernate_sequence` table for
    `GenerationType.AUTO` ids (see Migration notes).
- **README** – prerequisite updated from Java 8 to Java 21.

### Added

- `com.h2database:h2` (test scope) so the Spring context-load test can run without a
  database server.
- `src/test/resources/application.properties` – test profile pointing at an in-memory
  H2 database in MySQL compatibility mode
  (`jdbc:h2:mem:deva_inventory;MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=USER`,
  `ddl-auto=create-drop`), so `mvn verify` needs no running MySQL instance.
- `.gitignore` ignoring `target/`.

### Removed

- `filter/CustomAuthorizationFilter` – the class was entirely commented out and unused.
- Explicit `com.fasterxml.jackson.core:jackson-databind` dependency – it is already
  provided transitively (and version-managed) by `spring-boot-starter-web`.
- Previously committed Maven build output under `target/` (compiled `.class` files
  and `maven-status` listings).

### Migration notes / behavioral differences

- **Runtime requirement** – a Java 21 JDK is now required to build and run the
  application. Java 8 is no longer supported.
- **ID generation / `hibernate_sequence`** – Hibernate 6 changed the default naming
  strategy for `GenerationType.AUTO`/`SEQUENCE` ids: it creates one `<table>_seq`
  sequence table per entity, each starting at 1. Against an existing database that
  was populated by the Java 8 build (which used the single shared `hibernate_sequence`
  table) this would produce duplicate primary keys. Setting
  `hibernate.id.db_structure_naming_strategy=single` restores the legacy behaviour and
  keeps the existing `hibernate_sequence` table in use. Do not remove this property
  unless the id generators / data are migrated accordingly.
- **Trailing-slash request matching** – Spring Framework 6 no longer matches a
  trailing-slash URL to a path without one. For example `GET /api/brands/` now
  returns `404`; clients must call `GET /api/brands`. This applies to all controller
  endpoints.
- **`sale_order` DDL warnings** – with `spring.jpa.hibernate.ddl-auto=update`,
  Hibernate logs foreign-key DDL warnings for the `sale_order` table at startup.
  These are pre-existing schema-mapping issues in the entity model, not introduced by
  this migration, and do not prevent startup.
- **Deprecated `JpaRepository.getById`** – Spring Data JPA 3 deprecates
  `getById(ID)` in favour of `getReferenceById(ID)`. The service layer still calls
  `getById`; it continues to work but emits deprecation warnings at compile time and
  should be migrated in a follow-up.
- **JWT library** – `java-jwt` 4.x drops Java 8 support and changes some exception
  types/packages; the token creation code in `CustomAuthenticationFilter` was
  verified against 4.4.0.
- **Dialect** – MySQL dialect selection is now automatic. If you need to pin it,
  use `org.hibernate.dialect.MySQLDialect` (the versioned `MySQL8Dialect` class is
  deprecated in Hibernate 6).
- **Testing** – `mvn verify` runs the context-load test against H2. Running against a
  real MySQL database is still done through `src/main/resources/application.properties`
  when starting the application normally.
