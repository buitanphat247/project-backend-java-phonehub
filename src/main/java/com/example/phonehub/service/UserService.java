package com.example.phonehub.service;

import com.example.phonehub.dto.CreateUserRequest;
import com.example.phonehub.dto.UserDto;
import com.example.phonehub.entity.Role;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.RoleRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.utils.UserUtils;
import com.example.phonehub.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRankService rankService;

    // Helper method: Tự động cập nhật rank của user dựa trên điểm số
    private void updateUserRank(User user) {
        if (user == null) return;
        
        Integer points = user.getPoints() != null ? user.getPoints() : 0;
        rankService.getRankEntityByPoints(points).ifPresent(user::setRank);
    }
    
    // Helper method: Đảm bảo user có rank (nếu null thì tự động set dựa trên points)
    private void ensureUserHasRank(User user) {
        if (user == null) return;
        
        // Nếu user chưa có rank hoặc rank bị null, tự động set rank dựa trên points
        if (user.getRank() == null) {
            updateUserRank(user);
            // Nếu user đã có ID (đã persist), lưu lại để persist rank vào DB
            if (user.getId() != null) {
                userRepository.save(user);
            }
        }
    }
    
    // Helper method: Convert User to DTO và đảm bảo có rank (full object - cho chi tiết)
    private UserDto toDtoWithRank(User user) {
        if (user == null) return null;
        
        // Đảm bảo user có rank trước khi convert
        ensureUserHasRank(user);
        
        // Dùng toDtoFull để trả về full role và rank objects
        return UserUtils.toDtoFull(user);
    }

    // Lấy users với phân trang - chỉ thông tin cơ bản, chỉ có roleId và rankId
    public Page<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        // Dùng findAllBasic để không load role và rank (tránh N+1 query, tối ưu performance)
        Page<User> userPage = userRepository.findAllBasic(pageable);
        
        // Dùng toDtoPageBasic để chỉ convert roleId và rankId (không có full objects)
        return UserUtils.toDtoPageBasic(userPage);
    }

    // Lấy user theo ID (kèm full role và rank objects)
    public Optional<UserDto> getUserById(Integer id) {
        Optional<User> user = userRepository.findById(id); // Đã có @EntityGraph để load role và rank
        return user.map(this::toDtoWithRank);
    }

    // Lấy user theo username (kèm rank)
    public Optional<UserDto> getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(this::toDtoWithRank);
    }

    // Lấy user theo email (kèm rank)
    public Optional<UserDto> getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(this::toDtoWithRank);
    }

    // Tìm kiếm user theo username hoặc email với phân trang (kèm rank)
    public Page<UserDto> searchByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.searchByUsernameOrEmail(keyword, pageable);
        
        // Đảm bảo tất cả users đều có rank
        userPage.getContent().forEach(this::ensureUserHasRank);
        
        return UserUtils.toDtoPage(userPage);
    }

    // Tạo user mới với role ID = 3 (cấp thấp nhất)
    public UserDto createUser(CreateUserRequest request) {
        // Kiểm tra password là required khi create
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("User with username '" + request.getUsername() + "' already exists");
        }

        // Kiểm tra email đã tồn tại chưa (nếu có email)
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email '" + request.getEmail() + "' already exists");
        }

        // Lấy role: nếu có roleId thì dùng, không thì mặc định role ID = 3 (cấp thấp nhất)
        Role userRole;
        if (request.getRoleId() != null) {
            userRole = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));
        } else {
            userRole = roleRepository.findById(3)
                    .orElseThrow(() -> new RuntimeException("Default role with ID 3 not found"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtils.encodeMD5(request.getPassword())); // Mã hóa password MD5
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setAvatar(request.getAvatar());
        user.setBirthday(request.getBirthday());
        user.setRole(userRole);
        
        // Tự động set rank dựa trên điểm số (mặc định 0)
        updateUserRank(user);
        
        User savedUser = userRepository.save(user);
        // Đảm bảo rank được load sau khi save
        return toDtoWithRank(savedUser);
    }

    // Cập nhật user
    public UserDto updateUser(Integer id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Chỉ update username nếu được truyền vào
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            // Kiểm tra username mới có trùng với user khác không
            if (!user.getUsername().equals(request.getUsername()) && 
                userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("User with username '" + request.getUsername() + "' already exists");
            }
            user.setUsername(request.getUsername());
        }

        // Chỉ update password nếu được truyền vào
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(PasswordUtils.encodeMD5(request.getPassword()));
        }

        // Chỉ update email nếu được truyền vào
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String email = request.getEmail().trim();
            // Validate email format
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new RuntimeException("Email format is invalid");
            }
            // Kiểm tra email mới có trùng với user khác không
            if (user.getEmail() == null || !email.equals(user.getEmail())) {
                if (userRepository.existsByEmail(email)) {
                    throw new RuntimeException("User with email '" + email + "' already exists");
                }
            }
            user.setEmail(email);
        }

        // Chỉ update phone nếu được truyền vào
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone());
        }

        // Chỉ update address nếu được truyền vào
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress().trim().isEmpty() ? null : request.getAddress());
        }

        // Chỉ update avatar nếu được truyền vào
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar().trim().isEmpty() ? null : request.getAvatar());
        }

        // Chỉ update birthday nếu được truyền vào
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }

        // Chỉ update role nếu được truyền vào
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));
            user.setRole(role);
        }
        
        // Tự động cập nhật rank dựa trên điểm số hiện tại của user (đảm bảo rank luôn đúng)
        updateUserRank(user);
        
        User updatedUser = userRepository.save(user);
        // Đảm bảo rank được load sau khi save
        return toDtoWithRank(updatedUser);
    }

    // Xóa user
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // Kiểm tra user có tồn tại không
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Verify password cho user
    public boolean verifyUserPassword(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return PasswordUtils.verifyPassword(password, user.get().getPassword());
        }
        return false;
    }
    
    // Cập nhật điểm số của user (tự động cập nhật rank)
    public UserDto updateUserPoints(Integer userId, Integer points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (points == null || points < 0) {
            throw new RuntimeException("Points must be a non-negative number");
        }
        
        user.setPoints(points);
        updateUserRank(user);
        
        User updatedUser = userRepository.save(user);
        return toDtoWithRank(updatedUser);
    }
    
    // Cộng điểm cho user (tự động cập nhật rank)
    public UserDto addPointsToUser(Integer userId, Integer pointsToAdd) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (pointsToAdd == null || pointsToAdd < 0) {
            throw new RuntimeException("Points to add must be a non-negative number");
        }
        
        Integer currentPoints = user.getPoints() != null ? user.getPoints() : 0;
        user.setPoints(currentPoints + pointsToAdd);
        updateUserRank(user);
        
        User updatedUser = userRepository.save(user);
        return toDtoWithRank(updatedUser);
    }
}
