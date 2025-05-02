package com.example.part35teammonew.domain.articleView.mapper;

import com.example.part35teammonew.domain.articleView.Dto.ArticleViewDto;
import com.example.part35teammonew.domain.articleView.entity.ArticleView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleViewMapper {

  @Mapping(source = "articleId", target = "articleId")
  @Mapping(source = "readUserIds", target = "readUserIds")
  ArticleViewDto toDto(ArticleView articleView);

}
