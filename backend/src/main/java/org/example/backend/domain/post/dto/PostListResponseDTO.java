package org.example.backend.domain.post.dto;

import java.util.List;

public record PostListResponseDTO(
    List<PostReadResponseDto> posts,
    int totalCount
) {}