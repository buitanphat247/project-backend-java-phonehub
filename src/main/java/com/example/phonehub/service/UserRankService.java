package com.example.phonehub.service;

import com.example.phonehub.dto.CreateUserRankRequest;
import com.example.phonehub.dto.UpdateUserRankRequest;
import com.example.phonehub.dto.UserRankDto;
import com.example.phonehub.entity.UserRank;
import com.example.phonehub.repository.UserRankRepository;
import com.example.phonehub.utils.UserRankUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserRankService {
    
    @Autowired
    private UserRankRepository rankRepository;
    
    // Lấy tất cả ranks
    public List<UserRankDto> getAllRanks() {
        return UserRankUtils.toDtoList(rankRepository.findAllOrderByMinPointsAsc());
    }
    
    // Lấy rank theo ID
    public Optional<UserRankDto> getById(Integer id) {
        return rankRepository.findById(id).map(UserRankUtils::toDto);
    }
    
    // Lấy rank theo tên
    public Optional<UserRankDto> getByName(String name) {
        return rankRepository.findByName(name).map(UserRankUtils::toDto);
    }
    
    // Tìm rank phù hợp dựa trên điểm số
    public Optional<UserRankDto> getRankByPoints(Integer points) {
        return rankRepository.findRankByPoints(points != null ? points : 0)
                .map(UserRankUtils::toDto);
    }
    
    // Lấy UserRank entity (dùng cho internal)
    public Optional<UserRank> getRankEntityByPoints(Integer points) {
        return rankRepository.findRankByPoints(points != null ? points : 0);
    }
    
    // Tạo rank mới
    public UserRankDto create(CreateUserRankRequest req) {
        // Kiểm tra tên rank đã tồn tại chưa
        if (rankRepository.existsByName(req.getName())) {
            UserRank existingRank = rankRepository.findByName(req.getName()).orElse(null);
            String errorMsg = String.format(
                "Tên rank '%s' đã tồn tại. Rank hiện tại: %s (điểm: %d - %d)",
                req.getName(),
                existingRank != null ? existingRank.getName() : req.getName(),
                existingRank != null ? existingRank.getMinPoints() : 0,
                existingRank != null ? existingRank.getMaxPoints() : 0
            );
            throw new RuntimeException(errorMsg);
        }
        
        // Kiểm tra minPoints <= maxPoints
        if (req.getMinPoints() > req.getMaxPoints()) {
            throw new RuntimeException(
                String.format("Min points (%d) không thể lớn hơn max points (%d)", 
                    req.getMinPoints(), req.getMaxPoints())
            );
        }
        
        // Kiểm tra overlap với các rank khác
        List<UserRank> existingRanks = rankRepository.findAll();
        for (UserRank existing : existingRanks) {
            // Kiểm tra xem có khoảng điểm bị overlap không
            boolean hasOverlap = !(req.getMaxPoints() < existing.getMinPoints() || 
                                  req.getMinPoints() > existing.getMaxPoints());
            if (hasOverlap) {
                String errorMsg = String.format(
                    "Khoảng điểm số bị trùng lặp với rank '%s' hiện có. " +
                    "Rank mới: %d - %d điểm | Rank hiện có: %d - %d điểm",
                    existing.getName(),
                    req.getMinPoints(), req.getMaxPoints(),
                    existing.getMinPoints(), existing.getMaxPoints()
                );
                throw new RuntimeException(errorMsg);
            }
        }
        
        // Tạo rank mới
        UserRank rank = new UserRank();
        rank.setName(req.getName());
        rank.setMinPoints(req.getMinPoints());
        rank.setMaxPoints(req.getMaxPoints());
        rank.setDiscount(req.getDiscount() != null ? req.getDiscount() : BigDecimal.ZERO);
        
        return UserRankUtils.toDto(rankRepository.save(rank));
    }
    
    // Cập nhật rank
    public UserRankDto update(Integer id, UpdateUserRankRequest req) {
        UserRank rank = rankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rank not found"));
        
        // Cập nhật name (nếu có)
        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            // Kiểm tra tên rank mới có trùng với rank khác không (nếu đổi tên)
            if (!rank.getName().equals(req.getName()) && rankRepository.existsByName(req.getName())) {
                UserRank existingRank = rankRepository.findByName(req.getName()).orElse(null);
                String errorMsg = String.format(
                    "Tên rank '%s' đã tồn tại. Rank hiện tại: %s (điểm: %d - %d)",
                    req.getName(),
                    existingRank != null ? existingRank.getName() : req.getName(),
                    existingRank != null ? existingRank.getMinPoints() : 0,
                    existingRank != null ? existingRank.getMaxPoints() : 0
                );
                throw new RuntimeException(errorMsg);
            }
            rank.setName(req.getName());
        }
        
        // Cập nhật minPoints và maxPoints (nếu có)
        Integer newMinPoints = req.getMinPoints() != null ? req.getMinPoints() : rank.getMinPoints();
        Integer newMaxPoints = req.getMaxPoints() != null ? req.getMaxPoints() : rank.getMaxPoints();
        
        // Kiểm tra minPoints <= maxPoints
        if (newMinPoints > newMaxPoints) {
            throw new RuntimeException("Min points (" + newMinPoints + ") cannot be greater than max points (" + newMaxPoints + ")");
        }
        
        // Kiểm tra overlap với các rank khác (trừ rank hiện tại) - chỉ khi có thay đổi min/max
        if (req.getMinPoints() != null || req.getMaxPoints() != null) {
            List<UserRank> existingRanks = rankRepository.findAll();
            for (UserRank existing : existingRanks) {
                if (!existing.getId().equals(id)) {
                    boolean hasOverlap = !(newMaxPoints < existing.getMinPoints() || 
                                          newMinPoints > existing.getMaxPoints());
                    if (hasOverlap) {
                        String errorMsg = String.format(
                            "Khoảng điểm số bị trùng lặp với rank '%s' hiện có. " +
                            "Rank mới: %d - %d điểm | Rank hiện có: %d - %d điểm",
                            existing.getName(),
                            newMinPoints, newMaxPoints,
                            existing.getMinPoints(), existing.getMaxPoints()
                        );
                        throw new RuntimeException(errorMsg);
                    }
                }
            }
            rank.setMinPoints(newMinPoints);
            rank.setMaxPoints(newMaxPoints);
        }
        
        // Cập nhật discount (nếu có)
        if (req.getDiscount() != null) {
            rank.setDiscount(req.getDiscount());
        }
        
        return UserRankUtils.toDto(rankRepository.save(rank));
    }
    
    // Xóa rank
    public void delete(Integer id) {
        if (!rankRepository.existsById(id)) {
            throw new RuntimeException("Rank not found");
        }
        
        // TODO: Kiểm tra xem có user nào đang sử dụng rank này không
        // Nếu có, không cho xóa hoặc set rank của họ về null
        
        rankRepository.deleteById(id);
    }
}

