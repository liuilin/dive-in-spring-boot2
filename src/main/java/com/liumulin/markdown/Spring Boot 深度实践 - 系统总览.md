# Spring Boot 深度实践 - 系统总览

> 技术选型。为什么选择 Spring Boot？
>
> 1. 市场占有率高
>
> 2. 拥有优秀的家族基因
>
>    我们平时说的 Spring 呢，通常指的是 Spring 的技术体系，`Spring Framework` `Spring Boot` `Spring Cloud` ，从功能上来说呢，`Spring Boot` 继承了 Spring 的所有特性，同时它又背靠着 `Spring Data` `Spring Security` 等先进技术，在 Spring 家族中，起到了一个承上启下的作用。同时它为底层的 `Spring Framework` 快速的搭建 Spring 应用，为高层的 `Spring Cloud` 提供基础设施，所以 Spring 官方都使用的是 **BUILD ANYTHING** 来体现它的家族地位
>
> 3. 与时俱进的技术信仰
>
>    `Spring Boot 1.0` 时代是完全构建在 Java EE 技术体系上的，包括我们常见的 `Spring MVC` 它是属于 Servlet 技术栈，在数据存储层面我们可以使用 JDBC、JPA 这样的规范来进行操作（1.0 时代是无法逃脱 Java EE 的束缚的）
>
>    `Spring Boot 2.0` 时代呢，把 Java EE 以前的必选项调整为了可选项，这个我认为是 Sun 公司轰然倒塌的原因，而且 Oracle 对 Java 的热情是骤减的，实践发展举步维艰。
>
>    反观 Spring 的阵营呢，技术可以说是蒸蒸日上，它以广阔的胸襟去拥抱业界的变化，最为显著的变化可以说是编程模型的变化。以 Reactive 为代表的编程范式，使用 **异步+非阻塞** 的方式帮助程序充分的利用系统资源，`Spring Framework` 提供了一套完整的 Reactive 技术栈，其中包括 Web 层的 WebFlux，它就是一种非阻塞的 Web 框架。同时在数据存储方面呢 Spring 已经提升到了相关的核心基础设施，包括 Mongo，Redis 等。最终使用 Reactive 来实现异步+非阻塞的编程方式，这个就是目前的技术趋势。而 `Spring 5` 或者说是 `Spring Boot 2.0` 在提早的布局，等待趋势变为现实

问题？

- Spring Boot 是如何基于 Spring Framework 逐步走向自动装配？
- SpringApplication 是怎样掌控 Spring 应用生命周期？
- Spring Boot 外部化配置与 Spring Environment 抽象之间是什么关系？
- Spring Web MVC 向 Spring Reactive WebFlux 过渡的真实价值和意义？

**Spring Boot 易学**

1. 组件自动装配：约定大于配置，专注于核心业务代码

   装配是一件特别麻烦的事，包括 Java 代码

2. 外部化配置：一次构建、按需调配，到处运行

   当应用一次性构建成 war 包或 jar 包时，根据当时的环境需要，比如要调整 Web 端口，实现在不同环境下到处运行的目的

3. 嵌入式容器：内置容器、无需部署、独立运行

   除了传统的 Servlet 容器外，它还可以实现 Netty 容器（也就是标准的 WebFlux  - Web 容器），达到了我们无需把应用部署到传统的 Servlet 环境，从而实现独立运行的目的

4. Spring Boot Starter：简单依赖、按需装配，自我包含

   Starter 可以理解为模块化的自包含的执行单元，可以减少我们的配置、减少我们的依赖。按需装配（需要通过一定条件触发）

5. Production-Ready：一站式运维、生态无缝整合

**Spring Boot 难精**

1. 组件自动装配：模式注解、@Enable 模块、条件装配、加载机制
2. 外部化配置：Environment 抽象、生命周期、破坏性变更
3. 嵌入式容器：Servlet Web 容器、Reactive Web 容器
4. Spring Boot Starter：依赖管理、装配条件、装配顺序
5. Production-Ready：健康检查、数据指标、@Endpoint 管控

## 三大核心特性

### 1. 组件自动装配

组件自动装配：Web MVC、Web Flux、JDBC

> Spring Boot 将 Spring Framework 手动的方式转为了自动的方式，这可以帮助我们减少许多代码的编写，它是有一点条件方式的触发，也是帮助我们去理解相应的特性，更关注我们业务的开发，提高我们的开发效率

激活：`@EnableAutoConfiguration`

配置：`/META-INF/spring.factories`

实现：`xxxAutoConfiguration`

https://docs.spring.io/spring-boot/docs/2.0.1.RELEASE/reference/htmlsingle/#boot-features-spring-mvc-auto-configuration

Web MVC 依赖

```java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### 2. 嵌入式 Web 容器

- Web Servlet：Tomcat、Jetty 以及 Undertow

- Web Reactive：Netty Web Server

  Spring Boot 2.0 可以自由的去切换 Web 容器

### 3. 生产准备特性

- 指标（Metrics）：/actuator/metrics

  > metrics 信息可以是 CPU、内存、磁盘等利用率的信息

- 健康检查（Health Check）：/actuator/

  > 查看应用、数据库、磁盘等是否健康

- 外部化配置（Externalized Configuration）

  > 不用写代码的方式调整应用的行为，比如 Web 端口，可以通过 -Dserver.port=8090（或 properties、yml） 来做相应的调整，相应的应用服务器的端口就会发生相应的变化。而要是以前可能需要配置一个 XML 的方式或者写代码的方式来操作

## Web 应用

### 传统 Servlet 应用

**依赖**

```java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**Servlet 组件**

- Servlet
  - 实现
    - `@WebServlet`
    - HttpServlet
    - 注册
  - URL 映射
    - `@WebServlet(urlPatterns = "/my/servlet")`
  - 注册
    - `@ServletComponentScan("com.liumulin.controller")`
- Filter
- Listener

> Filter 与 Listener 的区别

**Servlet 注册**

Servlet 注解

- `@ServletComponentScan`
  - `@WebServlet`
  - `@WebFilter`
  - `@WebListener`

Spring Bean

- `@Bean`
  - Servlet
  - Filter
  - Listener

RegistrationBean

- ServletRegistrationBean
- FilterRegistrationBean
- ServletListenerRegistrationBean

**异步非阻塞**

异步 Servlet

- `javax.servlet.ServletRequest#startAsync()  `
- `javax.servlet.AsyncContext  `

非阻塞 Servlet

- `javax.servlet.ServletInputStream#setReadListener  `
  - `javax.servlet.ReadListener  `
- `javax.servlet.ServletOutputStream#setWriteListener  `
  - `javax.servlet.WriteListener  `

### Spring Web MVC 应用

**Web MVC 视图**

- `ViewResolver`
- `View`

模板引擎

- Thymeleaf
- Freemarker
- JSP

内容协商

- ContentNegotiationConfigurer
- ContentNegotiationStrategy
- ContentNegotiatingViewResolver

异常处理

- `@ExceptionHandler`
- HandlerExceptionResolver
  - ExceptionHandlerExceptionResolver

- BasicErrorController (Spring Boot)  

**Web MVC REST**

资源服务

- `@RequestMapping`
  - `@GetMapping`
  - `@PostMapping`
  - ...
- `@ResponseBody`
- `@RequestBody`

资源跨域

- CrossOrigin
- WebMvcConfigurer#addCorsMappings

> 传统解决方案
>
> - IFrame
> - JSONP

服务发现

- HATEOS

**Web MVC 核心**

- `DispatcherServlet`
- `HandlerMapping`
- `HandlerAdapter`
- `ViewResolver`
- ...

### Spring Web Flux 应用

**Reactor 基础**

Java Lambda

Mono

Flux

**Web Flux 核心**

Web MVC 注解兼容

- `@Controller`
- `@RequestMapping`
- `@ResponseBody`
- `@RequestBody`
- ...

函数式申明

- `RouterFunction`

异步非阻塞

- Servlet 3.1+
- Netty Reator

使用

### Web Server 应用

**切换 Web Server**

切换其他 Servlet 容器

- Tomcat -> Jetty

  ```java
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
      <exclusions>
          <!-- Exclude the Tomcat dependency -->
          <exclusion>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-tomcat</artifactId>
          </exclusion>
      </exclusions>
  </dependency>
  <!-- Use Jetty instead -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-jetty</artifactId>
  </dependency>
  ```

替换 Servlet 容器

- WebFlux

  ```java
  <!--<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
      <exclusions>
          &lt;!&ndash; Exclude the Tomcat dependency &ndash;&gt;
          <exclusion>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-tomcat</artifactId>
          </exclusion>
      </exclusions>
  </dependency>
  &lt;!&ndash; Use Jetty instead &ndash;&gt;
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-jetty</artifactId>
  </dependency>-->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webflux</artifactId>
  </dependency>
  ```

**自定义 Servlet Web Server**

- `WebServerFactoryCustomizer`

**自定义 Reactive Web Server**

- `ReactiveWebServerFactoryCustomizer`























