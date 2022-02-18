package com.liumulin.configuration;

import com.liumulin.annotation.EnableHelloWorld;
import org.springframework.context.annotation.Configuration;

/**
 * @author liuqiang
 * @since 2022-02-18
 */
@Configuration // Spring 模式注解装配
@EnableHelloWorld // Spring @Enable 模块装配
//@ConditionalOnSystemProperty(name = "user.name", value = "Mercy") // 条件装配
public class HelloWorldAutoConfiguration {
}
