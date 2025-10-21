package org.example.backend.domain.postcomment.dto;

public record MyPostCommentReadResponseDto(
    Long id,
    String postTitle,
    String content
) {}
