# jpa-swagger-sqlserver-auto-comment

使用jpa时, 根据swagger注解为sqlserver数据表生成注释

## 原理

**由于sqlserver的语法问题, 无法直接更改原建表语句sql来生成注释**

**因此只能自己去扫描实体类, 生成添加注释的sql, 并执行, 使得jpa生成的表及字段有注释**

## 快速使用

## 使用项目

* 项目中需要使用jpa框架
* 项目中使用Swagger提供相关字段的注释

> 即 需要有 swagger 和 jpa 的依赖才可以正常使用

>
> tips: 需要添加扫描路径, 确保 `com.zuijianren.jpaSwaggerSqlserverAutoComment.core` 文件可以被扫描到, 否则不会执行

### 安装依赖

> 由于本身只是一个小工具包, 因此并未上传 maven 仓库, 需要下载后手动安装到本地仓库才能使用

```cmd
mvn install:install-file  -DgroupId=com.zuijianren -DartifactId=jpa-swagger-sqlserver-auto-comment -Dversion=1.0.0 -Dpackaging=jar -Dfile=jpa-swagger-sqlserver-auto-comment-1.0.0.jar
```

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