package com.github.derjust.spring_data_dynamodb_examples.custom;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

@SuppressWarnings("unused") // This class is used as per https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.custom-implementations
public class CustomUserRepositoryMethodsImpl implements CustomUserRepositoryMethods {

    private final DynamoDBMapper mapper;

    @Autowired
    public CustomUserRepositoryMethodsImpl(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public User calculateAge(User user) {
        // Just some javax.time mumbo-jumbo
        Instant birthday = user.getBirthday();
        LocalDate now = LocalDate.now();
        Period age = Period.between(LocalDate.ofInstant(birthday, ZoneId.systemDefault()), now);

        user.setAge(age.getYears());
        mapper.save(user);

        return user;
    }

}
