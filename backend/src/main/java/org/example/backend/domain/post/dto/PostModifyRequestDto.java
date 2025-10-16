package org.example.backend.domain.post.dto;

import java.util.List;
import org.example.backend.domain.post.entity.PostImage;

public record PostModifyRequestDto(
    String title,
    String content,
    List<PostImage> images
) {

}
