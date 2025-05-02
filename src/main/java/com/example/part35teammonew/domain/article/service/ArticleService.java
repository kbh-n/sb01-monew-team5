package com.example.part35teammonew.domain.article.service;

import com.example.part35teammonew.domain.article.dto.ArticleBaseDto;
import com.example.part35teammonew.domain.article.dto.ArticleCursorRequest;
import com.example.part35teammonew.domain.article.dto.ArticleSourceAndDateAndInterestsRequest;
import com.example.part35teammonew.domain.article.dto.ArticlesResponse;
import com.example.part35teammonew.domain.article.dto.findByCursorPagingResponse;
import java.util.List;
import java.util.UUID;

public interface ArticleService {
  UUID save(ArticleBaseDto dto);
  ArticleBaseDto findById(UUID id);
  List<ArticleBaseDto> findAll();
  List<ArticleBaseDto> findByIds(List<UUID> ids);
  void deletePhysical(UUID id);
  void deleteLogical(UUID id);
  List<UUID> backup(String from, String to);
  void increaseCountReadUser(UUID id);

  ArticlesResponse getPageArticle(String keyword, String interestId, String[] sourceIn, String publishDateFrom,
      String publishDateTo, String orderBy, String direction, String cursor, String after,
      int limit, String userId);
}

