package org.example.videocall;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VideocallApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideocallApplication.class, args);
    }
}

