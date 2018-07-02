package com.github.derjust.spring_data_dynamodb_examples.rest;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

@EnableScan()
public interface UserRepository extends CrudRepository<User, UUID> {




}
