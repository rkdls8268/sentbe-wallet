package com.sentbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SentbeApplication {

  public static void main(String[] args) {
    SpringApplication.run(SentbeApplication.class, args);
  }

}
