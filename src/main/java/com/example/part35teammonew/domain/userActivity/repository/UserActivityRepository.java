package com.example.part35teammonew.domain.userActivity.repository;

import com.example.part35teammonew.domain.userActivity.entity.UserActivity;
import java.util.Optional;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityRepository extends MongoRepository<UserActivity, ObjectId> {

  Optional<UserActivity> findByUserId(UUID userId);

  void deleteByUserId(UUID id);
}
