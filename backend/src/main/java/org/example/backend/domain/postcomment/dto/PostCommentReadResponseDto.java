package org.example.backend.domain.postcomment.dto;

public record PostCommentReadResponseDto(
    String content,
    String nickname
) {}
