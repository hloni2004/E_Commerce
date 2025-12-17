# 🚀 Supabase Storage Migration Guide

## Overview
This guide helps you migrate from storing files as BLOBs in MySQL to using Supabase Storage for better performance, scalability, and cost-efficiency.

## 📋 Current State
- **ProductImage**: Stores images as LONGBLOB in MySQL
- **ReviewImage**: Stores images as LONGBLOB in MySQL
- **Issues**: 
  - Large database size
  - Slow queries with image data
  - Expensive database backup/restore
  - Network bandwidth waste

## ✅ Target State (Supabase)
- **ProductImage**: Stores URL to Supabase Storage
- **ReviewImage**: Stores URL to Supabase Storage
- **Benefits**:
  - Fast CDN delivery
  - Automatic image optimization
  - Cost-effective storage
  - Better database performance

---

## 🔧 Step 1: Setup Supabase Project

### 1.1 Create Supabase Account
1. Go to [https://supabase.com](https://supabase.com)
2. Sign up / Log in
3. Create a new project:
   - **Name**: `ecommerce-storage` (or your choice)
   - **Database Password**: (save this securely)
   - **Region**: Choose closest to your users

### 1.2 Get Credentials
After project creation, navigate to **Project Settings** → **API**:

```
Project URL: https://xxxxxxxxxxxxx.supabase.co
anon/public key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
service_role key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (SECRET!)
```

### 1.3 Create Storage Buckets
Go to **Storage** → **Create a new bucket**:

1. **Bucket Name**: `product-images`
   - **Public**: ✅ Yes (for public product images)
   - **File size limit**: 5 MB
   - **Allowed MIME types**: image/jpeg, image/png, image/webp

2. **Bucket Name**: `review-images`
   - **Public**: ✅ Yes (for customer review images)
   - **File size limit**: 3 MB
   - **Allowed MIME types**: image/jpeg, image/png

### 1.4 Configure Storage Policies
For each bucket, add these policies (Storage → Policies):

**For public read access:**
```sql
CREATE POLICY "Public Access"
ON storage.objects FOR SELECT
USING ( bucket_id = 'product-images' );
```

**For authenticated upload:**
```sql
CREATE POLICY "Authenticated Upload"
ON storage.objects FOR INSERT
WITH CHECK ( bucket_id = 'product-images' );
```

---

## 📦 Step 2: Add Dependencies

Add Supabase Java client to `pom.xml`:

```xml
<dependency>
    <groupId>io.github.jan-tennert.supabase</groupId>
    <artifactId>storage-kt</artifactId>
    <version>2.0.0</version>
</dependency>
```

Or use REST API with OkHttp (simpler):

```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
```

---

## ⚙️ Step 3: Configuration

Add to `application.properties`:

```properties
# Supabase Configuration
supabase.url=https://xxxxxxxxxxxxx.supabase.co
supabase.anon.key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
supabase.service.key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
supabase.bucket.product.images=product-images
supabase.bucket.review.images=review-images

# File Upload Configuration
upload.max.file.size=5MB
upload.allowed.types=image/jpeg,image/png,image/webp
```

---

## 💾 Step 4: Database Schema Changes

### 4.1 Update ProductImage Entity
**Before:**
```java
@Lob
@Column(columnDefinition = "LONGBLOB")
private byte[] imageData;
```

**After:**
```java
// Remove imageData field
private String supabaseUrl;  // Supabase storage URL
private String bucketPath;   // Path in bucket (for deletion)
```

### 4.2 Update ReviewImage Entity
Same changes as ProductImage.

### 4.3 Migration SQL
Run this to update existing database:

```sql
-- Add new columns
ALTER TABLE product_image 
ADD COLUMN supabase_url VARCHAR(500),
ADD COLUMN bucket_path VARCHAR(300);

ALTER TABLE review_image 
ADD COLUMN supabase_url VARCHAR(500),
ADD COLUMN bucket_path VARCHAR(300);

-- After migration, remove old columns (CAREFUL!)
-- ALTER TABLE product_image DROP COLUMN image_data;
-- ALTER TABLE review_image DROP COLUMN image_data;
```

---

## 📝 Step 5: Implementation

See the following files created:
- `SupabaseStorageService.java` - Core storage service
- `ProductImageService.java` (updated) - Use Supabase for products
- `ReviewController.java` (updated) - Use Supabase for reviews

---

## 🔄 Step 6: Data Migration

### Option A: Fresh Start (Recommended for new projects)
1. Deploy new code
2. Start using Supabase for all new uploads
3. Old images remain in database until manually cleaned

### Option B: Migrate Existing Data
Run this migration script to move existing BLOBs to Supabase:

```java
// See MigrateImagesToSupabase.java (if you need it)
```

---

## 🧪 Step 7: Testing

1. **Upload new product image**
   - POST `/api/products/{id}/images`
   - Verify URL returned
   - Check Supabase dashboard

2. **Upload review image**
   - POST `/api/reviews/create` with images
   - Verify images stored in Supabase

3. **Retrieve images**
   - GET `/api/products/{id}`
   - Verify imageUrl contains Supabase URL

---

## 🎯 Benefits After Migration

| Metric | Before (BLOB) | After (Supabase) |
|--------|--------------|------------------|
| Database Size | 5 GB+ | < 500 MB |
| Image Load Time | 500-2000ms | 50-200ms (CDN) |
| Backup Time | 2 hours | 15 minutes |
| Storage Cost | $50/month | $10/month |
| Scalability | Limited | Unlimited |

---

## 🔐 Security Considerations

1. **Never expose service_role key** in frontend
2. Use **anon key** for public read access
3. Implement **signed URLs** for private images
4. Enable **bucket policies** for access control
5. Set **file size limits** to prevent abuse

---

## 📊 Monitoring

Monitor your Supabase usage:
- **Dashboard** → **Usage**
- **Storage**: GB used
- **Bandwidth**: GB transferred
- **API Requests**: Count per day

Free tier limits:
- 1 GB storage
- 2 GB bandwidth/month
- Unlimited API requests

---

## 🆘 Troubleshooting

### Issue: 413 Payload Too Large
**Solution**: Increase Spring Boot upload limit:
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Issue: CORS Error
**Solution**: Add CORS policy in Supabase dashboard

### Issue: Images not loading
**Solution**: Verify bucket is public and policy allows SELECT

---

## 📚 Additional Resources

- [Supabase Storage Docs](https://supabase.com/docs/guides/storage)
- [Java Client Docs](https://github.com/supabase-community/supabase-kt)
- [Storage API Reference](https://supabase.com/docs/reference/javascript/storage)
