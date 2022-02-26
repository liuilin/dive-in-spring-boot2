package com.liumulin.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

/**
 * 加载应用事件监听器
 *
 * @author liuqiang
 * @since 2022-02-26
 */
public class AfterHelloWorldApplicationListener implements ApplicationListener<ContextRefreshedEvent>, Ordered {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("After Hello World: " + event.getApplicationContext().getId() +
                "timestamp: " + event.getTimestamp());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
