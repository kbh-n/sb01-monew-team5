package com.example.part35teammonew.domain.interestUserList.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "interestUserList")
@Getter
public class InterestUserList {

  @Id
  private ObjectId id;
  @Indexed
  private final UUID interest;
  private Set<UUID> subscribedUser;

  @Builder
  private InterestUserList(UUID interest) {
    this.interest = interest;
    this.subscribedUser = new HashSet<>();
  }

  public static InterestUserList setUpNewInterestUserList(UUID interest) {
    return InterestUserList.builder()
        .interest(interest)
        .build();
  }

  public void addUser(UUID readerId) {
    subscribedUser.add(readerId);
  }

  public void subtractUser(UUID readerId) {
    subscribedUser.remove(readerId);
  }

  public boolean findUser(UUID readerId) {
    return subscribedUser.contains(readerId);
  }

  public long getUserCount() {
    return subscribedUser.size();
  }
}
