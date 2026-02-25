package com.realtime.code_editor.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Value("${MONGODB_URI}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        System.out.println("=== CREATING MONGO CLIENT ===");
        System.out.println("URI: " + mongoUri);

        ConnectionString connectionString = new ConnectionString(mongoUri);

        System.out.println("Hosts: " + connectionString.getHosts());
        System.out.println("Database: " + connectionString.getDatabase());

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, "code_editor");
    }
}