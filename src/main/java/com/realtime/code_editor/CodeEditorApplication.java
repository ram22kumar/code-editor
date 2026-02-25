package com.realtime.code_editor;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CodeEditorApplication {

	@Value("${spring.data.mongodb.uri:NOT_SET}")
	private String mongoUri;

	@PostConstruct
	public void logMongoUri() {
		System.out.println("Mongo URI LOADED = " + mongoUri);
	}
	public static void main(String[] args) {
		SpringApplication.run(CodeEditorApplication.class, args);
	}

}
