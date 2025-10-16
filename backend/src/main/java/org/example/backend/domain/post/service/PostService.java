package org.example.backend.domain.post.service;

import static org.example.backend.domain.post.entity.Post.Displaying.PUBLIC;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.post.dto.PostModifyRequestDto;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.entity.Post.Displaying;
import org.example.backend.domain.post.repository.PostRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    public List<Post> findByBoardTypeAndDisplaying(BoardType boardType, Displaying displaying) {
        return postRepository.findByBoardTypeAndDisplaying(boardType,PUBLIC);
    }

    public Post write(PostWriteRequestDto reqBody, Member member) {

        Post post = new Post(reqBody, member);
        return postRepository.save(post);

    }

    public void modify(Post post, PostModifyRequestDto reqBody) {

        post.updateTitle(reqBody.title());
        post.updateContent(reqBody.content());
        post.updateImages(reqBody.images());
    }

    public List<Post> findByBoardType(BoardType boardType) {
        return postRepository.findByBoardType(boardType);
    }
}