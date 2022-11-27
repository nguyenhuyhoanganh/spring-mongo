package com.bigdata.mongodb.repository;


import com.bigdata.mongodb.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
