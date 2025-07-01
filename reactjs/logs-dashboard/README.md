# 📊 Logs Dashboard UI

Giao diện trực quan để xem log từ hệ thống **OpenSearch**.  
Dự án này được xây bằng ReactJS, hiển thị biểu đồ, bảng log, popup chi tiết và hỗ trợ lọc/tìm kiếm.

---

## 🚀 Tính năng chính

- Hiển thị biểu đồ thống kê theo Action, Target Type, ngày
- Lọc theo Action, Target, Subject, khoảng ngày
- Tìm kiếm nhanh log
- Phân trang danh sách log
- Popup xem chi tiết từng log
- Xuất CSV toàn bộ dữ liệu hiện có
- Tự động cập nhật mỗi 10 giây (tuỳ chọn)

---

## 🧰 Yêu cầu

- **Node.js ≥ 18**
- **npm ≥ 9**
- Backend OpenSearch đã khởi chạy tại `http://localhost:9200` (dữ liệu trong index `storage_event`)

---

## 📦 Cài đặt & chạy (không cần dùng Git)

> ⚠️ Hướng dẫn này dành cho người chưa cài gì.

### 1. Cài Node.js

- Truy cập: https://nodejs.org
- Tải bản **LTS (Long Term Support)** và cài đặt

### 2. Bật quyền chạy lệnh trên Windows (PowerShell)
```
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```
### 3. Tải mã nguồn
- Nhận file .zip chứa thư mục dự án từ người phát triển

- Giải nén vào thư mục, ví dụ D:\logs-dashboard

### 4. Cài thư viện
```
cd ./logs-dashboard
npm install
```

### 5. Chạy ứng dụng

```
npm start
```
- Mở trình duyệt tại: http://localhost:3000

### 🧩 Cấu trúc dự án
```
logs-dashboard-ui/
├── public/
│   └── index.html
├── src/
│   ├── App.js              # File giao diện chính
│   ├── opensearch.js       # Kết nối tới OpenSearch
│   └── ...                 # Các file khác nếu có
├── package.json
└── README.md
```

### ⚙️ Cấu hình kết nối OpenSearch
Trong file src/opensearch.js, chỉnh thông tin nếu backend khác:
```
const OPENSEARCH_URL = 'http://localhost:9200';
const AUTH = 'Basic ' + btoa('admin:SNTB@13nkt'); // Đổi user/pass tại đây
```

### 🛠 Build production
```
npm run build
```
- Thư mục build/ sẽ chứa các file sẵn sàng để deploy lên server (nginx, apache, v.v...)

### 🧪 Ví dụ dữ liệu log
- Dữ liệu từ index storage_event trong OpenSearch nên có dạng:

```
{
  "created_at": "2025-06-24T12:00:00Z",
  "action": "delete",
  "target_type": "user",
  "target_id": 42,
  "subject_type": "admin",
  "subject_id": 1,
  "correlation_id": "abc-xyz-123",
  "data": {
    "ip": "127.0.0.1",
    "message": "Deleted user account"
  }
}
```

### 🧯 Lỗi thường gặp
Lỗi	Nguyên nhân & cách xử lý

- npm: command not found -->	NodeJS chưa cài hoặc cần khởi động lại máy
- Không hiển thị dữ liệu -->	OpenSearch chưa chạy hoặc không có dữ liệu
CORS error khi fetch -->	Cần bật CORS trong cấu hình OpenSearch

📬 Liên hệ
Dev: [BXH]

Email: [email@example.com]



