package com.MySpringBoot.my_first_app.controler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class UploadController {

    // 鍏佽涓婁紶鐨勫浘鐗囨牸寮?
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );
    // 鏈€澶ф枃浠跺ぇ灏忥細5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Value("${file.upload.path:/var/lib/uploads}")
    private String uploadPath;

    @PostMapping("/api/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        // 妫€鏌ユ枃浠舵槸鍚︿负绌?
        if (file.isEmpty()) {
            response.put("code", 400);
            response.put("message", "璇烽€夋嫨瑕佷笂浼犵殑鏂囦欢");
            return response;
        }

        // 妫€鏌ユ枃浠跺ぇ灏?
        if (file.getSize() > MAX_FILE_SIZE) {
            response.put("code", 400);
            response.put("message", "鏂囦欢澶у皬涓嶈兘瓒呰繃 5MB");
            return response;
        }

        // 妫€鏌ユ枃浠舵墿灞曞悕
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            response.put("code", 400);
            response.put("message", "涓嶆敮鎸佺殑鏂囦欢鏍煎紡");
            return response;
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            response.put("code", 400);
            response.put("message", "浠呮敮鎸?JPG/PNG/GIF/WebP 鏍煎紡鐨勫浘鐗?);
            return response;
        }

        try {
            File dir = new File(uploadPath + File.separator + "img");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String newFileName = "img" + File.separator + UUID.randomUUID().toString() + extension;
            File destFile = new File(uploadPath + File.separator + newFileName);
            file.transferTo(destFile);

            String imageUrl = "/" + newFileName;
            response.put("code", 200);
            response.put("message", "涓婁紶鎴愬姛");
            response.put("data", imageUrl);

        } catch (IOException e) {
            response.put("code", 500);
            response.put("message", "涓婁紶澶辫触锛? + e.getMessage());
        }

        return response;
    }
}


