# Permission Orientation

## Cách sử dụng Permission System

### 1. Resource & Actions

```java
// Cố định trong hệ thống
Resource: "file"
Actions: "read", "write", "upload", "download"
```

### 2. Kiểm tra quyền

```java
// Sử dụng thư viện có sẵn
boolean hasPermission = PermissionChecker.getInstance()
    .getEnforcer()
    .enforce(token, "file", "read");
```

### 3. Patterns cho từng loại API

#### Admin API (cần check permission)

```java
router.post("/file/v1/admin/storages").handler(ctx -> {
    Token token = getTokenFromContext(ctx);

    // Check quyền write
    if (!PermissionChecker.getInstance()
            .getEnforcer()
            .enforce(token, "file", "write")) {
        ctx.response()
            .setStatusCode(403)
            .end(Json.encode(BaseResponse.forbidden("Insufficient permission")));
        return;
    }

    // Xử lý logic bình thường
    // ...
});
```

#### Private API (chỉ cần token)

```java
router.post("/file/v1/private/storages/filter").handler(ctx -> {
    Token token = getTokenFromContext(ctx);

    if (token == null) {
        ctx.response()
            .setStatusCode(401)
            .end(Json.encode(BaseResponse.unauthorized("Token required")));
        return;
    }

    // Xử lý logic bình thường
    // ...
});
```

#### Public API (không cần gì)

```java
router.get("/file/v1/public/version").handler(ctx -> {
    // Xử lý logic trực tiếp
    // ...
});
```

### 4. Actions mapping

```java
// READ
POST /file/v1/admin/storages/filter -> "read"
GET  /file/v1/admin/storages/:code  -> "read"

// WRITE
POST   /file/v1/admin/storages      -> "write"
PUT    /file/v1/admin/storages/:code -> "write"
DELETE /file/v1/admin/storages/:code -> "write"

// UPLOAD
POST /file/v1/admin/files/upload    -> "upload"

// DOWNLOAD
GET  /file/v1/admin/files/:id/download -> "download"
```

### 5. Template code

```java
// Admin API template
if (!PermissionChecker.getInstance()
        .getEnforcer()
        .enforce(token, "file", "ACTION")) {
    ctx.response()
        .setStatusCode(403)
        .end(Json.encode(BaseResponse.forbidden("Insufficient permission")));
    return;
}

// Private API template
if (token == null) {
    ctx.response()
        .setStatusCode(401)
        .end(Json.encode(BaseResponse.unauthorized("Token required")));
    return;
}
```

### 6. Lưu ý

- Mỗi admin API phải check permission riêng
- Không thể tổng quát hóa được
- Sử dụng BaseResponse cho error
- Set đúng HTTP status code (401/403)
