package com.ecommerce.user.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Test {

    @Value("${spring.data.mongodb.connection-string}")
    private String uri;

    @PostConstruct
    public void init() {
        System.out.println(uri);
    }
}