package org.example.backend.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record PostWriteRequestDto(

    @NotBlank(message = "제목은 필수입니다.")
    String title,
    @NotBlank(message = "내용은 필수입니다.")
    String content,

    @NotBlank(message = "게시판 타입은 필수입니다.")
    String boardType,

    List<MultipartFile> images,

    String category

) {

}
