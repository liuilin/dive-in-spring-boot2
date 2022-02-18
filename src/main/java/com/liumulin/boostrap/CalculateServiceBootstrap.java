package com.liumulin.boostrap;

import com.liumulin.service.CalculateService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author liuqiang
 * @since 2022-02-19
 */
@SpringBootApplication(scanBasePackages = "com.liumulin.service")
public class CalculateServiceBootstrap {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(CalculateServiceBootstrap.class)
                .web(WebApplicationType.NONE)
                .profiles("Java8")
                .run(args);
        CalculateService calculateService = ctx.getBean(CalculateService.class);
        Integer sum = calculateService.sum(1, 2, 3, 4, 5, 6, 7, 8, 9);
        System.out.println("sum = " + sum);
        ctx.close();
    }
}
