package com.liumulin.annotation;

import com.liumulin.configuration.HelloWorldConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author liuqiang
 * @since 2022-02-18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@Import(HelloWorldConfiguration.class)
@Import(HelloWorldImportSelector.class) // 比 @Import(HelloWorldConfiguration.class) 更具有优势，可以在里面增加判断条件
public @interface EnableHelloWorld {
}
