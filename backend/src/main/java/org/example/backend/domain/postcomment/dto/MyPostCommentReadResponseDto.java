package org.example.backend.domain.postcomment.dto;

public record MyPostCommentReadResponseDto(
    Long id,
    Long postId,
    String postTitle,
    String content
) {}
