package com.github.spring_data_dynamodb.examples.multirepo;

import org.socialsignin.spring.data.dynamodb.core.DynamoDBTemplate;
import org.springframework.beans.factory.annotation.Autowired;

// Most important this class has to be named like the interface with an 'Impl' suffix
// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.single-repository-behavior
public class DeviceAdditionRepositoryImpl implements DeviceAdditionRepository {

    // Inject everything you want as this is created like a normal bean
    @Autowired
    private DynamoDBTemplate dynamoDBTemplate;

    @Override
    public String fancyCustomMethod(DeviceKey key) {
        // custom code here

        Device device = dynamoDBTemplate.load(Device.class, key);
        if (device == null) {
            return "Not found";
        } else {
            return device.getProductId();
        }
    }
}
