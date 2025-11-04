# 📮 Postman Collection Generator

Script để tự động generate Postman Collection từ Swagger/OpenAPI documentation.

## 📋 Yêu cầu

- Node.js (v12+)
- Server đang chạy với Swagger/OpenAPI enabled

## 🚀 Cách sử dụng

### 1. Chạy script cơ bản (mặc định: http://localhost:8080)

```bash
node scripts/generate-postman.js
```

### 2. Chỉ định URL server khác

```bash
node scripts/generate-postman.js --url http://localhost:3000
```

### 3. Chỉ định file output

```bash
node scripts/generate-postman.js --url http://localhost:8080 --output my-api.postman.json
```

### 4. Sử dụng npm scripts (nếu có package.json)

```bash
cd scripts
npm run generate
npm run generate:dev
npm run generate:prod
```

## 📁 Output

Script sẽ tạo file `phonehub.postman.json` (hoặc tên file bạn chỉ định) chứa:
- ✅ Tất cả endpoints từ Swagger/OpenAPI
- ✅ Request methods (GET, POST, PUT, DELETE, etc.)
- ✅ Request parameters (query, path, header)
- ✅ Request body examples
- ✅ Response examples
- ✅ JWT Bearer Token authentication
- ✅ Được group theo tags/folders

## 📥 Import vào Postman

1. Mở Postman
2. Click **Import** (góc trên bên trái)
3. Chọn file `phonehub.postman.json`
4. Collection sẽ được import với tất cả requests

## 🔧 Cấu hình

### Biến môi trường trong Postman

Sau khi import, bạn có thể tạo environment variables:
- `baseUrl`: URL của server (mặc định: http://localhost:8080)
- `accessToken`: JWT token để authenticate (lấy từ login API)

### Ví dụ Environment Variables

```json
{
  "baseUrl": "http://localhost:8080",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## 🐛 Troubleshooting

### Lỗi: "Request failed: connect ECONNREFUSED"
- Đảm bảo server đang chạy
- Kiểm tra URL và port đúng chưa

### Lỗi: "HTTP 404"
- Kiểm tra Swagger/OpenAPI đã được enable chưa
- Kiểm tra endpoint `/api-docs` có tồn tại không

### Lỗi: "Failed to parse JSON"
- Kiểm tra response từ server có đúng format OpenAPI không
- Thử truy cập `/api-docs` trực tiếp trên browser

## 📝 Notes

- Script tự động fetch OpenAPI spec từ endpoint `/api-docs`
- Tất cả endpoints được group theo tags từ Swagger
- JWT Bearer Token được tự động thêm vào header nếu endpoint yêu cầu authentication
- Request body examples được generate từ JSON Schema

