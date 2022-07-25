package com.liumulin.reactive.reactor;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Flux 示例
 *
 * @author Daniel Liu
 * @since 2022-07-26
 */
public class FluxDemo {

    public static void main(String[] args) throws InterruptedException {
        println("run...");
        Flux.just("A", "B", "C") // 发布 A->B->C
                .publishOn(Schedulers.boundedElastic()) // 线程池切换
                .map(value -> "+" + value) // "A" -> "+A"
//                .subscribe(
//                        FluxDemo::println, // 数据消费 = onNext(T)
//                        FluxDemo::println, // 异常处理 = onError(Throwable)
//                        () -> println("完成操作！"), // 完成操作 = onComplete()
//                        subscription -> subscription.request(1) // 背压操作 onSubscribe(Subscription)
//                );
                .subscribe(new Subscriber<>() {
                    Subscription subscription;
                    int count=0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(String s) {
                        if (count==2) {
                            throw new RuntimeException("自定义抛出异常");
                        }
                        println(s);
                        count++;
                        subscription.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        println(t);
                    }

                    @Override
                    public void onComplete() {
                        println("完成操作！");
                    }
                });

        Thread.sleep(1000);
    }

    public static void println(Object object) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[线程：" + threadName + "]:" + object);
    }

}
