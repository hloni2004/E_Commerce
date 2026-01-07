# Render Deployment Configuration

## Backend URL
https://e-commerce-7lqm.onrender.com

## Frontend URL
https://client-hub-portal.vercel.app

## Environment Variables for Render

Copy all variables from `render.env` and add them to your Render service:

### How to Add Environment Variables in Render:
1. Go to your service dashboard: https://dashboard.render.com
2. Select your service: `e-commerce-7lqm`
3. Click on "Environment" tab
4. Add each variable from `render.env` file
5. Click "Save Changes"

### Critical Environment Variables:

**Database (Supabase):**
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

**Email (Mailjet):**
- `MAILJET_API_KEY`
- `MAILJET_API_SECRET`
- `MAIL_SENDER_EMAIL`

**Security:**
- `JWT_SECRET`

**Supabase Storage:**
- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `SUPABASE_SERVICE_KEY`

**CORS:**
- `APP_CORS_ALLOWED_ORIGINS=https://client-hub-portal.vercel.app`

## Build Command
```bash
./mvnw clean package -DskipTests
```

## Start Command
```bash
java -jar target/*.jar
```

## Health Check Endpoint
https://e-commerce-7lqm.onrender.com/api/health

## Notes:
- CORS is already configured for `https://client-hub-portal.vercel.app`
- Frontend API calls point to `https://e-commerce-7lqm.onrender.com/api`
- JWT tokens use Bearer authentication
- Cookies use `withCredentials: true`
