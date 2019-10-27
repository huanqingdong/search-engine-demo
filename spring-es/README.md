## 项目简介

提供全文检索后台接口和前端展示页面

## 用法

1. 在application.yml中配置你es的连接地址、用户名、密码
```yml
spring:
  elasticsearch:
    rest:
      # es连接地址
      uris: http://192.168.1.14:9200
      # 用户名
      username: search
      # 密码
      password: 123456
```

2. 启动`app.SpringEsApplication`的main方法
```java
@SpringBootApplication
public class SpringEsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringEsApplication.class, args);
    }

}
```
3. 访问`http://localhost:8080/search/index`


 