package com.example.phonehub.service;

import com.example.phonehub.dto.CreateUserRequest;
import com.example.phonehub.dto.UserDto;
import com.example.phonehub.entity.Role;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.RoleRepository;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.service.helper.UserHelper;
import com.example.phonehub.service.redis_cache.UserCacheService;
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
    private UserCacheService userCacheService;
    
    @Autowired
    private UserHelper userHelper;
    

    // Lấy users với phân trang (chỉ roleId và rankId)
    public Page<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<User> userPage = userRepository.findAllBasic(pageable);
        return UserUtils.toDtoPageBasic(userPage);
    }

    // Lấy user theo ID - với Redis cache
    public Optional<UserDto> getUserById(Integer id) {
        if (id == null) return Optional.empty();
        
        UserDto cachedUser = userCacheService.getUserFromCacheById(id);
        return userCacheService.getUserWithCacheStrategy(
            cachedUser,
            () -> userRepository.findById(id),
            userHelper::toDtoWithRank
        );
    }

    // Lấy user theo username - với Redis cache
    public Optional<UserDto> getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return Optional.empty();
        
        UserDto cachedUser = userCacheService.getUserFromCacheByUsername(username);
        return userCacheService.getUserWithCacheStrategy(
            cachedUser,
            () -> userRepository.findByUsername(username),
            userHelper::toDtoWithRank
        );
    }

    // Lấy user theo email - với Redis cache
    public Optional<UserDto> getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return Optional.empty();
        
        UserDto cachedUser = userCacheService.getUserFromCacheByEmail(email);
        return userCacheService.getUserWithCacheStrategy(
            cachedUser,
            () -> userRepository.findByEmail(email),
            userHelper::toDtoWithRank
        );
    }

    // Tìm kiếm user theo keyword (username/email) với phân trang
    public Page<UserDto> searchByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.searchByUsernameOrEmail(keyword, pageable);
        userPage.getContent().forEach(userHelper::ensureUserHasRank);
        return UserUtils.toDtoPage(userPage);
    }

    // Tạo user mới (role mặc định = 3)
    public UserDto createUser(CreateUserRequest request) {
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("User with username '" + request.getUsername() + "' already exists");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email '" + request.getEmail() + "' already exists");
        }

        Role userRole = request.getRoleId() != null
            ? roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()))
            : roleRepository.findById(3)
                .orElseThrow(() -> new RuntimeException("Default role with ID 3 not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtils.encodeMD5(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setAvatar(request.getAvatar());
        user.setBirthday(request.getBirthday());
        user.setRole(userRole);
        
        userHelper.updateUserRank(user);
        
        User savedUser = userRepository.save(user);
        UserDto savedDto = userHelper.toDtoWithRank(savedUser);
        userCacheService.saveUserToCache(savedDto);
        
        return savedDto;
    }

    // Cập nhật user - với Redis cache
    public UserDto updateUser(Integer id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (!user.getUsername().equals(request.getUsername()) && 
                userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("User with username '" + request.getUsername() + "' already exists");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(PasswordUtils.encodeMD5(request.getPassword()));
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String email = request.getEmail().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new RuntimeException("Email format is invalid");
            }
            if (user.getEmail() == null || !email.equals(user.getEmail())) {
                if (userRepository.existsByEmail(email)) {
                    throw new RuntimeException("User with email '" + email + "' already exists");
                }
            }
            user.setEmail(email);
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone());
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress().trim().isEmpty() ? null : request.getAddress());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar().trim().isEmpty() ? null : request.getAvatar());
        }

        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));
            user.setRole(role);
        }
        
        userHelper.updateUserRank(user);
        
        String oldUsername = user.getUsername();
        String oldEmail = user.getEmail();
        
        User updatedUser = userRepository.save(user);
        UserDto updatedDto = userHelper.toDtoWithRank(updatedUser);
        
        userCacheService.removeUserFromCache(user.getId(), oldUsername, oldEmail);
        userCacheService.saveUserToCache(updatedDto);
        
        return updatedDto;
    }

    // Xóa user - với Redis cache invalidation
    public void deleteUser(Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        User user = userOpt.get();
        userRepository.deleteById(id);
        userCacheService.removeUserFromCache(user.getId(), user.getUsername(), user.getEmail());
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Verify password
    public boolean verifyUserPassword(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return PasswordUtils.verifyPassword(password, user.get().getPassword());
        }
        return false;
    }
    
    // Cập nhật điểm số - với Redis cache
    public UserDto updateUserPoints(Integer userId, Integer points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (points == null || points < 0) {
            throw new RuntimeException("Points must be a non-negative number");
        }
        
        String oldUsername = user.getUsername();
        String oldEmail = user.getEmail();
        
        user.setPoints(points);
        userHelper.updateUserRank(user);
        
        User updatedUser = userRepository.save(user);
        UserDto updatedDto = userHelper.toDtoWithRank(updatedUser);
        
        userCacheService.removeUserFromCache(userId, oldUsername, oldEmail);
        userCacheService.saveUserToCache(updatedDto);
        
        return updatedDto;
    }
    
    // Cộng điểm - với Redis cache
    public UserDto addPointsToUser(Integer userId, Integer pointsToAdd) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (pointsToAdd == null || pointsToAdd < 0) {
            throw new RuntimeException("Points to add must be a non-negative number");
        }
        
        String oldUsername = user.getUsername();
        String oldEmail = user.getEmail();
        
        Integer currentPoints = user.getPoints() != null ? user.getPoints() : 0;
        user.setPoints(currentPoints + pointsToAdd);
        userHelper.updateUserRank(user);
        
        User updatedUser = userRepository.save(user);
        UserDto updatedDto = userHelper.toDtoWithRank(updatedUser);
        
        userCacheService.removeUserFromCache(userId, oldUsername, oldEmail);
        userCacheService.saveUserToCache(updatedDto);
        
        return updatedDto;
    }
}
