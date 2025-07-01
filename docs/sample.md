# Backup Mode API

## Public Endpoints

### GET /record/v1/public/backup-modes

Lấy danh sách tất cả backup modes

**Payload:** Không

**Response:**

```json
{
  "data": [
    {
      "code": "BAT_BUOC",
      "name": "Bắt buộc",
      "description": "Bắt buộc phải lập bản dự phòng",
      "isMandatory": true,
      "isAutomatic": false,
      "backupType": "MIXED",
      "schedulePattern": null,
      "priorityLevel": 10,
      "isActive": true,
      "createdAt": 1733919200000,
      "updatedAt": 1733919200000
    },
    {
      "code": "TU_DONG",
      "name": "Tự động",
      "description": "Lập bản dự phòng tự động",
      "isMandatory": false,
      "isAutomatic": true,
      "backupType": "MIXED",
      "schedulePattern": "0 0 2 * * ?",
      "priorityLevel": 8,
      "isActive": true,
      "createdAt": 1733919200000,
      "updatedAt": 1733919200000
    }
  ]
}
```

### GET /record/v1/public/backup-modes/{code}

Lấy thông tin backup mode theo code

**Path Parameters:**

- `code`: Mã backup mode (ví dụ: BAT_BUOC)

**Payload:** Không

**Response:**

```json
{
  "data": {
    "code": "BAT_BUOC",
    "name": "Bắt buộc",
    "description": "Bắt buộc phải lập bản dự phòng",
    "isMandatory": true,
    "isAutomatic": false,
    "backupType": "MIXED",
    "schedulePattern": null,
    "priorityLevel": 10,
    "isActive": true,
    "createdAt": 1733919200000,
    "updatedAt": 1733919200000
  }
}
```

## Admin Endpoints

### PUT /record/v1/admin/backup-modes/{code}

Cập nhật backup mode

**Path Parameters:**

- `code`: Mã backup mode

**Payload:**

```json
{
  "name": "Bắt buộc cập nhật",
  "description": "Bắt buộc phải lập bản dự phòng theo quy định mới",
  "isMandatory": true,
  "isAutomatic": false,
  "backupType": "MIXED",
  "priorityLevel": 10,
  "isActive": true
}
```

**Response:**

```json
{
  "data": {
    "code": "BAT_BUOC",
    "name": "Bắt buộc cập nhật",
    "description": "Bắt buộc phải lập bản dự phòng theo quy định mới",
    "isMandatory": true,
    "isAutomatic": false,
    "backupType": "MIXED",
    "schedulePattern": null,
    "priorityLevel": 10,
    "isActive": true,
    "createdAt": 1733919200000,
    "updatedAt": 1733919300000
  }
}
```

## Error Responses

**404 Not Found:**

```json
{
  "error": "Backup mode không tồn tại với code: INVALID_CODE"
}
```

**400 Bad Request:**

```json
{
  "error": "Validation failed",
  "details": "Name không được để trống"
}
```

# API Sample Requests & Responses - Hoppscotch Format

## Environment Setup

Trước khi test, setup environment variables trong Hoppscotch:

```
BASE_URL = http://localhost:8080
ACCESS_TOKEN = your-jwt-token-here
ADMIN_TOKEN = your-admin-jwt-token-here
```

## File Management API Examples

### 1. Upload File (with options)

#### Default Upload (REJECT on conflict)

**Endpoint:** `<<BASE_URL>>/file/v1/private/upload`  
**Method:** POST  
**Auth:** Bearer `<<ACCESS_TOKEN>>`  
**Content-Type:** application/json

**Body:**

```json
{
  "storageCode": "my-storage",
  "name": "document",
  "extension": "pdf",
  "size": 1024000,
  "mimeType": "application/pdf",
  "folderId": null,
  "metadata": {
    "title": "Important Document",
    "author": "John Doe",
    "tags": ["important", "contract"]
  }
}
```

**Response:**

```json
{
  "success": true,
  "data": {
    "id": 123,
    "storageCode": "my-storage",
    "name": "document",
    "extension": "pdf",
    "objectKey": "2024/01/15/10/uuid-123_document.pdf",
    "size": 1024000,
    "mimeType": "application/pdf",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00Z",
    "metadata": {
      "title": "Important Document",
      "author": "John Doe",
      "tags": ["important", "contract"]
    }
  },
  "message": "File uploaded successfully"
}
```

#### Upload with OVERWRITE option

**Endpoint:** `<<BASE_URL>>/file/v1/private/upload`  
**Method:** POST  
**Query Params:** `option=OVERWRITE`  
**Auth:** Bearer `<<ACCESS_TOKEN>>`

**Body:**

```json
{
  "storageCode": "my-storage",
  "name": "document",
  "extension": "pdf",
  "size": 1024000,
  "mimeType": "application/pdf",
  "folderId": 1,
  "metadata": {
    "title": "Updated Document",
    "version": "2.0"
  }
}
```

#### Upload with RENAME option

**Endpoint:** `<<BASE_URL>>/file/v1/private/upload`  
**Method:** POST  
**Query Params:** `option=RENAME`  
**Auth:** Bearer `<<ACCESS_TOKEN>>`

**Response (renamed):**

```json
{
  "success": true,
  "data": {
    "id": 124,
    "name": "document-1",
    "objectKey": "2024/01/15/10/uuid-124_document-1.pdf",
    "generatedName": true
  },
  "message": "File uploaded with auto-generated name"
}
```

### 2. Get Files

#### Get My Files

**Endpoint:** `<<BASE_URL>>/file/v1/private/files`  
**Method:** GET  
**Auth:** Bearer `<<ACCESS_TOKEN>>`

#### Get Files by Folder

**Endpoint:** `<<BASE_URL>>/file/v1/private/files/folder/1`  
**Method:** GET  
**Auth:** Bearer `<<ACCESS_TOKEN>>`

**Response:**

```json
{
  "success": true,
  "data": [
    {
      "id": 123,
      "storageCode": "my-storage",
      "folderId": 1,
      "name": "document",
      "extension": "pdf",
      "objectKey": "2024/01/15/10/uuid-123_document.pdf",
      "size": 1024000,
      "mimeType": "application/pdf",
      "status": "ACTIVE",
      "createdAt": "2024-01-15T10:30:00Z",
      "metadata": {
        "title": "Important Document"
      }
    }
  ],
  "message": "Files retrieved successfully"
}
```

### 3. Update File

**Endpoint:** `<<BASE_URL>>/file/v1/private/files/update`  
**Method:** PUT  
**Auth:** Bearer `<<ACCESS_TOKEN>>`  
**Content-Type:** application/json

**Body:**

```json
{
  "id": 123,
  "storageCode": "my-storage",
  "name": "updated-document",
  "extension": "pdf",
  "size": 1024000,
  "mimeType": "application/pdf",
  "folderId": 1,
  "status": "ACTIVE",
  "metadata": {
    "title": "Updated Important Document",
    "author": "John Doe",
    "version": "2.0",
    "lastModified": "2024-01-15T11:00:00Z"
  }
}
```

### 4. Move File

**Endpoint:** `<<BASE_URL>>/file/v1/private/files/move`  
**Method:** PUT  
**Query Params:** `fileId=123&toFolderId=456`  
**Auth:** Bearer `<<ACCESS_TOKEN>>`

**Response:**

```json
{
  "success": true,
  "data": {
    "id": 123,
    "folderId": 456,
    "path": "/Documents/Reports/document.pdf"
  },
  "message": "File moved successfully"
}
```

### 5. Download File

**Endpoint:** `<<BASE_URL>>/file/v1/private/download/123`  
**Method:** GET  
**Auth:** Bearer `<<ACCESS_TOKEN>>`

## Storage Management API Examples

### 1. Create Storage

**Endpoint:** `<<BASE_URL>>/file/v1/admin/storages`  
**Method:** POST  
**Auth:** Bearer `<<ADMIN_TOKEN>>`  
**Content-Type:** application/json

**Body:**

```json
{
  "code": "document-storage",
  "minioBucket": "doc-storage-bucket",
  "name": "Document Storage",
  "description": "Storage for company documents",
  "type": "DOCUMENT",
  "status": "ACTIVE",
  "metadata": {
    "maxFileSize": 104857600,
    "allowedTypes": ["pdf", "doc", "docx", "txt"],
    "retentionDays": 365,
    "autoCleanup": true
  }
}
```

### 2. List All Storages

**Endpoint:** `<<BASE_URL>>/file/v1/admin/storages`  
**Method:** GET  
**Auth:** Bearer `<<ADMIN_TOKEN>>`

### 3. Get Storage by Code

**Endpoint:** `<<BASE_URL>>/file/v1/admin/storages/my-storage`  
**Method:** GET  
**Auth:** Bearer `<<ADMIN_TOKEN>>`

### 4. Update Storage

**Endpoint:** `<<BASE_URL>>/file/v1/admin/storages`  
**Method:** PUT  
**Auth:** Bearer `<<ADMIN_TOKEN>>`  
**Content-Type:** application/json

**Body:**

```json
{
  "code": "document-storage",
  "minioBucket": "doc-storage-bucket",
  "name": "Updated Document Storage",
  "description": "Updated storage configuration",
  "type": "DOCUMENT",
  "status": "ACTIVE",
  "metadata": {
    "maxFileSize": 209715200,
    "allowedTypes": ["pdf", "doc", "docx", "txt", "xlsx"],
    "retentionDays": 730,
    "autoCleanup": true,
    "compressionEnabled": true
  }
}
```

### 5. Delete Storage

**Endpoint:** `<<BASE_URL>>/file/v1/admin/storages/my-storage`  
**Method:** DELETE  
**Auth:** Bearer `<<ADMIN_TOKEN>>`

## Folder Management API Examples

### 1. Create Folder Structure

#### Root folder

**Endpoint:** `<<BASE_URL>>/file/v1/private/folders`  
**Method:** POST  
**Auth:** Bearer `<<ACCESS_TOKEN>>`  
**Content-Type:** application/json

**Body:**

```json
{
  "storageCode": "my-storage",
  "name": "Documents",
  "parentId": null,
  "metadata": {
    "description": "Main documents folder",
    "category": "root",
    "tags": ["important", "work"]
  }
}
```

#### Subfolder

**Body:**

```json
{
  "storageCode": "my-storage",
  "name": "Reports",
  "parentId": 1,
  "metadata": {
    "description": "Monthly and quarterly reports",
    "category": "reports",
    "department": "Finance"
  }
}
```

### 2. Get Folders

#### Get All Folders

**Endpoint:** `<<BASE_URL>>/file/v1/private/folders`  
**Method:** GET  
**Auth:** Bearer `<<ACCESS_TOKEN>>`

#### Get Folders by Parent

**Endpoint:** `<<BASE_URL>>/file/v1/private/folders/parent/1`  
**Method:** GET  
**Auth:** Bearer `<<ACCESS_TOKEN>>`

#### Get Folder by ID

**Endpoint:** `<<BASE_URL>>/file/v1/private/folders/1`  
**Method:** GET  
**Auth:** Bearer `<<ACCESS_TOKEN>>`

### 3. Move Folder

**Endpoint:** `<<BASE_URL>>/file/v1/private/folders/move`  
**Method:** PUT  
**Query Params:** `folderId=2&toParentFolderId=3`  
**Auth:** Bearer `<<ACCESS_TOKEN>>`

**Response:**

```json
{
  "success": true,
  "data": {
    "id": 2,
    "name": "Reports",
    "parentId": 3,
    "oldPath": "/Documents/Reports",
    "newPath": "/Documents/Templates/Reports",
    "affectedChildren": 5
  },
  "message": "Folder moved successfully"
}
```

### 4. Update Folder

**Endpoint:** `<<BASE_URL>>/file/v1/private/folders/update`  
**Method:** PUT  
**Auth:** Bearer `<<ACCESS_TOKEN>>`  
**Content-Type:** application/json

**Body:**

```json
{
  "id": 2,
  "storageCode": "my-storage",
  "name": "Monthly Reports",
  "parentId": 1,
  "status": "ACTIVE",
  "metadata": {
    "description": "Updated monthly reports folder",
    "category": "reports",
    "department": "Finance",
    "lastReorganized": "2024-01-15T11:00:00Z"
  }
}
```

### 5. Delete Folder

**Endpoint:** `<<BASE_URL>>/file/v1/private/folders/1`  
**Method:** DELETE  
**Auth:** Bearer `<<ACCESS_TOKEN>>`

## Admin Operations Examples

### Get Files by Storage (Admin)

**Endpoint:** `<<BASE_URL>>/file/v1/admin/files/storage/my-storage`  
**Method:** GET  
**Auth:** Bearer `<<ADMIN_TOKEN>>`

### Get Files by User (Admin)

**Endpoint:** `<<BASE_URL>>/file/v1/admin/files/user/12345`  
**Method:** GET  
**Auth:** Bearer `<<ADMIN_TOKEN>>`

### Get Folders by Storage (Admin)

**Endpoint:** `<<BASE_URL>>/file/v1/admin/folders/storage/my-storage`  
**Method:** GET  
**Auth:** Bearer `<<ADMIN_TOKEN>>`

## Public Endpoints

### Get Version

**Endpoint:** `<<BASE_URL>>/file/v1/public/version`  
**Method:** GET  
**Auth:** None

**Response:**

```
v1.0.0
```

## Error Response Examples

### 1. Validation Error

```json
{
  "success": false,
  "error": "VALIDATION_ERROR",
  "message": "Invalid file size. Maximum allowed size is 100MB",
  "details": {
    "field": "size",
    "providedValue": 209715200,
    "maxAllowed": 104857600
  }
}
```

### 2. Conflict Error (File Upload)

```json
{
  "success": false,
  "error": "CONFLICT",
  "message": "File with same name already exists in this location",
  "details": {
    "conflictingFile": {
      "id": 100,
      "name": "document.pdf",
      "objectKey": "2024/01/14/15/uuid-100_document.pdf"
    },
    "suggestion": "Use option=OVERWRITE or option=RENAME"
  }
}
```

### 3. Not Found Error

```json
{
  "success": false,
  "error": "NOT_FOUND",
  "message": "File with ID 999 not found",
  "details": {
    "resourceType": "FILE",
    "resourceId": 999
  }
}
```

### 4. Authorization Error

```json
{
  "success": false,
  "error": "UNAUTHORIZED",
  "message": "Access denied. Admin privileges required for this operation",
  "details": {
    "requiredRole": "ADMIN",
    "currentRole": "USER"
  }
}
```

## Hoppscotch Testing Workflow

### 1. Setup Phase

1. Import all three collection files into Hoppscotch
2. Create environment with variables:
   - `BASE_URL`: http://localhost:8080
   - `ACCESS_TOKEN`: Your JWT token
   - `ADMIN_TOKEN`: Your admin JWT token

### 2. Admin Setup

1. Test public endpoint first: Get Version
2. Create storage using Storage API
3. Verify storage creation with List All Storages

### 3. Folder Structure

1. Create root folders using Folder API
2. Create subfolders with parentId
3. Test folder retrieval by parent

### 4. File Operations

1. Upload files to different locations
2. Test different upload options (REJECT, OVERWRITE, RENAME)
3. Move files between folders
4. Update file metadata
5. Test download operations

### 5. Admin Operations

1. Test admin file queries by storage/user
2. Test admin folder queries by storage
3. Verify admin-only access control

### 6. Error Testing

1. Test with invalid tokens
2. Test with missing required fields
3. Test conflict scenarios
4. Verify proper error responses

## Tips for Hoppscotch Usage

### Environment Variables

- Use `<<VARIABLE>>` syntax instead of `{{variable}}`
- Set up different environments for dev/staging/prod
- Use secret variables for sensitive tokens

### Request Organization

- Group related requests in folders
- Use descriptive names for requests
- Add descriptions for complex operations

### Testing Best Practices

- Test happy path first
- Then test error scenarios
- Use pre-request scripts for setup if needed
- Save common responses as examples
