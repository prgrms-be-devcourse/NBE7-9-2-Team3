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
            && (reqBody.images() == null || reqBody.images().isEmpty())) {
            throw new IllegalArgumentException("자랑 게시판 게시글은 최소 1개의 이미지가 필요합니다.");
        }

        if (reqBody.images() != null && !reqBody.images().isEmpty()) {
            for (MultipartFile file : reqBody.images()) {
                // 1. S3 업로드
                String imageUrl = imageService.uploadFile(file, "post");

                // 2. PostImage 생성 후 Post에 추가
                PostImage postImage = new PostImage(imageUrl, post);
                post.addImage(postImage);
            }
        }

        postRepository.save(post);
    }

    public void modify(Post post, PostModifyRequestDto reqBody) {

        post.updateTitle(reqBody.title());
        post.updateContent(reqBody.content());

        List<String> keepImages = reqBody.existingImages();
        List<PostImage> toDelete = post.getImages().stream()
            .filter(img -> keepImages == null || !keepImages.contains(img.getImageUrl()))
            .toList();

        if (!toDelete.isEmpty()) {
            List<String> deleteUrls = toDelete.stream()
                .map(PostImage::getImageUrl)
                .toList();
            imageService.deleteFiles(deleteUrls);
            post.getImages().removeAll(toDelete);
        }

        if (reqBody.images() != null && !reqBody.images().isEmpty()) {
            for (MultipartFile file : reqBody.images()) {
                // 1. S3 업로드
                String imageUrl = imageService.uploadFile(file, "post");

                // 2. PostImage 생성 후 Post에 추가
                PostImage postImage = new PostImage(imageUrl, post);
                post.addImage(postImage);
            }
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