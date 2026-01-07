# Promo Code Per-User Usage Limits

## Overview
The promo code system now supports configurable per-user usage limits, allowing each user to use a promo code a specific number of times while the code remains available for other users.

## Features

### 1. Per-User Usage Tracking
- **Field**: `perUserUsageLimit` (nullable Integer)
- **Behavior**: 
  - `null` = unlimited uses per user
  - `1` = one-time use per user
  - `2+` = specified number of uses per user

### 2. Independent from Total Usage Limit
- `usageLimit`: Total uses across ALL users
- `perUserUsageLimit`: Uses per INDIVIDUAL user
- Both limits are checked independently

### 3. Clear Error Messages
When a user reaches their limit:
```
"You have reached your usage limit (X) for this promo code"
```

## Database Schema

### PromoCode Table
```sql
ALTER TABLE promo_codes ADD COLUMN per_user_usage_limit INTEGER;
```

### PromoUsage Table
Already tracks individual usage:
- Links `promo_code` + `user` + `order`
- Timestamps each usage
- Enables accurate per-user counting

## Backend Implementation

### 1. Domain Model (`PromoCode.java`)
```java
private Integer perUserUsageLimit; // Max times each user can use this promo
```

### 2. Validation Logic (`PromoCodeServiceImpl.java`)
```java
// Check per-user usage limit
if (promoCode.getPerUserUsageLimit() != null && promoCode.getPerUserUsageLimit() > 0) {
    long userUsageCount = getUserPromoUsageCount(promoCode.getPromoId(), userId);
    if (userUsageCount >= promoCode.getPerUserUsageLimit()) {
        return PromoApplicationResult.failure(
            String.format("You have reached your usage limit (%d) for this promo code", 
                promoCode.getPerUserUsageLimit()));
    }
}
```

### 3. Repository Query
```java
// PromoUsageRepository.java
@Query("SELECT COUNT(pu) FROM PromoUsage pu WHERE pu.promoCode.promoId = :promoId AND pu.user.userId = :userId")
long countByPromoIdAndUserId(@Param("promoId") Integer promoId, @Param("userId") Integer userId);
```

## Frontend Implementation

### Admin Promo Code Form
Added new input field:
```tsx
<Label htmlFor="perUserUsageLimit">Per-User Usage Limit</Label>
<Input
  id="perUserUsageLimit"
  type="number"
  min="1"
  placeholder="Unlimited per user"
  value={formData.perUserUsageLimit || ''}
  onChange={(e) => setFormData({ 
    ...formData, 
    perUserUsageLimit: e.target.value ? parseInt(e.target.value) : null 
  })}
/>
<p className="text-xs text-muted-foreground">
  Maximum times each user can use this code
</p>
```

## Usage Examples

### Example 1: Holiday Sale (3 Uses Per Customer)
```json
{
  "code": "HOLIDAY25",
  "discountType": "PERCENTAGE",
  "discountValue": 25,
  "usageLimit": 1000,
  "perUserUsageLimit": 3,
  "description": "Holiday sale - each customer can use 3 times"
}
```

**Result**: 
- Total 1000 uses allowed across all users
- Each user can apply the code up to 3 times
- After 3 uses, user sees: "You have reached your usage limit (3) for this promo code"
- Code remains active for other users

### Example 2: VIP Unlimited
```json
{
  "code": "VIP50",
  "discountType": "PERCENTAGE",
  "discountValue": 50,
  "usageLimit": null,
  "perUserUsageLimit": null,
  "description": "VIP customers - unlimited uses"
}
```

**Result**: No limits at all

### Example 3: One-Time Trial
```json
{
  "code": "FIRSTORDER",
  "discountType": "FIXED",
  "discountValue": 100,
  "usageLimit": 10000,
  "perUserUsageLimit": 1,
  "description": "First order discount - one per customer"
}
```

**Result**: 
- 10,000 total redemptions available
- Each user can only use once
- Prevents abuse while serving many customers

## API Endpoints

### Create Promo with Per-User Limit
```http
POST /api/promos/create
Content-Type: application/json

{
  "code": "SUMMER30",
  "discountType": "PERCENTAGE",
  "discountValue": 30,
  "perUserUsageLimit": 2,
  "productIds": [1, 2, 3]
}
```

### Update Promo
```http
PUT /api/promos/update/{id}
Content-Type: application/json

{
  "perUserUsageLimit": 5
}
```

## Testing Scenarios

### Scenario 1: User Reaches Limit
1. User applies promo code → success
2. User places order → usage recorded
3. User applies same code again → success (if limit > 1)
4. User repeats until limit reached
5. Next attempt → error message displayed

### Scenario 2: Different Users
1. User A uses code 3 times → reaches limit
2. User B can still use code 3 times
3. Limits are independent per user

### Scenario 3: Global Limit First
1. Promo has usageLimit=100, perUserUsageLimit=5
2. If 100 total uses reached → code expires for everyone
3. Individual user limits don't matter

## Migration Strategy

### For Existing Promo Codes
```sql
-- All existing codes default to null (unlimited per user)
UPDATE promo_codes SET per_user_usage_limit = NULL WHERE per_user_usage_limit IS NULL;

-- Optional: Convert old one-time-use flag to per-user limit
UPDATE promo_codes 
SET per_user_usage_limit = 1 
WHERE one_time_use = true AND per_user_usage_limit IS NULL;
```

## Storage-Agnostic Design

The logic is database-independent and works with:
- **SQL databases**: via JPA repositories
- **NoSQL**: count documents matching `{promoId, userId}`
- **In-memory**: filter array/map entries
- **Key-value stores**: track `promo:{id}:user:{id}:count`

Core validation logic:
```java
long userCount = countUsagesByPromoAndUser(promoId, userId);
if (limit != null && userCount >= limit) {
    return error("limit reached");
}
```

## Benefits

1. **Flexible**: Admins can set any limit (1, 5, 100, etc.)
2. **Fair**: Prevents single-user abuse while serving many customers
3. **Clear**: Users see exact limit in error messages
4. **Scalable**: Works for both small and large user bases
5. **Backwards Compatible**: Existing codes work without changes

## Future Enhancements

- Time-based resets (monthly limits)
- Category-specific limits
- Tier-based limits (bronze/silver/gold users)
- Usage analytics dashboard
