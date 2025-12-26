# Caching Overview

This document explains how caching works in this project (E-Commerce), what is currently cached, how to operate/update caches, and how to add server-side caching if you want to improve performance further.

---

## 1) Current caching layers (what's in place today) ✅

- Browser / CDN for static assets and image hosting

  - Product and category images are uploaded to Supabase Storage and served via public URLs (e.g., `https://<supabase>/storage/v1/object/public/...`). These URLs are cacheable by CDN and browsers which provides fast delivery.
  - Note: cached images may persist until the object path changes or CDN cache is purged.

- API responses: explicitly _not_ cached by browsers

  - The backend registers a `NoCacheFilter` that sets strong no-cache headers for any path that starts with `/api/`.
  - File: `src/main/java/za/ac/styling/filter/NoCacheFilter.java`
  - Headers set: `Cache-Control: no-store, no-cache, must-revalidate, private`, `Pragma: no-cache`, `Expires: 0`.
  - This avoids accidental caching of JSON API responses by browsers or intermediate proxies.

- Frontend in-memory state (Zustand store)

  - The frontend uses a Zustand store (`client-hub-portal/.../productStore.ts`) to cache categories and product data in memory for the lifetime of the page.
  - Example behavior: the admin pages call `fetchCategories()` and the store holds the categories until the page is refreshed / store updated.
  - The admin UI already clears the frontend category cache after create/update/delete by setting `categories: []` and calling `fetchCategories()` to force refetch.

- Migration utilities and image storage
  - There is a `MigrateImagesToSupabase` utility used to move legacy blobs into Supabase (useful when converting DB-stored images into CDN-backed public URLs).

---

## 2) How caching affects typical flows (practical notes) 💡

- Uploading a category or product image

  - Image is uploaded to Supabase Storage and a public URL is stored in the DB.
  - Because the image is served via a public CDN URL, it will be cached; to ensure clients see the new image after an overwrite, either:
    - Upload with a new filename (the code generates UUID filenames by default), or
    - Change the object path (e.g., include a timestamp/version) or
    - Purge CDN cache (if supported) or append a version query param to the URL.

- Creating or updating categories

  - Backend: No server-side cache for categories by default (calls `categoryRepository.findAll()` directly).
  - Frontend: category list is stored in the Zustand store; the admin UI refreshes it after changes.

- API responses are intentionally uncached by browsers (NoCacheFilter). If you add server-side caching, make sure cache entries are invalidated appropriately.

---

## 3) How to add server-side caching (Spring Boot) — recommended approach 🔧

You can add a caching layer on the server to reduce DB load for read-heavy endpoints (e.g., categories, products list). Two popular options:

A) In-memory with Caffeine (fast, local to app)

- Add dependencies:

  - `org.springframework.boot:spring-boot-starter-cache`
  - `com.github.ben-manes.caffeine:caffeine`

- Enable caching in your app (e.g. in main application class or config):

```java
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Application { }
```

- Provide a Caffeine CacheManager bean:

```java
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("categories", "products");
    cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000));
    return cacheManager;
}
```

- Annotate service methods:

```java
@Cacheable("categories")
public List<Category> getAll() { return categoryRepository.findAll(); }

@CacheEvict(value = "categories", allEntries = true)
public Category create(Category c) { return categoryRepository.save(c); }
```

B) Distributed caching with Redis (recommended for multi-instance)

- Add `spring-boot-starter-data-redis` and configure `RedisConnectionFactory`.
- Use `RedisCacheManager` instead of `CaffeineCacheManager`.
- Same `@Cacheable` / `@CacheEvict` annotations can be used.

---

## 4) Invalidation & consistency (best practices) ⚠️

- Always evict relevant caches after writes (create/update/delete). Use `@CacheEvict` with `allEntries=true` when the entire set must be refreshed, or evict by key for finer control.
- For image uploads that may overwrite the same path, use unique filenames or change path/version so clients get a new URL (avoid stale CDN caches). If you truly need to replace the object in-place, use the storage provider's CDN purge API (if available).
- Consider short TTLs (minutes) for mutable datasets and longer TTLs for static lookups.

---

## 5) How to test & debug caching

- Add Spring Boot Actuator and expose cache endpoints to view cache metrics (depending on cache implementation).
- Log cache hits/misses in service methods during development.
- For frontend, open DevTools Network tab — check cached image responses and their `Age`/`Cache-Control` headers.
- To verify API responses are not cached by browsers, inspect response headers from any `/api/*` call and confirm `Cache-Control: no-store, no-cache, ...`.

---

## 6) Quick checklist & recommendations ✅

- If your app is single-instance and you want simple low-latency caching: use Caffeine.
- If you run multiple app instances or use autoscaling: use Redis so caches are shared.
- Always evict cache entries on writes and handle image versioning to prevent CDN staleness.
- Keep API JSON responses uncached by browsers (NoCacheFilter) to avoid stale data in user agents.

---

Done: I added server-side caching using Caffeine and enabled Actuator monitoring.

### What I implemented (quick summary)

- **Enabled caching** by adding `@EnableCaching` and a `CacheManager` that uses **Caffeine** with a 10-minute TTL and max size 1000.
- **Annotated** `CategoryServiceImpl` with `@Cacheable("categories")` for reads and `@CacheEvict(value = "categories", allEntries = true)` on writes (create/update/delete and state changes).
- **Added** Spring Boot Actuator and exposed `health`, `info`, and `metrics` endpoints for basic monitoring.

### Files changed

- `pom.xml` — added `spring-boot-starter-cache`, `caffeine`, and `spring-boot-starter-actuator` dependencies
- `src/main/java/za/ac/styling/Main.java` — added `@EnableCaching`
- `src/main/java/za/ac/styling/config/CacheConfig.java` — new Caffeine `CacheManager` bean
- `src/main/java/za/ac/styling/service/impl/CategoryServiceImpl.java` — added `@Cacheable/@CacheEvict` annotations
- `src/main/resources/application.properties` — configured `spring.cache.type=caffeine` and actuator exposure

### How to validate (quick test)

1. Start the backend.
2. Call `GET /api/categories/getAll` once to populate the cache.
3. Call it again and measure response time — should be faster and hit the cache.
4. Create/update/delete a category and call `GET /api/categories/getAll` again — cached entries are evicted and the result should reflect the change.
5. Check actuator metrics at `GET /actuator/metrics` and search for cache-related metrics (e.g. `cache.gets`, `cache.puts`, if present).

If you'd like, I can also:

- Add a small integration test that exercises the cache behavior, or
- Wire up Micrometer cache metrics more explicitly or add Redis for distributed caching.

Tell me which follow-up you'd like next. 👍
