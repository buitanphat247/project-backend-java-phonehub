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

    // Lấy users với phân trang
    public Page<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        return UserUtils.toDtoPage(userPage);
    }

    // Lấy user theo ID
    public Optional<UserDto> getUserById(Integer id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(UserUtils::toDto);
    }

    // Lấy user theo username
    public Optional<UserDto> getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(UserUtils::toDto);
    }

    // Lấy user theo email
    public Optional<UserDto> getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(UserUtils::toDto);
    }

    // Tạo user mới với role ID = 3 (cấp thấp nhất)
    public UserDto createUser(CreateUserRequest request) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("User with username '" + request.getUsername() + "' already exists");
        }

        // Kiểm tra email đã tồn tại chưa (nếu có email)
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email '" + request.getEmail() + "' already exists");
        }

        // Lấy role ID = 3 (cấp thấp nhất)
        Role defaultRole = roleRepository.findById(3)
                .orElseThrow(() -> new RuntimeException("Default role with ID 3 not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtils.encodeMD5(request.getPassword())); // Mã hóa password MD5
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setAvatar(request.getAvatar());
        user.setRole(defaultRole); // Gán role ID = 3
        
        User savedUser = userRepository.save(user);
        return UserUtils.toDto(savedUser);
    }

    // Cập nhật user
    public UserDto updateUser(Integer id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Kiểm tra username mới có trùng với user khác không
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("User with username '" + request.getUsername() + "' already exists");
        }

        // Kiểm tra email mới có trùng với user khác không
        if (request.getEmail() != null && 
            !user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with email '" + request.getEmail() + "' already exists");
        }

        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtils.encodeMD5(request.getPassword())); // Mã hóa password MD5
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setAvatar(request.getAvatar());
        
        User updatedUser = userRepository.save(user);
        return UserUtils.toDto(updatedUser);
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
}
