package com.smartcrop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartCropApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCropApplication.class, args);
    }
}