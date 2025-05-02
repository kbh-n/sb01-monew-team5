package com.example.part35teammonew.domain.interestUserList.repository;

import com.example.part35teammonew.domain.interestUserList.entity.InterestUserList;
import java.util.Optional;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InterestUserListRepository extends MongoRepository<InterestUserList, ObjectId> {

  Optional<InterestUserList> findByInterest(UUID interest);

  void deleteByInterest(UUID interest);
}
