# NewFolderApi Implementation Summary

## Overview

Đã thành công tạo NewFolderApi học hỏi từ cách tổ chức và phân quyền của StorageApi, với focus vào việc tạo folder cho private users với các rule phân quyền nghiêm ngặt.

## Files Created/Modified

### 1. Core Implementation Files

#### `FolderManager.java` - Enhanced

- **Added**: `createFolderForPrivateUser(Token, FolderEntity)` method
- **Features**:
  - Permission validation based on storage type
  - Only allows PUBLIC and PRIVATE storage types for private users
  - Ownership validation for PRIVATE storages
  - Parent folder ownership validation
  - Uses storage cache for fast permission checks
  - Direct database access for folder operations (no caching)

#### `NewFolderApi.java` - New

- **Structure**: Following StorageApi pattern with organized sections:
  - `privateReadApi()` - Future implementations
  - `privateWriteApi()` - Contains folder creation endpoint
  - `privateOtherApi()` - Future implementations
  - `adminReadApi()`, `adminWriteApi()`, `adminOtherApi()` - Future admin operations
- **Endpoint**: `POST /file/v1/private/storages/{storageCode}/folders`
- **Permission**: Uses `ApiPermissionHandler.create("file", "write")`

### 2. Router Registration

#### `VerticleApi.java` - Updated

- Added `NewFolderApi.configAPI(router)` to register new API endpoints

### 3. API Documentation

#### `newfolder-api.json` - New

- Postman/Insomnia collection for testing
- Includes examples for:
  - Root folder creation (`parentId: null`)
  - Subfolder creation (`parentId: <number>`)
- Variables for `USER_TOKEN`, `ADMIN_TOKEN`, `baseUrl`, `storageCode`

#### `newfolder-api.md` - New

- Comprehensive documentation covering:
  - API overview and key features
  - Detailed endpoint specifications
  - Permission model and access rules
  - Request/response examples
  - Error handling
  - Implementation details
  - Testing instructions

## Key Features Implemented

### 1. Permission-Based Access Control

```java
// Only PUBLIC and PRIVATE storage types allowed
if (!"PUBLIC".equals(storageType) && !"PRIVATE".equals(storageType)) {
    return BaseResponse.of(CommonError.FORBIDDEN);
}

// For PRIVATE storages, user must be owner
if ("PRIVATE".equals(storageType)) {
    Long userId = Long.valueOf(token.getSubjectId().toString());
    if (!storage.getCreatedBy().equals(userId)) {
        return BaseResponse.of(CommonError.FORBIDDEN);
    }
}
```

### 2. Parent Folder Ownership Validation

```java
// Parent folder must be owned by current user
if (folder.getParentId() != null) {
    FolderEntity parentFolder = StorageSharedServices.folderService.getById(folder.getParentId());
    Long userId = Long.valueOf(token.getSubjectId().toString());
    if (!parentFolder.getCreatedBy().equals(userId)) {
        return BaseResponse.of(CommonError.FORBIDDEN);
    }
}
```

### 3. Cache Integration

```java
// Get storage from cache for fast permission checks
StorageEntity storage = StorageManager.getInstance().getCacheManager().getStorage(storageCode);
```

### 4. Automatic Audit Fields

```java
// Set audit fields automatically
folder.setCreatedBy(Long.valueOf(token.getSubjectId().toString()));
folder.setCreatedAt(System.currentTimeMillis());
folder.setUpdatedAt(System.currentTimeMillis());
folder.setStatus(FolderStatus.ACTIVE);
```

## API Endpoint Details

### Create Folder Endpoint

- **URL**: `POST /file/v1/private/storages/{storageCode}/folders`
- **Auth**: Bearer Token (USER_TOKEN)
- **Permission**: `file:write`
- **Request Body**:
  ```json
  {
    "name": "string (required)",
    "parentId": "number (optional)",
    "metadata": "object (optional)"
  }
  ```

### Permission Rules

1. **Storage Type**: Only PUBLIC and PRIVATE allowed
2. **PRIVATE Storage**: User must be storage owner
3. **Parent Folder**: Must be owned by current user if specified
4. **Name Uniqueness**: No conflicts within same parent

## Validation Flow

1. **Token Validation** → Check authentication
2. **Storage Lookup** → Get from cache
3. **Storage Type Check** → PUBLIC/PRIVATE only
4. **Ownership Check** → Validate storage owner for PRIVATE
5. **Parent Validation** → Check parent folder ownership
6. **Name Conflict Check** → Ensure uniqueness
7. **Path Generation** → Build folder hierarchy path
8. **Database Insert** → Create with audit fields

## Error Handling

Consistent error response format:

- `401 UNAUTHORIZED` - Invalid/missing token
- `403 FORBIDDEN` - Permission denied (storage type, ownership)
- `404 NOT_FOUND` - Storage/parent folder not found
- `409 CONFLICT` - Folder name already exists
- `400 BAD_REQUEST` - Invalid request data

## Testing

API collection provides test cases for:

- ✅ Root folder creation in PUBLIC storage
- ✅ Root folder creation in PRIVATE storage (by owner)
- ✅ Subfolder creation under owned parent
- ❌ Folder creation in non-PUBLIC/PRIVATE storage
- ❌ PRIVATE storage access by non-owner
- ❌ Subfolder creation under non-owned parent
- ❌ Duplicate folder name conflicts

## Future Enhancements

The API structure is prepared for future operations:

### Private User APIs (Planned)

- `GET /file/v1/private/storages/{storageCode}/folders` - List folders
- `GET /file/v1/private/folders/{folderId}` - Get folder by ID
- `PUT /file/v1/private/folders/{folderId}/move` - Move folder

### Admin APIs (Planned)

- Full CRUD operations
- Bulk operations
- Advanced folder management

## Compliance with StorageApi Pattern

✅ **API Structure**: Same organization with private/admin sections
✅ **Permission Handler**: Uses `ApiPermissionHandler`
✅ **Token Handling**: `VertxHelper.getTokenFromRoutingContext()`
✅ **Response Format**: `BaseResponse.of(CommonError.*, data)`
✅ **Manager Pattern**: Business logic in `FolderManager`
✅ **Service Integration**: Uses `StorageSharedServices`
✅ **Cache Integration**: Storage cache for permissions
✅ **Database Direct**: No folder caching as requested

## Differences from StorageApi

1. **No Caching**: Folders read directly from DB (as requested)
2. **Permission Focus**: Heavy emphasis on ownership validation
3. **Storage Type Restrictions**: Only PUBLIC/PRIVATE for private users
4. **Parent Validation**: Additional parent folder ownership checks

## Summary

NewFolderApi successfully implements:

- ✅ Learned from StorageApi organization pattern
- ✅ Private user folder creation with strict permissions
- ✅ PUBLIC/PRIVATE storage type restrictions
- ✅ Parent folder ownership validation
- ✅ Storage cache integration for permissions
- ✅ Direct database access for folder data
- ✅ Comprehensive documentation and testing resources
- ✅ Future-ready structure for additional operations

The implementation provides a secure, well-organized foundation for folder management that can be extended with additional operations while maintaining the established security and architectural patterns.
