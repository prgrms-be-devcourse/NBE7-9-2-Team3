package org.example.backend.domain.post.dto;

import java.util.List;

public record PostListResponse(
    List<PostReadResponseDto> posts,
    int totalCount
) {}