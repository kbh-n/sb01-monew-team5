package com.example.part35teammonew.exeception.comment;

public class CommentLikeConflict extends RuntimeException {
  public CommentLikeConflict(String message) { //좋아요 충돌 관련 (이미 좋아요누른 상태에서 다시 좋아요 요청)
    super(message);
  }
}