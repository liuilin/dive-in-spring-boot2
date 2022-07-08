> Webflux 底层用的是 Reactor 框架，Reactor 框架是 Reactive Stream 规范的一个具体实现

- Reactor 反应堆模式：同步非阻塞
- Proactor 模式：异步非阻塞

# 从 Reactive 到 WebFlux

## 理解 Reactive

### 关于 Reactive 的一些说法

- Reactive 是异步非阻塞编程？
- Reactive 能够提升程序性能？
- Reactive 摆脱传统编程模型遇到的困境？

### Reactive 实现框架

- RxJava：Reactive extensions
- Reactor：Spring WebFlux Reactive类库
- Flow API：Java9 Flow API实现

### 传统编程模型中的某些困境

**Reactor 认为阻塞可能是浪费的**

> 3.1. Blocking Can Be Wasteful
>
> Modern applications can reach huge numbers of concurrent users, and, even though the capabilities of modern hardware have continued to improve, performance of modern software is still a key concern.
>
> There are broadly two ways one can improve a program’s performance:
>
> 1. **parallelize:** use more threads and more hardware resources.
> 2. **seek more efficiency** in how current resources are used.
>
> Usually, Java developers write programs using blocking code. This practice is fine until there is a performance bottleneck, at which point the time comes to introduce additional threads, running similar blocking code. But this scaling in resource utilization can quickly introduce contention and concurrency problems.
>
> Worse still, blocking wastes resources.
>
> So the parallelization approach is not a silver bullet.  

观点归纳

- 阻塞导致性能瓶颈和浪费资源
- 增加线程可能会引起资源竞争和并发问题

  > 比如并发时的可见性、原子性问题，读和写看到的东西是不同的
- 并行的方式不是银弹（不能解决所有问题）

  > 银弹：万金油，什么都能做，但不精
  >
  > 比如你 8 核的机器来开一万个线程，那 JVM 受不了，你的操作系统更加受不了

**理解阻塞的弊端**

串行（阻塞）场景 - 数据顺序加载，加载流程如下：

```sequence
load() ->> loadConfigurations() : 耗时 1s
loadConfigurations() - >>  loadUsers() : 耗时 2s
loadUsers() - >> loadOrders() : 耗时 3s
```

Java 实现

```java
public class DataLoader {
    public static void main(String[] args) {
        new DataLoader().load();
    }

    public final void load() {
        long startTime = System.currentTimeMillis(); // 开始时间
        doLoad(); // 具体执行
        long costTime = System.currentTimeMillis() - startTime; // 消耗时间
        System.out.println("load() 总耗时：" + costTime + " 毫秒");
    }

    protected void doLoad() { // 串行计算
        loadConfigurations(); // 耗时 1s
        loadUsers(); // 耗时 2s
        loadOrders(); // 耗时 3s
    } // 总耗时 1s + 2s + 3s = 6s

    protected final void loadConfigurations() {
        loadMock("loadConfigurations()", 1);
    }

    protected final void loadUsers() {
        loadMock("loadUsers()", 2);
    }

    protected final void loadOrders() {
        loadMock("loadOrders()", 3);
    }

    private void loadMock(String source, int seconds) {
        try {
            long startTime = System.currentTimeMillis();
            long milliseconds = TimeUnit.SECONDS.toMillis(seconds);
            Thread.sleep(milliseconds);
            long costTime = System.currentTimeMillis() - startTime;
            System.out.printf("[线程 : %s] %s 耗时 : %d 毫秒\n",
                    Thread.currentThread().getName(), source, costTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

> 结论：由于加载过程串行执行的关系，导致消耗线性累加（总耗时 6s）。Blocking 模式即串行执行



并行（非阻塞）场景 - 并行数据加载，加载流程如下：

```sequence
load() ->> loadConfigurations() : 耗时 1s
load() ->> loadUsers() : 耗时 2s
load() ->> loadOrders() : 耗时 3s
```

Java 实现

```java
public class ParallelDataLoader extends DataLoader {
    public static void main(String[] args) {
        new ParallelDataLoader().load();
    }

    @Override
    protected void doLoad() { // 并行计算
        ExecutorService executorService = Executors.newFixedThreadPool(3); // 创建线程池
        CompletionService completionService = new ExecutorCompletionService(executorService);
        completionService.submit(super::loadConfigurations, null); // 耗时 >= 1s
        completionService.submit(super::loadUsers, null); // 耗时 >= 2s
        completionService.submit(super::loadOrders, null); // 耗时 >= 3s
        int count = 0;
        while (count < 3) { // 等待三个任务完成
            // 一直拉取，知道全部拉取完就结束线程
            if (completionService.poll() != null) {
                count++;
            }
        }
        executorService.shutdown();
    } // 总耗时 max(1s, 2s, 3s) >= 3s
}
```

> 结论：使用并行执行，由于执行方法之间没有相互依赖，所以并行执行耗时降低到了 3s（即最耗时的线程）



**延伸思考**

1. 如果阻塞导致性能瓶颈和资源浪费的话，Reactive 也能解决这个问题？

2. 为什么不直接使用 Future#get() 方法强制所有任务执行完毕，然后再统计总耗时？

   [答案](#Future 问答2)

3. 由于以上三个方法之间没有数据依赖关系，所以执行方式由串行调整为并行后，能够达到性能提升的
   效果。如果方法之间存在依赖关系时，那么提升效果是否还会如此明显，并且如何确保它们的执行顺
   序？  



### Reactor 认为异步不一定能够救赎

> 3.2. Asynchronicity to the Rescue?
>
> The second approach (mentioned earlier), seeking more efficiency, can be a solution to the resource wasting problem. By writing *asynchronous*, *non-blocking* code, you let the execution switch to another active task **using the same underlying resources** and later come back to the current process when the asynchronous processing has finished.
>
> Java offers two models of asynchronous programming:
>
> - **Callbacks**: Asynchronous methods do not have a return value but take an extra callback parameter (a lambda or anonymous class) that gets called when the result is available. A well known example is Swing’s EventListener hierarchy.
> - **Futures**: Asynchronous methods return a Future<T> immediately. The asynchronous process computes a T value, but the Future object wraps access to it. The value is not immediately available, and the object can be polled until the value is available. For instance, ExecutorService running Callable<T> tasks use Future objects.
>
> Are these techniques good enough? Not for every use case, and both approaches have limitations.
>
> Callbacks are hard to compose together, quickly leading to code that is difficult to read and maintain (known as "Callback Hell").
>
> Futures are a bit better than callbacks, but they still do not do well at composition, despite the improvements brought in Java 8 by CompletableFuture . 

**观点归纳**

- Callbacks 是解决非阻塞的方案，然而他们之间很难组合，并且快速地将代码引导至 "Callback Hell"
  的不归路
- Futures 相对于 Callbacks 好一点，不过还是无法组合，不过 CompletableFuture 能够提升这方面
  的不足  

**理解 "Callback Hell"**

```java
public class JavaGUI {
    public static void main(String[] args) {
        JFrame jFrame = new JFrame("GUI 示例");
        jFrame.setBounds(500, 300, 400, 300);
        LayoutManager layoutManager = new BorderLayout(400, 300);
        jFrame.setLayout(layoutManager);
        jFrame.addMouseListener(new MouseAdapter() { // callback 1
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.printf("[线程 : %s] 鼠标点击，坐标(X : %d, Y : %d)\n",
                        currentThreadName(), e.getX(), e.getY());
            }
        });
        jFrame.addWindowListener(new WindowAdapter() { // callback 2
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.printf("[线程 : %s] 清除 jFrame... \n", currentThreadName());
                jFrame.dispose(); // 清除 jFrame
            }

            @Override
            public void windowClosed(WindowEvent e) {
                System.out.printf("[线程 : %s] 退出程序... \n", currentThreadName());
                System.exit(0); // 退出程序
            }
        });
        System.out.println("当前线程：" + currentThreadName());
        jFrame.setVisible(true);
    }

    private static String currentThreadName() { // 当前线程名称
        return Thread.currentThread().getName();
    }
}
```







Java Swing 的 GUI 程序鼠标点击就是非阻塞回调的方式，也就是事件监听（Callback 回调方式）。每次的鼠标回调事件都没有被主线程阻塞

> Callable、Runnable 都是 @FunctionalInterface 修饰的函数式接口
>
> 同步、异步是线程模型；阻塞、非阻塞是编程模型



结论：Java GUI 以及事件/监听模式基本采用匿名内置类实现，即回调实现。从本例可以得出，鼠标的点击确实没有被其他线程给阻塞。不过当监听的维度增多时，Callback 实现也随之增多。同时，事件/监听者模式的并发模型可为同步或异步。

> 回顾
>
> Spring 事件/监听器（同步/异步）：
>
> - 事件： ApplicationEvent
> - 事件监听器： ApplicationListener
> - 事件广播器： ApplicationEventMulticaster
> - 事件发布器： ApplicationEventPublisher
>
> Servlet 事件/监听器
>
> - 同步
>   - 事件： ServletContextEvent
>   - 事件监听器： ServletContextListener
> - 异步
>   - 事件： AsyncEvent
>   - 事件监听器： AsyncListener  



**理解 Future 阻塞问题**

如果 DataLoader 的 loadOrders() 方法依赖于 loadUsers() 的结果，而 loadUsers() 又依赖于
loadConfigurations() ，调整实现：  

```java
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
```

> Future#get() 方法不得不等待任务执行完成，换言之，如果多个任务提交后，返回的多个 Future 逐一调用 get() 方法时，将会依次阻塞，任务的执行从并行变为串行。
>
> 这也是之前 ”“延伸思考”问答 2 的<span id="Future 问答2">答案：</span>

**理解 Future 链式问题**

### Reactive Programming 特性

- Reactor 反应堆模式：同步非阻塞
- Proactor 模式：异步非阻塞
- 观察者模式（Observer）：推的模式，从服务器推往客户端（事件/监听者模式，相当于监听事件）
- 迭代器模式（Iterator）：拉的模式，从客户端向服务器拉（像比如 List、Array 都有迭代器，可以去循环获取）

> Webflux 底层就是 reactive，是同步+异步+设计模式的综合体

## Reactive Streams 规范

## Reactor 框架运用

















并行并不是银弹（并不能解决所有问题）：8 核 CPU 开 10000 个线程，那 JVM 受不了，操作系统也更加受不了

NIO 其实就是 Reactor 模式，也就是同步非阻塞

Proactor 是异步非阻塞

同步/异步是线程模型，而阻塞/非阻塞是编程模型

**思考：**Future.get() 方式会将异步并行变为串行，导致阻塞。执行时长和串行一样，还增加了线程开销。所以并发模型或者说线程池的一些底层 API 用起来要非常的小心，增加了我们使用的难度

非阻塞是编程模型，并不是并发模型



**设计模式（Design Patterns）**

- 扩展模式：观察者（[Observer](https://en.wikipedia.org/wiki/Observer_pattern)） - 推模式（push-based）
- 混合模式：[Reactor](https://en.wikipedia.org/wiki/Reactor_pattern)（同步非阻塞）、[Proactor](https://en.wikipedia.org/wiki/Proactor_pattern)（异步非阻塞），共同点都是非阻塞
- 对立模式：迭代器（[Iterator](https://en.wikipedia.org/wiki/Iterator_pattern)） - 拉模式（pull-based）

**模式对比**

An Observable(RxJava) is the asynchronous/push “[dual](http://en.wikipedia.org/wiki/Dual_(category_theory))” to the synchronous/pull Iterable  

| event          | Iterable (pull)  | Observable (push)  |
| -------------- | ---------------- | ------------------ |
| data           | T next()         | onNext(T)          |
| discover error | throws Exception | onError(Exception) |
| complete       | !hasNext()       | onCompleted()      |



**数据结构（Data Structure）**

- 流式（Streams）
- 序列（Sequences ）
- 事件（Events）  

**并发模式（Concurrency Model）**

非阻塞（Non-Blocking）

- 同步（Synchronous ）
- 异步（Asynchronous ）  

小结：屏蔽并发编程细节，如线程、同步、线程安全以及并发数据结构

> 可以让开发者关注于加锁、解锁等等的细节，从而不犯错



