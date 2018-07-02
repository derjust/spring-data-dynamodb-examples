package com.github.derjust.spring_data_dynamodb_examples.common;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
public class DynamoDBConfig {
    private static final Logger log = LoggerFactory.getLogger(DynamoDBConfig.class);

    @Value("${amazon.aws.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey}")
    private String amazonAWSSecretKey;
    
    public AWSCredentialsProvider amazonAWSCredentialsProvider() {
        return new AWSStaticCredentialsProvider(amazonAWSCredentials());
    }

    @Bean
    public AWSCredentials amazonAWSCredentials() {
        return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        return DynamoDBMapperConfig.DEFAULT;
    }

    @Bean
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB, DynamoDBMapperConfig config) {
        return new DynamoDBMapper(amazonDynamoDB, config);
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
                .withCredentials(amazonAWSCredentialsProvider())
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    @Bean
    @Profile("rest")
    public DynamoDBMappingContext dynamoDBMappingContext() {
        return new DynamoDBMappingContext();
    }

    @Bean
    @Profile("multirepo")
    public MySQLShutdownApplicationListener mySQLShutdownApplicationListener() {
        return new MySQLShutdownApplicationListener();
    }


    public static void checkOrCreateTable(AmazonDynamoDB amazonDynamoDB, DynamoDBMapper mapper, DynamoDBMapperConfig config, Class<?> entityClass) {
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

    public static void waitForDynamoDBTable(AmazonDynamoDB amazonDynamoDB, String tableName) {
        do {
            try {
                Thread.sleep(5 * 1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException("Couldn't wait detect table " + tableName);
            }
        }
        while (!amazonDynamoDB.describeTable(tableName).getTable().getTableStatus().equals(TableStatus.ACTIVE.name()));
    }
}