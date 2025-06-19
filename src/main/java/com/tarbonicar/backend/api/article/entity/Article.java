package com.tarbonicar.backend.api.article.entity;

import com.tarbonicar.backend.api.category.entity.CarAge;
import com.tarbonicar.backend.api.member.entity.Member;
import com.tarbonicar.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Table(name = "article")
@NoArgsConstructor
@AllArgsConstructor
public class Article extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private long viewCount;
    private long likeCount;
    private boolean modify;

    @Enumerated(EnumType.STRING)
    private ArticleType articleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_age_id", nullable = false)
    private CarAge carAge;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ArticleLike> articleLikes = new ArrayList<>();

    public void modify(String title, String content, ArticleType articleType, CarAge carAge) {
        this.title = title;
        this.content = content;
        this.articleType = articleType;
        this.carAge = carAge;
        this.modify = true;
    }

}
