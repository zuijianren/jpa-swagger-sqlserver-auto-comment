package com.zuijianren.jpaSwaggerSqlserverAutoComment.core;

import io.swagger.annotations.ApiModelProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SqlServer 注释自动初始化
 *
 * @author 姜辞旧
 * @date 2023/1/29 9:56
 */
@Component
public class SqlServerCommentAutoInitializing implements SmartInitializingSingleton {

    private final EntityManager entityManager;

    private final SqlServerCommentConfig sqlServerCommentConfig;

    private static final Logger log = LoggerFactory.getLogger(SqlServerCommentAutoInitializing.class);

    public SqlServerCommentAutoInitializing(EntityManager entityManager, SqlServerCommentConfig sqlServerCommentConfig) {
        this.entityManager = entityManager;
        this.sqlServerCommentConfig = sqlServerCommentConfig;
    }

    // 缓存解析到的类
    private static final Set<Class<?>> CLASS_CONTAINER = new HashSet<>();

    @Override
    public void afterSingletonsInstantiated() {
        // bean实例化完成后  执行 (此时 jpa 已完成 表的自动创建)
        if (!sqlServerCommentConfig.isAutoAdd()) {
            return;
        }
        try {
            log.info("SqlServer字段注释添加开始...");
            createComment();
            log.info("SqlServer字段注释添加结束...");
        } catch (Exception exception) {
            log.error("SqlServer注释添加失败", exception);
        }
    }

    // 创建注释
    private void createComment() {
        doScan(sqlServerCommentConfig.getEntityPackages());
        doParse();
    }

    // 解析实体类
    private void doParse() {
        for (Class<?> aClass : CLASS_CONTAINER) {
            // 获取 @Entity 标识的类
            if (!aClass.isAnnotationPresent(Entity.class)) {
                continue;
            }
            log.info("添加 " + aClass.getSimpleName() + " 表注释");
            String tableName = getTableName(aClass);
            parseEntity(aClass, tableName);
        }
    }

    /**
     * 解析实体类  并添加注释
     *
     * @param aClass    实体类
     * @param tableName 表名
     */
    private void parseEntity(Class<?> aClass, String tableName) {
        Class<?> superclass = aClass.getSuperclass();
        if (!superclass.equals(Object.class)) {
            parseEntity(superclass, tableName);
        }
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            // 为使用 ApiModelProperty 注解 并且 不存在 Transient 注解 的属性生成注释
            if (declaredField.isAnnotationPresent(ApiModelProperty.class) && !declaredField.isAnnotationPresent(Transient.class)) {
                String comment = getComment(declaredField);
                String declaredFieldName = declaredField.getName();

                /*
                 IF ((SELECT COUNT(*) FROM ::fn_listextendedproperty('MS_Description',
                'SCHEMA', N'dbo',
                'TABLE', N'T_TEMP_batch_InputPond',
                'COLUMN', N'failReason')) > 0)

                  EXEC sp_updateextendedproperty
                'MS_Description', N'审核失败原因2',
                'SCHEMA', N'dbo',
                'TABLE', N'T_TEMP_batch_InputPond',
                'COLUMN', N'failReason'

                ELSE

                  EXEC sp_addextendedproperty
                'MS_Description', N'审核失败原因2',
                'SCHEMA', N'dbo',
                'TABLE', N'T_TEMP_batch_InputPond',
                'COLUMN', N'failReason'
                 */
                StringBuilder builder = new StringBuilder("");

                addIfSql(builder, tableName, declaredFieldName);
                addIfTrueSql(builder, tableName, declaredFieldName, comment);
                addIfFalseSql(builder, tableName, declaredFieldName, comment);

                String commentSql = builder.toString();

                // 展示 sql
                if (sqlServerCommentConfig.isShowSql()) {
                    log.info("commentSql: \r\n" + commentSql);
                }

                try {
                    entityManager
                            .createNativeQuery(commentSql)
                            .getSingleResult();
                } catch (Exception exception) {
                    // nothing
                }
            }
        }
    }

    // 添加 修改注释语句
    private void addIfTrueSql(StringBuilder builder, String tableName, String declaredFieldName, String comment) {
        builder
                .append("EXEC sp_updateextendedproperty \n")
                .append("'MS_Description', N'").append(comment).append("',").append("\n")
                .append("'SCHEMA', N'dbo',\n")
                .append("'TABLE', N'").append(tableName).append("',").append("\n")
                .append("'COLUMN', N'").append(declaredFieldName).append("'").append("\n");
    }

    // 添加 添加注释语句
    private void addIfFalseSql(StringBuilder builder, String tableName, String declaredFieldName, String comment) {
        builder
                .append("ELSE\n")
                .append("EXEC sp_addextendedproperty\n")
                .append("'MS_Description', N'").append(comment).append("',").append("\n")
                .append("'SCHEMA', N'dbo',\n")
                .append("'TABLE', N'").append(tableName).append("',").append("\n")
                .append("'COLUMN', N'").append(declaredFieldName).append("'").append("\n");
    }

    // 添加 条件判断语句
    private void addIfSql(StringBuilder builder, String tableName, String declaredFieldName) {
        /*
             IF ((SELECT COUNT(*) FROM ::fn_listextendedproperty('MS_Description',
                'SCHEMA', N'dbo',
                'TABLE', N'T_TEMP_batch_InputPond',
                'COLUMN', N'failReason')) > 0)
         */
        builder
                .append("IF ((SELECT COUNT(*) FROM ::::fn_listextendedproperty('MS_Description',\n")
                .append("'SCHEMA', N'dbo',\n")
                .append("'TABLE', N'").append(tableName).append("',\n")
                .append("'COLUMN', N'").append(declaredFieldName).append("')) > 0)\n");
    }

    /**
     * 扫描指定包
     *
     * @param basePackage 指定包
     */
    private void doScan(String basePackage) {
        // 获取 需要 添加注解的实体类
        URL url = SqlServerCommentAutoInitializing.class.getClassLoader().getResource("");
        assert url != null;
        String path = url.getPath();
        String entityFilePath = basePackage.replaceAll("\\.", "/");
        try {
            path = URLDecoder.decode(path, "utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("url编码异常");
        }
        File file = new File(path + entityFilePath);
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File childFile : files) {
            if (childFile.isDirectory()) {
                doScan(basePackage + "." + childFile.getName());
            } else if (childFile.getName().endsWith(".class")) {
                String className = basePackage + "." + childFile.getName().substring(0, childFile.getName().lastIndexOf(".class"));
                try {
                    CLASS_CONTAINER.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    log.error("未找到对应的类. " + className, e);
                }
            }
        }
    }

    /**
     * 扫描指定包
     *
     * @param entityPackages 包集合
     */
    private void doScan(List<String> entityPackages) {
        for (String entityPackage : entityPackages) {
            doScan(entityPackage);
        }
    }


    // 获取表名
    private String getTableName(Class<?> aClass) {
        Table table = aClass.getAnnotation(Table.class);
        String tableName = table.name();
        if (tableName.isEmpty()) {
            tableName = aClass.getSimpleName();
        }
        return tableName;
    }

    // 获取属性注释
    private String getComment(Field declaredField) {
        ApiModelProperty apiModelProperty = declaredField.getAnnotation(ApiModelProperty.class);
        return apiModelProperty.value();
    }


}
