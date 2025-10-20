package org.example.backend.domain.post.dto;

import java.util.List;

public record PostReadResponseDto(

    String title,
    String content,
    String nickname,
    List<String> images

) {

}
