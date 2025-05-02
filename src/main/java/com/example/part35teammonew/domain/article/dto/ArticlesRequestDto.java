package com.example.part35teammonew.domain.article.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticlesRequestDto {
  private String keyword;
  private String[] sourceIn;
  private String publishDateFrom;
  private String publishDateTo;
  @NotNull
  private String orderBy;
  @NotNull
  private String direction;
  private String cursor;
  @NotNull
  private int limit;

  private String interestId;
  private String monew_Request_User_ID;
}
