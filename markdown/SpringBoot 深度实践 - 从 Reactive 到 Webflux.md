> Webflux 底层用的是 Reactor 框架，Reactor 框架是 Reactive Stream 规范的一个具体实现

- Reactor 反应堆模式：同步非阻塞
- Proactor 模式：异步非阻塞

# 从 Reactive 到 WebFlux

## 理解 Reactive

### 关于 Reactive 的一些说法

- Reactive 是异步非阻塞编程？
- Reactive 能够提升程序性能？
- Reactive 摆脱传统编程模型遇到的困境？

> Wikipedia 上查的一些概念是大家综合性的描述，不仅仅是局限于 Java

### Reactive 实现框架

- RxJava：Reactive extensions
- Reactor：Spring WebFlux Reactive类库
- Flow API：Java9 Flow API实现

### 传统编程模型中的某些困境

1. **Reactor 认为阻塞可能是浪费的**

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

   **观点归纳**

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

   **理解并行的复杂**

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

   3. 由于以上三个方法之间没有数据依赖关系，所以执行方式由串行调整为并行后，能够达到性能提升的效果。如果方法之间存在依赖关系时，那么提升效果是否还会如此明显，并且如何确保它们的执行顺序？

2. **Reactor 认为异步不一定能够救赎**

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

   > 回顾：
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

   如果 DataLoader 的 loadOrders() 方法依赖于 loadUsers() 的结果，而 loadUsers() 又依赖 loadConfigurations() ，调整实现：  

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

   结论：Future#get() 方法不得不等待任务执行完成，换言之，如果多个任务提交后，返回的多个 Future 逐一调 get() 方法时，将会依次阻塞，任务的执行从并行变为串行。

   > 这也是之前 ”“延伸思考”问答 2 的<span id="Future 问答2">答案：</span>

   **理解 Future 链式问题**

   由于 Future 无法实现异步执行结果链式处理，尽管 FutureBlockingDataLoader 能够解决方法数据依赖以及顺序执行的问题，不过它将并行执行带回了阻塞（串行）执行。所以，它不是一个理想实现。不过 CompletableFuture 可以帮助提升 Future 的限制：  

   ```java
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
   ```

   ```sequence
   main 线程 ->> CompletableFuture 线程 : 线程切换
   CompletableFuture 线程 ->> loadConfigurations() : 耗时 1s
   loadConfigurations() ->> loadUsers() : 耗时 2s
   loadUsers() ->> loadOrders() : 耗时 3s
   loadOrders() ->> wehenComplete(BiConsumer) : 执行完成时回调
   CompletableFuture 线程 ->> main 线程: 等待 CompletableFuture 线程执行结束
   ```

   结论：

   - 如果阻塞导致性能瓶颈和资源浪费的话，Reactive 也能解决这个问题？
   - CompletableFuture 属于异步操作，如果强制等待结束的话，又回到了阻塞编程的方式，那么 Reactive 也会面临同样的问题吗？
   - CompletableFuture 让我们理解到非阻塞不一定提升性能，那么 Reactive 也会这样吗？

3. **[Reactive Streams JVM]((https://github.com/reactive-streams/reactive-streams-jvm)) 认为异步系统和资源消费需要特殊处理**

   > Handling streams of data—especially “live” data whose volume is not predetermined—requires special care in an asynchronous system. The most prominent issue is that resource consumption needs to be carefully controlled such that a fast data source does not overwhelm the stream destination. Asynchrony is needed in order to enable the parallel use of computing resources, on collaborating network hosts or multiple CPU cores within a single machine.

   观点归纳：

   - 流数据大小难以预判

   - 异步编程复杂（底层 API 多语义。调用起来复杂）

   - 数据源和消费端之间资源消费难以平衡

     > such that a fast data source does not overwhelm the stream destination：这样一个快速的数据源就不会压倒流目的地。（也就是说会遇到数据生产源发射数据比较快，消费端消费比较慢等一系列问题）

4. **思考**

   - Reactive 到底是什么？

   - Reactive 的使用场景在哪里？
   - Reactive 存在怎样限制/不足？

### Reactive Programming 定义

RxJava 和竞争对手 Reactor

- [The Reactive Manifesto](https://www.reactivemanifesto.org/)

  > Reactive Systems are: Responsive, Resilient, Elastic and Message Driven

  关键字：

  - 响应的（Responsive）
  - 适应性强的（Resilient）
  - 弹性的（Elastic）
  - 消息驱动的（Message Driven）

  侧重点：

  - 面向 Reactive 系统
  - Reactive 系统原则

- [Reactive 维基百科](https://en.wikipedia.org/wiki/Reactive_programming)

  > In computing, reactive programming is a declarative programming paradigm concerned with **data streams** and **the propagation of change**. With this paradigm, it's possible to express static (e.g., arrays) or dynamic (e.g., event emitters) data streams with ease, and also communicate that an inferred dependency within the associated execution model exists, which facilitates the automatic propagation of the changed data flow.

  关键字：

  - 数据流（data streams ）
  - 传播变化（ propagation of change）

  侧重点：

  - 数据结构
  - 数组（arrays）
  - 事件发射器（event emitters）
  - 数据变化

  技术连接：

  - 数据流：Java 8 `Stream`

  - 传播变化：Java `Observable` / `Observer`

  - 事件：Java `EventObject` / `EventListener`

    > 规范（规定）：与事件相关的都要实现 EventObject，监听器相关的要实现 EventListenr。比如 Spring 中的 `ApplicationEvent` 和 `ApplicationListener`

- Spring Framework

  [Web on Reactive Stack](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#spring-webflux)

  > The term, “reactive,” refers to programming models that are built around reacting to change — network components reacting to I/O events, UI controllers reacting to mouse events, and others. In that sense, non-blocking is reactive, because, instead of being blocked, we are now in the mode of reacting to notifications as operations complete or data becomes available.

  关键字：

  - 变化响应（reacting to change ）
  - 非阻塞（non-blocking）

  侧重点：

  - 响应通知
  - 操作完成（operations complete）
  - 数据可用（data becomes available）

  技术连接：

  - 非阻塞：Servlet 3.1 `ReadListener` / `WriteListener`
  - 响应通知：Servlet 3.0 `AsyncListener`

- [ReactiveX](https://reactivex.io/intro.html)

  > It extends the observer pattern to support sequences of data and/or events and adds operators that allow you to compose sequences together declaratively while abstracting away concerns about things like low-level threading, synchronization, thread-safety, concurrent data structures, and non-blocking I/O.

  关键词：

  - 观察者模式（Observer pattern ）
  - 数据/事件序列（Sequences of data and/or events )
  - 序列操作符（Opeators）
  - 屏蔽并发细节（abstracting away…）

  侧重点：

  - 设计模式
  - 数据结构
  - 数据操作
  - 并发模型

  技术连接：

  - 观察者模式：Java `Observable` / `Observer`
  - 数据/事件序列：Java 8 Stream
  - 数据操作：Java 8 Stream
  - 屏蔽并发细节（abstracting away…）： `Exectuor` 、 `Future` 、 `Runnable`

- [Reactor](https://projectreactor.io/docs/core/release/reference/#intro-reactive)

  > The reactive programming paradigm is often presented in object-oriented languages as an extension of the Observer design pattern. You can also compare the main reactive streams pattern with the familiar Iterator design pattern, as there is a duality to the Iterable-Iterator pair in all of these libraries. One major difference is that, while an Iterator is pull-based, reactive streams are push-based.

  关键词：

  - 观察者模式（Observer pattern ）
  - 响应流模式（Reactive streams pattern ）
  - 迭代器模式（Iterator pattern）
  - 拉模式（pull-based）
  - 推模式（push-based）

  侧重点：

  - 设计模式（Reactive 是观察者模式的扩展）
  - 数据获取方式（Reactive Stream 数据流和 Iterator 分别是推和拉的方式）

  技术连接：

  - 观察者模式：Java `Observable` / `Observer`
  - 响应流模式：Java 8 `Stream`
  - 迭代器模式：Java 8 `Iterator`

- @andrestaltz

  > **Reactive programming is programming with asynchronous data streams.**
  >
  > In a way, this isn't anything new. Event buses or your typical click events are really an asynchronous event stream, on which you can observe and do some side effects. Reactive is that idea on steroids. You are able to create data streams of anything, not just from click and hover events. Streams are cheap and ubiquitous, anything can be a stream: variables, user inputs, properties, caches, data structures, etc. For example, imagine your Twitter feed would be a data stream in the same fashion that click events are. You can listen to that stream and react accordingly.

  关键词：

  - 异步（asynchronous ）
  - 数据流（data streams）
  - 并非新鲜事物（not anything new）
  - 过于理想化（idea on steroids）

  侧重点：

  - 并发模型
  - 数据结构
  - 技术本质

  技术连接：

  - 异步：Java `Future`
  - 数据流：Java 8 `Stream`

  > 但我觉得 Reactive 也并非是异步的，但是观察者（事件监听）模式的实现本质还是同步的。JDK 中的 `Observable` / `Observer` 就是同步的典型代表，比如他在通知的时候 Observable#notifyObservers 方法里面就是同步的去传输，只不过接收方 Observer#update 方法是不断的能接收到回调。所以我觉得这个非阻塞并不完全指的是并发模型，主要还是指的是编程模型。不过 Reactive 大多数还是属于异步的，但同步还是存在
  >
  > 技术需要 **” 兼听则明偏信则暗 “**，需要不断的对比

### Reactive Programming 特性

- [编程模型（Programming Models）](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-programming-models)

  - 响应式编程
  - 函数式编程

  对立模型 - [Imperative programming - Wikipedia](https://en.wikipedia.org/wiki/Imperative_programming)

  > imperative programming is a programming paradigm of software that uses statements that change a program's state. In much the same way that the imperative mood in natural languages expresses commands, an imperative program consists of commands for the computer to perform. Imperative programming focuses on describing how a program operates step by step, rather than on high-level descriptions of its expected results.

  小结：

  Reactive Programming：同步或异步非阻塞执行，数据传播被动通知

  Imperative programming：同步阻塞执行，数据主动获取

- 设计模式（Design Patterns）

  - 扩展模式：观察者（[Observer](https://en.wikipedia.org/wiki/Observer_pattern)） - 推模式（push-based）

    > 单单是观察者模式是不满足 Reactive 的，它只能满足非阻塞，用的是异步的方式来进行回调，而不是阻塞下面即将要执行的代码

  - 混合模式：反应器（[Reactor](https://en.wikipedia.org/wiki/Reactor_pattern)）（同步非阻塞）、[Proactor](https://en.wikipedia.org/wiki/Proactor_pattern)（异步非阻塞），共同点都是非阻塞

  - 对立模式：迭代器（[Iterator](https://en.wikipedia.org/wiki/Iterator_pattern)） - 拉模式（pull-based）

  模式对比

  An Observable（RxJava) is the asynchronous/push “[dual](http://en.wikipedia.org/wiki/Dual_(category_theory))” to the synchronous/pull Iterable  

  | event          | Iterable (pull)  | Observable (push)  |
  | -------------- | ---------------- | ------------------ |
  | data           | T next()         | onNext(T)          |
  | discover error | throws Exception | onError(Exception) |
  | complete       | !hasNext()       | onCompleted()      |

  小结：Reactive Programming 作为观察者模式（Observer） 的延伸，在处理流式数据的过程中，并非使用传统的命令编程方式（ Imperative programming）同步拉取数据，如迭代器模式（Iterator） ，而是采用**同步或异步非阻塞地推和拉**（观察者+迭代器）相结合的方式，响应数据传播时的变化

  > Reactive 模式 = 同步/异步非阻塞 + 推和拉两种模型

- 数据结构（Data Structure）

  - 流式（Streams）
  - 序列（Sequences）
  - 事件（Events）

  [小结](https://gist.github.com/staltz/868e7e9bc2a7b8c1f754#what-is-reactive-programming)：A stream is a sequence of **ongoing events ordered in time**.

- 并发模式（Concurrency Model）

  - 非阻塞（Non-Blocking）
    - 同步（Synchronous）
    - 异步（Asynchronous）

  小结：屏蔽并发编程细节，如线程、同步、线程安全以及并发数据结构。（如：Tread.start()、Lock 的 lock()/unlock()、可见性，原子性，有序性、CopyOnWriteArrayList）

  > 想要同步异步自己切换，并且有顺序（一条流水线，加载顺序不能乱），在前面的 `CompletableFuture` 链式调用的时候，保证了 loadConfigurations()、loadUsers()、loadOrders() 的顺序执行。而线程池的线程执行是由 JVM 或操作系统来调度的，具体什么时候执行我们是不知道。那我们就需要一种数据结构或是编程模型来保证顺序，并且非阻塞（可认为是 callback 回调），2. 需要它是异步或同步交由开发人员来选择，而不是实现更复杂的线程、同步、线程安全等一系列事项









> - Reactor 反应堆模式：同步非阻塞
>
> - Proactor 模式：异步非阻塞
>
> - 观察者模式（Observer）：推的模式，从服务器推往客户端（事件/监听者模式，相当于监听事件）
>
> - 迭代器模式（Iterator）：拉的模式，从客户端向服务器拉（像比如 List、Array 都有迭代器，可以去循环获取）
>
>   Webflux 底层就是 reactive，是同步+异步+设计模式的综合体

### Reactive Programming 使用场景

[Reactive Streams JVM](https://github.com/reactive-streams/reactive-streams-jvm)

> The main goal of Reactive Streams is to govern the exchange of stream data across an asynchronous boundary

主要目的：

- 管理流式数据交换（ govern the exchange of stream data）
- 异步边界（asynchronous boundary）

[Spring Framework](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-performance)

> Reactive and non-blocking generally **do not make applications run faster**. They can, in some cases, (for example, if using the WebClient to run remote calls in parallel). On the whole, it requires more work to do things the non-blocking way and that can slightly increase the required processing time.
>
> The key expected benefit of reactive and non-blocking is the ability to scale with a small, fixed number of threads and less memory. That makes applications more resilient under load, because they scale in a more predictable way.

主要目的：

- 通常并非让应用运行更快速（generally do not make applications run faster）
- 利用较少的资源提升伸缩性（scale with a small, fixed number of threads and less memory）

![Reactive 吞吐量](http://cdn.liumulin.top/Reactive%20%E5%90%9E%E5%90%90%E9%87%8F.jpg)

> 有 5 个 Client 分别进行 RPC 远程调用，每个消耗 100ms。而 Worker 线程池中开辟 5 个线程，每个线程占用 1m 空间，共占用 5m。假设是 8 核 CPU，那么并行的话就一下子占了 5 个核。
>
> 如果此时转给只开辟 3 个核的 Reactive 线程池来管理，那么内存占用从 5m 降为了 3m，核数由占用 5 个核降为了 3 个核。
>
> 并行的时候，5 个线程执行 5 个任务，100ms 就执行完了。转为 Reactive 线程池时，时间就为 100ms+100ms（第一次的 100ms 执行完才能够释放）
>
> 假如说有 10 个客户端发送请求，并行就 2 次 200ms 完成。而到 Reactive 线程池里就要花 4 次 400ms 完成，而且最后一次的还容易超时，这种长时间运行的任务就适合放到消息队列里面，不设置超时时间，让消息积压，等待 Reactive 线程池里处理完了消息才处理
>
> 所以 Webflux 并不适合 Web 的应用，它可以提升吞吐量，但它的 RT 反而会变得更长

如果说 Webflux 比 Tomcat 性能好的话，应该说是 Netty WebServer 比 Tomcat 性能好。但实际上他们性能都差不多。主要是因为 Tomcat 默认只有 200 个线程池，而 Netty 可以随便设置 N 个线程池。越大的话性能越好，相等时它两差不多。

跨异步边界 = 线程切换

[ReactiveX]

> The ReactiveX Observable model allows you to treat streams of asynchronous events with the
> same sort of simple, composable operations that you use for collections of data items like
> arrays. It frees you from tangled webs of callbacks, and thereby makes your code more readable and less prone to bugs.  

主要目的：

- 更好可读性（more readable）
- 减少 bugs（less prone to bugs）

核心技术：

- 异步（asynchronous）
- 同顺序（same sort）
- 组合操作（composable operations）

Java 原生技术限制：Stream 存在组合限制（两个流合并在一起）

[Reactor]

> Composability and readability
>
> Data as a flow manipulated with a rich vocabulary of operators
>
> Nothing happens until you subscribe
>
> Backpressure or the ability for the consumer to signal the producer that the rate of emission is
> too high
>
> High level but high value abstraction that is concurrency-agnostic

主要目的：

- 结构性和可读性（Composability and readability）
- 高层次并发抽象（High level abstraction）

核心技术：

- 丰富的数据操作符（ rich vocabulary of operators）
- 背压（Backpressure）
- 订阅式数据消费（Nothing happens until you subscribe）

Java 原生技术限制：

- Stream 有限操作符
- Stream 不支持背压
- Stream 不支持订阅（Stream 是迭代器模式，也就是 pull 拉的模式，所以不支持订阅）

**总结 Reactive Programming：**

Reactive Programming 作为观察者模式（Observer） 的延伸，不同于传统的命令编程方式（Imperative programming）同步拉取数据的方式，如迭代器模式（Iterator） 。而是采用数据发布者同步或异步地推送到数据流（Data Streams）的方案。当该数据流（Data Steams）订阅者监听到传播变化时，立即作出响应动作。在实现层面上，Reactive Programming 可结合函数式编程简化面向对象语言语法的臃肿性，屏蔽并发实现的复杂细节，提供数据流的有序操作，从而达到提升代码的可读性，以及减少 Bugs 出现的目的。同时，Reactive Programming 结合背压（Backpressure）的技术解决发布端生成数据的速率高于订阅端消费的问题。  

## Reactive Streams 规范

### API 组件

Publisher：数据发布者，数据上游

Subscriber：数据订阅者，数据下游

信号事件

- onSubscribe：当下游订阅时
- onNext：当下游接收数据时
- onComplete：当数据流（Data Streams）执行完成时
- onError： 当数据流（Data Streams）执行错误时

Subscription：订阅信号控制

信号操作

- request：请求上游元素的数量
- cancel：请求停止发送数据并且清除资源

Processor：消息发布者和订阅者综合体

### 背压（Backpressure）

[维基百科](https://en.wikipedia.org/wiki/Back_pressure)

> The term is also used analogously in the field of information technology to describe the buildup of data behind an I/O switch if the buffers are full and incapable of receiving any more data;the transmitting device halts the sending of data packets until the buffers have been emptied and are once more capable of storing information. It also refers to an algorithm for routing data according to congestion gradients (see backpressure routing).

关键字：

- I/O 切换（I/O switch ）
- 缓冲填满（the buffers are full ）
- 数据无法接受（incapable of receiving any more data）
- 传输设备（transmitting device ）
- 停止发送数据包（halts the sending of data packets ）

[Reactive Streams JVM](https://github.com/reactive-streams/reactive-streams-jvm)

> Backpressure is an integral part of this model in order to allow the queues which mediate
> between threads to be bounded.
>
> Since back-pressure is mandatory the use of unbounded buffers can be avoided. In general, the only time when a queue might grow without bounds is when the publisher side maintains a higher rate than the subscriber for an extended period of time, but this scenario is handled by backpressure instead.  

关键字：

- 线程和边界间调停（mediate between threads to be bounded）
- 发布者维持速率高于订阅者（publisher side maintains a higher rate than the subscriber）
- 背压处理（handled by backpressure）

Reactor

> Propagating signals upstream is also used to implement **backpressure**, which we described in
> the assembly line analogy as a feedback signal sent up the line when a workstation processes
> more slowly than an upstream workstation.
>
> The real mechanism defined by the Reactive Streams specification is pretty close to the analogy: a subscriber can work in *unbounded* mode and let the source push all the data at its
> fastest achievable rate or it can use the `request` mechanism to signal the source that it is
> ready to process at most `n` elements.

关键字：

- Propagating signals upstream（传播上游信号）
- 无边界模式（*unbounded* mode）
- 处理最大元素数量（process at most n elements）

**总结背压**

假设下游 Subscriber 工作在无边界大小的数据流水线时，当上游 Publisher 提供数据的速率快于下游 Subscriber 的消费数据速率时，下游 Subscriber 将通过传播信号（request）到上游 Publisher，请求限制数据的数量（ Demand ）或通知上游停止数据生产。

> 当消费者消费不过来，或者消费端 CPU 过高了，或者内存消耗过大。此时可以选择终止生产数据结束流处理，或者限定一个数量，只消费这么多

## Reactor 框架运用

### 核心 API

**`Mono`**

定义：0-1 的非阻塞结果

实现：Reactive Streams JVM API `Publisher`

类比：非阻塞 `Optional`

类似模式：点对点模式

![image-20220716074605657](http://cdn.liumulin.top/image-20220716074605657.png)

图解

![Mono](https://projectreactor.io/docs/core/release/reference/images/mono.svg)

> 红色叉叉那里相当于阻塞住了（被终止了），一般是 CPU 过高进行背压操作，或者数据库连接异常，IO 异常等情况

**`Flux`**

定义：0-N 的非阻塞序列

实现：Reactive Streams JVM API `Publisher`

类比：非阻塞 `Stream`

类似模式：发布 / 订阅者模式

图解

![Flux](http://cdn.liumulin.top/flux.svg)

**`Scheduler`**

定义：Reactor 调度线程池

- 当前线程： Schedulers.immediate()
  - 等价关系：Thread.currentThread()
- 单复用线程： Schedulers.single()
  - 内部名称："single"
  - 线程名称："single"
  - 线程数量：单个
  - 线程 idel 时间：Long Live
  - 底层实现：ScheduledThreadPoolExecutor (core 1)
- 弹性线程池： Schedulers.elastic()
  - 内部名称："elastic"
  - 线程名称："elastic-evictor-{num}"
  - 线程数量：无限制（unbounded）
  - 线程 idel 时间：60 秒
  - 底层实现：ScheduledThreadPoolExecutor
- 并行线程池： Schedulers.parallel()
  - 内部名称："parallel"
  - 线程名称："parallel-{num}"
  - 线程数量：处理器数量
  - 线程 idel 时间：60 秒
  - 底层实现：ScheduledThreadPoolExecutor

> 同一个处理器上面多个线程时并发；不同处理器上面不同线程时并行

### 实战

**Maven 依赖**

```java
<dependency>
	<groupId>io.projectreactor</groupId>
	<artifactId>reactor-core</artifactId>
</dependency>
```

**同步处理**

Flux：打印的全是 main 线程，所以应该称之为非阻塞序列，而非 “异步” 序列

```java
public class FluxDemo {

    public static void main(String[] args) {
        Flux.just("A", "B", "C")
                .subscribe(FluxDemo::println);
    }

    public static void println(Object object) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[线程：" + threadName + "]:" + object);
    }

}
// [线程：main]:A
// [线程：main]:B
// [线程：main]:C
```

```java
public class FluxDemo {

    public static void main(String[] args) throws InterruptedException {
        println("run...");
        Flux.just("A", "B", "C") // 发布 A->B->C
                .publishOn(Schedulers.boundedElastic()) // 线程池切换
                .map(value -> "+" + value) // "A" -> "+A"
                .subscribe(
                        FluxDemo::println, // 数据消费
                        FluxDemo::println, // 异常处理
                        () -> println("完成操作！") // 完成操作
                );

        Thread.sleep(1000);
    }

    public static void println(Object object) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[线程：" + threadName + "]:" + object);
    }

}
```

> 为什么没有执行 map 转换操作和最后一步打印 “完成操作！”。需要主线程睡一秒
>
> 异步编程、并发编程带来的问题：最后一步是随着主线程的退出而退出，增加一定的等待时间就 OK
>
> 在注释掉 `.publishOn(Schedulers.boundedElastic())` 以后，切换为单线程执行就没有问题，直接打印 map 操作和 “完成操作！”语句

```java
public class FluxDemo {

    public static void main(String[] args) throws InterruptedException {
        println("run...");
        Flux.just("A", "B", "C") // 发布 A->B->C
                .publishOn(Schedulers.boundedElastic()) // 线程池切换
                .map(value -> "+" + value) // "A" -> "+A"
                .subscribe(
                        FluxDemo::println, // 数据消费 = onNext(T)
                        FluxDemo::println, // 异常处理 = onError(Throwable)
                        () -> println("完成操作！"), // 完成操作 = onComplete()
                        subscription -> subscription.request(1) // 背压操作 onSubscribe(Subscription)
                );

        Thread.sleep(1000);
    }

    public static void println(Object object) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[线程：" + threadName + "]:" + object);
    }

}
```

标准的 Reactive Stream API 调用结果

```java
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
```

> Netty 在长连接的情况下才有优势，否则和 Tomcat 差不多
>
> Netty 的事件循环在短连接里面作用不是很大
>
> Spring Cloud Hystrix 用的是 RxJava；Webflux 用的是 Reactor

总结思考：Reactive 过于理想化了，是旧的东西重新包装了一下。目的花更小的代价来做更多的事情，利用更小的 CPU 资源去抗住更大的吞吐量（缺点：接收的处理任务的次数变多，请求的次数变长）

> maven 配置 `<relativePath>../</relativePath>` 可以减少寻址的依赖















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



