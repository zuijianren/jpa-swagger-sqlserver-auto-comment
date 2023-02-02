# jpa-swagger-sqlserver-auto-comment

使用jpa时, 根据swagger注解为sqlserver数据表生成注释

## 快速使用

## 使用项目

* 项目中需要使用jpa框架
* 项目中使用Swagger提供相关字段的注释

> 即 需要有 swagger 和 jpa 的依赖才可以正常使用

### 导入依赖

```xml

<dependency>
    <groupId>com.zuijianren</groupId>
    <artifactId>jpa-swagger-sqlserver-auto-comment</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 添加配置

```properties
# 开启添加注释功能
auto-comment.sqlserver.comment.auto-add=true
# 展示用于添加注释的sql
auto-comment.sqlserver.comment.show-sql=true
# 指定需要添加的地方
auto-comment.sqlserver.comment.entityPackages[0]=com.zuijianren.entity
auto-comment.sqlserver.comment.entityPackages[1]=com.zuijianren.entity2
```