package com.example.phonehub.service;

import com.example.phonehub.dto.CreateRoleRequest;
import com.example.phonehub.dto.RoleDto;
import com.example.phonehub.entity.Role;
import com.example.phonehub.repository.RoleRepository;
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

    // Lấy roles với phân trang
    public Page<RoleDto> getAllRoles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Role> rolePage = roleRepository.findAll(pageable);
        return RoleUtils.toDtoPage(rolePage);
    }

    // Lấy role theo ID
    public Optional<RoleDto> getRoleById(Integer id) {
        Optional<Role> role = roleRepository.findById(id);
        return role.map(RoleUtils::toDto);
    }

    // Lấy role theo tên
    public Optional<RoleDto> getRoleByName(String name) {
        Optional<Role> role = roleRepository.findByName(name);
        return role.map(RoleUtils::toDto);
    }

    // Tạo role mới
    public RoleDto createRole(CreateRoleRequest request) {
        // Kiểm tra role đã tồn tại chưa
        if (roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Role with name '" + request.getName() + "' already exists");
        }

        Role role = new Role();
        role.setName(request.getName());

        Role savedRole = roleRepository.save(role);
        return RoleUtils.toDto(savedRole);
    }

    // Cập nhật role
    public RoleDto updateRole(Integer id, CreateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // Kiểm tra tên mới có trùng với role khác không
        if (!role.getName().equals(request.getName()) &&
                roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Role with name '" + request.getName() + "' already exists");
        }

        role.setName(request.getName());
        Role updatedRole = roleRepository.save(role);
        return RoleUtils.toDto(updatedRole);
    }

    // Xóa role
    public void deleteRole(Integer id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

    // Kiểm tra role có tồn tại không
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
}
