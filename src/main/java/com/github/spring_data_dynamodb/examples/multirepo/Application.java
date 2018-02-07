package com.github.spring_data_dynamodb.examples.multirepo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
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
import java.util.List;

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
	public CommandLineRunner demo(CustomerRepository jpaRepository, DeviceRepository nosqlRepository, AmazonDynamoDB amazonDynamoDB) {
		return (args) -> {
			demoJPA(jpaRepository);

			prepareNoSql(amazonDynamoDB, Device.class);

			demoNoSQL(nosqlRepository);
		};
	}

	private boolean dynamoDBTableExists(AmazonDynamoDB amazonDynamoDB, String tableName) {
		List<String> existingTables = amazonDynamoDB.listTables().getTableNames();
		if (existingTables.contains(tableName)) {
			return true;
		} else {
			return false;
		}
	}

	private void waitForDynamoDBTable(AmazonDynamoDB amazonDynamoDB, String tableName, boolean exists) {
		do {
			try {
				Thread.sleep(5 * 1000L);
			} catch (InterruptedException e) {
				throw new RuntimeException("Couldn't wait detect table " + tableName);
			}
		}
		while(dynamoDBTableExists(amazonDynamoDB, tableName) != exists);
	}

	private void prepareNoSql(AmazonDynamoDB amazonDynamoDB, Class<?> entityClass) {
		String tableName = entityClass.getAnnotation(DynamoDBTable.class).tableName();
		try {
			amazonDynamoDB.describeTable(tableName);

			log.info("Table {} found", tableName);
			return;
		} catch (ResourceNotFoundException rnfe) {
			log.warn("Table {} doesn't exist - Creating", tableName);
		}

		CreateTableRequest ctr = new CreateTableRequest().withTableName(tableName);
		ctr.withProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
		ctr.withAttributeDefinitions(
				new AttributeDefinition().withAttributeName("VendorId").withAttributeType(ScalarAttributeType.N),
				new AttributeDefinition().withAttributeName("ProductId").withAttributeType(ScalarAttributeType.S));

		ctr.withKeySchema(
				new KeySchemaElement().withAttributeName("VendorId").withKeyType(KeyType.HASH),
				new KeySchemaElement().withAttributeName("ProductId").withKeyType(KeyType.RANGE));

		amazonDynamoDB.createTable(ctr);
		waitForDynamoDBTable(amazonDynamoDB, tableName, true);
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
