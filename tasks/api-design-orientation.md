Hãy cập nhật định hướng về các thiết kế api và
tổ chức kĩ thuật liên quan trong file api-design-orientation.md
theo cách đơn giản dễ hiểu để người sau có thể tuân thủ
theo quy tắc này và thực hiện công việc dễ dàng.

Hãy viết tổng quát hóa và dễ hiểu, đừng chi tiết quá
nội dung.

# API Design Orientation

## Tổng quan

Tài liệu này mô tả các nguyên tắc và quy chuẩn thiết kế API cho hệ thống File Storage, nhằm đảm bảo tính nhất quán, bảo mật và dễ bảo trì.

## 1. Cấu trúc API

### 1.1 Phân chia theo vai trò

```
/file/v1/admin/*     - APIs dành cho Admin (full permissions)
/file/v1/private/*   - APIs dành cho User (limited permissions)
/file/v1/public/*    - APIs công khai (no authentication)
```

### 1.2 Tổ chức endpoint trong code

```java
public static void configAPI(Router router) {
    privateReadApi(router);
    privateWriteApi(router);
    privateOtherApi(router);
    adminReadApi(router);
    adminWriteApi(router);
    adminOtherApi(router);
}
```

## 2. Authentication & Authorization

### 2.1 Token-based Authentication

- **Admin APIs**: Yêu cầu Admin Bearer Token
- **Private APIs**: Yêu cầu User Bearer Token
- **Public APIs**: Không cần authentication

### 2.2 Ownership-based Access Control

- Sử dụng field `createdBy` để kiểm soát quyền truy cập
- Private users chỉ truy cập được resources của mình
- Admin có thể truy cập tất cả resources

## 3. API Patterns

### 3.1 Filter Pattern

**Sử dụng POST với DTO thay vì GET với query params cho filter phức tạp**

```
❌ GET /api/resources?keyword=x&status=y&page=0&size=10
✅ POST /api/resources/filter + JSON body
```

**Lý do**:

- Hỗ trợ filter phức tạp (arrays, objects)
- Không giới hạn độ dài URL
- Dễ validate và type-safe

### 3.2 CRUD Operations

```
POST   /api/resources         - Create
POST   /api/resources/filter  - Read/List với filter
GET    /api/resources/:id     - Read single
PUT    /api/resources/:id     - Update
DELETE /api/resources/:id     - Hard delete
PUT    /api/resources/:id/soft-delete - Soft delete
```

### 3.3 Soft Delete vs Hard Delete

- **Soft Delete**: Chuyển status thành `DELETED`, data vẫn tồn tại
- **Hard Delete**: Xóa hoàn toàn khỏi database
- Admin có cả 2 quyền, Private user không có quyền delete

## 4. Data Models

### 4.1 Entity Structure

```java
// Base fields cho mọi entity
private String code;           // Unique identifier
private String name;           // Display name
private String description;    // Optional description
private String status;         // Enum: ACTIVE, INACTIVE, DELETED
private Long createdBy;        // Owner ID
private Long createdAt;        // Timestamp (epoch millis)
private Long updatedAt;        // Timestamp (epoch millis)
```

### 4.2 Filter DTO Structure

```java
// Base filter pattern
private Long ownerId;          // For ownership filtering
private String keyword;        // Search in name/description
private List<String> statuses; // Status filtering
private Long createdFrom;      // Date range filtering
private Long createdTo;
private Long updatedFrom;
private Long updatedTo;
private Long page;             // Pagination
private Long size;
```

## 5. Service Layer Design

### 5.1 Service Method Patterns

```java
// Basic CRUD
T create(T entity);
T getByCode(String code);
T update(T entity);
void deleteByCode(String code);
void softDeleteByCode(String code);

// Ownership-based methods
T get(Long ownerId, String code);
void delete(Long ownerId, String code);

// Filter methods
List<T> filter(FilterDTO filter);
List<T> filter(Long ownerId, FilterDTO filter);

// Utility methods
boolean existsByCode(String code);
```

### 5.2 Manager Pattern

- Manager layer nằm giữa API và Service
- Xử lý business logic, token validation
- Tự động set `createdBy` từ token
- Validate ownership cho private users

## 6. Database Design

### 6.1 Table Naming

- Sử dụng plural: `storages`, `files`, `folders`
- Snake_case: `created_by`, `updated_at`

### 6.2 Standard Fields

```sql
-- Required fields
code VARCHAR(255) NOT NULL UNIQUE,
name VARCHAR(255) NOT NULL,
status VARCHAR(50) NOT NULL,
created_by BIGINT NOT NULL,
created_at BIGINT NOT NULL,
updated_at BIGINT NOT NULL,

-- Optional fields
description TEXT,
metadata JSONB
```

## 7. Response Format

### 7.1 Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 7.2 Error Response

```json
{
  "code": 404,
  "message": "Resource not found",
  "data": null
}
```

### 7.3 Pagination Response

```json
{
  "code": 200,
  "message": "success",
  "data": [ ... ],
  "pagination": {
    "page": 0,
    "size": 10
  }
}
```

**Note**: Không trả về `total` để tối ưu performance

## 8. Status Management

### 8.1 Standard Status Enum

```java
public enum Status {
    ACTIVE,    // Đang hoạt động
    INACTIVE,  // Tạm ngưng
    DELETED    // Đã xóa mềm
}
```

### 8.2 Status Transitions

- `ACTIVE` ↔ `INACTIVE`: Admin có thể chuyển đổi
- `ACTIVE/INACTIVE` → `DELETED`: Soft delete
- `DELETED` → Hard delete: Xóa khỏi DB

## 9. Security Best Practices

### 9.1 Input Validation

- Validate tất cả input từ client
- Sử dụng DTO để type-safe
- Kiểm tra required fields

### 9.2 Access Control

- Luôn check ownership với private users
- Admin có thể bypass ownership check
- Validate token trước khi xử lý request

### 9.3 SQL Injection Prevention

- Sử dụng parameterized queries
- Validate input trước khi build dynamic SQL

## 10. Performance Optimization

### 10.1 Database Queries

- Sử dụng `SELECT 1` cho existence check
- Tránh `SELECT *`, chỉ lấy fields cần thiết
- Sử dụng index cho các fields filter thường xuyên

### 10.2 Pagination

- Luôn implement pagination cho list APIs
- Default page size: 10
- Max page size: 100

## 11. Error Handling

### 11.1 HTTP Status Codes

- `200`: Success
- `400`: Bad Request (validation error)
- `401`: Unauthorized (invalid token)
- `403`: Forbidden (insufficient permission)
- `404`: Not Found
- `409`: Conflict (duplicate resource)

### 11.2 Error Messages

- Sử dụng message rõ ràng, dễ hiểu
- Không expose sensitive information
- Consistent format cho tất cả errors

## 12. Code Organization

### 12.1 Package Structure

```
com.defi.storage/
├── entity/          # Data models
├── service/         # Business logic
│   └── impl/        # Service implementations
├── manager/         # API business logic
├── vertx/
│   └── api/         # REST endpoints
└── util/            # Helper utilities
```

### 12.2 File Naming

- Entity: `ResourceEntity.java`
- Service: `ResourceService.java` + `ResourceServiceImpl.java`
- Manager: `ResourceManager.java`
- API: `ResourceApi.java`
- Filter: `ResourceFilter.java`

## 13. Testing Strategy

### 13.1 API Testing

- Test với cả Admin và Private user tokens
- Test ownership validation
- Test filter combinations
- Test pagination edge cases

### 13.2 Unit Testing

- Mock external dependencies
- Test business logic trong Manager/Service
- Test validation logic

---

## Checklist cho Developer

Khi implement API mới, hãy đảm bảo:

- [ ] Phân chia đúng Admin/Private/Public endpoints
- [ ] Implement ownership-based access control
- [ ] Sử dụng POST /filter cho complex filtering
- [ ] Có cả soft delete và hard delete (nếu cần)
- [ ] Response format nhất quán
- [ ] Proper error handling
- [ ] Input validation
- [ ] Pagination cho list APIs
- [ ] Database migration script
- [ ] Update API documentation
- [ ] Write tests

---

_Tài liệu này sẽ được cập nhật theo thời gian khi có thêm patterns mới._
