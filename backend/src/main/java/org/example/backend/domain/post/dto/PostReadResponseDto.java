package org.example.backend.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostReadResponseDto(

    Long id,
    String title,
    String content,
    String nickname,
    LocalDateTime createDate,
    List<String> images,
    int likeCount,
    boolean liked

) {

}
