package com.example.part35teammonew.domain.userActivity.maper;

import com.example.part35teammonew.domain.userActivity.Dto.UserActivityDto;
import com.example.part35teammonew.domain.userActivity.entity.UserActivity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserActivityMapper {

  @Mapping(source = "recentcomments", target = "comments")
  @Mapping(source = "likeComment", target = "commentLikes")
  @Mapping(source = "nickName", target = "nickname")
  UserActivityDto toDto(UserActivity userActivity);

}
