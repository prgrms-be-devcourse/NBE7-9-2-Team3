package org.example.backend.domain.post.dto;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record PostModifyRequestDto(
    String title,
    String content,
    List<MultipartFile> images,
    List<String> existingImages
) {

}
