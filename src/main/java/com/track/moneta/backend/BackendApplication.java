package com.track.moneta.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
//        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
//        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
//        System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
//        System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
		SpringApplication.run(BackendApplication.class, args);
	}

}
