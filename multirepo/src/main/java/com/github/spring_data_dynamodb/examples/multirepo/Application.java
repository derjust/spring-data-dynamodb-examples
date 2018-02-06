package com.github.spring_data_dynamodb.examples.multirepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Date;

@SpringBootApplication
@EnableJpaRepositories(
		includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
				CustomerRepository.class}
		)}
)
@EnableDynamoDBRepositories(
		includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
				DeviceRepository.class}
		)}
)
@Configuration
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}

	@Bean
	public CommandLineRunner demo(CustomerRepository jpaRepository, DeviceRepository nosqlRepository) {
		return (args) -> {
			demoJPA(jpaRepository);

			demoNoSQL(nosqlRepository);
		};
	}

	private void demoNoSQL(DeviceRepository nosqlRepository) {
		// save a couple of devices
		nosqlRepository.save(new Device(1L, "Product A", "A", new Date()));
		nosqlRepository.save(new Device(1L, "Product B", "B", new Date()));
		nosqlRepository.save(new Device(2L, "Product CB", "C", new Date()));

		// fetch all devices
		log.info("Devices found with findAll():");
		log.info("-------------------------------");
		for (Device device : nosqlRepository.findAll()) {
            log.info(device.toString());
        }
		log.info("");

		// fetch by custom method
		log.info("Device's product found with fancyCustomMethod(1L, 'Product B'):");
		log.info("--------------------------------------------");
		String product = nosqlRepository.fancyCustomMethod(new DeviceKey(1L, "Product B"));
		log.info(product);
		log.info("");
	}

	private void demoJPA(CustomerRepository jpaRepository) {
		// save a couple of customers
		jpaRepository.save(new Customer("Jack", "Bauer"));
		jpaRepository.save(new Customer("Chloe", "O'Brian"));
		jpaRepository.save(new Customer("Kim", "Bauer"));
		jpaRepository.save(new Customer("David", "Palmer"));
		jpaRepository.save(new Customer("Michelle", "Dessler"));

		// fetch all customers
		log.info("Customers found with findAll():");
		log.info("-------------------------------");
		for (Customer customer : jpaRepository.findAll()) {
            log.info(customer.toString());
        }
		log.info("");

		// fetch an individual customer by ID
		Customer customer = jpaRepository.findOne(1L);
		log.info("Customer found with findOne(1L):");
		log.info("--------------------------------");
		log.info(customer.toString());
		log.info("");

		// fetch customers by last name
		log.info("Customer found with findByLastName('Bauer'):");
		log.info("--------------------------------------------");
		for (Customer bauer : jpaRepository.findByLastName("Bauer")) {
            log.info(bauer.toString());
        }
		log.info("");
	}

}
