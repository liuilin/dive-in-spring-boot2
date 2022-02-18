package com.liumulin.boostrap;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author liuqiang
 * @since 2022-02-18
 */
@ComponentScan(basePackages = "com.liumulin.configuration")
public class RepositoryBootstrap {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(RepositoryBootstrap.class)
                .web(WebApplicationType.NONE)
                .run(args);
        // 验证 Bean 是否存在
//        MyFirstLevelRepository firstLevelRepository = context.getBean("firstLevelRepository", MyFirstLevelRepository.class);
//        System.out.println("firstLevelRepository = " + firstLevelRepository);
        String helloWorld = context.getBean("helloWorld", String.class);
        System.out.println("helloWorld = " + helloWorld);
        // 关闭容器
        context.close();
    }
}
