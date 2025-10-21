package org.example.backend.domain.post.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.postcomment.entity.PostComment;
import org.example.backend.global.jpa.entity.BaseEntity;

@NoArgsConstructor
@Getter
@Entity
public class Post extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType boardType;

    public enum BoardType {
        SHOWOFF, QUESTION
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Displaying displaying;

    public enum Displaying {
        PUBLIC, PRIVATE
    }

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    private int likeCount = 0;

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }


    public Post(PostWriteRequestDto reqBody, Member member) {
        this.title = reqBody.title();
        this.content = reqBody.content();
        this.author = member;
        this.boardType = BoardType.valueOf(reqBody.boardType());
        this.displaying = Post.Displaying.PUBLIC;
        this.images = new ArrayList<>();
    }

    public void addImage(PostImage image) {
        images.add(image);
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void deleteImageUrls() {
        this.images.clear();

    }
}