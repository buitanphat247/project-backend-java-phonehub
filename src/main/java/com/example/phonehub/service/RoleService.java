package com.example.phonehub.service;

import com.example.phonehub.dto.CreateRoleRequest;
import com.example.phonehub.dto.RoleDto;
import com.example.phonehub.entity.Role;
import com.example.phonehub.repository.RoleRepository;
import com.example.phonehub.service.redis_cache.RoleCacheService;
import com.example.phonehub.utils.RoleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private RoleCacheService roleCacheService;

    /**
     * 📖 Lấy roles với phân trang - với Redis cache
     * 
     * Flow: Cache → DB → Cache lại
     * 
     * @param page Số trang
     * @param size Số lượng mỗi trang
     * @return Page<RoleDto>
     */
    public Page<RoleDto> getAllRoles(int page, int size) {
        // Lưu ý: Cache phân trang có thể phức tạp, nên để đơn giản không cache phân trang
        // Hoặc có thể cache từng trang riêng biệt nếu cần
        Pageable pageable = PageRequest.of(page, size);
        Page<Role> rolePage = roleRepository.findAll(pageable);
        return RoleUtils.toDtoPage(rolePage);
    }

    /**
     * 📖 Lấy role theo ID - với Redis cache
     * 
     * Flow: Cache → DB → Cache lại
     * 
     * @param id ID của role
     * @return Optional<RoleDto> - Empty nếu không tìm thấy
     */
    public Optional<RoleDto> getRoleById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        
        RoleDto cachedRole = roleCacheService.getRoleFromCacheById(id);
        return roleCacheService.getRoleWithCacheStrategy(
            cachedRole,
            () -> roleRepository.findById(id)
        );
    }

    /**
     * 📖 Lấy role theo tên - với Redis cache
     * 
     * Flow: Cache → DB → Cache lại
     * 
     * @param name Tên của role
     * @return Optional<RoleDto> - Empty nếu không tìm thấy
     */
    public Optional<RoleDto> getRoleByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        
        RoleDto cachedRole = roleCacheService.getRoleFromCacheByName(name);
        return roleCacheService.getRoleWithCacheStrategy(
            cachedRole,
            () -> roleRepository.findByName(name)
        );
    }

    /**
     * ✏️ Tạo role mới - với cache invalidation
     */
    public RoleDto createRole(CreateRoleRequest request) {
        // Kiểm tra role đã tồn tại chưa
        if (roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Role with name '" + request.getName() + "' already exists");
        }

        Role role = new Role();
        role.setName(request.getName());

        Role savedRole = roleRepository.save(role);
        RoleDto savedDto = RoleUtils.toDto(savedRole);
        
        // 💾 Cache role mới tạo
        roleCacheService.saveRoleToCache(savedDto);
        
        // 🗑️ Xóa cache phân trang (vì đã thêm role mới)
        roleCacheService.invalidateRolePageCache();
        
        return savedDto;
    }

    /**
     * ✏️ Cập nhật role - với cache invalidation
     */
    public RoleDto updateRole(Integer id, CreateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // 💾 Lưu thông tin cũ để invalidate cache
        String oldName = role.getName();

        // Kiểm tra tên mới có trùng với role khác không
        if (!role.getName().equals(request.getName()) &&
                roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Role with name '" + request.getName() + "' already exists");
        }

        role.setName(request.getName());
        Role updatedRole = roleRepository.save(role);
        RoleDto updatedDto = RoleUtils.toDto(updatedRole);
        
        // 🗑️ Xóa cache cũ (name có thể đã thay đổi)
        roleCacheService.removeRoleFromCache(id, oldName);
        
        // 💾 Cache lại với thông tin mới
        roleCacheService.saveRoleToCache(updatedDto);
        
        // 🗑️ Xóa cache phân trang (vì đã update)
        roleCacheService.invalidateRolePageCache();
        
        return updatedDto;
    }

    /**
     * 🗑️ Xóa role - với cache invalidation
     * 
     * Flow:
     * 1. Lấy thông tin role (để lấy name cho cache invalidation)
     * 2. Xóa từ database
     * 3. Xóa khỏi Redis cache
     * 
     * @param id ID của role cần xóa
     */
    public void deleteRole(Integer id) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("Role not found with id: " + id);
        }
        
        Role role = roleOpt.get();
        String roleName = role.getName();
        
        // Xóa từ database
        roleRepository.deleteById(id);
        
        // 🗑️ Xóa khỏi Redis cache
        roleCacheService.removeRoleFromCache(id, roleName);
        
        // 🗑️ Xóa cache phân trang (vì đã xóa role)
        roleCacheService.invalidateRolePageCache();
        
        // Lưu ý: Nếu có users đang dùng role này, cần xử lý (set về role mặc định)
    }

    /**
     * 🔍 Kiểm tra role có tồn tại không
     * 
     * @param name Tên của role
     * @return true nếu tồn tại, false nếu không
     */
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
}
