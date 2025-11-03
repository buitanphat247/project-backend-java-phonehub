package com.example.phonehub.repository;

import com.example.phonehub.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    @EntityGraph(attributePaths = {"role", "rank"})
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY u.id ASC")
    Page<User> searchByUsernameOrEmail(@Param("keyword") String keyword, Pageable pageable);
    
    @EntityGraph(attributePaths = {"role", "rank"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithRole(@Param("id") Integer id);
    
    @EntityGraph(attributePaths = {"role", "rank"})
    @Override
    Optional<User> findById(Integer id);
    
    @EntityGraph(attributePaths = {"role", "rank"})
    Optional<User> findByUsername(String username);
    
    @EntityGraph(attributePaths = {"role", "rank"})
    Optional<User> findByEmail(String email);
    
    @EntityGraph(attributePaths = {"role", "rank"})
    @Query("SELECT u FROM User u ORDER BY u.id ASC")
    @Override
    Page<User> findAll(Pageable pageable);
    
    // Lấy tất cả users không load role và rank (chỉ thông tin cơ bản)
    @Query("SELECT u FROM User u ORDER BY u.id ASC")
    Page<User> findAllBasic(Pageable pageable);
}
