package com.github.spring_data_dynamodb.examples.multirepo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;

import java.io.Serializable;

public class DeviceKey implements Serializable {

    private Long vendorId;

    private String product;

    public DeviceKey() {}

    public DeviceKey(Long vendorIr, String product) {
        this.vendorId = vendorIr;
        this.product = product;
    }

    @DynamoDBHashKey
    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    @DynamoDBRangeKey
    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}
