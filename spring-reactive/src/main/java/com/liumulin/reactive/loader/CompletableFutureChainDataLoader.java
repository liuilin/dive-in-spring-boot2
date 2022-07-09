package com.liumulin.reactive.loader;

import java.util.concurrent.CompletableFuture;

/**
 * 链式数据加载器
 *
 * @author Daniel Liu
 * @since 2022-07-09
 */
public class CompletableFutureChainDataLoader extends DataLoader {
    public static void main(String[] args) {
        new CompletableFutureChainDataLoader().load();
    }

    @Override
    protected void doLoad() {
        CompletableFuture
                .runAsync(super::loadConfigurations)
                .thenRun(super::loadUsers)
                .thenRun(super::loadOrders)
                .whenComplete((res, throwable) -> { // 完成时回调
                    System.out.println("加载完成");
                })
                .join(); // 等待完成
    }
}
