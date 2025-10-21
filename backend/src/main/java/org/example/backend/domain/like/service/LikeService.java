package org.example.backend.domain.like.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.like.dto.PostLikeResponseDto;
import org.example.backend.domain.like.entity.Like;
import org.example.backend.domain.like.repository.LikeRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.repository.PostRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public Map<String, Object> toggleLike(Long postId, Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Optional<Like> existingLike = likeRepository.findByMemberAndPost(member, post);

        boolean liked;
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            post.decreaseLikeCount();
            liked = false;
        } else {
            likeRepository.save(new Like(member, post));
            post.increaseLikeCount();
            liked = true;
        }

        postRepository.save(post); // 변경된 likeCount 반영

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", post.getLikeCount());
        return result;
    }



    public List<PostLikeResponseDto> getLikedPosts(Long memberId) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return likeRepository.findAllByMember(member).stream()
            .map(like -> {
                Post post = postRepository.findById(like.getPost().getId())
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
                return new PostLikeResponseDto(post.getId(), post.getTitle());
            })
            .toList();
    }
}
