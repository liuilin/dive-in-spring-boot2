package com.liumulin.boostrap;

import com.liumulin.annotation.EnableHelloWorld;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author liuqiang
 * @since 2022-02-18
 */
@EnableAutoConfiguration
public class EnableAutoConfigurationBootstrap {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(EnableAutoConfigurationBootstrap.class)
                .web(WebApplicationType.NONE)
                .run(args);
        // check helloWorld bean exist
        String helloWorld = ctx.getBean("helloWorld", String.class);
        System.out.println("helloWorld = " + helloWorld);
        ctx.close();
    }
}
