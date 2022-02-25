# 一、Spring Boot 深度实践 - 系统总览

> 技术选型。为什么选择 Spring Boot？
>
> 1. 市场占有率高
>
> 2. 拥有优秀的家族基因
>
>    我们平时说的 Spring 呢，通常指的是 Spring 的技术体系 `Spring Framework`、`Spring Boot` 以及 `Spring Cloud` ，从功能上来讲呢，`Spring Boot` 继承了 Spring 的所有特性，而它又同时背靠 `Spring Data`、`Spring Security` 等先进技术，在 Spring 家族中，起到了一个承上启下的作用。同时它为底层的 `Spring Framework` 快速的搭建 Spring 应用，为高层的 `Spring Cloud` 提供基础设施，所以 Spring 官网都使用的是 **BUILD ANYTHING** 来体现它的家族地位
>
> 3. 与时俱进的技术信仰
>
>    `Spring Boot 1.0` 时代是完全构建在 Java EE 技术体系上的，包括我们常见的 `Spring MVC` 它是属于 Servlet 技术栈，在数据存储层面我们可以使用 JDBC、JPA 这样的规范来进行操作（1.0 时代是无法逃脱 Java EE 的束缚的）
>
>    `Spring Boot 2.0` 时代呢，把 Java EE 以前的必选项调整为了可选项，这个我认为是 Sun 公司轰然倒塌的原因，而且 Oracle 对 Java 的热情是骤减的，实践发展举步维艰。
>
>    反观 Spring 的阵营，技术可以说是蒸蒸日上，它以广阔的胸襟去拥抱业界的变化，最为显著的变化可以说是编程模型的变化。以 Reactive 为代表的编程范式，使用 **异步+非阻塞** 的方式帮助程序充分的利用系统资源，`Spring Framework` 提供了一套完整的 Reactive 技术栈，其中包括 Web 层的 WebFlux，它就是一种非阻塞的 Web 框架。同时在数据存储方面呢 Spring 已经提升到了相关的核心基础设施，包括 Mongo，Redis 等。最终使用 Reactive 来实现异步+非阻塞的编程方式，这就是目前的技术趋势。而 `Spring 5` 或者说是 `Spring Boot 2.0` 在提早的布局，等待趋势变为现实

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

> Spring Boot 将 Spring Framework 手动装配转为了自动装配，这可以帮助我们减少许多代码的编写，它是有一点条件方式的触发，也是帮助我们去理解相应的特性。让我们更关注于业务的开发，并提高开发效率

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

- 健康检查（Health Check）：/actuator/health

  > 查看应用、数据库、磁盘等是否健康

- 外部化配置（Externalized Configuration）：/actuator/configprops

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
    - `@ServletComponentScan("com.liumulin.web.servlet")`
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

```java
@WebServlet(urlPatterns = "/my/servlet", asyncSupported = true)
...
AsyncContext asyncContext = request.startAsync();
asyncContext.start(() -> {
    try {
        response.getWriter().write("Hello World");
        // 触发完成
        asyncContext.complete();
    } catch (IOException e) {
        e.printStackTrace();
    }
});
```



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

兼容 Web MVC 注解

- `@Controller`
- `@RequestMapping`
- `@ResponseBody`
- `@RequestBody`
- ...

函数式申明

- `RouterFunction`

> 函数式申明有什么好处：1. 函数式编程可以更丰富弹性，比如说你可以通过很多条件来做，尽管这个可以通过 Condition 来做（也就是条件方面来做一个判断），但是呢函数的灵活性就远远大于其它的方式；2. 可以绑定事件方法，就是说我可以在一个 Spring 表达式里面部署多个 Endpoint，以及映射方法的处理机制，我不需要导出相应的去绑定	

异步非阻塞

- Servlet 3.1+
- Netty Reator

> WebFlux 有一定的优劣势，它能提供系统的吞吐量，但提升系统吞吐量并不代表它快

使用场景

> 性能测试：https://blog.ippon.tech/spring-5-webflux-performance-tests/

### Web Server 应用

**切换 Web Server**

切换其他 Servlet 容器

- Tomcat -> Jetty

  ```java
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
      <exclusions>
          <!-- Tomcat 运行优先级高于 jetty，所以需要排除 -->
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
          <!-- Tomcat 运行优先级高于 jetty，所以需要排除 -->
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
  <!--WebFlux 容器优先级低于了传统 Web 容器，所以要把其它容器全部注释掉-->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webflux</artifactId>
  </dependency>
  ```

**自定义 Servlet Web Server**

- `WebServerFactoryCustomizer`

**自定义 Reactive Web Server**

- `ReactiveWebServerFactoryCustomizer`

# 二、走向自动装配

## Spring 模式注解装配

模式注解（Stereotype Annotations）

> A stereotype annotation is an annotation that is used to declare the role that a component plays within the application. For example, the @Repository annotation in the Spring Framework is a marker for any class that fulfills the role or *stereotype* of a repository (also known as Data Access Object or DAO).
>
> @Component is a generic stereotype for any Spring-managed component. Any component annotated with @Component is a candidate for component scanning. Similarly, any component annotated with an annotation that is itself meta-annotated with @Component is also a candidate for component scanning. For example, @Service is meta-annotated with @Component .  

模式注解是一种用于声明在应用中扮演“组件”角色的注解。如 Spring Framework 中的 @Repository 标注在任何类上 ，用于扮演仓储角色的模式注解。

@Component 作为一种由 Spring 容器托管的通用模式组件，任何被 @Component 标准的组件均为组件扫描的候选对象。类似地，凡是被 @Component 元标注了（**meta-annotated**）的注解，如 @Service ，当任何组件标注它时，也会被视作组件扫描的候选对象  

**模式注解举例**

| Spring Framework 注解 | 场景说明           | 起始版本 |
| --------------------- | ------------------ | -------- |
| @Repository           | 数据仓储模式注解   | 2.0      |
| @Component            | 通用组件模式注解   | 2.5      |
| @Service              | 服务模式注解       | 2.5      |
| @Controller           | Web 控制器模式注解 | 2.5      |
| @Configuration        | 配置类模式注解     | 3.0      |

**装配方式**

`<context:component-scan>` 方式

```java
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
http://www.springframework.org/schema/beans/spring-beans.xsd
http://www.springframework.org/schema/context http://www.springframework.org/schema/context/springcontext.xsd">
    <!-- 激活注解驱动特性 -->
    <context:annotation-config/>
    <!-- 找寻被 @Component 或者其派生 Annotation 标记的类（Class），将它们注册为 Spring Bean -->
    <context:component-scan base-package="com.liumulin.web.servlet"/>
</beans>
```

`@ComponentScan` 方式

```java
@ComponentScan(basePackages = "com.imooc.dive.in.spring.boot")
public class SpringConfiguration {
	...
}
```

**自定义注解模式**

`@Component` "派生性"

```java
/**
 * 一级 {@link Repository @Repository}
 *
 * @author liuqiang
 * @since 2022-02-18
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repository
public @interface FirstLevelRepository {
    String value() default "";
}
```

- `@Component`
  - `@Repository`
    - `@FirstLevelRepository`

`@Component` ”层次性“

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@FirstLevelRepository
public @interface SecondLevelRepository {
    String value() default "";
}

```

- `@Component`
  - `@Repository`
    - `@FirstLevelRepository`
      - `@SecondLevelRepository`

> ```java
> //@FirstLevelRepository(value = "firstLevelRepository")
> @SecondLevelRepository(value = "firstLevelRepository")
> public class MyFirstLevelRepository {
> }
> ```
>
> ```java
> @ComponentScan(basePackages = "com.liumulin.repository")
> public class RepositoryBootstrap {
>     public static void main(String[] args) {
>         ConfigurableApplicationContext context = new SpringApplicationBuilder(RepositoryBootstrap.class)
>                 .web(WebApplicationType.NONE)
>                 .run(args);
>         // 验证 Bean 是否存在
>         MyFirstLevelRepository firstLevelRepository = context.getBean("firstLevelRepository", MyFirstLevelRepository.class);
>         System.out.println("firstLevelRepository = " + firstLevelRepository);
>         // 关闭容器
>         context.close();
>     }
> }
> ```
>
> 



ImportSelector（3.1） 比 `Configuration` （3.0）方式更加灵活，因为里面可以添加一些分支、条件判断啥的

## Spring @Enable 模块装配

Spring Framework 3.1 开始支持 ”@Enable 模块驱动 “。所谓 “模块” 是指具备相同领域的功能组件集合， 组合所形成一个独立
的单元。比如 Web MVC 模块、AspectJ 代理模块、Caching（缓存）模块、JMX（Java 管 理扩展）模块、Async（异步处
理）模块等。

### `@Enable` 注解模块举例

| 框架实现            | @Enable 注解模块               | 激活模块            | 激活模块 |
| ------------------- | ------------------------------ | ------------------- | -------- |
| Spring    Framework | @EnableWebMvc                  | Web MVC 模块        |          |
|                     | @EnableTransactionManagement   | 事务管理模块        |          |
|                     | @EnableCaching                 | Caching 模块        |          |
|                     | @EnableMBeanExport             | JMX 模块            |          |
|                     | @EnableAsync                   | 异步处理模块        |          |
|                     | @EnableWebFlux                 | Web Flux 模块       |          |
|                     | @EnableAspectJAutoProxy        | AspectJ 代理模块    |          |
| Spring Boot         | @EnableAutoConfiguration       | 自动装配模块        |          |
|                     | @EnableManagementContext       | Actuator 管理模块   |          |
|                     | @EnableConfigurationProperties | 配置属性绑定模块    |          |
|                     | @EnableOAuth2Sso               | OAuth2 单点登录模块 |          |
| Spring Cloud        | @EnableEurekaServer            | Eureka服务器模块    |          |
|                     | @EnableConfigServer            | 配置服务器模块      |          |
|                     | @EnableFeignClients            | Feign 客户端模块    |          |
|                     | @EnableZuulProxy               | 服务网关 Zuul 模块  |          |
|                     | @EnableCircuitBreaker          | 服务熔断模块        |          |

### 实现方式

注解驱动方式

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({DelegatingWebMvcConfiguration.class})
public @interface EnableWebMvc {
}
```

```java
@Configuration(
    proxyBeanMethods = false
)
public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {
    ...
}
```

接口编程方式

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CachingConfigurationSelector.class})
public @interface EnableCaching {
    boolean proxyTargetClass() default false;

    AdviceMode mode() default AdviceMode.PROXY;

    int order() default 2147483647;
}
```

```java
public class CachingConfigurationSelector extends AdviceModeImportSelector<EnableCaching> {
    public String[] selectImports(AdviceMode adviceMode) {
        switch(adviceMode) {
        case PROXY:
            return this.getProxyImports();
        case ASPECTJ:
            return this.getAspectJImports();
        default:
            return null;
        }
    }
    ...
}
```

### 自定义 @Enable 模块

基于自定义注解驱动实现 - `@EnableHelloWorld`

基于接口驱动实现 - `@EnableServer`

`HelloWorldImportSelector `-> `HelloWorldConfiguration `-> `HelloWorld`

## Spring 条件装配

从 Spring Framework 3.1 开始，允许在 Bean 装配时增加前置条件判断

| Spring 注解  | 场景说明       | 起始版本 |
| ------------ | -------------- | -------- |
| @Profile     | 配置化条件装配 | 3.1      |
| @Conditional | 编程条件装配   | 4.0      |

### 实现方式

- 配置的方式 @Profile
- 编程的方式 @Conditional

### 自定义条件装配

**基于配置方式实现 @Profile**

计算服务，多整数求和 sum

```java
@SpringBootApplication(scanBasePackages = "com.liumulin.service")
public class CalculateServiceBootstrap {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(CalculateServiceBootstrap.class)
                .web(WebApplicationType.NONE)
                .profiles("Java8")
                .run(args);
        CalculateService calculateService = ctx.getBean(CalculateService.class);
        Integer sum = calculateService.sum(1, 2, 3, 4, 5, 6, 7, 8, 9);
        System.out.println("sum = " + sum);
        ctx.close();
    }
}
```

```java
@Profile("Java8")
@Service
public class Java8CalculateService implements CalculateService {
    @Override
    public Integer sum(Integer... values) {
        System.out.println("Java 8 Lambda 实现");
        return Stream.of(values).reduce(0, Integer::sum);
    }
}
```

**基于编程方式实现 @ConditionalOnSystemProperty**

```java
public class ConditionalOnSystemPropertyBootstrap {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ConditionalOnSystemPropertyBootstrap.class)
                .web(WebApplicationType.NONE)
                .run(args);

        String helloWorld = context.getBean("helloWorld", String.class);
        System.out.println("helloWorld = " + helloWorld);
        context.close();
    }

    @Bean
    @ConditionalOnSystemProperty(name = "user.name", value = "Daniel")
    public String helloWorld() {
        return "Hello World condition";
    }
}
```

```java
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
```

```java
public class OnSystemPropertyCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnSystemProperty.class.getName());
        String propertyName = String.valueOf(attributes.get("name"));
        String propertyValue = String.valueOf(attributes.get("value"));
        String javaPropertyValue = SystemProperties.get(propertyName);
        return propertyValue.equals(javaPropertyValue);
    }
}
```

# 三、理解 SpringApplication

## SpringApplication 基本使用

[19. SpringApplication](https://docs.spring.io/spring-boot/docs/1.0.0.RC5/reference/html/boot-features-spring-application.html)

### 普通 SpringApplication 运行

```java
SpringApplication.run(DiveInSpringBootApplication.class, args)
```

### 自定义 SpringApplication 运行

通过 SpringApplication API 调整

```java
SpringApplication springApplication = new SpringApplication(DiveInSpringBootApplication.class);
springApplication.setBannerMode(Banner.Mode.CONSOLE);
springApplication.setWebApplicationType(WebApplicationType.NONE);
springApplication.setAdditionalProfiles("prod");
springApplication.setHeadless(true);
```

通过 SpringApplicationBuilder API 调整

```java
new SpringApplicationBuilder(DiveInSpringBootApplication.class)
	.bannerMode(Banner.Mode.CONSOLE)
	.web(WebApplicationType.NONE)
	.profiles("prod")
	.headless(true)
	.run(args);
```

## SpringApplication 准备阶段

### 配置 Spring Boot Bean 源

Java 配置 Class 或 XML 上下文配置文件集合，用于 Spring Boot 的 `BeanDefinitionLoader` 读取，并将配置源解析加载为 Spring Bean 定义

> 数量：一个或多个以上

**Java 配置 Class**

用于 Spring 注解驱动中的 Java 配置类，大多数情况是 Spring 模式注解所标注的类，如 `@Configuration` 。

**XML 上下文配置文件**

用于 Spring 传统配置驱动中的 XML 文件

### 推断 Web 引用类型

根据当前应用 ClassPath 中是否存在相关实现类来推断 Web 应用的类型，包括：

- Web Reactive：`WebApplicationType.REATIVE`
- Web Servlet：`WebApplicationType.SERVLET`
- 非 Web：`WebApplicationType.NONE`

参考方法：`org.springframework.boot.WebApplicationType#deduceFromClasspath`

```java
static WebApplicationType deduceFromClasspath() {
	if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) && !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
			&& !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
		return WebApplicationType.REACTIVE;
	}
	for (String className : SERVLET_INDICATOR_CLASSES) {
		if (!ClassUtils.isPresent(className, null)) {
			return WebApplicationType.NONE;
		}
	}
	return WebApplicationType.SERVLET;
}
```

### 推断引导类（Main Class）

根据 Main 线程执行堆栈判断实际的引导类  

参考方法：`org.springframework.boot.SpringApplication#deduceMainApplicationClass`

```java
private Class<?> deduceMainApplicationClass() {
	try {
		StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
		for (StackTraceElement stackTraceElement : stackTrace) {
			if ("main".equals(stackTraceElement.getMethodName())) {
				return Class.forName(stackTraceElement.getClassName());
			}
		}
	}
	catch (ClassNotFoundException ex) {
		// Swallow and continue
	}
	return null;
}
```

### 加载应用上下文初始器（"backquote"ApplicationContextInitializer"backquote"）

利用 Spring 工厂加载机制，实例化 ApplicationContextInitializer 实现类，并排序对象集合。

实现：

```java
private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
    ClassLoader classLoader = getClassLoader();
    // Use names and ensure unique to protect against duplicates
    Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
    List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
    AnnotationAwareOrderComparator.sort(instances);
    return instances;
}
```

技术：

- 实现类：`org.springframework.core.io.support.SpringFactoriesLoader`
- 配置资源：`META-INF/spring.factories`
- 排序：`org.springframework.core.annotation.AnnotationAwareOrderComparator#sort(java.util.List<?>)`

### 加载应用事件监听器（"backquote"ApplicationListener"backquote"）

利用 Spring 工厂加载机制，实例化 ApplicationListener 实现类，并排序对象集合

## SpringApplication 运行阶段















































