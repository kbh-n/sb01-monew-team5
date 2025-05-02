package com.example.part35teammonew.domain.userActivity.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoDto {

  private String nickName;

  public UserInfoDto(String nickName){
    this.nickName=nickName;
  }
}
