package org.example.backend.domain.post.dto;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record PostWriteRequestDto(

    String title,
    String content,
    String boardType,
    List<MultipartFile> images,
    String category

) {

}
