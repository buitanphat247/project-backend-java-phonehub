package com.example.phonehub.service.helper;

import com.example.phonehub.dto.UserDto;
import com.example.phonehub.entity.User;
import com.example.phonehub.repository.UserRepository;
import com.example.phonehub.service.UserRankService;
import com.example.phonehub.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 🛠️ Helper class chứa các utility methods cho User
 * 
 * Tách logic helper ra khỏi UserService để code gọn gàng và dễ bảo trì hơn
 */
@Component
public class UserHelper {

    @Autowired
    private UserRankService rankService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 🔄 Tự động cập nhật rank của user dựa trên điểm số
     * 
     * Logic:
     * - Lấy điểm số của user (mặc định 0 nếu null)
     * - Tìm rank phù hợp với điểm số đó
     * - Set rank cho user
     * 
     * @param user User cần cập nhật rank
     */
    public void updateUserRank(User user) {
        if (user == null) return;
        
        Integer points = user.getPoints() != null ? user.getPoints() : 0;
        rankService.getRankEntityByPoints(points).ifPresent(user::setRank);
    }
    
    /**
     * ✅ Đảm bảo user có rank (nếu null thì tự động set dựa trên points)
     * 
     * Logic:
     * - Nếu user chưa có rank hoặc rank bị null
     * - Tự động cập nhật rank dựa trên điểm số
     * - Nếu user đã có ID (đã persist), lưu lại vào DB
     * 
     * @param user User cần đảm bảo có rank
     */
    public void ensureUserHasRank(User user) {
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
    
    /**
     * 🔄 Convert User to DTO và đảm bảo có rank (full object - cho chi tiết)
     * 
     * Logic:
     * 1. Đảm bảo user có rank trước khi convert
     * 2. Convert User → UserDto với full role và rank objects
     * 
     * @param user User entity cần convert
     * @return UserDto với full role và rank objects
     */
    public UserDto toDtoWithRank(User user) {
        if (user == null) return null;
        
        // Đảm bảo user có rank trước khi convert
        ensureUserHasRank(user);
        
        // Dùng toDtoFull để trả về full role và rank objects
        return UserUtils.toDtoFull(user);
    }
}

