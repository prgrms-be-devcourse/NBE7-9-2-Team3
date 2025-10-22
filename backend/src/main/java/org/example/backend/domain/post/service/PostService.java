package org.example.backend.domain.post.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.follow.service.FollowService;
import org.example.backend.domain.like.service.LikeService;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.post.dto.PostListResponse;
import org.example.backend.domain.post.dto.PostModifyRequestDto;
import org.example.backend.domain.post.dto.PostReadResponseDto;
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
    private final FollowService followService;
    private final LikeService likeService;

    public Optional<Post> findById(Long id) {

        return postRepository.findById(id);

    }

    public void delete(Long id, Member member) {

        Post post = postRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. id=" + id));

        if (!post.getAuthor().getMemberId().equals(member.getMemberId())) {
            throw new SecurityException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

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

    public void modify(Long id, PostModifyRequestDto reqBody, Member member) {

        Post post = postRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. id=" + id));

        // 작성자 검증
        if (!post.getAuthor().getMemberId().equals(member.getMemberId())) {
            throw new SecurityException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

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

    public PostListResponse getPosts(BoardType boardType, String filterType, Member member,
        String keyword, String category, Pageable pageable) {

        Page<Post> postPage;

        if (filterType.equals("following")) {

            List<Long> followeeIds = followService.findFolloweeIdsByFollower(
                member);

            postPage = postRepository.searchByBoardTypeAndDisplayingAndAuthorIdInAndKeywordAndCategory(
                boardType, Post.Displaying.PUBLIC, followeeIds, keyword, category, pageable);

        } else {

            if ((keyword == null || keyword.isBlank()) && (category == null || category.equals(
                "all"))) {

                postPage = postRepository.findByBoardTypeAndDisplaying(boardType,
                    Post.Displaying.PUBLIC, pageable);

            } else {

                postPage = postRepository.searchByBoardTypeAndDisplayingAndKeywordAndCategory(
                    boardType, Post.Displaying.PUBLIC, keyword, category, pageable

                );

            }
        }

        List<PostReadResponseDto> postDtos = postPage.getContent().stream()
            .map(post -> {

                boolean liked = likeService.existsByMemberAndPost(member, post);
                boolean following = followService.existsByFollowerAndFollowee(
                    member,                     // 로그인 사용자
                    post.getAuthor()           // 게시글 작성자
                );
                boolean isMine = post.getAuthor().getMemberId()
                    .equals(member.getMemberId());

                return new PostReadResponseDto(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getAuthor().getNickname(),
                    post.getCreateDate(),
                    post.getImages().stream().map(PostImage::getImageUrl).toList(),
                    post.getLikeCount(),
                    liked,
                    following,
                    post.getAuthor().getMemberId(),
                    post.getCategory(),
                    isMine
                );
            })
            .toList();

        int totalCount = (int) postPage.getTotalElements();
        return new PostListResponse(postDtos, totalCount);

    }

    public PostReadResponseDto getPostById(Long id, Member member) {

        Post post = postRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. id=" + id));

        boolean liked = likeService.existsByMemberAndPost(member, post);
        boolean following = followService.existsByFollowerAndFollowee(
            member,                     // 로그인 사용자
            post.getAuthor()          // 게시글 작성자
        );
        boolean isMine = post.getAuthor().getMemberId()
            .equals(member.getMemberId());

        return new PostReadResponseDto(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getAuthor().getNickname(),
            post.getCreateDate(),
            post.getImages().stream()
                .map(PostImage::getImageUrl)
                .toList(),
            post.getLikeCount(),
            liked,
            following,
            post.getAuthor().getMemberId(),
            post.getCategory(),
            isMine
        );
    }
}