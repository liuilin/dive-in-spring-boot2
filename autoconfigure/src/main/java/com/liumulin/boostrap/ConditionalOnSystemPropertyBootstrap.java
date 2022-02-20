package com.liumulin.boostrap;

import com.liumulin.annotation.condition.ConditionalOnSystemProperty;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @author liuqiang
 * @since 2022-02-19
 */
public class ConditionalOnSystemPropertyBootstrap {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ConditionalOnSystemPropertyBootstrap.class)
                .web(WebApplicationType.NONE)
                .run(args);

        String helloWorld = context.getBean("helloWorld", String.class);
        System.out.println("helloWorld = " + helloWorld);
        context.close();
    }

    @Bean
    @ConditionalOnSystemProperty(name = "user.name", value = "Daniel")
    public String helloWorld() {
        return "Hello World condition";
    }
}
