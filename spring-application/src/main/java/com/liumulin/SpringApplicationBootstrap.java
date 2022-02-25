package com.liumulin;

import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * {@link SpringApplication 引导类}
 */
public class SpringApplicationBootstrap {

    public static void main(String[] args) {
//        SpringApplication.run(SpringApplicationBootstrap.class, args);

        Set<String> sources = new HashSet<>();
        sources.add(ApplicationConfiguration.class.getName());
        SpringApplication springApplication = new SpringApplication();
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.setSources(sources);
        ConfigurableApplicationContext context = springApplication.run(args);
        System.out.println("Bean " + context.getBean(ApplicationConfiguration.class));
    }

    // 必须要加 static，否则会报错：
    // Parameter 0 of constructor in com.liumulin.SpringApplicationBootstrap$ApplicationConfiguration required a bean of type 'com.liumulin.SpringApplicationBootstrap' that could not be found.
    @SpringBootApplication
    public static class ApplicationConfiguration {

    }
}
