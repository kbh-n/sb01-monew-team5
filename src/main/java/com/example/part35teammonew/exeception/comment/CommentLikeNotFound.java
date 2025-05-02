package com.example.part35teammonew.exeception.comment;

public class CommentLikeNotFound extends RuntimeException {
  public CommentLikeNotFound(String message) { //좋아요을 찾을 수 없을때 (존재하지 않는 좋아요에 대한 작업)
    super(message);
  }
}