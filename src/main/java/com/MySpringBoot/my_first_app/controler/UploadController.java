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

    // 允许上传的图片格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );
    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Value("${file.upload.path:/var/lib/uploads}")
    private String uploadPath;

    @PostMapping("/api/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        // 检查文件是否为空
        if (file.isEmpty()) {
            response.put("code", 400);
            response.put("message", "请选择要上传的文件");
            return response;
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            response.put("code", 400);
            response.put("message", "文件大小不能超过 5MB");
            return response;
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            response.put("code", 400);
            response.put("message", "不支持的文件格式");
            return response;
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            response.put("code", 400);
            response.put("message", "仅支持 JPG/PNG/GIF/WebP 格式的图片");
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

            String imageUrl = "/img/" + newFileName;
            response.put("code", 200);
            response.put("message", "上传成功");
            response.put("data", imageUrl);

        } catch (IOException e) {
            response.put("code", 500);
            response.put("message", "上传失败：" + e.getMessage());
        }

        return response;
    }
}

