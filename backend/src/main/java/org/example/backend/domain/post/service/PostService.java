package org.example.backend.domain.post.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.post.dto.PostModifyRequestDto;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.entity.PostImage;
import org.example.backend.domain.post.repository.PostRepository;
import org.example.backend.global.image.ImageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ImageService imageService;

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public void delete(Post post) {

        if (!post.getImages().isEmpty()) {
            List<String> oldImageUrls = post.getImages().stream()
                .map(PostImage::getImageUrl)
                .toList();
            imageService.deleteFiles(oldImageUrls);
            post.deleteImageUrls(); // 이미지 리스트 초기화
        }

        postRepository.delete(post);
    }

    public void write(PostWriteRequestDto reqBody, Member member) {
        Post post = new Post(reqBody, member);

        if (Post.BoardType.valueOf(reqBody.boardType()) == Post.BoardType.SHOWOFF
            && (reqBody.imageUrls() == null || reqBody.imageUrls().isEmpty())) {
            throw new IllegalArgumentException("자랑 게시판 게시글은 최소 1개의 이미지가 필요합니다.");
        }

        if (reqBody.imageUrls() != null && !reqBody.imageUrls().isEmpty()) {
            reqBody.imageUrls().forEach(url ->
                post.addImage(new PostImage(url, post)));
        }

        postRepository.save(post);
    }

    public void modify(Post post, PostModifyRequestDto reqBody) {

        post.updateTitle(reqBody.title());
        post.updateContent(reqBody.content());

        // 새 이미지 URL이 있고, 기존과 다를 때만 교체
        if (reqBody.imageUrls() != null) {
            List<String> oldImageUrls = post.getImages().stream()
                .map(PostImage::getImageUrl)
                .toList();

            List<String> newImageUrls = reqBody.imageUrls();

            // 삭제할 이미지 : 기존에는 있었는데 새 목록에는 없는 것
            List<String> toDelete = oldImageUrls.stream()
                .filter(url -> !newImageUrls.contains(url))
                .toList();

            // S3에서 삭제
            if (!toDelete.isEmpty()) {
                imageService.deleteFiles(toDelete);
            }

            // DB 이미지 목록 갱신
            post.deleteImageUrls();
            newImageUrls.forEach(url ->
                post.addImage(new PostImage(url, post)));
        }

        postRepository.save(post);

    }

    public List<Post> findByBoardType(BoardType boardType) {
        return postRepository.findByBoardType(boardType);
    }

    public Page<Post> findByBoardTypeAndDisplayingAndAuthorIdInAndKeywordAndCategory(
        BoardType boardType,
        Post.Displaying displaying,
        List<Long> authorIds,
        String keyword,
        String category,
        Pageable pageable) {

        return postRepository.searchByBoardTypeAndDisplayingAndAuthorIdInAndKeywordAndCategory(
            boardType, displaying, authorIds, keyword, category, pageable
        );
    }

    // 전체 게시판 + keyword + category
    public Page<Post> findByBoardTypeAndDisplayingAndKeywordAndCategory(
        BoardType boardType,
        Post.Displaying displaying,
        String keyword,
        String category,
        Pageable pageable) {

        if ((keyword == null || keyword.isBlank()) && (category == null || category.equals("all"))) {
            return postRepository.findByBoardTypeAndDisplaying(boardType, displaying, pageable);
        }
        return postRepository.searchByBoardTypeAndDisplayingAndKeywordAndCategory(
            boardType, displaying, keyword, category, pageable
        );
    }
}