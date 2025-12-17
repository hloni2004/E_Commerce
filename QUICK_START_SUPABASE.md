# 🚀 Quick Start: Supabase Storage Setup

## ⚡ 5-Minute Setup Guide

### Step 1: Create Supabase Project (2 minutes)

1. **Go to** [https://supabase.com](https://supabase.com)
2. **Sign in** with GitHub or email
3. **Click** "New Project"
4. **Fill in:**
   - Name: `ecommerce-storage`
   - Database Password: (save this!)
   - Region: Choose closest to you
5. **Wait** for project to be created (~1 minute)

---

### Step 2: Create Storage Buckets (1 minute)

1. **Go to** Storage (left sidebar)
2. **Create Bucket #1:**
   - Name: `product-images`
   - Public: ✅ YES
   - Click "Create bucket"

3. **Create Bucket #2:**
   - Name: `review-images`
   - Public: ✅ YES
   - Click "Create bucket"

---

### Step 3: Get API Credentials (30 seconds)

1. **Go to** Settings → API (left sidebar)
2. **Copy these values:**

```
Project URL: https://xxxxxxxxxxxxx.supabase.co
anon/public: eyJhbGci...
service_role: eyJhbGci... (THIS IS SECRET!)
```

---

### Step 4: Configure Application (1 minute)

**Open:** `application.properties`

**Replace** these lines:

```properties
supabase.url=YOUR_SUPABASE_PROJECT_URL
supabase.anon.key=YOUR_SUPABASE_ANON_KEY
supabase.service.key=YOUR_SUPABASE_SERVICE_ROLE_KEY
```

**With your actual values** from Step 3.

---

### Step 5: Update Dependencies (30 seconds)

**Run in terminal:**

```bash
cd C:\Users\Lehlohonolo.Mokoena\IdeaProjects\Web\E_Commerce
mvn clean install
```

This will download the OkHttp dependency for Supabase API calls.

---

## ✅ You're Done! Test It

### Test Upload (Product Images)

**Using Postman/Insomnia:**

```http
POST http://localhost:8080/api/products/1/images
Content-Type: multipart/form-data

images: [select image files]
setPrimary: true
```

### Test Upload (Review Images)

```http
POST http://localhost:8080/api/reviews/create
Content-Type: multipart/form-data

userId: 1
productId: 1
rating: 5
comment: "Great product!"
images: [select image files]
```

---

## 🎯 What Changed?

### Database Schema (Auto-Updated by Hibernate)

**ProductImage** & **ReviewImage** tables now have:
- `supabase_url` VARCHAR(500) - Full public URL
- `bucket_path` VARCHAR(300) - Path for deletion
- `image_data` LONGBLOB - Still exists for backward compatibility

### New Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/products/{id}/images` | POST | Upload product images |
| `/api/products/images/{imageId}` | DELETE | Delete image |
| `/api/products/{id}/images/{imageId}/set-primary` | PUT | Set primary |
| `/api/products/{id}/images` | GET | Get all images |
| `/api/reviews/create` | POST | Upload review with images |

---

## 📊 Benefits You'll See Immediately

✅ **Faster API responses** - No more BLOB queries  
✅ **Smaller database** - Images not in MySQL  
✅ **CDN delivery** - Images load from edge servers  
✅ **Automatic backups** - Supabase handles it  
✅ **Image optimization** - Supabase auto-optimizes  

---

## 🔒 Security Notes

⚠️ **NEVER expose** `service_role` key in frontend!  
✅ **Use** `anon` key for public read access  
✅ **Configure** bucket policies in Supabase dashboard  

---

## 🆘 Troubleshooting

### Error: "Unable to upload to Supabase"
**Solution:** Check your `service_role` key is correct in `application.properties`

### Error: "Image URL not accessible"
**Solution:** Verify bucket is marked as **Public** in Supabase dashboard

### Error: "413 Payload Too Large"
**Solution:** Already configured! `max-file-size=10MB` in properties

---

## 📈 Next Steps

1. ✅ **Test uploads** with existing products
2. ✅ **Monitor storage** in Supabase dashboard (Usage tab)
3. ⏭️ **Optional:** Migrate existing BLOBs to Supabase (see migration guide)
4. ⏭️ **Production:** Add image compression before upload
5. ⏭️ **Advanced:** Implement signed URLs for private images

---

## 💰 Pricing (Free Tier)

Supabase Free Tier includes:
- ✅ **1 GB storage**
- ✅ **2 GB bandwidth/month**
- ✅ **Unlimited API requests**
- ✅ **Unlimited projects** (2 active)

Perfect for development and small-scale production! 🎉

---

## 📚 Need Help?

- [Supabase Storage Docs](https://supabase.com/docs/guides/storage)
- [Storage API Reference](https://supabase.com/docs/reference/javascript/storage)
- [Full Migration Guide](./SUPABASE_MIGRATION_GUIDE.md)
