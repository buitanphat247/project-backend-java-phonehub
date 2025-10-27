# Hướng dẫn sử dụng Authentication Annotations

## Cấu hình Route Authentication

### 1. Annotation @Public
Đánh dấu route không cần đăng nhập

**Ví dụ:**
```java
@RestController
@RequestMapping("/api/v1/products")
@Public  // Tất cả endpoint trong controller này đều public
public class ProductController {
    
    @GetMapping("/list")
    public ResponseEntity<?> getAllProducts() {
        // Không cần JWT token
        return ResponseEntity.ok(products);
    }
}
```

Hoặc chỉ một method cụ thể:
```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    
    @GetMapping("/public")
    @Public  // Chỉ endpoint này là public - không cần đăng nhập
    public ResponseEntity<?> getPublicProducts() {
        return ResponseEntity.ok(publicProducts);
    }
    
    @GetMapping("/private")
    @RequiresAuth  // Chỉ endpoint này cần đăng nhập
    public ResponseEntity<?> getPrivateProducts() {
        return ResponseEntity.ok(privateProducts);
    }
}
```

### Ví dụ: Controller có cả Public và Protected routes
```java
@RestController
@RequestMapping("/api/v1/products")
@Public  // Mặc định tất cả là public
public class ProductController {
    
    @GetMapping("/list")
    // Không cần annotation - kế thừa @Public từ class
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/featured")
    // Không cần annotation - public
    public ResponseEntity<?> getFeaturedProducts() {
        return ResponseEntity.ok(featuredProducts);
    }
    
    @PostMapping("/create")
    @RequiresAuth  // Endpoint này CẦN đăng nhập
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok("Created");
    }
    
    @DeleteMapping("/{id}")
    @RequiresAuth(roles = {"ADMIN"})  // Chỉ ADMIN mới xóa được
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        return ResponseEntity.ok("Deleted");
    }
}
```

### 2. Annotation @RequiresAuth
Đánh dấu route cần đăng nhập (mặc định)

**Ví dụ:**
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiresAuth  // Tất cả endpoint cần đăng nhập
public class UserController {
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        // Cần JWT token
        return ResponseEntity.ok(userProfile);
    }
    
    @DeleteMapping("/{id}")
    @RequiresAuth(roles = {"ADMIN"})  // Chỉ ADMIN mới được xóa
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        // Cần JWT token + role ADMIN
        return ResponseEntity.ok("Deleted");
    }
}
```

### 3. Cách sử dụng JWT Token

**Đăng nhập để lấy token:**
```bash
POST /api/v1/auth/signin
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "username": "your_username"
  }
}
```

**Sử dụng token để gọi API:**
```bash
GET /api/v1/users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 4. Endpoints tự động Public (không cần annotation)

- `/api/v1/auth/**` - Authentication endpoints
- `/swagger-ui/**` - Swagger UI
- `/api-docs/**` - API Documentation
- `/` - Home page
- `/actuator/**` - Health check

### 5. Kiểm tra User trong Controller

Bạn có thể lấy thông tin user hiện tại trong controller:

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        // Lấy username từ authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Hoặc lấy full UserDetails
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        return ResponseEntity.ok(userDetails);
    }
}
```

## Lưu ý

1. Mặc định TẤT CẢ endpoint đều cần authentication
2. Sử dụng `@Public` để bypass authentication
3. Sử dụng `@RequiresAuth` để yêu cầu authentication (có thể kèm role)
4. JWT token có thời gian sống (development: 1 giờ, production: 15 phút)
5. Sử dụng `refreshToken` để lấy token mới khi hết hạn

