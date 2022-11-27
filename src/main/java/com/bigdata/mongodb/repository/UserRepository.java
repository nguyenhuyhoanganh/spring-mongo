package com.bigdata.mongodb.repository;

import com.bigdata.mongodb.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
}
