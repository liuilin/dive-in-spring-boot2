package com.liumulin.annotation.condition;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 条件装配注解
 *
 * @author liuqiang
 * @since 2022-02-19
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnSystemPropertyCondition.class)
public @interface ConditionalOnSystemProperty {

    /**
     * 系统属性名
     */
    String name();

    /**
     * 系统属性值
     */
    String value() default "";
}
