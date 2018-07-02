package com.github.derjust.spring_data_dynamodb_examples.custom;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.github.derjust.spring_data_dynamodb_examples.common.DynamoDBConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.boot.CommandLineRunner;
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

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

import static com.github.derjust.spring_data_dynamodb_examples.common.DynamoDBConfig.checkOrCreateTable;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, //No JPA
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@EnableDynamoDBRepositories(
        includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                UserRepository.class}
        )}
)
@Configuration
@Import(DynamoDBConfig.class)
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .profiles("custom")
                .run(args);
    }

    @Bean
    public CommandLineRunner custom(ConfigurableApplicationContext ctx, UserRepository userRepository, AmazonDynamoDB amazonDynamoDB, DynamoDBMapper dynamoDBMapper, DynamoDBMapperConfig config) {
        return (args) -> {
            checkOrCreateTable(amazonDynamoDB, dynamoDBMapper, config, User.class);

            demoCustomInterface(userRepository);

            ctx.close();
        };
    }

    private void demoCustomInterface(UserRepository userRepository) {

        // Create user & save it (creates Id)
        User user = createUser();
        userRepository.save(user);

        log.info("Created user: {}", user);

        // Call custom method on interface
        userRepository.calculateAge(user);
        log.info("Called custom method: {}", user);

        // Reload instance to ensure custom method worked
        Optional<User> reloadedUser = userRepository.findById(user.getId());

        assert reloadedUser.isPresent();

        log.info("Comparison - Old entity: {}", user);
        log.info("Comparison - New entity: {}", reloadedUser.get());
    }

    private User createUser() {
        User user = new User();

        user.setFirstname("Sebastian");
        user.setLastname("Mueller");

        Random r = new Random();
        user.setBirthday(Instant.ofEpochMilli(r.nextInt()));

        return user;
    }

}
