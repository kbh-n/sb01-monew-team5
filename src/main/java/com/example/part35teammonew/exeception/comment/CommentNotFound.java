package com.example.part35teammonew.exeception.comment;

public class CommentNotFound extends RuntimeException {
  public CommentNotFound(String message) { //댓글을 찾을 수 없을 때
    super(message);
  }
}