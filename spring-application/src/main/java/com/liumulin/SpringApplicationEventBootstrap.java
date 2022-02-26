package com.liumulin;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Spring Framework 事件监听启动类
 *
 * @author liuqiang
 * @since 2022-02-26
 */
public class SpringApplicationEventBootstrap {

    public static void main(String[] args) {
        // 创建上下文
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        // 注册应用事件监听器
        context.addApplicationListener(event -> System.out.println("监听到事件：" + event));
        // 启动上下文
        context.refresh();
        context.publishEvent("HelloWorld");
        context.publishEvent("2022");
        context.publishEvent(new ApplicationEvent("Nice") {
//            @Override
//            public Object getSource() {
//                return "Nice";
//            }
        });
        // 关闭上下文
        context.close();
    }
}
