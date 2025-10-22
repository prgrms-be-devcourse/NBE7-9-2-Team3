package org.example.backend.domain.post.dto;

import java.util.List;

public record PostWriteRequestDto(

    String title,
    String content,
    String boardType,
    List<String> imageUrls,
    String category

) {

}
