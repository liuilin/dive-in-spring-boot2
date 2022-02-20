package com.liumulin.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liuqiang
 * @since 2022-02-18
 */
@Configuration
public class HelloWorldConfiguration {
    /**
     * 方法名即 Bean 名称
     */
    @Bean
    public String helloWorld(){
        return "Hello World, Bean Test";
    }
}
