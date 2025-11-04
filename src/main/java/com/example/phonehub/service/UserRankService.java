package com.example.phonehub.service;

import com.example.phonehub.dto.CreateUserRankRequest;
import com.example.phonehub.dto.UpdateUserRankRequest;
import com.example.phonehub.dto.UserRankDto;
import com.example.phonehub.entity.UserRank;
import com.example.phonehub.repository.UserRankRepository;
import com.example.phonehub.service.redis_cache.UserRankCacheService;
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
    
    @Autowired
    private UserRankCacheService rankCacheService;
    
    public List<UserRankDto> getAllRanks() {
        List<UserRankDto> cachedRanks = rankCacheService.getAllRanksFromCache();
        if (cachedRanks != null && !cachedRanks.isEmpty()) {
            return cachedRanks;
        }
        
        List<UserRankDto> ranks = UserRankUtils.toDtoList(rankRepository.findAllOrderByMinPointsAsc());
        rankCacheService.saveAllRanksToCache(ranks);
        return ranks;
    }
    
    public Optional<UserRankDto> getById(Integer id) {
        if (id == null) return Optional.empty();
        
        UserRankDto cachedRank = rankCacheService.getRankFromCacheById(id);
        return rankCacheService.getRankWithCacheStrategy(
            cachedRank,
            () -> rankRepository.findById(id)
        );
    }
    
    public Optional<UserRankDto> getByName(String name) {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        
        UserRankDto cachedRank = rankCacheService.getRankFromCacheByName(name);
        return rankCacheService.getRankWithCacheStrategy(
            cachedRank,
            () -> rankRepository.findByName(name)
        );
    }
    
    public Optional<UserRankDto> getRankByPoints(Integer points) {
        Integer validPoints = points != null ? points : 0;
        
        UserRankDto cachedRank = rankCacheService.getRankFromCacheByPoints(validPoints);
        if (cachedRank != null) {
            return Optional.of(cachedRank);
        }
        
        Optional<UserRank> rankFromDb = rankRepository.findRankByPoints(validPoints);
        Optional<UserRankDto> rankDto = rankFromDb.map(UserRankUtils::toDto);
        
        rankDto.ifPresent(rank -> {
            rankCacheService.saveRankToCache(rank);
            rankCacheService.saveRankByPointsToCache(validPoints, rank);
        });
        
        return rankDto;
    }
    
    public Optional<UserRank> getRankEntityByPoints(Integer points) {
        return rankRepository.findRankByPoints(points != null ? points : 0);
    }
    
    public UserRankDto create(CreateUserRankRequest req) {
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
        
        if (req.getMinPoints() > req.getMaxPoints()) {
            throw new RuntimeException(
                String.format("Min points (%d) không thể lớn hơn max points (%d)", 
                    req.getMinPoints(), req.getMaxPoints())
            );
        }
        
        List<UserRank> existingRanks = rankRepository.findAll();
        for (UserRank existing : existingRanks) {
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
        
        UserRank rank = new UserRank();
        rank.setName(req.getName());
        rank.setMinPoints(req.getMinPoints());
        rank.setMaxPoints(req.getMaxPoints());
        rank.setDiscount(req.getDiscount() != null ? req.getDiscount() : BigDecimal.ZERO);
        
        UserRankDto savedDto = UserRankUtils.toDto(rankRepository.save(rank));
        rankCacheService.saveRankToCache(savedDto);
        rankCacheService.invalidateAllRankCache();
        
        return savedDto;
    }
    
    public UserRankDto update(Integer id, UpdateUserRankRequest req) {
        UserRank rank = rankRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rank not found"));
        
        String oldName = rank.getName();
        
        if (req.getName() != null && !req.getName().trim().isEmpty()) {
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
        
        Integer newMinPoints = req.getMinPoints() != null ? req.getMinPoints() : rank.getMinPoints();
        Integer newMaxPoints = req.getMaxPoints() != null ? req.getMaxPoints() : rank.getMaxPoints();
        
        if (newMinPoints > newMaxPoints) {
            throw new RuntimeException("Min points (" + newMinPoints + ") cannot be greater than max points (" + newMaxPoints + ")");
        }
        
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
        
        if (req.getDiscount() != null) {
            rank.setDiscount(req.getDiscount());
        }
        
        UserRankDto updatedDto = UserRankUtils.toDto(rankRepository.save(rank));
        rankCacheService.removeRankFromCache(id, oldName);
        rankCacheService.saveRankToCache(updatedDto);
        rankCacheService.invalidateAllRankCache();
        
        return updatedDto;
    }
    
    public void delete(Integer id) {
        Optional<UserRank> rankOpt = rankRepository.findById(id);
        if (rankOpt.isEmpty()) {
            throw new RuntimeException("Rank not found");
        }
        
        UserRank rank = rankOpt.get();
        String rankName = rank.getName();
        
        rankRepository.deleteById(id);
        rankCacheService.removeRankFromCache(id, rankName);
    }
}

