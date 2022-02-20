package com.liumulin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan("com.liumulin.web.servlet")
public class OverviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(OverviewApplication.class, args);
    }

}
