/**
 * Copyright © 2018 spring-data-dynamodb-example (https://github.com/derjust/spring-data-dynamodb-examples)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.derjust.spring_data_dynamodb_examples.rest;

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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.github.derjust.spring_data_dynamodb_examples.common.DynamoDBConfig;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, // No JPA
		DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@EnableDynamoDBRepositories(mappingContextRef = "dynamoDBMappingContext", basePackageClasses = UserRepository.class)
@Configuration
@Import({DynamoDBConfig.class})
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).profiles("rest").web(WebApplicationType.SERVLET).run(args);
	}

	@Bean
	public CommandLineRunner rest(ConfigurableApplicationContext ctx, UserRepository dynamoDBRepository,
			AmazonDynamoDB amazonDynamoDB, DynamoDBMapper dynamoDBMapper, DynamoDBMapperConfig config) {
		return (args) -> {

			CreateTableRequest ctr = dynamoDBMapper.generateCreateTableRequest(User.class)
					.withProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
			TableUtils.createTableIfNotExists(amazonDynamoDB, ctr);
			TableUtils.waitUntilActive(amazonDynamoDB, ctr.getTableName());

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
