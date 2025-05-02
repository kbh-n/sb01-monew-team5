package com.example.part35teammonew.domain.articleView.mapper;

import com.example.part35teammonew.domain.articleView.Dto.ArticleViewDto;
import com.example.part35teammonew.domain.articleView.entity.ArticleView;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-28T16:35:21+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ArticleViewMapperImpl implements ArticleViewMapper {

    @Override
    public ArticleViewDto toDto(ArticleView articleView) {
        if ( articleView == null ) {
            return null;
        }

        UUID articleId = null;
        Long count = null;
        Set<UUID> readUserIds = null;

        articleId = articleView.getArticleId();
        count = articleView.getCount();
        Set<UUID> set = articleView.getReadUserIds();
        if ( set != null ) {
            readUserIds = new LinkedHashSet<UUID>( set );
        }

        ArticleViewDto articleViewDto = new ArticleViewDto( articleId, count, readUserIds );

        return articleViewDto;
    }
}
