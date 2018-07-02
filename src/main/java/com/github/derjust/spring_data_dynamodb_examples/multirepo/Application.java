package com.github.derjust.spring_data_dynamodb_examples.multirepo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.github.derjust.spring_data_dynamodb_examples.common.DynamoDBConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.github.derjust.spring_data_dynamodb_examples.common.DynamoDBConfig.waitForDynamoDBTable;

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
@Import(DynamoDBConfig.class)
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}

	@Bean
	public CommandLineRunner multirepo(CustomerRepository jpaRepository, DeviceRepository nosqlRepository, AmazonDynamoDB amazonDynamoDB, DynamoDBMapper dynamoDBMapper, DynamoDBMapperConfig config) {
		return (args) -> {
			demoJPA(jpaRepository);

			prepareNoSql(amazonDynamoDB, dynamoDBMapper, config, Device.class);

			demoNoSQL(nosqlRepository);
		};
	}


	private void prepareNoSql(AmazonDynamoDB amazonDynamoDB, DynamoDBMapper mapper, DynamoDBMapperConfig config, Class<?> entityClass) {
		String tableName = entityClass.getAnnotation(DynamoDBTable.class).tableName();
		try {
			amazonDynamoDB.describeTable(tableName);

			log.info("Table {} found", tableName);
			return;
		} catch (ResourceNotFoundException rnfe) {
			log.warn("Table {} doesn't exist - Creating", tableName);
		}


		CreateTableRequest ctr = mapper.generateCreateTableRequest(entityClass, config);
		ProvisionedThroughput pt = new ProvisionedThroughput(1L, 1L);
		ctr.withProvisionedThroughput(pt);
		List<GlobalSecondaryIndex> gsi = ctr.getGlobalSecondaryIndexes();
		if (gsi != null) {
			gsi.forEach(aGsi -> aGsi.withProvisionedThroughput(pt));
		}

		amazonDynamoDB.createTable(ctr);
		waitForDynamoDBTable(amazonDynamoDB, tableName);
	}

	private void demoNoSQL(DeviceRepository nosqlRepository) {
		// save a couple of devices
		nosqlRepository.save(new Device(1L, "Product A", "A", new Date()));
		nosqlRepository.save(new Device(1L, "Product B", "B", new Date()));
		nosqlRepository.save(new Device(2L, "Product C", "C", new Date()));

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
		Optional<Customer> customer = jpaRepository.findById(1L);
		log.info("Customer found with findOne(1L):");
		log.info("--------------------------------");
		log.info(customer.get().toString());
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
