package com.example.part35teammonew.domain.articleView.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ArticleView")
@Getter
//
@ToString
//
public class ArticleView {

  @Id
  private ObjectId id;
  @Indexed
  private final UUID articleId;
  private Set<UUID> readUserIds;


  @Builder
  private ArticleView(UUID articleId) {
    this.articleId = articleId;
    this.readUserIds = new HashSet<>();
  }

  public static ArticleView setUpNewArticleView(UUID articleId) {
    return ArticleView.builder()
        .articleId(articleId)
        .build();
  }

  public boolean addNewReader(UUID readerId) {
    if(readUserIds.contains(readerId)){
      return false;
    }
    readUserIds.add(readerId);
    return true;
  }

}
