# Postman API Tests for E-Commerce Application

## Base URL
```
http://localhost:8080/api
```

---

## 1. Create Role (Required First)

### POST `/roles/create`

**Request Body:**
```json
{
  "roleName": "CUSTOMER"
}
```

**Expected Response:** `201 Created`
```json
{
  "roleId": 1,
  "roleName": "CUSTOMER",
  "permissions": []
}
```

---

## 2. Create User

### POST `/users/create`

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "role": {
    "roleId": 1
  },
  "isActive": true,
  "createdAt": "2025-12-11T10:00:00"
}
```

**Expected Response:** `201 Created`
```json
{
  "userId": 1,
  "username": "john_doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "role": {
    "roleId": 1,
    "roleName": "CUSTOMER",
    "permissions": []
  },
  "cart": null,
  "addresses": [],
  "active": true,
  "createdAt": "2025-12-11T10:00:00",
  "paymentMethods": []
}
```

---

## 3. Get User by ID

### GET `/users/read/1`

**Expected Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890",
    "role": {
      "roleId": 1,
      "roleName": "CUSTOMER"
    },
    "active": true,
    "createdAt": "2025-12-11T10:00:00"
  }
}
```

---

## 4. User Login

### POST `/users/login?email=john.doe@example.com&password=securePassword123`

**Expected Response:** `200 OK`
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": {
      "roleId": 1,
      "roleName": "CUSTOMER"
    }
  }
}
```

---

## 5. Get User by Email

### GET `/users/email/john.doe@example.com`

**Expected Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

---

## 6. Update User

### PUT `/users/update`

**Request Body:**
```json
{
  "userId": 1,
  "username": "john_doe_updated",
  "email": "john.doe@example.com",
  "password": "newPassword456",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567891",
  "role": {
    "roleId": 1
  },
  "isActive": true,
  "createdAt": "2025-12-11T10:00:00"
}
```

**Expected Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "username": "john_doe_updated",
    "email": "john.doe@example.com",
    "phone": "+1234567891"
  }
}
```

---

## 7. Get All Users

### GET `/users/getAll`

**Expected Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "userId": 1,
      "username": "john_doe_updated",
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe"
    }
  ]
}
```

---

## 8. Delete User

### DELETE `/users/delete/1`

**Expected Response:** `200 OK`
```json
{
  "success": true,
  "message": "User deleted successfully"
}
```

---

## Additional Test Scenarios

### Create Multiple Users with Different Roles

#### Create ADMIN Role
**POST** `/roles/create`
```json
{
  "roleName": "ADMIN"
}
```

#### Create Admin User
**POST** `/users/create`
```json
{
  "username": "admin_user",
  "email": "admin@example.com",
  "password": "adminPass123",
  "firstName": "Admin",
  "lastName": "User",
  "phone": "+9876543210",
  "role": {
    "roleId": 2
  },
  "isActive": true,
  "createdAt": "2025-12-11T11:00:00"
}
```

---

## Error Scenarios

### 1. Login with Invalid Password
**POST** `/users/login?email=john.doe@example.com&password=wrongpassword`

**Expected Response:** `401 Unauthorized`
```json
{
  "success": false,
  "message": "Invalid password"
}
```

### 2. Login with Non-existent Email
**POST** `/users/login?email=notfound@example.com&password=anypassword`

**Expected Response:** `404 Not Found`
```json
{
  "success": false,
  "message": "User not found"
}
```

### 3. Get Non-existent User
**GET** `/users/read/999`

**Expected Response:** `404 Not Found`
```json
{
  "success": false,
  "message": "User not found"
}
```

---

## Tips for Postman Testing

1. **Set Environment Variables:**
   - `baseUrl`: `http://localhost:8080/api`
   - `userId`: Save from create response
   - `roleId`: Save from role creation

2. **Test Order:**
   - Create Role first
   - Then create User (referencing the role)
   - Test CRUD operations
   - Test login functionality

3. **Headers:**
   - Content-Type: `application/json`
   - Accept: `application/json`

4. **Save Tests:**
   Create a Postman Collection with all these requests for reusable testing.
