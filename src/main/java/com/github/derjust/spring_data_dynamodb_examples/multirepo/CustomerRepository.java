package com.github.derjust.spring_data_dynamodb_examples.multirepo;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/** The JPA repository */
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    List<Customer> findByLastName(String lastName);
}
