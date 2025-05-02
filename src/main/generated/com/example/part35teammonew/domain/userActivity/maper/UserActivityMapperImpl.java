package com.example.part35teammonew.domain.userActivity.maper;

import com.example.part35teammonew.domain.userActivity.Dto.ArticleInfoView;
import com.example.part35teammonew.domain.userActivity.Dto.InterestView;
import com.example.part35teammonew.domain.userActivity.Dto.LikeCommentView;
import com.example.part35teammonew.domain.userActivity.Dto.RecentCommentView;
import com.example.part35teammonew.domain.userActivity.Dto.UserActivityDto;
import com.example.part35teammonew.domain.userActivity.entity.UserActivity;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-29T13:30:24+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class UserActivityMapperImpl implements UserActivityMapper {

    @Override
    public UserActivityDto toDto(UserActivity userActivity) {
        if ( userActivity == null ) {
            return null;
        }

        UserActivityDto userActivityDto = new UserActivityDto();

        LinkedList<RecentCommentView> linkedList = userActivity.getRecentcomments();
        if ( linkedList != null ) {
            userActivityDto.setComments( new LinkedList<RecentCommentView>( linkedList ) );
        }
        LinkedList<LikeCommentView> linkedList1 = userActivity.getLikeComment();
        if ( linkedList1 != null ) {
            userActivityDto.setCommentLikes( new LinkedList<LikeCommentView>( linkedList1 ) );
        }
        userActivityDto.setNickname( userActivity.getNickName() );
        userActivityDto.setUserId( userActivity.getUserId() );
        userActivityDto.setEmail( userActivity.getEmail() );
        userActivityDto.setCreatedAt( userActivity.getCreatedAt() );
        Set<InterestView> set = userActivity.getSubscriptions();
        if ( set != null ) {
            userActivityDto.setSubscriptions( new LinkedHashSet<InterestView>( set ) );
        }
        LinkedList<ArticleInfoView> linkedList2 = userActivity.getArticleViews();
        if ( linkedList2 != null ) {
            userActivityDto.setArticleViews( new LinkedList<ArticleInfoView>( linkedList2 ) );
        }

        return userActivityDto;
    }
}
