package com.misuzu.service.impl;

import com.misuzu.dto.VideoUploadDTO;
import com.misuzu.entity.Video;
import com.misuzu.entity.User;
import com.misuzu.entity.VideoStatus;
import com.misuzu.repository.UserRepository;
import com.misuzu.repository.VideoRepository;
import com.misuzu.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    @Value("${file.storage.location}")
    private String storagePath;

    @Value("${file.thumbnail.location}")
    private String thumbnailPath;

    @Override
    @Transactional
    public Video processVideoUpload(VideoUploadDTO dto) {
        try {
            // 1. 保存视频文件
            Path videoFilePath = saveFile(dto.getFile(), storagePath);

            // 2. 生成缩略图
            String thumbnailUrl = generateThumbnail(videoFilePath);

            // 3. 获取用户信息
//            User user = userRepository.findById(dto.getUserId())
//                    .orElseThrow(() -> new VideoProcessingException("用户不存在"));

            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 4. 构建并保存视频记录
            return videoRepository.save(Video.builder()
                    .user(user)
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .fileUrl(videoFilePath.toString())
                    .thumbnailUrl(thumbnailUrl)
                    .duration(extractDuration(videoFilePath))
                    .fileSize((int) dto.getFile().getSize())
                    .videoType(dto.getFile().getContentType())
                    .status(VideoStatus.PENDING)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build());

        } catch (IOException e) {
            log.error("视频处理失败", e);
           throw new RuntimeException("视频上传失败: " + e.getMessage());
        }
    }

    private Path saveFile(MultipartFile file, String directory) throws IOException {
        Path dirPath = Paths.get(directory);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = dirPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filePath;
    }

    private String generateThumbnail(Path videoPath) throws IOException {
        // 实现缩略图生成逻辑（需集成FFmpeg）
        // 返回缩略图路径
        return "path/to/thumbnail.jpg";
    }

    private int extractDuration(Path videoPath) {
        // 实现视频时长提取逻辑
        return 120; // 示例值
    }
}