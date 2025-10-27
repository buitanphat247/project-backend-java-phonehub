package com.example.phonehub.utils;

public class SlugUtils {
    
    /**
     * Chuyển đổi text sang slug
     * Ví dụ: "BÙI TẤN PHÁT" -> "bui-tan-phat"
     * @param text Text cần chuyển đổi
     * @return Slug đã chuyển đổi
     */
    public static String generateSlug(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // Loại bỏ khoảng trắng đầu và cuối
        String slug = text.trim();
        
        // Chuyển thành chữ thường
        slug = slug.toLowerCase();
        
        // Chuyển đổi các ký tự đặc biệt tiếng Việt
        slug = slug
                .replace("á", "a").replace("à", "a").replace("ả", "a").replace("ã", "a").replace("ạ", "a")
                .replace("ă", "a").replace("ắ", "a").replace("ằ", "a").replace("ẳ", "a").replace("ẵ", "a").replace("ặ", "a")
                .replace("â", "a").replace("ấ", "a").replace("ầ", "a").replace("ẩ", "a").replace("ẫ", "a").replace("ậ", "a")
                .replace("đ", "d")
                .replace("é", "e").replace("è", "e").replace("ẻ", "e").replace("ẽ", "e").replace("ẹ", "e")
                .replace("ê", "e").replace("ế", "e").replace("ề", "e").replace("ể", "e").replace("ễ", "e").replace("ệ", "e")
                .replace("í", "i").replace("ì", "i").replace("ỉ", "i").replace("ĩ", "i").replace("ị", "i")
                .replace("ó", "o").replace("ò", "o").replace("ỏ", "o").replace("õ", "o").replace("ọ", "o")
                .replace("ô", "o").replace("ố", "o").replace("ồ", "o").replace("ổ", "o").replace("ỗ", "o").replace("ộ", "o")
                .replace("ơ", "o").replace("ớ", "o").replace("ờ", "o").replace("ở", "o").replace("ỡ", "o").replace("ợ", "o")
                .replace("ú", "u").replace("ù", "u").replace("ủ", "u").replace("ũ", "u").replace("ụ", "u")
                .replace("ư", "u").replace("ứ", "u").replace("ừ", "u").replace("ử", "u").replace("ữ", "u").replace("ự", "u")
                .replace("ý", "y").replace("ỳ", "y").replace("ỷ", "y").replace("ỹ", "y").replace("ỵ", "y");
        
        // Loại bỏ tất cả các ký tự không phải chữ cái, số, dấu gạch ngang
        slug = slug.replaceAll("[^a-z0-9\\-]", " ");
        
        // Loại bỏ khoảng trắng thừa
        slug = slug.trim().replaceAll("\\s+", "-");
        
        // Loại bỏ nhiều dấu gạch ngang liên tiếp
        slug = slug.replaceAll("-+", "-");
        
        // Loại bỏ dấu gạch ngang ở đầu và cuối
        slug = slug.replaceAll("^-|-$", "");
        
        return slug;
    }
}
