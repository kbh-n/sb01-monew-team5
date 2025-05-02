package com.example.part35teammonew.exeception.comment;

public class CommentDeleteUnauthorized extends RuntimeException {
  public CommentDeleteUnauthorized(String message) { //댓글 삭제 권한 없을때 (댓 작성자 != 삭제하려는 요청자)
    super(message);
  }
}
