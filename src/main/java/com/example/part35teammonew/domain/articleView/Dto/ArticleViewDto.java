package com.example.part35teammonew.domain.articleView.Dto;


import java.util.Set;
import java.util.UUID;

public record ArticleViewDto(
    UUID articleId, Set<UUID> readUserIds
) {

}
