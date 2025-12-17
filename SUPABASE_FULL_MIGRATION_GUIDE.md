# Full Supabase Migration Guide
## Moving from MySQL to Supabase Database + Storage

---

## 🎯 Migration Overview

**Current Architecture:**
- MySQL database (products, orders, users, images metadata)
- Supabase Storage (image files only)

**Target Architecture:**
- ✅ **Supabase PostgreSQL Database** (ALL data - products, orders, users, image URLs)
- ✅ **Supabase Storage** (image files)
- ❌ No more MySQL

---

## 📋 Migration Steps

### Phase 1: Set Up Supabase Database (10 minutes)

#### 1. Create Supabase Project
1. Go to https://supabase.com
2. Click **"New Project"**
3. Fill in:
   - **Project Name**: e-commerce-db
   - **Database Password**: (save this securely!)
   - **Region**: Choose closest to your users
4. Click **"Create new project"**
5. Wait 2-3 minutes for provisioning

#### 2. Get Database Credentials
In your Supabase dashboard:
1. Click **"Project Settings"** (gear icon)
2. Go to **"Database"** section
3. Copy these values:

```
Host: db.xxxxxx.supabase.co
Database: postgres
Port: 5432
User: postgres
Password: [your database password]
```

**Connection String:**
```
postgresql://postgres:[PASSWORD]@db.xxxxxx.supabase.co:5432/postgres
```

#### 3. Get Storage Credentials
In **"Project Settings"** → **"API"**:
```
Project URL: https://xxxxxx.supabase.co
anon/public key: eyJhbGc...
service_role key: eyJhbGc... (keep secret!)
```

---

### Phase 2: Update Spring Boot Configuration (5 minutes)

#### 1. Update `pom.xml` - Add PostgreSQL Driver

```xml
<!-- Remove MySQL driver -->
<!-- <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency> -->

<!-- Add PostgreSQL driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### 2. Update `application.properties`

**Replace MySQL configuration:**
```properties
# OLD MySQL (REMOVE THESE)
# spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
# spring.datasource.username=root
# spring.datasource.password=yourpassword
# spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# NEW Supabase PostgreSQL
spring.datasource.url=jdbc:postgresql://db.xxxxxx.supabase.co:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=YOUR_DATABASE_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Connection Pool (important for Supabase)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000

# Hibernate settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Supabase Storage (keep existing)
supabase.url=https://xxxxxx.supabase.co
supabase.anon.key=YOUR_ANON_KEY
supabase.service.key=YOUR_SERVICE_ROLE_KEY
supabase.bucket.product.images=product-images
supabase.bucket.review.images=review-images

# File upload limits
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
```

---

### Phase 3: Create Storage Buckets (3 minutes)

1. In Supabase dashboard, click **"Storage"**
2. Click **"New bucket"**
3. Create bucket: **`product-images`**
   - Name: `product-images`
   - Public: ✅ **Yes** (so images load in browser)
   - Click **"Create bucket"**

4. Create bucket: **`review-images`**
   - Name: `review-images`
   - Public: ✅ **Yes**
   - Click **"Create bucket"**

#### Set Storage Policies (Allow Public Read)

For each bucket, click **bucket name** → **"Policies"** → **"New Policy"**:

```sql
-- Policy: Public read access
CREATE POLICY "Public read access"
ON storage.objects FOR SELECT
TO public
USING (bucket_id = 'product-images');

-- Policy: Authenticated users can upload
CREATE POLICY "Authenticated upload"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (bucket_id = 'product-images');

-- Policy: Users can delete own uploads
CREATE POLICY "Authenticated delete"
ON storage.objects FOR DELETE
TO authenticated
USING (bucket_id = 'product-images');
```

Repeat for `review-images` bucket.

---

### Phase 4: Migrate Database Schema (2 minutes)

**Good news:** Hibernate will auto-create tables in PostgreSQL!

1. **Run your Spring Boot application:**
   ```bash
   cd E_Commerce
   mvn spring-boot:run
   ```

2. **Hibernate will automatically create all tables** in Supabase PostgreSQL:
   - product
   - product_image
   - review
   - review_image
   - customer
   - orders
   - order_item
   - cart
   - cart_item
   - payment
   - etc.

3. **Check tables created:**
   - Go to Supabase dashboard → **"Table Editor"**
   - You should see all your tables!

---

### Phase 5: Migrate Existing Data (Optional - if you have MySQL data)

#### Option A: Export MySQL → Import to PostgreSQL (Manual)

**Step 1: Export from MySQL**
```bash
# Export data (without BLOB columns for now)
mysqldump -u root -p ecommerce_db \
  --no-create-info \
  --skip-triggers \
  --complete-insert \
  --ignore-table=ecommerce_db.product_image \
  --ignore-table=ecommerce_db.review_image \
  > mysql_data.sql
```

**Step 2: Convert MySQL SQL to PostgreSQL SQL**

Common changes needed:
```sql
-- MySQL: AUTO_INCREMENT
-- PostgreSQL: SERIAL or IDENTITY

-- MySQL: TINYINT(1) for boolean
-- PostgreSQL: BOOLEAN

-- MySQL: DATETIME
-- PostgreSQL: TIMESTAMP

-- MySQL backticks: `table_name`
-- PostgreSQL double quotes: "table_name" (or remove quotes)
```

**Step 3: Import to Supabase**

1. In Supabase dashboard → **"SQL Editor"**
2. Paste your converted SQL
3. Click **"Run"**

#### Option B: Use Migration Script (Programmatic)

Create `MigrateToSupabase.java`:

```java
@Component
@Profile("migration") // Only run with --spring.profiles.active=migration
public class MigrateToSupabase implements CommandLineRunner {

    // OLD MySQL connection
    @Qualifier("mysqlDataSource")
    private DataSource mysqlDataSource;
    
    // NEW Supabase connection
    @Qualifier("supabaseDataSource")
    private DataSource supabaseDataSource;
    
    @Override
    public void run(String... args) {
        migrateProducts();
        migrateCustomers();
        migrateOrders();
        migrateImages(); // Upload BLOBs to Storage, save URLs to PostgreSQL
    }
    
    private void migrateProducts() {
        // Read from MySQL, write to PostgreSQL
    }
}
```

---

### Phase 6: Update Entity Classes for PostgreSQL

#### Minor Changes Needed

**1. ID Generation Strategy**

PostgreSQL prefers `IDENTITY` over `AUTO`:

```java
// Before (MySQL)
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private Long id;

// After (PostgreSQL) - more explicit
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

**2. Remove MySQL-specific annotations**

```java
// Remove columnDefinition for LONGBLOB
// Before:
@Lob
@Column(columnDefinition = "LONGBLOB")
private byte[] imageData;

// After (or just remove this field entirely if using Supabase Storage):
// Remove the field or make it:
@Lob
private byte[] imageData; // PostgreSQL uses BYTEA type
```

**3. Boolean fields**

PostgreSQL handles booleans natively:

```java
// Works in both MySQL and PostgreSQL
@Column(name = "is_active")
private Boolean isActive;
```

---

### Phase 7: Test Application (5 minutes)

**1. Start Application**
```bash
mvn clean install
mvn spring-boot:run
```

**2. Check Logs**
Look for:
```
Hibernate: create table product (...)
Hibernate: create table product_image (...)
✅ All tables created successfully
```

**3. Test Endpoints**

**Create a product:**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "price": 99.99,
    "description": "Testing Supabase"
  }'
```

**Upload product image:**
```bash
curl -X POST http://localhost:8080/api/products/1/images \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test-image.jpg"
```

**4. Verify in Supabase Dashboard**

- **Database**: Table Editor → `product` table → should see your test product
- **Storage**: Storage → `product-images` → should see uploaded image

---

## 🗄️ New Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT APPLICATION                        │
│                     (React, Angular, etc.)                       │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT BACKEND                           │
│                                                                  │
│  ┌──────────────────┐         ┌─────────────────────────┐      │
│  │ REST Controllers │ ←────── │ Spring Security (JWT)    │      │
│  └──────────────────┘         └─────────────────────────┘      │
│           ↓                                                      │
│  ┌──────────────────┐         ┌─────────────────────────┐      │
│  │   Services       │ ←────── │ SupabaseStorageService   │      │
│  └──────────────────┘         └─────────────────────────┘      │
│           ↓                              ↓                       │
│  ┌──────────────────┐                    ↓                      │
│  │  Repositories    │                    ↓                      │
│  │  (Spring Data)   │                    ↓                      │
│  └──────────────────┘                    ↓                      │
└──────────┬────────────────────────────────┼─────────────────────┘
           ↓                                ↓
           ↓                                ↓
┌──────────┴────────────────┐   ┌──────────┴──────────────────────┐
│   SUPABASE POSTGRESQL     │   │    SUPABASE STORAGE             │
│   (Database)              │   │    (File Storage)               │
│                           │   │                                 │
│  ┌────────────────────┐   │   │  ┌──────────────────────────┐  │
│  │ product            │   │   │  │ product-images/          │  │
│  │ product_image      │   │   │  │   products/1/uuid.jpg    │  │
│  │ review             │   │   │  │   products/2/uuid.jpg    │  │
│  │ review_image       │   │   │  │                          │  │
│  │ customer           │   │   │  │ review-images/           │  │
│  │ orders             │   │   │  │   reviews/10/uuid.jpg    │  │
│  │ order_item         │   │   │  │                          │  │
│  │ cart               │   │   │  └──────────────────────────┘  │
│  │ cart_item          │   │   │                                 │
│  │ payment            │   │   │  All files served via CDN:      │
│  └────────────────────┘   │   │  https://xxx.supabase.co/       │
│                           │   │  storage/v1/object/public/...   │
└───────────────────────────┘   └─────────────────────────────────┘
```

---

## 📊 Data Flow: Complete Example

### User Uploads Product Image

```
1. User selects image in browser
   ↓
2. Frontend sends POST /api/products/123/images
   ↓
3. Spring Boot Controller receives MultipartFile
   ↓
4. SupabaseStorageService.uploadProductImage()
   - Uploads to Supabase Storage bucket
   - Returns public URL: https://xxx.supabase.co/storage/.../uuid-image.jpg
   ↓
5. ProductImage entity created:
   productImage.setSupabaseUrl("https://xxx.supabase.co/storage/.../uuid-image.jpg");
   productImage.setBucketPath("products/123/uuid-image.jpg");
   ↓
6. JPA saves to Supabase PostgreSQL database:
   INSERT INTO product_image (supabase_url, bucket_path, product_id)
   VALUES ('https://...', 'products/123/...', 123);
   ↓
7. Response to frontend:
   { "imageId": 1, "imageUrl": "https://xxx.supabase.co/storage/..." }
   ↓
8. Frontend displays image:
   <img src="https://xxx.supabase.co/storage/..." />
   (Loaded directly from Supabase CDN)
```

---

## 🔄 What Changes in Your Code?

### Minimal Changes Required! ✅

**1. `pom.xml`**
- Remove: `mysql-connector-j`
- Add: `postgresql`

**2. `application.properties`**
- Change: `spring.datasource.url` to PostgreSQL connection string
- Change: `spring.datasource.driver-class-name` to `org.postgresql.Driver`
- Change: `spring.jpa.properties.hibernate.dialect` to `PostgreSQLDialect`

**3. Entity classes (optional cleanup)**
- Remove `imageData` BLOB field (no longer needed)
- Use `GenerationType.IDENTITY` for IDs

**4. No changes needed:**
- ✅ Controllers - work as-is
- ✅ Services - work as-is
- ✅ Repositories - work as-is
- ✅ SupabaseStorageService - works as-is

---

## 🎯 Benefits of Full Supabase Migration

| Feature | MySQL + Supabase Storage | Full Supabase |
|---------|-------------------------|---------------|
| **Database** | MySQL (self-hosted) | PostgreSQL (managed) |
| **Storage** | Supabase Storage | Supabase Storage |
| **Backups** | Manual | Automatic daily backups |
| **Scaling** | Manual | Auto-scaling |
| **Monitoring** | Setup required | Built-in dashboard |
| **API** | Custom only | REST + GraphQL auto-generated |
| **Real-time** | Not available | Built-in real-time subscriptions |
| **Auth** | Custom JWT | Built-in Auth (optional) |
| **Cost** | Server + MySQL license | Free tier: 500MB DB + 1GB storage |

---

## 💰 Supabase Pricing

### Free Tier (Perfect for Development)
- ✅ 500 MB PostgreSQL database
- ✅ 1 GB file storage
- ✅ 2 GB bandwidth/month
- ✅ 50,000 monthly active users
- ✅ Unlimited API requests

### Pro Tier ($25/month)
- 8 GB database
- 100 GB storage
- 200 GB bandwidth
- Daily backups (7 days retention)
- Email support

---

## 🚀 Quick Start Commands

```bash
# 1. Update dependencies
cd E_Commerce
mvn clean install

# 2. Update application.properties with Supabase credentials

# 3. Run application (Hibernate creates tables automatically)
mvn spring-boot:run

# 4. Test endpoint
curl http://localhost:8080/api/products

# 5. Upload test image
curl -X POST http://localhost:8080/api/products/1/images \
  -F "file=@test.jpg"
```

---

## 🔍 Verification Checklist

After migration, verify:

- [ ] Application starts without errors
- [ ] All tables visible in Supabase → Table Editor
- [ ] Can create products via API
- [ ] Can upload images (visible in Supabase → Storage)
- [ ] Image URLs saved to database (check product_image table)
- [ ] Images load in browser from Supabase CDN
- [ ] Can retrieve product with images
- [ ] Can delete images (removed from storage + database)

---

## 📚 Next Steps

1. **Complete Setup** (15 minutes total)
   - Create Supabase project
   - Update `pom.xml` and `application.properties`
   - Create storage buckets
   - Run application

2. **Test Thoroughly**
   - Upload images
   - Create products
   - Place orders

3. **Optional: Enable Supabase Auth**
   - Replace Spring Security with Supabase Auth
   - Use built-in user management

4. **Optional: Use Supabase Real-time**
   - Get live updates when products change
   - No polling needed

5. **Deploy to Production**
   - Use production Supabase project
   - Configure environment variables
   - Enable SSL

---

## 🆘 Troubleshooting

### "Connection refused" error
```
Check: spring.datasource.url is correct
Fix: Copy exact URL from Supabase → Settings → Database
```

### "Password authentication failed"
```
Check: Database password in application.properties
Fix: Use password from when you created Supabase project
```

### "No suitable driver found"
```
Check: postgresql dependency in pom.xml
Fix: Run mvn clean install
```

### Images upload but don't display
```
Check: Buckets are set to PUBLIC
Fix: Storage → Bucket → Policies → Add public read policy
```

---

## 📖 Resources

- **Supabase Docs**: https://supabase.com/docs
- **PostgreSQL Dialect**: https://docs.jboss.org/hibernate/orm/current/javadocs/org/hibernate/dialect/PostgreSQLDialect.html
- **Spring Boot + PostgreSQL**: https://spring.io/guides/gs/accessing-data-jpa/
- **Supabase Storage**: https://supabase.com/docs/guides/storage

---

**You're now ready to migrate to full Supabase! 🚀**

The migration is straightforward:
1. Update 2 files (pom.xml, application.properties)
2. Create Supabase project + buckets
3. Run application
4. Hibernate handles the rest!
