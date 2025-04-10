// VideoRepository.java
package com.misuzu.repository;

import com.misuzu.entity.Video;
import com.misuzu.entity.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Integer> {
    // 添加自定义查询方法
    List<Video> findByUserId(Integer userId);
    List<Video> findByStatusOrderByCreatedAtDesc(VideoStatus status);
}