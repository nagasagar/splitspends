# SplitSpends API Documentation

## Base URL
```
Development: http://localhost:8080/api/v1
Production: https://splitspends-api.herokuapp.com/api/v1
```

## Authentication
All authenticated endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## API Endpoints

### 🔐 Authentication

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

#### Google OAuth
```http
POST /auth/google
Content-Type: application/json

{
  "googleToken": "google-oauth-token"
}
```

### 👤 User Management

#### Get Current User
```http
GET /users/me
Authorization: Bearer <token>
```

#### Update Profile
```http
PUT /users/me
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated Name",
  "phoneNumber": "+1234567890"
}
```

### 🏠 Group Management

#### Create Group
```http
POST /groups
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Trip to Bali",
  "description": "Our amazing vacation expenses",
  "currency": "USD"
}
```

#### Get User Groups
```http
GET /groups
Authorization: Bearer <token>
```

#### Get Group Details
```http
GET /groups/{groupId}
Authorization: Bearer <token>
```

#### Update Group
```http
PUT /groups/{groupId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated Group Name",
  "description": "Updated description"
}
```

#### Add Member to Group
```http
POST /groups/{groupId}/members
Authorization: Bearer <token>
Content-Type: application/json

{
  "email": "member@example.com"
}
```

### 💰 Expense Management

#### Create Expense
```http
POST /expenses
Authorization: Bearer <token>
Content-Type: application/json

{
  "groupId": 1,
  "description": "Dinner at restaurant",
  "amount": 150.00,
  "currency": "USD",
  "category": "FOOD",
  "splits": [
    {
      "userId": 1,
      "amount": 75.00,
      "splitType": "EQUAL"
    },
    {
      "userId": 2,
      "amount": 75.00,
      "splitType": "EQUAL"
    }
  ]
}
```

#### Get Group Expenses
```http
GET /groups/{groupId}/expenses
Authorization: Bearer <token>
```

#### Get Expense Details
```http
GET /expenses/{expenseId}
Authorization: Bearer <token>
```

#### Update Expense
```http
PUT /expenses/{expenseId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "description": "Updated expense description",
  "amount": 200.00
}
```

#### Delete Expense
```http
DELETE /expenses/{expenseId}
Authorization: Bearer <token>
```

### 🔄 Settlement Management

#### Get Group Balances
```http
GET /groups/{groupId}/balances
Authorization: Bearer <token>
```

#### Create Settlement
```http
POST /settlements
Authorization: Bearer <token>
Content-Type: application/json

{
  "groupId": 1,
  "payerId": 1,
  "payeeId": 2,
  "amount": 50.00,
  "description": "Settling dinner expense"
}
```

#### Confirm Settlement
```http
PUT /settlements/{settlementId}/confirm
Authorization: Bearer <token>
```

#### Reject Settlement
```http
PUT /settlements/{settlementId}/reject
Authorization: Bearer <token>
Content-Type: application/json

{
  "reason": "Amount is incorrect"
}
```

### 📨 Invitation Management

#### Send Invitation
```http
POST /invitations
Authorization: Bearer <token>
Content-Type: application/json

{
  "groupId": 1,
  "email": "friend@example.com",
  "personalMessage": "Join our group!"
}
```

#### Accept Invitation
```http
POST /invitations/{token}/accept
Authorization: Bearer <token>
```

#### Decline Invitation
```http
POST /invitations/{token}/decline
Authorization: Bearer <token>
Content-Type: application/json

{
  "reason": "Cannot participate"
}
```

### 📎 Attachment Management

#### Upload Attachment
```http
POST /expenses/{expenseId}/attachments
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <binary-file-data>
description: "Receipt for dinner"
```

#### Download Attachment
```http
GET /attachments/{attachmentId}/download
Authorization: Bearer <token>
```

#### Delete Attachment
```http
DELETE /attachments/{attachmentId}
Authorization: Bearer <token>
```

### 🔔 Notification Management

#### Get User Notifications
```http
GET /notifications
Authorization: Bearer <token>
```

#### Mark Notification as Read
```http
PUT /notifications/{notificationId}/read
Authorization: Bearer <token>
```

#### Mark All Notifications as Read
```http
PUT /notifications/read-all
Authorization: Bearer <token>
```

## Response Format

### Success Response
```json
{
  "success": true,
  "data": {
    // Response data
  },
  "message": "Operation successful"
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Error description",
    "details": {}
  }
}
```

## HTTP Status Codes

| Code | Description |
|------|-------------|
| 200  | OK - Request successful |
| 201  | Created - Resource created successfully |
| 400  | Bad Request - Invalid request data |
| 401  | Unauthorized - Authentication required |
| 403  | Forbidden - Insufficient permissions |
| 404  | Not Found - Resource not found |
| 409  | Conflict - Resource already exists |
| 422  | Unprocessable Entity - Validation error |
| 500  | Internal Server Error - Server error |

## Data Models

### User
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "+1234567890",
  "profilePictureUrl": "https://example.com/avatar.jpg",
  "createdAt": "2025-01-01T00:00:00Z",
  "isActive": true
}
```

### Group
```json
{
  "id": 1,
  "name": "Trip to Bali",
  "description": "Our amazing vacation expenses",
  "currency": "USD",
  "createdBy": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "members": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "role": "ADMIN"
    }
  ],
  "createdAt": "2025-01-01T00:00:00Z",
  "isActive": true
}
```

### Expense
```json
{
  "id": 1,
  "description": "Dinner at restaurant",
  "amount": 150.00,
  "currency": "USD",
  "category": "FOOD",
  "paidBy": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "group": {
    "id": 1,
    "name": "Trip to Bali"
  },
  "splits": [
    {
      "id": 1,
      "user": {
        "id": 1,
        "name": "John Doe"
      },
      "amount": 75.00,
      "splitType": "EQUAL"
    }
  ],
  "attachments": [
    {
      "id": 1,
      "originalFilename": "receipt.jpg",
      "fileSize": 1024,
      "contentType": "image/jpeg"
    }
  ],
  "createdAt": "2025-01-01T00:00:00Z"
}
```

## Rate Limiting

- **General API**: 100 requests per minute per IP
- **Authentication**: 10 requests per minute per IP
- **File Upload**: 5 requests per minute per user

## Pagination

List endpoints support pagination:
```http
GET /groups?page=0&size=20&sort=createdAt,desc
```

Response includes pagination metadata:
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

## WebSocket Events

Connect to: `ws://localhost:8080/ws`

### Events
- `group.member.added`
- `group.member.removed`
- `expense.created`
- `expense.updated`
- `settlement.requested`
- `settlement.confirmed`
- `notification.new`

## Testing

Use the provided Postman collection: [SplitSpends.postman_collection.json](./SplitSpends.postman_collection.json)

## Support

For API support, please contact: api-support@splitspends.com
