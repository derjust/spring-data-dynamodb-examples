package com.github.derjust.spring_data_dynamodb_examples.multirepo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

/** The JPA repository */
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    List<Customer> findByLastName(String lastName);
}
