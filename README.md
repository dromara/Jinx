happylifeplat-netty
================

旺生活平台采用netty取代tomcat做http服务的实现


#### Spring-boot用户

* 首先引起jar包

```
<dependency>
    <groupId>com.happylife.netty</groupId>
     <artifactId>happylife-netty</artifactId>
      <version>1.0-SNAPSHOT</version>
</dependency>

```  
 * 在spring-boot的Application类中加上如下代码：
```
@Bean
public EmbeddedServletContainerFactory servletContainer(){
  NettyContainerConfig nettyContainerConfig = new NettyContainerConfig();
  NettyEmbeddedServletContainerFactory factory = new NettyEmbeddedServletContainerFactory(nettyContainerConfig);
  return factory;
}

```