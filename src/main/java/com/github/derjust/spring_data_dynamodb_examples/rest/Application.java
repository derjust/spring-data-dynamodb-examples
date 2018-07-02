package com.github.derjust.spring_data_dynamodb_examples.rest;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.github.derjust.spring_data_dynamodb_examples.common.DynamoDBConfig;
import com.github.derjust.spring_data_dynamodb_examples.multirepo.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import static com.github.derjust.spring_data_dynamodb_examples.common.DynamoDBConfig.checkOrCreateTable;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, //No JPA
		DataSourceTransactionManagerAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class})
@EnableDynamoDBRepositories(
		mappingContextRef = "dynamoDBMappingContext",
		includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
				UserRepository.class}
		)}
)
@Configuration
@Import({DynamoDBConfig.class})
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class)
				.profiles("rest")
				.web(WebApplicationType.SERVLET)
				.run(args);
	}

	@Bean
	public CommandLineRunner rest(ConfigurableApplicationContext ctx, UserRepository dynamoDBRepository, AmazonDynamoDB amazonDynamoDB, DynamoDBMapper dynamoDBMapper, DynamoDBMapperConfig config) {
		return (args) -> {

			checkOrCreateTable(amazonDynamoDB, dynamoDBMapper, config, Device.class);

			createEntities(dynamoDBRepository);

			log.info("");
			log.info("Run curl -v http://localhost:8080/users and follow the HATEOS links");
			log.info("");
			log.info("Press <enter> to shutdown");
			System.in.read();
			ctx.close();
		};
	}


	private void createEntities(UserRepository dynamoDBRepository) {
		// save a couple of devices
		dynamoDBRepository.save(new User("me", "me"));
		dynamoDBRepository.save(new User("you", "you"));

		// fetch all devices
		log.info("Users found with findAll():");
		log.info("-------------------------------");
		for (User user : dynamoDBRepository.findAll()) {
            log.info(user.toString());
        }
		log.info("");

	}

}
