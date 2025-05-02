package com.example.part35teammonew.domain.interestUserList.mapper;

import com.example.part35teammonew.domain.interestUserList.Dto.InterestUserListDto;
import com.example.part35teammonew.domain.interestUserList.entity.InterestUserList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface InterestUserListMapper {

  @Mapping(source = "interest", target = "interest")
  @Mapping(source = "subscribedUser", target = "subscribedUser")
  InterestUserListDto toDto(InterestUserList interestUserList);

}
