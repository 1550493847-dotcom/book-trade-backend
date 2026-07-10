package com.MySpringBoot.my_first_app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class UploadController {

    @Value("${file.upload.path:D:/MySpringBoot/my-first-app/src/main/resources/static/img/}")
    private String uploadPath;

    @PostMapping("/api/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 创建目录（如果不存在）
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println("创建目录: " + uploadPath + " 成功: " + created);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + extension;

            // 保存文件到磁盘
            File destFile = new File(uploadPath + newFileName);
            file.transferTo(destFile);

            System.out.println("文件保存成功: " + destFile.getAbsolutePath());

            // 返回浏览器访问的 URL（相对路径）
            String imageUrl = "/img/" + newFileName;

            response.put("code", 200);
            response.put("message", "上传成功");
            response.put("data", imageUrl);

        } catch (IOException e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("message", "上传失败：" + e.getMessage());
        }

        return response;
    }
}
