package com.liumulin.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 加载应用事件监听器
 *
 * @author liuqiang
 * @since 2022-02-26
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HelloWorldApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("Hello World: " + event.getApplicationContext().getId() +
                "timestamp: " + event.getTimestamp());
    }
}
