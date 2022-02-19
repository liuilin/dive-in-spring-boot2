package com.liumulin.configuration;

import com.liumulin.annotation.EnableHelloWorld;
import com.liumulin.annotation.condition.ConditionalOnSystemProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 自动装配实现
 *
 * @author liuqiang
 * @since 2022-02-19
 */
@Configuration // Spring 模式注解装配
@EnableHelloWorld // Spring @Enable 模块装配
@ConditionalOnSystemProperty(name = "user.name", value = "Daniel")
public class HelloWorldAutoConfiguration {
}
