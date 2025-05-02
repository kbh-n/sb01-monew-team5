package com.example.part35teammonew.exeception.comment;

public class CommentUpdateUnauthorized extends RuntimeException {
  public CommentUpdateUnauthorized(String message) { //댓글 수정 권한이 없을 때 (댓글 수정자 != 요청자)
    super(message);
  }
}