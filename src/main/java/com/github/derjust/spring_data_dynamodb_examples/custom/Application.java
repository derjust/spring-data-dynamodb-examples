package com.github.derjust.spring_data_dynamodb_examples.custom;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.github.derjust.spring_data_dynamodb_examples.common.DynamoDBConfig;
import com.github.derjust.spring_data_dynamodb_examples.multirepo.CustomerRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.github.derjust.spring_data_dynamodb_examples.common.DynamoDBConfig.waitForDynamoDBTable;

@SpringBootApplication
@EnableJpaRepositories(
        includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                CustomerRepository.class}
        )}
)
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
        SpringApplication.run(Application.class);
    }

    @Bean
    public CommandLineRunner custom(UserRepository userRepository, AmazonDynamoDB amazonDynamoDB, DynamoDBMapper dynamoDBMapper, DynamoDBMapperConfig config) {
        return (args) -> {
            prepareNoSql(amazonDynamoDB, dynamoDBMapper, config, User.class);

            demoCustomInterface(userRepository);
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
}
