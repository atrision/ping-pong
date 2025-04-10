package com.misuzu.service;

import com.misuzu.dto.VideoUploadDTO;
import com.misuzu.entity.Video;
import org.springframework.stereotype.Service;


public interface VideoService {
    Video processVideoUpload(VideoUploadDTO videoDTO);
}
