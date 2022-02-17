package com.liumulin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan("com.liumulin.controller")
public class DiveInSpringBoot2Application {

    public static void main(String[] args) {
        SpringApplication.run(DiveInSpringBoot2Application.class, args);
    }

}