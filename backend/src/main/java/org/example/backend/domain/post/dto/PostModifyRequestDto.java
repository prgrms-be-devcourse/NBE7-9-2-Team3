package org.example.backend.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record PostModifyRequestDto(

    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    String content,

    List<MultipartFile> images,
    List<String> existingImages
) {

}
