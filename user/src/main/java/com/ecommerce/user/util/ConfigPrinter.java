package com.ecommerce.user.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//@Component
public class ConfigPrinter {

    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${spring.data.mongodb.database:NOT_SET}")
    private String database;

//    @PostConstruct
//    public void init() {
//        System.out.println("Mongo URI = " + uri);
//        System.out.println("Mongo Database = " + database);
//    }
}

