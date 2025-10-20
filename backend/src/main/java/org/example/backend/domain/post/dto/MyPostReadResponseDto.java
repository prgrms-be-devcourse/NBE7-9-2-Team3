package org.example.backend.domain.post.dto;

import org.example.backend.domain.post.entity.Post.Displaying;

public record MyPostReadResponseDto(
    String title,
    Displaying displaying
) {

}
