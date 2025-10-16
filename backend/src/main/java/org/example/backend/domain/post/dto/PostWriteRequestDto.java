package org.example.backend.domain.post.dto;

import java.util.List;
import org.example.backend.domain.post.entity.PostImage;

public record PostWriteRequestDto(

    String title,
    String content,
    String BoardType,
    List<PostImage> images

) {

}
