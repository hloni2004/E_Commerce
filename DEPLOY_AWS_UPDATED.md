# Deploy Updated Backend to AWS

## Changes Made:
1. ✅ Disabled HTTPS enforcement filter (AWS handles SSL at load balancer)
2. ✅ Updated frontend to connect to AWS backend

## Deploy Steps:

### 1. Build Updated Backend
```bash
cd C:\Users\Lehlohonolo.Mokoena\IdeaProjects\Website\E_Commerce
mvn clean package -DskipTests
```

### 2. Upload to AWS Elastic Beanstalk

**Option A: Via AWS Console (Recommended)**
1. Go to: https://console.aws.amazon.com/elasticbeanstalk
2. Select region: **eu-north-1**
3. Click on: **MaisonLuxeBackend-env**
4. Click: **Upload and deploy**
5. Click: **Choose file**
6. Select: `target/E-Commerce-1.0-SNAPSHOT.jar`
7. Click: **Deploy**
8. Wait 5-10 minutes for deployment

**Option B: Via AWS CLI**
```bash
aws elasticbeanstalk create-application-version \
  --application-name MaisonLuxeBackend \
  --version-label v2-https-fix-$(date +%Y%m%d-%H%M%S) \
  --source-bundle S3Bucket=elasticbeanstalk-eu-north-1-361907858345,S3Key=path/to/E-Commerce-1.0-SNAPSHOT.jar \
  --region eu-north-1

aws elasticbeanstalk update-environment \
  --environment-name MaisonLuxeBackend-env \
  --version-label v2-https-fix-$(date +%Y%m%d-%H%M%S) \
  --region eu-north-1
```

## 3. Test Backend Registration

**Postman Request:**
```
POST http://maisonluxebackend-env.eba-uf7iacuq.eu-north-1.elasticbeanstalk.com/api/users/register

Headers:
  Content-Type: application/json

Body (raw JSON):
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "SecurePass123!",
  "firstName": "Test",
  "lastName": "User",
  "phone": "+27123456789"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User registered successfully"
}
```

## 4. Test Frontend Connection

1. Open: http://localhost:5173
2. Go to: Register page
3. Fill in the form
4. Submit
5. Should successfully register and connect to AWS backend

## 5. Deploy Frontend to Vercel (if needed)

Since `.env.production` is updated, redeploy to Vercel:

```bash
cd C:\Users\Lehlohonolo.Mokoena\IdeaProjects\Website\client-hub-portal-e5248649
git add .env .env.production src/lib/api.ts
git commit -m "Update frontend to use AWS backend"
git push
```

Vercel will auto-deploy or:
```bash
npx vercel --prod
```

## URLs:
- **Backend**: http://maisonluxebackend-env.eba-uf7iacuq.eu-north-1.elasticbeanstalk.com
- **Frontend (Dev)**: http://localhost:5173
- **Frontend (Prod)**: https://client-hub-portal.vercel.app
- **API Endpoint**: http://maisonluxebackend-env.eba-uf7iacuq.eu-north-1.elasticbeanstalk.com/api/users/register

## Troubleshooting:

### Still Getting 403?
Check AWS environment variables have:
```
SPRING_PROFILES_ACTIVE=
```
(Leave empty or remove it - don't set to "production")

### CORS Errors?
Backend already allows: https://client-hub-portal.vercel.app

If you get CORS errors, verify in AWS EB Console:
```
APP_CORS_ALLOWED_ORIGINS=https://client-hub-portal.vercel.app,http://localhost:5173,http://localhost:3000
```

### Frontend Can't Connect?
1. Check browser console for errors
2. Verify `.env` file is updated
3. Restart Vite dev server: `npm run dev`
4. Clear browser cache (Ctrl+Shift+Delete)
