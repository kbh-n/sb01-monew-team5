package com.example.part35teammonew.domain.articleView.service;

import static com.example.part35teammonew.domain.articleView.entity.ArticleView.setUpNewArticleView;

import com.example.part35teammonew.domain.article.service.ArticleService;
import com.example.part35teammonew.domain.articleView.Dto.ArticleViewDto;
import com.example.part35teammonew.domain.articleView.entity.ArticleView;
import com.example.part35teammonew.domain.articleView.mapper.ArticleViewMapper;
import com.example.part35teammonew.domain.articleView.repository.ArticleViewRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleViewServiceImp implements ArticleViewServiceInterface {

  private final ArticleViewRepository articleViewRepository;
  private final ArticleViewMapper articleViewMapper;

  public ArticleViewServiceImp(
      @Autowired ArticleViewRepository articleViewRepository,
      @Autowired ArticleViewMapper articleViewMapper) {

    this.articleViewRepository = articleViewRepository;
    this.articleViewMapper = articleViewMapper;
  }

  @Override
  @Transactional  //기사가 만들어 질떄 호출
  public ArticleViewDto createArticleView(UUID articleId) {
    ArticleView articleView = setUpNewArticleView(articleId);
    ArticleView saved = articleViewRepository.save(articleView);
    return articleViewMapper.toDto(saved);
  }

  @Override
  @Transactional //유저가 기사 읽을떄 호출
  public boolean addReadUser(UUID articleId, UUID userId) {
    return articleViewRepository.findByArticleId(articleId)
        .map(articleView -> {
          boolean result=articleView.addNewReader(userId);
          articleViewRepository.save(articleView);
          return result;
        })
        .orElse(false);
  }


}
