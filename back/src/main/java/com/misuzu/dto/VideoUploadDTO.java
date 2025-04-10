package com.misuzu.dto;

import com.misuzu.validation.FileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class VideoUploadDTO {
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    @NotBlank(message = "视频标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "视频文件不能为空")
    @FileType(
            allowed = {"video/mp4", "video/avi", "video/quicktime"},
            message = "仅支持MP4/AVI/MOV格式"
    )
    private MultipartFile file;
}