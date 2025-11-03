package com.example.phonehub.repository;

import com.example.phonehub.entity.UserRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRankRepository extends JpaRepository<UserRank, Integer> {
    
    Optional<UserRank> findByName(String name);
    
    boolean existsByName(String name);
    
    // Tìm rank phù hợp dựa trên điểm số (tìm rank có điểm số nằm trong khoảng min-max)
    @Query("SELECT ur FROM UserRank ur WHERE :points BETWEEN ur.minPoints AND ur.maxPoints ORDER BY ur.minPoints DESC")
    Optional<UserRank> findRankByPoints(@Param("points") Integer points);
    
    // Lấy tất cả ranks sắp xếp theo minPoints (từ thấp đến cao)
    @Query("SELECT ur FROM UserRank ur ORDER BY ur.minPoints ASC")
    List<UserRank> findAllOrderByMinPointsAsc();
    
    // Lấy tất cả ranks sắp xếp theo minPoints (từ cao đến thấp)
    @Query("SELECT ur FROM UserRank ur ORDER BY ur.minPoints DESC")
    List<UserRank> findAllOrderByMinPointsDesc();
}

