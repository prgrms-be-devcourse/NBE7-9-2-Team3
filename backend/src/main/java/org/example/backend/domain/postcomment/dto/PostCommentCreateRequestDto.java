package org.example.backend.domain.postcomment.dto;

public record PostCommentCreateRequestDto(
    String content,
    Long postId
) {}
