package com.liumulin.reactive.loader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * {@link Future} 阻塞数据加载器
 * @author Daniel
 */
public class FutureBlockingDataLoader extends DataLoader {
    public static void main(String[] args) {
        new FutureBlockingDataLoader().load();
    }

    @Override
    protected void doLoad() {
        ExecutorService executorService = Executors.newFixedThreadPool(3); // 创建线程池
        runCompletely(executorService.submit(super::loadConfigurations));
        runCompletely(executorService.submit(super::loadUsers));
        runCompletely(executorService.submit(super::loadOrders));
        executorService.shutdown();
    }

    private void runCompletely(Future<?> future) {
        try {
            future.get();
        } catch (Exception e) {
        }
    }
}